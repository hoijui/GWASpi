/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.threadbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.gwaspi.progress.AbstractProgressListener;
import org.gwaspi.progress.ProcessStatusChangeEvent;

public class TaskQueue {

	private static final TaskQueue SINGLETON = new TaskQueue();

	private final List<TaskQueueListener> taskListeners;
	/**
	 * All the tasks in original queue order.
	 */
	private final List<Task> tasks;
	/**
	 * The tasks awaiting to be scheduled for processing.
	 */
	private final Queue<Task> queued;
	/**
	 * The tasks currently being processed.
	 */
	private final List<Task> scheduled;
	/**
	 * The tasks that already ended.
	 */
	private final List<Task> done;
//	private final Map<Future, Task> futureToTask;
	private final Map<Task, Future> taskToFuture;
	private final Map<Task, Integer> taskToIndex;
	private final Map<Task, TaskQueueProgressListener> taskToProgressListener;
	private final ExecutorService executorService;
	private final TaskDependencyHandler dependencyHandler;
	private final Lock queueLock;
	private final Lock scheduleLock;
	private final Lock doneLock;

	public static TaskQueue getInstance() { // HACK ugly singleton
		return SINGLETON;
	}

	private class TaskQueueProgressListener extends AbstractProgressListener {

		private final Task task;

		TaskQueueProgressListener(final Task task) {

			this.task = task;
		}

		@Override
		public void statusChanged(ProcessStatusChangeEvent evt) {

			if (evt.getNewStatus().isEnd()) {
				// NOTE This would cause a ConcurrentModificationException
				//   in AbstractProgressSource#fireStatusChanged(),
				//   if it was not for the precaution in that very method.
				task.getProgressSource().removeProgressListener(this);
				done(task);
			}
		}
	}

	private static class TaskThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable runnable) {
			return new Thread(runnable, "Tasks executor");
		}
	}

	private TaskQueue() {

		this.taskListeners = new ArrayList<TaskQueueListener>();
		this.tasks = new LinkedList<Task>();
		this.queued = new LinkedList<Task>(); // TODO maybe rather use an other implementation here
		this.scheduled = new LinkedList<Task>();
		this.done = new LinkedList<Task>();
//		this.futureToTask = new HashMap<Future, Task>();
		this.taskToFuture = new HashMap<Task, Future>();
		this.taskToIndex = new HashMap<Task, Integer>();
		this.taskToProgressListener = new HashMap<Task, TaskQueueProgressListener>();
		this.dependencyHandler = new TaskDependencyHandler();
		this.queueLock = new ReentrantLock();
		this.scheduleLock = new ReentrantLock();
		this.doneLock = new ReentrantLock();
		this.executorService = Executors.newSingleThreadExecutor(new TaskThreadFactory());
	}

	/**
	 * @see TaskDependencyHandler#canBeDone(Task)
	 */
	public boolean canBeDone(final Task task) {
		return dependencyHandler.canBeDone(task);
	}

	/**
	 * Registers a task listener.
	 * @param tasksListener this listener will receive task-registered and -deleted events
	 */
	public void addTaskListener(TaskQueueListener tasksListener) {
		taskListeners.add(tasksListener);
	}

	public void removeTaskListener(TaskQueueListener tasksListener) {
		taskListeners.remove(tasksListener);
	}

	private List<TaskQueueListener> getTaskListeners() {
		return taskListeners;
	}

	private void fireStatusChanged(final TaskQueueStatusChangedEvent taskEvent) {

		for (TaskQueueListener tasksListener : getTaskListeners()) {
			tasksListener.taskStatusChanged(taskEvent);
		}
	}

	public void queueTask(final Task task) {

		queueLock.lock();
		try {
			final int taskIndex = tasks.size();
			tasks.add(task);
			taskToIndex.put(task, taskIndex);
			queued.add(task);
			dependencyHandler.add(task);
			final TaskQueueProgressListener progressListener = new TaskQueueProgressListener(task);
			taskToProgressListener.put(task, progressListener);
			task.getProgressSource().addProgressListener(progressListener);
			task.setStatus(QueueState.QUEUED);
			fireStatusChanged(new TaskQueueStatusChangedEvent(this, task, taskIndex));
			tryToSchedule();
		} finally {
			queueLock.unlock();
		}
	}

	private void tryToSchedule() {

		for (final Task queuedTask : queued) {
			if (dependencyHandler.canBeDone(queuedTask)) {
				schedule(queuedTask);
			}
		}
	}

	private void schedule(final Task task) {

		scheduleLock.lock();
		try {
			final boolean wasQueued = queued.remove(task);
			if (!wasQueued) {
				return;
			}
			final Future<?> taskFuture = executorService.submit(task);
//			futureToTask.put(taskFuture, task);
			taskToFuture.put(task, taskFuture);
			scheduled.add(task);
			final Integer taskIndex = taskToIndex.get(task);
			task.setStatus(QueueState.SCHEDULED);
			fireStatusChanged(new TaskQueueStatusChangedEvent(this, task, taskIndex));
		} finally {
			scheduleLock.unlock();
		}
	}

	public boolean abort(final Task task) {

		doneLock.lock();
		try {
			final Future<?> taskFuture = taskToFuture.get(task);
			if (taskFuture == null) {
				// is not scheduled
				return false;
			}
			return taskFuture.cancel(true);
		} finally {
			doneLock.unlock();
		}
	}

	private void done(final Task task) {

		doneLock.lock();
		try {
			final boolean wasScheduled = scheduled.remove(task);
			if (!wasScheduled) {
				return;
			}
//			final Future<?> taskFuture = taskToFuture.get(task);
//			futureToTask.remove(taskFuture);
			taskToFuture.remove(task);
			dependencyHandler.remove(task);
			done.add(task);
			final Integer taskIndex = taskToIndex.get(task);
			task.setStatus(QueueState.DONE);
			fireStatusChanged(new TaskQueueStatusChangedEvent(this, task, taskIndex));
		} finally {
			doneLock.unlock();
		}
	}

	public void clearDone() {

		doneLock.lock();
		try {
			tasks.removeAll(done);
			for (final Task doneTask : done) {
				final TaskQueueProgressListener progressListener = taskToProgressListener.remove(doneTask);
				doneTask.getProgressSource().removeProgressListener(progressListener);
				final Integer taskIndex = taskToIndex.remove(doneTask);
				doneTask.setStatus(QueueState.REMOVED);
				fireStatusChanged(new TaskQueueStatusChangedEvent(this, doneTask, taskIndex));
			}
			done.clear();
		} finally {
			doneLock.unlock();
		}
	}

	public List<Task> getTasks() {
		return Collections.unmodifiableList(tasks);
	}

	public int size() {
		return tasks.size();
	}

	/**
	 * Returns whether there are unfinished tasks.
	 * @return true if there are not yet done tasks in the queue, false otherwise.
	 */
	public boolean isActive() {
		return (tasks.size() > done.size());
	}
}

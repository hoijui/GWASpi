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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.gwaspi.progress.AbstractProgressListener;
import org.gwaspi.progress.ProcessStatusChangeEvent;

public class TaskQueue {

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
	private final Map<Thread, Task> threadToTask;
	private final Map<Task, Thread> taskToThread;
	private final Map<Task, TaskQueueProgressListener> taskToProgressListener;
	private final TaskDependencyHandler dependencyHandler;
	private final Lock queueLock;
	private final Lock scheduleLock;
	private final Lock doneLock;

	private class TaskQueueProgressListener extends AbstractProgressListener {

		private final Task task;

		TaskQueueProgressListener(final Task task) {

			this.task = task;
		}

		@Override
		public void statusChanged(ProcessStatusChangeEvent evt) {

			if (evt.getNewStatus().isEnd()) {
				task.getProgressSource().removeProgressListener(this); // XXX This will probably throw or at least cause later: a concurrent modification exception
				done(task);
			}
		}
	}

	public TaskQueue() {

		this.taskListeners = new ArrayList<TaskQueueListener>();
		this.tasks = new LinkedList<Task>();
		this.queued = new LinkedList<Task>(); // TODO maybe rather use an other implementation here
		this.scheduled = new LinkedList<Task>();
		this.done = new LinkedList<Task>();
		this.threadToTask = new HashMap<Thread, Task>();
		this.taskToThread = new HashMap<Task, Thread>();
		this.taskToProgressListener = new HashMap<Task, TaskQueueProgressListener>();
		this.dependencyHandler = new TaskDependencyHandler();
		this.queueLock = new ReentrantLock();
		this.scheduleLock = new ReentrantLock();
		this.doneLock = new ReentrantLock();
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
			tasks.add(task);
			queued.add(task);
			dependencyHandler.add(task);
			final TaskQueueProgressListener progressListener = new TaskQueueProgressListener(task);
			taskToProgressListener.put(task, progressListener);
			task.getProgressSource().addProgressListener(progressListener);
			fireStatusChanged(new TaskQueueStatusChangedEvent(this, task));
		} finally {
			queueLock.unlock();
		}
	}

	private void schedule(final Task task) {

		scheduleLock.lock();
		try {
			final boolean wasQueued = queued.remove(task);
			if (!wasQueued) {
				return;
			}
			final Thread thread = new Thread(task);
			threadToTask.put(thread, task);
			taskToThread.put(task, thread);
			scheduled.add(task);
			fireStatusChanged(new TaskQueueStatusChangedEvent(this, task));
			thread.start();
		} finally {
			scheduleLock.unlock();
		}
	}

	private void done(final Task task) {

		doneLock.lock();
		try {
			final boolean wasScheduled = scheduled.remove(task);
			if (!wasScheduled) {
				return;
			}
			final Thread thread = taskToThread.get(task);
			threadToTask.remove(thread);
			taskToThread.remove(task);
			dependencyHandler.remove(task);
			done.add(task);
			fireStatusChanged(new TaskQueueStatusChangedEvent(this, task));
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
				fireStatusChanged(new TaskQueueStatusChangedEvent(this, doneTask));
			}
			done.clear();
		} finally {
			doneLock.unlock();
		}
	}
}

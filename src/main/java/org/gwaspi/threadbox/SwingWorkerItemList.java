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
import java.util.List;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.StudyKey;

public class SwingWorkerItemList {

	private static List<SwingWorkerItem> swingWorkerItems = new ArrayList<SwingWorkerItem>();
	private static List<Integer> parentStudyIds = new ArrayList<Integer>();
	private static List<Integer> parentMatricesIds = new ArrayList<Integer>();
	private static List<Integer> parentOperationsIds = new ArrayList<Integer>();
	private static List<TaskQueueListener> taskListeners = new ArrayList<TaskQueueListener>();

	private SwingWorkerItemList() {
	}

	/**
	 * Registers a task listener.
	 * @param tasksListener this listener will receive task-registered and -deleted events
	 */
	public static void addTaskListener(TaskQueueListener tasksListener) {
		taskListeners.add(tasksListener);
	}

	public static void removeTaskListener(TaskQueueListener tasksListener) {
		taskListeners.remove(tasksListener);
	}

	static List<TaskQueueListener> getTaskListeners() {
		return taskListeners;
	}

	static void fireTaskRegistered(final TaskQueueStatusChangedEvent taskEvent) {

		for (TaskQueueListener tasksListener : getTaskListeners()) {
			tasksListener.taskStatusChanged(taskEvent);
		}
	}

	private static boolean startNow(final SwingWorkerItem swingWorkerItem) {

		boolean started = false;

		if (swingWorkerItem.getQueueState().equals(QueueState.QUEUED)) {
			final Thread thread = new Thread(swingWorkerItem.getTask(), swingWorkerItem.getTask().getName());
			thread.start();
			swingWorkerItem.setQueueState(QueueState.PROCESSING);
			started = true;
		}

		return started;
	}

	public static void add(SwingWorkerItem swi) {

		SwingDeleterItemList.purgeDoneDeletes();
		swingWorkerItems.add(swi);

		// LOCK PARENT ITEMS
		final TaskLockProperties taskLockProperties = swi.getTask().getTaskLockProperties();
		parentStudyIds.addAll(taskLockProperties.getStudyIds());
		parentMatricesIds.addAll(taskLockProperties.getMatricesIds());
		parentOperationsIds.addAll(taskLockProperties.getOperationsIds());

		// CHECK IF ANY ITEM IS ALLREADY RUNNING
		boolean kickStart = true;
		for (SwingWorkerItem currentSwi : swingWorkerItems) {
			if (currentSwi.getQueueState().equals(QueueState.PROCESSING)) {
				kickStart = false;
			}
		}

		// START PROCESSING NEWLY ADDED SwingWorker
		if (kickStart) {
			startNow(swi);
		}

		final TaskQueueStatusChangedEvent taskEvent = new TaskQueueStatusChangedEvent(null, swi, swi.getTask().getProgressSource());
		fireTaskRegistered(taskEvent);
	}

	public static void startNext() {
		boolean started = false;
		for (SwingWorkerItem currentSwi : swingWorkerItems) {
			started = startNow(currentSwi);
			if (started) {
				break;
			}
		}
		if (!started) {
			SwingDeleterItemList.deleteAllListed(); // This will also update the tree
		}
	}

	public static List<SwingWorkerItem> getItems() {
		return swingWorkerItems;
	}

	public static SwingWorkerItem getItemByTimeStamp(String timeStamp) {

		SwingWorkerItem result = null;

		for (SwingWorkerItem currentSwi : swingWorkerItems) {
			if (currentSwi.getTimeStamp().equals(timeStamp)) {
				result = currentSwi;
			}
		}

		return result;
	}

	public static SwingWorkerItem getItemByIndex(int rowIdx) {

		SwingWorkerItem swi = null;

		SwingWorkerItem currentSwi = swingWorkerItems.get(rowIdx);
		if (currentSwi.isCurrent()) {
			swi = currentSwi;
		}

		return swi;
	}

	private static void flagItem(final SwingWorkerItem currentSwi, QueueState newState) {

		if (!QueueState.isFinalizingState(newState)) {
			throw new RuntimeException("Can not flag as " + newState.name());
		}

		if (currentSwi != null) {
			currentSwi.setQueueState(newState);

			unlockParentItems(currentSwi);
		}
	}

	public static void flagItemDone(final SwingWorkerItem item) {
		flagItem(item, QueueState.DONE);
	}

	public static void flagItemAborted(final SwingWorkerItem item) {
		flagItem(item, QueueState.ABORT);
		unlockParentItems(item);
	}

	public static void flagItemError(final SwingWorkerItem item) {
		flagItem(item, QueueState.ERROR);
	}

	public static boolean permitsDeletionOf(StudyKey studyKey) {
		return !parentStudyIds.contains(studyKey.getId());
	}

	public static boolean permitsDeletionOf(MatrixKey matrixKey) {
		return !parentMatricesIds.contains(matrixKey.getMatrixId());
	}

	public static boolean permitsDeletionOf(OperationKey operationKey) {
		return !parentOperationsIds.contains(operationKey.getId());
	}

	public static void unlockParentItems(SwingWorkerItem swi) {

		// NOTE We can not use removeAll, because we only want to remove each ID once.
		// It might still be locked a second time by an other thread.
		final TaskLockProperties taskLockProperties = swi.getTask().getTaskLockProperties();
		parentStudyIds.removeAll(taskLockProperties.getStudyIds());
		parentMatricesIds.removeAll(taskLockProperties.getMatricesIds());
		parentOperationsIds.removeAll(taskLockProperties.getOperationsIds());
	}

	public static int size() {
		return swingWorkerItems.size();
	}

	public static int sizePending() {

		int numPending = 0;

		for (SwingWorkerItem swi : SwingWorkerItemList.getItems()) {
			if (swi.isCurrent()) {
				numPending++;
			}
		}

		return numPending;
	}
}

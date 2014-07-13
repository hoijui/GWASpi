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
	private static List<TasksListener> taskListeners = new ArrayList<TasksListener>();

	private SwingWorkerItemList() {
	}

	/**
	 * Registers a task listener.
	 * @param tasksListener this listener will receive task-registered and -deleted events
	 */
	public static void addTaskListener(TasksListener tasksListener) {
		taskListeners.add(tasksListener);
	}

	public static void removeTaskListener(TasksListener tasksListener) {
		taskListeners.remove(tasksListener);
	}

	static List<TasksListener> getTaskListeners() {
		return taskListeners;
	}

	static void fireTaskRegistered(CommonRunnable task) {

		final TaskEvent taskEvent = new TaskEvent(task);
		for (TasksListener tasksListener : getTaskListeners()) {
			tasksListener.taskRegistered(taskEvent);
		}
	}

	public static void add(SwingWorkerItem swi) {

		SwingDeleterItemList.purgeDoneDeletes();
		swingWorkerItems.add(swi);

		// LOCK PARENT ITEMS
		parentStudyIds.addAll(swi.getParentStudyIds());
		parentMatricesIds.addAll(swi.getParentMatricesIds());
		parentOperationsIds.addAll(swi.getParentOperationsIds());

		// CHECK IF ANY ITEM IS ALLREADY RUNNING
		boolean kickStart = true;
		for (SwingWorkerItem currentSwi : swingWorkerItems) {
			if (currentSwi.getQueueState().equals(QueueState.PROCESSING)) {
				kickStart = false;
			}
		}

		// START PROCESSING NEWLY ADDED SwingWorker
		if (kickStart) {
			Thread thread = new Thread(swi.getTask(), swi.getTask().getThreadName());
			thread.start();
			swi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
			swi.setQueueState(QueueState.PROCESSING);
		}

		fireTaskRegistered(swi.getTask());
	}

	public static void startNext() {
		boolean started = false;
		for (SwingWorkerItem currentSwi : swingWorkerItems) {
			if (currentSwi.getQueueState().equals(QueueState.QUEUED)) {
				Thread thread = new Thread(currentSwi.getTask(), currentSwi.getTask().getThreadName());
				thread.start();
				currentSwi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
				currentSwi.setQueueState(QueueState.PROCESSING);
				started = true;
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

	private static SwingWorkerItem getCurrentItemByTimeStamp(String timeStamp) {

		SwingWorkerItem swi = null;

		SwingWorkerItem swiTmp = getItemByTimeStamp(timeStamp);
		if ((swiTmp != null) && swiTmp.isCurrent()) {
			swi = swiTmp;
		}

		return swi;
	}

	private static SwingWorkerItem getCurrentItemByIndex(int rowIdx) {

		SwingWorkerItem swi = null;

		SwingWorkerItem currentSwi = swingWorkerItems.get(rowIdx);
		if (currentSwi.isCurrent()) {
			swi = currentSwi;
		}

		return swi;
	}

	private static void flagCurrentItem(String timeStamp, QueueState newState) {

		if (!QueueState.isFinalizingState(newState)) {
			throw new RuntimeException("Can not flag as " + newState.name());
		}

		SwingWorkerItem currentSwi = getCurrentItemByTimeStamp(timeStamp);
		if (currentSwi != null) {
			currentSwi.setQueueState(newState);
			currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

			unlockParentItems(currentSwi);
		}
	}

	public static void flagCurrentItemDone(String timeStamp) {
		flagCurrentItem(timeStamp, QueueState.DONE);
	}

	public static void flagCurrentItemAborted(String timeStamp) {
		flagCurrentItem(timeStamp, QueueState.ABORT);
	}

	public static void flagCurrentItemAborted(int rowIdx) {
		SwingWorkerItem currentSwi = getCurrentItemByIndex(rowIdx);
		if (currentSwi != null) {
			currentSwi.setQueueState(QueueState.ABORT);

			unlockParentItems(currentSwi);
		}
	}

	public static void flagCurrentItemError(String timeStamp) {
		flagCurrentItem(timeStamp, QueueState.ERROR);
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
		for (Integer studyId : swi.getParentStudyIds()) {
			parentStudyIds.remove(studyId);
		}
		for (Integer matrixId : swi.getParentMatricesIds()) {
			parentMatricesIds.remove(matrixId);
		}
		for (Integer operationId : swi.getParentOperationsIds()) {
			parentOperationsIds.remove(operationId);
		}
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

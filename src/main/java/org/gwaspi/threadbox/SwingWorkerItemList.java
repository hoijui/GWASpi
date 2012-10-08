package org.gwaspi.threadbox;

import org.gwaspi.gui.ProcessTab;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SwingWorkerItemList {

	private static List<SwingWorkerItem> swingWorkerItems = new ArrayList<SwingWorkerItem>();
	private static List<Integer> parentStudyIds = new ArrayList<Integer>();
	private static List<Integer> parentMatricesIds = new ArrayList<Integer>();
	private static List<Integer> parentOperationsIds = new ArrayList<Integer>();

	private SwingWorkerItemList() {
	}

	public static void add(SwingWorkerItem swi)
	{
		SwingDeleterItemList.purgeDoneDeletes();
		SwingWorkerItemList.swingWorkerItems.add(swi);

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
			swi.getSwingWorker().start();
			swi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
			swi.setQueueState(QueueState.PROCESSING);
		}
	}

	public static void startNext() {
		boolean started = false;
		for (SwingWorkerItem currentSwi : swingWorkerItems) {
			if (currentSwi.getQueueState().equals(QueueState.QUEUED)) {
				currentSwi.getSwingWorker().start();
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

	public static List<SwingWorkerItem> getSwingWorkerItems() {
		return swingWorkerItems;
	}

	private static SwingWorkerItem getCurrentItemByTimeStamp(String timeStamp) {

		SwingWorkerItem swi = null;

		SwingWorkerItem swiTmp = getSwingWorkerItemByTimeStamp(timeStamp);
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

	public static void flagCurrentItemDone(String timeStamp) {
		SwingWorkerItem currentSwi = getCurrentItemByTimeStamp(timeStamp);
		if (currentSwi != null) {
			currentSwi.setQueueState(QueueState.DONE);
			currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

			unlockParentItems(currentSwi);
		}
	}

	public static void flagCurrentItemAborted(String timeStamp) {
		SwingWorkerItem currentSwi = getCurrentItemByTimeStamp(timeStamp);
		if (currentSwi != null) {
			currentSwi.setQueueState(QueueState.ABORT);
			currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

			unlockParentItems(currentSwi);
		}
	}

	public static void flagCurrentItemAborted(int rowIdx) {
		SwingWorkerItem currentSwi = getCurrentItemByIndex(rowIdx);
		if (currentSwi != null) {
			currentSwi.setQueueState(QueueState.ABORT);
			ProcessTab.getSingleton().updateProcessOverview();

			unlockParentItems(currentSwi);
		}
	}

	public static void flagCurrentItemError(String timeStamp) {
		SwingWorkerItem currentSwi = getCurrentItemByTimeStamp(timeStamp);
		if (currentSwi != null) {
			currentSwi.setQueueState(QueueState.ERROR);
			currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

			unlockParentItems(currentSwi);
		}
	}

	public static boolean permitsDeletionOfStudyId(int studyId) {
		return !parentStudyIds.contains(studyId);
	}

	public static boolean permitsDeletionOfMatrixId(int matrixId) {
		return !parentMatricesIds.contains(matrixId);
	}

	public static boolean permitsDeletionOfOperationId(int opId) {
		return !parentOperationsIds.contains(opId);
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

		for (SwingWorkerItem swi : SwingWorkerItemList.getSwingWorkerItems()) {
			if (swi.isCurrent()) {
				numPending++;
			}
		}

		return numPending;
	}

	public static SwingWorkerItem getSwingWorkerItemByTimeStamp(String timeStamp) {

		SwingWorkerItem result = null;

		for (SwingWorkerItem currentSwi : swingWorkerItems) {
			if (currentSwi.getTimeStamp().equals(timeStamp)) {
				result = currentSwi;
			}
		}

		return result;
	}
}

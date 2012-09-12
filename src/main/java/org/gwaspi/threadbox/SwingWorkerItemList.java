package org.gwaspi.threadbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SwingWorkerItemList {

	private static List<SwingWorkerItem> swingWorkerItemsAL = new ArrayList<SwingWorkerItem>();
	private static List<Integer> parentStudyIds = new ArrayList<Integer>();
	private static List<Integer> parentMatricesIds = new ArrayList<Integer>();
	private static List<Integer> parentOperationsIds = new ArrayList<Integer>();

	SwingWorkerItemList() {
	}

	public void add(SwingWorkerItem swi,
			Integer[] _parentStudyId,
			Integer[] _parentMatricesIds,
			Integer[] _parentOperationsIds)
	{
		SwingDeleterItemList.purgeDoneDeletes();
		SwingWorkerItemList.swingWorkerItemsAL.add(swi);

		// LOCK PARENT ITEMS
		if (_parentStudyId != null) {
			parentStudyIds.addAll(Arrays.asList(_parentStudyId));
		}
		if (_parentMatricesIds != null) {
			parentMatricesIds.addAll(Arrays.asList(_parentMatricesIds));
		}
		if (_parentOperationsIds != null) {
			parentOperationsIds.addAll(Arrays.asList(_parentOperationsIds));
		}

		// CHECK IF ANY ITEM IS ALLREADY RUNNING
		boolean kickStart = true;
		for (SwingWorkerItem currentSwi : swingWorkerItemsAL) {
			if (currentSwi.queueState.equals(QueueStates.PROCESSING)) {
				kickStart = false;
			}
		}

		// START PROCESSING NEWLY ADDED SwingWorker
		if (kickStart) {
			swi.getSwingWorker().start();
			swi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
			swi.setQueueState(QueueStates.PROCESSING);
		}
	}

	public static void startNext() {
		boolean idle = true;
		for (SwingWorkerItem currentSwi : swingWorkerItemsAL) {
			if (idle && currentSwi.getQueueState().equals(QueueStates.QUEUED)) {
				idle = false;
				currentSwi.swingWorker.start();
				currentSwi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
				currentSwi.setQueueState(QueueStates.PROCESSING);
			}
		}
		if (idle) {
			SwingDeleterItemList.deleteAllListed(); //This will also update the tree
		}
	}

	public static List<SwingWorkerItem> getSwingWorkerItemsAL() {
		return swingWorkerItemsAL;
	}

	public static void flagCurrentItemDone(String timeStamp) {
		for (SwingWorkerItem currentSwi : swingWorkerItemsAL) {
			if (currentSwi.getTimeStamp().equals(timeStamp)) {
				currentSwi.setQueueState(QueueStates.DONE);
				currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

				unlockParentItems(currentSwi.parentStudyIds,
						currentSwi.parentMatricesIds,
						currentSwi.parentOperationsIds);
			}
		}
	}

	public static void flagCurrentItemAborted(String timeStamp) {
		for (SwingWorkerItem currentSwi : swingWorkerItemsAL) {
			if (currentSwi.getTimeStamp().equals(timeStamp)) {
				currentSwi.setQueueState(QueueStates.ABORT);
				currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

				unlockParentItems(currentSwi.parentStudyIds,
						currentSwi.parentMatricesIds,
						currentSwi.parentOperationsIds);
			}
		}
	}

	public static void flagCurrentItemAborted(int rowIdx) {
		SwingWorkerItem currentSwi = swingWorkerItemsAL.get(rowIdx);
		String queueState = currentSwi.getQueueState();
		if (queueState.equals(QueueStates.PROCESSING) || queueState.equals(QueueStates.QUEUED)) {
			swingWorkerItemsAL.get(rowIdx).setQueueState(QueueStates.ABORT);
			org.gwaspi.gui.ProcessTab.updateProcessOverview();

			unlockParentItems(currentSwi.parentStudyIds,
					currentSwi.parentMatricesIds,
					currentSwi.parentOperationsIds);
		}
	}

	public static void flagCurrentItemError(String timeStamp) {
		for (SwingWorkerItem currentSwi : swingWorkerItemsAL) {
			if (currentSwi.getTimeStamp().equals(timeStamp)) {
				currentSwi.setQueueState(QueueStates.ERROR);
				currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

				unlockParentItems(currentSwi.parentStudyIds,
						currentSwi.parentMatricesIds,
						currentSwi.parentOperationsIds);
			}
		}
	}

	public static boolean permitsDeletion(Integer studyId, Integer matrixId, Integer opId) {
		boolean result = true;
		if (studyId != null && parentStudyIds.contains(studyId)) {
			result = false;
		}
		if (matrixId != null && parentMatricesIds.contains(matrixId)) {
			result = false;
		}
		if (opId != null && parentOperationsIds.contains(opId)) {
			result = false;
		}
		return result;
	}

	public static void unlockParentItems(Integer[] studyIds, Integer[] matrixIds, Integer[] opIds) {
		if (studyIds != null) {
			for (Integer id : studyIds) {
				parentStudyIds.remove(id);
			}
		}
		if (matrixIds != null) {
			for (Integer id : matrixIds) {
				parentMatricesIds.remove(id);
			}
		}
		if (opIds != null) {
			for (Integer id : opIds) {
				parentOperationsIds.remove(id);
			}
		}
	}

	public static int getSwingWorkerItemsALsize() {
		return swingWorkerItemsAL.size();
	}

	public static int getSwingWorkerPendingItemsNb() {
		int result = 0;
		for (SwingWorkerItem currentSwi : SwingWorkerItemList.getSwingWorkerItemsAL()) {
			if (currentSwi.queueState.equals(QueueStates.PROCESSING)) {
				result++;
			}
			if (currentSwi.queueState.equals(QueueStates.QUEUED)) {
				result++;
			}
		}
		return result;
	}

	public static SwingWorkerItem getSwingWorkerItemByTimeStamp(String timeStamp) {
		SwingWorkerItem result = null;
		for (SwingWorkerItem currentSwi : swingWorkerItemsAL) {
			if (currentSwi.getTimeStamp().equals(timeStamp)) {
				result = currentSwi;
			}
		}
		return result;
	}
}

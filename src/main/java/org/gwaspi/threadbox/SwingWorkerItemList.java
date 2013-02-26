package org.gwaspi.threadbox;

import java.util.ArrayList;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gwaspi.gui.ProcessTab;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
@Component
@Service(SwingWorkerItemList.class)
//@References({
//	@Reference(
//			name = "canvas",
//			policy = ReferencePolicy.DYNAMIC,
//			referenceInterface = Canvas.class,
////			cardinality = ReferenceCardinality.MANDATORY_UNARY)//,
//			cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)//,
//})
public class SwingWorkerItemList {

	private List<SwingWorkerItem> swingWorkerItems = new ArrayList<SwingWorkerItem>();
	private List<Integer> parentStudyIds = new ArrayList<Integer>();
	private List<Integer> parentMatricesIds = new ArrayList<Integer>();
	private List<Integer> parentOperationsIds = new ArrayList<Integer>();
	@Reference
	private SwingDeleterItemList swingDeleterItemList;

	protected void bindSwingDeleterItemList(SwingDeleterItemList swingDeleterItemList) {
		this.swingDeleterItemList = swingDeleterItemList;
	}

	protected void unbindSwingDeleterItemList(SwingDeleterItemList swingDeleterItemList) {

		if (this.swingDeleterItemList == swingDeleterItemList) {
			this.swingDeleterItemList = null;
		}
	}

	public void add(SwingWorkerItem swi)
	{
		swingDeleterItemList.purgeDoneDeletes();
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
	}

	public void startNext() {
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
			swingDeleterItemList.deleteAllListed(); // This will also update the tree
		}
	}

	public List<SwingWorkerItem> getItems() {
		return swingWorkerItems;
	}

	public SwingWorkerItem getItemByTimeStamp(String timeStamp) {

		SwingWorkerItem result = null;

		for (SwingWorkerItem currentSwi : swingWorkerItems) {
			if (currentSwi.getTimeStamp().equals(timeStamp)) {
				result = currentSwi;
			}
		}

		return result;
	}

	private SwingWorkerItem getCurrentItemByTimeStamp(String timeStamp) {

		SwingWorkerItem swi = null;

		SwingWorkerItem swiTmp = getItemByTimeStamp(timeStamp);
		if ((swiTmp != null) && swiTmp.isCurrent()) {
			swi = swiTmp;
		}

		return swi;
	}

	private SwingWorkerItem getCurrentItemByIndex(int rowIdx) {

		SwingWorkerItem swi = null;

		SwingWorkerItem currentSwi = swingWorkerItems.get(rowIdx);
		if (currentSwi.isCurrent()) {
			swi = currentSwi;
		}

		return swi;
	}

	private void flagCurrentItem(String timeStamp, QueueState newState) {

		if (QueueState.isFinalizingState(newState)) {
			throw new RuntimeException("Can not flag as " + newState.name());
		}

		SwingWorkerItem currentSwi = getCurrentItemByTimeStamp(timeStamp);
		if (currentSwi != null) {
			currentSwi.setQueueState(newState);
			currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

			unlockParentItems(currentSwi);
		}
	}

	public void flagCurrentItemDone(String timeStamp) {
		flagCurrentItem(timeStamp, QueueState.DONE);
	}

	public void flagCurrentItemError(String timeStamp) {
		flagCurrentItem(timeStamp, QueueState.ERROR);
	}

	public void flagCurrentItemAborted(int rowIdx) {
		SwingWorkerItem currentSwi = getCurrentItemByIndex(rowIdx);
		if (currentSwi != null) {
			currentSwi.setQueueState(QueueState.ABORT);
			ProcessTab.getSingleton().updateProcessOverview();

			unlockParentItems(currentSwi);
		}
	}

	public boolean permitsDeletionOfStudyId(int studyId) {
		return !parentStudyIds.contains(studyId);
	}

	public boolean permitsDeletionOfMatrixId(int matrixId) {
		return !parentMatricesIds.contains(matrixId);
	}

	public boolean permitsDeletionOfOperationId(int opId) {
		return !parentOperationsIds.contains(opId);
	}

	public void unlockParentItems(SwingWorkerItem swi) {

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

	public int size() {
		return swingWorkerItems.size();
	}

	public int sizePending() {

		int numPending = 0;

		for (SwingWorkerItem swi : getItems()) {
			if (swi.isCurrent()) {
				numPending++;
			}
		}

		return numPending;
	}
}

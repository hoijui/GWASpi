package org.gwaspi.threadbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gwaspi.global.Text;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.ProcessTab;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.gui.utils.CursorUtils;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.StudyList;
import org.gwaspi.threadbox.SwingDeleterItem.DeleteTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
@Component
@Service(SwingDeleterItemList.class)
public class SwingDeleterItemList {

	private static final Logger log = LoggerFactory.getLogger(SwingDeleterItemList.class);

	private List<SwingDeleterItem> swingDeleterItems = new ArrayList<SwingDeleterItem>();
	@Reference
	private SwingWorkerItemList swingWorkerItemList;
	@Reference
	private MultiOperations multiOperations;

	protected void bindSwingWorkerItemList(SwingWorkerItemList swingWorkerItemList) {
		this.swingWorkerItemList = swingWorkerItemList;
	}

	protected void unbindSwingWorkerItemList(SwingWorkerItemList swingWorkerItemList) {

		if (this.swingWorkerItemList == swingWorkerItemList) {
			this.swingWorkerItemList = null;
		}
	}

	protected void bindMultiOperations(MultiOperations multiOperations) {
		this.multiOperations = multiOperations;
	}

	protected void unbindMultiOperations(MultiOperations multiOperations) {

		if (this.multiOperations == multiOperations) {
			this.multiOperations = null;
		}
	}

	public void add(SwingDeleterItem sdi) {

		//purgeDoneDeletes();
		boolean addMe = true;
		for (SwingDeleterItem allreadySdi : swingDeleterItems) {
			if (allreadySdi.getStudyId() == sdi.getStudyId()
					&& allreadySdi.getMatrixId() == sdi.getMatrixId()
					&& allreadySdi.getOpId() == sdi.getOpId()) {
				addMe = false;
			}
		}
		if (addMe) {
			// Add at start of list
			swingDeleterItems.add(swingDeleterItems.size(), sdi);
		}

		// CHECK IF ANY ITEM IS RUNNING, START PROCESSING NEWLY ADDED SwingDeleter
		if (swingWorkerItemList.sizePending() == 0) {
			deleteAllListed();
		}
	}

	public void deleteAllListed() {
		if (StartGWASpi.guiMode) {
			StartGWASpi.mainGUIFrame.setCursor(CursorUtils.waitCursor);
		}

		for (SwingDeleterItem currentSdi : swingDeleterItems) {
			if (currentSdi.getQueueState().equals(QueueState.QUEUED)) {
				DeleteTarget deleteTarget = currentSdi.getDeleteTarget();

				// DELETE STUDY
				if (deleteTarget.equals(DeleteTarget.STUDY)) {
					try {
						currentSdi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
						currentSdi.setQueueState(QueueState.PROCESSING);

						StudyList.deleteStudy(currentSdi.getStudyId(), currentSdi.isDeleteReports());
						MultiOperations.printCompleted("deleting Study ID: " + currentSdi.getStudyId());

						GWASpiExplorerNodes.deleteStudyNode(currentSdi.getStudyId());
						flagCurrentItemDeleted();
					} catch (IOException ex) {
						log.error(null, ex);
					}
				}
				// DELETE MATRIX
				if (deleteTarget.equals(DeleteTarget.MATRIX)) {
					currentSdi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
					currentSdi.setQueueState(QueueState.PROCESSING);

					MatricesList.deleteMatrix(currentSdi.getMatrixId(), currentSdi.isDeleteReports());
					MultiOperations.printCompleted("deleting Matrix ID:" + currentSdi.getMatrixId());

					GWASpiExplorerNodes.deleteMatrixNode(currentSdi.getMatrixId());
					flagCurrentItemDeleted();
				}
				// DELETE OPERATION BY OPID
				if (deleteTarget.equals(DeleteTarget.OPERATION_BY_OPID)) {
					try {
						currentSdi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
						currentSdi.setQueueState(QueueState.PROCESSING);

						OperationsList.deleteOperationBranch(currentSdi.getStudyId(), currentSdi.getOpId(), currentSdi.isDeleteReports());
						MultiOperations.printCompleted("deleting Operation ID: " + currentSdi.getOpId());

						GWASpiExplorerNodes.deleteOperationNode(currentSdi.getOpId());
						flagCurrentItemDeleted();
					} catch (IOException ex) {
						log.error(null, ex);
					}
				}
				// DELETE REPORTS BY MATRIX-ID -- NOT IN USE!
				if (deleteTarget.equals(DeleteTarget.REPORTS_BY_MATRIXID)) {
					try {
						currentSdi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
						currentSdi.setQueueState(QueueState.PROCESSING);

						ReportsList.deleteReportByMatrixId(currentSdi.getMatrixId());
						MultiOperations.printCompleted("deleting Reports from Matrix ID: " + currentSdi.getMatrixId());

						flagCurrentItemDeleted();
					} catch (IOException ex) {
						log.error(null, ex);
					}
				}
			}
		}

		// IF WE ARE IN GUI MODE, UPDATE TREE. ELSE EXIT PROGRAM
		if (StartGWASpi.guiMode) {
			StartGWASpi.mainGUIFrame.setCursor(CursorUtils.defaultCursor);
			ProcessTab.getSingleton().updateProcessOverview();
			try {
				multiOperations.updateTreeAndPanel();
			} catch (IOException ex) {
				log.warn(null, ex);
			}
			GWASpiExplorerPanel.getSingleton().setAllNodesCollapsable();
		} else {
			log.info(Text.Cli.done);
		}
	}

	public List<SwingDeleterItem> getItems() {
		return swingDeleterItems;
	}

	/** @deprecated unused! */
	private void flagCurrentItemAborted() {
		flagCurrentItemEnd(QueueState.ABORT);
	}

	/** @deprecated unused! */
	private void flagCurrentItemError() {
		flagCurrentItemEnd(QueueState.ERROR);
	}

	private void flagCurrentItemDeleted() {
		flagCurrentItemEnd(QueueState.DELETED);
	}

	private void flagCurrentItemEnd(QueueState endQueueState) {
		for (SwingDeleterItem currentSdi : swingDeleterItems) {
			if (currentSdi.getQueueState().equals(QueueState.PROCESSING)) {
				currentSdi.setQueueState(endQueueState);
				currentSdi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
				break;
			}
		}
	}

	public int size() {
		return swingDeleterItems.size();
	}

	public int sizePending() {

		int numPending = 0;

		for (SwingDeleterItem currentSdi : getItems()) {
			if (currentSdi.isCurrent()) {
				numPending++;
			}
		}

		return numPending;
	}

	public void purgeDoneDeletes() {
		for (int i = swingDeleterItems.size() - 1; i >= 0; i--) {
			if (swingDeleterItems.get(i).getQueueState() == QueueState.DELETED) {
				swingDeleterItems.remove(i);
			}
		}
	}

	public void abortSwingWorker(int idx) {
		SwingDeleterItem sdi = swingDeleterItems.get(idx);
		if (sdi.isCurrent()) {
			sdi.setQueueState(QueueState.ABORT);

			log.info("");
			log.info(Text.Processes.abortingProcess);
			log.info(sdi.getDescription());
			log.info("Delete Launch Time: {}", sdi.getLaunchTime());
			log.info("");
			ProcessTab.getSingleton().updateProcessOverview();
		}
	}
}

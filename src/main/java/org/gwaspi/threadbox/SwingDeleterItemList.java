package org.gwaspi.threadbox;

import org.gwaspi.global.Text;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.ProcessTab;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.gui.utils.CursorUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.OperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.threadbox.SwingDeleterItem.DeleteTarget;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SwingDeleterItemList extends SwingWorkerItemList {

	private final static Logger log = LoggerFactory.getLogger(SwingDeleterItemList.class);
	protected static List<SwingDeleterItem> swingDeleterItemsAL = new ArrayList<SwingDeleterItem>();

	SwingDeleterItemList() {
	}

	public void add(SwingDeleterItem sdi) {

		//SwingDeleterItemList.purgeDoneDeletes();
		boolean addMe = true;
		for (SwingDeleterItem allreadySdi : swingDeleterItemsAL) {
			if (allreadySdi.getStudyId() == sdi.getStudyId()
					&& allreadySdi.getMatrixId() == sdi.getMatrixId()
					&& allreadySdi.getOpId() == sdi.getOpId()) {
				addMe = false;
			}
		}
		if (addMe) {
			SwingDeleterItemList.swingDeleterItemsAL.add(SwingDeleterItemList.swingDeleterItemsAL.size(), sdi); //Add at start of list
		}

		// CHECK IF ANY ITEM IS RUNNING, START PROCESSING NEWLY ADDED SwingDeleter
		if (SwingWorkerItemList.getSwingWorkerPendingItemsNb() == 0) {
			deleteAllListed();
		}
	}

	public static void deleteAllListed() {
		if (StartGWASpi.guiMode) {
			StartGWASpi.mainGUIFrame.setCursor(CursorUtils.waitCursor);
		}

		for (SwingDeleterItem currentSdi : swingDeleterItemsAL) {
			if (currentSdi.queueState.equals(QueueState.QUEUED)) {
				DeleteTarget deleteTarget = currentSdi.getDeleteTarget();

				// DELETE STUDY
				if (deleteTarget.equals(DeleteTarget.STUDY)) {
					try {
						currentSdi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
						currentSdi.setQueueState(QueueState.PROCESSING);

						org.gwaspi.database.StudyGenerator.deleteStudy(currentSdi.getStudyId(), currentSdi.isDeleteReports());
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

					org.gwaspi.netCDF.matrices.MatrixManager.deleteMatrix(currentSdi.getMatrixId(), currentSdi.isDeleteReports());
					MultiOperations.printCompleted("deleting Matrix ID:" + currentSdi.getMatrixId());

					GWASpiExplorerNodes.deleteMatrixNode(currentSdi.getMatrixId());
					flagCurrentItemDeleted();
				}
				// DELETE OPERATION BY OPID
				if (deleteTarget.equals(DeleteTarget.OPERATION_BY_OPID)) {
					try {
						currentSdi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
						currentSdi.setQueueState(QueueState.PROCESSING);

						OperationManager.deleteOperationBranch(currentSdi.getStudyId(), currentSdi.getOpId(), currentSdi.isDeleteReports());
						MultiOperations.printCompleted("deleting Operation ID: " + currentSdi.getOpId());

						GWASpiExplorerNodes.deleteOperationNode(currentSdi.getOpId());
						flagCurrentItemDeleted();
					} catch (IOException ex) {
						log.error(null, ex);
					}
				}
				// DELETE REPORTS BY MATRIXID -- NOT IN USAGE???
				if (deleteTarget.equals(DeleteTarget.REPORTS_BY_MATRIXID)) {
					try {
						currentSdi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
						currentSdi.setQueueState(QueueState.PROCESSING);

						org.gwaspi.reports.ReportManager.deleteReportByMatrixId(currentSdi.getMatrixId());
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
				MultiOperations.updateTreeAndPanel();
			} catch (IOException ex) {
				log.warn(null, ex);
			}
			GWASpiExplorerPanel.getSingleton().setAllNodesCollapsable();
		} else {
			log.info(Text.Cli.doneExiting);
			StartGWASpi.exit();
		}
	}

	public static List<SwingDeleterItem> getSwingDeleterItems() {
		return swingDeleterItemsAL;
	}

	public static void flagCurrentItemAborted() {
		boolean idle = false;
		for (SwingDeleterItem currentSdi : swingDeleterItemsAL) {
			if (!idle && currentSdi.queueState.equals(QueueState.PROCESSING)) {
				idle = true;
				currentSdi.setQueueState(QueueState.ABORT);
				currentSdi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
			}
		}
	}

	public static void flagCurrentItemError() {
		boolean idle = false;
		for (SwingDeleterItem currentSdi : swingDeleterItemsAL) {
			if (!idle && currentSdi.queueState.equals(QueueState.PROCESSING)) {
				idle = true;
				currentSdi.setQueueState(QueueState.ERROR);
				currentSdi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
			}
		}
	}

	public static void flagCurrentItemDeleted() {
		boolean idle = false;
		for (SwingDeleterItem currentSdi : swingDeleterItemsAL) {
			if (!idle && currentSdi.queueState.equals(QueueState.PROCESSING)) {
				idle = true;
				currentSdi.setQueueState(QueueState.DELETED);
				currentSdi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
			}
		}
	}

	public static int getSwingDeleterItemsALsize() {
		return swingDeleterItemsAL.size();
	}

	public static int getSwingDeleterPendingItemsNb() {
		int result = 0;
		for (SwingDeleterItem currentSdi : SwingDeleterItemList.getSwingDeleterItems()) {
			if (currentSdi.queueState.equals(QueueState.PROCESSING)) {
				result++;
			}
			if (currentSdi.queueState.equals(QueueState.QUEUED)) {
				result++;
			}
		}
		return result;
	}

	public static void purgeDoneDeletes() {
		for (int i = swingDeleterItemsAL.size(); i > 0; i--) {
			if (swingDeleterItemsAL.get(i - 1).getQueueState().equals(QueueState.DELETED)) {
				swingDeleterItemsAL.remove(i - 1);
			}
		}
	}

	public static void abortSwingWorker(int idx) {
		QueueState queueState = swingDeleterItemsAL.get(idx).getQueueState();
		if (queueState.equals(QueueState.PROCESSING) || queueState.equals(QueueState.QUEUED)) {
			swingDeleterItemsAL.get(idx).setQueueState(QueueState.ABORT);

			log.info("");
			log.info(Text.Processes.abortingProcess);
			log.info(swingDeleterItemsAL.get(idx).getDescription());
			log.info("Delete Launch Time: {}", swingDeleterItemsAL.get(idx).getLaunchTime());
			log.info("");
			ProcessTab.getSingleton().updateProcessOverview();
		}
	}
}

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
import org.gwaspi.netCDF.matrices.MatrixManager;
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
public class SwingDeleterItemList {

	private static final Logger log = LoggerFactory.getLogger(SwingDeleterItemList.class);
	private static List<SwingDeleterItem> swingDeleterItems = new ArrayList<SwingDeleterItem>();

	private SwingDeleterItemList() {
	}

	public static void add(SwingDeleterItem sdi) {

		//SwingDeleterItemList.purgeDoneDeletes();
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
		if (SwingWorkerItemList.sizePending() == 0) {
			deleteAllListed();
		}
	}

	public static void deleteAllListed() {
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

					MatrixManager.deleteMatrix(currentSdi.getMatrixId(), currentSdi.isDeleteReports());
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
				// DELETE REPORTS BY MATRIX-ID -- NOT IN USE!
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
		return swingDeleterItems;
	}

	/** @deprecated unused! */
	private static void flagCurrentItemAborted() {
		flagCurrentItemEnd(QueueState.ABORT);
	}

	/** @deprecated unused! */
	private static void flagCurrentItemError() {
		flagCurrentItemEnd(QueueState.ERROR);
	}

	private static void flagCurrentItemDeleted() {
		flagCurrentItemEnd(QueueState.DELETED);
	}

	private static void flagCurrentItemEnd(QueueState endQueueState) {
		for (SwingDeleterItem currentSdi : swingDeleterItems) {
			if (currentSdi.getQueueState().equals(QueueState.PROCESSING)) {
				currentSdi.setQueueState(endQueueState);
				currentSdi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
				break;
			}
		}
	}

	public static int size() {
		return swingDeleterItems.size();
	}

	public static int sizePending() {

		int numPending = 0;

		for (SwingDeleterItem currentSdi : getSwingDeleterItems()) {
			if (currentSdi.isCurrent()) {
				numPending++;
			}
		}

		return numPending;
	}

	public static void purgeDoneDeletes() {
		for (int i = swingDeleterItems.size() - 1; i >= 0; i--) {
			if (swingDeleterItems.get(i).getQueueState() == QueueState.DELETED) {
				swingDeleterItems.remove(i);
			}
		}
	}

	public static void abortSwingWorker(int idx) {
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

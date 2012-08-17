package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import java.io.File;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.MatrixDataExtractor_opt;
import org.gwaspi.netCDF.operations.OP_QAMarkers_opt;
import org.gwaspi.netCDF.operations.OP_QASamples_opt;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_ExtractMatrix implements Runnable {

	private Thread runner;
	private String timeStamp = "";
	private static int resultMatrixId;
	private static int studyId;
	private static int parentMatrixId;
	private static String newMatrixName;
	private static String description;
	private static cNetCDF.Defaults.SetMarkerPickCase markerPickCase;
	private static cNetCDF.Defaults.SetSamplePickCase samplePickCase;
	private static String markerPickVar;
	private static String samplePickVar;
	private static HashSet markerCriteria;
	private static HashSet sampleCriteria;
	private static File markerCriteriaFile;
	private static File sampleCriteriaFile;

	public Threaded_ExtractMatrix(String threadName,
			String _timeStamp,
			int _studyId,
			int _parentMatrixId,
			String _newMatrixName,
			String _description,
			cNetCDF.Defaults.SetMarkerPickCase _markerPickCase,
			cNetCDF.Defaults.SetSamplePickCase _samplePickCase,
			String _markerPickVar,
			String _samplePickVar,
			HashSet _markerCriteria,
			HashSet _sampleCriteria,
			File _markerCriteriaFile,
			File _sampleCriteriaFile) {
		try {
			timeStamp = _timeStamp;
			org.gwaspi.global.Utils.sysoutStart("Extracting");
			org.gwaspi.global.Config.initPreferences(false, null);

			studyId = _studyId;
			parentMatrixId = _parentMatrixId;
			newMatrixName = _newMatrixName;
			description = _description;
			markerPickCase = _markerPickCase;
			samplePickCase = _samplePickCase;
			markerPickVar = _markerPickVar;
			samplePickVar = _samplePickVar;
			markerCriteria = _markerCriteria;
			sampleCriteria = _sampleCriteria;
			markerCriteriaFile = _markerCriteriaFile;
			sampleCriteriaFile = _sampleCriteriaFile;

			runner = new Thread(this, threadName); // (1) Create a new thread.
			runner.start(); // (2) Start the thread.
			runner.join();

		} catch (InterruptedException ex) {
			//Logger.getLogger(Threaded_ExtractMatrix.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@SuppressWarnings("static-access")
	public void run() {
		SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

		try {

			if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
				MatrixDataExtractor_opt exMatrix = new MatrixDataExtractor_opt(studyId,
						parentMatrixId,
						newMatrixName,
						description,
						markerPickCase,
						samplePickCase,
						markerPickVar,
						samplePickVar,
						markerCriteria,
						sampleCriteria,
						Integer.MIN_VALUE, //Filter pos, not used now
						markerCriteriaFile,
						sampleCriteriaFile);
				resultMatrixId = exMatrix.extractGenotypesToNewMatrix();
				GWASpiExplorerNodes.insertMatrixNode(studyId, resultMatrixId);
			}

			if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
				int sampleQAOpId = OP_QASamples_opt.processMatrix(resultMatrixId);
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, sampleQAOpId);
				org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
			}

			if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
				int markersQAOpId = OP_QAMarkers_opt.processMatrix(resultMatrixId);
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
				org.gwaspi.reports.OutputQAMarkers_opt.writeReportsForQAMarkersData(markersQAOpId);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
				MultiOperations.printCompleted("Matrix Quality Control");
			}

			//FINISH OFF
			if (!thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.ABORT)) {
				MultiOperations.printFinished("Extracting Data");
				MultiOperations.swingWorkerItemList.flagCurrentItemDone(timeStamp);
			} else {
				System.out.println("\n");
				System.out.println(Text.Processes.abortingProcess);
				System.out.println("Process Name: " + thisSwi.getSwingWorkerName());
				System.out.println("Process Launch Time: " + thisSwi.getLaunchTime());
				System.out.println("\n\n");
			}

			MultiOperations.updateProcessOverviewStartNext();

		} catch (OutOfMemoryError e) {
			System.out.println(Text.App.outOfMemoryError);
		} catch (Exception ex) {
			Logger.getLogger(Threaded_ExtractMatrix.class.getName()).log(Level.SEVERE, null, ex);
			MultiOperations.printError("Extracting Data");
			try {
				MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
				MultiOperations.updateTree();
				MultiOperations.updateProcessOverviewStartNext();
			} catch (Exception ex1) {
			}
		}
	}
}

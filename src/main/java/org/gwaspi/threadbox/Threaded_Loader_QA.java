package org.gwaspi.threadbox;

import org.gwaspi.global.Text;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.loader.LoadManager;
import org.gwaspi.netCDF.operations.OP_QAMarkers_opt;
import org.gwaspi.netCDF.operations.OP_QASamples_opt;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_Loader_QA implements Runnable {

	private Thread runner;
	private String timeStamp = "";
	private static int resultMatrixId;
	private static int resultOpId;
	private static String format;
	private static LinkedHashMap sampleInfoLHM;
	private static String newMatrixName;
	private static String newMatrixDescription;
	private static String file1;
	private static String fileSampleInfo;
	private static String file2;
	private static String chromosome;
	private static String strandType;
	private static String gtCode;
	private static int studyId;

	public Threaded_Loader_QA(String threadName,
			String _timeStamp,
			String _format,
			LinkedHashMap _sampleInfoLHM,
			String _newMatrixName,
			String _newMatrixDescription,
			String _file1,
			String _fileSampleInfo,
			String _file2,
			String _chromosome,
			String _strandType,
			String _gtCode,
			int _studyId) {
		try {
			timeStamp = _timeStamp;

			org.gwaspi.global.Utils.sysoutStart("Loading Genotypes");
			org.gwaspi.global.Config.initPreferences(false, null);

			format = _format;
			sampleInfoLHM = _sampleInfoLHM;
			newMatrixName = _newMatrixName;
			newMatrixDescription = _newMatrixDescription;
			file1 = _file1;
			fileSampleInfo = _fileSampleInfo;
			file2 = _file2;
			chromosome = _chromosome;
			strandType = _strandType;
			gtCode = _gtCode;
			studyId = _studyId;

			runner = new Thread(this, threadName); // (1) Create a new thread.
			runner.start(); // (2) Start the thread.
			runner.join();

		} catch (InterruptedException ex) {
			//Logger.getLogger(Threaded_Loader_QA.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@SuppressWarnings("static-access")
	public void run() {
		try {
			SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

			if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
				resultMatrixId = LoadManager.dispatchLoadByFormat(format,
						sampleInfoLHM,
						newMatrixName,
						newMatrixDescription,
						file1,
						fileSampleInfo,
						file2,
						chromosome,
						strandType,
						gtCode,
						studyId);

				MultiOperations.printCompleted("Loading Genotypes");
				GWASpiExplorerNodes.insertMatrixNode(studyId, resultMatrixId);
			}



			if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
				resultOpId = OP_QASamples_opt.processMatrix(resultMatrixId);
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, resultOpId);
				org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(resultOpId, true);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(resultOpId);
			}



			if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
				resultOpId = OP_QAMarkers_opt.processMatrix(resultMatrixId);
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, resultOpId);
				org.gwaspi.reports.OutputQAMarkers_opt.writeReportsForQAMarkersData(resultOpId);
				MultiOperations.printCompleted("Matrix Quality Control");
				GWASpiExplorerNodes.insertReportsUnderOperationNode(resultOpId);
			}

			if (!thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.ABORT)) {
				MultiOperations.printFinished("Loading Genotypes");
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
			MultiOperations.printError("Loading Genotypes");
			Logger.getLogger(Threaded_Loader_QA.class.getName()).log(Level.SEVERE, null, ex);
			try {
				MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
				MultiOperations.updateTree();
				MultiOperations.updateProcessOverviewStartNext();
			} catch (Exception ex1) {
			}
		}
	}
}

package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.MatrixTranslator_opt;
import org.gwaspi.netCDF.operations.OP_QAMarkers_opt;
import org.gwaspi.netCDF.operations.OP_QASamples_opt;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_TranslateMatrix implements Runnable {

	Thread runner;
	protected String timeStamp = "";
	protected static int resultMatrixId;
	protected static int studyId;
	protected static int parentMatrixId;
	protected static cNetCDF.Defaults.GenotypeEncoding gtEncoding;
	protected static String newMatrixName;
	protected static String description;

	public Threaded_TranslateMatrix(String threadName,
			String _timeStamp,
			int _studyId,
			int _parentMatrixId,
			cNetCDF.Defaults.GenotypeEncoding _gtEncoding,
			String _newMatrixName,
			String _description) {
		try {
			timeStamp = _timeStamp;
			org.gwaspi.global.Utils.sysoutStart("Translating Matrix");
			org.gwaspi.global.Config.initPreferences(false, null);

			studyId = _studyId;
			parentMatrixId = _parentMatrixId;
			gtEncoding = _gtEncoding;
			newMatrixName = _newMatrixName;
			description = _description;

			runner = new Thread(this, threadName); // (1) Create a new thread.
			runner.start(); // (2) Start the thread.
			runner.join();
		} catch (InterruptedException ex) {
			//Logger.getLogger(Threaded_TranslateMatrix.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@SuppressWarnings("static-access")
	public void run() {
		SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

		try {

			if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
				MatrixTranslator_opt matrixTransformer = new MatrixTranslator_opt(studyId,
						parentMatrixId,
						newMatrixName,
						description);

				if (gtEncoding.equals(cNetCDF.Defaults.GenotypeEncoding.AB0)
						|| gtEncoding.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {

					resultMatrixId = matrixTransformer.translateAB12AllelesToACGT();
				} else if (gtEncoding.equals(cNetCDF.Defaults.GenotypeEncoding.O1234)) {

					resultMatrixId = matrixTransformer.translate1234AllelesToACGT();
				}
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
				MultiOperations.printFinished("Translating Matrix");
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
			Logger.getLogger(Threaded_TranslateMatrix.class.getName()).log(Level.SEVERE, null, ex);
			MultiOperations.printError("Translating Matrix");
			try {
				MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
				MultiOperations.updateTree();
				MultiOperations.updateProcessOverviewStartNext();
			} catch (Exception ex1) {
			}
		}
	}
}

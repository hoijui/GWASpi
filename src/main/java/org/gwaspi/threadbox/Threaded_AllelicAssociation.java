package org.gwaspi.threadbox;

import org.gwaspi.global.Text;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.gwaspi.reports.OutputAllelicAssociation_opt;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_AllelicAssociation implements Runnable {

	private Thread runner;
	private String timeStamp = "";
	private static int matrixId;
	private static int censusOpId;
	private static int hwOpId;
	private static GWASinOneGOParams gwasParams;

	public Threaded_AllelicAssociation(String threadName,
			String _timeStamp,
			int _matrixId,
			int _censusOpId,
			int _hwOpId,
			GWASinOneGOParams _gwasParams) {
		try {
			timeStamp = _timeStamp;
			org.gwaspi.global.Utils.sysoutStart("Allelic Association Test");
			org.gwaspi.global.Config.initPreferences(false, null);

			matrixId = _matrixId;
			censusOpId = _censusOpId;
			hwOpId = _hwOpId;
			gwasParams = _gwasParams;

			runner = new Thread(this, threadName); // (1) Create a new thread.
			runner.start(); // (2) Start the thread.
			runner.join();
		} catch (InterruptedException ex) {
			//Logger.getLogger(Threaded_AllelicAssociation.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@SuppressWarnings("static-access")
	public void run() {
		SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

		try {

			OperationsList opList = new OperationsList(matrixId);
			int markersQAOpId = opList.getIdOfLastOperationTypeOccurance(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA);

			//<editor-fold defaultstate="collapsed" desc="ALLELICTEST PROCESS">
			if (!gwasParams.discardMarkerByMisRat) {
				gwasParams.discardMarkerMisRatVal = 1;
			}
			if (!gwasParams.discardMarkerByHetzyRat) {
				gwasParams.discardMarkerHetzyRatVal = 1;
			}
			if (!gwasParams.discardSampleByMisRat) {
				gwasParams.discardSampleMisRatVal = 1;
			}
			if (!gwasParams.discardSampleByHetzyRat) {
				gwasParams.discardSampleHetzyRatVal = 1;
			}




			//ALLELIC ALLELICTEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)

			OperationMetadata markerQAMetadata = new OperationMetadata(markersQAOpId);

			if (gwasParams.discardMarkerHWCalc) {
				gwasParams.discardMarkerHWTreshold = (double) 0.05 / markerQAMetadata.getOpSetSize();
			}

			if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
				int assocOpId = org.gwaspi.netCDF.operations.OperationManager.performCleanAllelicTests(matrixId,
						censusOpId,
						hwOpId,
						gwasParams.discardMarkerHWTreshold);
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, assocOpId);

				//////Make Reports (needs newMatrixId, QAopId, AssocOpId)
				if (assocOpId != Integer.MIN_VALUE) {
					OutputAllelicAssociation_opt.writeReportsForAssociationData(assocOpId);
					GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
				}
			}


			//</editor-fold>

			//FINISH OFF
			if (!thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.ABORT)) {
				MultiOperations.printFinished("Performing Allelic Association Study");
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
			MultiOperations.printError("Performing Allelic Association Study");
			Logger.getLogger(Threaded_AllelicAssociation.class.getName()).log(Level.SEVERE, null, ex);
			try {
				MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
				MultiOperations.updateTree();
				MultiOperations.updateProcessOverviewStartNext();
			} catch (Exception ex1) {
			}
		}
	}
}

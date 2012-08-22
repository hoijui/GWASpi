package org.gwaspi.threadbox;

import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.reports.OutputAllelicAssociation_opt;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_AllelicAssociation extends CommonRunnable {

	private int matrixId;
	private int censusOpId;
	private int hwOpId;
	private GWASinOneGOParams gwasParams;

	public Threaded_AllelicAssociation(String threadName,
			String timeStamp,
			int matrixId,
			int censusOpId,
			int hwOpId,
			GWASinOneGOParams gwasParams)
	{
		super(threadName, timeStamp, "Allelic Association Study");

		this.matrixId = matrixId;
		this.censusOpId = censusOpId;
		this.hwOpId = hwOpId;
		this.gwasParams = gwasParams;

		startInternal("Allelic Association Test");
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_AllelicAssociation.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		OperationsList opList = new OperationsList(matrixId);
		int markersQAOpId = opList.getIdOfLastOperationTypeOccurance(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA);

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

		// ALLELIC ALLELICTEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)

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

			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
			if (assocOpId != Integer.MIN_VALUE) {
				OutputAllelicAssociation_opt.writeReportsForAssociationData(assocOpId);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
			}
		}
	}
}

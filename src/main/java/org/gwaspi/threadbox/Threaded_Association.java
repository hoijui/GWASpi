package org.gwaspi.threadbox;

import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.reports.OutputAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_Association extends CommonRunnable {

	private final int matrixId;
	private final int censusOpId;
	private final int hwOpId;
	private final GWASinOneGOParams gwasParams;
	private final boolean allelic;

	public Threaded_Association(
			int matrixId,
			int censusOpId,
			int hwOpId,
			GWASinOneGOParams gwasParams,
			boolean allelic)
	{
		super(
				(allelic ? "Allelic" : "Genotypic") + " Association Test",
				(allelic ? "Allelic" : "Genotypic") + " Association Study",
				(allelic ? "Allelic" : "Genotypic") + " Association Test on Matrix ID: " + matrixId,
				(allelic ? "Allelic" : "Genotypic") + " Association Test");

		this.matrixId = matrixId;
		this.censusOpId = censusOpId;
		this.hwOpId = hwOpId;
		this.gwasParams = gwasParams;
		this.allelic = allelic;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Association.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<Operation> operations = OperationsList.getOperationsList(matrixId);
		int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

		if (!gwasParams.isDiscardMarkerByMisRat()) {
			gwasParams.setDiscardMarkerMisRatVal(1);
		}
		if (!gwasParams.isDiscardMarkerByHetzyRat()) {
			gwasParams.setDiscardMarkerHetzyRatVal(1);
		}
		if (!gwasParams.isDiscardSampleByMisRat()) {
			gwasParams.setDiscardSampleMisRatVal(1);
		}
		if (!gwasParams.isDiscardSampleByHetzyRat()) {
			gwasParams.setDiscardSampleHetzyRatVal(1);
		}

		// TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);

		if (gwasParams.isDiscardMarkerHWCalc()) {
			gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getOpSetSize());
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			int assocOpId = OperationManager.performCleanAssociationTests(
					matrixId,
					censusOpId,
					hwOpId,
					gwasParams.getDiscardMarkerHWTreshold(),
					allelic);
			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, assocOpId);

			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
			if (assocOpId != Integer.MIN_VALUE) {
				new OutputAssociation(allelic).writeReportsForAssociationData(assocOpId);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
			}
		}
	}
}

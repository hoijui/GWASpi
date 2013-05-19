package org.gwaspi.threadbox;

import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.reports.OutputTrendTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_TrendTest extends CommonRunnable {

	private int matrixId;
	private int censusOpId;
	private int hwOpId;
	private GWASinOneGOParams gwasParams;

	public Threaded_TrendTest(
			int matrixId,
			int censusOpId,
			int hwOpId,
			GWASinOneGOParams gwasParams)
	{
		super(
				"Cochran-Armitage Trend Test",
				"Cochran-Armitage Trend Test",
				"Cochran-Armitage Trend Test on Matrix ID: " + matrixId,
				"Cochran-Armitage Trend Test");

		this.matrixId = matrixId;
		this.censusOpId = censusOpId;
		this.hwOpId = hwOpId;
		this.gwasParams = gwasParams;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_TrendTest.class);
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

		// TREND-TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)

		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);

		if (gwasParams.isDiscardMarkerHWCalc()) {
			gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getOpSetSize());
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			int trendTestOpId = OperationManager.performCleanTrendTests(matrixId,
					censusOpId,
					hwOpId, gwasParams.getDiscardMarkerHWTreshold());
			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, trendTestOpId);

			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
			if (trendTestOpId != Integer.MIN_VALUE) {
				OutputTrendTest.writeReportsForTrendTestData(trendTestOpId);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(trendTestOpId);
			}
		}
	}
}

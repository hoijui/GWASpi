package org.gwaspi.threadbox;

import java.util.List;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.reports.OutputTrendTest;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_TrendTest extends CommonRunnable {

	private int matrixId;
	private int censusOpId;
	private int hwOpId;
	private GWASinOneGOParams gwasParams;

	public Threaded_TrendTest(String threadName,
			String timeStamp,
			int matrixId,
			int censusOpId,
			int hwOpId,
			GWASinOneGOParams gwasParams)
	{
		super(threadName, timeStamp, "Cochran-Armitage Trend Test");

		this.matrixId = matrixId;
		this.censusOpId = censusOpId;
		this.hwOpId = hwOpId;
		this.gwasParams = gwasParams;

		startInternal("Cochran-Armitage Trend Test");
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_TrendTest.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		OperationsList opList = new OperationsList(matrixId);
		List<Operation> opAL = opList.operationsListAL;
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

		// TREND-TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)

		OperationMetadata markerQAMetadata = new OperationMetadata(markersQAOpId);

		if (gwasParams.discardMarkerHWCalc) {
			gwasParams.discardMarkerHWTreshold = (double) 0.05 / markerQAMetadata.getOpSetSize();
		}

		if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
			int trendTestOpId = org.gwaspi.netCDF.operations.OperationManager.performCleanTrendTests(matrixId,
					censusOpId,
					hwOpId,
					gwasParams.discardMarkerHWTreshold);
			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, trendTestOpId);

			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
			if (trendTestOpId != Integer.MIN_VALUE) {
				OutputTrendTest.writeReportsForTrendTestData(trendTestOpId);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(trendTestOpId);
			}
		}
	}
}

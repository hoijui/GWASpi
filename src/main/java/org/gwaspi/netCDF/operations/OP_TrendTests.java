package org.gwaspi.netCDF.operations;

import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OP_TrendTests extends AbstractTestMatrixOperation {

	private final Logger log = LoggerFactory.getLogger(OP_TrendTests.class);

	public OP_TrendTests(
			int rdMatrixId,
			Operation markerCensusOP,
			Operation hwOP,
			double hwThreshold)
	{
		super(
			rdMatrixId,
			markerCensusOP,
			hwOP,
			hwThreshold,
			"Cochran-Armitage Trend Test",
			OPType.TRENDTEST);
	}

	/**
	 * Performs the Cochran-Armitage Trend Test.
	 * @param wrNcFile
	 * @param wrCaseMarkerIdSetMap
	 * @param wrCtrlMarkerSet
	 */
	@Override
	protected void performTest(NetcdfFileWriteable wrNcFile, Map<MarkerKey, Object> wrCaseMarkerIdSetMap, Map<MarkerKey, Object> wrCtrlMarkerSet) {
		// Iterate through markerset
		int markerNb = 0;
		for (Map.Entry<MarkerKey, Object> entry : wrCaseMarkerIdSetMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();

			int[] caseCntgTable = (int[]) entry.getValue();
			int[] ctrlCntgTable = (int[]) wrCtrlMarkerSet.get(markerKey);

			// INIT VALUES
			int caseAA = caseCntgTable[0];
			int caseAa = caseCntgTable[1];
			int caseaa = caseCntgTable[2];

			int ctrlAA = ctrlCntgTable[0];
			int ctrlAa = ctrlCntgTable[1];
			int ctrlaa = ctrlCntgTable[2];

			// COCHRAN ARMITAGE TREND TEST
			double armitageT = org.gwaspi.statistics.Associations.calculateChocranArmitageTrendTest(caseAA, caseAa, caseaa, ctrlAA, ctrlAa, ctrlaa, 2); //Model 2, codominant
			double armitagePval = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(armitageT, 1);  // 1 Degree of freedom

			Double[] store = new Double[7];
			store[0] = armitageT;
			store[1] = armitagePval;
			entry.setValue(store); // Re-use Map to store P-value and stuff

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers", markerNb);
			}
		}

		//<editor-fold defaultstate="expanded" desc="TREND-TEST DATA WRITER">
		int[] boxes = new int[] {0, 1};
		Utils.saveDoubleMapD2ToWrMatrix(wrNcFile, wrCaseMarkerIdSetMap, boxes, cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP);
		//</editor-fold>
	}
}

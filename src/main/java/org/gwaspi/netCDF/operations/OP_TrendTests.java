/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.netCDF.operations;

import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFileWriteable;

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
	protected void performTest(NetcdfFileWriteable wrNcFile, Map<MarkerKey, int[]> wrCaseMarkerIdSetMap, Map<MarkerKey, int[]> wrCtrlMarkerSet) {
		// Iterate through markerset
		int markerNb = 0;
		Map<MarkerKey, Double[]> result = new LinkedHashMap<MarkerKey, Double[]>(wrCaseMarkerIdSetMap.size());
		for (Map.Entry<MarkerKey, int[]> entry : wrCaseMarkerIdSetMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();

			int[] caseCntgTable = entry.getValue();
			int[] ctrlCntgTable = wrCtrlMarkerSet.get(markerKey);

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
			result.put(markerKey, store); // store P-value and stuff

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers", markerNb);
			}
		}

		//<editor-fold defaultstate="expanded" desc="TREND-TEST DATA WRITER">
		int[] boxes = new int[] {0, 1};
		Utils.saveDoubleMapD2ToWrMatrix(wrNcFile, result, boxes, cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP);
		//</editor-fold>
	}
}

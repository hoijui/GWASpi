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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Census;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.trendtest.DefaultTrendTestOperationEntry;
import org.gwaspi.operations.trendtest.TrendTestOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OP_TrendTests extends AbstractTestMatrixOperation {

	private final Logger log = LoggerFactory.getLogger(OP_TrendTests.class);

	public OP_TrendTests(
			MatrixKey rdMatrixKey,
			OperationMetadata markerCensusOP,
			OperationMetadata hwOP,
			double hwThreshold)
	{
		super(
			rdMatrixKey,
			markerCensusOP,
			hwOP,
			hwThreshold,
			"Cochran-Armitage Trend Test",
			OPType.TRENDTEST);
	}

	/**
	 * Performs the Cochran-Armitage Trend Test.
	 */
	@Override
	protected void performTest(
			OperationDataSet dataSet,
			Map<Integer, MarkerKey> caseMarkersOrigIndexKey,
			Map<Integer, Census> caseMarkersOrigIndexCensus,
			Map<Integer, MarkerKey> ctrlMarkersOrigIndexKey,
			Map<Integer, Census> ctrlMarkersOrigIndexCensus)
			throws IOException
	{
		TrendTestOperationDataSet trendTestDataSet = (TrendTestOperationDataSet) dataSet;
		((AbstractNetCdfOperationDataSet) dataSet).setNumMarkers(caseMarkersOrigIndexKey.size()); // HACK

		// Iterate through markerset
		int markerNb = 0;
		Iterator<Census> caseMarkerCensusIt = caseMarkersOrigIndexCensus.values().iterator();
		for (Map.Entry<Integer, MarkerKey> caseMarkerOrigIndexKey : caseMarkersOrigIndexKey.entrySet()) {
			Integer origIndex = caseMarkerOrigIndexKey.getKey();
			MarkerKey markerKey = caseMarkerOrigIndexKey.getValue();
			Census caseCensus = caseMarkerCensusIt.next();
			Census ctrlCensus = ctrlMarkersOrigIndexCensus.get(origIndex);

			// COCHRAN ARMITAGE TREND TEST
			double armitageT = org.gwaspi.statistics.Associations.calculateChocranArmitageTrendTest(
					caseCensus.getAA(), caseCensus.getAa(), caseCensus.getaa(),
					ctrlCensus.getAA(), ctrlCensus.getAa(), ctrlCensus.getaa(),
					2); // Model 2, codominant
			double armitagePval = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(armitageT, 1);  // 1 Degree of freedom

			trendTestDataSet.addEntry(new DefaultTrendTestOperationEntry(
					markerKey,
					origIndex,
					armitageT,
					armitagePval));

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers", markerNb);
			}
		}
	}
}

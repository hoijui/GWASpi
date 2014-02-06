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
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Census;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.trendtest.DefaultTrendTestOperationEntry;
import org.gwaspi.operations.trendtest.TrendTestOperationDataSet;

/**
 * Performs the Cochran-Armitage Trend Test.
 */
public class OP_TrendTests extends AbstractTestMatrixOperation<TrendTestOperationDataSet> {

	public OP_TrendTests(
			OperationKey markerCensusOPKey,
			OperationKey hwOPKey,
			double hwThreshold)
	{
		super(
			markerCensusOPKey,
			hwOPKey,
			hwThreshold,
			"Cochran-Armitage Trend Test");
	}

	@Override
	public OPType getType() {
		return OPType.TRENDTEST;
	}

	@Override
	protected void performTest(
			OperationDataSet dataSet,
			Map<Integer, MarkerKey> markerOrigIndicesKeys,
			List<Census> caseMarkersCensus,
			List<Census> ctrlMarkersCensus)
			throws IOException
	{
		TrendTestOperationDataSet trendTestDataSet = (TrendTestOperationDataSet) dataSet;
		((AbstractNetCdfOperationDataSet) dataSet).setNumMarkers(markerOrigIndicesKeys.size()); // HACK

		Iterator<Census> caseMarkerCensusIt = caseMarkersCensus.iterator();
		Iterator<Census> ctrlMarkersCensusIt = ctrlMarkersCensus.iterator();
		for (Map.Entry<Integer, MarkerKey> caseMarkerOrigIndexKey : markerOrigIndicesKeys.entrySet()) {
			final Integer origIndex = caseMarkerOrigIndexKey.getKey();
			final MarkerKey markerKey = caseMarkerOrigIndexKey.getValue();
			final Census caseCensus = caseMarkerCensusIt.next();
			final Census ctrlCensus = ctrlMarkersCensusIt.next();

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
		}
	}
}

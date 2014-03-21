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
import org.gwaspi.global.Text;
import org.gwaspi.model.Census;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.trendtest.DefaultTrendTestOperationEntry;
import org.gwaspi.operations.trendtest.TrendTestOperationDataSet;
import org.gwaspi.operations.trendtest.TrendTestOperationParams;
import org.gwaspi.statistics.Associations;
import org.gwaspi.statistics.Pvalue;

/**
 * Performs the Cochran-Armitage trend test.
 */
public class OP_TrendTests extends AbstractTestMatrixOperation<TrendTestOperationDataSet, TrendTestOperationParams> {

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					Text.Operation.trendTest,
					Text.Operation.trendTest); // TODO We need a more elaborate description of this operation!
	static {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationFactory.registerOperationTypeInfo(
				OP_QASamples.class,
				OPERATION_TYPE_INFO);
	}

	public OP_TrendTests(final TrendTestOperationParams params) {
		super(params);
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
		trendTestDataSet.setNumMarkers(markerOrigIndicesKeys.size());

		Iterator<Census> caseMarkerCensusIt = caseMarkersCensus.iterator();
		Iterator<Census> ctrlMarkersCensusIt = ctrlMarkersCensus.iterator();
		for (Map.Entry<Integer, MarkerKey> caseMarkerOrigIndexKey : markerOrigIndicesKeys.entrySet()) {
			final Integer origIndex = caseMarkerOrigIndexKey.getKey();
			final MarkerKey markerKey = caseMarkerOrigIndexKey.getValue();
			final Census caseCensus = caseMarkerCensusIt.next();
			final Census ctrlCensus = ctrlMarkersCensusIt.next();

			final double armitageT = Associations.calculateChocranArmitageTrendTest(
					caseCensus.getAA(),
					caseCensus.getAa(),
					caseCensus.getaa(),
					ctrlCensus.getAA(),
					ctrlCensus.getAa(),
					ctrlCensus.getaa(),
					Associations.ChocranArmitageTrendTestModel.CODOMINANT);
			final double armitagePval = Pvalue.calculatePvalueFromChiSqr(armitageT, 1);

			trendTestDataSet.addEntry(new DefaultTrendTestOperationEntry(
					markerKey,
					origIndex,
					armitageT,
					armitagePval));
		}
	}
}

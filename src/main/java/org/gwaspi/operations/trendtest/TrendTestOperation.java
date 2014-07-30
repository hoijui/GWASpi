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

package org.gwaspi.operations.trendtest;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.global.Text;
import org.gwaspi.model.Census;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.AbstractTestMatrixOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.statistics.Associations;
import org.gwaspi.statistics.Pvalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the Cochran-Armitage trend test.
 */
public class TrendTestOperation extends AbstractTestMatrixOperation<TrendTestOperationDataSet, TrendTestOperationParams> {

	private final Logger log = LoggerFactory.getLogger(TrendTestOperation.class);

	private static final ProcessInfo trendTestProcessInfo = new DefaultProcessInfo(
			Text.Operation.trendTest,
			Text.Operation.trendTest); // TODO We need a more elaborate description of this operation!

	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new TrendTestOperationFactory());
	}

	public TrendTestOperation(final TrendTestOperationParams params) {
		super(params);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return TrendTestOperationFactory.OPERATION_TYPE_INFO;
	}

//	@Override
//	public ProgressSource getProgressSource() throws IOException {
//should be gone, casue it is same as in parent, just need to export process info!
//		if (operationPH == null) {
//			final DataSetSource parentDataSetSource = getParentDataSetSource();
//			final int numMarkers = parentDataSetSource.getNumMarkers();
//
//			operationPH =  new IntegerProgressHandler(
//					trendTestProcessInfo,
//					0, // start state, first marker
//					numMarkers - 1); // end state, last marker
//		}
//
//		return operationPH;
//	}

	@Override
	public ProcessInfo getProcessInfo() {
		return trendTestProcessInfo;
	}

	@Override
	protected void performTest(
			OperationDataSet dataSet,
			Map<Integer, MarkerKey> markerOrigIndicesKeys,
			List<Census> caseMarkersCensus,
			List<Census> ctrlMarkersCensus,
			ProgressHandler rawTestPH)
			throws IOException
	{
		rawTestPH.setNewStatus(ProcessStatus.INITIALIZING);
		TrendTestOperationDataSet trendTestDataSet = (TrendTestOperationDataSet) dataSet;
		trendTestDataSet.setNumMarkers(markerOrigIndicesKeys.size());

		Iterator<Census> caseMarkerCensusIt = caseMarkersCensus.iterator();
		Iterator<Census> ctrlMarkersCensusIt = ctrlMarkersCensus.iterator();
		boolean nonInformativeMarkers = false;
		int localMarkerIndex = 0;
		rawTestPH.setNewStatus(ProcessStatus.RUNNING);
		for (Map.Entry<Integer, MarkerKey> markerOrigIndexKey : markerOrigIndicesKeys.entrySet()) {
			final Integer origIndex = markerOrigIndexKey.getKey();
			final MarkerKey markerKey = markerOrigIndexKey.getValue();
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
			if (Double.isNaN(armitageT)) {
				nonInformativeMarkers = true;
			}

			final double armitagePval = Pvalue.calculatePvalueFromChiSqr(armitageT, 1);

			rawTestPH.setProgress(localMarkerIndex);
			trendTestDataSet.addEntry(new DefaultTrendTestOperationEntry(
					markerKey,
					origIndex,
					armitageT,
					armitagePval));

			localMarkerIndex++;
		}
		rawTestPH.setNewStatus(ProcessStatus.FINALIZING);
		if (nonInformativeMarkers) {
			log.warn("There were markers baring no information (all genotypes are equal). You may consider first filtering them out.");
		}
		rawTestPH.setNewStatus(ProcessStatus.COMPLEETED);
	}
}

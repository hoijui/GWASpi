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

package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.markercensus.MarkerCensusOperation;
import org.gwaspi.operations.qamarkers.QAMarkersOperation;
import org.gwaspi.operations.qasamples.QASamplesOperation;
import org.gwaspi.operations.trendtest.TrendTestOperation;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperation;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperationParams;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.operations.qamarkers.QAMarkersOperationParams;
import org.gwaspi.operations.qasamples.QASamplesOperationParams;
import org.gwaspi.operations.trendtest.TrendTestOperationParams;
import org.gwaspi.reports.OutputTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_Combi extends CommonRunnable {

	private final CombiTestOperationParams paramsTest;
	private final ByCombiWeightsFilterOperationParams paramsFilter;

	public Threaded_Combi(
			final CombiTestOperationParams paramsTest,
			final ByCombiWeightsFilterOperationParams paramsFilter)
	{
		super(
				"Combi Association Test",
				"Combi Association Study",
				"Combi Association Test on: " + paramsTest.getParent().toString(),
				"Combi Association Test");

		this.paramsTest = paramsTest;
		this.paramsFilter = paramsFilter;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Combi.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

//		List<Operation> operations = OperationsList.getOperationsList(params.getMatrixKey().getId());
//		int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

		// XXX TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
//		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);

		OperationKey combiTestOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			combiTestOpKey = OperationManager.performRawCombiTest(paramsTest);
		}

		OperationKey combiFilterOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			paramsFilter.setParent(new DataSetKey(combiTestOpKey));
			combiFilterOpKey = OperationManager.performOperation(new ByCombiWeightsFilterOperation(paramsFilter));
		}

		OperationKey qaSamplesOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			QASamplesOperationParams samplesQAParams = new QASamplesOperationParams(new DataSetKey(combiFilterOpKey));
			qaSamplesOpKey = OperationManager.performOperation(new QASamplesOperation(samplesQAParams));
		}

		OperationKey qaMarkersOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			QAMarkersOperationParams markersQAParams = new QAMarkersOperationParams(new DataSetKey(combiFilterOpKey));
			qaMarkersOpKey = OperationManager.performOperation(new QAMarkersOperation(markersQAParams));
		}

		OperationKey markerCensusOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			MarkerCensusOperationParams markerCensusParams = new MarkerCensusOperationParams(new DataSetKey(combiFilterOpKey), qaSamplesOpKey, qaMarkersOpKey);
			markerCensusOpKey = OperationManager.performOperation(new MarkerCensusOperation(markerCensusParams));
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			TrendTestOperationParams trendTestParams = new TrendTestOperationParams(combiFilterOpKey, null, markerCensusOpKey);
			OperationKey trendTestOpKey = OperationManager.performOperation(new TrendTestOperation(trendTestParams));

			if (trendTestOpKey != null) {
				new OutputTest(trendTestOpKey, OPType.TRENDTEST, qaMarkersOpKey).writeReportsForTestData();
				GWASpiExplorerNodes.insertReportsUnderOperationNode(trendTestOpKey);
			}
		}
	}
}

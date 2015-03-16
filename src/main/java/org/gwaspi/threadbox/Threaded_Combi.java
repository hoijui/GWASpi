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

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperation;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperationParams;
import org.gwaspi.operations.combi.CombiTestMatrixOperation;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperation;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.operations.trendtest.TrendTestOperation;
import org.gwaspi.operations.trendtest.TrendTestOperationParams;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.gwaspi.reports.OutputTest;
import org.gwaspi.reports.TestOutputParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_Combi extends CommonRunnable {

	private static final ProcessInfo fullCombiProcessInfo
			= new DefaultProcessInfo("Full COMBI Test",
					"Complete COMBI Test procedure and evaluation of the results"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_COMBI_TEST = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_COMBI_TEST", null));
	private static final ProgressSource PLACEHOLDER_PS_COMBI_FILTER = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_COMBI_FILTER", null));
	private static final ProgressSource PLACEHOLDER_PS_QA = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_QA", null));
	private static final ProgressSource PLACEHOLDER_PS_MARKER_CENSUS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_MARKER_CENSUS", null));
	private static final ProgressSource PLACEHOLDER_PS_TREND_TEST = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_TREND_TEST", null));
	private static final ProgressSource PLACEHOLDER_PS_TEST_OUTPUT = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_TEST_OUTPUT", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(6);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_COMBI_TEST, 0.5);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_COMBI_FILTER, 0.1);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_QA, 0.1);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_MARKER_CENSUS, 0.1);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_TREND_TEST, 0.1);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_TEST_OUTPUT, 0.1);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final CombiTestOperationParams paramsTest;
	private final ByCombiWeightsFilterOperationParams paramsFilter;
	private final SuperProgressSource progressSource;
	private final TaskLockProperties taskLockProperties;

	public Threaded_Combi(
			final CombiTestOperationParams paramsTest,
			final ByCombiWeightsFilterOperationParams paramsFilter)
	{
		super("Combi Association Test", "on " + paramsTest.getParent().toString());

		this.paramsTest = paramsTest;
		this.paramsFilter = paramsFilter;
		this.progressSource = new SuperProgressSource(fullCombiProcessInfo, subProgressSourcesAndWeights);
		this.taskLockProperties = MultiOperations.createTaskLockProperties(paramsTest.getParent());
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressSource;
	}

	@Override
	protected ProgressHandler getProgressHandler() {
		return progressSource;
	}

	@Override
	public TaskLockProperties getTaskLockProperties() {
		return taskLockProperties;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Combi.class);
	}

	@Override
	protected void runInternal() throws IOException {

//		List<Operation> operations = OperationsList.getOperationsList(params.getMatrixKey().getId());
//		int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

		// XXX TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
//		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);

		// NOTE ABORTION_POINT We could be gracefully abort here

		progressSource.setNewStatus(ProcessStatus.RUNNING);
		final MatrixOperation combiTestOperation = new CombiTestMatrixOperation(paramsTest);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_COMBI_TEST, combiTestOperation.getProgressSource(), null);
		final OperationKey combiTestOpKey = OperationManager.performOperationCreatingOperation(combiTestOperation);

		// NOTE ABORTION_POINT We could be gracefully abort here

		paramsFilter.setParent(new DataSetKey(combiTestOpKey));
		final MatrixOperation byCombiWeightsFilterOperation = new ByCombiWeightsFilterOperation(paramsFilter);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_COMBI_FILTER, byCombiWeightsFilterOperation.getProgressSource(), null);
		final OperationKey combiFilterOpKey = OperationManager.performOperationCreatingOperation(byCombiWeightsFilterOperation);

		final Threaded_MatrixQA threaded_MatrixQA = new Threaded_MatrixQA(new DataSetKey(combiFilterOpKey), false);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_QA, threaded_MatrixQA.getProgressSource(), null);
		// run within this thread
		CommonRunnable.doRunNowInThread(threaded_MatrixQA);
		final OperationKey qaSamplesOpKey = threaded_MatrixQA.getSamplesQAOperationKey();
		final OperationKey qaMarkersOpKey = threaded_MatrixQA.getMarkersQAOperationKey();

		// NOTE ABORTION_POINT We could be gracefully abort here

		MarkerCensusOperationParams markerCensusParams = new MarkerCensusOperationParams(new DataSetKey(combiFilterOpKey), qaSamplesOpKey, qaMarkersOpKey);
		final MatrixOperation markerCensusOperation = new MarkerCensusOperation(markerCensusParams);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_MARKER_CENSUS, markerCensusOperation.getProgressSource(), null);
		final OperationKey markerCensusOpKey = OperationManager.performOperationCreatingOperation(markerCensusOperation);

		// NOTE ABORTION_POINT We could be gracefully abort here

		final TrendTestOperationParams trendTestParams = new TrendTestOperationParams(combiFilterOpKey, null, markerCensusOpKey);
		final MatrixOperation testOperation = new TrendTestOperation(trendTestParams);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_TREND_TEST, testOperation.getProgressSource(), null);
		final OperationKey trendTestOpKey = OperationManager.performOperationCreatingOperation(testOperation);

		if (trendTestOpKey != null) {
			final TestOutputParams testOutputParams = new TestOutputParams(trendTestOpKey, OPType.TRENDTEST, qaMarkersOpKey);
			final MatrixOperation testOutputOperation = new OutputTest(testOutputParams);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_TEST_OUTPUT, testOutputOperation.getProgressSource(), null);
			OperationManager.performOperationCreatingOperation(testOutputOperation);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(trendTestOpKey);
		}
		progressSource.setNewStatus(ProcessStatus.COMPLEETED);
	}
}

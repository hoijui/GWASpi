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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.qamarkers.QAMarkersOperation;
import org.gwaspi.operations.qamarkers.QAMarkersOperationParams;
import org.gwaspi.operations.qasamples.QASamplesOperation;
import org.gwaspi.operations.qasamples.QASamplesOperationParams;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.gwaspi.reports.OutputQAMarkers;
import org.gwaspi.reports.OutputQASamples;
import org.gwaspi.reports.QAMarkersOutputParams;
import org.gwaspi.reports.QASamplesOutputParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO rename to just QA, because it can be done on matrices and operations alike.
 */
public class Threaded_MatrixQA extends CommonRunnable {

	static final ProgressSource PLACEHOLDER_PS_QA = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_QA", null));
	private static final ProcessInfo fullQAProcessInfo
			= new DefaultProcessInfo("Full QA Test",
					"Complete QA Test (Samples & Markers) procedure and generation of reports"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_QA_SAMPLES = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_QA_SAMPLES", null));
	private static final ProgressSource PLACEHOLDER_PS_QA_SAMPLES_REPORTS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_QA_SAMPLES_REPORTS", null));
	private static final ProgressSource PLACEHOLDER_PS_QA_MARKERS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_QA_MARKERS", null));
	private static final ProgressSource PLACEHOLDER_PS_QA_MARKERS_REPORTS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_QA_MARKERS_REPORTS", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(4);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_QA_SAMPLES, 0.2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_QA_SAMPLES_REPORTS, 0.1);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_QA_MARKERS, 0.6);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_QA_MARKERS_REPORTS, 0.1);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final DataSetKey parentKey;
	private final boolean createReports;
	private OperationKey samplesQAOperationKey;
	private OperationKey markersQAOperationKey;
	private final SuperProgressSource progressSource;

	public Threaded_MatrixQA(final DataSetKey parentKey, final boolean createReports) {
		super(
				"Matrix QA & Reports",
				"Matrix Quality Control",
				"Matrix QA & Reports on: " + parentKey.toString(),
				"Matrix Quality Control"); // NOTE actually: Quality Assurance

		this.parentKey = parentKey;
		this.createReports = createReports;
		this.samplesQAOperationKey = null;
		this.markersQAOperationKey = null;
		this.progressSource = new SuperProgressSource(fullQAProcessInfo, subProgressSourcesAndWeights);
	}

	public Threaded_MatrixQA(final DataSetKey parentKey) {
		this(parentKey, true);
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressSource;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_MatrixQA.class);
	}

	public OperationKey getSamplesQAOperationKey() {
		return samplesQAOperationKey;
	}

	public OperationKey getMarkersQAOperationKey() {
		return markersQAOperationKey;
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<OPType> necessaryOpTypes = new ArrayList<OPType>();
		necessaryOpTypes.add(OPType.SAMPLE_QA);
		necessaryOpTypes.add(OPType.MARKER_QA);
		List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOpTypes, parentKey, true);


		if (missingOPs.contains(OPType.SAMPLE_QA)) {
			final QASamplesOperationParams qaSamplesOperationParams = new QASamplesOperationParams(parentKey);
			final MatrixOperation qaSamplesOperation = new QASamplesOperation(qaSamplesOperationParams);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_QA_SAMPLES, qaSamplesOperation.getProgressSource(), null);
			samplesQAOperationKey = OperationManager.performOperation(qaSamplesOperation);

			if (createReports) {
				final QASamplesOutputParams qaSamplesOutputParams = new QASamplesOutputParams(samplesQAOperationKey, true);
				final MatrixOperation outputQASamples = new OutputQASamples(qaSamplesOutputParams);
				progressSource.replaceSubProgressSource(PLACEHOLDER_PS_QA_SAMPLES_REPORTS, outputQASamples.getProgressSource(), null);
				OperationManager.performOperation(outputQASamples);
			}
		}
		if (missingOPs.contains(OPType.MARKER_QA)) {
			final QAMarkersOperationParams qaMarkersOperationParams = new QAMarkersOperationParams(parentKey);
			final MatrixOperation qaMarkersOperation = new QAMarkersOperation(qaMarkersOperationParams);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_QA_MARKERS, qaMarkersOperation.getProgressSource(), null);
			markersQAOperationKey = OperationManager.performOperation(qaMarkersOperation);

			if (createReports) {
				final QAMarkersOutputParams qaMarkersOutputParams = new QAMarkersOutputParams(markersQAOperationKey);
				final MatrixOperation outputQAMarkers = new OutputQAMarkers(qaMarkersOutputParams);
				progressSource.replaceSubProgressSource(PLACEHOLDER_PS_QA_MARKERS_REPORTS, outputQAMarkers.getProgressSource(), null);
				OperationManager.performOperation(outputQAMarkers);
			}
		}
	}

	static OperationKey[] matrixCompleeted(SwingWorkerItem thisSwi, MatrixKey matrixKey, final SuperProgressSource superProgressSource)
			throws Exception
	{
		OperationKey[] resultOperationKeys = new OperationKey[2];

		GWASpiExplorerNodes.insertMatrixNode(matrixKey);

		if (!thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			return resultOperationKeys;
		}
		final DataSetKey parent = new DataSetKey(matrixKey);
		final Threaded_MatrixQA matrixQA = new Threaded_MatrixQA(parent, true);
		superProgressSource.replaceSubProgressSource(PLACEHOLDER_PS_QA, matrixQA.getProgressSource(), null);

		// run within this thread
		CommonRunnable.doRunNowInThread(matrixQA, thisSwi);

		resultOperationKeys[0] = matrixQA.getSamplesQAOperationKey();
		resultOperationKeys[1] = matrixQA.getMarkersQAOperationKey();

		return resultOperationKeys;
	}
}

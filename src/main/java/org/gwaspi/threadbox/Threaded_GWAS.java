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
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_GWAS extends CommonRunnable {

	private static final ProcessInfo fullGwasProcessInfo
			= new DefaultProcessInfo("Full GWAS",
					"Complete Genome Wide Association Study"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_GTFREQ_HW = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_GTFREQ_HW", null));
	public static final ProgressSource PLACEHOLDER_PS_ALLELIC_TEST = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_ALLELIC_TEST", null));
	public static final ProgressSource PLACEHOLDER_PS_GENOTYPIC_TEST = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_GENOTYPIC_TEST", null));
	public static final ProgressSource PLACEHOLDER_PS_TREND_TEST = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_TREND_TEST", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(4);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_GTFREQ_HW, 0.4);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_ALLELIC_TEST, 0.2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_GENOTYPIC_TEST, 0.2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_TREND_TEST, 0.2);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final GWASinOneGOParams gwasParams;
	private final SuperProgressSource progressSource;
	private final TaskLockProperties taskLockProperties;

	public Threaded_GWAS(GWASinOneGOParams gwasParams) {
		super("GWAS", "on " + gwasParams.getMarkerCensusOperationParams().getParent().toString());

		this.gwasParams = gwasParams;
		this.progressSource = new SuperProgressSource(fullGwasProcessInfo, subProgressSourcesAndWeights);
		final DataSetKey parentKey = gwasParams.getMarkerCensusOperationParams().getParent();
		this.taskLockProperties = MultiOperations.createTaskLockProperties(parentKey);
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
		return LoggerFactory.getLogger(Threaded_GWAS.class);
	}

	@Override
	protected void runInternal() throws IOException {

		progressSource.setNewStatus(ProcessStatus.INITIALIZING);
		final Threaded_GTFreq_HW threaded_GTFreq_HW = new Threaded_GTFreq_HW(gwasParams);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_GTFREQ_HW, threaded_GTFreq_HW.getProgressSource(), null);
		progressSource.setNewStatus(ProcessStatus.RUNNING);
		CommonRunnable.doRunNowInThread(threaded_GTFreq_HW);

		OperationKey censusOpKey = threaded_GTFreq_HW.getMarkerCensusOperationKey();
		OperationKey hwOpKey = threaded_GTFreq_HW.getHardyWeinbergOperationKey();
		performGWAS(gwasParams, censusOpKey, hwOpKey, progressSource);
		progressSource.setNewStatus(ProcessStatus.COMPLEETED);
	}

	private static void performGWAS(GWASinOneGOParams gwasParams, OperationKey censusOpKey, OperationKey hwOpKey, final SuperProgressSource superProgressSource) throws IOException {

		// tests (need newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
		if ((censusOpKey != null) && (hwOpKey != null)) {
			// NOTE ABORTION_POINT We could be gracefully abort here
			if (gwasParams.isPerformAllelicTests()) {
				performTest(gwasParams, censusOpKey, hwOpKey, OPType.ALLELICTEST, superProgressSource, PLACEHOLDER_PS_ALLELIC_TEST);
			}

			// NOTE ABORTION_POINT We could be gracefully abort here
			if (gwasParams.isPerformGenotypicTests()) {
				performTest(gwasParams, censusOpKey, hwOpKey, OPType.GENOTYPICTEST, superProgressSource, PLACEHOLDER_PS_GENOTYPIC_TEST);
			}

			// NOTE ABORTION_POINT We could be gracefully abort here
			if (gwasParams.isPerformTrendTests()) {
				performTest(gwasParams, censusOpKey, hwOpKey, OPType.TRENDTEST, superProgressSource, PLACEHOLDER_PS_TREND_TEST);
			}
		}
	}

	private static void performTest(GWASinOneGOParams gwasParams, OperationKey censusOpKey, OperationKey hwOpKey, final OPType testType, final SuperProgressSource superProgressSource, ProgressSource placeholderPS) throws IOException {

		final OperationKey markersQAOpKey = gwasParams.getMarkerCensusOperationParams().getMarkerQAOpKey();

		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpKey);

		if (gwasParams.isDiscardMarkerHWCalc()) {
			gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getNumMarkers());
		}

		final Threaded_Test threaded_Test = new Threaded_Test(censusOpKey, hwOpKey, gwasParams, testType);
		superProgressSource.replaceSubProgressSource(placeholderPS, threaded_Test.getProgressSource(), null);
		try {
			CommonRunnable.doRunNowInThread(threaded_Test);
		} catch (Exception ex) {
			throw new IOException(ex);
		}
//		OperationKey testOpKey = OperationManager.performCleanTests(
//				censusOpKey,
//				hwOpKey,
//				gwasParams.getDiscardMarkerHWTreshold(),
//				testType);
//
//		// Make Reports (needs newMatrixId, QAopId, AssocOpId)
//		if (testOpKey != null) {
//			final TestOutputParams testOutputParams = new TestOutputParams(testOpKey, testType, markersQAOpKey);
//			final OutputTest outputTest = new OutputTest(testOutputParams);
//			OperationManager.performOperation(outputTest);
//			GWASpiExplorerNodes.insertReportsUnderOperationNode(testOpKey);
//		}
	}
}

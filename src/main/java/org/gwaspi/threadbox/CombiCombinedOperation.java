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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.RuntimeAnalyzer;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperation;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperationParams;
import org.gwaspi.operations.combi.CombiOutputOperation;
import org.gwaspi.operations.combi.CombiOutputOperationParams;
import org.gwaspi.operations.combi.CombiTestOperation;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperation;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.operations.trendtest.TrendTestOperation;
import org.gwaspi.operations.trendtest.TrendTestOperationDataSet;
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

public class CombiCombinedOperation extends CommonRunnable {

	private static final ProcessInfo PROCESS_INFO
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
	private static final ProgressSource PLACEHOLDER_PS_COMBI_OUTPUT = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_COMBI_OUTPUT", null));
	private static final Map<ProgressSource, Double> SUB_PROGRESS_SOURCESS_AND_WEIGHTS;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(6);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_COMBI_TEST, 0.5);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_COMBI_FILTER, 0.1);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_QA, 0.1);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_MARKER_CENSUS, 0.1);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_TREND_TEST, 0.1);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_TEST_OUTPUT, 0.05);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_COMBI_OUTPUT, 0.05);
		SUB_PROGRESS_SOURCESS_AND_WEIGHTS = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final CombiTestOperationParams paramsTest;
	private final ByCombiWeightsFilterOperationParams paramsFilter;
	private final SuperProgressSource progressSource;
	private final TaskLockProperties taskLockProperties;
	private final List<CombiTestOperation.Kernel> recyclableKernel;
	private OperationKey resultingTrendTestOperationKey;

	public CombiCombinedOperation(
			final CombiTestOperationParams paramsTest,
			final ByCombiWeightsFilterOperationParams paramsFilter,
			final List<CombiTestOperation.Kernel> recyclableKernel)
	{
		super("Combi Association Test", "on " + paramsTest.getParent().toString());

		this.paramsTest = paramsTest;
		this.paramsFilter = paramsFilter;
		this.progressSource = new SuperProgressSource(PROCESS_INFO, SUB_PROGRESS_SOURCESS_AND_WEIGHTS);
		this.taskLockProperties = MultiOperations.createTaskLockProperties(paramsTest.getParent());
		this.recyclableKernel = recyclableKernel;
		this.resultingTrendTestOperationKey = null;
	}

	public CombiCombinedOperation(
			final CombiTestOperationParams paramsTest,
			final ByCombiWeightsFilterOperationParams paramsFilter)
	{
		this(paramsTest, paramsFilter, null);
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
		return LoggerFactory.getLogger(CombiCombinedOperation.class);
	}

	public OperationKey getResultingTrendTestOperationKey() {
		return resultingTrendTestOperationKey;
	}

	private void extractSmallestPValues(
			final CombiTestOperationParams prototypeTestParams,
			final TrendTestOperationDataSet trendTestDataSet,
			final int numChromosomes,
			final List<Double> ps,
			final List<List<Double>> smallestPs)
			throws IOException
	{
		if (prototypeTestParams.isPerChromosome()) {
			// NOTE The following code assumes that markers are ordered by chromosome
			final List<String> markersChromosomes = trendTestDataSet.getMarkersMetadatasSource().getChromosomes();
			String currentChrom = markersChromosomes.get(0);
			int ci = 0;
			for (int mi = 0; mi < ps.size(); mi++) {
				final List<Double> chromPs = new ArrayList<Double>(ps.size() / numChromosomes); // approximate markers per chromosome
				while ((mi < ps.size()) && currentChrom.equals(markersChromosomes.get(mi))) {
					chromPs.add(ps.get(mi));
					mi++;
				}
				Collections.sort(chromPs);
				smallestPs.get(ci).add(chromPs.get(0));
				ci++;
				mi--;
			}
//				for (int ci = 0; ci < numChromosomes; ci++) {
//					final List<Double> chromPs = new ArrayList(XXX resultingTrendTestDataSet.getPs(-1, -1));
//					Collections.sort(ps);
//					smallestPs.get(ci).add(ps.get(0));
//				}
		} else {
			Collections.sort(ps);
			smallestPs.get(0).add(ps.get(0));
		}
	}

	private List<Double> thresholdCalibration(
			final CombiTestOperationParams prototypeTestParams,
			final ByCombiWeightsFilterOperationParams prototypeFilterParams,
			final List<CombiTestOperation.Kernel> kernel,
			final List<Double> alphas)
			throws IOException
	{
//		// HACK this is(?):
		final OperationDataSet toLoadFromDataSet = OperationManager.generateOperationDataSet(prototypeTestParams.getQAMarkerOperationKey());
//		final int totalMarkers = toLoadFromDataSet.getNumMarkers();

		final int numChromosomes = prototypeTestParams.isPerChromosome() ? toLoadFromDataSet.getNumChromosomes() : 1;
		final List<List<Double>> smallestPs = new ArrayList<List<Double>>(
				numChromosomes);
		for (int ci = 0; ci < numChromosomes; ci++) {
			smallestPs.add(new ArrayList<Double>(
					prototypeTestParams.getThresholdCalibrationIterations()));
		}
		for (int tci = 0; tci < prototypeTestParams.getThresholdCalibrationIterations(); tci++) {
			final CombiTestOperationParams combiParams
					= new CombiTestOperationParams(
							prototypeTestParams.getQAMarkerOperationKey(),
							prototypeTestParams.getEncoder(),
							prototypeTestParams.getEncodingParams(),
							false, // thresholdCalibrationEnabled
							null, // thresholdCalibrationAlpha
							null, // thresholdCalibrationIterations
							false, // thresholdCalibrationAlphasCalculationEnabled
							null, // thresholdCalibrationAlphasCalculationPValueTarget
							null, // thresholdCalibrationAlphasCalculationIterations
							prototypeTestParams.isPerChromosome(),
							prototypeTestParams.getSolverLibrary(),
							prototypeTestParams.getSolverParams(),
							true, //prototypeTestParams.isUsingRandomSampleAffections(),
							prototypeTestParams.getName() + "_threasholdCalibrationIteration" + tci);
			final boolean mtkFraction = (prototypeFilterParams.getMarkersToKeep() == -1);
			final ByCombiWeightsFilterOperationParams combiFilterParams
					= new ByCombiWeightsFilterOperationParams(
					prototypeFilterParams.getTotalMarkers(), //totalMarkers,
					prototypeFilterParams.isPerChromosome(), //perChromosome,
					prototypeFilterParams.getWeightsFilterWidth(), //weightsFilterWidth,
//					mtkFraction ? null : prototypeFilterParams.getMarkersToKeep(), //markersToKeep,
//					mtkFraction ? prototypeFilterParams.getMarkersToKeepFraction() : null, //prototypeFilterParams.getMarkersToKeepFraction(), //markersToKeepFraction
					null, //markersToKeep,
					1.0, //markersToKeepFraction
					prototypeFilterParams.getName() + "_threasholdCalibrationIteration" + tci); //resultFilterOperationName);
			final CombiCombinedOperation combinedCombi = new CombiCombinedOperation(combiParams, combiFilterParams, kernel);
			combinedCombi.run();
			final OperationKey resultingPermTrendTestOperationKey = combinedCombi.getResultingTrendTestOperationKey();
			final TrendTestOperationDataSet resultingTrendTestDataSet = (TrendTestOperationDataSet)
					OperationManager.generateOperationDataSet(resultingPermTrendTestOperationKey);
			final List<Double> ps = new ArrayList(resultingTrendTestDataSet.getPs(-1, -1));
			extractSmallestPValues(prototypeTestParams, resultingTrendTestDataSet, numChromosomes, ps, smallestPs);
		}

		final List<Double> selectedPs = new ArrayList<Double>(numChromosomes);
		for (int ci = 0; ci < numChromosomes; ci++) {
			final List<Double> chromSmallestPs = smallestPs.get(ci);
			Collections.sort(chromSmallestPs);
			final Double chromSelectedP = chromSmallestPs.get(
					(int) Math.round(chromSmallestPs.size() * alphas.get(ci)));
			selectedPs.add(chromSelectedP);

		}
		return selectedPs;
	}

	private static Double evaluateAlpha(final List<Double> smallestPs, final Double pValueTarget) {

		int pi = 0;
		for (final Double pValue : smallestPs) {
			if (pValue.compareTo(pValueTarget) >= 0) {
				break;
			}
			pi++;
		}
		final Double pValueBefore = (pi > 0) ? smallestPs.get(pi - 1) : 0.0;
		final Double pValueChosen = smallestPs.get(pi);
		final Double alpha;
		if (pValueChosen.equals(pValueTarget)) {
			// exact match
			alpha = (double) pi / pValueChosen;
		} else {
			// inbetween two indices -> interpolate
			final Double alphaBefore = (double) ((pi > 0) ? (pi - 1) : pi) / pValueBefore;
			final Double alphaChosen = (double) pi / pValueChosen;

			final Double betweenValuesInterpolation = (pValueTarget - pValueBefore) / (pValueChosen - pValueBefore);
			alpha = alphaBefore + ((alphaChosen - alphaBefore) * betweenValuesInterpolation);
		}

		return alpha;
	}

	private List<Double> evaluateThresholdCalibrationAlpha(
			final CombiTestOperationParams prototypeTestParams)
			throws IOException
	{
//		// HACK this is(?):
		final OperationDataSet toLoadFromDataSet = OperationManager.generateOperationDataSet(prototypeTestParams.getQAMarkerOperationKey());
//		final int totalMarkers = toLoadFromDataSet.getNumMarkers();

		final int numChromosomes = prototypeTestParams.isPerChromosome() ? toLoadFromDataSet.getNumChromosomes() : 1;
		final List<List<Double>> smallestPs = new ArrayList<List<Double>>(
				numChromosomes);
		for (int ci = 0; ci < numChromosomes; ci++) {
			smallestPs.add(new ArrayList<Double>(
					prototypeTestParams.getThresholdCalibrationAlphasCalculationIterations()));
		}

		final DataSetKey parentQAMarkers = prototypeTestParams.getParent(); // NOTE As of now, this returns the QA-Markers operation of/below our actual parent
		final DataSetKey parent = new DataSetKey(parentQAMarkers.getOperationParent().getParentMatrixKey());
		final List<OPType> necessaryOpTypes = Arrays.asList(OPType.SAMPLE_QA, OPType.MARKER_QA);
		final List<OperationKey> necessaryOperations = OperationManager.findNecessaryOperations(necessaryOpTypes, parent, true);
		if (necessaryOperations.size() < necessaryOpTypes.size()) {
			throw new RuntimeException("Not all necessary operation types ("+necessaryOpTypes+") were found on parent \""+parent+"\""); // TODO FIXME Instead, just run these operations!
		}
		final MarkerCensusOperationParams mcVorlageParams = new MarkerCensusOperationParams(
				parent,
				necessaryOperations.get(0),
				necessaryOperations.get(1));
		for (int tci = 0; tci < prototypeTestParams.getThresholdCalibrationAlphasCalculationIterations(); tci++) {
			final MarkerCensusOperationParams markerCensusParams
					= new MarkerCensusOperationParams(
							mcVorlageParams.getParent(), // parent
							prototypeTestParams.getName() + "_threasholdCalibrationAlphaDiscoveryIteration" + tci, // name
							mcVorlageParams.getSampleQAOpKey(), // qaSamplesOp
							mcVorlageParams.getSampleMissingRatio(),
							mcVorlageParams.getSampleHetzygRatio(),
							mcVorlageParams.getMarkerQAOpKey(), // qaMarkersOp
							mcVorlageParams.isDiscardMismatches(),
							mcVorlageParams.getMarkerMissingRatio(),
							mcVorlageParams.getPhenotypeFile(), // phenotypeFile
							true); // random samples
			final MarkerCensusOperation markerCensus = new MarkerCensusOperation(markerCensusParams);
			final OperationKey markerCensusOpKey = markerCensus.call();

			final TrendTestOperationParams trendTestParams
					= new TrendTestOperationParams(
							prototypeTestParams.getParent(),
							prototypeTestParams.getName() + "_threasholdCalibrationIteration" + tci,
							markerCensusOpKey);
			final TrendTestOperation trendTest = new TrendTestOperation(trendTestParams);
			final OperationKey trendTestOpKey = trendTest.call();

			final TrendTestOperationDataSet resultingTrendTestDataSet = (TrendTestOperationDataSet)
					OperationManager.generateOperationDataSet(trendTestOpKey);
			final List<Double> ps = new ArrayList(resultingTrendTestDataSet.getPs(-1, -1));
			extractSmallestPValues(prototypeTestParams, resultingTrendTestDataSet, numChromosomes, ps, smallestPs);
		}

		final Double pValueTarget = prototypeTestParams.getThresholdCalibrationAlphasCalculationPValueTarget();
		final List<Double> alphas = new ArrayList<Double>(numChromosomes);
		// Find t_star(s, one per chromosome) as alpha-percentile of sorted p-values
		for (int ci = 0; ci < numChromosomes; ci++) {
			final List<Double> chromSmallestPs = smallestPs.get(ci);
			Collections.sort(chromSmallestPs);
			final Double alpha = evaluateAlpha(chromSmallestPs, pValueTarget);
			alphas.add(alpha);
		}
		return alphas;
	}

	@Override
	protected void runInternal() throws IOException {

//		List<Operation> operations = OperationsList.getOperationsList(params.getMatrixKey().getId());
//		int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

		// XXX TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
//		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);

		// NOTE ABORTION_POINT We could be gracefully aborted here

		progressSource.setNewStatus(ProcessStatus.RUNNING);
		final OperationDataSet toLoadFromDataSet = OperationManager.generateOperationDataSet(paramsTest.getQAMarkerOperationKey());
		final int numParts = paramsTest.isPerChromosome() ? toLoadFromDataSet.getNumChromosomes() : 1;
		final List<CombiTestOperation.Kernel> kernel = new ArrayList<CombiTestOperation.Kernel>(Collections.nCopies(numParts, (CombiTestOperation.Kernel) null));
		final List<Double> pValueThreasholds;
		if (paramsTest.isThresholdCalibrationEnabled()) {
			final List<Double> thresholdCalibrationAlphas;
			if (paramsTest.isThresholdCalibrationAlphasCalculationEnabled()) {
				RuntimeAnalyzer.getInstance().log("COMBI-permutation-part1", true);
				RuntimeAnalyzer.getInstance().setDiscard(true);
				thresholdCalibrationAlphas = evaluateThresholdCalibrationAlpha(paramsTest);
				RuntimeAnalyzer.getInstance().setDiscard(false);
				RuntimeAnalyzer.getInstance().log("COMBI-permutation-part1", false);
			} else {
				thresholdCalibrationAlphas = paramsTest.getThresholdCalibrationAlphas();
			}
			RuntimeAnalyzer.getInstance().log("COMBI-permutation-part2", true);
			RuntimeAnalyzer.getInstance().setDiscard(true);
			pValueThreasholds = thresholdCalibration(paramsTest, paramsFilter, kernel, thresholdCalibrationAlphas);
			RuntimeAnalyzer.getInstance().setDiscard(false);
			RuntimeAnalyzer.getInstance().log("COMBI-permutation-part2", false);
			getLog().debug("pValueThreashold: {}", pValueThreasholds);
		} else {
			// use default value
			pValueThreasholds = Collections.EMPTY_LIST;
			getLog().debug("pValueThreashold (empty): {}", pValueThreasholds);
		}

		// NOTE ABORTION_POINT We could be gracefully aborted here

		RuntimeAnalyzer.getInstance().log("COMBI-combined-SVM", true);
		final MatrixOperation combiTestOperation = new CombiTestOperation(paramsTest, recyclableKernel);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_COMBI_TEST, combiTestOperation.getProgressSource(), null);
		final OperationKey combiTestOpKey = OperationManager.performOperationCreatingOperation(combiTestOperation);

		// NOTE ABORTION_POINT We could be gracefully aborted here

		paramsFilter.setParent(new DataSetKey(combiTestOpKey));
		final MatrixOperation byCombiWeightsFilterOperation = new ByCombiWeightsFilterOperation(paramsFilter);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_COMBI_FILTER, byCombiWeightsFilterOperation.getProgressSource(), null);
		final OperationKey combiFilterOpKey = OperationManager.performOperationCreatingOperation(byCombiWeightsFilterOperation);
		if (combiFilterOpKey.getId() == OperationKey.NULL_ID) {
			progressSource.setNewStatus(ProcessStatus.ABORTED);
			return;
		}
		final DataSetKey combiFilterDataSetKey = new DataSetKey(combiFilterOpKey);
		RuntimeAnalyzer.getInstance().log("COMBI-combined-SVM", false);

		// NOTE ABORTION_POINT We could be gracefully aborted here

		RuntimeAnalyzer.getInstance().log("COMBI-onlyPValueCalculation", true);
		final QACombinedOperation threaded_MatrixQA = new QACombinedOperation(combiFilterDataSetKey, false);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_QA, threaded_MatrixQA.getProgressSource(), null);
		// run within this thread
		CommonRunnable.doRunNowInThread(threaded_MatrixQA);
		final OperationKey qaSamplesOpKey = threaded_MatrixQA.getSamplesQAOperationKey();
		final OperationKey qaMarkersOpKey = threaded_MatrixQA.getMarkersQAOperationKey();

		// NOTE ABORTION_POINT We could be gracefully aborted here

		final MarkerCensusOperationParams markerCensusParams = new MarkerCensusOperationParams(combiFilterDataSetKey, qaSamplesOpKey, qaMarkersOpKey);
		final MatrixOperation markerCensusOperation = new MarkerCensusOperation(markerCensusParams);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_MARKER_CENSUS, markerCensusOperation.getProgressSource(), null);
		final OperationKey markerCensusOpKey = OperationManager.performOperationCreatingOperation(markerCensusOperation);

		// NOTE ABORTION_POINT We could be gracefully aborted here

		final TrendTestOperationParams trendTestParams = new TrendTestOperationParams(combiFilterDataSetKey, null, markerCensusOpKey);
		final MatrixOperation testOperation = new TrendTestOperation(trendTestParams);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_TREND_TEST, testOperation.getProgressSource(), null);
		final OperationKey trendTestOpKey = OperationManager.performOperationCreatingOperation(testOperation);
		resultingTrendTestOperationKey = trendTestOpKey;
		RuntimeAnalyzer.getInstance().log("COMBI-onlyPValueCalculation", false);

		// Only possibly write to output files if we are the actual COMBI test run,
		// versus a threshold calibration run (which uses random sample affection).
		if ((trendTestOpKey != null) && paramsTest.isUsingRandomSampleAffections()) {
			final TestOutputParams testOutputParams = new TestOutputParams(trendTestOpKey, OPType.TRENDTEST, qaMarkersOpKey, pValueThreasholds);
			final MatrixOperation testOutputOperation = new OutputTest(testOutputParams);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_TEST_OUTPUT, testOutputOperation.getProgressSource(), null);
			OperationManager.performOperationCreatingOperation(testOutputOperation);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(trendTestOpKey);

			final CombiOutputOperationParams combiOutputOperationParams = new CombiOutputOperationParams(trendTestOpKey, combiTestOpKey, null, pValueThreasholds, null);
			final MatrixOperation combiOutputOperation = new CombiOutputOperation(combiOutputOperationParams);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_COMBI_OUTPUT, combiOutputOperation.getProgressSource(), null);
			OperationManager.performOperationCreatingOperation(combiOutputOperation);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(trendTestOpKey);
		}
		progressSource.setNewStatus(ProcessStatus.COMPLEETED);
	}
}

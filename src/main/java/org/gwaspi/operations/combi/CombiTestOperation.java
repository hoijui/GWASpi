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

package org.gwaspi.operations.combi;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.gwaspi.constants.GlobalConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.IndicesSubList;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleKey;
import org.gwaspi.operations.AbstractOperationCreatingOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class comment
 * - n / nSamples : #samples == #data-points in the SVM feature space == rows & columns in the SVM kernel matrix
 * - dSamples : #markers == #SNPs
 * - dEncoded : #markers * encodingFactor == #dimensions in the SVM  feature space
 */
public class CombiTestOperation
		extends AbstractOperationCreatingOperation<CombiTestOperationDataSet, CombiTestOperationParams>
{
	private static final Logger LOG
			= LoggerFactory.getLogger(CombiTestOperation.class);

	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new CombiTestOperationFactory());
	}

	private static final boolean REQUIRE_ONLY_VALID_AFFECTION = false;

	public static int KERNEL_CALCULATION_ALGORTIHM = 5;

	public static CombiTestOperationSpy spy = null;

	private static final ProcessInfo combiProcessInfo = new DefaultProcessInfo("COMBI Test", ""); // TODO
	private static final ProcessInfo PI_SVM_GENOME = new SubProcessInfo(combiProcessInfo, "Train genome-wide SVM", ""); // TODO

	private Boolean valid;
	private String problemDescription;
	private ProgressHandler customProgressHandler;
	private List<SvmProgressHandler> svmPHs;

	public CombiTestOperation(CombiTestOperationParams params) {
		super(params);

		this.valid = null;
		this.problemDescription = null;
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return CombiTestOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return combiProcessInfo;
	}

	static class SvmProgressHandler extends SuperProgressSource {

		private final ProgressHandler<Integer> creatingKernelMatrixPH;
		private final ProgressHandler<Integer> transcribeKernelMatrixPH;
		private final ProgressHandler<?> trainSVMPH;
		private final ProgressHandler<Integer> calculateOriginalSpaceWeightsPH;

		public SvmProgressHandler(final ProcessInfo processInfo, final int n, final int dEncoded, final int numChunks) {
			super(processInfo);

			ProcessInfo creatingKernelMatrixPI = new SubProcessInfo(combiProcessInfo,
					"encoding features and creating kernel matrix", null);
			creatingKernelMatrixPH
					= new IntegerProgressHandler(
							creatingKernelMatrixPI,
							0, // start state, first marker
							numChunks - 1); // end state, last marker

			ProcessInfo transcribeKernelMatrixPI = new SubProcessInfo(combiProcessInfo,
					"store kernel matrix rows", null);
			transcribeKernelMatrixPH
					= new IntegerProgressHandler(
							transcribeKernelMatrixPI,
							0, // start state, first marker
							n - 1); // end state, last marker

			ProcessInfo trainSVMPI = new SubProcessInfo(combiProcessInfo,
					"train the SVM", null);
			trainSVMPH = new IndeterminateProgressHandler(trainSVMPI);

			ProcessInfo calculateOriginalSpaceWeightsPI = new SubProcessInfo(combiProcessInfo,
					"calculate original space weights", null);
			calculateOriginalSpaceWeightsPH
					= new IntegerProgressHandler(
							calculateOriginalSpaceWeightsPI,
							0, // start state
							dEncoded - 1); // end state. this value is too high and will be adjusted later on!

			addSubProgressSource(creatingKernelMatrixPH, 0.80);
			addSubProgressSource(transcribeKernelMatrixPH, 0.05);
			addSubProgressSource(trainSVMPH, 0.05);
			addSubProgressSource(calculateOriginalSpaceWeightsPH, 0.1);
		}

		public SvmProgressHandler() {
			super(null);

			creatingKernelMatrixPH = new NullProgressHandler<Integer>(null);
			transcribeKernelMatrixPH = new NullProgressHandler<Integer>(null);
			trainSVMPH = new NullProgressHandler(null);
			calculateOriginalSpaceWeightsPH = new NullProgressHandler<Integer>(null);

			addSubProgressSource(creatingKernelMatrixPH, 0.80);
			addSubProgressSource(transcribeKernelMatrixPH, 0.05);
			addSubProgressSource(trainSVMPH, 0.05);
			addSubProgressSource(calculateOriginalSpaceWeightsPH, 0.1);
		}

		public ProgressHandler<Integer> getCreatingKernelMatrixPH() {
			return creatingKernelMatrixPH;
		}

		public ProgressHandler<Integer> getTranscribeKernelMatrixPH() {
			return transcribeKernelMatrixPH;
		}

		public ProgressHandler<?> getTrainSVMPH() {
			return trainSVMPH;
		}

		public ProgressHandler<Integer> getCalculateOriginalSpaceWeightsPH() {
			return calculateOriginalSpaceWeightsPH;
		}
	}

	private static Map<String, List<Integer>> extractChromToIndicesMap(
			final DataSetSource dataSetSource)
			throws IOException
	{
		final Map<String, List<Integer>> markersChromosomeToIndices
				= new LinkedHashMap<String, List<Integer>>(GlobalConstants.NUM_CHROMOSOMES + 2);

//		final List<Integer> markersOrigIndices
//				= dataSetSource.getMarkersKeysSource().getIndices();
		final List<String> markersChromosomes
				= dataSetSource.getMarkersMetadatasSource().getChromosomes();
		for (int markerIndex = 0; markerIndex < markersChromosomes.size(); markerIndex++) {
//			final int markerOrigIndex = markersOrigIndices.get(markerIndex);
			final String markerChromosome = markersChromosomes.get(markerIndex);
			List<Integer> chromosomeMarkerIndices
					= markersChromosomeToIndices.get(markerChromosome);
			if (chromosomeMarkerIndices == null) {
				chromosomeMarkerIndices = new LinkedList<Integer>();
				markersChromosomeToIndices.put(markerChromosome, chromosomeMarkerIndices);
			}
			chromosomeMarkerIndices.add(markerIndex);
		}

		return markersChromosomeToIndices;
	}

	@Override
	protected ProgressHandler getProgressHandler() throws IOException {

		if (customProgressHandler == null) {
			final DataSetSource parentDataSetSource = getParentDataSetSource();
			final GenotypeEncoder genotypeEncoder = getParams().getEncoder();
			final int n = parentDataSetSource.getNumSamples();

			if (getParams().isPerChromosome()) {
				// run SVM once per chromosome
				final Map<String, List<Integer>> markersChromosomeToIndices
						= extractChromToIndicesMap(parentDataSetSource);
				svmPHs = new ArrayList<SvmProgressHandler>(markersChromosomeToIndices.size());
				for (final Map.Entry<String, List<Integer>> chromosomeMarkerToIndices
						: markersChromosomeToIndices.entrySet())
				{
					final String chromosome = chromosomeMarkerToIndices.getKey();
					final List<Integer> markerIndices = chromosomeMarkerToIndices.getValue();
					final int dSamples = markerIndices.size();
					final int dEncoded = dSamples * genotypeEncoder.getEncodingFactor();
					final int maxChunkSize = MarkerGenotypesEncoder.calculateMaxChunkSize(genotypeEncoder, dSamples, n, null);
					final int numChunks = MarkerGenotypesEncoder.calculateNumChunks(dSamples, maxChunkSize);

					final ProcessInfo piSvmChromosome = new SubProcessInfo(combiProcessInfo, "Train chromosome-wide SVM for chromosome " + chromosome, "");
					svmPHs.add(new SvmProgressHandler(piSvmChromosome, n, dEncoded, numChunks));
				}
			} else {
				// run SVM only once, genome-wide
				final int dSamples = parentDataSetSource.getNumMarkers();
				final int dEncoded = dSamples * genotypeEncoder.getEncodingFactor();
				final int maxChunkSize = MarkerGenotypesEncoder.calculateMaxChunkSize(genotypeEncoder, dSamples, n, null);
				final int numChunks = MarkerGenotypesEncoder.calculateNumChunks(dSamples, maxChunkSize);

				svmPHs = Collections.singletonList(new SvmProgressHandler(PI_SVM_GENOME, n, dEncoded, numChunks));
			}

			customProgressHandler = new SuperProgressSource(combiProcessInfo, svmPHs);
		}

		return customProgressHandler;
	}

	@Override
	public boolean isValid() throws IOException {

		if (valid != null) {
			return valid;
		}

		List<OPType> ancestorOperationTypes = OperationsList.getAncestorOperationTypes(getParentKey().getOperationParent());

		if (ancestorOperationTypes.isEmpty()
				|| (ancestorOperationTypes.get(0) != OPType.MARKER_QA))
		{
			problemDescription = "the direct parent has to be a QA markers operation";
			valid = false;
			return valid;
		}

		if (REQUIRE_ONLY_VALID_AFFECTION) {
			// We also require that somewhere in our ancestry,
			// all the samples with invalid affection info have been excluded.
			boolean hasOnlyValidAffections = false;
			for (OPType ancestorOpType : ancestorOperationTypes) {
				if (ancestorOpType == OPType.FILTER_BY_VALID_AFFECTION) {
					hasOnlyValidAffections = true;
					break;
				}
			}
			valid = hasOnlyValidAffections;
			if (!hasOnlyValidAffections) {
				problemDescription = "somewhere in the ancestry, all the samples with invalid affection info have to be excluded";
			}
		} else {
			valid = true;
		}

		return valid;
	}

	@Override
	public String getProblemDescription() {
		return problemDescription;
	}

	@Override
	public OperationKey call() throws IOException {

		final ProgressHandler progressHandler = getProgressHandler();
		progressHandler.setNewStatus(ProcessStatus.INITIALIZING);

		DataSetSource parentDataSetSource = getParentDataSetSource();
		QAMarkersOperationDataSet parentQAMarkersOperationDataSet
				= (QAMarkersOperationDataSet) parentDataSetSource;

		CombiTestOperationDataSet dataSet = generateFreshOperationDataSet();

		dataSet.setNumMarkers(parentDataSetSource.getNumMarkers());
		dataSet.setNumChromosomes(parentDataSetSource.getNumChromosomes());
		dataSet.setNumSamples(parentDataSetSource.getNumSamples());

		final List<Double> weights;
		if (getParams().isPerChromosome()) {
			// run SVM once per chromosome
			final Map<String, List<Integer>> markersChromosomeToIndices // TODO maybe store it from getProgressHandler()?
					= extractChromToIndicesMap(parentDataSetSource);

			final List<SampleKey> validSamplesKeys = parentDataSetSource.getSamplesKeysSource();
			final List<Affection> validSampleAffections = parentDataSetSource.getSamplesInfosSource().getAffections();

			weights = new ArrayList<Double>(Collections.nCopies(parentDataSetSource.getNumMarkers(), -1.0));
			float[][] recyclableKernelMatrix = null;
			svm_problem recyclableProblem = null;
			Problem recyclableProblemLinear = null;
			progressHandler.setNewStatus(ProcessStatus.RUNNING);
			int chromoIndex = 0;
			for (final Map.Entry<String, List<Integer>> markerChromosomeIndices : markersChromosomeToIndices.entrySet()) {
//				final String chromosome = markerChromosomeIndices.getKey();
				final SvmProgressHandler phSvmChromosome = svmPHs.get(chromoIndex);
				final List<Integer> markerIndices = markerChromosomeIndices.getValue();

				phSvmChromosome.setNewStatus(ProcessStatus.INITIALIZING);
				// run SVM only once, genome-wide
				final int dSamples = markerIndices.size();
				final int dEncoded = dSamples * getParams().getEncoder().getEncodingFactor();
				final int n = parentDataSetSource.getNumSamples();

				final List<MarkerKey> markerKeys = new IndicesSubList<MarkerKey>(parentDataSetSource.getMarkersKeysSource(), markerIndices);

				LOG.debug("Combi Association Test: #samples: " + n);
				LOG.debug("Combi Association Test: #markers: " + dSamples);
				LOG.debug("Combi Association Test: encoding factor: " + getParams().getEncoder().getEncodingFactor());
				LOG.debug("Combi Association Test: #SVM-dimensions: " + dEncoded);

				final List<Byte> majorAlleles = new IndicesSubList<Byte>(parentQAMarkersOperationDataSet.getKnownMajorAllele(), markerIndices);
				final List<Byte> minorAlleles = new IndicesSubList<Byte>(parentQAMarkersOperationDataSet.getKnownMinorAllele(), markerIndices);
				final List<int[]> markerGenotypesCounts = new IndicesSubList<int[]>(parentQAMarkersOperationDataSet.getGenotypeCounts(), markerIndices);
				final List<GenotypesList> markersGenotypesSource = new IndicesSubList<GenotypesList>(parentDataSetSource.getMarkersGenotypesSource(), markerIndices);

				phSvmChromosome.setNewStatus(ProcessStatus.RUNNING);
				final RunSVMResults svmResults = runEncodingAndSVM(
						markerKeys,
						majorAlleles,
						minorAlleles,
						markerGenotypesCounts,
						validSamplesKeys,
						validSampleAffections,
						markersGenotypesSource,
						getParams().getEncoder(),
						getParams().getEncodingParams(),
						getParams().getSolverLibrary(),
						getParams().getSolverParams(),
						recyclableKernelMatrix,
						recyclableProblem,
						recyclableProblemLinear,
						phSvmChromosome);
				phSvmChromosome.setNewStatus(ProcessStatus.FINALIZING);
				final List<Double> chromosomeWeights = svmResults.getWeights();
				recyclableKernelMatrix = svmResults.getRecyclableKernelMatrix();
				recyclableProblem = svmResults.getRecyclableProblem();
				recyclableProblemLinear = svmResults.getRecyclableProblemLinear();

				int chromosomeMarkerIndex = 0;
				for (final Integer genomeMarkerIndex : markerIndices) {
					weights.set(genomeMarkerIndex, chromosomeWeights.get(chromosomeMarkerIndex++));
				}

				phSvmChromosome.setNewStatus(ProcessStatus.COMPLEETED);
				chromoIndex++;
			}
			progressHandler.setNewStatus(ProcessStatus.FINALIZING);
		} else {
			// run SVM only once, genome-wide
			final int dSamples = parentDataSetSource.getNumMarkers();
			final int dEncoded = dSamples * getParams().getEncoder().getEncodingFactor();
			final int n = parentDataSetSource.getNumSamples();

			final List<MarkerKey> markerKeys = parentDataSetSource.getMarkersKeysSource();
			final List<SampleKey> validSamplesKeys = parentDataSetSource.getSamplesKeysSource();
			final List<Affection> validSampleAffections = parentDataSetSource.getSamplesInfosSource().getAffections();

			LOG.debug("Combi Association Test: #samples: " + n);
			LOG.debug("Combi Association Test: #markers: " + dSamples);
			LOG.debug("Combi Association Test: encoding factor: " + getParams().getEncoder().getEncodingFactor());
			LOG.debug("Combi Association Test: #SVM-dimensions: " + dEncoded);

			final List<Byte> majorAlleles = parentQAMarkersOperationDataSet.getKnownMajorAllele();
			final List<Byte> minorAlleles = parentQAMarkersOperationDataSet.getKnownMinorAllele();
			final List<int[]> markerGenotypesCounts = parentQAMarkersOperationDataSet.getGenotypeCounts();
			final MarkersGenotypesSource markersGenotypesSource = parentDataSetSource.getMarkersGenotypesSource();

			progressHandler.setNewStatus(ProcessStatus.RUNNING);

			final RunSVMResults svmResults = runEncodingAndSVM(
					markerKeys,
					majorAlleles,
					minorAlleles,
					markerGenotypesCounts,
					validSamplesKeys,
					validSampleAffections,
					markersGenotypesSource,
					getParams().getEncoder(),
					getParams().getEncodingParams(),
					getParams().getSolverLibrary(),
					getParams().getSolverParams(),
					null,
					null,
					null,
					svmPHs.get(0));
			weights = svmResults.getWeights();

			// TODO sort the weights (should already be absolute? .. hopefully not!)
			// TODO write stuff to a matrix (maybe the list of important markers?)
			// NOTE both of these are done in the ByCombiWeightsFilterOperation, so these todos are probably obsolete

			progressHandler.setNewStatus(ProcessStatus.FINALIZING);
		}


		dataSet.setWeights(weights);

		dataSet.finnishWriting();

		progressHandler.setNewStatus(ProcessStatus.COMPLEETED);

		return dataSet.getOperationKey();
	}

	private static Map<SampleKey, Double> encodeAffectionStates(final List<SampleKey> sampleKeys, final List<Affection> sampleAffections) {

		// we use LinkedHashMap to preserve the input order
		Map<SampleKey, Double> affectionStates
				= new LinkedHashMap<SampleKey, Double>(sampleKeys.size());
		// we iterate over sampleKeys now, to get the correct order
		Iterator<SampleKey> sampleKeysIt = sampleKeys.iterator();
		for (Affection sampleAffection : sampleAffections) {
			SampleKey key = sampleKeysIt.next();
			// NOTE
			//   We ensured earlier already,
			//   that we have only affected & unaffected samples,
			//   no unknown ones.
			Double encodedDisease = sampleAffection.equals(Affection.AFFECTED) ? 1.0 : -1.0; // XXX or should it be 0.0 instead of -1.0?
			affectionStates.put(key, encodedDisease);
		}

		return affectionStates;
	}

	/**
	 * Encodes the SVM samples, but just a few (one chunk of) markers at a time.
	 * So first all sample values for marker markerIndexFrom,
	 * then all sample values for marker markerIndexFrom + 1, ...
	 * until markerIndexFrom + markersChunkSize - 1.
	 * @param markerGTs
	 * @param majorAlleles
	 * @param minorAlleles
	 * @param markerGenotypesCounts
	 * @param encoder
	 * @param markerIndexFrom
	 * @param markersChunkSize
	 * @param dSamples dimension of the samples space (== #markers)
	 * @param n #samples
	 * @param encodedSamples
	 */
	static void encodeAndWhitenSamples(
			final List<GenotypesList> markerGTs,
			final List<Byte> majorAlleles,
			final List<Byte> minorAlleles,
			final List<int[]> markerGenotypesCounts,
			final GenotypeEncoder encoder,
			final GenotypeEncodingParams genotypeEncodingParams,
			final int markerIndexFrom,
			final int markersChunkSize,
			final int dSamples,
			final int n,
			final SamplesFeaturesStorage<Float> encodedSamples)
	{
		ProcessInfo encodingMarkersChunkPI = new DefaultProcessInfo("encoding markers chunk", null);
		ProgressHandler encodingMarkersChunkProgressSource
				// NOTE the IntegerP*H* would give more detailed info about the process,
				//   but it also slows it down.
//				= new IntegerProgressHandler(
//						encodingMarkersChunkPI,
//						markerIndexFrom,
//						markerIndexFrom + markersChunkSize - 1);
				= new NullProgressHandler(encodingMarkersChunkPI);

		encodingMarkersChunkProgressSource.setNewStatus(ProcessStatus.INITIALIZING);
		encodingMarkersChunkProgressSource.setNewStatus(ProcessStatus.RUNNING);
		for (int mi = markerIndexFrom; mi < (markerIndexFrom + markersChunkSize); mi++) {
			List<byte[]> gtsForOneMarker = markerGTs.get(mi);
			Byte majorAllele = majorAlleles.get(mi);
			Byte minorAllele = minorAlleles.get(mi);
			int[] genotypeCounts = markerGenotypesCounts.get(mi);
			encoder.encodeGenotypes(gtsForOneMarker, majorAllele, minorAllele, genotypeCounts, genotypeEncodingParams, encodedSamples, mi);

			encodingMarkersChunkProgressSource.setProgress(mi);
		}
		encodingMarkersChunkProgressSource.setNewStatus(ProcessStatus.COMPLEETED);
	}

	static float[][] encodeFeaturesAndCalculateKernel(
			final MarkerGenotypesEncoder markerGenotypesEncoder,
			final float[][] recyclableKernelMatrix,
			final ProgressHandler<Integer> creatingKernelMatrixPH)
			throws IOException
	{
		final int n = markerGenotypesEncoder.getNumSamples();

		final float[][] kernelMatrix;
		if (recyclableKernelMatrix != null && (recyclableKernelMatrix.length >= n)) {
			kernelMatrix = recyclableKernelMatrix;
		} else {
			try {
				// NOTE This allocates quite some memory!
				//   We use float instead of double to half the memory,
				//   this might be subject to change, as in:
				//   change to use double.
				LOG.info("allocating kernel-matrix memory: {}", Util.bytes2humanReadable(4L * n * n));
				kernelMatrix = new float[n][n];
			} catch (final OutOfMemoryError er) {
				throw new IOException(er);
			}
		}

		encodeFeaturesAndCreateKernelMatrix(
				markerGenotypesEncoder,
				kernelMatrix,
				creatingKernelMatrixPH);

		return kernelMatrix;
	}

	static class RunSVMResults {

		private final List<Double> weights;
		private final float[][] recyclableKernelMatrix;
		private final svm_problem recyclableProblem;
		private final Problem recyclableProblemLinear;

		RunSVMResults(
				final List<Double> weights,
				final float[][] recyclableKernelMatrix,
				final svm_problem recyclableProblem,
				final Problem recyclableProblemLinear)
		{
			this.weights = weights;
			this.recyclableKernelMatrix = recyclableKernelMatrix;
			this.recyclableProblem = recyclableProblem;
			this.recyclableProblemLinear = recyclableProblemLinear;
		}

		public List<Double> getWeights() {
			return weights;
		}

		public float[][] getRecyclableKernelMatrix() {
			return recyclableKernelMatrix;
		}

		public svm_problem getRecyclableProblem() {
			return recyclableProblem;
		}

		public Problem getRecyclableProblemLinear() {
			return recyclableProblemLinear;
		}
	}

	static RunSVMResults runEncodingAndSVM(
			List<MarkerKey> markerKeys,
			final List<Byte> majorAlleles,
			final List<Byte> minorAlleles,
			final List<int[]> markerGenotypesCounts,
			List<SampleKey> sampleKeys,
			List<Affection> sampleAffections,
			List<GenotypesList> markerGTs,
			GenotypeEncoder genotypeEncoder,
			final GenotypeEncodingParams genotypeEncodingParams,
			final SolverLibrary solverLibrary,
			final SolverParams solverParams,
			final float[][] recyclableKernelMatrix,
			final svm_problem recyclableProblem,
			final Problem recyclableProblemLinear,
			final SvmProgressHandler svmPH)
			throws IOException
	{
		final int dSamples = markerKeys.size();
		final int n = sampleKeys.size();

		LOG.debug("creating solver parameters ...");
		LOG.debug("solver param: eps: {}", solverParams.getEps());
		LOG.debug("solver param: C:   {}", solverParams.getC());
		final boolean useLibSvm = (solverLibrary == SolverLibrary.LIB_SVM);
		final svm_parameter libSvmParameters;
		final Parameter libLinearParameters;
		if (useLibSvm) {
			libSvmParameters = createLibSvmParameters(solverParams);
			libLinearParameters = null;
		} else {
			libSvmParameters = null;
			libLinearParameters = createLibLinearParameters(solverParams);
		}

		LOG.debug("encode affection states");
		Map<SampleKey, Double> encodedAffectionStates = encodeAffectionStates(
				sampleKeys,
				sampleAffections);

		final int maxChunkSize = MarkerGenotypesEncoder.calculateMaxChunkSize(genotypeEncoder, dSamples, n, null);
		LOG.debug("working with feature chunks of {} markers", maxChunkSize);

		MarkerGenotypesEncoder markerGenotypesEncoder = new MarkerGenotypesEncoder(
				markerGTs,
				majorAlleles,
				minorAlleles,
				markerGenotypesCounts,
				genotypeEncoder,
				genotypeEncodingParams,
				dSamples,
				n,
				maxChunkSize);

		if (spy != null) {
			spy.initializing(markerKeys, majorAlleles, minorAlleles, markerGenotypesCounts, sampleKeys, sampleAffections, markerGTs, genotypeEncoder, markerGenotypesEncoder);
		}

		final float[][] kernelMatrix;
		if (useLibSvm) {
			kernelMatrix = encodeFeaturesAndCalculateKernel(markerGenotypesEncoder, recyclableKernelMatrix, svmPH.getCreatingKernelMatrixPH());

			if (spy != null) {
				spy.kernelCalculated(kernelMatrix);
			}
		} else {
			kernelMatrix = null;
		}

		final Object libXProblem = createLibSvmOrLinearProblem(
					markerGenotypesEncoder,
					kernelMatrix,
					encodedAffectionStates.values(),
					libSvmParameters,
					libLinearParameters,
					recyclableProblem,
					recyclableProblemLinear,
					svmPH.getTranscribeKernelMatrixPH(),
					useLibSvm);
		final svm_problem libSvmProblem;
		final Problem libLinearProblem;
		if (useLibSvm) {
			libSvmProblem = (svm_problem) libXProblem;
			libLinearProblem = null;
		} else {
			libSvmProblem = null;
			libLinearProblem = (Problem) libXProblem;
		}

		final List<Double> weights = runSVM(
				markerGenotypesEncoder,
				libSvmProblem,
				libLinearProblem,
				libSvmParameters,
				libLinearParameters,
				genotypeEncoder,
				genotypeEncodingParams,
				svmPH);

		return new RunSVMResults(weights, kernelMatrix, libSvmProblem, libLinearProblem);
	}

	/**
	 *
	 * @param markersGenotypesSource
	 * @param majorAlleles
	 * @param minorAlleles
	 * @param markerGenotypesCounts
	 * @param genotypeEncoder
	 * @param dSamples dimension of the samples space (== #markers)
	 * @param n #samples
	 * @param maxChunkSize
	 * @return
	 * @throws IOException
	 */
	static MarkerGenotypesEncoder createMarkerGenotypesEncoder(
			final List<GenotypesList> markersGenotypesSource,
			final List<Byte> majorAlleles,
			final List<Byte> minorAlleles,
			final List<int[]> markerGenotypesCounts,
			final GenotypeEncoder genotypeEncoder,
			final GenotypeEncodingParams genotypeEncodingParams,
			final int dSamples,
			final int n,
			final int maxChunkSize)
			throws IOException
	{
		LOG.debug("working with feature chunks of {} markers", maxChunkSize);

		MarkerGenotypesEncoder markerGenotypesEncoder = new MarkerGenotypesEncoder(
				markersGenotypesSource,
				majorAlleles,
				minorAlleles,
				markerGenotypesCounts,
				genotypeEncoder,
				genotypeEncodingParams,
				dSamples,
				n,
				maxChunkSize);

		return markerGenotypesEncoder;
	}

	/**
	 * Doing all of this in one method, allows us to balance max memory usage
	 * vs back-end storage (usually the hard-disc) reads.
	 * NOTE XXX We could save more memory by storing only half of the kernel matrix,
	 * as it is symmetric.
	 * @param markerGenotypesEncoder
	 * @param kernelMatrix
	 * @param creatingKernelMatrixProgressSource
	 * @throws IOException
	 */
	private static void encodeFeaturesAndCreateKernelMatrix(
			final MarkerGenotypesEncoder markerGenotypesEncoder,
			final float[][] kernelMatrix,
			final ProgressHandler<Integer> creatingKernelMatrixProgressSource)
			throws IOException
	{
		final int n = markerGenotypesEncoder.getNumSamples();

		// initialize the kernelMatrix
		// this should not be required, if the array was just created,
		// but who knows who will call this function in what way in the future!?
		LOG.info("initialize kernel-matrix values to 0.0 ...");
		creatingKernelMatrixProgressSource.setNewStatus(ProcessStatus.INITIALIZING);
		for (int kernelRowIndex = 0; kernelRowIndex < n; kernelRowIndex++) {
			Arrays.fill(kernelMatrix[kernelRowIndex], 0.0f);

		}

		LOG.info("calculate the kernel-matrix ...");
		creatingKernelMatrixProgressSource.setNewStatus(ProcessStatus.RUNNING);
		// XXX this loop uses lots of time!
		for (int fci = 0; fci < markerGenotypesEncoder.size(); fci++) {
			final Float[][] featuresChunk = markerGenotypesEncoder.get(fci);
			final int numFeaturesInChunk = markerGenotypesEncoder.getChunkSize(fci);

			calculateKernelMatrixPart(n, kernelMatrix, featuresChunk, numFeaturesInChunk);
			creatingKernelMatrixProgressSource.setProgress(fci);
		}
		creatingKernelMatrixProgressSource.setNewStatus(ProcessStatus.FINALIZING);
		creatingKernelMatrixProgressSource.setNewStatus(ProcessStatus.COMPLEETED);
	}

	/**
	 * Calculates the part of the kernel matrix defined by
	 * the supplied chunk of the feature matrix.
	 * @param n #samples == kernel size
	 * @param kernelMatrix n*n matrix (though the actual 2D array might be bigger, do not use additional elements!)
	 * @param featuresChunk
	 * @param numFeaturesInChunk
	 */
	private static void calculateKernelMatrixPart(
			final int n,
			final float[][] kernelMatrix,
			final Float[][] featuresChunk,
			final int numFeaturesInChunk)
	{
		// Test data-set: ~ 22'000 markers, ~ 1'200 samples

		switch (KERNEL_CALCULATION_ALGORTIHM) {
			case 0:
				// With this version, encoding of the whole feature-matrix takes ~ 3'200s (73 markers chunks)
				for (int smi = 0; smi < numFeaturesInChunk; smi++) {
					for (int krci = 0; krci < n; krci++) { // kernel column sample index
						final float curColValue = featuresChunk[krci][smi];
						for (int krsi = 0; krsi < n; krsi++) { // kernel row sample index
							final float curRowValue = featuresChunk[krsi][smi];
							kernelMatrix[krsi][krci] += curRowValue * curColValue;
						}
					}
				}
				break;

			case 1:
				// With this version, encoding of the whole feature-matrix takes ~ 3'000s (73 markers chunks)
				for (int krci = 0; krci < n; krci++) { // kernel column sample index
					for (int smi = 0; smi < numFeaturesInChunk; smi++) {
						for (int krsi = 0; krsi < n; krsi++) { // kernel row sample index
							kernelMatrix[krsi][krci] += featuresChunk[krsi][smi] * featuresChunk[krci][smi];
						}
					}
				}
				break;

			case 2:
				// With this version, encoding of the whole feature-matrix takes ~ 1'900s (73 markers chunks)
				for (int krsi = 0; krsi < n; krsi++) { // kernel row sample index
					for (int smi = 0; smi < numFeaturesInChunk; smi++) {
						for (int krci = 0; krci < n; krci++) { // kernel column sample index
							kernelMatrix[krsi][krci] += featuresChunk[krsi][smi] * featuresChunk[krci][smi];
						}
					}
				}
				break;

			case 3:
				// With this version, encoding of the whole feature-matrix takes ~ 900s (73 markers chunks)
				for (int smi = 0; smi < numFeaturesInChunk; smi++) {
					for (int krsi = 0; krsi < n; krsi++) { // kernel row sample index
						for (int krci = 0; krci < n; krci++) { // kernel column sample index
							kernelMatrix[krsi][krci] += featuresChunk[krsi][smi] * featuresChunk[krci][smi];
						}
					}
				}
				break;

			case 4:
				// With this version, encoding of the whole feature-matrix takes ~ 430s (73 markers chunks)
				for (int krci = 0; krci < n; krci++) { // kernel column sample index
					for (int krsi = 0; krsi < n; krsi++) { // kernel row sample index
						for (int smi = 0; smi < numFeaturesInChunk; smi++) {
							kernelMatrix[krsi][krci] += featuresChunk[krsi][smi] * featuresChunk[krci][smi];
						}
					}
				}
				break;

			case 5:
				// With this version, encoding of the whole feature-matrix takes ~ 400s (73 markers chunks)
				for (int krsi = 0; krsi < n; krsi++) { // kernel row sample index
					for (int krci = 0; krci < n; krci++) { // kernel column sample index
						for (int smi = 0; smi < numFeaturesInChunk; smi++) {
							kernelMatrix[krsi][krci] += featuresChunk[krsi][smi] * featuresChunk[krci][smi];
						}
					}
				}
				break;

			default:
				throw new UnsupportedOperationException("KERNEL_CALCULATION_ALGORTIHM has to have a value in [0, 5], is: " + KERNEL_CALCULATION_ALGORTIHM);
		}
	}

	private static void createAndAddKernelNode(
			final svm_problem probSvm,
			final Problem probLinear,
			final int index1,
			final int index2,
			final double value,
			final boolean useLibSvm)
	{
		if (useLibSvm) {
			final svm_node sampleIndexNode = new svm_node();
			sampleIndexNode.index = index2;
			sampleIndexNode.value = value;
			probSvm.x[index1][index2] = sampleIndexNode;
		} else {
			final Feature sampleIndexNode = new FeatureNode(index2, value);
			probLinear.x[index1][index2 - 1] = sampleIndexNode;
		}
	}

	private static Object createLibSvmOrLinearProblem(
			final MarkerGenotypesEncoder markerGenotypesEncoder,
			final float[][] kernelMatrix,
			final Collection<Double> labels,
			final svm_parameter libSvmParameters,
			final Parameter libLinearParameters,
			final svm_problem recyclableProblem,
			final Problem recyclableProblemLinear,
			final ProgressHandler<Integer> transcribeKernelMatrixPH,
			final boolean useLibSvm)
			throws IOException
	{
		final svm_problem probSvm;
		final Problem probLinear;

		if (useLibSvm) {
			if (recyclableProblem == null) {
				probSvm = new svm_problem();
			} else {
				probSvm = recyclableProblem;
			}
			probLinear = null;
		} else {
			probSvm = null;
			if (recyclableProblemLinear == null) {
				probLinear = new Problem();
			} else {
				probLinear = recyclableProblemLinear;
			}
		}

		final int n = labels.size();

		// prepare the features
		if (useLibSvm && (libSvmParameters.kernel_type != svm_parameter.PRECOMPUTED)) {
			throw new IllegalStateException("unsupported lib{SVM, Linear} kernel type: " + libSvmParameters.kernel_type);
		}
		// transfer the kernel
		final int libSvmProblemBytes = (n * 8) + (n * n * (8 + 4 + 8));
		final String humanReadableLibSvmProblemMemory = Util.bytes2humanReadable(libSvmProblemBytes);
		LOG.debug("lib{SVM, Linear} preparation: required memory: ~ {} (on a 64bit system)", humanReadableLibSvmProblemMemory);

		if (useLibSvm) {
			if ((probSvm.x != null) && (probSvm.x.length >= n) && (probSvm.x[0].length >= (1 + n))) {
				LOG.debug("libSVM preparation: recycle kernel memory");
			} else {
				LOG.debug("libSVM preparation: allocate kernel memory");
				try {
					probSvm.x = new svm_node[n][1 + n];
				} catch (final OutOfMemoryError er) {
					throw new IOException(er);
				}
			}
		} else {
			final int numFeatures = markerGenotypesEncoder.getNumFeatures();
			if ((probLinear.x != null) && (probLinear.x.length >= n) && (probLinear.x[0].length >= numFeatures)) {
				LOG.debug("libLinear preparation: recycle features memory");
			} else {
				LOG.debug("libLinear preparation: allocate features memory");
				try {
					probLinear.x = new Feature[n][numFeatures];
				} catch (final OutOfMemoryError er) {
					throw new IOException(er);
				}
			}
		}

		LOG.debug("lib{SVM, Linear} preparation: store the kernel elements");
		transcribeKernelMatrixPH.setNewStatus(ProcessStatus.INITIALIZING);
		transcribeKernelMatrixPH.setNewStatus(ProcessStatus.RUNNING);
		for (int si = 0; si < n; si++) {
			if (useLibSvm) {
				// This is required by the libSVM standard for a PRECOMPUTED kernel, not for libLinear
				createAndAddKernelNode(probSvm, probLinear, si, 0, si + 1, useLibSvm);

				for (int s2i = si; s2i < n; s2i++) {
					final double kernelValue = kernelMatrix[s2i][si]; // XXX or indices other way around?

					createAndAddKernelNode(probSvm, probLinear, si, 1 + s2i, kernelValue, useLibSvm);
					if (si != s2i) {
						// because the matrix is symmetric
						createAndAddKernelNode(probSvm, probLinear, s2i, 1 + si, kernelValue, useLibSvm);
					}
				}
			} else {
				for (int fci = 0; fci < markerGenotypesEncoder.size(); fci++) {
					final Float[][] featuresChunk = markerGenotypesEncoder.get(fci);
					final int numFeaturesInChunk = markerGenotypesEncoder.getChunkSize(fci);

					final int featuresOffset = fci * markerGenotypesEncoder.getMaxChunkSize();
					for (int sampleIndex = 0; sampleIndex < n; sampleIndex++) {
						for (int localFeatureIndex = 0; localFeatureIndex < numFeaturesInChunk; localFeatureIndex++) {
							probLinear.x[sampleIndex][featuresOffset + localFeatureIndex]
									= new FeatureNode(1 + featuresOffset + localFeatureIndex, featuresChunk[sampleIndex][localFeatureIndex]);
						}
					}
				}
			}

			transcribeKernelMatrixPH.setProgress(si + 1);
		}
		transcribeKernelMatrixPH.setNewStatus(ProcessStatus.FINALIZING);
		transcribeKernelMatrixPH.setNewStatus(ProcessStatus.COMPLEETED);

		// prepare the labels
		LOG.debug("lib{SVM, Linear} preparation: store the labels");
		final int l = n;
		double[] y;
		if (useLibSvm) {
			probSvm.l = l;
			y = probSvm.y;
		} else {
			probLinear.l = n;
			probLinear.n = markerGenotypesEncoder.getNumFeatures();
			y = probLinear.y;
		}
		if ((y != null) && (y.length >= l)) {
			LOG.debug("lib{SVM, Linear} preparation: recycle labels memory");
		} else {
			y = new double[l];
			if (useLibSvm) {
				probSvm.y = y;
			} else {
				probLinear.y = y;
			}
		}
		Iterator<Double> itY = labels.iterator();
		for (int si = 0; si < l; si++) {
			final double label = itY.next();
			y[si] = label;
		}

		final Object prob;
		if (useLibSvm) {
			prob = probSvm;
		} else {
			prob = probLinear;
		}
		return prob;
	}

	private static Parameter createLibLinearParameters(final SolverParams solverParams) {

		return new Parameter(SolverType.L2R_L1LOSS_SVC_DUAL, solverParams.getC(), solverParams.getEps()/*, p*/);
	}

	private static svm_parameter createLibSvmParameters(final SolverParams solverParams) {

		svm_parameter svmParams = new svm_parameter();

		// TODO make many of these values configurable throuhg the *Params! see the matlab scripts

		// possible values: C_SVC, NU_SVC, ONE_CLASS, EPSILON_SVR, NU_SVR
		svmParams.svm_type = svm_parameter.C_SVC;
		// possible values: LINEAR, POLY, RBF, SIGMOID, PRECOMPUTED
//		svmParams.kernel_type = svm_parameter.LINEAR;
		svmParams.kernel_type = svm_parameter.PRECOMPUTED;
		// for poly
		svmParams.degree = 3;
		// for poly/RBF/sigmoid
		svmParams.gamma = 0.0;
		// for poly/sigmoid
		svmParams.coef0 = 0;

		// these are for training only
		// The cache size in MB
		svmParams.cache_size = 40; // TODO make configurable, or at least dynamic (depending on available memory and/or problem size)
		// stopping criteria
		svmParams.eps = solverParams.getEps();
		// for C_SVC, EPSILON_SVR and NU_SVR
		svmParams.C = solverParams.getC();
		// for C_SVC
		svmParams.nr_weight = 0;
		// for C_SVC
		svmParams.weight_label = new int[svmParams.nr_weight];
		// for C_SVC
		svmParams.weight = new double[svmParams.nr_weight];
		// for NU_SVC, ONE_CLASS, and NU_SVR
		svmParams.nu = 0.5;
		// for EPSILON_SVR
		svmParams.p = 0.5;
		// use the shrinking heuristics
		svmParams.shrinking = 1;
		// do probability estimates
		svmParams.probability = 0;

		return svmParams;
	}

	/**
	 * Calculate the weights 'w' in the original/feature space,
	 * using the weights 'alpha' from the kernel-space.
	 * In our case, the original space as known to the SVM
	 * is the encoded genotypes space, not the actual genotype space yet.
	 * <math>\mathbf{w} = \sum_i \alpha_i y_i \mathbf{x}_i \quad \forall i = 1 ... nSamples</math>
	 * with <math>\mathbf{w}, \mathbf{x}_i</math> having dimension <math>dEncoded</math>.
	 * @param nonZeroAlphas the SVM problem weights in kernel-space [nSamples]
	 * @param xs the support-vector coordinates in the feature space [nSamples * dEncoded]
	 * @param ys the labels of the data (-1, or 1) [nSamples]
	 * @param calculateOriginalSpaceWeightsPH
	 * @return the SVM problem weights 'w' in the feature space [dEncoded]
	 */
	private static List<Double> calculateOriginalSpaceWeights(
			Map<Integer, Double> nonZeroAlphas,
			final MarkerGenotypesEncoder xs,
			final double[] ys,
			final ProgressHandler<Integer> calculateOriginalSpaceWeightsPH)
	{
		calculateOriginalSpaceWeightsPH.setNewStatus(ProcessStatus.INITIALIZING);

		// number of data-points/samples
		final int n = ys.length;
		// number of dimensions/features/markers*encodingFactor
		final int d = xs.getNumFeatures();

		double[] weights = new double[d];
		// This is probably not required, as Java initialized to zero anyway,
		// but it does not hurt to make things clear.
		Arrays.fill(weights, 0.0);

		if (calculateOriginalSpaceWeightsPH instanceof IntegerProgressHandler) { // HACK
			((IntegerProgressHandler) calculateOriginalSpaceWeightsPH).setEndState(xs.size() - 1);
		}
		calculateOriginalSpaceWeightsPH.setNewStatus(ProcessStatus.RUNNING);
		for (int ci = 0; ci < xs.size(); ci++) {
			final Float[][] featuresChunk = xs.get(ci);
			final int chunkSize = xs.getChunkSize(ci);
			final int firstFeatureIndex = ci * xs.getMaxChunkSize();

			for (Map.Entry<Integer, Double> nonZeroAlpha : nonZeroAlphas.entrySet()) {
				final int svi = nonZeroAlpha.getKey();
				final double alpha = nonZeroAlpha.getValue();
				for (int cldi = 0; cldi < chunkSize; cldi++) {
					final int di = firstFeatureIndex + cldi;
					final double x = featuresChunk[svi][cldi];
					// NOTE We dismiss the y, which would be part of normal SVM,
					// because we want the absolute sum (i forgot again why so :/ )
//					final double y = ...;
//					final double alphaYXi = -alpha * y * x; // XXX why here change sign again?!?!
					final double alphaYXi = alpha * x;
					weights[di] += alphaYXi;
				}
			}
			calculateOriginalSpaceWeightsPH.setProgress(ci);
		}
		calculateOriginalSpaceWeightsPH.setNewStatus(ProcessStatus.FINALIZING);

		// convert array to list
		List<Double> weightsList = new ArrayList<Double>(weights.length);
		for (int wi = 0; wi < weights.length; wi++) {
			weightsList.add(weights[wi]);
		}

		calculateOriginalSpaceWeightsPH.setNewStatus(ProcessStatus.COMPLEETED);

		return weightsList;
	}

	/**
	 * @return feature index and value of non-zero alphas
	 */
	public static Map<Integer, Double> extractNonZeroAlphas(final svm_model svmModel) {

		final int numClasses = svmModel.sv_coef.length + 1;
		final int classIndex = 0;
		final int numFeatures = svmModel.sv_coef[classIndex].length;
		final Map<Integer, Double> nonZeroAlphas = new LinkedHashMap<Integer, Double>(svmModel.sv_coef[classIndex].length);
		for (int nzai = 0; nzai < svmModel.sv_coef[classIndex].length; nzai++) {
			final int featureIndex = (int) svmModel.SV[nzai][classIndex].value - 1; // NOTE this only works with PRECOMPUTED!
			final double featureValue = svmModel.sv_coef[classIndex][nzai];
			nonZeroAlphas.put(featureIndex, featureValue);
		}

		return nonZeroAlphas;
	}

	public static List<Double> extractFeatureWeights(final Model svmModel) {

		final double[] featureWeightsRaw = svmModel.getFeatureWeights();
		final List<Double> featureWeights = new ArrayList<Double>(featureWeightsRaw.length);
		for (int featureIndex = 0; featureIndex < featureWeightsRaw.length; featureIndex++) {
			featureWeights.add(featureWeightsRaw[featureIndex]);
		}

		return featureWeights;
	}

	private static List<Double> runSVM(
			final MarkerGenotypesEncoder markerGenotypesEncoder,
			final svm_problem libSvmProblem,
			final Problem libLinearProblem,
			final svm_parameter libSvmParameters,
			final Parameter libLinearParameters,
			final GenotypeEncoder genotypeEncoder,
			final GenotypeEncodingParams genotypeEncodingParams,
			final SvmProgressHandler svmPH)
	{
		final boolean useLibSvm = (libSvmProblem != null);
		final ProgressHandler<?> trainSVMPH = svmPH.getTrainSVMPH();
		trainSVMPH.setNewStatus(ProcessStatus.INITIALIZING);
		// dimension of the encoded samples space (== #markers * encoding-factor)
		final int dEncoded = markerGenotypesEncoder.getNumFeatures();
		final int dSamples = dEncoded / genotypeEncoder.getEncodingFactor();
		final int n = useLibSvm ? libSvmProblem.x.length : libLinearProblem.x.length;

		LOG.debug("train the SVM model");
		trainSVMPH.setNewStatus(ProcessStatus.RUNNING);
		final svm_model svmModel;
		final Model svmModelLinear;
		if (useLibSvm) {
			svmModel = svm.svm_train(libSvmProblem, libSvmParameters);
			svmModelLinear = null;
		} else {
			svmModel = null;
			svmModelLinear = Linear.train(libLinearProblem, libLinearParameters);
		}
		trainSVMPH.setNewStatus(ProcessStatus.FINALIZING);

		final List<Double> weightsEncoded;
		if (useLibSvm) {
			if (spy != null) {
				spy.svmModelTrained(svmModel, svmModelLinear);
			}
			// sample index and value of non-zero alphas
			final Map<Integer, Double> nonZeroAlphas;
			nonZeroAlphas = extractNonZeroAlphas(svmModel);
			trainSVMPH.setNewStatus(ProcessStatus.COMPLEETED);

			LOG.debug("calculate original space weights from alphas");
			weightsEncoded = calculateOriginalSpaceWeights(
					nonZeroAlphas,
					markerGenotypesEncoder,
					useLibSvm ? libSvmProblem.y : libLinearProblem.y,
					svmPH.getCalculateOriginalSpaceWeightsPH());
		} else {
			weightsEncoded = extractFeatureWeights(svmModelLinear);
		}

		if (spy != null) {
			spy.originalSpaceWeightsCalculated(weightsEncoded);
		}

		LOG.debug("weights(encoded): " + weightsEncoded.size());
//		LOG.debug("\t" + weightsEncoded); // this is way too verbose

		LOG.debug("dSamples: " + dSamples);
		LOG.debug("dEncoded: " + dEncoded);
		LOG.debug("n: " + n);
		LOG.debug("genotypeEncoder: " + genotypeEncoder.getClass().getSimpleName());
		LOG.debug("encodingFactor: " + genotypeEncoder.getEncodingFactor());

		LOG.debug("decode weights from the encoded feature space into marker space");
		List<Double> weights = new ArrayList<Double>(dSamples);
		genotypeEncoder.decodeWeights(weightsEncoded, genotypeEncodingParams, weights);

		if (spy != null) {
			spy.decodedWeightsCalculated(weights);
		}

		return weights;
	}
}

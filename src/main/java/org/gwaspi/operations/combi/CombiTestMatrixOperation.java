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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
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
 * TODO
 * - n / nSamples : #samples == #data-points in the SVM feature space == rows & columns in the SVM kernel matrix
 * - dSamples : #markers == #SNPs
 * - dEncoded : #markers * encodingFactor == #dimensions in the SVM  feature space
 */
public class CombiTestMatrixOperation
		extends AbstractOperationCreatingOperation<CombiTestOperationDataSet, CombiTestOperationParams>
{
	private static final Logger LOG
			= LoggerFactory.getLogger(CombiTestMatrixOperation.class);

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

	public CombiTestMatrixOperation(CombiTestOperationParams params) {
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

	@Override
	protected ProgressHandler getProgressHandler() throws IOException {

		if (customProgressHandler == null) {
			final DataSetSource parentDataSetSource = getParentDataSetSource();
			final GenotypeEncoder genotypeEncoder = getParams().getEncoder();
			final int n = parentDataSetSource.getNumSamples();
			final int dSamples = parentDataSetSource.getNumMarkers();
			final int dEncoded = dSamples * genotypeEncoder.getEncodingFactor();
			final int maxChunkSize = MarkerGenotypesEncoder.calculateMaxChunkSize(genotypeEncoder, dSamples, n, null);
			final int numChunks = MarkerGenotypesEncoder.calculateNumChunks(dSamples, maxChunkSize);

			svmPHs = new ArrayList<SvmProgressHandler>();
			svmPHs.add(new SvmProgressHandler(PI_SVM_GENOME, n, dEncoded, numChunks));

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

		List<Double> weights = runEncodingAndSVM(
				markerKeys,
				majorAlleles,
				minorAlleles,
				markerGenotypesCounts,
				validSamplesKeys,
				validSampleAffections,
				markersGenotypesSource,
				getParams().getEncoder(),
				svmPHs.get(0));

		// TODO sort the weights (should already be absolute? .. hopefully not!)
		// TODO write stuff to a matrix (maybe the list of important markers?)
		// NOTE both of these are done in the ByCombiWeightsFilterOperation, so these todos are probably obsolete

		progressHandler.setNewStatus(ProcessStatus.FINALIZING);

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
	 * @param sampleAffections
	 * @param encoder
	 * @param dSamples
	 * @param n
	 * @param usedSamples
	 * @param encodedSamples
	 * @throws IOException
	 */
	static void encodeAndWhitenSamples(
			final List<GenotypesList> markerGTs,
			final List<Byte> majorAlleles,
			final List<Byte> minorAlleles,
			final List<int[]> markerGenotypesCounts,
			final GenotypeEncoder encoder,
			final int markerIndexFrom,
			final int markersChunkSize,
			final int dSamples,
			final int n,
			final SamplesFeaturesStorage<Float> encodedSamples)
			throws IOException
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
			encoder.encodeGenotypes(gtsForOneMarker, majorAllele, minorAllele, genotypeCounts, encodedSamples, mi);

			encodingMarkersChunkProgressSource.setProgress(mi);
		}
		encodingMarkersChunkProgressSource.setNewStatus(ProcessStatus.COMPLEETED);
	}

	static float[][] encodeFeaturesAndCalculateKernel(
			final int n,
			final MarkerGenotypesEncoder markerGenotypesEncoder,
			final ProgressHandler<Integer> creatingKernelMatrixPH)
			throws IOException
	{
		final float[][] kernelMatrix;
		try {
			// NOTE This allocates quite some memory!
			//   We use float instead of double to half the memory,
			//   this might be subject to change, as in:
			//   change to use double.
			LOG.info("allocating kernel-matrix memory: {}", Util.bytes2humanReadable(4L * n * n));
			kernelMatrix = new float[n][n];
		} catch (OutOfMemoryError er) {
			throw new IOException(er);
		}

		encodeFeaturesAndCreateKernelMatrix(
				markerGenotypesEncoder,
				kernelMatrix,
				creatingKernelMatrixPH);

		return kernelMatrix;
	}

	static List<Double> runEncodingAndSVM(
			List<MarkerKey> markerKeys,
			final List<Byte> majorAlleles,
			final List<Byte> minorAlleles,
			final List<int[]> markerGenotypesCounts,
			List<SampleKey> sampleKeys,
			List<Affection> sampleAffections,
			List<GenotypesList> markerGTs,
			GenotypeEncoder genotypeEncoder,
			final SvmProgressHandler svmPH)
			throws IOException
	{
		final int dSamples = markerKeys.size();
		final int n = sampleKeys.size();

		LOG.debug("create SVM parameters");
		svm_parameter libSvmParameters = createLibSvmParameters();

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
				dSamples,
				n,
				maxChunkSize);

		if (spy != null) {
			spy.initializing(markerKeys, majorAlleles, minorAlleles, markerGenotypesCounts, sampleKeys, sampleAffections, markerGTs, genotypeEncoder, markerGenotypesEncoder);
		}

		final float[][] kernelMatrix = encodeFeaturesAndCalculateKernel(n, markerGenotypesEncoder, svmPH.getCreatingKernelMatrixPH());

		if (spy != null) {
			spy.kernelCalculated(kernelMatrix);
		}

		svm_problem libSvmProblem = createLibSvmProblem(
				kernelMatrix,
				encodedAffectionStates.values(),
				libSvmParameters,
				svmPH.getTranscribeKernelMatrixPH());

		return runSVM(markerGenotypesEncoder, libSvmProblem, genotypeEncoder, svmPH);
	}

	static MarkerGenotypesEncoder createMarkerGenotypesEncoder(
			final List<GenotypesList> markersGenotypesSource,
			final List<Byte> majorAlleles,
			final List<Byte> minorAlleles,
			final List<int[]> markerGenotypesCounts,
			final GenotypeEncoder genotypeEncoder,
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
	 * @param markerGTs
	 * @param sampleAffections
	 * @param encoder
	 * @param dSamples
	 * @param dEncoded
	 * @param n
	 * @param usedSamples
	 * @param encodedSamples
	 * @return
	 * @throws IOException
	 */
	private static void encodeFeaturesAndCreateKernelMatrix(
			final MarkerGenotypesEncoder markerGenotypesEncoder,
			final float[][] kernelMatrix,
			ProgressHandler<Integer> creatingKernelMatrixProgressSource)
			throws IOException
	{
		// initialize the kernelMatrix
		// this should not be required, if the array was just created,
		// but who knows who will call this function in what way in the future!?
		creatingKernelMatrixProgressSource.setNewStatus(ProcessStatus.INITIALIZING);
		for (float[] kernelMatrixRow : kernelMatrix) {
			for (int ci = 0; ci < kernelMatrixRow.length; ci++) {
				kernelMatrixRow[ci] = 0.0f;
			}
		}
		creatingKernelMatrixProgressSource.setNewStatus(ProcessStatus.RUNNING);

		// XXX this loop uses lots of time!
		for (int fci = 0; fci < markerGenotypesEncoder.size(); fci++) {
			final Float[][] featuresChunk = markerGenotypesEncoder.get(fci);
			final int numFeaturesInChunk = markerGenotypesEncoder.getChunkSize(fci);

			calculateKernelMatrixPart(kernelMatrix, featuresChunk, numFeaturesInChunk);
			creatingKernelMatrixProgressSource.setProgress(fci);
		}

		creatingKernelMatrixProgressSource.setNewStatus(ProcessStatus.FINALIZING);
		creatingKernelMatrixProgressSource.setNewStatus(ProcessStatus.COMPLEETED);
	}

	/**
	 * Calculates the part of the kernel matrix defined by
	 * the supplied chunk of the feature matrix.
	 * @param kernelMatrix
	 * @param featuresChunk
	 * @param numFeaturesInChunk
	 */
	private static void calculateKernelMatrixPart(
			final float[][] kernelMatrix,
			final Float[][] featuresChunk,
			final int numFeaturesInChunk)
	{
		final int n = kernelMatrix.length;

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

	private static svm_problem createLibSvmProblem(
			float[][] K,
			Collection<Double> Y,
			svm_parameter libSvmParameters,
			final ProgressHandler<Integer> transcribeKernelMatrixPH)
			throws IOException
	{
		svm_problem prob = new svm_problem();

		final int n = Y.size();

		// prepare the features
		if (libSvmParameters.kernel_type == svm_parameter.PRECOMPUTED) {
			// KERNEL
			final int libSvmProblemBytes = (n * 8) + (n * n * (8 + 4 + 8));
			final String humanReadableLibSvmProblemMemory = Util.bytes2humanReadable(libSvmProblemBytes);
			LOG.debug("libSVM preparation: required memory: ~ {} (on a 64bit system)", humanReadableLibSvmProblemMemory);

			LOG.debug("libSVM preparation: allocate kernel memory");
			try {
				prob.x = new svm_node[n][1 + n];
			} catch (OutOfMemoryError er) {
				throw new IOException(er);
			}

			LOG.debug("libSVM preparation: store the kernel elements");
			transcribeKernelMatrixPH.setNewStatus(ProcessStatus.INITIALIZING);
			transcribeKernelMatrixPH.setNewStatus(ProcessStatus.RUNNING);
			for (int si = 0; si < n; si++) {
				// This is required by the libSVM standard for a PRECOMPUTED kernel
				svm_node sampleIndexNode = new svm_node();
				sampleIndexNode.index = 0;
				sampleIndexNode.value = si + 1;
				prob.x[si][0] = sampleIndexNode;

				for (int s2i = si; s2i < n; s2i++) {
					final double kernelValue = K[s2i][si]; // XXX or indices other way around?

					svm_node curNode = new svm_node();
					curNode.index = 1 + s2i;
					curNode.value = kernelValue;
					prob.x[si][1 + s2i] = curNode;
					if (si != s2i) {
						// because the matrix is symmetric
						svm_node curNodeT = new svm_node();
						curNodeT.index = 1 + si;
						curNodeT.value = kernelValue;
						prob.x[s2i][1 + si] = curNodeT;
					}
				}

				transcribeKernelMatrixPH.setProgress(si + 1);
			}
			transcribeKernelMatrixPH.setNewStatus(ProcessStatus.FINALIZING);
			transcribeKernelMatrixPH.setNewStatus(ProcessStatus.COMPLEETED);
		} else {
			throw new IllegalStateException("unsupported libSVM kernel type: " + libSvmParameters.kernel_type);
		}

		// prepare the labels
		LOG.debug("libSVM preparation: store the labels");
		prob.l = n;
		prob.y = new double[prob.l];
		Iterator<Double> itY = Y.iterator();
		for (int si = 0; si < n; si++) {
			double y = itY.next();
			prob.y[si] = y;
		}

		return prob;
	}

	private static svm_parameter createLibSvmParameters() {

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
		svmParams.cache_size = 40;
		// stopping criteria
		svmParams.eps = 1E-7;
		// for C_SVC, EPSILON_SVR and NU_SVR
		svmParams.C = 1.0;
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
	 * @param alphas the SVM problem weights in kernel-space [nSamples]
	 * @param xs the support-vector coordinates in the feature space [nSamples * dEncoded]
	 * @param ys the labels of the data (-1, or 1) [nSamples]
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
					final double alphaYXi = -alpha * x; // XXX why here change sign again?!?!
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
	 * @return sample index and value of non-zero alphas
	 */
	private static Map<Integer, Double> extractNonZeroAlphas(final svm_model svmModel) {

		// sample index and value of non-zero alphas
		Map<Integer, Double> nonZeroAlphas = new LinkedHashMap<Integer, Double>(svmModel.sv_coef[0].length);
		for (int i = 0; i < svmModel.sv_coef[0].length; i++) {
			final double value = svmModel.sv_coef[0][i];
			int index = (int) svmModel.SV[i][0].value - 1; // NOTE this only works with PRECOMPUTED!
			nonZeroAlphas.put(index, value);
		}

		return nonZeroAlphas;
	}

	private static List<Double> runSVM(
			final MarkerGenotypesEncoder markerGenotypesEncoder,
			svm_problem libSvmProblem,
			GenotypeEncoder genotypeEncoder,
			final SvmProgressHandler svmPH)
	{
		final ProgressHandler<?> trainSVMPH = svmPH.getTrainSVMPH();
		trainSVMPH.setNewStatus(ProcessStatus.INITIALIZING);
		final int dEncoded = markerGenotypesEncoder.getNumFeatures();
		final int dSamples = dEncoded / genotypeEncoder.getEncodingFactor();
		final int n = libSvmProblem.x.length;

		LOG.debug("create SVM parameters");
		svm_parameter libSvmParameters = createLibSvmParameters();

		LOG.debug("train the SVM model");
		trainSVMPH.setNewStatus(ProcessStatus.RUNNING);
		svm_model svmModel = svm.svm_train(libSvmProblem, libSvmParameters);
		trainSVMPH.setNewStatus(ProcessStatus.FINALIZING);

		if (spy != null) {
			spy.svmModelTrained(svmModel);
		}

		// sample index and value of non-zero alphas
		Map<Integer, Double> nonZeroAlphas = extractNonZeroAlphas(svmModel);
		trainSVMPH.setNewStatus(ProcessStatus.COMPLEETED);

		LOG.debug("calculate original space weights from alphas");
		List<Double> weightsEncoded = calculateOriginalSpaceWeights(
				nonZeroAlphas, markerGenotypesEncoder, libSvmProblem.y, svmPH.getCalculateOriginalSpaceWeightsPH());

		if (spy != null) {
			spy.originalSpaceWeightsCalculated(weightsEncoded);
		}

		LOG.debug("weights(encoded): " + weightsEncoded.size());
		LOG.debug("\t" + weightsEncoded);

		LOG.debug("dSamples: " + dSamples);
		LOG.debug("dEncoded: " + dEncoded);
		LOG.debug("n: " + n);
		LOG.debug("genotypeEncoder: " + genotypeEncoder.getClass().getSimpleName());
		LOG.debug("encodingFactor: " + genotypeEncoder.getEncodingFactor());

		LOG.debug("decode weights from the encoded feature space into marker space");
		List<Double> weights = new ArrayList<Double>(dSamples);
		genotypeEncoder.decodeWeights(weightsEncoded, weights);

		if (spy != null) {
			spy.decodedWeightsCalculated(weights);
		}

		return weights;
	}
}

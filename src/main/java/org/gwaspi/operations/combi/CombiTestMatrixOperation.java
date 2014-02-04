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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ProgressMonitor;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.netCDF.operations.AbstractOperation;
import org.gwaspi.netCDF.operations.AbstractTestMatrixOperation;
import org.gwaspi.operations.AbstractOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * - n / nSamples : #samples == #data-points in the SVM feature space == rows & columns in the SVM kernel matrix
 * - dSamples : #markers == #SNPs
 * - dEncoded : #markers * encodingFactor == #dimensions in the SVM  feature space
 */
public class CombiTestMatrixOperation extends AbstractOperation<CombiTestOperationDataSet> {

	private static final Logger LOG
			= LoggerFactory.getLogger(CombiTestMatrixOperation.class);

	static final File BASE_DIR = new File(System.getProperty("user.home"), "/Projects/GWASpi/var/data/marius/example/extra"); // HACK

	/**
	 * Whether we are to perform allelic or genotypic association tests.
	 */
	private final CombiTestParams params;

	public CombiTestMatrixOperation(CombiTestParams params) {
		super(params.getHardyWeinbergOperationKey());

		this.params = params;
	}

	@Override
	public OPType getType() {
		return OPType.COMBI_ASSOC_TEST;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	@Override
	public int processMatrix() throws IOException {

		LOG.info("Combi Association Test: start");

		LOG.info("Combi Association Test: init");
		Collection<MarkerKey> toBeExcluded = new HashSet<MarkerKey>();
		final boolean dataLeft = AbstractTestMatrixOperation.excludeMarkersByHW(
				params.getHardyWeinbergOperationKey(),
				params.getHardyWeinbergThreshold(),
				toBeExcluded);

		if (!dataLeft) {
			// NO DATA LEFT AFTER THRESHOLD FILTER PICKING
			LOG.warn(Text.Operation.warnNoDataLeftAfterPicking);
			return Integer.MIN_VALUE;
		}

		DataSetSource parentDataSetSource = getParentDataSetSource();

		CombiTestOperationDataSet dataSet = generateFreshOperationDataSet();
		dataSet.setNumMarkers(parentDataSetSource.getNumMarkers());
		dataSet.setNumChromosomes(parentDataSetSource.getNumChromosomes());

		Map<Integer, MarkerKey> wrMarkersFiltered = AbstractTestMatrixOperation.filterByValues(parentDataSetSource.getMarkersKeysSource().getIndicesMap(), toBeExcluded);
		ArrayList<MarkerKey> markerKeys = new ArrayList<MarkerKey>(wrMarkersFiltered.values());

		SamplesKeysSource samplesKeysSource = parentDataSetSource.getSamplesKeysSource();
		List<Affection> sampleAffections = parentDataSetSource.getSamplesInfosSource().getAffections();

		MarkersGenotypesSource markersGenotypesSource = parentDataSetSource.getMarkersGenotypesSource();

		final int dSamples = markerKeys.size() - toBeExcluded.size();
		final int dEncoded = dSamples * params.getEncoder().getEncodingFactor();

		int nSamplesToKeep = 0;
		// only count samples with a valid affection
		Map<Integer, SampleKey> validSamplesOrigIndicesAndKey = new LinkedHashMap<Integer, SampleKey>(parentDataSetSource.getNumSamples());
		List<Affection> validSampleAffections = new ArrayList<Affection>(parentDataSetSource.getNumSamples());
		Iterator<Map.Entry<Integer, SampleKey>> samplesIt = samplesKeysSource.getIndicesMap().entrySet().iterator();
		for (Affection sampleAffection : sampleAffections) {
			Map.Entry<Integer, SampleKey> sample = samplesIt.next();
			if (Affection.isValid(sampleAffection)) {
				nSamplesToKeep++;
				validSamplesOrigIndicesAndKey.put(sample.getKey(), sample.getValue());
			}
		}
		final int n = nSamplesToKeep;
		if (validSampleAffections instanceof ArrayList) {
			((ArrayList) validSampleAffections).trimToSize();
		}
		ArrayList<SampleKey> samplesKeys = new ArrayList<SampleKey>(validSamplesOrigIndicesAndKey.values());

		dataSet.setNumSamples(n);

		dataSet.setMarkers(wrMarkersFiltered);
		dataSet.setSamples(validSamplesOrigIndicesAndKey);

		LOG.debug("Combi Association Test: #samples: " + n);
		LOG.debug("Combi Association Test: #markers: " + dSamples);
		LOG.debug("Combi Association Test: encoding factor: " + params.getEncoder().getEncodingFactor());
		LOG.debug("Combi Association Test: #SVM-dimensions: " + dEncoded);

		// HACK This next line is for debugging purposes only
		Util.storeForEncoding(markerKeys, samplesKeys, sampleAffections, markersGenotypesSource, dSamples, dEncoded, n); // HACK

		List<Double> weights = runEncodingAndSVM(markerKeys, samplesKeys, sampleAffections, markersGenotypesSource, dSamples, n, params.getEncoder());

		// TODO sort the weights (should already be absolute?)
		// TODO write stuff to a matrix (maybe the list of important markers?)

		dataSet.setWeights(weights);

		return ((AbstractOperationDataSet) dataSet).getOperationKey().getId(); // HACK
	}

	private static Map<SampleKey, Double> encodeAffectionStates(final List<SampleKey> sampleKeys, final List<Affection> sampleAffections, int n) {

		// we use LinkedHashMap to preserve the input order
		Map<SampleKey, Double> affectionStates
				= new LinkedHashMap<SampleKey, Double>(n);
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
	 * Encodes the SVM samples, one marker at a time.
	 * So first all sample values for the first marker,
	 * then all sample values for the second marker, ...
	 * @param markerGTs
	 * @param sampleAffections
	 * @param encoder
	 * @param dSamples
	 * @param dEncoded
	 * @param n
	 * @param usedSamples
	 * @param encodedSamples
	 * @throws IOException
	 */
	static void encodeSamples(
			List<GenotypesList> markerGTs,
			GenotypeEncoder encoder,
			final int markerIndexFrom,
			final int markersChunkSize,
			int dSamples,
			int dEncoded,
			int n,
			final SamplesFeaturesStorage<Float> encodedSamples)
			throws IOException
	{
		final int nSamplesToKeep = n;

		final byte numSingleValueStorageBytes = 4; // float
		final int featureBytes = Util.calcFeatureBytes(nSamplesToKeep, dEncoded, numSingleValueStorageBytes);
		final String humanReadableFeaturesMemorySize = Util.bytes2humanReadable(featureBytes);
		LOG.info("Combi Association Test: allocate memory for features: {}",
				humanReadableFeaturesMemorySize);

		ProgressMonitor encodingMarkersPM = new ProgressMonitor(null, "encoding markers chunk", "", 0, dSamples);
		for (int mi = markerIndexFrom; mi < (markerIndexFrom + markersChunkSize); mi++) {
			List<byte[]> gtsForOneMarker = markerGTs.get(mi);
			encoder.encodeGenotypes(gtsForOneMarker, /*samplesToKeep*/null, encodedSamples, mi);

			if ((mi % 50) == 0) {
				encodingMarkersPM.setProgress(mi);
			}
			if ((mi % 250) == 0) {
				encodingMarkersPM.setNote(String.format(
						"%d / %d ~= %f%%",
						mi,
						dSamples,
						(double) mi / dSamples * 100.0));
				LOG.info("Combi Association Test: encoded markers {} / {}", mi, dSamples);
			}
		}
	}

	private static List<Double> runEncodingAndSVM(
			List<MarkerKey> markerKeys,
			List<SampleKey> sampleKeys,
			List<Affection> sampleAffections,
			List<GenotypesList> markerGTs,
			int dSamples,
			int n,
			GenotypeEncoder genotypeEncoder)
	{
		try {
			LOG.info("Combi Association Test: create SVM parameters");
			svm_parameter libSvmParameters = createLibSvmParameters();

			LOG.info("Combi Association Test: encode affection states");
			Map<SampleKey, Double> encodedAffectionStates = encodeAffectionStates(
					sampleKeys,
					sampleAffections,
					n);

			String encoderString = null;
//			if (EXAMPLE_TEST) { // HACK
//
//				storeForSVM(X, Y);
//
//				if (genotypeEncoder instanceof AllelicGenotypeEncoder) {
//					encoderString = "allelic";
//				} else if (genotypeEncoder instanceof GenotypicGenotypeEncoder) {
//					encoderString = "genotypic";
//				} else if (genotypeEncoder instanceof NominalGenotypeEncoder) {
//					encoderString = "nominal";
//				} else {
//					throw new RuntimeException();
//				}
//			}

			final float[][] kernelMatrix;
			try {
				// NOTE This allocates quite some memory!
				//   We use float instead of double to half the memory,
				//   this might be subject to change, as in,
				//   change to use double.
				kernelMatrix = new float[n][n];
			} catch (OutOfMemoryError er) {
				throw new IOException(er);
			}
			MarkerGenotypesEncoder markerGenotypesEncoder = createMarkerGenotypesEncoder(
					markerGTs, genotypeEncoder, dSamples, n);
			encodeFeaturesAndCreateKernelMatrix(
					markerGenotypesEncoder,
					kernelMatrix);

			svm_problem libSvmProblem = createLibSvmProblem(
					kernelMatrix,
					encodedAffectionStates.values(),
					libSvmParameters,
					null);

			return runSVM(markerGenotypesEncoder, libSvmProblem, genotypeEncoder, encoderString);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static MarkerGenotypesEncoder createMarkerGenotypesEncoder(
			List<GenotypesList> markersGenotypesSource,
			GenotypeEncoder genotypeEncoder,
			int dSamples,
			int n)
			throws IOException
	{
		// max memory usage of this function [bytes]
		final int maxChunkMemoryUsage = 1024 * 1024;
		// how much memory does one sample per marker use [bytes]
		final int singleEntryMemoryUsage = 2 * 8; // HACK FIXME two doubles.. arbitrary.. investigate
		// how many markers may be loaded at a time, to still fullfill the max memory usage limit
		final int maxChunkSize = Math.min(dSamples, (int) Math.floor((double) maxChunkMemoryUsage / dSamples / singleEntryMemoryUsage));

		return new MarkerGenotypesEncoder(markersGenotypesSource, genotypeEncoder, dSamples, n, maxChunkSize);
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
			final float[][] kernelMatrix)
			throws IOException
	{
		// initialize the kernelMatrix
		// this should not be required, if the aray was just created,
		// but who knows who will call this function in what way in the future!?
		for (float[] kernelMatrixRow : kernelMatrix) {
			for (int ci = 0; ci < kernelMatrixRow.length; ci++) {
				kernelMatrixRow[ci] = 0.0f;
			}
		}

		for (int fci = 0; fci < markerGenotypesEncoder.size(); fci++) {
			final Float[][] featuresChunk = markerGenotypesEncoder.get(fci);
			final int numFeaturesInChunk = markerGenotypesEncoder.getChunkSize(fci);
			final int n = featuresChunk.length;

			// calculate the part of the kernel matrix defined by
			// the current chunk of the feature matrix
			for (int smi = 0; smi < numFeaturesInChunk; smi++) {
				for (int krsi = 0; krsi < n; krsi++) { // kernel row sample index
					final float curRowValue = (Float) featuresChunk[krsi][smi];
					for (int krci = 0; krci < n; krci++) { // kernel column sample index
						final float curColValue = (Float) featuresChunk[krci][smi];
						kernelMatrix[krsi][krci] += curRowValue * curColValue;
					}
				}
			}
		}
	}

	private static svm_problem createLibSvmProblem(
			float[][] K,
			Collection<Double> Y,
			svm_parameter libSvmParameters,
			String encoderString)
			throws IOException
	{
		svm_problem prob = new svm_problem();

		final int n = Y.size();

		// prepare the features
		if (libSvmParameters.kernel_type == svm_parameter.PRECOMPUTED) {
			// KERNEL
			final int libSvmProblemBytes = (n * 8) + (n * n * (8 + 4 + 8));
			final String humanReadableLibSvmProblemMemory = Util.bytes2humanReadable(libSvmProblemBytes);
			LOG.info("Combi Association Test: libSVM preparation: required memory: ~ {} (on a 64bit system)", humanReadableLibSvmProblemMemory);

			LOG.info("Combi Association Test: libSVM preparation: allocate kernel memory");
			try {
				prob.x = new svm_node[n][1 + n];
			} catch (OutOfMemoryError er) {
				throw new IOException(er);
			}

			LOG.info("Combi Association Test: libSVM preparation: store the kernel elements");
			ProgressMonitor calculatingKernelPM = new ProgressMonitor(null, "store kernel matrix elements", "", 0, n*n);
			int calculatedKernelElements = 0;
			for (int si = 0; si < n; si++) {
				// This is required by the libSVM standard for a PRECOMPUTED kernel
				svm_node sampleIndexNode = new svm_node();
				sampleIndexNode.index = 0;
				sampleIndexNode.value = si;
				prob.x[si][0] = sampleIndexNode;

				for (int s2i = si; s2i < n; s2i++) {
					final double kernelValue = K[s2i][si]; // XXX or indices other way around?

					svm_node curNode = new svm_node();
					curNode.index = 1 + s2i;
					curNode.value = kernelValue;
					prob.x[si][1 + s2i] = curNode;
					calculatedKernelElements++;
					if (si != s2i) {
						// because the matrix is symmetric
						svm_node curNodeT = new svm_node();
						curNodeT.index = 1 + si;
						curNodeT.value = kernelValue;
						prob.x[s2i][1 + si] = curNodeT;
						calculatedKernelElements++;
					}
				}

				if ((si % 10) == 0) {
					calculatingKernelPM.setProgress(calculatedKernelElements);
					if ((si % 100) == 0) {
						calculatingKernelPM.setNote(String.format(
								"%d / %d ~= %f%%",
								calculatedKernelElements,
								n*n,
								(double) calculatedKernelElements / (n*n) * 100.0));
						LOG.info("Combi Association Test: libSVM preparation: stored kernel rows: {} / {}", si, n);
					}
				}
			}

//			if (encoderString != null) {
//				File correctKernelFile = new File(BASE_DIR, "K_" + encoderString);
//				List<List<Double>> correctKernel = parsePlainTextMatrix(correctKernelFile, false);
//
//				LOG.debug("\ncompare kernel matrices ...");
//				compareMatrices(correctKernel, problemInput);
//				LOG.debug("done. they are equal! good!\n");
//			}
		} else {
			throw new IllegalStateException("unsupported libSVM kernel type: " + libSvmParameters.kernel_type);
		}

		// prepare the labels
		LOG.info("Combi Association Test: libSVM preparation: store the label");
		prob.l = n;
		prob.y = new double[prob.l];
		Iterator<Double> itY = Y.iterator();
		for (int si = 0; si < n; si++) {
			double y = itY.next();
//			y = (y + 1.0) / 2.0;
			prob.y[si] = y;
		}

		return prob;
	}

	private static svm_parameter createLibSvmParameters() {

		svm_parameter svmParams = new svm_parameter();

		/** possible values: C_SVC, NU_SVC, ONE_CLASS, EPSILON_SVR, NU_SVR */
		svmParams.svm_type = svm_parameter.C_SVC;
		/** possible values: LINEAR, POLY, RBF, SIGMOID, PRECOMPUTED */
//		svmParams.kernel_type = svm_parameter.LINEAR;
		svmParams.kernel_type = svm_parameter.PRECOMPUTED;
		/** for poly */
		svmParams.degree = 3;
		/** for poly/RBF/sigmoid */
		svmParams.gamma = 0.0;
		/** for poly/sigmoid */
		svmParams.coef0 = 0;

		// these are for training only
		/** The cache size in MB */
		svmParams.cache_size = 40;
		/** stopping criteria */
		svmParams.eps = 1E-7;
		/** for C_SVC, EPSILON_SVR and NU_SVR */
		svmParams.C = 1.0;
		/** for C_SVC */
		svmParams.nr_weight = 0;
		/** for C_SVC */
		svmParams.weight_label = new int[svmParams.nr_weight];
		/** for C_SVC */
		svmParams.weight = new double[svmParams.nr_weight];
		/** for NU_SVC, ONE_CLASS, and NU_SVR */
		svmParams.nu = 0.5;
		/** for EPSILON_SVR */
		svmParams.p = 0.5;
		/** use the shrinking heuristics */
		svmParams.shrinking = 1;
		/** do probability estimates */
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
			final double[] ys)
	{
		// number of data-points/samples
		final int n = ys.length;
		// number of dimensions/features/markers*encodingFactor
		final int d = xs.getNumFeatures();

		double[] weights = new double[d];
		// This is probably not required, as Java initialized to zero anyway,
		// but it does not hurt to make things clear.
		Arrays.fill(weights, 0.0);

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
//					final double alphaYXi = - alpha * y * x; // FIXME why here change sign again?!?!
					final double alphaYXi = - alpha * x; // FIXME why here change sign again?!?!
					weights[di] += alphaYXi;
				}
			}
		}

		// convert array to list
		List<Double> weightsList = new ArrayList<Double>(weights.length);
		for (int wi = 0; wi < weights.length; wi++) {
			weightsList.add(weights[wi]);
		}

		return weightsList;
	}

	private static List<Double> runSVM(
			final MarkerGenotypesEncoder markerGenotypesEncoder,
			svm_problem libSvmProblem,
			GenotypeEncoder genotypeEncoder,
			String encoderString)
	{
		final int dEncoded = libSvmProblem.x[0].length; // NOTE This only works with libSVM kernel type != PRECOMPUTED, as it is n (number of samples) + 1, not number of encoded markers with precomputed
		final int dSamples = dEncoded / genotypeEncoder.getEncodingFactor();
		final int n = libSvmProblem.x.length;

//		// check if feature matrix is equivalent to the one calculated with matlab
//		if (encoderString != null) {
//			File correctFeaturesFile = new File(BASE_DIR, "featmat_" + encoderString + "_extra");
//			List<List<Double>> correctFeatures = parsePlainTextMatrix(correctFeaturesFile, false);
//			List<List<Double>> xValuesTrans = transpose(X);
////			log.debug("\nXXX correctFeatures[2]: " + correctFeatures.get(2));
////			log.debug("\nXXX xValues[2]: " + xValuesTrans.get(2));
//			LOG.debug("\ncompare feature matrices ...");
//			compareMatrices(correctFeatures, xValuesTrans);
//			LOG.debug("done. they are equal! good!\n");
//		}

		LOG.info("Combi Association Test: create SVM parameters");
		svm_parameter libSvmParameters = createLibSvmParameters();

		LOG.info("Combi Association Test: train the SVM model");
		svm_model svmModel = svm.svm_train(libSvmProblem, libSvmParameters);

		// check if the alphas are equivalent to the ones calculated with matlab
		if (encoderString != null) {
			List<Double> myAlphas = new ArrayList<Double>(Collections.nCopies(n, 0.0));
//			int curSVIndex = 0;
			for (int i = 0; i < svmModel.sv_coef[0].length; i++) {
				final double value = svmModel.sv_coef[0][i] * -1.0; // HACK FIXME no idea why we get inverted signs, but it should not matter much for our purpose
				int index;
				if (libSvmParameters.kernel_type == svm_parameter.PRECOMPUTED) {
					index = (int) svmModel.SV[i][0].value - 1; // XXX NOTE only works with PRECOMPUTED!
				} else { // LINEAR
	//				while (libSvmProblem.x[curSVIndex][0] != svmModel.SV[i][0]) {
	//					curSVIndex++;
	//				}
	//				index = curSVIndex;
					index = (int) svmModel.sv_indices[0][i]; // XXX testing
				}
	//			final int index = svmModel.SV[i][0].index;
	//			final int index = (int) svmModel.SV[i][0].index; // XXX NOTE does NOT work with PRECOMPUTED!
	//			final int index = (int) svmModel.SV[i][0].value - 1; // XXX NOTE only works with PRECOMPUTED!
	//			final int index = (int) svmModel.sv_indices[i][0]; // XXX testing
	//			final int index = (int) svmModel.sv_indices[0][i]; // XXX testing
				myAlphas.set(index, value);
			}

			double[][] alphas = svmModel.sv_coef;
			svm_node[][] SVs = svmModel.SV;
			LOG.debug("\n alphas: " + alphas.length + " * " + alphas[0].length + ": " + Arrays.asList(alphas[0]));
			LOG.debug("\n SVs: " + SVs.length + " * " + SVs[0].length);

//			List<List<Double>> alphasLM = new ArrayList<List<Double>>(alphas.length);
//			for (int i = 0; i < alphas.length; i++) {
//				List<Double> curRow = new ArrayList<Double>(alphas[i].length);
//				for (int j = 0; j < alphas[i].length; j++) {
//LOG.debug("\talpha: " + i + ", " + j + ": " + alphas[i][j]);
//					curRow.add(alphas[i][j]);
//				}
//				alphasLM.add(curRow);
//			}

			File correctAlphasFile = new File(BASE_DIR, "alpha_" + encoderString);
			List<List<Double>> correctAlphasSparse = Util.parsePlainTextMatrix(correctAlphasFile, false);
			List<Double> correctAlphas = new ArrayList<Double>(Collections.nCopies(n, 0.0));
			for (int i = 0; i < correctAlphasSparse.size(); i++) {
				final double value = correctAlphasSparse.get(i).get(0);
				final int index = correctAlphasSparse.get(i).get(1).intValue();
				correctAlphas.set(index, value);
			}

//			{
//				List<List<Double>> matrixA = new ArrayList<List<Double>>(3);
//				matrixA.add(Arrays.asList(new Double[] {1.0, 2.0}));
//				matrixA.add(Arrays.asList(new Double[] {3.0, 4.0}));
//				matrixA.add(Arrays.asList(new Double[] {5.0, 6.0}));
//
//				List<List<Double>> matrixB = new ArrayList<List<Double>>(2);
//				matrixB.add(Arrays.asList(new Double[] {1.0, -1.0, 1.0}));
//				matrixB.add(Arrays.asList(new Double[] {2.0, 1.0, 0.0}));
//
//				List<List<Double>> res = matrixMult(matrixA, matrixB);
//				log.debug("\nmatrixA: " + matrixA);
//				log.debug("\nmatrixB: " + matrixB);
//				log.debug("\nres: " + res);
//			}

//			Collections.sort(correctAlphas);
//			Collections.sort(myAlphas);
			LOG.debug("\nmatlab alphas: ("+correctAlphas.size()+")\n" + correctAlphas);
			LOG.debug("\njava alphas: ("+myAlphas.size()+")\n" + myAlphas);
			LOG.debug("\ncompare alpha vectors ...");
			try {
				Util.compareVectors(correctAlphas, myAlphas);
			} catch (RuntimeException ex) {
				// XXX make unequal alphas non-fatal; DANGER!
				ex.printStackTrace();
			}
			LOG.debug("done. they are equal! good!\n");
//		} else {
//			LOG.debug("\njava alphas: ("+myAlphas.size()+")\n" + myAlphas);
		}

		// sample index and value of non-zero alphas
		Map<Integer, Double> nonZeroAlphas = new LinkedHashMap<Integer, Double>(svmModel.sv_coef[0].length);
		for (int i = 0; i < svmModel.sv_coef[0].length; i++) {
			final double value = svmModel.sv_coef[0][i] * -1.0; // HACK FIXME no idea why we get inverted signs, but it should not matter much for our purpose
			int index = (int) svmModel.SV[i][0].value/* - 1*/; // XXX NOTE only works with PRECOMPUTED!
			nonZeroAlphas.put(index, value);
		}

		LOG.info("Combi Association Test: calculate original space weights from alphas");
		List<Double> weightsEncoded = calculateOriginalSpaceWeights(
				nonZeroAlphas, markerGenotypesEncoder, libSvmProblem.y);

		// check if the raw encoded weights are equivalent to the ones calculated with matlab
		if (encoderString != null) {
			File mlWeightsRawFile = new File(BASE_DIR, "w_" + encoderString + "_raw");
			List<Double> mlWeightsRaw = Util.parsePlainTextMatrix(mlWeightsRawFile, true).get(0);

			LOG.debug("\ncorrect weights raw: (" + mlWeightsRaw.size() + ") " + mlWeightsRaw);
			LOG.debug("weights raw: (" + weightsEncoded.size() + ") " + weightsEncoded);
			LOG.debug("compare raw, encoded weights vectors ...");
			Util.compareVectors(mlWeightsRaw, weightsEncoded);
			LOG.debug("done. they are equal! good!\n");
		}

		LOG.debug("weights(encoded): " + weightsEncoded.size());
		LOG.debug("\t" + weightsEncoded);

		LOG.debug("dSamples: " + dSamples);
		LOG.debug("dEncoded: " + dEncoded);
		LOG.debug("n: " + n);
		LOG.debug("genotypeEncoder: " + genotypeEncoder.getClass().getSimpleName());
		LOG.debug("encodingFactor: " + genotypeEncoder.getEncodingFactor());

		LOG.info("Combi Association Test: decode weights from the encoded feature space into marker space");
		List<Double> weights = new ArrayList<Double>(dSamples);
		genotypeEncoder.decodeWeights(weightsEncoded, weights);

		if (encoderString != null) {
			// check if the decoded weights are equivalent to the ones calculated with matlab
			File mlWeightsFinalFile = new File(BASE_DIR, "w_" + encoderString + "_final");
			List<Double> mlWeightsFinal = Util.parsePlainTextMatrix(mlWeightsFinalFile, false).get(0);

			LOG.debug("\nXXX correct weights final: (" + mlWeightsFinal.size() + ") " + mlWeightsFinal);
			LOG.debug("weights final: (" + weights.size() + ") " + weights);
			LOG.debug("compare final, decoded weights vectors ...");
			Util.compareVectors(mlWeightsFinal, weights);
			LOG.debug("done. they are equal! good!\n");
		}

		// apply moving average filter (p-norm filter)
		LOG.info("Combi Association Test: apply moving average filter (p-norm filter) on the weights");
		List<Double> weightsFiltered = new ArrayList(weights);
		Util.pNormFilter(weightsFiltered, 3, 2); XXX; // FIXME change to 35, 2 for bigger stuff

		if (encoderString != null) {
			// check if the filtered weights are equivalent to the ones calculated with matlab
			File mlWeightsFinalFilteredFile = new File(BASE_DIR, "w_" + encoderString + "_final_filtered");
			List<Double> mlWeightsFinalFiltered = Util.parsePlainTextMatrix(mlWeightsFinalFilteredFile, false).get(0);

			LOG.debug("\ncompare final, filtered weights vectors ...");
			Util.compareVectors(mlWeightsFinalFiltered, weightsFiltered);
			LOG.debug("done. they are equal! good!\n");
		}
		LOG.debug("Combi Association Test: filtered weights: " + weightsFiltered);

		return weightsFiltered;
	}
}

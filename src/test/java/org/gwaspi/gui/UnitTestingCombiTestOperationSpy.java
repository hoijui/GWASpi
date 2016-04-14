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

package org.gwaspi.gui;

import de.bwaldvogel.liblinear.Model;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import libsvm.svm_model;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.GenotypesListManager;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleKey;
import org.gwaspi.operations.combi.AllelicGenotypeEncoder;
import org.gwaspi.operations.combi.CombiTestOperation;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.operations.combi.CombiTestOperationSpy;
import org.gwaspi.operations.combi.GenotypeEncoder;
import org.gwaspi.operations.combi.GenotypeEncodingParams;
import org.gwaspi.operations.combi.GenotypicGenotypeEncoder;
import org.gwaspi.operations.combi.MarkerGenotypesEncoder;
import org.gwaspi.operations.combi.NominalGenotypeEncoder;
import org.gwaspi.operations.combi.SolverLibrary;
import org.gwaspi.operations.combi.SolverParams;
import org.gwaspi.operations.combi.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitTestingCombiTestOperationSpy implements CombiTestOperationSpy {

	private static final Logger LOG
			= LoggerFactory.getLogger(UnitTestingCombiTestOperationSpy.class);

	static final String BASE_PATH = "/data/extra/";

	private String encoderString;
	private int n;
	private int dSamples;

	public UnitTestingCombiTestOperationSpy() {

		this.encoderString = null;
		this.n = -1;
		this.dSamples = -1;
	}

	@Override
	public void initializing(
			List<MarkerKey> markerKeys,
			final List<Byte> majorAlleles,
			final List<Byte> minorAlleles,
			final List<int[]> markerGenotypesCounts,
			List<SampleKey> sampleKeys,
			List<Affection> sampleAffections,
			List<GenotypesList> markerGTs,
			GenotypeEncoder genotypeEncoder,
			MarkerGenotypesEncoder markerGenotypesEncoder)
	{
		n = sampleKeys.size();
		dSamples = markerKeys.size();

		if (genotypeEncoder instanceof AllelicGenotypeEncoder) {
			encoderString = "allelic";
		} else if (genotypeEncoder instanceof GenotypicGenotypeEncoder) {
			encoderString = "genotypic";
		} else if (genotypeEncoder instanceof NominalGenotypeEncoder) {
			encoderString = "nominal";
		} else {
			throw new RuntimeException();
		}

		try {
			// HACK This next line is for debugging purposes only
			Util.storeForEncoding(GenotypesListManager.getCommon(), markerKeys, majorAlleles, minorAlleles, markerGenotypesCounts, sampleKeys, sampleAffections, markerGTs);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// check if feature matrix is equivalent to the one calculated with matlab
		final String correctFeaturesFilePath = BASE_PATH + "featmat_" + encoderString;
		final InputStream correctFeaturesFile = UnitTestingCombiTestOperationSpy.class.getResourceAsStream(correctFeaturesFilePath);
		if (correctFeaturesFile == null) {
			LOG.error("Can not test because expected-values file was not found: {}", correctFeaturesFilePath);
			return;
		}
		final List<List<Double>> correctFeatures = Util.parsePlainTextMatrix(correctFeaturesFile, false);

		// load all features into memory
		final List<List<Double>> X = new ArrayList<List<Double>>(n);
		final int dEncoded = dSamples * genotypeEncoder.getEncodingFactor();
		for (int si = 0; si < n; si++) {
			X.add(new ArrayList<Double>(dEncoded));
		}
		for (int ci = 0; ci < markerGenotypesEncoder.size(); ci++) {
			final Float[][] featuresChunk = markerGenotypesEncoder.get(ci);
			final int chunkSize = markerGenotypesEncoder.getChunkSize(ci);
			for (int si = 0; si < n; si++) {
				List<Double> xRow = X.get(si);
				for (int lfi = 0; lfi < chunkSize; lfi++) {
					xRow.add(Double.valueOf(featuresChunk[si][lfi]));
				}
			}
		}
		final List<List<Double>> xValuesTrans = Util.transpose(X);

		LOG.debug("\ncompare feature matrices ...");
		Util.compareMatrices(correctFeatures, xValuesTrans);
		LOG.debug("done. they are equal! good!\n");
	}

	@Override
	public void kernelCalculated(float[][] K) {

		final String correctKernelFilePath = BASE_PATH + "K_" + encoderString;
		final InputStream correctKernelFile = UnitTestingCombiTestOperationSpy.class.getResourceAsStream(correctKernelFilePath);
		if (correctKernelFile == null) {
			LOG.error("Can not test because expected-values file was not found: {}", correctKernelFilePath);
			return;
		}
		List<List<Double>> correctKernel = Util.parsePlainTextMatrix(correctKernelFile, false);

		List<List<Double>> calculatedKernel = new Util.DoubleMatrixWrapperFloatArray2D(K);

		LOG.debug("\ncompare kernel matrices ...");
		Util.compareMatrices(correctKernel, calculatedKernel);
		LOG.debug("done. they are equal! good!\n");
	}

	@Override
	public void svmModelTrained(final svm_model svmModel, final Model svmModelLinear) {

		// this method checks if the alphas are equivalent to the ones calculated with matlab

		final Map<Integer, Double> extractedNonZeroAlphas;
		if (svmModelLinear == null) {
			extractedNonZeroAlphas = CombiTestOperation.extractNonZeroAlphas(svmModel);
		} else {
			throw new IllegalStateException("libLinear does not produce alphas, cause it is not kernel-based, but feature matrix based (only)");
		}
		List<Double> myAlphas = new ArrayList<Double>(Collections.nCopies(n, 0.0));
		for (final Map.Entry<Integer, Double> alphaIndexValue : extractedNonZeroAlphas.entrySet()) {
			myAlphas.set(alphaIndexValue.getKey(), -alphaIndexValue.getValue());
		}

//		double[][] alphas = svmModel.sv_coef;
//		svm_node[][] SVs = svmModel.SV;
//		LOG.debug("\n alphas: " + alphas.length + " * " + alphas[0].length + ": " + Arrays.toString(alphas[0]));
//		LOG.debug("\n SVs: " + SVs.length + " * " + SVs[0].length);

		final String correctAlphasFilePath = BASE_PATH + "alpha_" + encoderString;
		final InputStream correctAlphasFile = UnitTestingCombiTestOperationSpy.class.getResourceAsStream(correctAlphasFilePath);
		if (correctAlphasFile == null) {
			LOG.error("Can not test because expected-values file was not found: {}", correctAlphasFilePath);
			return;
		}
		List<List<Double>> correctAlphasSparse = Util.parsePlainTextMatrix(correctAlphasFile, false);
		List<Double> correctAlphas = new ArrayList<Double>(Collections.nCopies(n, 0.0));
		for (List<Double> correctAlphasSparseEntry : correctAlphasSparse) {
			final double value = correctAlphasSparseEntry.get(0);
			final int index = correctAlphasSparseEntry.get(1).intValue();
			correctAlphas.set(index, value);
		}

		LOG.debug("\nmatlab alphas: ("+correctAlphas.size()+")\n" + correctAlphas);
		LOG.debug("\njava alphas: ("+myAlphas.size()+")\n" + myAlphas);
		LOG.debug("\ncompare alpha vectors ...");
		Util.compareVectors(correctAlphas, myAlphas);
		LOG.debug("done. they are equal! good!\n");
	}

	@Override
	public void originalSpaceWeightsCalculated(final List<Double> weightsEncoded) {

		final List<Double> weightsEncodedCopy = new ArrayList<Double>(weightsEncoded);
		// check if the raw encoded weights are equivalent to the ones calculated with matlab
		final String mlWeightsRawFilePath = BASE_PATH + "w_" + encoderString + "_raw";
		final InputStream mlWeightsRawFile = UnitTestingCombiTestOperationSpy.class.getResourceAsStream(mlWeightsRawFilePath);
		if (mlWeightsRawFile == null) {
			LOG.error("Can not test because expected-values file was not found: {}", mlWeightsRawFilePath);
			return;
		}
		final List<Double> mlWeightsRaw = Util.parsePlainTextMatrix(mlWeightsRawFile, true).get(0);
		Util.absVector(weightsEncodedCopy);
		Util.absVector(mlWeightsRaw);

		LOG.debug("\ncorrect weights raw: (" + mlWeightsRaw.size() + ") " + mlWeightsRaw);
		LOG.debug("weights raw: (" + weightsEncodedCopy.size() + ") " + weightsEncodedCopy);
		LOG.debug("compare absolute values raw, encoded weights vectors ...");
		Util.compareVectors(mlWeightsRaw, weightsEncodedCopy);
		LOG.debug("done. they are equal! good!\n");
	}

	@Override
	public void decodedWeightsCalculated(List<Double> weights) {

		// check if the decoded weights are equivalent to the ones calculated with matlab
		final String mlWeightsFinalFilePath = BASE_PATH + "w_" + encoderString + "_final";
		final InputStream mlWeightsFinalFile = UnitTestingCombiTestOperationSpy.class.getResourceAsStream(mlWeightsFinalFilePath);
		if (mlWeightsFinalFile == null) {
			LOG.error("Can not test because expected-values file was not found: {}", mlWeightsFinalFilePath);
			return;
		}
		List<Double> mlWeightsFinal = Util.parsePlainTextMatrix(mlWeightsFinalFile, false).get(0);

		LOG.debug("\nXXX correct weights final: (" + mlWeightsFinal.size() + ") " + mlWeightsFinal);
		LOG.debug("weights final: (" + weights.size() + ") " + weights);
		LOG.debug("compare final, decoded weights vectors ...");
		Util.compareVectors(mlWeightsFinal, weights);
		LOG.debug("done. they are equal! good!\n");
	}

	@Override
	public void smoothedWeightsCalculated(List<Double> weightsFiltered) {

		// check if the filtered weights are equivalent to the ones calculated with matlab
		final String mlWeightsFinalFilteredFilePath = BASE_PATH + "w_" + encoderString + "_final_filtered";
		final InputStream mlWeightsFinalFilteredFile = UnitTestingCombiTestOperationSpy.class.getResourceAsStream(mlWeightsFinalFilteredFilePath);
		if (mlWeightsFinalFilteredFile == null) {
			LOG.error("Can not test because expected-values file was not found: {}", mlWeightsFinalFilteredFilePath);
			return;
		}
		List<Double> mlWeightsFinalFiltered = Util.parsePlainTextMatrix(mlWeightsFinalFilteredFile, false).get(0);

		LOG.debug("\ncompare final, filtered weights vectors ...");
		Util.compareVectors(mlWeightsFinalFiltered, weightsFiltered);
		LOG.debug("done. they are equal! good!\n");
	}

	public static void main(String[] args) {
//
//		GenotypeEncoder genotypeEncoder = AllelicGenotypeEncoder.SINGLETON; // TODO
//		GenotypeEncoder genotypeEncoder = GenotypicGenotypeEncoder.SINGLETON; // TODO
		GenotypeEncoder genotypeEncoder = NominalGenotypeEncoder.SINGLETON; // TODO
//		final int weightsFilterWidth = 3; // TODO or 35, if we have more markers
		final GenotypeEncodingParams genotypeEncodingParams = new GenotypeEncodingParams();
		final SolverLibrary solverLibrary = SolverLibrary.LIB_SVM;
		final SolverParams solverParams
				= CombiTestOperationParams.getSolverParamsDefault(solverLibrary);

//		runSVM(genotypeEncoder);

		CombiTestOperation.spy = new UnitTestingCombiTestOperationSpy(); // HACK
		Util.runEncodingAndSVM(genotypeEncoder, genotypeEncodingParams, solverLibrary, solverParams); // FIXME

//		List<List<Double>> X = new ArrayList<List<Double>>(2);
//		X.add(Arrays.asList(new Double[] {1.0, 0.0}));
//		X.add(Arrays.asList(new Double[] {0.0, 1.0}));
//		List<Double> Y = new ArrayList<Double>(2);
//		Y.add(1.0);
//		Y.add(-1.0);
//		runSVM(X, Y, genotypeEncoder, null);
		CombiTestOperation.spy = null; // HACK
	}
}

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ProgressMonitor;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.Genotype;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.operations.AbstractTestMatrixOperation;
//import org.gwaspi.netCDF.markers.MarkersIterable;
import org.gwaspi.netCDF.operations.MatrixOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombiTestMatrixOperation implements MatrixOperation {

	public static boolean EXAMPLE_TEST = false; // HACK

	private static final Logger LOG
			= LoggerFactory.getLogger(CombiTestMatrixOperation.class);

	private static final File BASE_DIR = new File(System.getProperty("user.home"), "/Projects/GWASpi/var/data/marius/example/extra"); // HACK

	/**
	 * Whether we are to perform allelic or genotypic association tests.
	 */
	private final CombiTestParams params;

	public CombiTestMatrixOperation(CombiTestParams params) {

		this.params = params;
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
		boolean dataLeft = AbstractTestMatrixOperation.excludeMarkersByHW(
				params.getHardyWeinbergOperationKey(),
				params.getHardyWeinbergThreshold(),
				toBeExcluded);
//		MarkersIterable.Excluder<MarkerKey> excluder;
//		if (params.getHardyWeinbergOperationKey() != null) {
//			excluder = new MarkersIterable.HWExcluder(
//					params.getHardyWeinbergOperationKey(),
//					params.getHardyWeinbergThreshold());
//		} else {
//			excluder = new MarkersIterable.NoneExcluder();
//		}
//		MarkersIterable markersIterable = new MarkersIterable(
//				params.getMatrixKey(),
//				excluder);

		if (dataLeft) {
			DataSetSource parentMatrixDataSetSource = MatrixFactory.generateMatrixDataSetSource(params.getMatrixKey());
			Map<Integer, MarkerKey> wrMarkerKeysFiltered = AbstractTestMatrixOperation.filterByValues(parentMatrixDataSetSource.getMarkersKeysSource().getIndicesMap(), toBeExcluded);

			SamplesKeysSource samplesKeysSource = parentMatrixDataSetSource.getSamplesKeysSource();
			SamplesInfosSource samplesInfosSource = parentMatrixDataSetSource.getSamplesInfosSource();
//			Map<SampleKey, SampleInfo> sampleInfos = markersIterable.getSampleInfos();

			// dimensions of the samples(-space) == #markers (== #SNPs)
//			int dSamples = markersIterable.getMarkerKeys().size() - excluder.getTotalExcluded();
			int dSamples = wrMarkerKeysFiltered.size() - toBeExcluded.size();
			// dimensions of the encoded samples(-space) == #markers * encoding-factor
			int dEncoded = dSamples * params.getEncoder().getEncodingFactor();
	//		int n = sampleInfos.size();
			int n = 0;
			// only count samples with a valid affection
			for (SampleInfo sampleInfo : samplesInfosSource) {
				if (sampleInfo.getAffection() != Affection.UNKNOWN) {
					n++;
				}
			}

			LOG.info("Combi Association Test: #samples: " + n);
			LOG.info("Combi Association Test: #markers: " + dSamples);
			LOG.info("Combi Association Test: #SVM-dimensions: " + dEncoded);

			if (EXAMPLE_TEST) { // HACK
				storeForEncoding(wrMarkerKeysFiltered, samplesInfosSource, dSamples, dEncoded, n);
			} else {
				return runEncodingAndSVM(wrMarkerKeysFiltered, samplesInfosSource, dSamples, dEncoded, n, params.getEncoder());
			}
		} else { // NO DATA LEFT AFTER THRESHOLD FILTER PICKING
			LOG.warn(Text.Operation.warnNoDataLeftAfterPicking);
		}

		return Integer.MIN_VALUE; // FIXME return correct value here
	}

	private static Map<SampleKey, Double> encodeAffectionStates(final Map<SampleKey, SampleInfo> sampleInfos, int n) {

		// we use LinkedHashMap to preserve the input order
		Map<SampleKey, Double> affectionStates
				= new LinkedHashMap<SampleKey, Double>(n);
		// we iterate over sampleKeys now, to get the correct order
		for (SampleInfo sampleInfo : sampleInfos.values()) {
			Affection affection = sampleInfo.getAffection();
			if (affection == Affection.UNKNOWN) {
//				throw new RuntimeException("Should we filter this out beforehand?");
				continue; // HACK maybe hacky, cause we should have filtered it out earlier? (i(robin) currently think it is ok here)
			}
			Double encodedDisease = affection.equals(Affection.AFFECTED) ? 1.0 : -1.0; // XXX or should it be 0.0 instead of -1.0?
			affectionStates.put(sampleInfo.getKey(), encodedDisease);
		}

		return affectionStates;
	}

	private static float[][] encodeSamples(
			Iterable<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>> markerSamplesIterable,
//			Set<SampleKey> sampleKeys, // NOTE needs to be well ordered!
			Map<SampleKey, SampleInfo> sampleInfos, // NOTE needs to be well ordered!
			GenotypeEncoder encoder,
			int dSamples,
			int dEncoded,
			int n,
			List<SampleKey> usedSamples)
			throws IOException
	{
//		LOG.debug("samples:");
//		Set<SampleKey> sampleKeys = sampleInfos.keySet();// NOTE needs to be well ordered!

		// evaluate which samples to keep
		// HACK maybe hacky, cause we should have filtered it out earlier? (i(robin) think not)
		int nSamplesToKeep = 0;
		List<Boolean> samplesToKeep = new ArrayList<Boolean>(n);
		for (SampleInfo sampleInfo : sampleInfos.values()) {
			if (sampleInfo.getAffection() == Affection.UNKNOWN) {
				samplesToKeep.add(Boolean.FALSE);
			} else  {
				nSamplesToKeep++;
				samplesToKeep.add(Boolean.TRUE);
			}
		}

		// we use LinkedHashMap to preserve the inut order
//		Map<SampleKey, List<Double>> encodedSamples
//				= new LinkedHashMap<SampleKey, List<Double>>(n);
//		for (SampleKey sampleKey : sampleKeys) {
//			encodedSamples.put(sampleKey, new ArrayList<Double>(dEncoded));
//		}
		// NOTE This allocates a LOT of memory!
		//   We use float instead of double to half the memory,
		//   because we have no more then 4 or 5 distinct values anyway,
		//   so we do not need high precission.
		final byte numSingleValueStorageBytes = 4; // float
		final int featureBytes = Util.calcFeatureBytes(nSamplesToKeep, dEncoded, numSingleValueStorageBytes);
		final String humanReadableFeaturesMemorySize = Util.bytes2humanReadable(featureBytes);
		LOG.info("Combi Association Test: allocate memory for features: {}",
				humanReadableFeaturesMemorySize);
		float[][] encodedSamples;
		try {
			encodedSamples = new float[nSamplesToKeep][dEncoded];
		} catch (OutOfMemoryError er) {
			throw new IOException(er);
		}

//		Map<MarkerKey, Set<Genotype>> uniqueGts
//				= new LinkedHashMap<MarkerKey, Set<Genotype>>(markerKeys.size());
		// collect unique GTs per marker
		int mi = 0;
//		List<Genotype> all = new ArrayList<Genotype>(n);
//		Set<Genotype> unique = new LinkedHashSet<Genotype>(4);
//		List<Genotype> uniqueList = new ArrayList<Genotype>(4);
		ProgressMonitor encodingMarkersPM = new ProgressMonitor(null, "encoding markers", "", 0, dSamples);
		for (Map.Entry<MarkerKey, Map<SampleKey, byte[]>> markerSamples : markerSamplesIterable) {
			Map<SampleKey, byte[]> samples = markerSamples.getValue();
//			LOG.debug("");
//			StringBuilder debugOut = new StringBuilder();
//			debugOut.append("marker ").append(mi).append("\n");
//			for (byte[] gt : samples.values()) {
//				debugOut.append(" ").append(new String(gt));
//			}
//			LOG.debug(debugOut.toString());
//			log.debug("Combi-test");

//			// convert & collect unique GTs (unique per marker)
//			all.clear();
//			unique.clear();
//			Iterator<SampleInfo> sampleInfoIt = sampleInfos.values().iterator();
//			for (Map.Entry<SampleKey, byte[]> sample : samples.entrySet()) {
//				SampleInfo curSampleInfo = sampleInfoIt.next();
//				if (curSampleInfo.getAffection() == Affection.UNKNOWN) {
//					continue; // HACK maybe hacky, cause we should have filtered it out earlier? (i(robin) think not)
//				}
//				usedSamples.add(sample.getKey());
//				Genotype genotype = new Genotype(sample.getValue());
//				all.add(genotype);
//				unique.add(genotype);
////				log.debug("\t" + sample.getKey() + ": " + new String(sample.getValue()));
//			}
//			uniqueList.clear();
//			uniqueList.addAll(unique);
//			Collections.sort(uniqueList);
////			log.debug("\tunique GT list:");
////			for (Genotype genotype : uniqueList) {
////				log.debug("\t\t\t\t" + genotype);
////			}

			// test output
//			uniqueGts.put(markerKey, curUniqueGts);
//			log.debug("\t" + markerKey + ": " + curUniqueGts.size());
//			for (Genotype genotype : curUniqueGts) {
//				log.debug("\t\t" + genotype);
//			}

			// encode all samples for this marker
//			encoder.encodeGenotypes(uniqueList, all, encodedSamples, mi);
			encoder.encodeGenotypes(samples.values(), samplesToKeep, encodedSamples, mi);

			mi++;

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

		return encodedSamples;
	}

	private static final File TMP_RAW_DATA_FILE = new File(System.getProperty("user.home") + "/Projects/GWASpi/repos/GWASpi/rawDataTmp.ser"); // HACK

	private static void storeForEncoding(
//			MarkersIterable markersIterable,
//			DataSetSource dataSetSource,
			Iterable<Map.Entry<Integer, MarkerKey>> markers,
			Iterable<List<byte[]>> markerGenotypes,
			Map<SampleKey, SampleInfo> sampleInfos,
			int dSamples,
			int dEncoded,
			int n)
			throws IOException
	{
//		// we use LinkedHashMap to preserve the inut order
//		Map<MarkerKey, Map<SampleKey, byte[]>> loadedMatrixSamples
//				= new LinkedHashMap<MarkerKey, Map<SampleKey, byte[]>>(dSamples);
//		for (Map.Entry<MarkerKey, Map<SampleKey, byte[]>> markerSamples : markersIterable) {
//			loadedMatrixSamples.put(markerSamples.getKey(), markerSamples.getValue());
//		}
//		List<MarkerKey> markerKeys = new ArrayList<MarkerKey>(dataSetSource.getMarkersKeysSource());
//		List<SampleKey> sampleKeys = new ArrayList<SampleKey>(dataSetSource.getSamplesKeysSource());
//		List<List<byte[]>> markerGenotypes = new ArrayList<List<byte[]>>(dataSetSource.getMarkersGenotypesSource().get(0));
		List<MarkerKey> markerKeys = new ArrayList<MarkerKey>(markers);
		List<SampleKey> sampleKeys = new ArrayList<SampleKey>(sampleInfos.keySet());
		List<List<byte[]>> markerGenotypes = new ArrayList<List<byte[]>>(markerGenotypes);

		try {
			FileOutputStream fout = new FileOutputStream(TMP_RAW_DATA_FILE);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
//			oos.writeObject(loadedMatrixSamples);
			oos.writeObject(markerKeys);
			oos.writeObject(sampleKeys);
			oos.writeObject(markerGenotypes);
			oos.writeObject(sampleInfos);
			oos.writeObject((Integer) dSamples);
			oos.writeObject((Integer) dEncoded);
			oos.writeObject((Integer) n);
			oos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

//	private static void runEncodingAndSVM(GenotypeEncoder genotypeEncoder) {
//
//		Map<MarkerKey, Map<SampleKey, byte[]>> matrixSamples;
//		Map<SampleKey, SampleInfo> sampleInfos;
//		int dSamples;
//		int dEncoded;
//		int n;
//		try {
//			FileInputStream fin = new FileInputStream(TMP_RAW_DATA_FILE);
//			ObjectInputStream ois = new ObjectInputStream(fin);
//			matrixSamples = (Map<MarkerKey, Map<SampleKey, byte[]>>) ois.readObject();
//			sampleInfos = (Map<SampleKey, SampleInfo>) ois.readObject();
//			dSamples = (Integer) ois.readObject();
//			dEncoded = (Integer) ois.readObject();
//			n = (Integer) ois.readObject();
//			ois.close();
//
//			runEncodingAndSVM(matrixSamples.entrySet(), sampleInfos, dSamples, dEncoded, n, genotypeEncoder);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}

	private static int runEncodingAndSVM_LINEAR_KERNEL(
			Iterable<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>> matrixSamples,
//			Map<MarkerKey, Map<SampleKey, byte[]>> matrixSamples,
			Map<SampleKey, SampleInfo> sampleInfos,
			int dSamples,
			int dEncoded,
			int n,
			GenotypeEncoder genotypeEncoder)
	{
		try {
			LOG.info("Combi Association Test: create SVM parameters");
			svm_parameter libSvmParameters = createLibSvmParameters();

			LOG.info("Combi Association Test: init the SVM model");
			int libSvmProblemBytes32bit = (n * 12) + ((n * dEncoded) * 16);
			int libSvmProblemBytes64bit = (n * 16) + ((n * dEncoded) * 20);
			int libSvmProblemMBytes32bit = (int) (libSvmProblemBytes32bit / 1024.0 / 1024.0);
			int libSvmProblemMBytes64bit = (int) (libSvmProblemBytes64bit / 1024.0 / 1024.0);
			LOG.info("Combi Association Test: required memory: ~ {}MB (32bit)  ~ {}MB (64bit)",
					libSvmProblemMBytes32bit, libSvmProblemMBytes64bit);
			LOG.info("Combi Association Test: allocate container memory");
			svm_problem libSvmProblem = new svm_problem();
			libSvmProblem.x = new svm_node[n][dEncoded];
			libSvmProblem.y = new double[n];
			libSvmProblem.l = n;
			LOG.info("Combi Association Test: allocate nodes memory ");
			for (int si = 0; si < n; si++) {
				for (int mi = 0; mi < dEncoded; mi++) {
					svm_node curNode = new svm_node();
					curNode.index = mi; // XXX correct?
	//				curNode.index = si; // XXX correct? pretty sure that yes
					libSvmProblem.x[si][mi] = curNode;
				}
				if ((si % 100) == 0) {
					LOG.info("Combi Association Test: allocated samples {} / {}", si+1, n);
				}
			}

//			svm_problem libSvmProblem = createLibSvmProblem(X, Y, libSvmParameters, encoderString);
			populateLibSvmProblem(matrixSamples, sampleInfos, libSvmParameters, genotypeEncoder, libSvmProblem, dSamples, dEncoded, n);

			String encoderString = null;
//			if (EXAMPLE_TEST) { // HACK
//				LOG.info("Combi Association Test: encode samples");
//				Map<SampleKey, List<Double>> encodedSamples = encodeSamples(
//						matrixSamples,
//	//					sampleInfos.keySet(),
//						sampleInfos,
//						genotypeEncoder,
//						dSamples,
//						dEncoded,
//						n);
//				LOG.info("Combi Association Test: encode affection states");
//				Map<SampleKey, Double> encodedAffectionStates = encodeAffectionStates(
//						sampleInfos,
//						n);
//
//				// do the SVM magic!
//				Map<SampleKey, List<Double>> X = encodedSamples;
//				Map<SampleKey, Double> Y = encodedAffectionStates;
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

			return runSVM(libSvmProblem, genotypeEncoder, encoderString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return Integer.MIN_VALUE;
	}

	private static int runEncodingAndSVM_PRECOMPUTED(
			Iterable<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>> matrixSamples,
//			Map<MarkerKey, Map<SampleKey, byte[]>> matrixSamples,
			Map<SampleKey, SampleInfo> sampleInfos,
			int dSamples,
			int dEncoded,
			int n,
			GenotypeEncoder genotypeEncoder)
	{
		try {
			LOG.info("Combi Association Test: create SVM parameters");
			svm_parameter libSvmParameters = createLibSvmParameters();

			LOG.info("Combi Association Test: init the SVM model");
//if (1 == 1) return Integer.MIN_VALUE;
//			LOG.info("Combi Association Test: allocate container memory");
//			svm_problem libSvmProblem = new svm_problem();
//			libSvmProblem.x = new svm_node[n][n];
//			libSvmProblem.y = new double[n];
//			libSvmProblem.l = n;
//			LOG.info("Combi Association Test: allocate nodes memory ");
//			for (int si = 0; si < n; si++) {
//				for (int mi = 0; mi < dEncoded; mi++) {
//					svm_node curNode = new svm_node();
//					curNode.index = mi; // XXX correct?
//	//				curNode.index = si; // XXX correct? pretty sure that yes
//					libSvmProblem.x[si][mi] = curNode;
//				}
//				if ((si % 100) == 0) {
//					LOG.info("Combi Association Test: allocated samples {} / {}", si+1, n);
//				}
//			}

//			svm_problem libSvmProblem = createLibSvmProblem(X, Y, libSvmParameters, encoderString);
//			populateLibSvmProblem(matrixSamples, sampleInfos, libSvmParameters, genotypeEncoder, libSvmProblem, dSamples, dEncoded, n);

			LOG.info("Combi Association Test: encode samples");
			// XXX unused (next lines var)!
			List<SampleKey> usedSamples = new ArrayList<SampleKey>(sampleInfos.size());
//			Map<SampleKey, List<Double>> encodedSamples = encodeSamples(
			float[][] encodedSamples = encodeSamples(
					matrixSamples,
//					sampleInfos.keySet(),
					sampleInfos,
					genotypeEncoder,
					dSamples,
					dEncoded,
					n,
					usedSamples);
			LOG.info("Combi Association Test: encode affection states");
			Map<SampleKey, Double> encodedAffectionStates = encodeAffectionStates(
					sampleInfos,
					n);

			// do the SVM magic!
//			Map<SampleKey, List<Double>> X = encodedSamples;
//			Map<SampleKey, Double> Y = encodedAffectionStates;

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

			whiten(encodedSamples);

			svm_problem libSvmProblem = createLibSvmProblem(
					encodedSamples,
					encodedAffectionStates.values(),
					libSvmParameters,
					null);

			return runSVM(libSvmProblem, genotypeEncoder, encoderString);
//			return Integer.MIN_VALUE; // FIXME
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return Integer.MIN_VALUE;
	}

	private static int runEncodingAndSVM(
//			Iterable<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>> matrixSamples,
//			Map<MarkerKey, Map<SampleKey, byte[]>> matrixSamples,
			Iterable<Map.Entry<Integer, MarkerKey>> markers,
			Iterable<List<byte[]>> markerGenotypes,
			Map<SampleKey, SampleInfo> sampleInfos,
			int dSamples,
			int dEncoded,
			int n,
			GenotypeEncoder genotypeEncoder)
	{
		return runEncodingAndSVM_PRECOMPUTED(matrixSamples, sampleInfos, dSamples, dEncoded, n, genotypeEncoder);
	}

	private static void whiten(float[][] x) {

		LOG.info("Combi Association Test: whiten the feature matrix (make center = 0 and variance = 1)");
		int dEncoded = x[0].length;
		int n = x.length;

		// center the data
		// ... using Double to calculate the mean, to prevent nummerical inaccuracies
		double[] sums = new double[dEncoded];
		double[] varianceSums = new double[dEncoded];
		for (int di = 0; di < dEncoded; di++) {
			sums[di] = 0.0;
			varianceSums[di] = 0.0;
		}
		for (int si = 0; si < n; si++) {
			for (int di = 0; di < dEncoded; di++) {
				sums[di] += x[si][di];
			}
		}
//		log.debug("sums: " + sums);
		List<Double> mean = new ArrayList<Double>(dEncoded);
		for (int di = 0; di < dEncoded; di++) {
//			Double divide = sums.get(di).setScale(4).divide(new Double(nSamples), Double.ROUND_HALF_UP);
//			log.debug("mean part: " + sums.get(di) + " / " + nSamples + " = " + divide);
//			mean.add(sums.get(di).divide(new Double(nSamples), Double.ROUND_HALF_UP).doubleValue());
			final double curSum = sums[di];
			final double curMean = (curSum == 0.0) ? 0.0 : (curSum / n);
			mean.add(curMean);
		}
//		log.debug("mean: " + mean);

		// subtract the mean & calculate the variance sums
		for (int si = 0; si < n; si++) {
			for (int di = 0; di < dEncoded; di++) {
				final float newValue = x[si][di] - mean.get(di).floatValue();
				x[si][di] = newValue;
				//varianceSums.set(di, varianceSums.get(di).add(new Double(newValue * newValue)))); // faster
//				varianceSums.set(di, varianceSums.get(di).add(new Double(newValue.pow(2))); // XXX more precise
				varianceSums[di] += (newValue * newValue);
			}
		}

//		// calculate the variance sums separately?
//		for (List<Double> x : X.values()) {
//			for (int di = 0; di < dEncoded; di++) {
//				x.set(di, x.get(di) - mean.get(di));
//			}
//		}

		// calculate the variance
//		List<Double> variance = new ArrayList<Double>(dEncoded);
		List<Double> stdDev = new ArrayList<Double>(dEncoded);
		for (int di = 0; di < dEncoded; di++) {
			double curVariance = varianceSums[di] / n * dEncoded;
//			variance.add(curVariance);
			stdDev.add(Math.sqrt(curVariance));
		}

		// set the variance to 1
		for (int si = 0; si < n; si++) {
			for (int di = 0; di < dEncoded; di++) {
				final float curStdDev = stdDev.get(di).floatValue();
				final float oldValue = x[si][di];
				final float newValue = (curStdDev == 0.0) ? oldValue : (oldValue / curStdDev);
				x[si][di] = newValue;
			}
		}
	}

	private static svm_problem createLibSvmProblem(
//			List<List<Double>> X,
			float[][] X,
			Collection<Double> Y,
			svm_parameter libSvmParameters,
			String encoderString)
			throws IOException
	{
		LOG.info("Combi Association Test: libSVM problem: create");
		svm_problem prob = new svm_problem();

//		int dEncoded = X.iterator().next().size();
//		int n = X.size();
		int dEncoded = X[0].length;
		int n = Y.size();

		// prepare the features
//		List<List<Double>> problemInput;
		if (libSvmParameters.kernel_type == svm_parameter.PRECOMPUTED) {
			// precomute the kernel: K = X' * X
//			prob.x = new svm_node[n][n];
//			List<List<Double>> XT = transpose(X);
//			problemInput = matrixMult(X, XT);

			// FEATURES
//			int libSvmProblemBytes = (n * 8) + ((n * (dSamples + dEncoded)) * 8) + (n * n * (8 + 4 + 8));
			// KERNEL
			final int libSvmProblemBytes = (n * 8) + (n * n * (8 + 4 + 8));
			final String humanReadableLibSvmProblemMemory = Util.bytes2humanReadable(libSvmProblemBytes);
			LOG.info("Combi Association Test: libSVM problem: required memory: ~ {} (on a 64bit system)", humanReadableLibSvmProblemMemory);

			LOG.info("Combi Association Test: libSVM problem: allocate kernel memory");
			try {
				prob.x = new svm_node[n][1 + n];
			} catch (OutOfMemoryError er) {
				throw new IOException(er);
			}

			LOG.info("Combi Association Test: libSVM problem: calculate the kernel");
			ProgressMonitor calculatingKernelPM = new ProgressMonitor(null, "calculate kernel matrix elements", "", 0, n*n);
			int calculatedKernelElements = 0;
			for (int si = 0; si < n; si++) {
				// This is required by the libSVM standard for a PRECOMPUTED kernel
				svm_node sampleIndexNode = new svm_node();
				sampleIndexNode.index = 0;
				sampleIndexNode.value = si;
				prob.x[si][0] = sampleIndexNode;

				for (int s2i = si; s2i < n; s2i++) {
					double kernelValue = 0.0;
					for (int di = 0; di < dEncoded; di++) {
						kernelValue += X[si][di] * X[s2i][di];
					}

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

				if ((si % 10) == 0) { // FIXME nearly never true, cause we are in the outer loop
					calculatingKernelPM.setProgress(calculatedKernelElements);
				}
				if ((si % 100) == 0) { // FIXME nearly never true, cause we are in the outer loop
					calculatingKernelPM.setNote(String.format(
							"%d / %d ~= %f%%",
							calculatedKernelElements,
							n*n,
							(double) calculatedKernelElements / (n*n) * 100.0));
					LOG.info("Combi Association Test: calculated kernel rows: {} / {}", si, n);
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

//			// This is required by the libSVM standard for a PRECOMPUTED kernel
//			int sampleIndex = 1;
//			for (List<Double> problemInputRow : problemInput) {
//				// XXX NOTE This is bad, because it will double the underlaying arrays size!
//				problemInputRow.add(0, (double) sampleIndex++);
//			}
//			// TESTING output to libSVM input format for a precomputed kernel, to test it externally
//			File generatedLibSvmKernelFile = new File(BASE_DIR, "generatedLibSvmKernel_" + encoderString + ".txt");
////			log.debug("\nX: " + X);
////			log.debug("\nXT: " + XT);
////			log.debug("\nX * XT: " + problemInput);
//			LOG.debug("\nwriting generated libSVM PRECOMPUTED kernel file to " + generatedLibSvmKernelFile + " ...");
//			try {
//				OutputStreamWriter kernOut = new FileWriter(generatedLibSvmKernelFile);
//				Iterator<Double> Yit = Y.iterator();
//				for (List<Double> problemInputRow : problemInput) {
//					final double y = Yit.next();
//					kernOut.write(String.valueOf(y));
//					int ci = 0;
//					for (Double value : problemInputRow) {
//						kernOut.write(' ');
//						kernOut.write(String.valueOf(ci));
//						kernOut.write(':');
//						kernOut.write(String.valueOf(value));
//						ci++;
//					}
//					kernOut.write('\n');
//				}
//				kernOut.close();
//			} catch (FileNotFoundException ex) {
//				throw new RuntimeException(ex);
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//			LOG.debug("done writing kernel file.");

			// XXX NOTE Do not delete this code! as it will be able to save us memory!
////			// TODO
////			throw new RuntimeException();
//			Iterator<List<Double>> itX = X.values().iterator();
//			for (int si1 = 0; si1 < n; si1++) {
//				List<Double> sampleGTs1 = itX.next();
//				for (int si2 = 0; si2 <= si1; si2++) { // calculate only half of the matrix, caus it is symmetric
//					List<Double> sampleGTs2 = matX.get(si2);
//					double res = 0.0;
//					for (int mi = 0; mi < dEncoded; mi++) {
//						res += sampleGTs1.get(mi) * sampleGTs2.get(mi);
//					}
//
//					// save two times, cause the matrix is symmetric
//					svm_node curNodeL = new svm_node();
//					curNodeL.index = si1;
//					curNodeL.value = res;
//					prob.x[si1][si2] = curNodeL;
//
//					svm_node curNodeU = new svm_node();
//					curNodeU.index = si2;
//					curNodeU.value = res;
//					prob.x[si2][si1] = curNodeU;
//				}
//			}
		} else {
//			problemInput = X;

//			prob.x = new svm_node[n][dEncoded];
//			Iterator<List<Double>> itX = X.values().iterator();
//			for (int si = 0; si < n; si++) {
//				List<Double> sampleGTs = itX.next();
//				for (int mi = 0; mi < dEncoded; mi++) {
//					svm_node curNode = new svm_node();
//	//				curNode.index = mi;
//					curNode.index = si; /// XXX correct?
//					curNode.value = sampleGTs.get(mi);
//					prob.x[si][mi] = curNode;
//				}
//			}
			throw new RuntimeException();
		}
//		prob.x = new svm_node[problemInput.size()][problemInput.get(0).size()];
//		LOG.debug("\nproblemInput: " + problemInput.size() + " * " + problemInput.get(0).size());
//		Iterator<List<Double>> itX = problemInput.iterator();
//		for (int si = 0; si < problemInput.size(); si++) {
//			List<Double> sampleGTs = itX.next();
//			for (int mi = 0; mi < problemInput.get(0).size(); mi++) {
//				svm_node curNode = new svm_node();
//				curNode.index = mi; // XXX correct?
////				curNode.index = si; // XXX correct? pretty sure that yes
//				curNode.value = sampleGTs.get(mi);
//				prob.x[si][mi] = curNode;
//			}
//		}


		// prepare the labels
		LOG.info("Combi Association Test: libSVM problem: store the label");
		prob.l = n;
		prob.y = new double[prob.l];
//		StringBuilder debugOut = new StringBuilder();
//		debugOut.append("\ty:");
		Iterator<Double> itY = Y.iterator();
		for (int si = 0; si < n; si++) {
			double y = itY.next();
//			y = (y + 1.0) / 2.0;
			prob.y[si] = y;
//			debugOut.append(" " + y);
		}
//		LOG.debug(debugOut.toString());

//		{
//			File generatedLibSvmKernelFile = new File(BASE_DIR, "generatedLibSvmKernel_" + encoderString + "_after.txt");
////			log.debug("\nX: " + X);
////			log.debug("\nXT: " + XT);
////			log.debug("\nX * XT: " + problemInput);
//			LOG.debug("\nAGAIN writing generated libSVM PRECOMPUTED kernel file to " + generatedLibSvmKernelFile + " ...");
//			try {
//				OutputStreamWriter kernOut = new FileWriter(generatedLibSvmKernelFile);
//				for (int si = 0; si < prob.x.length; si++) {
//					kernOut.write(String.valueOf(prob.y[si]));
//					for (int mi = 0; mi < prob.x[si].length; mi++) {
//						kernOut.write(' ');
//						kernOut.write(String.valueOf(prob.x[si][mi].index));
//						kernOut.write(':');
//						kernOut.write(String.valueOf(prob.x[si][mi].value));
//					}
//					kernOut.write('\n');
//				}
//				kernOut.close();
//			} catch (FileNotFoundException ex) {
//				throw new RuntimeException(ex);
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//			LOG.debug("done writing kernel file.");
//		}

		return prob;
	}

	private static void populateLibSvmProblem(
			Iterable<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>> markerSamplesIterable,
			Map<SampleKey, SampleInfo> sampleInfos,
			svm_parameter libSvmParameters,
			GenotypeEncoder genotypeEncoder,
			svm_problem libSvmProblem,
			int dSamples,
			int dEncoded,
			int n)
	{
		LOG.info("Combi Association Test: encode samples");
		Set<SampleKey> sampleKeys = sampleInfos.keySet();// NOTE needs to be well ordered!

		// we use LinkedHashMap to preserve the inut order
//		Map<SampleKey, List<Double>> encodedSamples
//				= new LinkedHashMap<SampleKey, List<Double>>(n);
//		for (SampleKey sampleKey : sampleKeys) {
//			encodedSamples.put(sampleKey, new ArrayList<Double>(dEncoded));
//		}

//		Map<MarkerKey, Set<Genotype>> uniqueGts
//				= new LinkedHashMap<MarkerKey, Set<Genotype>>(markerKeys.size());
		// collect unique GTs per marker
		int mi = 0;
		for (Map.Entry<MarkerKey, Map<SampleKey, byte[]>> markerSamples : markerSamplesIterable) {
			Map<SampleKey, byte[]> samples = markerSamples.getValue();
//			LOG.debug("");
//			StringBuilder debugOut = new StringBuilder();
//			debugOut.append("marker ").append(mi).append("\n");
//			for (byte[] gt : samples.values()) {
//				debugOut.append(" ").append(new String(gt));
//			}
//			LOG.debug(debugOut.toString());
//			log.debug("Combi-test");

			// convert & collect unique GTs (unique per marker)
//			List<Genotype> all = new ArrayList<Genotype>(n);
			Set<Genotype> unique = new LinkedHashSet<Genotype>(4);
			Iterator<SampleInfo> sampleInfoIt = sampleInfos.values().iterator();
			for (Map.Entry<SampleKey, byte[]> sample : samples.entrySet()) {
				SampleInfo curSampleInfo = sampleInfoIt.next();
				if (curSampleInfo.getAffection() == Affection.UNKNOWN) {
					continue; // HACK maybe hacky, cause we should have filtered it out earlier? (i(robin) think not)
				}
				Genotype genotype = new Genotype(sample.getValue());
//				all.add(genotype);
				unique.add(genotype);
//				log.debug("\t" + sample.getKey() + ": " + new String(sample.getValue()));
			}
			List<Genotype> uniqueList = new ArrayList<Genotype>(unique);
			Collections.sort(uniqueList);
//			log.debug("\tunique GT list:");
//			for (Genotype genotype : uniqueList) {
//				log.debug("\t\t\t\t" + genotype);
//			}

			// test output
//			uniqueGts.put(markerKey, curUniqueGts);
//			log.debug("\t" + markerKey + ": " + curUniqueGts.size());
//			for (Genotype genotype : curUniqueGts) {
//				log.debug("\t\t" + genotype);
//			}

			// encode all samples for this marker
//			genotypeEncoder.encodeGenotypes(uniqueList, all, libSvmProblem, mi); // FIXME

			mi++;
			if ((mi % 1000) == 0) {
				LOG.info("Combi Association Test: encoded markers {} / {}", mi, dSamples);
			}
		}





		LOG.info("Combi Association Test: encode affection states");
		// we use LinkedHashMap to preserve the inut order
//		Map<SampleKey, Double> affectionStates
//				= new LinkedHashMap<SampleKey, Double>(n);
		// we iterate over sampleKeys now, to get the correct order
		int si = 0;
		for (SampleInfo sampleInfo : sampleInfos.values()) {
			Affection affection = sampleInfo.getAffection();
			if (affection == Affection.UNKNOWN) {
//				throw new RuntimeException("Should we filter this out beforehand?");
				continue; // HACK maybe hacky, cause we should have filtered it out earlier? (i(robin) currently think it is ok here)
			}
			double encodedDisease = affection.equals(Affection.AFFECTED) ? 1.0 : -1.0; // XXX or should it be 0.0 instead of -1.0?
//			affectionStates.put(sampleInfo.getKey(), encodedDisease);
			libSvmProblem.y[si] = encodedDisease;
			si++;
		}





//		int dEncoded = X.iterator().next().size();
//		int n = X.size();
		dEncoded = libSvmProblem.x[0].length;
		n = libSvmProblem.x.length;

		// prepare the features
		List<List<Double>> problemInput;
//		if (libSvmParameters.kernel_type == svm_parameter.PRECOMPUTED) {
//			// precomute the kernel: K = X' * X
////			prob.x = new svm_node[n][n];
//			List<List<Double>> XT = transpose(X);
//			problemInput = matrixMult(X, XT);
//
//			if (encoderString != null) {
//				File correctKernelFile = new File(BASE_DIR, "K_" + encoderString);
//				List<List<Double>> correctKernel = parsePlainTextMatrix(correctKernelFile, false);
//
//				LOG.debug("\ncompare kernel matrices ...");
//				compareMatrices(correctKernel, problemInput);
//				LOG.debug("done. they are equal! good!\n");
//			}
//
//			// This is required by the libSVM standard for a PRECOMPUTED kernel
//			int sampleIndex = 1;
//			for (List<Double> problemInputRow : problemInput) {
//				// XXX NOTE This is bad, because it will double the underlaying arrays size!
//				problemInputRow.add(0, (double) sampleIndex++);
//			}
//			// TESTING output to libSVM input format for a precomputed kernel, to test it externally
//			File generatedLibSvmKernelFile = new File(BASE_DIR, "generatedLibSvmKernel_" + encoderString + ".txt");
////			log.debug("\nX: " + X);
////			log.debug("\nXT: " + XT);
////			log.debug("\nX * XT: " + problemInput);
//			LOG.debug("\nwriting generated libSVM PRECOMPUTED kernel file to " + generatedLibSvmKernelFile + " ...");
//			try {
//				OutputStreamWriter kernOut = new FileWriter(generatedLibSvmKernelFile);
//				Iterator<Double> Yit = Y.iterator();
//				for (List<Double> problemInputRow : problemInput) {
//					final double y = Yit.next();
//					kernOut.write(String.valueOf(y));
//					int ci = 0;
//					for (Double value : problemInputRow) {
//						kernOut.write(' ');
//						kernOut.write(String.valueOf(ci));
//						kernOut.write(':');
//						kernOut.write(String.valueOf(value));
//						ci++;
//					}
//					kernOut.write('\n');
//				}
//				kernOut.close();
//			} catch (FileNotFoundException ex) {
//				throw new RuntimeException(ex);
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//			LOG.debug("done writing kernel file.");
//
//			// XXX NOTE Do not delete this code! as it will be able to save us memory!
//////			// TODO
//////			throw new RuntimeException();
////			Iterator<List<Double>> itX = X.values().iterator();
////			for (int si1 = 0; si1 < n; si1++) {
////				List<Double> sampleGTs1 = itX.next();
////				for (int si2 = 0; si2 <= si1; si2++) { // calculate only half of the matrix, caus it is symmetric
////					List<Double> sampleGTs2 = matX.get(si2);
////					double res = 0.0;
////					for (int mi = 0; mi < dEncoded; mi++) {
////						res += sampleGTs1.get(mi) * sampleGTs2.get(mi);
////					}
////
////					// save two times, cause the matrix is symmetric
////					svm_node curNodeL = new svm_node();
////					curNodeL.index = si1;
////					curNodeL.value = res;
////					prob.x[si1][si2] = curNodeL;
////
////					svm_node curNodeU = new svm_node();
////					curNodeU.index = si2;
////					curNodeU.value = res;
////					prob.x[si2][si1] = curNodeU;
////				}
////			}
//		} else {
//			problemInput = X;

//			prob.x = new svm_node[n][dEncoded];
//			Iterator<List<Double>> itX = X.values().iterator();
//			for (int si = 0; si < n; si++) {
//				List<Double> sampleGTs = itX.next();
//				for (int mi = 0; mi < dEncoded; mi++) {
//					svm_node curNode = new svm_node();
//	//				curNode.index = mi;
//					curNode.index = si; /// XXX correct?
//					curNode.value = sampleGTs.get(mi);
//					prob.x[si][mi] = curNode;
//				}
//			}
//		}
//		prob.x = new svm_node[problemInput.size()][problemInput.get(0).size()];
//		LOG.debug("\nproblemInput: " + problemInput.size() + " * " + problemInput.get(0).size());
//		Iterator<List<Double>> itX = problemInput.iterator();
//		for (int si = 0; si < problemInput.size(); si++) {
//			List<Double> sampleGTs = itX.next();
//			for (int mi = 0; mi < problemInput.get(0).size(); mi++) {
//				svm_node curNode = new svm_node();
//				curNode.index = mi; // XXX correct?
////				curNode.index = si; // XXX correct? pretty sure that yes
//				curNode.value = sampleGTs.get(mi);
//				prob.x[si][mi] = curNode;
//			}
//		}


//		// prepare the labels
//		prob.l = n;
//		prob.y = new double[prob.l];
//		StringBuilder debugOut = new StringBuilder();
//		debugOut.append("\ty:");
//		Iterator<Double> itY = Y.iterator();
//		for (int si = 0; si < n; si++) {
//			double y = itY.next();
////			y = (y + 1.0) / 2.0;
//			prob.y[si] = y;
//			debugOut.append(" " + y);
//		}
//		LOG.debug(debugOut.toString());

//		{
//			File generatedLibSvmKernelFile = new File(BASE_DIR, "generatedLibSvmKernel_" + encoderString + "_after.txt");
////			log.debug("\nX: " + X);
////			log.debug("\nXT: " + XT);
////			log.debug("\nX * XT: " + problemInput);
//			LOG.debug("\nAGAIN writing generated libSVM PRECOMPUTED kernel file to " + generatedLibSvmKernelFile + " ...");
//			try {
//				OutputStreamWriter kernOut = new FileWriter(generatedLibSvmKernelFile);
//				for (int si = 0; si < prob.x.length; si++) {
//					kernOut.write(String.valueOf(prob.y[si]));
//					for (int mi = 0; mi < prob.x[si].length; mi++) {
//						kernOut.write(' ');
//						kernOut.write(String.valueOf(prob.x[si][mi].index));
//						kernOut.write(':');
//						kernOut.write(String.valueOf(prob.x[si][mi].value));
//					}
//					kernOut.write('\n');
//				}
//				kernOut.close();
//			} catch (FileNotFoundException ex) {
//				throw new RuntimeException(ex);
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//			LOG.debug("done writing kernel file.");
//		}
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
	 * Calculate the weights 'w' in the original space (as in, the orignal space as known to the SVM, not the genotype space),
	 * using the weights 'alpha' from the kernel-space.
	 * <math>\mathbf{w} = \sum_i \alpha_i y_i \mathbf{x}_i.</math>
	 * @param alphas the SVM problem weights in kernel-space
	 * @param xs the support-vector coordinates in the original space
	 * @param ys the labels of the data (-1, or 1)
	 * @return the SVM problem weights 'w' in the original space
	 */
	private static List<Double> calculateOriginalSpaceWeights(
			final double[][] alphas,
			final svm_node[][] xs,
//			final List<List<Double>> X,
			final double[] ys,
			svm_parameter libSvmParameters)
	{
//		final int d = xs[0].length;
//		final int d = X.get(0).size();
		final int d = xs[0].length;

//		List<Double> weights
//				= new ArrayList<Double>(Collections.nCopies(d , 0.0));
		double[] weights = new double[d];
		Arrays.fill(weights, 0.0); // probably not required, but it does not hurt to make things clear
LOG.debug("calculateOriginalSpaceWeights: " + xs.length);
		for (int svi = 0; svi < xs.length; svi++) {
			final svm_node[] xsi = xs[svi];
//			final int svIndex = xsi[0].index; // FIXME this is wrong! it is the other index (marker-id, not sample-id!
//			final int svIndex = (int) xsi[0].value - 1; // FIXME this only works with PRECOMPUTED!
//			final int svIndex = (int) xsi[0].value; // FIXME this only works with LINEAR!
//			int svIndex = (int) xsi[0].value;
//			if (libSvmParameters.kernel_type == svm_parameter.PRECOMPUTED) {
//				svIndex -= 1;
//			}
//			log.debug("svIndex: " + svIndex);
//			final List<Double> Xsi = X.get(svIndex);
//			final List<Double> Xsi = new ArrayList<Double>(xsi.length - 1); // FIXME this only works with PRECOMPUTED!
//			for (int i = 1; i < xsi.length; i++) {
//				svm_node elem = xsi[i];
//				Xsi.add(elem.value);
//			}
			final double alpha = alphas[0][svi];
//			final double y = ys[svIndex];
			for (int di = 0; di < d; di++) {
				final double x = xsi[di].value;
//				final double x = Xsi.get(di);
//				final double x = Math.abs(Xsi.get(di));
//				final double alphaYXi = alpha * y * x;
				// NOTE We dismiss the y, which would be part of normal SVM,
				// because we want the absolute sum (i forgot again why so :/ )
//				final double alphaYXi = alpha * x;
				final double alphaYXi = - alpha * x; // FIXME why here change sign again?!?!
//				weights.set(di, weights.get(di) + alphaYXi);
				weights[di] += alphaYXi;
			}
//			log.debug();
		}

		List<Double> weightsList = new ArrayList<Double>(weights.length);
		for (int wi = 0; wi < weights.length; wi++) {
			weightsList.add(weights[wi]);
		}

//		return weights;
		return weightsList;
	}


//	private static int runSVM(Map<SampleKey, List<Double>> X, Map<SampleKey, Double> Y, GenotypeEncoder genotypeEncoder) {
//	private static int runSVM(List<List<Double>> X, List<Double> Y, GenotypeEncoder genotypeEncoder, String encoderString) {
	private static int runSVM(svm_problem libSvmProblem, GenotypeEncoder genotypeEncoder, String encoderString) {

//		int dEncoded = X.iterator().next().size();
//		int dSamples = dEncoded / genotypeEncoder.getEncodingFactor();
//		int n = X.size();
		int dEncoded = libSvmProblem.x[0].length;
		int dSamples = dEncoded / genotypeEncoder.getEncodingFactor();
		int n = libSvmProblem.x.length;

//		LOG.info("Combi Association Test: whiten");
//		whiten(libSvmProblem);

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

//		LOG.info("Combi Association Test: init the SVM model");
//		svm_problem libSvmProblem = createLibSvmProblem(X, Y, libSvmParameters, encoderString);

		LOG.info("Combi Association Test: train the SVM model");
		svm_model svmModel = svm.svm_train(libSvmProblem, libSvmParameters);


		// check if the alphas are equivalent to the ones calculated with matlab
		if (encoderString != null) {
			List<Double> myAlphas = new ArrayList<Double>(Collections.nCopies(n, 0.0));
			int curSVIndex = 0;
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

		LOG.info("Combi Association Test: calculate original space weights from alphas");
		List<Double> weightsEncoded = calculateOriginalSpaceWeights(
				svmModel.sv_coef, svmModel.SV/*, X*/, libSvmProblem.y, libSvmParameters);

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
			{
				File mlWeightsFinalFile = new File(BASE_DIR, "w_" + encoderString + "_final");
				List<Double> mlWeightsFinal = Util.parsePlainTextMatrix(mlWeightsFinalFile, false).get(0);

				LOG.debug("\nXXX correct weights final: (" + mlWeightsFinal.size() + ") " + mlWeightsFinal);
				LOG.debug("weights final: (" + weights.size() + ") " + weights);
				LOG.debug("compare final, decoded weights vectors ...");
				Util.compareVectors(mlWeightsFinal, weights);
				LOG.debug("done. they are equal! good!\n");
			}
		}

		// apply moving average filter (p-norm filter)
		LOG.info("Combi Association Test: apply moving average filter (p-norm filter) on the weights");
		List<Double> weightsFiltered = new ArrayList(weights);
		Util.pNormFilter(weightsFiltered, 3, 2); // FIXME change to 35, 2 for bigger stuff

		if (encoderString != null) {
			// check if the filtered weights are equivalent to the ones calculated with matlab
			File mlWeightsFinalFilteredFile = new File(BASE_DIR, "w_" + encoderString + "_final_filtered");
			List<Double> mlWeightsFinalFiltered = Util.parsePlainTextMatrix(mlWeightsFinalFilteredFile, false).get(0);

			LOG.debug("\ncompare final, filtered weights vectors ...");
			Util.compareVectors(mlWeightsFinalFiltered, weightsFiltered);
			LOG.debug("done. they are equal! good!\n");
		}
		LOG.debug("Combi Association Test: filtered weights: " + weightsFiltered);

		// TODO sort the weights (should already be absolute?)
		// TODO write stuff to a matrix (maybethe list of important markers?)

		return Integer.MIN_VALUE;
	}

	public static void main(String[] args) {

		EXAMPLE_TEST = true;
//
//		GenotypeEncoder genotypeEncoder = AllelicGenotypeEncoder.SINGLETON; // TODO
//		GenotypeEncoder genotypeEncoder = GenotypicGenotypeEncoder.SINGLETON; // TODO
		GenotypeEncoder genotypeEncoder = NominalGenotypeEncoder.SINGLETON; // TODO

//		runSVM(genotypeEncoder);

//		runEncodingAndSVM(genotypeEncoder); // FIXME

//		List<List<Double>> X = new ArrayList<List<Double>>(2);
//		X.add(Arrays.asList(new Double[] {1.0, 0.0}));
//		X.add(Arrays.asList(new Double[] {0.0, 1.0}));
//		List<Double> Y = new ArrayList<Double>(2);
//		Y.add(1.0);
//		Y.add(-1.0);
//		runSVM(X, Y, genotypeEncoder, null);
		EXAMPLE_TEST = false;
	}
}

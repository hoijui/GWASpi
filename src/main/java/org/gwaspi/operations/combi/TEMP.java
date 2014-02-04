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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.gwaspi.model.SampleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stuff from CombiTestMatrixOperation that is currently unused
 */
public class TEMP {

	private static final Logger LOG
			= LoggerFactory.getLogger(TEMP.class);

//	private static class EncodedSVMProblemData {
//
////		private final Map<SampleKey, List<Double>> samples;
////		private final Map<SampleKey, Double> affection;
//
//		EncodedSVMProblemData(int samples, int markers) {
//		}
//	}

	private static final File TMP_SVM_DATA_FILE = new File(System.getProperty("user.home") + "/Projects/GWASpi/repos/GWASpi/svmDataTmp.ser");

	private static void storeForSVM(Map<SampleKey, List<Double>> X, Map<SampleKey, Double> Y) {

		try {
			FileOutputStream fout = new FileOutputStream(TMP_SVM_DATA_FILE);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(X);
			oos.writeObject(Y);
			oos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Sets the means to 0, and the variances to 1.
	 * We need the complete set of samples for this to work,
	 * but it may be only a subset of the markers, and still work properly.
	 * @param x [samples][markers]; each marker is a data-point for the SVM
	 */
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

	private static void runSVM(GenotypeEncoder genotypeEncoder) {

		Map<SampleKey, List<Double>> X;
		Map<SampleKey, Double> Y;
		try {
			FileInputStream fin = new FileInputStream(TMP_SVM_DATA_FILE);
			ObjectInputStream ois = new ObjectInputStream(fin);
			X = (Map<SampleKey, List<Double>>) ois.readObject();
			Y = (Map<SampleKey, Double>) ois.readObject();
			ois.close();

			String encoderString;
			if (genotypeEncoder instanceof AllelicGenotypeEncoder) {
				encoderString = "allelic";
			} else if (genotypeEncoder instanceof GenotypicGenotypeEncoder) {
				encoderString = "genotypic";
			} else if (genotypeEncoder instanceof NominalGenotypeEncoder) {
				encoderString = "nominal";
			} else {
				throw new RuntimeException();
			}

//			runSVM(new ArrayList<List<Double>>(X.values()), new ArrayList<Double>(Y.values()), genotypeEncoder, encoderString); // FIXME
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void whiten(List<List<Double>> X) {

		LOG.info("Combi Association Test: whiten the feature matrix (make center = 0 and variance = 1)");
		int dEncoded = X.iterator().next().size();
		int n = X.size();

//		log.debug("X raw: " + X.size() + " * " + X.values().iterator().next().size());
//		for (List<Double> x : X.values()) {
//			log.debug("\tx: " + x);
//		}

		// center the data
		// ... using Double to calculate the mean, to prevent nummerical inaccuracies
		List<Double> sums = new ArrayList<Double>(dEncoded);
		List<Double> varianceSums = new ArrayList<Double>(dEncoded);
		for (int di = 0; di < dEncoded; di++) {
			sums.add(0.0);
			varianceSums.add(0.0);
		}
		for (List<Double> x : X) {
			for (int di = 0; di < dEncoded; di++) {
				sums.set(di, sums.get(di) + x.get(di));
			}
		}
//		log.debug("sums: " + sums);
		List<Double> mean = new ArrayList<Double>(dEncoded);
		for (int di = 0; di < dEncoded; di++) {
//			Double divide = sums.get(di).setScale(4).divide(new Double(nSamples), Double.ROUND_HALF_UP);
//			log.debug("mean part: " + sums.get(di) + " / " + nSamples + " = " + divide);
//			mean.add(sums.get(di).divide(new Double(nSamples), Double.ROUND_HALF_UP).doubleValue());
			final double curSum = sums.get(di);
			final double curMean = (curSum == 0.0) ? 0.0 : (curSum / n);
			mean.add(curMean);
		}
//		log.debug("mean: " + mean);
		// alternatively, using a moving average as described in the second formula here:
		// https://en.wikipedia.org/wiki/Moving_average#Cumulative_moving_average
		// this might be faster, might not.
		// TODO

		// subtract the mean & calculate the variance sums
		for (List<Double> x : X) {
			for (int di = 0; di < dEncoded; di++) {
				final double newValue = x.get(di) - mean.get(di);
				x.set(di, newValue);
				//varianceSums.set(di, varianceSums.get(di).add(new Double(newValue * newValue)))); // faster
//				varianceSums.set(di, varianceSums.get(di).add(new Double(newValue.pow(2))); // XXX more precise
				varianceSums.set(di, varianceSums.get(di) + (newValue * newValue));
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
			double curVariance = varianceSums.get(di) / n * dEncoded;
//			variance.add(curVariance);
			stdDev.add(Math.sqrt(curVariance));
		}

		// set the variance to 1
		for (List<Double> x : X) {
			for (int di = 0; di < dEncoded; di++) {
				final double curStdDev = stdDev.get(di);
				final double oldValue = x.get(di);
				final double newValue = (curStdDev == 0.0) ? oldValue : (oldValue / curStdDev);
				x.set(di, newValue);
			}
		}
	}

	private static void whiten(svm_problem libSvmProblem) {

		LOG.info("Combi Association Test: whiten the feature matrix (make center = 0 and variance = 1)");
		int dEncoded = libSvmProblem.x[0].length;
		int n = libSvmProblem.x.length;

//		log.debug("X raw: " + X.size() + " * " + X.values().iterator().next().size());
//		for (List<Double> x : X.values()) {
//			log.debug("\tx: " + x);
//		}

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
				sums[di] += libSvmProblem.x[si][di].value;
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
		// alternatively, using a moving average as described in the second formula here:
		// https://en.wikipedia.org/wiki/Moving_average#Cumulative_moving_average
		// this might be faster, might not.
		// TODO

		// subtract the mean & calculate the variance sums
		for (int si = 0; si < n; si++) {
			for (int di = 0; di < dEncoded; di++) {
				final double newValue = libSvmProblem.x[si][di].value - mean.get(di);
				libSvmProblem.x[si][di].value = newValue;
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
				final double curStdDev = stdDev.get(di);
				final double oldValue = libSvmProblem.x[si][di].value;
				final double newValue = (curStdDev == 0.0) ? oldValue : (oldValue / curStdDev);
				libSvmProblem.x[si][di].value = newValue;
			}
		}
	}

	private static void writeLibSvmTrainingFile(
			svm_problem libSvmProblem,
			String encoderString)
			throws IOException
	{
		final int n = libSvmProblem.y.length;

		// TESTING output to libSVM input format for a precomputed kernel, to test it externally
		File generatedLibSvmKernelFile = new File(CombiTestMatrixOperation.BASE_DIR, "generatedLibSvmKernel_" + encoderString + ".txt");

		LOG.info("\nwriting generated libSVM PRECOMPUTED kernel file to " + generatedLibSvmKernelFile + " ...");
		OutputStreamWriter kernOut = null;
		try {
			kernOut = new FileWriter(generatedLibSvmKernelFile);
			for (int ri = 0; ri < n; ri++) {
				final double y = libSvmProblem.y[ri];
				kernOut.write(String.valueOf(y));
				for (int ci = 0; ci <= n; ci++) {
					kernOut.write(' ');
					kernOut.write(String.valueOf(ci));
					kernOut.write(':');
					kernOut.write(String.valueOf(libSvmProblem.x[ri][ci].value));
					ci++;
				}
				kernOut.write('\n');
			}
		} finally {
			if (kernOut != null) {
				kernOut.close();
			}
		}
		LOG.debug("done writing kernel file.");
	}
}

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.model.CompactGenotypesList;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleKey;

public class Util {

	private Util() {}

	static int calcFeatureBytes(int samples, int dimensions, byte baseStorageTypeBytes) {

		int bytes = samples * dimensions * baseStorageTypeBytes;

		return bytes;
	}

	static int calcFeatureBytes(int samples, int markers, GenotypeEncoder encoder, byte baseStorageTypeBytes) {
		return calcFeatureBytes(samples, markers * encoder.getEncodingFactor(), baseStorageTypeBytes);
	}

	static int calcKernelBytes(int samples) {

		boolean precomputed = true;
		final int n = samples;

		int bytes;
		if (precomputed) {
			// kernel
			bytes = (n * 8) + (n * n * (8 + 4 + 8));
		} else {
			// features
//			bytes = (n * 8) + ((n * (dSamples + dEncoded)) * 8) + (n * n * (8 + 4 + 8));
			bytes = 0;
		}

		return bytes;
	}

	static double bytes2gigaBytes(int bytes) {
		return bytes / (1024.0 * 1024.0 * 1024.0);
	}

	static final String[] MEMORY_SIZE_PREFIXES = new String[] {
		"K", "M", "G", "T", "E", "P"
	};
	static String bytes2humanReadable(long bytes) {

		String humanReadable = "";
		long remaining = bytes;
		int prefixIndex = 0;
		long reminder;
		do {
			reminder = remaining % 1024;
			remaining = remaining / 1024;
			humanReadable = reminder + " " + MEMORY_SIZE_PREFIXES[prefixIndex++] + "B" + humanReadable;
		} while (remaining > 0);

		return humanReadable.toString();
	}

	private static List<List<Double>> matrixMult(
			List<List<Double>> matrixA,
			List<List<Double>> matrixB)
	{
		final int nA = matrixA.size();
		final int mA = matrixA.get(0).size();
		final int nB = matrixB.size();
		final int mB = matrixB.get(0).size();

		if (mA != nB) {
			throw new RuntimeException(String.format(
					"can not multiply matrizes with sizes (%d x %d) and (%d x %d)",
					nA, mA, nB, mB));
		}

		// init the resulting matrix wiht zeros
		List<List<Double>> res = new ArrayList<List<Double>>(matrixA.size());
		for (int ri = 0; ri < nA; ri++) {
			List<Double> row = new ArrayList<Double>(mB);
			for (int ci = 0; ci < mB; ci++) {
				row.add(0.0);
			}
			res.add(row);
		}

		for (int ri = 0; ri < nA; ri++) {
			List<Double> rowA = matrixA.get(ri);
			List<Double> rowRes = res.get(ri);
			for (int ci = 0; ci < mB; ci++) {
				for (int ii = 0; ii < mA; ii++) {
					rowRes.set(ci, rowRes.get(ci) + (rowA.get(ii) * matrixB.get(ii).get(ci)));
				}
			}
		}

		return res;
	}

	public static void compareMatrices(
			List<List<Double>> matrixA,
			List<List<Double>> matrixB)
	{
		final int rowsA = matrixA.size();
		final int rowsB = matrixB.size();
		final int colsA = matrixA.get(0).size();
		final int colsB = matrixB.get(0).size();

		if ((rowsA != rowsB) || (colsA != colsB)) {
			throw new RuntimeException(String.format(
					"matrix A dimension (%d, %d) differ from dimensions of matrix B (%d, %d)",
					rowsA, colsA, rowsB, colsB));
		}
		for (int y = 0; y < matrixA.size(); y++) {
			List<Double> rowA = matrixA.get(y);
			List<Double> rowB = matrixB.get(y);
			for (int x = 0; x < rowA.size(); x++) {
				double valA = rowA.get(x);
				double valB = rowB.get(x);
				if (!compareValues(valA, valB)) {
					throw new RuntimeException(String.format(
						"matrix A differs from matrix B at (%d, %d): %f, %f",
						y, x, valA, valB));
				}
			}

		}
	}

	public static void compareVectors(
			List<Double> vectorA,
			List<Double> vectorB)
	{
		final int rowsA = vectorA.size();
		final int rowsB = vectorB.size();

		if (rowsA != rowsB) {
			throw new RuntimeException(String.format(
					"vector A dimension (%d) differs from dimension of vector B (%d)",
					rowsA, rowsB));
		}
		for (int y = 0; y < vectorA.size(); y++) {
			double valA = vectorA.get(y);
			double valB = vectorB.get(y);
			if (!compareValues(valA, valB)) {
				throw new RuntimeException(String.format(
					"vector A differs from vector B at (%d): %f, %f",
					y, valA, valB));
			}

		}
	}

	/**
	 * Returns true if the supplied values are (quite) equal.
	 */
	public static boolean compareValues(double valA, double valB) {

		final double diff = Math.abs(valA - valB);
		final double relativeDiff = Math.abs((valA - valB) / (valA + valB));
		return !(diff > 0.00000000001) ||Double.isNaN(relativeDiff) || !(relativeDiff > 0.01);
	}

	public static List<List<Double>> transpose(List<List<Double>> matrix) {

		List<List<Double>> transposed = new ArrayList<List<Double>>(matrix.get(0).size());

		for (int c = 0; c < matrix.get(0).size(); c++) {
			transposed.add(new ArrayList<Double>(matrix.size()));
		}

		for (int r = 0; r < matrix.size(); r++) {
			List<Double> row = matrix.get(r);
			for (int c = 0; c < row.size(); c++) {
				transposed.get(c).add(row.get(c));
			}
		}

		return transposed;
	}

	public static List<List<Double>> parsePlainTextMatrix(File sourceFile) {
		return parsePlainTextMatrix(sourceFile, false);
	}
	public static List<List<Double>> parsePlainTextMatrix(File sourceFile, boolean transposed) {

		List<List<Double>> matrix = new ArrayList<List<Double>>();

		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(sourceFile);
			bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			while (line != null) {
				String[] strValues = line.split("[ ]+");
				if (transposed) {
					if (matrix.isEmpty()) {
						// initialize the rows
						for (int svi = 0; svi < strValues.length; svi++) {
							matrix.add(new ArrayList<Double>());
						}
					}
					for (int svi = 0; svi < strValues.length; svi++) {
						matrix.get(svi).add(Double.parseDouble(strValues[svi]));
					}
				} else {
					List<Double> row = new ArrayList<Double>(strValues.length);
					for (int svi = 0; svi < strValues.length; svi++) {
						row.add(Double.parseDouble(strValues[svi]));
					}
					matrix.add(row);
				}
				line = bufferedReader.readLine();
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			} else if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		return matrix;
	}

	public static boolean isEven(int number) {
		return ((number % 2) == 0);
	}

	/**
	 * Filters the weights with a p-norm moving average filter.
	 * See {@link http://www.mathworks.com/matlabcentral/fileexchange/12276-movingaverage-v3-1-mar-2008}.
	 * @param weights the weights to be filtered
	 * @param filterWidth the kernel size k (has to be an odd number)
	 * @param norm the value p for the p-norm to use
	 * @return filtered version of weights (same length)
	 */
	public static void pNormFilter(List<Double> weights, int filterWidth, int norm) {

		if (filterWidth < 1 || Util.isEven(filterWidth)) {
			throw new IllegalArgumentException("filterWidth has to be a positive, odd number");
		}

		if (filterWidth == 1) {
			return;
		}

		final int n = weights.size();
		final int kHalf = (filterWidth - 1) / 2;
		final double normInv = 1.0 / norm;
		final double filterWidthInvNormed = Math.pow(filterWidth, normInv);

		// calculate the cummultive sum
		List<Double> cumSum = new ArrayList<Double>(n + filterWidth);
		double curSum = 0.0;
		for (int i = -(kHalf + 1); i < n + kHalf; i++) {
			final double value = (i < 0) || (i >= n) ? 0.0 : Math.pow(weights.get(i), norm);
			curSum += value;
			cumSum.add(curSum);
		}

		// calculate the p-norm moving average values
		for (int i = 0; i < n; i++) {
			final double valSum = Math.pow(cumSum.get(i + filterWidth) - cumSum.get(i), normInv);
			weights.set(i, valSum / filterWidthInvNormed);
		}

//		N = length(w);
//		kHalf = (k - 1) / 2;
//		w = w.^ p;
//		%% Recursive moving average method
//		% With CUMSUM trick copied from RUNMEAN bwnew Jos van der Geest (12 mar 2008)
//		wnew = [zeros(1, kHalf+1), w, zeros(1, kHalf)];
//		wnew = cumsum(wnew);
//		wnew = (wnew(k+1:end) - wnew(1:end-k)).^(1/p);
//		wnew = wnew ./ (k.^(1/p));
	}






	private static final File TMP_RAW_DATA_FILE = new File(System.getProperty("user.home") + "/Projects/GWASpi/repos/GWASpi/rawDataTmp.ser"); // HACK

	static void storeForEncoding(
//			MarkersIterable markersIterable,
//			DataSetSource dataSetSource,
//			Iterable<Map.Entry<Integer, MarkerKey>> markers,
			List<MarkerKey> markers,
//			Map<SampleKey, SampleInfo> sampleInfos,
			List<SampleKey> samples,
			List<Affection> sampleAffecs,
			List<GenotypesList> markerGTs,
			int dSamples,
			int dEncoded,
			int n)
			throws IOException
	{
		if (!EXAMPLE_TEST) {
			return;
		}

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
		List<SampleKey> sampleKeys = new ArrayList<SampleKey>(samples);
		List<SampleInfo.Affection> sampleAffections = new ArrayList<SampleInfo.Affection>(sampleAffecs);
		List<GenotypesList> markerGenotypes = new ArrayList<GenotypesList>(markerGTs.size());
		for (GenotypesList mGTs : markerGTs) {
			markerGenotypes.add(CompactGenotypesList.FACTORY.createGenotypesList(mGTs));
		}

		try {
			FileOutputStream fout = new FileOutputStream(TMP_RAW_DATA_FILE);
			ObjectOutputStream oos = new ObjectOutputStream(fout);

			oos.writeObject((Integer) dSamples);
			oos.writeObject((Integer) dEncoded);
			oos.writeObject((Integer) n);

//			oos.writeObject(loadedMatrixSamples);
			oos.writeObject(markerKeys);
			oos.writeObject(sampleKeys);
			oos.writeObject(sampleAffections);
			oos.writeObject(markerGenotypes);

			oos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void runEncodingAndSVM(GenotypeEncoder genotypeEncoder) {

		int dSamples;
		int dEncoded;
		int n;

		List<MarkerKey> markerKeys;
		List<SampleKey> sampleKeys;
		List<Affection> sampleAffections;
		List<GenotypesList> markerGenotypes;
		try {
			FileInputStream fin = new FileInputStream(TMP_RAW_DATA_FILE);
			ObjectInputStream ois = new ObjectInputStream(fin);

			dSamples = (Integer) ois.readObject();
			dEncoded = (Integer) ois.readObject();
			n = (Integer) ois.readObject();

			markerKeys = (List<MarkerKey>) ois.readObject();
			sampleKeys = (List<SampleKey>) ois.readObject();
			sampleAffections = (List<Affection>) ois.readObject();
			markerGenotypes = (List<GenotypesList>) ois.readObject();

			ois.close();

			CombiTestMatrixOperation.runEncodingAndSVM(markerKeys, sampleKeys, sampleAffections, markerGenotypes, dSamples, dEncoded, n, genotypeEncoder);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean EXAMPLE_TEST = false; // HACK

	public static void main(String[] args) {

		EXAMPLE_TEST = true;
//
//		GenotypeEncoder genotypeEncoder = AllelicGenotypeEncoder.SINGLETON; // TODO
//		GenotypeEncoder genotypeEncoder = GenotypicGenotypeEncoder.SINGLETON; // TODO
		GenotypeEncoder genotypeEncoder = NominalGenotypeEncoder.SINGLETON; // TODO

//		runSVM(genotypeEncoder);

		runEncodingAndSVM(genotypeEncoder); // FIXME

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

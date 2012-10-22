package org.gwaspi.statistics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.gwaspi.global.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D1;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class ChiSqrBoundaryCalculator {

	private static final Logger log = LoggerFactory.getLogger(ChiSqrBoundaryCalculator.class);

	protected static int df = 2;
	protected static String method = "2stDev"; // variance, 2stDev, samplingCI, calculatedCI
	protected static int simNb = 400;
	protected static int pointsNb = 10000; //Nb of evenly spaced points to be kept from the complete distribution
	protected static int N = 10000; //Nb of points in each distribution
	protected static double lowFrac = 0.025;
	protected static double uppFrac = 0.025;
	protected static String netCDFFile = "";
	protected static String boundaryPath = "";

	public static void main(String[] args) throws IOException, MathException {

		boundaryPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/chisqrboundary" + df + "_" + method + simNb + "x" + N + ".txt";
		netCDFFile = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/chisqrdist" + df + ".nc";
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(netCDFFile, false);

		generatChisqrDistributions(ncfile, df);

		if (method.equals("variance")) {
			calculateChisqrBoundaryByVariance();
		}
		if (method.equals("2stDev")) {
			calculateChisqrBoundaryByStDev();
		}
		if (method.equals("samplingCI")) {
			calculateChisqrBoundaryBySampling();
		}
		if (method.equals("calculatedCI")) {
			calculateChisqrBoundaryByFormula();
		}
	}

	protected static void generatChisqrDistributions(NetcdfFileWriteable ncfile, int df) throws IOException {

		// add dimensions
		Dimension sizeDim = ncfile.addDimension("size", 0, true, true, false);
		Dimension simsDim = ncfile.addDimension("sims", simNb); //0=>AA, 1=>Aa, 2=>aa, 3=>00
		List<Dimension> distSpace = new ArrayList<Dimension>();
		distSpace.add(sizeDim);
		distSpace.add(simsDim);

		// define Variable
		ncfile.addVariable("distributions", DataType.DOUBLE, distSpace);

		// create the file
		try {
			ncfile.create();
		} catch (IOException ex) {
			log.error("Failed creating file " + ncfile.getLocation(), ex);
		}

		// Make simNb X² distributions
		for (int i = 0; i < simNb; i++) {
			try {
				List<Double> expChiSqrDist = null;
				if (df == 1) {
					expChiSqrDist = Chisquare.getChiSquareDistributionDf1(N, 1.0f);
				}
				if (df == 2) {
					expChiSqrDist = Chisquare.getChiSquareDistributionDf2(N, 1.0f);
				}

				Collections.sort(expChiSqrDist);

				int offset = Math.round(N / pointsNb);

				ArrayDouble chiArray = new ArrayDouble.D2(pointsNb, 1);
				Index ima = chiArray.getIndex();
				int k = 0;
				for (int j = 0; j < expChiSqrDist.size(); j = j + offset) {
					chiArray.setDouble(ima.set(k, 0), expChiSqrDist.get(j));
					k++;
				}
				int[] offsetOrigin = new int[]{0, i}; //0,0
				ncfile.write("distributions", offsetOrigin, chiArray);

				if (i % 100 == 0) {
					log.info("{} X² simulations of {} run", i, simNb);
				}
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			}
		}

		// close the file
		try {
			ncfile.close();
		} catch (IOException ex) {
			log.error("Failed creating file " + ncfile.getLocation(), ex);
		}
	}

	protected static void calculateChisqrBoundaryBySampling() throws IOException {

		FileWriter repFW = new FileWriter(boundaryPath);
		BufferedWriter repBW = new BufferedWriter(repFW);

		NetcdfFile ncfile = NetcdfFile.open(netCDFFile);
		List<Dimension> dims = ncfile.getDimensions();
		Dimension sizeDim = dims.get(0);
		Dimension simsDim = dims.get(1);

		String varName = "distributions";
		Variable distributions = ncfile.findVariable(varName);

		try {
			for (int i = 0; i < pointsNb; i++) {
				//distributions(i:i:1, 0:simsNb:1)
				ArrayDouble.D2 rdDoubleArrayD2 = (ArrayDouble.D2) distributions.read(i + ":" + i + ":1, 0:" + (simsDim.getLength() - 1) + ":1");
				ArrayDouble.D1 rdDoubleArrayD1 = (D1) rdDoubleArrayD2.reduce();

				SortedSet<Double> currentTS = new TreeSet<Double>();
				for (int j = 0; j < rdDoubleArrayD2.getSize(); j++) {
					currentTS.add(rdDoubleArrayD1.get(j));
				}

				double currentTot = 0;

				int loCount = 0;
				double low95 = 0;
				int topCount = 0;
				double top95 = 0;
				for (Double key : currentTS) {
					long lowLimit = Math.round(simNb * lowFrac) - 1;
					if (loCount == lowLimit) {
						low95 = key;
						loCount++;
					} else {
						loCount++;
					}

					long uppLimit = Math.round(simNb * uppFrac) - 1;
					if (topCount == currentTS.size() - uppLimit) {
						top95 = key;
						topCount++;
					} else {
						topCount++;
					}

					currentTot += key;
				}
				double avg = currentTot / simNb;

				StringBuilder sb = new StringBuilder();
				sb.append(top95);
				sb.append(",");
				sb.append(avg);
				sb.append(",");
				sb.append(low95);
				repBW.append(sb + "\n");
			}
		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}

		repBW.close();
		repFW.close();

		log.info("Confidence boundary created for {} points", N);
	}

	protected static void calculateChisqrBoundaryByFormula() throws IOException, MathException {

		FileWriter repFW = new FileWriter(boundaryPath);
		BufferedWriter repBW = new BufferedWriter(repFW);

		NetcdfFile ncfile = NetcdfFile.open(netCDFFile);
		List<Dimension> dims = ncfile.getDimensions();
		Dimension sizeDim = dims.get(0);
		Dimension simsDim = dims.get(1);

		String varName = "distributions";
		Variable distributions = ncfile.findVariable(varName);

		try {
			for (int i = 0; i < pointsNb; i++) {
				//distributions(i:i:1, 0:simsNb:1)
				ArrayDouble.D2 rdDoubleArrayD2 = (ArrayDouble.D2) distributions.read(i + ":" + i + ":1, 0:" + (simsDim.getLength() - 1) + ":1");
				ArrayDouble.D1 rdDoubleArrayD1 = (D1) rdDoubleArrayD2.reduce();

				double sampleSize = rdDoubleArrayD2.getSize();
				double currentTot = 0;

				double[] allValues = new double[(int) sampleSize];
				for (int j = 0; j < sampleSize; j++) {
					allValues[j] = rdDoubleArrayD1.get(j);
					currentTot += rdDoubleArrayD1.get(j);
				}

				StandardDeviation stdDev = new StandardDeviation();
				double stdDevValue = stdDev.evaluate(allValues);

				double currentAvg = currentTot / simNb;

				TDistributionImpl tDistImpl = new TDistributionImpl(sampleSize - 1);
				double tInvCumulProb = tDistImpl.inverseCumulativeProbability(0.05d);
				double tCumulProb = tDistImpl.cumulativeProbability(0.05d);

//                confidenceInterval = (
//                                        STDEV(Ys)
//                                        /
//                                        SQRT(COUNT(Ys))
//                                     )
//                                     *
//                                     TINV(0.05, COUNT(Ys)-1)

				double confidenceInterval = (stdDevValue
						/ Math.sqrt(sampleSize))
						* tInvCumulProb;

				double low95 = currentAvg - confidenceInterval;
				double top95 = currentAvg + confidenceInterval;

				StringBuilder sb = new StringBuilder();
				sb.append(top95);
				sb.append(",");
				sb.append(currentAvg);
				sb.append(",");
				sb.append(low95);
				repBW.append(sb + "\n");
			}
		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}

		repBW.close();
		repFW.close();

		log.info("Confidence boundary created for {} points", N);
	}

	protected static void calculateChisqrBoundaryByStDev() throws IOException, MathException {

		FileWriter repFW = new FileWriter(boundaryPath);
		BufferedWriter repBW = new BufferedWriter(repFW);

		NetcdfFile ncfile = NetcdfFile.open(netCDFFile);
		List<Dimension> dims = ncfile.getDimensions();
		Dimension sizeDim = dims.get(0);
		Dimension simsDim = dims.get(1);

		String varName = "distributions";
		Variable distributions = ncfile.findVariable(varName);

		try {
			for (int i = 0; i < pointsNb; i++) {
				//distributions(i:i:1, 0:simsNb:1)
				ArrayDouble.D2 rdDoubleArrayD2 = (ArrayDouble.D2) distributions.read(i + ":" + i + ":1, 0:" + (simsDim.getLength() - 1) + ":1");
				ArrayDouble.D1 rdDoubleArrayD1 = (D1) rdDoubleArrayD2.reduce();

				double sampleSize = rdDoubleArrayD2.getSize();
				double currentTot = 0;

				double[] allValues = new double[(int) sampleSize];
				for (int j = 0; j < sampleSize; j++) {
					allValues[j] = rdDoubleArrayD1.get(j);
					currentTot += rdDoubleArrayD1.get(j);
				}

				StandardDeviation stdDev = new StandardDeviation();
				double stdDevValue = stdDev.evaluate(allValues);

				double currentAvg = currentTot / simNb;

				double low95 = currentAvg - (2 * stdDevValue); //Display 2 standard deviations
				double top95 = currentAvg + (2 * stdDevValue); //Display 2 standard deviations

				StringBuilder sb = new StringBuilder();
				sb.append(top95);
				sb.append(",");
				sb.append(currentAvg);
				sb.append(",");
				sb.append(low95);
				repBW.append(sb + "\n");
			}
		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}

		repBW.close();
		repFW.close();

		log.info("Confidence boundary created for {} points", N);
	}

	protected static void calculateChisqrBoundaryByVariance() throws IOException, MathException {

		FileWriter repFW = new FileWriter(boundaryPath);
		BufferedWriter repBW = new BufferedWriter(repFW);

		NetcdfFile ncfile = NetcdfFile.open(netCDFFile);
		List<Dimension> dims = ncfile.getDimensions();
		Dimension sizeDim = dims.get(0);
		Dimension simsDim = dims.get(1);

		String varName = "distributions";
		Variable distributions = ncfile.findVariable(varName);

		try {
			for (int i = 0; i < pointsNb; i++) {
				//distributions(i:i:1, 0:simsNb:1)
				ArrayDouble.D2 rdDoubleArrayD2 = (ArrayDouble.D2) distributions.read(i + ":" + i + ":1, 0:" + (simsDim.getLength() - 1) + ":1");
				ArrayDouble.D1 rdDoubleArrayD1 = (D1) rdDoubleArrayD2.reduce();

				double sampleSize = rdDoubleArrayD2.getSize();
				double currentTot = 0;

				double[] allValues = new double[(int) sampleSize];
				for (int j = 0; j < sampleSize; j++) {
					allValues[j] = rdDoubleArrayD1.get(j);
					currentTot += rdDoubleArrayD1.get(j);
				}

				Variance variance = new Variance();
				double varianceValue = variance.evaluate(allValues);

				double currentAvg = currentTot / simNb;

				double low95 = currentAvg - varianceValue;
				double top95 = currentAvg + varianceValue;

				StringBuilder sb = new StringBuilder();
				sb.append(top95);
				sb.append(",");
				sb.append(currentAvg);
				sb.append(",");
				sb.append(low95);
				repBW.append(sb + "\n");
			}
		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}

		repBW.close();
		repFW.close();

		log.info("Confidence boundary created for {} points", N);
	}
}

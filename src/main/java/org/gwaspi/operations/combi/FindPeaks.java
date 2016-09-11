/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.differentiation.UnivariateFunctionDifferentiator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.gwaspi.global.IndicesList;

/**
 * Very simple implementation of a 1D peak-finding algorithm.
 * It is adapted from the GPL licensed Octave implementation of ??? find min peaks? ???.
 * But as we do not require all of its parameters,
 * the implementation of some of them was omitted (minPeakWidth, doubleSided).
 *
 */
public class FindPeaks {

	private static class Entry<T extends Number & Comparable<T>> implements Comparable<Entry<T>> {

		private final T value;
		private final Integer index;

		public Entry(final T value, final Integer index) {

			this.value = value;
			this.index = index;
		}

		/**
		 * @return the value
		 */
		public T getValue() {
			return value;
		}

		/**
		 * @return the index
		 */
		public Integer getIndex() {
			return index;
		}

		@Override
		public int compareTo(final Entry<T> other) {
			return value.compareTo(other.value);
		}
	}

	private final List<Integer> origIndices;
	private final List<Double> origEntries;
	private final Double minPeakHeight;
	private final Integer minPeakDistance;

	public FindPeaks(
			final List<Integer> origIndices,
			final List<Double> origEntries,
			final Double minPeakHeight,
			final Integer minPeakDistance,
			final Integer minPeakWidth,
			final Boolean doubleSided)
	{
		this.origIndices = (origIndices != null) ? origIndices : new IndicesList(origEntries.size());
		this.origEntries = origEntries;
		this.minPeakHeight = minPeakHeight;
		this.minPeakDistance = minPeakDistance;
	}

	public FindPeaks(
			final List<Double> origEntries,
			final Double minPeakHeight,
			final Integer minPeakDistance,
			final Integer minPeakWidth,
			final Boolean doubleSided)
	{
		this(null, origEntries, minPeakHeight, minPeakDistance, minPeakWidth, doubleSided);
	}

	public Double getMinPeakHeightDefault() {

		final List<Double> origEntriesClean = new ArrayList<Double>(origEntries);
		abs((List<Double>) origEntriesClean);
		return (Double) (2 * stdDev((List<Double>) origEntriesClean));
	}

	public Double getMinPeakHeight() {

		if (minPeakHeight == null) {
			return getMinPeakHeightDefault();
		} else {
			return minPeakHeight;
		}
	}

	public Integer getMinPeakDistanceDefault() {
		return 4;
	}

	public Integer getMinPeakDistance() {

		if (minPeakDistance == null) {
			return getMinPeakDistanceDefault();
		} else {
			return minPeakDistance;
		}
	}

	private static Double mean(final List<Double> data) {

		double sum = 0.0;
		for (final Double date : data) {
			sum += date;
		}

		return sum / data.size();
	}

	private static Double variance(final List<Double> data) {

		final double mean = mean(data);
		double variance = 0.0;
		for (final Double date : data) {
			final double deviation = date - mean;
			variance += deviation * deviation;
		}

		return variance;
	}

	private static Double stdDev(final List<Double> data) {

		final double variance = variance(data);
		return Math.sqrt(variance);
	}

	private static void abs(final List<Double> data) {

		for (int i = 0; i < data.size(); i++) {
			data.set(i, Math.abs(data.get(i)));
		}
	}

	private static double min(final List<Double> data) {

		double min = Double.MAX_VALUE;
		for (final Double date : data) {
			if (date < min) {
				min = date;
			}
		}

		return min;
	}

	/**
	 * Finds peaks.
	 * @return peaks in descending order of value, with original indices as keys
	 */
	public Map<Integer, Double> findPeaks() {

		if (origEntries.size() < 3) {
			throw new IllegalArgumentException("must be a vector of at least 3 elements");
		}

		List<Double> data = (List<Double>) origEntries; // HACK

		if (min(data) < 0.0) {
			throw new IllegalArgumentException(
					"Data contains negative values. You may want the \"DoubleSided\" option");
		}

//		XXX the first basic find peaks can be done much simpler then with derivatives, with a simple walk through, as we are not in matlab, and our loops are fast! :D;

		// We extend the function with one value above and one below our actual values,
		// so we can do propper numerical differentiation at the first
		// and the last actual value aswell.
		// We do this by adding the 2nd value in front, and the second last at the end,
		// as this allows to detect peaks at the ends aswell.
		double x[] = new double[data.size() + 2];
		for (int index = -1; index <= data.size(); index++) {
			x[index + 1] = index;
		}
		double y[] = new double[data.size() + 2];
		y[0] = data.get(1);
		for (int index = 0; index < data.size(); index++) {
			y[index + 1] = data.get(index);
		}
		y[data.size() + 1] = data.get(data.size() - 2);

//		UnivariateInterpolator interpolator = new SplineInterpolator();
//		UnivariateInterpolator interpolator = new DividedDifferenceInterpolator();
		UnivariateInterpolator interpolator = new LinearInterpolator();
		UnivariateFunction function = interpolator.interpolate(x, y);

		// create a differentiator using 5 points and 0.01 step
		UnivariateFunctionDifferentiator differentiator
				= new FiniteDifferencesDifferentiator(5, 0.01);

		// create a new function that computes both the value and the derivatives
		// using DerivativeStructure
		UnivariateDifferentiableFunction completeF = differentiator.differentiate(function);

		// now we can compute and display the value and its derivatives.
		// here we decided to display up to second order derivatives,
		// because we feed completeF with order 2 DerivativeStructure instances
		List<Integer> peakIndices = new ArrayList<Integer>(); // TODO check performance; is LinkedList faster?
//		List<Integer> peakIndices = new LinkedList<Integer>();
		double lastDf1Signum = 0.0;
		for (int index = 0; index < data.size(); index++) {
			final double x2 = index;
			DerivativeStructure xDS = new DerivativeStructure(1, 2, 0, x2);
			DerivativeStructure yDS = completeF.value(xDS);
			double df1Signum = Math.signum(yDS.getPartialDerivative(1));
			if ((((df1Signum - lastDf1Signum) != 0.0)
					|| (Math.abs(yDS.getPartialDerivative(1)) < 0.0000000000001))
					&& (yDS.getPartialDerivative(2) < 0.0))
			{
				if (data.get(index) >= minPeakHeight) {
					peakIndices.add(index);
				}
			}
			lastDf1Signum = df1Signum;
		}

		final List<Double> dataC = data;
		Collections.sort(peakIndices, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return dataC.get(o2).compareTo(dataC.get(o1));
			}
		});

		// TODO: start with first node, comparing to all following ones, then continue to the second, compare to all following ones, ...
		for (int piCur = 0; piCur < (peakIndices.size() - 1); piCur++) {
			for (int piComp = piCur + 1; piComp < peakIndices.size(); piComp++) {
				if (Math.abs(origIndices.get(peakIndices.get(piCur))
						- origIndices.get(peakIndices.get(piComp)))
						< minPeakDistance)
				{
					peakIndices.remove(piComp);
					piComp--;
				}
			}
		}

		final Map<Integer, Double> peakIndicesToValue
				= new LinkedHashMap<Integer, Double>();
		for (final Integer peakIndex : peakIndices) {
			peakIndicesToValue.put(peakIndex, data.get(peakIndex));
		}

		return peakIndicesToValue;
	}
}

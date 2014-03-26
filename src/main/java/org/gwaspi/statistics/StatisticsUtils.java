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

package org.gwaspi.statistics;

import java.math.BigInteger;

public class StatisticsUtils {

	private StatisticsUtils() {
	}

	/**
	 * {@see https://en.wikipedia.org/wiki/Hardy%E2%80%93Weinberg_principle}
	 */
	public static double calculatePunnettFrequency(final int obsAA, final int obsAa, final int numSamples) {
		return (double) ((obsAA * 2) + obsAa) / (numSamples * 2);
	}

	/**
	 * Calculates the mathematical factorial of a non-negative integer.
	 * {@see https://en.wikipedia.org/wiki/Factorial}
	 * @param n non-negative integer
	 * @return n!
	 */
	public static BigInteger factorial(final BigInteger n) {

		if (n.compareTo(BigInteger.ONE) <= 0) { // base case
			return BigInteger.ONE;
		} else {
			return factorial(n.subtract(BigInteger.ONE)).multiply(n);
		}
	}
}

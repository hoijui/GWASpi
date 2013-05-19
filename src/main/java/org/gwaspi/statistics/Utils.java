package org.gwaspi.statistics;

import java.math.BigInteger;

public class Utils {

	private Utils() {
	}

	public static double calculatePunnettFrequency(int obsAA, int obsAa, int sampleNb) {
		double freq = (double) ((obsAA * 2) + obsAa) / (sampleNb * 2);
		return freq;

	}

	public static BigInteger factorial(BigInteger n) {
		if (n.compareTo(BigInteger.ONE) <= 0) // base case
		{
			return BigInteger.ONE;
		} else {
			return factorial(n.subtract(BigInteger.ONE)).multiply(n);
		}
	}
}

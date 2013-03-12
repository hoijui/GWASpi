package org.gwaspi.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author u56124
 */
public class StandardDeviation {

	private final Logger log = LoggerFactory.getLogger(StandardDeviation.class);

	public double findMean(int[] array) {
		double total = 0;
		for (int i = 0; i < array.length; i++) {
			total = total + array[i];
		}
		double mean = total / array.length;
		return mean;
	}

	public void findStandardDeviation(int[] array) {
		double mean = findMean(array);
		log.info("Mean is: {}", mean);
		double d1 = 0;
		double d2;
		for (int i = 0; i < array.length; i++) {
			d2 = (mean - array[i]) * (mean - array[i]);
			d1 = d2 + d1;
		}
		log.info("Standard Deviation: " + Math.sqrt((d1 / (array.length - 1))));
	}

	public static void main(String[] args) {
		int[] array = new int[]{0, 1, 6, 4, 8787, 2, 3564, 645, 54};
		StandardDeviation sd = new StandardDeviation();
		sd.findStandardDeviation(array);
	}
}

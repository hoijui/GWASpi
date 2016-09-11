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
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestFindPeaks {

	public static <T> String createErrorString(
			final String description,
			final List<T> result,
			final List<T> expected)
	{
		return description
				+ "\n result  : \"" + result.toString() + "\""
				+ "\n expected: \"" + expected.toString() + "\"";
	}

	private static <T> void checkResultEqualsAnswer(final List<T> result, final List<T> expected) {

		if (result.size() != expected.size()) {
			Assert.fail(createErrorString("Result and expected answer aren't the same length, "
					+ result.size() + " vs " + expected.size() + ".", result, expected));
		}
		for (int i = 0; i < result.size(); i++) {
			if (!result.get(i).equals(expected.get(i))) {
				Assert.fail(createErrorString("Failed to convert correctly at position: " + i,
						result, expected));
			}
		}
	}

	public static void testFindPeaks(final FindPeaks findPeaks, final List<Integer> expectedPeaks) {

		final List<Integer> result = new ArrayList<Integer>(findPeaks.findPeaks().keySet());
		checkResultEqualsAnswer(result, expectedPeaks);
	}

	@Test
	public void testSimple() {

		final List<Double> input = new ArrayList<Double>();
		input.add(0.1);
		input.add(0.2);
		input.add(0.3);
		input.add(0.4);
		input.add(0.3);
		input.add(0.2);
		input.add(0.3);
		input.add(0.4);
		input.add(0.5);
		input.add(0.6);
		input.add(0.5);
		input.add(0.6);
		input.add(0.5);
		input.add(0.4);
		input.add(0.3);

		final List<Integer> expectedPeaks = new ArrayList<Integer>();
		expectedPeaks.add(9);
		expectedPeaks.add(3);

		final FindPeaks findPeaks = new FindPeaks(input, 0.35, 4, 1, false);

		testFindPeaks(findPeaks, expectedPeaks);
	}

	@Test
	public void testStart() {

		final List<Double> input = new ArrayList<Double>();
		input.add(0.7);
		input.add(0.2);
		input.add(0.3);
		input.add(0.4);
		input.add(0.3);
		input.add(0.2);
		input.add(0.3);
		input.add(0.4);
		input.add(0.5);
		input.add(0.6);
		input.add(0.5);
		input.add(0.6);
		input.add(0.5);
		input.add(0.4);
		input.add(0.3);

		final List<Integer> expectedPeaks = new ArrayList<Integer>();
		expectedPeaks.add(0);
		expectedPeaks.add(9);

		final FindPeaks findPeaks = new FindPeaks(input, 0.35, 4, 1, false);

		testFindPeaks(findPeaks, expectedPeaks);
	}

	@Test
	public void testEnd() {

		final List<Double> input = new ArrayList<Double>();
		input.add(0.1);
		input.add(0.2);
		input.add(0.3);
		input.add(0.4);
		input.add(0.3);
		input.add(0.2);
		input.add(0.3);
		input.add(0.4);
		input.add(0.5);
		input.add(0.6);
		input.add(0.5);
		input.add(0.6);
		input.add(0.5);
		input.add(0.4);
		input.add(0.6);

		final List<Integer> expectedPeaks = new ArrayList<Integer>();
		expectedPeaks.add(9);
		expectedPeaks.add(14);
		expectedPeaks.add(3);

		final FindPeaks findPeaks = new FindPeaks(input, 0.35, 4, 1, false);

		testFindPeaks(findPeaks, expectedPeaks);
	}
}

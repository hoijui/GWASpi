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
package org.gwaspi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.ComparisonFailure;
import org.junit.Test;

/**
 *
 * @author hardy
 */
public class TestCompactGenotypesList {

	@Test
	public void testCalcByteBitMask() {

		testCompareBitmasks((byte) 0, (byte) 0x00, CompactGenotypesList.calcByteBitMask((byte) 0));
		testCompareBitmasks((byte) 1, (byte) 0x01, CompactGenotypesList.calcByteBitMask((byte) 1));
		testCompareBitmasks((byte) 2, (byte) 0x03, CompactGenotypesList.calcByteBitMask((byte) 2));
		testCompareBitmasks((byte) 3, (byte) 0x07, CompactGenotypesList.calcByteBitMask((byte) 3));
		testCompareBitmasks((byte) 4, (byte) 0x0F, CompactGenotypesList.calcByteBitMask((byte) 4));
		testCompareBitmasks((byte) 5, (byte) 0x1F, CompactGenotypesList.calcByteBitMask((byte) 5));
		testCompareBitmasks((byte) 6, (byte) 0x3F, CompactGenotypesList.calcByteBitMask((byte) 6));
		testCompareBitmasks((byte) 7, (byte) 0x7F, CompactGenotypesList.calcByteBitMask((byte) 7));
		testCompareBitmasks((byte) 8, (byte) 0xFF, CompactGenotypesList.calcByteBitMask((byte) 8));
	}

	private static void testCompareBitmasks(byte numBits, byte expected, byte actual) {

		if (expected != actual) {
			throw new ComparisonFailure(
					"Generated wrong bit mask for " + numBits + " bits",
					CompactGenotypesList.byteToBitString(expected),
					CompactGenotypesList.byteToBitString(actual)
			);
		}
	}

	@Test
	public void testAllSame() {

		List<byte[]> originalGenotypes = new ArrayList<byte[]>();
		originalGenotypes.add(new byte[] {'A', 'A'});
		originalGenotypes.add(new byte[] {'A', 'A'});
		originalGenotypes.add(new byte[] {'A', 'A'});
		originalGenotypes.add(new byte[] {'A', 'A'});
		originalGenotypes.add(new byte[] {'A', 'A'});

		test(originalGenotypes);
	}

	@Test
	public void testFullMarkerAllBitsUsed() {

		// 4 different values -> 2 bits per value
		// 8 values -> 2*8 bits = 2 Bytes of storage required
		// We use this to check that there is no problem
		// if storage bytes are fully used.

		List<byte[]> originalGenotypes = new ArrayList<byte[]>();
		originalGenotypes.add(new byte[] {'A', 'A'});
		originalGenotypes.add(new byte[] {'A', 'T'});
		originalGenotypes.add(new byte[] {'T', 'A'});
		originalGenotypes.add(new byte[] {'T', 'T'});
		originalGenotypes.add(new byte[] {'A', 'A'});
		originalGenotypes.add(new byte[] {'A', 'T'});
		originalGenotypes.add(new byte[] {'T', 'A'});

		test(originalGenotypes);
	}

	@Test
	public void testFullMarkerUnusedBits() {

		// 5 different values -> 3 bits per value
		// 10 values -> 3*10 bits = 4- Bytes of storage required
		// We use this to check that there is no problem
		// if storage bytes are not fully used.

		List<byte[]> originalGenotypes = new ArrayList<byte[]>();
		originalGenotypes.add(new byte[] {'A', 'A'});
		originalGenotypes.add(new byte[] {'A', 'T'});
		originalGenotypes.add(new byte[] {'T', 'A'});
		originalGenotypes.add(new byte[] {'T', 'T'});
		originalGenotypes.add(new byte[] {'A', 'A'});
		originalGenotypes.add(new byte[] {'0', '0'});
		originalGenotypes.add(new byte[] {'A', 'T'});
		originalGenotypes.add(new byte[] {'T', 'A'});
		originalGenotypes.add(new byte[] {'0', '0'});
		originalGenotypes.add(new byte[] {'T', 'T'});

		test(originalGenotypes);
	}

	private static void test(List<byte[]> originalGenotypes) {

		Collection<byte[]> possibleGenotypes = extractUniqueValues(originalGenotypes);

		CompactGenotypesList cgl = new CompactGenotypesList(originalGenotypes, possibleGenotypes);

		compare(cgl, originalGenotypes);
	}

	private static void compare(GenotypesList genotypesList, Collection<byte[]> originalValues) {

		if (genotypesList.size() != originalValues.size()) {
			throw new RuntimeException("Different sizes in original ("
					+ originalValues.size() + ") and resulting ("
					+ genotypesList.size() + ") list");
		}

		Iterator<byte[]> resultIt = genotypesList.iterator();
		int index = 0;
		for (byte[] originalValue : originalValues) {
			byte[] resultValue = resultIt.next();
			if (resultValue.length != originalValue.length) {
				throw new ComparisonFailure(
						"Different length in original ("
						+ originalValue.length + ") and resulting ("
						+ resultValue.length + ") value at index "
						+ index + " / " + originalValues.size(),
						String.valueOf(originalValue.length),
						String.valueOf(resultValue.length)
				);
			}
			int arrInd = 0;
			for (byte origVal : originalValue) {
				if (resultValue[arrInd] != origVal) {
					throw new ComparisonFailure(
							"Different value in original ("
							+ (char) origVal + ") and resulting ("
							+ (char) resultValue[arrInd] + ") value at index "
							+ arrInd + " / " + originalValue.length,
							String.valueOf((char) origVal),
							String.valueOf((char) resultValue[arrInd])
					);
				}
				arrInd++;
			}
			index++;
		}
	}

	private static Collection<byte[]> extractUniqueValues(Collection<byte[]> originalValues) {

		Map<Integer, byte[]> uniqueValues = new HashMap<Integer, byte[]>();
		for (byte[] value : originalValues) {
			uniqueValues.put(Genotype.hashCode(value), value);
		}
		return uniqueValues.values();
	}
}

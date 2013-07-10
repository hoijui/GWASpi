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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Genotype;

/**
 * Uses this encoding scheme:<br/>
 * {'A', 'A'} -> {0.0, 1.0,   0.0, 1.0}<br/>
 * {'A', 'C'} -> {0.0, 1.0,   1.0, 0.0}<br/>
 * {'C', 'A'} -> {1.0, 0.0,   0.0, 1.0}<br/>
 * {'C', 'C'} -> {1.0, 0.0,   1.0, 0.0}<br/>
 * With <code>'A'</code> standing representative for the lexicographically
 * smaller allele, and <code>'C'</code> for the bigger one.
 */
public class AllelicGenotypeEncoder extends EncodingTableBasedGenotypeEncoder {

	public static final AllelicGenotypeEncoder SINGLETON = new AllelicGenotypeEncoder();

//	private static final List<List<Double>> ENCODED_VALUES;
//	static {
//		ENCODED_VALUES = new ArrayList<List<Double>>(4);
//
//		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
//				Arrays.asList(0.0, 1.0, 0.0, 1.0)))); // {'A', 'A'}
//		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
//				Arrays.asList(0.0, 1.0, 1.0, 0.0)))); // {'A', 'C'}
//		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
//				Arrays.asList(1.0, 0.0, 0.0, 1.0)))); // {'C', 'A'}
//		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
//				Arrays.asList(1.0, 0.0, 1.0, 0.0)))); // {'C', 'C'}
//	}
	private static final Map<Integer, List<Double>> ENCODED_VALUES_LOWER;
	static {
		ENCODED_VALUES_LOWER = new HashMap<Integer, List<Double>>(5);

		ENCODED_VALUES_LOWER.put(0, Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(0.0, 0.0, 0.0, 0.0)))); // "00"
//				Arrays.asList(1.0, 1.0, 1.0, 1.0)))); // "00"
		ENCODED_VALUES_LOWER.put(4, Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(0.0, 1.0, 0.0, 1.0)))); // "AA"
		ENCODED_VALUES_LOWER.put(5, Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(0.0, 1.0, 1.0, 0.0)))); // "AT"
		ENCODED_VALUES_LOWER.put(7, Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(1.0, 0.0, 0.0, 1.0)))); // "TA"
		ENCODED_VALUES_LOWER.put(8, Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(1.0, 0.0, 1.0, 0.0)))); // "TT"
	}
	private static final Map<Integer, List<Double>> ENCODED_VALUES_UPPER;
	static {
		ENCODED_VALUES_UPPER = new HashMap<Integer, List<Double>>(5);

		ENCODED_VALUES_UPPER.put(0, Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(0.0, 0.0, 0.0, 0.0)))); // "00"
//				Arrays.asList(1.0, 1.0, 1.0, 1.0)))); // "00"
		ENCODED_VALUES_UPPER.put(4, Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(1.0, 0.0, 1.0, 0.0)))); // "AA"
		ENCODED_VALUES_UPPER.put(5, Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(1.0, 0.0, 0.0, 1.0)))); // "AT"
		ENCODED_VALUES_UPPER.put(7, Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(0.0, 1.0, 1.0, 0.0)))); // "TA"
		ENCODED_VALUES_UPPER.put(8, Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(0.0, 1.0, 0.0, 1.0)))); // "TT"
	}

	private AllelicGenotypeEncoder() {
	}

	@Override
	public Map<Genotype, List<Double>> generateEncodingTable(
			List<Genotype> possibleGenotypes,
			List<Genotype> rawGenotypes)
	{
		Map<Genotype, List<Double>> encodingTable
				= new HashMap<Genotype, List<Double>>(possibleGenotypes.size());

		Map<Genotype, Integer> baseEncodingTable
				= generateBaseEncodingTable(possibleGenotypes);

//		SortedSet<Genotype> sortedGenotypes = new TreeSet<Genotype>(possibleGenotypes);
//
//		Iterator<List<Double>> encodedValues = ENCODED_VALUES.iterator();
//		for (Genotype genotype : sortedGenotypes) {
//			encodingTable.put(genotype, encodedValues.next());
//		}



//		Genotype lastNonZeroGt = rawGenotypes.get(0);
////		for (Genotype rawGenotype : rawGenotypes) {
//		for (int rgi = rawGenotypes.size() - 1; rgi >= 0; rgi--) {
//			Genotype rawGenotype = rawGenotypes.get(rgi);
//			if (rawGenotype.getFather() != '0' && rawGenotype.getMother() != '0') {
//				lastNonZeroGt = rawGenotype;
//				break;
//			}
//		}
//		// true if the alphabetically lower letter appears first in the gt samples,
//		// eg. the first value is "AA" or "AT", false is "TA" or "TT"
//		Iterator<Genotype> baseEncodingKeyIterator = baseEncodingTable.keySet().iterator();
//		Genotype firstNonZeroBaseGt = baseEncodingKeyIterator.next();
//		if (firstNonZeroBaseGt.getFather() == '0') {
//			firstNonZeroBaseGt = baseEncodingKeyIterator.next();
//		}
////		final byte lowestCharFirst = (byte) Math.min(firstNonZeroGt.getFather(), firstNonZeroGt.getMother());
//		final byte charLast = lastNonZeroGt.getMother();
//		final byte lowestCharFirstBase = (byte) Math.min(firstNonZeroBaseGt.getFather(), firstNonZeroBaseGt.getMother());
////		final boolean isLowerFirst = (firstNonZeroGt.getFather() == firstNonZeroBaseGt.getFather());
////		final boolean isLowerFirst = (firstNonZeroGt.getMother() == firstNonZeroBaseGt.getMother());
//		// true if the alphabetically lower letter appears in the first non-"00" GT.
//		// eg. the first value is "AA", "AT" or "TA", false if it is "TT"
//		final boolean lowerInFirst = (charLast == lowestCharFirstBase);



//		Map<Integer, List<Double>> encodedValues
//				= lowerInFirst
//				? ENCODED_VALUES_LOWER
//				: ENCODED_VALUES_UPPER;
		Map<Integer, List<Double>> encodedValues
				= ENCODED_VALUES_UPPER;

		for (Map.Entry<Genotype, Integer> baseEncoding : baseEncodingTable.entrySet()) {
System.out.println("XXX " + baseEncoding.getKey() + " -> " + baseEncoding.getValue());
//			double curValue;
//			switch (baseEncoding.getValue()) {
//				case 4: // "AA"
//					curValue = 3.0;
//					break;
//				case 5: // "AG"
//					curValue = 2.0;
//					break;
//				case 7: // "GA"
//					curValue = 1.0;
//					break;
//				case 8: // "GG"
//					curValue = 0.0;
//					break;
//				default: // "00"
//					curValue = 4.0;
//					break;
//			}
			encodingTable.put(baseEncoding.getKey(), encodedValues.get(baseEncoding.getValue()));
		}

		return encodingTable;
	}

	@Override
	public int getEncodingFactor() {
		return 4;
	}
}

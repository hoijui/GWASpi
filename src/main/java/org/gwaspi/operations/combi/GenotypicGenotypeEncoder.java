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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses this encoding scheme:<br/>
 * {'A', 'A'} -> {1.0, 0.0, 0.0}<br/>
 * {'A', 'C'} -> {0.0, 1.0, 0.0}<br/>
 * {'C', 'A'} -> {0.0, 1.0, 0.0}<br/>
 * {'C', 'C'} -> {0.0, 0.0, 1.0}<br/>
 * With <code>'A'</code> standing representative for the lexicographically
 * smaller allele, and <code>'C'</code> for the bigger one.
 */
public class GenotypicGenotypeEncoder extends EncodingTableBasedGenotypeEncoder {

	public static final GenotypicGenotypeEncoder SINGLETON = new GenotypicGenotypeEncoder();

//	private static final List<List<Double>> ENCODED_VALUES;
//	static {
//		ENCODED_VALUES = new ArrayList<List<Double>>(4);
//
//		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
//				Arrays.asList(1.0, 0.0, 0.0)))); // "AA"
//		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
//				Arrays.asList(0.0, 1.0, 0.0)))); // "AT"
//		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
//				Arrays.asList(0.0, 1.0, 0.0)))); // "TA"
//		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
//				Arrays.asList(0.0, 0.0, 1.0)))); // "TT"
//	}
	private static final Map<Integer, List<Float>> ENCODED_VALUES;
	static {
		ENCODED_VALUES = new HashMap<Integer, List<Float>>(5);

		ENCODED_VALUES.put(0, Collections.unmodifiableList(new ArrayList<Float>(
				Arrays.asList(0.0f, 0.0f, 0.0f)))); // "00"
		ENCODED_VALUES.put(4, Collections.unmodifiableList(new ArrayList<Float>(
				Arrays.asList(1.0f, 0.0f, 0.0f)))); // "AA"
		ENCODED_VALUES.put(5, Collections.unmodifiableList(new ArrayList<Float>(
				Arrays.asList(0.0f, 1.0f, 0.0f)))); // "AT"
		ENCODED_VALUES.put(7, Collections.unmodifiableList(new ArrayList<Float>(
				Arrays.asList(0.0f, 1.0f, 0.0f)))); // "TA"
		ENCODED_VALUES.put(8, Collections.unmodifiableList(new ArrayList<Float>(
				Arrays.asList(0.0f, 0.0f, 1.0f)))); // "TT"
	}

	private GenotypicGenotypeEncoder() {
	}

	@Override
	public Map<Integer, List<Float>> generateEncodingTable(
			List<byte[]> possibleGenotypes,
			Collection<byte[]> rawGenotypes)
	{
		Map<Integer, List<Float>> encodingTable
				= new HashMap<Integer, List<Float>>(possibleGenotypes.size());

//		SortedSet<Genotype> sortedGenotypes = new TreeSet<Genotype>(possibleGenotypes);
//
//		Iterator<List<Double>> encodedValues = ENCODED_VALUES.iterator();
//		for (Genotype genotype : sortedGenotypes) {
//			encodingTable.put(genotype, encodedValues.next());
//		}
		Map<Integer, Integer> baseEncodingTable
				= generateBaseEncodingTable(possibleGenotypes);
		for (Map.Entry<Integer, Integer> baseEncoding : baseEncodingTable.entrySet()) {
//System.out.println("XXX " + baseEncoding.getKey() + " -> " + baseEncoding.getValue());
			encodingTable.put(baseEncoding.getKey(), ENCODED_VALUES.get(baseEncoding.getValue()));
		}

		return encodingTable;
	}

	@Override
	public int getEncodingFactor() {
		return 3;
	}
}

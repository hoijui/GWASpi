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
import org.gwaspi.constants.cNetCDF.Defaults.AlleleByte;
import org.gwaspi.model.Genotype;
import org.gwaspi.operations.qamarkers.QAMarkersOperationEntry.GenotypeCounts;

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
//	private static final Map<Integer, List<Float>> ENCODED_VALUES_MAP;
	private static final List<List<Float>> ENCODED_VALUES;
	static {
//		ENCODED_VALUES_MAP = new LinkedHashMap<Integer, List<Float>>(5);
//
//		ENCODED_VALUES_MAP.put(
//				GenotypeCounts._00.ordinal(),
//				makeHard(Arrays.asList(0.0f, 0.0f, 0.0f)));
//		ENCODED_VALUES_MAP.put(
//				GenotypeCounts._AA.ordinal(),
//				makeHard(Arrays.asList(1.0f, 0.0f, 0.0f)));
//		ENCODED_VALUES_MAP.put(
//				GenotypeCounts._Aa.ordinal(),
//				makeHard(Arrays.asList(0.0f, 1.0f, 0.0f)));
//		ENCODED_VALUES_MAP.put(
//				GenotypeCounts._aA.ordinal(),
//				makeHard(Arrays.asList(0.0f, 1.0f, 0.0f)));
//		ENCODED_VALUES_MAP.put(
//				GenotypeCounts._aa.ordinal(),
//				makeHard(Arrays.asList(0.0f, 0.0f, 1.0f)));

		ENCODED_VALUES = new ArrayList<List<Float>>(5);

		ENCODED_VALUES.add(makeHard(Arrays.asList(1.0f, 0.0f, 0.0f))); // "AA"
		ENCODED_VALUES.add(makeHard(Arrays.asList(0.0f, 1.0f, 0.0f))); // "Aa"
		ENCODED_VALUES.add(makeHard(Arrays.asList(0.0f, 1.0f, 0.0f))); // "aA"
		ENCODED_VALUES.add(makeHard(Arrays.asList(0.0f, 0.0f, 1.0f))); // "aa"
		ENCODED_VALUES.add(makeHard(Arrays.asList(0.0f, 0.0f, 0.0f))); // "--"
	}

	private GenotypicGenotypeEncoder() {
	}

//	private Map<Integer, List<Float>> getGenotypeOrdinalToEncodedLookupTable() {
//		return ENCODED_VALUES_MAP;
//	}

	@Override
	protected List<List<Float>> getEncodedValuesLists() {
		return ENCODED_VALUES;
	}

	@Override
	public int getEncodingFactor() {
		return 3;
//		return getEncodedValuesLists().get(0).size();
	}
}

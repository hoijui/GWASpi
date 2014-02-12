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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.gwaspi.model.Genotype;

/**
 * Uses this encoding scheme:<br/>
 * {'A', 'A'} -> {0.0}<br/>
 * {'A', 'C'} -> {1.0}<br/>
 * {'C', 'A'} -> {1.0}<br/>
 * {'C', 'C'} -> {2.0}<br/>
 * With <code>'A'</code> standing representative for the lexicographically
 * smaller allele, and <code>'C'</code> for the bigger one.
 */
public class NominalGenotypeEncoder extends EncodingTableBasedGenotypeEncoder {

	public static final NominalGenotypeEncoder SINGLETON = new NominalGenotypeEncoder();
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

		ENCODED_VALUES.add(makeHard(Arrays.asList(2.0f))); // "AA"
		ENCODED_VALUES.add(makeHard(Arrays.asList(1.0f))); // "Aa"
		ENCODED_VALUES.add(makeHard(Arrays.asList(1.0f))); // "aA"
		ENCODED_VALUES.add(makeHard(Arrays.asList(0.0f))); // "aa"
		ENCODED_VALUES.add(makeHard(Arrays.asList(0.0f))); // "--"
	}

	private NominalGenotypeEncoder() {
	}

	@Override
	protected List<List<Float>> getEncodedValuesLists() {
		return ENCODED_VALUES;
	}

//	@Override
//	public Map<Integer, List<Float>> generateEncodingTable(
//			Set<byte[]> possibleGenotypes,
//			Collection<byte[]> rawGenotypes)
//	{
//		Map<Integer, List<Float>> encodingTable
//				= new HashMap<Integer, List<Float>>(possibleGenotypes.size());
//
//		Map<Integer, Integer> baseEncodingTable
//				= generateBaseEncodingTable(possibleGenotypes);
//
//		// extractthe possible GTs
//		SortedSet<Byte> possibleGTs = new TreeSet<Byte>();
//		for (byte[] possibleGenotype : possibleGenotypes) {
//			possibleGTs.add(Genotype.getFather(possibleGenotype));
//			possibleGTs.add(Genotype.getMother(possibleGenotype));
//		}
//
//		// count GTs
//		// FIXME This value can most likely be taken from the census or HW operation or something else that was already executed
//		// NOTE we use LHM, so the order of insertion stays the same
//		Map<Byte, Integer> gtValueOccurence = new LinkedHashMap<Byte, Integer>();
//		// init
//		for (Byte possibleGT : possibleGTs) {
//			gtValueOccurence.put(possibleGT, 0);
//		}
//		// count
//		for (byte[] rawGenotype : rawGenotypes) {
//			gtValueOccurence.put(Genotype.getFather(rawGenotype), gtValueOccurence.get(Genotype.getFather(rawGenotype)) + 1);
//			gtValueOccurence.put(Genotype.getMother(rawGenotype), gtValueOccurence.get(Genotype.getMother(rawGenotype)) + 1);
//		}
//		gtValueOccurence.remove((byte) '0');
//		List<Integer> occurences = new ArrayList<Integer>(gtValueOccurence.values());
//		// always count the GTs with lower occurences
//		// this leads to an overall smaller mean value
//		final boolean countHigher = (occurences.get(0) > occurences.get(1));
////		System.err.println(String.format("occurences: %d vs %d", occurences.get(0), occurences.get(1)));
////		System.err.println(String.format("countHigher: %b", countHigher));
//
//
////		Iterator<Genotype> baseEncodingKeyIterator = baseEncodingTable.keySet().iterator();
////		Genotype firstNonZeroBaseGt = baseEncodingKeyIterator.next();
////		if (firstNonZeroBaseGt.getFather() == '0') {
////			firstNonZeroBaseGt = baseEncodingKeyIterator.next();
////		}
////
////		Genotype lastNonZeroGt = rawGenotypes.get(0);
////		for (Genotype rawGenotype : rawGenotypes) {
////			Genotype rawGenotype = rawGenotypes.get(rgi);
////			if (rawGenotype.getFather() != '0' && rawGenotype.getMother() != '0') {
////				lastNonZeroGt = rawGenotype;
////				break;
////			}
////		}
////
////		Genotype lastNonZeroGt = rawGenotypes.get(0);
//////		for (Genotype rawGenotype : rawGenotypes) {
////		for (int rgi = rawGenotypes.size() - 1; rgi >= 0; rgi--) {
////			Genotype rawGenotype = rawGenotypes.get(rgi);
////			if (rawGenotype.getFather() != '0' && rawGenotype.getMother() != '0') {
////				lastNonZeroGt = rawGenotype;
////				break;
////			}
////		}
////		// true if the alphabetically lower letter appears first in the gt samples,
////		// eg. the first value is "AA" or "AT", false is "TA" or "TT"
////		Iterator<Genotype> baseEncodingKeyIterator = baseEncodingTable.keySet().iterator();
////		Genotype firstNonZeroBaseGt = baseEncodingKeyIterator.next();
////		if (firstNonZeroBaseGt.getFather() == '0') {
////			firstNonZeroBaseGt = baseEncodingKeyIterator.next();
////		}
//////		final byte lowestCharFirst = (byte) Math.min(firstNonZeroGt.getFather(), firstNonZeroGt.getMother());
////		final byte charLast = lastNonZeroGt.getMother();
////		final byte lowestCharFirstBase = (byte) Math.min(firstNonZeroBaseGt.getFather(), firstNonZeroBaseGt.getMother());
//////		final boolean isLowerFirst = (firstNonZeroGt.getFather() == firstNonZeroBaseGt.getFather());
//////		final boolean isLowerFirst = (firstNonZeroGt.getMother() == firstNonZeroBaseGt.getMother());
////		// true if the alphabetically lower letter appears in the first non-"00" GT.
////		// eg. the first value is "AA", "AT" or "TA", false if it is "TT"
////		final boolean lowerInFirst = (charLast == lowestCharFirstBase);
//
//		for (Map.Entry<Integer, Integer> baseEncoding : baseEncodingTable.entrySet()) {
//			float curValue;
////System.out.println("XXX " + baseEncoding.getKey() + " -> " + baseEncoding.getValue());
//			switch (baseEncoding.getValue()) {
//				case 4: // "AA"
////					curValue = 0.0f;
//					if (countHigher) {
//						curValue = 0.0f;
//					} else {
//						curValue = 2.0f;
//					}
//					break;
//				case 5: // "AG"
//					curValue = 1.0f;
//					break;
//				case 7: // "GA"
//					curValue = 1.0f;
//					break;
//				case 8: // "GG"
////					curValue = 2.0f;
//					if (countHigher) {
//						curValue = 2.0f;
//					} else {
//						curValue = 0.0f;
//					}
//					break;
//				default: // "00"
//					curValue = 0.0f;
//					break;
//			}
//			encodingTable.put(baseEncoding.getKey(), Collections.singletonList(curValue));
//		}
//
//		return encodingTable;
//	}

	@Override
	public int getEncodingFactor() {
		return 1;
	}
}

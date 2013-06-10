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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	@Override
	public Map<Genotype, List<Double>> generateEncodingTable(
			List<Genotype> possibleGenotypes,
			List<Genotype> rawGenotypes)
	{
		Map<Genotype, List<Double>> encodingTable
				= new HashMap<Genotype, List<Double>>(possibleGenotypes.size());

		Map<Genotype, Integer> baseEncodingTable
				= generateBaseEncodingTable(possibleGenotypes);

		Genotype lastNonZeroGt = rawGenotypes.get(0);
//		for (Genotype rawGenotype : rawGenotypes) {
		for (int rgi = rawGenotypes.size() - 1; rgi >= 0; rgi--) {
			Genotype rawGenotype = rawGenotypes.get(rgi);
			if (rawGenotype.getFather() != '0' && rawGenotype.getMother() != '0') {
				lastNonZeroGt = rawGenotype;
				break;
			}
		}
		// true if the alphabetically lower letter appears first in the gt samples,
		// eg. the first value is "AA" or "AT", false is "TA" or "TT"
		Iterator<Genotype> baseEncodingKeyIterator = baseEncodingTable.keySet().iterator();
		Genotype firstNonZeroBaseGt = baseEncodingKeyIterator.next();
		if (firstNonZeroBaseGt.getFather() == '0') {
			firstNonZeroBaseGt = baseEncodingKeyIterator.next();
		}
//		final byte lowestCharFirst = (byte) Math.min(firstNonZeroGt.getFather(), firstNonZeroGt.getMother());
		final byte charLast = lastNonZeroGt.getMother();
		final byte lowestCharFirstBase = (byte) Math.min(firstNonZeroBaseGt.getFather(), firstNonZeroBaseGt.getMother());
//		final boolean isLowerFirst = (firstNonZeroGt.getFather() == firstNonZeroBaseGt.getFather());
//		final boolean isLowerFirst = (firstNonZeroGt.getMother() == firstNonZeroBaseGt.getMother());
		// true if the alphabetically lower letter appears in the first non-"00" GT.
		// eg. the first value is "AA", "AT" or "TA", false if it is "TT"
		final boolean lowerInFirst = (charLast == lowestCharFirstBase);

		for (Map.Entry<Genotype, Integer> baseEncoding : baseEncodingTable.entrySet()) {
			double curValue;
//System.out.println("XXX " + baseEncoding.getKey() + " -> " + baseEncoding.getValue());
			switch (baseEncoding.getValue()) {
				case 4: // "AA"
//					curValue = 0.0;
					if (lowerInFirst)
						curValue = 0.0;
					else
						curValue = 2.0;
					break;
				case 5: // "AG"
					curValue = 1.0;
					break;
				case 7: // "GA"
					curValue = 1.0;
					break;
				case 8: // "GG"
//					curValue = 2.0;
					if (lowerInFirst)
						curValue = 2.0;
					else
						curValue = 0.0;
					break;
				default: // "00"
					curValue = 0.0;
					break;
			}
			encodingTable.put(baseEncoding.getKey(), Collections.singletonList(curValue));
		}

		return encodingTable;
	}

	@Override
	public int getEncodingFactor() {
		return 1;
	}
}

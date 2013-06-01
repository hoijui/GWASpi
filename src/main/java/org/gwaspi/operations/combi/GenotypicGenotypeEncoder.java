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
import java.util.SortedSet;
import java.util.TreeSet;
import org.gwaspi.model.Genotype;

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

	private static final List<List<Double>> ENCODED_VALUES;
	static {
		ENCODED_VALUES = new ArrayList<List<Double>>(4);

		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(1.0, 0.0, 0.0)))); // {'A', 'A'}
		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(0.0, 1.0, 0.0)))); // {'A', 'C'}
		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(0.0, 1.0, 0.0)))); // {'C', 'A'}
		ENCODED_VALUES.add(Collections.unmodifiableList(new ArrayList<Double>(
				Arrays.asList(0.0, 0.0, 1.0)))); // {'C', 'C'}
	}

	@Override
	public Map<Genotype, List<Double>> generateEncodingTable(
			List<Genotype> possibleGenotypes)
	{
		Map<Genotype, List<Double>> encodingTable
				= new HashMap<Genotype, List<Double>>(possibleGenotypes.size());

		SortedSet<Genotype> sortedGenotypes = new TreeSet<Genotype>(possibleGenotypes);

		Iterator<List<Double>> encodedValues = ENCODED_VALUES.iterator();
		for (Genotype genotype : sortedGenotypes) {
			encodingTable.put(genotype, encodedValues.next());
		}

		return encodingTable;
	}

	@Override
	public int getEncodingFactor() {
		return 3;
	}
}

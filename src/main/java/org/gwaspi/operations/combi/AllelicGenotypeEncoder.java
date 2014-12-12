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
import java.util.List;

/**
 * Uses this encoding scheme:<br/>
 * {'A', 'A'} -> {0.0, 1.0,   0.0, 1.0}<br/>
 * {'A', 'C'} -> {0.0, 1.0,   1.0, 0.0}<br/>
 * {'C', 'A'} -> {1.0, 0.0,   0.0, 1.0}<br/>
 * {'C', 'C'} -> {1.0, 0.0,   1.0, 0.0}<br/>
 * With <code>'A'</code> being the lexicographically smaller allele,
 * and <code>'C'</code> the bigger one.
 */
public class AllelicGenotypeEncoder extends EncodingTableBasedGenotypeEncoder {

	public static final AllelicGenotypeEncoder SINGLETON = new AllelicGenotypeEncoder();

	private AllelicGenotypeEncoder() {
	}

	@Override
	protected boolean isUsingLexicographicEncodingOrder() {
		return true;
	}

	@Override
	protected List<List<Float>> createEncodedValuesLists() {

		final List<List<Float>> encodedValues = new ArrayList<List<Float>>(5);

		encodedValues.add(makeUnmodifiable(Arrays.asList(1.0f, 0.0f, 1.0f, 0.0f))); // "AA"
		encodedValues.add(makeUnmodifiable(Arrays.asList(1.0f, 0.0f, 0.0f, 1.0f))); // "Aa"
		encodedValues.add(makeUnmodifiable(Arrays.asList(0.0f, 1.0f, 1.0f, 0.0f))); // "aA"
		encodedValues.add(makeUnmodifiable(Arrays.asList(0.0f, 1.0f, 0.0f, 1.0f))); // "aa"
		encodedValues.add(makeUnmodifiable(Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f))); // "--"

		return encodedValues;
	}

	@Override
	public int getEncodingFactor() {
		return 4;
	}
}

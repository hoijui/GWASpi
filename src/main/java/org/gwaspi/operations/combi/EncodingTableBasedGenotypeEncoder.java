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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Genotype;

public abstract class EncodingTableBasedGenotypeEncoder implements GenotypeEncoder {

	public abstract Map<Genotype, List<Double>> generateEncodingTable(
			List<Genotype> possibleGenotypes);

	@Override
	public void encodeGenotypes(
			List<Genotype> possibleGenotypes,
			List<Genotype> rawGenotypes,
			Map<?, List<Double>> encodedGenotypes)
	{
		// create the encoding table
		Map<Genotype, List<Double>> encodingTable
				= generateEncodingTable(possibleGenotypes);

		// encode
		Collection<List<Double>> encodedGTValues = encodedGenotypes.values();
		Iterator<List<Double>> encodedIt = encodedGTValues.iterator();
		for (Genotype genotype : rawGenotypes) {
			List<Double> encodedValues = encodedIt.next();
			encodedValues.addAll(encodingTable.get(genotype));
		}
	}

	@Override
	public void decodeWeights(List<Double> encodedWeights,
			List<Double> decodedWeights)
	{
		final int encodingFactor = getEncodingFactor();
		for (int ewi = 0; ewi < encodedWeights.size(); ewi += encodingFactor) {
			double sum = 0.0;
			for (int lwi = 0; lwi < encodingFactor; lwi++) {
				sum += encodedWeights.get(ewi + lwi);
			}
			decodedWeights.add(sum / encodingFactor);
		}
	}
}

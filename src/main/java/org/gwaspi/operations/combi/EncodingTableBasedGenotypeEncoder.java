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

	public abstract Map<Genotype, List<Double>> generateEncodingTable( use linked hash map
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
		System.err.println("XXX encodingTable: " + encodingTable.size() + " * " + encodingTable.values().iterator().next().size());
		for (Map.Entry<Genotype, List<Double>> encodingTableEntry : encodingTable.entrySet()) {
			System.err.print("\t" + encodingTableEntry.getKey() + " ->");
			for (Double value : encodingTableEntry.getValue()) {
				System.err.print(" " + value);
			}
			System.err.println();
		}

		// encode
		Collection<List<Double>> encodedGTValues = encodedGenotypes.values();
		Iterator<List<Double>> encodedIt = encodedGTValues.iterator();
		for (Genotype genotype : rawGenotypes) {
			List<Double> encodedValues = encodedIt.next();
			encodedValues.addAll(encodingTable.get(genotype));
		}
	}

	private double norm2(List<Double> numbers) {

		double norm = 0.0;
		for (Double number : numbers) {
			norm += number * number;
		}
		norm = Math.sqrt(norm);

		return norm;
	}

	@Override
	public void decodeWeights(List<Double> encodedWeights,
			List<Double> decodedWeights)
	{
		final double norm = norm2(encodedWeights);
		final int encodingFactor = getEncodingFactor();
		for (int ewi = 0; ewi < encodedWeights.size(); ewi += encodingFactor) {
			double sum = 0.0;
			for (int lwi = 0; lwi < encodingFactor; lwi++) {
				final double wEncNormalized = Math.abs(encodedWeights.get(ewi + lwi)) / norm;
				sum += wEncNormalized * wEncNormalized; // NOTE change this for a p-norm with p != 2
			}
//			final double wDecNormalized = sum / encodingFactor;
			final double wDecNormalized = Math.sqrt(sum); // NOTE change this for a p-norm with p != 2
			decodedWeights.add(wDecNormalized);
		}

//		final int encodingFactor = getEncodingFactor();
//		for (int ewi = 0; ewi < encodedWeights.size(); ewi += encodingFactor) {
//			double sum = 0.0;
//			for (int lwi = 0; lwi < encodingFactor; lwi++) {
//				sum += encodedWeights.get(ewi + lwi);
//			}
//			decodedWeights.add(sum / encodingFactor);
//		}
	}
}

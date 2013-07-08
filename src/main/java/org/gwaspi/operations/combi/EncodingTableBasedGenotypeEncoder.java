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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import libsvm.svm_node;
import libsvm.svm_problem;
import org.gwaspi.model.Genotype;

public abstract class EncodingTableBasedGenotypeEncoder implements GenotypeEncoder {

	public abstract Map<Integer, List<Float>> generateEncodingTable(
			List<byte[]> possibleGenotypes,
			Collection<byte[]> rawGenotypes);

	/**
	 *
	 * @param possibleGenotypes
	 * @return will e.g. contain (a sub-set of):
	 *   {"00".hash():0, "0A".hash():1, "0G".hash():2, "A0".hash():3, "AA".hash():4, "AG".hash():5, "G0".hash():6, "GA".hash():7, "GG".hash():8}
	 */
	protected static Map<Integer, Integer> generateBaseEncodingTable(
			List<byte[]> possibleGenotypes)
	{
		Map<Integer, Integer> baseEncodingTable
				= new LinkedHashMap<Integer, Integer>(possibleGenotypes.size());

		// this set will e.g. contain (a sub-set of): {'0', 'A', 'G'}
		Set<Byte> possibleValues = new TreeSet<Byte>();
		for (byte[] genotype : possibleGenotypes) {
			possibleValues.add(Genotype.getFather(genotype));
			possibleValues.add(Genotype.getMother(genotype));
		}

		// this map will e.g. contain (a sub-set of):
		// {'0':0, 'A':1, 'G':2}
		Map<Byte, Integer> possibleValuesSingleEncoding = new LinkedHashMap<Byte, Integer>(possibleValues.size());
		final int firstEncodedVal = possibleValues.contains(new Byte((byte) '0')) ? 0 : 1;
		int currentEncodedVal = firstEncodedVal;
		for (Byte possibleValue : possibleValues) {
			possibleValuesSingleEncoding.put(possibleValue, currentEncodedVal++);
		}

		// this map will e.g. contain (a sub-set of):
		// {"00":0, "0A":1, "0G":2, "A0":3, "AA":4, "AG":5, "G0":6, "GA":7, "GG":8}
		for (byte[] possibleGenotype : possibleGenotypes) {
			int value = possibleValuesSingleEncoding.get(Genotype.getFather(possibleGenotype));
			value = (value * 3) + possibleValuesSingleEncoding.get(Genotype.getMother(possibleGenotype));
			baseEncodingTable.put(Genotype.hashCode(possibleGenotype), value);
		}

		return baseEncodingTable;
	}

	@Override
//	public void encodeGenotypes(
//			List<Genotype> possibleGenotypes,
//			List<Genotype> rawGenotypes,
//			Map<?, List<Double>> encodedGenotypes)
//	public void encodeGenotypes(
//			final List<Genotype> possibleGenotypes,
//			final List<Genotype> rawGenotypes,
//			svm_problem libSvmProblem,
//			int mi)
//	public void encodeGenotypes(
//			final List<Genotype> possibleGenotypes,
//			final List<Genotype> rawGenotypes,
//			float[][] encodedSamplesMarkers,
//			int mi)
//	public void encodeGenotypes(
//			final List<Genotype> possibleGenotypes,
//			final List<Genotype> rawGenotypes,
//			float[][] encodedSamplesMarkers,
//			int mi)
	public void encodeGenotypes(
			final Collection<byte[]> rawGenotypes,
			final List<Boolean> samplesToKeep,
			float[][] encodedSamplesMarkers,
			int mi)
	{
		Map<Integer, byte[]> unique = new TreeMap<Integer, byte[]>();
		Iterator<Boolean> keep = samplesToKeep.iterator();
		for (byte[] genotype : rawGenotypes) {
			if (keep.next().booleanValue()) {
				unique.put(Genotype.hashCode(genotype), genotype);
			}
		}
		List<byte[]> possibleGenotypes = new ArrayList<byte[]>(unique.values());
//		Collections.sort(possibleGenotypes); // NOTE note required, because we use TreeMap

		// create the encoding table
		Map<Integer, List<Float>> encodingTable
				= generateEncodingTable(possibleGenotypes, rawGenotypes);
//		System.err.println("XXX encodingTable: " + encodingTable.size() + " * " + encodingTable.values().iterator().next().size());
//		for (Map.Entry<Genotype, List<Double>> encodingTableEntry : encodingTable.entrySet()) {
//			System.err.print("\t" + encodingTableEntry.getKey() + " ->");
//			for (Double value : encodingTableEntry.getValue()) {
//				System.err.print(" " + value);
//			}
//			System.err.println();
//		}

		if (possibleGenotypes.size() - (
				possibleGenotypes.contains(Genotype.INVALID.hashCode())
				? 1 : 0) == 1)
		{
			// only one valid GT type was found
			// for example, all SNPs have the value "AA"
			// -> write only 0.0 values under this SNP
//			final List<Double> nullEncoding = Collections.nCopies(getEncodingFactor(), 0.0);
//			Collection<List<Double>> encodedGTValues = encodedGenotypes.values();
//			Iterator<List<Double>> encodedIt = encodedGTValues.iterator();
//			for (Genotype genotype : rawGenotypes) {
//				List<Double> encodedValues = encodedIt.next();
//				encodedValues.addAll(nullEncoding);
//			}
//			Arrays.fill(libSvmProblem.x[markerIndex], 0.0);

//			for (int di = 0; di < libSvmProblem.x.length; di++) {
//				libSvmProblem.x[di][mi].value = 0.0;
//			}

			for (int di = 0; di < encodedSamplesMarkers.length; di++) {
				encodedSamplesMarkers[di][mi] = 0.0f;
			}
		} else {
			// encode
//			Collection<List<Double>> encodedGTValues = encodedGenotypes.values();
//			Iterator<List<Double>> encodedIt = encodedGTValues.iterator();
			int di = 0;
			/*Iterator<Boolean> */keep = samplesToKeep.iterator();
			for (byte[] genotype : rawGenotypes) {
				if (keep.next().booleanValue()) {
//					List<Double> encodedValues = encodedIt.next();
					List<Float> encodedGT = encodingTable.get(Genotype.hashCode(genotype));
//					encodedValues.addAll(encodedGT);
					for (Float encVal : encodedGT) {
//						libSvmProblem.x[di][mi].value = encVal;

						encodedSamplesMarkers[di][mi] = encVal.floatValue();
					}
				}
			}
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

	@Override
	public String getHumanReadableName() {
		return getClass().getSimpleName().replaceFirst(GenotypeEncoder.class.getSimpleName(), "");
	}

	@Override
	public String toString() {
		return getHumanReadableName();
	}
}

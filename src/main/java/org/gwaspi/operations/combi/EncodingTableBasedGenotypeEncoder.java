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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.gwaspi.model.Genotype;
import org.gwaspi.netCDF.operations.NetCdfUtils;

public abstract class EncodingTableBasedGenotypeEncoder implements GenotypeEncoder {

	public abstract Map<Integer, List<Float>> generateEncodingTable(
			Set<byte[]> possibleGenotypes,
			Collection<byte[]> rawGenotypes);

	/**
	 *
	 * @param possibleGenotypes
	 * @return will e.g. contain (a sub-set of):
	 *   {"00".hash():0, "0A".hash():1, "0G".hash():2, "A0".hash():3, "AA".hash():4, "AG".hash():5, "G0".hash():6, "GA".hash():7, "GG".hash():8}
	 */
	protected static Map<Integer, Integer> generateBaseEncodingTable(
			Set<byte[]> possibleGenotypes)
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
	public void encodeGenotypes(
			final List<byte[]> rawGenotypes,
			SamplesFeaturesStorage<Float> encodedSamplesFeatures,
			int markerIndex)
	{
		final Set<byte[]> possibleGenotypes
				= NetCdfUtils.extractUniqueGenotypesOrdered(rawGenotypes);
//		if (samplesToKeep == null) {
//			possibleGenotypes = NetCdfUtils.extractUniqueGenotypesOrdered(
//				rawGenotypes);
//		} else {
//			possibleGenotypes = NetCdfUtils.extractUniqueGenotypesOrdered(
//				rawGenotypes, samplesToKeep);
//		}

		final int numSamples = rawGenotypes.size();

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

		// In here, we store the features 1+ for the given marker,
		// in case the exist.
		// Each marker coresponds to encoding-factor many features.
		// We generate all of them, but store only the first one (index 0)
		// directly to the back-end storage, while buffering the later
		final int numHigherFeatures = getEncodingFactor() - 1;
		List<List<Float>> tempHigherFeaturesEncodedGTs = new ArrayList<List<Float>>(numHigherFeatures);
		for (int hfi = 0; hfi < numHigherFeatures; hfi++) {
			tempHigherFeaturesEncodedGTs.add(new ArrayList<Float>(numSamples));
		}

		if (possibleGenotypes.size() - (
				possibleGenotypes.contains(Genotype.INVALID.hashCode()) // FIXME
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

//			for (float[] encodedSamplesMarker : encodedSamplesMarkers) {
//				encodedSamplesMarker[mi] = 0.0f;
//			}
			for (int lfi = 0; lfi < getEncodingFactor(); lfi++) {
				final int fi = markerIndex + lfi;
				encodedSamplesFeatures.startStoringFeature(fi);
				for (int si = 0; si < encodedSamplesFeatures.getNumSamples(); si++) {
					encodedSamplesFeatures.setSampleValue(si, 0.0f);
				}
				encodedSamplesFeatures.endStoringFeature();
			}
		} else {
			// encode
			int fi = markerIndex * getEncodingFactor();
			encodedSamplesFeatures.startStoringFeature(fi);
			int si = 0;
//			if (samplesToKeep == null) {
				// include all samples
				for (byte[] genotype : rawGenotypes) {
					List<Float> encodedGT = encodingTable.get(Genotype.hashCode(genotype));
					encodedSamplesFeatures.setSampleValue(si++, encodedGT.get(0).floatValue());
						for (int hfi = 0; hfi < numHigherFeatures; hfi++) {
						tempHigherFeaturesEncodedGTs.get(hfi).add(encodedGT.get(1 + hfi));
					}
//					for (Float encVal : encodedGT) {
////						encodedSamplesMarkers[si++][mi] = encVal.floatValue();
//						encodedSamplesFeatures.setSampleValue(si++, encVal.floatValue());
//					}
				}
//			} else {
//				// include only samples in samplesToKeep
//				Iterator<Boolean> keep = samplesToKeep.iterator();
//				for (byte[] genotype : rawGenotypes) {
//					if (keep.next().booleanValue()) {
//						List<Float> encodedGT = encodingTable.get(Genotype.hashCode(genotype));
//						encodedSamplesFeatures.setSampleValue(si++, encodedGT.get(0).floatValue());
//						for (int hfi = 0; hfi < numHigherFeatures; hfi++) {
//							tempHigherFeaturesEncodedGTs.get(hfi).add(encodedGT.get(1 + hfi));
//						}
////						for (Float encVal : encodedGT) {
//////							encodedSamplesMarkers[si++][mi] = encVal.floatValue();
////							encodedSamplesFeatures.setSampleValue(si++, encVal.floatValue());
////						}
//					}
//				}
//			}
			encodedSamplesFeatures.endStoringFeature();

			for (int hfi = 0; hfi < numHigherFeatures; hfi++) {
				fi++;
				encodedSamplesFeatures.startStoringFeature(fi);
				final List<Float> featuresSampleValues = tempHigherFeaturesEncodedGTs.get(hfi);
				si = 0;
				for (Float sampleValue : featuresSampleValues) {
					encodedSamplesFeatures.setSampleValue(si++, sampleValue);
				}
				encodedSamplesFeatures.endStoringFeature();
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

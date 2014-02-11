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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.Genotype;
import org.gwaspi.operations.qamarkers.QAMarkersOperationEntry;

public abstract class EncodingTableBasedGenotypeEncoder implements GenotypeEncoder {

//	public abstract Map<Integer, List<Float>> generateEncodingTable(
////			Set<byte[]> possibleGenotypes,
////			Collection<byte[]> rawGenotypes);
//			final byte majorAllele,
//			final byte minorAllele,
//			final int[] genotypeCounts,
//			final int numMarkers);

	/**
	 * @return The encoded values for: "AA", "Aa", "aA", "aa", "--" (stands for everything else)
	 */
	protected abstract List<List<Float>> getEncodedValuesLists();


	protected static <V> List<V> makeHard(List<V> soft) {
		return Collections.unmodifiableList(new ArrayList<V>(soft));
	}
	private static List<List<Integer>> createGenotypesHashes(
			final byte majorAllele,
			final byte minorAllele)
	{
		final List<List<Integer>> genotypesHashes = new ArrayList<List<Integer>>(5);

		genotypesHashes.add(Arrays.asList(
				Genotype.hashCode(majorAllele, majorAllele),
				Genotype.hashCode(majorAllele, cNetCDF.Defaults.AlleleByte._0_VALUE),
				Genotype.hashCode(cNetCDF.Defaults.AlleleByte._0_VALUE, majorAllele)
		)); // AA
		genotypesHashes.add(Arrays.asList(
				Genotype.hashCode(majorAllele, minorAllele)
		)); // Aa
		genotypesHashes.add(Arrays.asList(
				Genotype.hashCode(minorAllele, majorAllele)
		)); // aA
		genotypesHashes.add(Arrays.asList(
				Genotype.hashCode(minorAllele, minorAllele),
				Genotype.hashCode(minorAllele, cNetCDF.Defaults.AlleleByte._0_VALUE),
				Genotype.hashCode(cNetCDF.Defaults.AlleleByte._0_VALUE, minorAllele)
		)); // aa
		genotypesHashes.add(null); // --
//		genotypeHashes.add(Genotype.hashCode(majorAllele, majorAllele)); // AA
//		genotypeHashes.add(Genotype.hashCode(majorAllele, minorAllele)); // Aa
//		genotypeHashes.add(Genotype.hashCode(minorAllele, majorAllele)); // aA
//		genotypeHashes.add(Genotype.hashCode(minorAllele, minorAllele)); // aa
//		genotypeHashes.add(Genotype.hashCode(AlleleByte._0_VALUE, AlleleByte._0_VALUE));

		return genotypesHashes;
	}

	private List<Integer> accumulateGenotypesCounts(int[] genotypeCounts) {

		final List<Integer> genotypeCountsAccumulated = new ArrayList<Integer>(5);

		genotypeCountsAccumulated.add(
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._AA.ordinal()]
				+ genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._A0.ordinal()]
				+ genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._0A.ordinal()]
		); // AA
		genotypeCountsAccumulated.add(
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._Aa.ordinal()]
		); // Aa
		genotypeCountsAccumulated.add(
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._aA.ordinal()]
		); // aA
		genotypeCountsAccumulated.add(
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._aa.ordinal()]
				+ genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._a0.ordinal()]
				+ genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._0a.ordinal()]
		); // aa
		genotypeCountsAccumulated.add(
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._00.ordinal()]
				+ genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._dash_dash.ordinal()]
		); // -- (everything else)

		return genotypeCountsAccumulated;
	}

//	private static int calculateTotalCount(int[] genotypeCounts) {
//
//		int sum = 0;
//
//		for (int genotypeCount : genotypeCounts) {
//			sum += genotypeCount;
//		}
//
//		return sum;
//	}

	private static List<List<Float>> createWhitenedValuesLists(List<List<Float>> valuesLists, List<Integer> genotypesCountsAccumulated, final int numSamples) {

		// calculate genotype weights
		List<Double> weights = new ArrayList<Double>(genotypesCountsAccumulated.size());
		for (Integer genotypeCountsAccumulated : genotypesCountsAccumulated) {
			weights.add((double) genotypeCountsAccumulated / numSamples);
		}

		// calculate weighted sum (== values centers)
		// We do it with double, to possible get some higher accuracy.
		double[] valueCenters = new double[valuesLists.get(0).size()];
//		Arrays.fill(summedValues, 0.0f);
		Iterator<Double> weightsIt = weights.iterator();
		for (List<Float> valuesList : valuesLists) {
			final double weight = weightsIt.next();
			for (int vi = 0; vi < valueCenters.length; vi++) {
				valueCenters[vi] += valuesList.get(vi) * weight;
			}
		}

		// calculate the weighted variance sums
		double[] varianceSums = new double[valueCenters.length];
		weightsIt = weights.iterator();
		for (List<Float> valuesList : valuesLists) {
			final double weight = weightsIt.next();
			for (int vi = 0; vi < valueCenters.length; vi++) {
				final double centeredValue = valuesList.get(vi) - valueCenters[vi];
				varianceSums[vi] += centeredValue * centeredValue * weight;
			}
		}

		// whiten (center & set variance to 1.0)
		final List<List<Float>> whitenedValuesLists = new ArrayList<List<Float>>(valuesLists.size());
		for (List<Float> valuesList : valuesLists) {
			final List<Float> whitenedValuesList = new ArrayList<Float>(valueCenters.length);
			for (int vi = 0; vi < valueCenters.length; vi++) {
				final double centeredValue = valuesList.get(vi) - valueCenters[vi];
				final double whitenedValue = centeredValue / varianceSums[vi];
				whitenedValuesList.add((float) whitenedValue);
			}
			whitenedValuesLists.add(whitenedValuesList);
		}

		return whitenedValuesLists;
	}

	private Map<Integer, List<Float>> stitchTogether(List<List<Integer>> genotypesHashes, List<List<Float>> valuesLists, List<Integer> genotypesCountsAccumulated) {

		Map<Integer, List<Float>> encodingTable = new HashMap<Integer, List<Float>>(5);

		Iterator<List<Float>> valuesListsIt = valuesLists.iterator();
		Iterator<Integer> genotypesCountsAccumulatedIt = genotypesCountsAccumulated.iterator();
		for (List<Integer> genotypeHashes : genotypesHashes) {
			List<Float> valuesList = valuesListsIt.next();
			Integer genotypesCount = genotypesCountsAccumulatedIt.next();
			if (genotypesCount > 0) {
				for (Integer genotypeHashe : genotypeHashes) {
					encodingTable.put(genotypeHashe, valuesList);
				}
			}
		}

		return encodingTable;
	}

//	@Override
	private Map<Integer, List<Float>> generateEncodingTable(
//			Set<byte[]> possibleGenotypes,
//			Collection<byte[]> rawGenotypes)
			final byte majorAllele,
			final byte minorAllele,
			final int[] genotypeCounts,
			final int numSamples)
	{
//		Map<Integer, List<Float>> encodingTable
//				= new HashMap<Integer, List<Float>>(possibleGenotypes.size());
//
////		SortedSet<Genotype> sortedGenotypes = new TreeSet<Genotype>(possibleGenotypes);
////
////		Iterator<List<Double>> encodedValues = ENCODED_VALUES.iterator();
////		for (Genotype genotype : sortedGenotypes) {
////			encodingTable.put(genotype, encodedValues.next());
////		}
//		Map<Integer, Integer> baseEncodingTable
//				= generateBaseEncodingTable(possibleGenotypes);
//		for (Map.Entry<Integer, Integer> baseEncoding : baseEncodingTable.entrySet()) {
////System.out.println("XXX " + baseEncoding.getKey() + " -> " + baseEncoding.getValue());
//			encodingTable.put(baseEncoding.getKey(), ENCODED_VALUES.get(baseEncoding.getValue()));
//		}
//
//		return encodingTable;


//		Map<Integer, List<Float>> genotypeOrdinalToEncodedLookupTable = getGenotypeOrdinalToEncodedLookupTable();
		List<List<Integer>> genotypesHashes = createGenotypesHashes(majorAllele, minorAllele);
		List<Integer> genotypesCountsAccumulated = accumulateGenotypesCounts(genotypeCounts);
		List<List<Float>> encodedValuesLists = getEncodedValuesLists();

		List<List<Float>> encodedAndWhitenedValuesLists = createWhitenedValuesLists(encodedValuesLists, genotypesCountsAccumulated, numSamples);

		Map<Integer, List<Float>> encodingTable = stitchTogether(genotypesHashes, encodedAndWhitenedValuesLists, genotypesCountsAccumulated);

		return encodingTable;
	}

	/**
	 * @param possibleGenotypes
	 * @return will e.g. contain (a sub-set of):
	 *   {"00".hash():0, "0A".hash():1, "0G".hash():2, "A0".hash():3, "AA".hash():4, "AG".hash():5, "G0".hash():6, "GA".hash():7, "GG".hash():8}
	 */
	private static Map<Integer, Integer> generateBaseEncodingTable(
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
			final byte majorAllele,
			final byte minorAllele,
			final int[] genotypeCounts,
			SamplesFeaturesStorage<Float> encodedSamplesFeatures,
			int markerIndex)
	{
//		final Set<byte[]> possibleGenotypes
//				= NetCdfUtils.extractUniqueGenotypesOrdered(rawGenotypes);
//		if (samplesToKeep == null) {
//			possibleGenotypes = NetCdfUtils.extractUniqueGenotypesOrdered(
//				rawGenotypes);
//		} else {
//			possibleGenotypes = NetCdfUtils.extractUniqueGenotypesOrdered(
//				rawGenotypes, samplesToKeep);
//		}

		final int numSamples = rawGenotypes.size();

		// create the encoding table
		final Map<Integer, List<Float>> encodingTable
//				= generateEncodingTable(possibleGenotypes, rawGenotypes);
				= generateEncodingTable(majorAllele, majorAllele, genotypeCounts, numSamples);
//		System.err.println("XXX encodingTable: " + encodingTable.size() + " * " + encodingTable.values().iterator().next().size());
//		for (Map.Entry<Genotype, List<Double>> encodingTableEntry : encodingTable.entrySet()) {
//			System.err.print("\t" + encodingTableEntry.getKey() + " ->");
//			for (Double value : encodingTableEntry.getValue()) {
//				System.err.print(" " + value);
//			}
//			System.err.println();
//		}
		// this is the -- value
		List<Float> invalidEncoded = encodingTable.remove(null);

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

		if (encodingTable.size() - (
				encodingTable.keySet().contains(Genotype.INVALID.hashCode())
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
			// just set everythign to 0.0f, as this marker contains no meaningful information
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
					if (encodedGT == null) {
						encodedGT = invalidEncoded;
					}
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

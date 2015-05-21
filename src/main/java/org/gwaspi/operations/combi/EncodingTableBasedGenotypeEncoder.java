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
import org.gwaspi.constants.NetCDFConstants.Defaults.AlleleByte;
import org.gwaspi.model.Genotype;
import org.gwaspi.operations.qamarkers.QAMarkersOperationEntry;

public abstract class EncodingTableBasedGenotypeEncoder implements GenotypeEncoder {

	private List<List<Float>> encodedValues;
	private List<List<Float>> encodedValuesSwapped;

	protected EncodingTableBasedGenotypeEncoder() {

		this.encodedValues = null;
		this.encodedValuesSwapped = null;
	}

	/**
	 * @return The encoded values for: "AA", "Aa", "aA", "aa", "--" (stands for everything else)
	 */
	protected abstract List<List<Float>> createEncodedValuesLists();

	/**
	 * @return true, if the the allele -> feature encoding order
	 *   ({AA, Aa, aA, aa} vs {aa, aA, Aa, AA}) depends on the
	 *   the frequency (major-, minor-allele),
	 *   instead of on the frequency (major-allele before minor-allele)
	 */
	protected abstract boolean isUsingLexicographicEncodingOrder();

	/**
	 * @param swapped
	 * @return The encoded values for: "AA", "Aa", "aA", "aa", "--" (stands for everything else)
	 */
	protected final List<List<Float>> getEncodedValuesLists(final boolean swapped) {

		if (encodedValues == null) {
			encodedValues = Collections.unmodifiableList(createEncodedValuesLists());
			encodedValuesSwapped = Collections.unmodifiableList(swapMajorMinor(encodedValues));
		}

		if (swapped) {
			return encodedValuesSwapped;
		} else {
			return encodedValues;
		}
	}


	protected static <V> List<V> makeUnmodifiable(List<V> soft) {
		return Collections.unmodifiableList(new ArrayList<V>(soft));
	}
	private static List<List<Integer>> createGenotypesHashes(
			final byte majorAllele,
			final byte minorAllele)
	{
		final List<List<Integer>> genotypesHashes = new ArrayList<List<Integer>>(5);

		genotypesHashes.add(Arrays.asList(
				Genotype.hashCode(majorAllele, majorAllele),
				Genotype.hashCode(majorAllele, AlleleByte._0_VALUE),
				Genotype.hashCode(AlleleByte._0_VALUE, majorAllele)
		)); // AA
		genotypesHashes.add(Arrays.asList(
				Genotype.hashCode(majorAllele, minorAllele)
		)); // Aa
		genotypesHashes.add(Arrays.asList(
				Genotype.hashCode(minorAllele, majorAllele)
		)); // aA
		genotypesHashes.add(Arrays.asList(
				Genotype.hashCode(minorAllele, minorAllele),
				Genotype.hashCode(minorAllele, AlleleByte._0_VALUE),
				Genotype.hashCode(AlleleByte._0_VALUE, minorAllele)
		)); // aa
		genotypesHashes.add(null); // -- (everything else)

		return genotypesHashes;
	}

	private static List<List<Integer>> createPerHashGenotypesCounts(
			final int[] genotypeCounts)
	{
		final List<List<Integer>> perHashGenotypesCounts = new ArrayList<List<Integer>>(5);

		perHashGenotypesCounts.add(Arrays.asList(
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._AA.ordinal()],
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._A0.ordinal()],
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._0A.ordinal()]
		)); // AA
		perHashGenotypesCounts.add(Arrays.asList(
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._Aa.ordinal()]
		)); // Aa
		perHashGenotypesCounts.add(Arrays.asList(
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._aA.ordinal()]
		)); // aA
		perHashGenotypesCounts.add(Arrays.asList(
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._aa.ordinal()],
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._a0.ordinal()],
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._0a.ordinal()]
		)); // aa
		perHashGenotypesCounts.add(Arrays.asList(
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._00.ordinal()],
				genotypeCounts[QAMarkersOperationEntry.GenotypeCounts._dash_dash.ordinal()]
		)); // -- (everything else)

		return perHashGenotypesCounts;
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

	private static List<List<Float>> createWhitenedValuesLists(List<List<Float>> valuesLists, List<Integer> genotypesCountsAccumulated, final int numSamples, final int numFeatures, final double pStdDev) {

		// calculate genotype weights
		List<Double> weights = new ArrayList<Double>(genotypesCountsAccumulated.size());
		for (Integer genotypeCountsAccumulated : genotypesCountsAccumulated) {
			weights.add((double) genotypeCountsAccumulated / numSamples);
		}

		// calculate weighted sum (== values centers)
		// We do it with double, to possibly get some higher accuracy.
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
		double[] varianceMeans = new double[valueCenters.length];
		weightsIt = weights.iterator();
		for (List<Float> valuesList : valuesLists) {
			final double weight = weightsIt.next();
			for (int vi = 0; vi < valueCenters.length; vi++) {
				final double centeredValue = valuesList.get(vi) - valueCenters[vi];
				varianceMeans[vi] += Math.pow(centeredValue, pStdDev) * weight;
			}
		}
		final double pStdDevInverse = 1.0 / pStdDev;
		double[] stdDevs = new double[varianceMeans.length];
		for (int vi = 0; vi < varianceMeans.length; vi++) {
//			final double weight = weightsIt.next();
			stdDevs[vi] = Math.pow(varianceMeans[vi] * numFeatures, pStdDevInverse);
		}

		// whiten (center & set variance to 1.0)
		final List<List<Float>> whitenedValuesLists = new ArrayList<List<Float>>(valuesLists.size());
		for (List<Float> valuesList : valuesLists) {
			final List<Float> whitenedValuesList = new ArrayList<Float>(valueCenters.length);
			for (int vi = 0; vi < valueCenters.length; vi++) {
				final double centeredValue = valuesList.get(vi) - valueCenters[vi];
				final double whitenedValue;
				if (stdDevs[vi] == 0.0) {
					whitenedValue = centeredValue;
				} else {
					whitenedValue = centeredValue / stdDevs[vi];
				}
				whitenedValuesList.add((float) whitenedValue);
			}
			whitenedValuesLists.add(whitenedValuesList);
		}

		return whitenedValuesLists;
	}

	private Map<Integer, List<Float>> stitchTogether(
			final List<List<Integer>> genotypesHashes,
			final List<List<Float>> valuesLists,
//			final List<Integer> genotypesCountsAccumulated)
			final List<List<Integer>> perHashGenotypesCounts)
	{
		Map<Integer, List<Float>> encodingTable = new HashMap<Integer, List<Float>>(5);

		Iterator<List<Float>> valuesListsIt = valuesLists.iterator();
//		Iterator<Integer> genotypesCountsAccumulatedIt = genotypesCountsAccumulated.iterator();
		Iterator<List<Integer>> perHashGenotypesCountsIt = perHashGenotypesCounts.iterator();
		for (List<Integer> genotypeHashes : genotypesHashes) {
			final List<Float> valuesList = valuesListsIt.next();
//			final Integer genotypesCount = genotypesCountsAccumulatedIt.next();
			final List<Integer> localPerHashGenotypesCounts = perHashGenotypesCountsIt.next();
			if (genotypeHashes  == null) {
				encodingTable.put(null, valuesList);
			} else {
				// This version includes all hashes of all categories
				// with at least one genotype in the current marker.
//				if (genotypesCount > 0) {
//					for (Integer genotypeHash : genotypeHashes) {
//						encodingTable.put(genotypeHash, valuesList);
//					}
//				}

				// This version includes only hashes which themselfs have
				// at least one genotype in the current marker.
				// -> smaller encoding table (faster encoding),
				//    but not as good when debugging
				final Iterator<Integer> localPerHashGenotypesCountsIt = localPerHashGenotypesCounts.iterator();
				for (final Integer genotypeHash : genotypeHashes) {
					final Integer perHashGenotypesCount = localPerHashGenotypesCountsIt.next();
					if (perHashGenotypesCount > 0) {
						encodingTable.put(genotypeHash, valuesList);
					}
				}
			}
		}

		return encodingTable;
	}

	private static List<List<Float>> swapMajorMinor(final List<List<Float>> orig) {

		final List<List<Float>> swapped = new ArrayList<List<Float>>(orig.size());

		swapped.add(orig.get(3)); // "aa" -> "AA"
		swapped.add(orig.get(2)); // "aA" -> "Aa"
		swapped.add(orig.get(1)); // "Aa" -> "aA"
		swapped.add(orig.get(0)); // "AA" -> "aa"
		swapped.add(orig.get(4)); // "--" -> "--" (everything else)

		return swapped;
	}

//	@Override
	private Map<Integer, List<Float>> generateEncodingTable(
//			Set<byte[]> possibleGenotypes,
//			Collection<byte[]> rawGenotypes)
			final byte majorAllele,
			final byte minorAllele,
			final int[] genotypeCounts,
			final int numSamples,
			final int numFeatures,
			final double pStdDev)
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
//			encodingTable.put(baseEncoding.getKey(), ENCODED_VALUES.get(baseEncoding.getValue()));
//		}
//
//		return encodingTable;

//		Map<Integer, List<Float>> genotypeOrdinalToEncodedLookupTable = getGenotypeOrdinalToEncodedLookupTable();
		List<List<Integer>> genotypesHashes = createGenotypesHashes(majorAllele, minorAllele);
		List<List<Integer>> perHashGenotypesCounts = createPerHashGenotypesCounts(genotypeCounts);
		List<Integer> genotypesCountsAccumulated = accumulateGenotypesCounts(genotypeCounts);

		final boolean lexOrder = isUsingLexicographicEncodingOrder();
		final boolean swapMajorMinor = (lexOrder && (majorAllele > minorAllele))
				|| (!lexOrder && (majorAllele < minorAllele) && genotypesCountsAccumulated.get(0).equals(genotypesCountsAccumulated.get(3)));

		// If swapping, instead of assigning encoding values based on the allele
		// frequencies (major & minor), we use the lexicographicly smaller
		// allele as 'A', and the other as 'a'.
		List<List<Float>> encodedValuesLists = getEncodedValuesLists(swapMajorMinor);

//		if (((majorAllele > minorAllele) && USE_LEXICOGRAPHIC_ENCODING_ORDER)
//				|| ((majorAllele < minorAllele) && (genotypesCountsAccumulated.get(0) == genotypesCountsAccumulated.get(3))))
//		{
//			// Instead of assigning ecoding values based on the allele
//			// frequencies (major & minor), we use the lexicographicly smaller
//			// allele as 'A', and the other as 'a'.
//
//			encodedValuesLists = swapMajorMinor(encodedValuesLists);
//		}

		List<List<Float>> encodedAndWhitenedValuesLists = createWhitenedValuesLists(encodedValuesLists, genotypesCountsAccumulated, numSamples, numFeatures, pStdDev);

//		Map<Integer, List<Float>> encodingTable = stitchTogether(genotypesHashes, encodedAndWhitenedValuesLists, genotypesCountsAccumulated);
		Map<Integer, List<Float>> encodingTable = stitchTogether(genotypesHashes, encodedAndWhitenedValuesLists, perHashGenotypesCounts);

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
		final int firstEncodedVal = possibleValues.contains((byte) '0') ? 0 : 1;
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
			final GenotypeEncodingParams params,
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
		final int numFeatures = encodedSamplesFeatures.getNumFeatures();

		// create the encoding table
		final Map<Integer, List<Float>> encodingTable
//				= generateEncodingTable(possibleGenotypes, rawGenotypes);
				= generateEncodingTable(majorAllele, minorAllele, genotypeCounts, numSamples, numFeatures, params.getFeatureScalingP());

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
			int fi = markerIndex * getEncodingFactor();
			for (int lfi = 0; lfi < getEncodingFactor(); lfi++) {
				encodedSamplesFeatures.startStoringFeature(fi);
				for (int si = 0; si < encodedSamplesFeatures.getNumSamples(); si++) {
					encodedSamplesFeatures.setSampleValue(si, 0.0f);
				}
				encodedSamplesFeatures.endStoringFeature();
				fi++;
			}
		} else {
			// encode
			int fi = markerIndex * getEncodingFactor();
			encodedSamplesFeatures.startStoringFeature(fi);
			int si = 0;
//			if (samplesToKeep == null) {
				// include all samples
// XXX Testing-/debug-code that may be removed at some point
int gti = 0;
				for (byte[] genotype : rawGenotypes) {
final byte[] genotypeIndexed = rawGenotypes.get(gti++);
if (genotypeIndexed[0] != genotype[0] || genotypeIndexed[1] != genotype[1]) {
	throw new RuntimeException();
}
					final int gtHash = Genotype.hashCode(genotype);
//final String gtStr = new String(genotype);
					List<Float> encodedGT = encodingTable.get(gtHash);
					if (encodedGT == null) {
						encodedGT = invalidEncoded;
					}
					encodedSamplesFeatures.setSampleValue(si++, encodedGT.get(0));
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

	private double norm(final List<Double> numbers, final double pNorm) {

		final double pNormInv = 1.0 / pNorm;

		double norm = 0.0;
		for (final Double number : numbers) {
			norm += Math.pow(number, pNorm);
		}
		norm = Math.pow(norm, pNormInv);

		return norm;
	}

	@Override
	public void decodeWeights(
			final List<Double> encodedWeights,
			final GenotypeEncodingParams params,
			final List<Double> decodedWeights)
	{
		final double pNorm = params.getWeightsDecodingP();
		final double norm = norm(encodedWeights, pNorm);
		final int encodingFactor = getEncodingFactor();
		final double pNormInv = 1.0 / pNorm;
		for (int ewi = 0; ewi < encodedWeights.size(); ewi += encodingFactor) {
			double sum = 0.0;
			for (int lwi = 0; lwi < encodingFactor; lwi++) {
				final double wEncNormalized = Math.abs(encodedWeights.get(ewi + lwi)) / norm;
				sum += Math.pow(wEncNormalized, pNorm);
			}
//			final double wDecNormalized = sum / encodingFactor;
			final double wDecNormalized = Math.pow(sum, pNormInv);
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

	/**
	 * Returns something like "Allelic", "Genotypic" or "Nominal".
	 * @return
	 */
	@Override
	public String getHumanReadableName() {
		return getClass().getSimpleName().replaceFirst(GenotypeEncoder.class.getSimpleName(), "");
	}

	@Override
	public String toString() {
		return getHumanReadableName();
	}
}

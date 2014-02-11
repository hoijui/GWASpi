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
import java.util.List;

/**
 * Encodes genotypes into values suitable for SVM input,
 * and decodes the weights (result of the SVM training)
 * into a usable format again.
 * May be
 */
public interface GenotypeEncoder {

//	int calculateEncodedGenotypeVectorSize(int oldSize);

	/**
	 * Encodes the genotypes for one marker (== one data-point for the SVM)
	 * into values suitable for SVM input.
	 * @param possibleGenotypes input, one up to three entries
	 *   per genotype(-pair) per marker, for example:
	 *   <code>
	 *   {
	 *     {{'A', 'A'}, {'A', 'G'}, {'G', 'G'}}, // possible values for marker 0
	 *     {{'A', 'T'}, {'T', 'T'}},             // possible values for marker 1
	 *     {{'G', 'G'}, {'G', 'T'}, {'T', 'T'}}, // possible values for marker 2
	 *   }
	 *   </code>
	 * @param rawGenotypes input, one genotype(-pair) per marker and sample,
	 *   for example:
	 *   <code>
	 *   {
	 *     {{'A', 'A'}, {'A', 'G'}, {'A', 'G'}, ...}, // samples for marker 0
	 *     {{'A', 'T'}, {'T', 'T'}, {'A', 'T'}, ...}, // samples for marker 1
	 *     {{'G', 'G'}, {'T', 'T'}, {'T', 'T'}, ...}, // samples for marker 2
	 *   }
	 *   </code>
	 * @param encodedSamplesMarkers output, possibly multiple values per marker,
	 *   for example (Nominal encoding):
	 *   <code>
	 *   {
	 *     {1, 2, 2, ...}, // encoded samples for marker 0
	 *     {2, 3, 2, ...}, // encoded samples for marker 1
	 *     {1, 3, 3, ...}, // encoded samples for marker 2
	 *   }
	 *   </code>
	 * @param markerIndex which marker the rawGenotypes belong to
	 */
	void encodeGenotypes(
			final List<byte[]> rawGenotypes,
			final byte majorAllele,
			final byte minorAllele,
			final int[] genotypeCounts,
			SamplesFeaturesStorage<Float> encodedSamplesMarkers,
			int markerIndex);


	/**
	 * Returns to how many values a genotype gets enlarged to.
	 * For example:
	 * {'A', 'G'} --gets-converted-to--> {0.0, 1.0, 1.0, 0.0}
	 * -> return 4
	 */
	int getEncodingFactor();

	/**
	 * Decodes the weights from the raw SVM output.
	 * @param encodedWeights raw input, as generated from the SVM algorithm,
	 *   possibly multiple values per marker,
	 *   depending on the output of the encoding
	 *   for example:
	 *   <code>
	 *   {
	 *     {0.1, 0.3, 0.0, ...}, // XXX
	 *   }
	 *   </code>
	 * @param decodedWeights output, one value per marker
	 *   for example:
	 * XXX
	 */
	void decodeWeights(
			final List<Double> encodedWeights,
			List<Double> decodedWeights);

	/**
	 * Returns a human friendly name of the encoding algorithm.
	 */
	String getHumanReadableName();
}

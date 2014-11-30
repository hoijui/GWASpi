/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.operations.qamarkers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.NetCDFConstants.Defaults.AlleleByte;

/**
 * Raw statistics (counts) of single allele and genotype
 * (the two allele in combination) within a single marker.
 */
public class RawMarkerAlleleAndGTStatistics {

	protected static final int NUM_GLOBALLY_POSSIBLE_ALLELE = AlleleByte.values().length;

	private final int[] alleleValueToOrdinalLookupTable;
	/**
	 * Counts which allele appears how many times per marker,
	 * whether in the father or in the mother position.
	 */
	private final float[] alleleOrdinalCounts;
	/**
	 * Counts which allele combinations (genotypes, father & mother allele)
	 * appears how many times per marker.
	 */
	private final float[][] gtOrdinalCounts;
	private int missingCount;

	public RawMarkerAlleleAndGTStatistics(final int[] alleleValueToOrdinalLookupTable) {

		this.alleleValueToOrdinalLookupTable = alleleValueToOrdinalLookupTable;
		this.alleleOrdinalCounts = new float[NUM_GLOBALLY_POSSIBLE_ALLELE];
		this.gtOrdinalCounts = new float[NUM_GLOBALLY_POSSIBLE_ALLELE][NUM_GLOBALLY_POSSIBLE_ALLELE];

		internalClear();
	}

	public int[] getAlleleValueToOrdinalLookupTable() {
		return alleleValueToOrdinalLookupTable;
	}

	public float[] getAlleleOrdinalCounts() {
		return alleleOrdinalCounts;
	}

	public float[][] getGtOrdinalCounts() {
		return gtOrdinalCounts;
	}

	public int getMissingCount() {
		return missingCount;
	}

	public void setMissingCount(int missingCount) {
		this.missingCount = missingCount;
	}

	/**
	 * Transcribes the allele ordinal counts table into a value map,
	 * containing only values with non-zero counts.
	 * @return
	 */
	public Map<Byte, Float> extractAllelesCounts() {

		// transcribe ordinal tables into value maps
		final Map<Byte, Float> alleleCounts = new LinkedHashMap<Byte, Float>(3);
		for (int ao = 0; ao < alleleOrdinalCounts.length; ao++) {
			if (alleleOrdinalCounts[ao] != 0.0f) {
				alleleCounts.put(AlleleByte.values()[ao].getValue(), alleleOrdinalCounts[ao]);
			}
		}

		return alleleCounts;
	}

	private void internalClear() {

		Arrays.fill(alleleOrdinalCounts, 0.0f);
		for (float[] gtOrdinalCountsRow : gtOrdinalCounts) {
			Arrays.fill(gtOrdinalCountsRow, 0.0f);
		}
		missingCount = -1;
	}

	public void clear() {
		internalClear();
	}
}

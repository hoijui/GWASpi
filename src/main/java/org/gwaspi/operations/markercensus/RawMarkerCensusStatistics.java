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

package org.gwaspi.operations.markercensus;

import java.util.Arrays;
import org.gwaspi.operations.qamarkers.RawMarkerAlleleAndGTStatistics;

/**
 * Raw statistics (counts) of single allele and genotype
 * (the two allele in combination) within a single marker,
 * with additional counts per category.
 */
public class RawMarkerCensusStatistics extends RawMarkerAlleleAndGTStatistics {

	/**
	 * Counts which allele combinations (genotypes, father & mother allele)
	 * appears how many times in per marker for CASE/AFFECTED samples.
	 */
	private final float[][] caseGtOrdinalCounts;
	/**
	 * Counts which allele combinations (genotypes, father & mother allele)
	 * appears how many times in per marker for CONTROL/UNAFFECTED samples.
	 */
	private final float[][] ctrlGtOrdinalCounts;
	/**
	 * Counts which allele combinations (genotypes, father & mother allele)
	 * appears how many times in per marker for HARDY&WEINBERG samples.
	 */
	private final float[][] hwGtOrdinalCounts;

	public RawMarkerCensusStatistics(final int[] alleleValueToOrdinalLookupTable) {
		super(alleleValueToOrdinalLookupTable);

		this.caseGtOrdinalCounts = new float[NUM_GLOBALLY_POSSIBLE_ALLELE][NUM_GLOBALLY_POSSIBLE_ALLELE];
		this.ctrlGtOrdinalCounts = new float[NUM_GLOBALLY_POSSIBLE_ALLELE][NUM_GLOBALLY_POSSIBLE_ALLELE];
		this.hwGtOrdinalCounts = new float[NUM_GLOBALLY_POSSIBLE_ALLELE][NUM_GLOBALLY_POSSIBLE_ALLELE];

		internalClear();
	}

	public float[][] getCaseGtOrdinalCounts() {
		return caseGtOrdinalCounts;
	}

	public float[][] getControlGtOrdinalCounts() {
		return ctrlGtOrdinalCounts;
	}

	public float[][] getHardyWeinbergGtOrdinalCounts() {
		return hwGtOrdinalCounts;
	}

	private void internalClear() {
		super.clear();

		for (float[] gtOrdinalCountsRow : caseGtOrdinalCounts) {
			Arrays.fill(gtOrdinalCountsRow, 0.0f);
		}
		for (float[] gtOrdinalCountsRow : ctrlGtOrdinalCounts) {
			Arrays.fill(gtOrdinalCountsRow, 0.0f);
		}
		for (float[] gtOrdinalCountsRow : hwGtOrdinalCounts) {
			Arrays.fill(gtOrdinalCountsRow, 0.0f);
		}
	}

	@Override
	public void clear() {
		internalClear();
	}
}

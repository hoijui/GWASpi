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

package org.gwaspi.operations.qamarkers;

import java.io.Serializable;
import org.gwaspi.global.Extractor;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.OperationDataEntry;


public interface QAMarkersOperationEntry extends OperationDataEntry<MarkerKey> {

	public static class MismatchStateExtractor
			implements Extractor<QAMarkersOperationEntry, Boolean>,
			Serializable
	{
		@Override
		public Boolean extract(QAMarkersOperationEntry from) {
			return from.getMismatchState();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, Boolean> TO_MISMATCH_STATE
			= new MismatchStateExtractor();

	public static class MajorAlleleExtractor
			implements Extractor<QAMarkersOperationEntry, Byte>,
			Serializable
	{
		@Override
		public Byte extract(QAMarkersOperationEntry from) {
			return from.getMajorAllele();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, Byte> TO_MAJOR_ALLELE
			= new MajorAlleleExtractor();

	public static class MajorAlleleFrequencyExtractor
			implements Extractor<QAMarkersOperationEntry, Double>,
			Serializable
	{
		@Override
		public Double extract(QAMarkersOperationEntry from) {
			return from.getMajorAlleleFrequency();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, Double> TO_MAJOR_ALLELE_FREQUENCY
			= new MajorAlleleFrequencyExtractor();

	public static class MinorAlleleExtractor
			implements Extractor<QAMarkersOperationEntry, Byte>,
			Serializable
	{
		@Override
		public Byte extract(QAMarkersOperationEntry from) {
			return from.getMinorAllele();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, Byte> TO_MINOR_ALLELE
			= new MinorAlleleExtractor();

	public static class MinorAlleleFrequencyExtractor
			implements Extractor<QAMarkersOperationEntry, Double>,
			Serializable
	{
		@Override
		public Double extract(QAMarkersOperationEntry from) {
			return from.getMinorAlleleFrequency();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, Double> TO_MINOR_ALLELE_FREQUENCY
			= new MinorAlleleFrequencyExtractor();

	public static class AlleleAAExtractor
			implements Extractor<QAMarkersOperationEntry, Integer>,
			Serializable
	{
		@Override
		public Integer extract(QAMarkersOperationEntry from) {
			return from.getAlleleAA();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, Integer> TO_ALLELE_AA
			= new AlleleAAExtractor();

	public static class AlleleAaExtractor
			implements Extractor<QAMarkersOperationEntry, Integer>,
			Serializable
	{
		@Override
		public Integer extract(QAMarkersOperationEntry from) {
			return from.getAlleleAa();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, Integer> TO_ALLELE_Aa
			= new AlleleAaExtractor();

	public static class AlleleaaExtractor
			implements Extractor<QAMarkersOperationEntry, Integer>,
			Serializable
	{
		@Override
		public Integer extract(QAMarkersOperationEntry from) {
			return from.getAlleleaa();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, Integer> TO_ALLELE_aa
			= new AlleleaaExtractor();

	public static class MissingCountExtractor
			implements Extractor<QAMarkersOperationEntry, Integer>,
			Serializable
	{
		@Override
		public Integer extract(QAMarkersOperationEntry from) {
			return from.getMissingCount();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, Integer> TO_MISSING_COUNT
			= new MissingCountExtractor();

	public static class MissingRatioExtractor
			implements Extractor<QAMarkersOperationEntry, Double>,
			Serializable
	{
		@Override
		public Double extract(QAMarkersOperationEntry from) {
			return from.getMissingRatio();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, Double> TO_MISSING_RATIO
			= new MissingRatioExtractor();

	public static class AlleleCountsExtractor
			implements Extractor<QAMarkersOperationEntry, int[]>,
			Serializable
	{
		@Override
		public int[] extract(QAMarkersOperationEntry from) {
			return from.getAlleleCounts();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, int[]> TO_ALLELE_COUNTS
			= new AlleleCountsExtractor();

	public static class GenotypeCountsExtractor
			implements Extractor<QAMarkersOperationEntry, int[]>,
			Serializable
	{
		@Override
		public int[] extract(QAMarkersOperationEntry from) {
			return from.getGenotypeCounts();
		}
	};
	public static final Extractor<QAMarkersOperationEntry, int[]> TO_GENOTYPE_COUNTS
			= new GenotypeCountsExtractor();

	public static enum AlleleCounts {
		_A,
		_a,
		_0,
		/** This stands for all other values summed up */
		_dash;
	}

	public static enum GenotypeCounts {
		_AA,
		_A0,
		_0A,
		_Aa,
		_aA,
		_aa,
		_a0,
		_0a,
		_00,
		/** This stands for all other values summed up */
		_dash_dash;
	}

	/**
	 * @return
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE
	 */
	boolean getMismatchState();

	/**
	 * @return dictionary allele 1 of this marker
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES
	 */
	byte getMajorAllele();

	/**
	 * @return frequency of dictionary allele 1 in all the alleles for any given marker
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ
	 */
	double getMajorAlleleFrequency();

	/**
	 * @return dictionary allele 2 of this marker
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_MINALLELES
	 */
	byte getMinorAllele();

	/**
	 * @return frequency of dictionary allele 2 in all the alleles for any given marker
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ
	 */
	double getMinorAlleleFrequency();

//	/**
//	 * @return allele-AA, allele-Aa, allele-aa, missing-count of this marker
//	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL
//	 * @deprecated use the 4 individual property fetchers instead
//	 */
//	int[] getAllCensus();

	/**
	 * @return allele-AA of this marker
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL [0]
	 */
	int getAlleleAA();

	/**
	 * @return allele-Aa of this marker
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL [1]
	 */
	int getAlleleAa();

	/**
	 * @return allele-aa of this marker
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL [2]
	 */
	int getAlleleaa();

	/**
	 * @return missing-count of this marker
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL [3]
	 */
	int getMissingCount();

	/**
	 * This is the missing count divided by the total number of samples.
	 * @return the missing ratio of this marker
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT
	 */
	double getMissingRatio();

//	/**
//	 * @return how many times each allele appears in this marker,
//	 *   indexed by ordinal (index) of the allele in
//	 *   {@link cNetCDF.Defaults.AlleleByte}.
//	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_ALLELE_COUNTS
//	 */
//	int[] getAlleleOrdinalCounts();
//	/**
//	 * @return how many times each allele appears in this marker.
//	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_ALLELE_COUNTS
//	 */
//	Map<Byte, Integer> getAlleleCounts();
	/**
	 * Returns how many times each allele appears in this marker.
	 * @return an array of fixed size 4, with the counts of alleles:
	 *   A, a, 0, - (stands for all others)
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_ALLELE_COUNTS
	 */
	int[] getAlleleCounts();

//	/**
//	 * @return how many times each genotype/allele-pair appears in this marker,
//	 *   indexed by the major and minor allele ordinals (indices) in
//	 *   {@link cNetCDF.Defaults.AlleleByte}.
//	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_GENOTYPE_COUNTS
//	 */
//	int[][] getGenotypeOrdinalCounts();
//	/**
//	 * @return how many times each genotype/allele-pair appears in this marker.
//	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_GENOTYPE_COUNTS
//	 */
//	Map<Byte, Map<Byte, Integer>> getGenotypeCounts();
	/**
	 * Returns how many times each genotype/allele-pair appears in this marker.
	 * @return an array of fixed size 9, with the counts of alleles:
	 *   AA, A0, 0A, Aa, aA, aa, a0, 0a, -- (this one stands for all others)
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_GENOTYPE_COUNTS
	 */
	int[] getGenotypeCounts();
}

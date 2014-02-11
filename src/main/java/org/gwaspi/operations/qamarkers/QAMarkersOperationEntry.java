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

import java.util.Map;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.OperationDataEntry;

public interface QAMarkersOperationEntry extends OperationDataEntry<MarkerKey> {

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

	/**
	 * @return allele-AA, allele-Aa, allele-aa, missing-count of this marker
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL
	 * @deprecated use the 4 individual property fetchers instead
	 */
	int[] getAllCensus();

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
	/**
	 * @return how many times each allele appears in this marker.
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_ALLELE_COUNTS
	 */
	Map<Byte, Integer> getAlleleCounts();

//	/**
//	 * @return how many times each genotype/allele-pair appears in this marker,
//	 *   indexed by the major and minor allele ordinals (indices) in
//	 *   {@link cNetCDF.Defaults.AlleleByte}.
//	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_GENOTYPE_COUNTS
//	 */
//	int[][] getGenotypeOrdinalCounts();
	/**
	 * @return how many times each genotype/allele-pair appears in this marker.
	 * NetCDF variable: cNetCDF.Census.VAR_OP_MARKERS_GENOTYPE_COUNTS
	 */
	Map<Byte, Map<Byte, Integer>> getGenotypeCounts();
}

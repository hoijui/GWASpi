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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.gwaspi.model.Census;
import org.gwaspi.operations.OperationDataSet;

public interface QAMarkersOperationDataSet extends OperationDataSet<QAMarkersOperationEntry> {

//	/**
//	 * @param markerMismatchStates
//	 *   whether there is a mismatch (true) or not (false), one per marker in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_MISMATCHSTATE
//	 * @throws IOException
//	 */
//	void setMarkerMismatchStates(Collection<Boolean> markerMismatchStates) throws IOException;
//
//	/**
//	 * @param markerKnownAlleles
//	 *   the dictionary allele 1 and 2 values and their frequencies, one such set per marker in this operation
//	 * NetCDF variable:
//	 * - Census.VAR_OP_MARKERS_MAJALLELES
//	 * - Census.VAR_OP_MARKERS_MAJALLELEFRQ
//	 * - Census.VAR_OP_MARKERS_MINALLELES
//	 * - Census.VAR_OP_MARKERS_MINALLELEFRQ
//	 * @throws IOException
//	 */
//	void setMarkerKnownAlleles(Collection<OrderedAlleles> markerKnownAlleles) throws IOException;

//	/**
//	 * @param markerMajorAlleles
//	 *   the dictionary allele 1 values, one per marker in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_MAJALLELES
//	 * @throws IOException
//	 */
//	void setMarkerMajorAlleles(Collection<Byte> markerMajorAlleles) throws IOException;
//
//	/**
//	 * @param markerMajorAllelesFrequencies
//	 *   the frequency of dictionary allele 1 in all the alleles for any given marker  in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_MAJALLELEFRQ
//	 * @throws IOException
//	 */
//	void setMarkerMajorAlleleFrequencies(Collection<Double> markerMajorAllelesFrequencies) throws IOException;
//
//	/**
//	 * @param markerMinorAlleles
//	 *   the dictionary allele 2 values, one per marker in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_MINALLELES
//	 * @throws IOException
//	 */
//	void setMarkerMinorAlleles(Collection<Byte> markerMinorAlleles) throws IOException;
//
//	/**
//	 * @param markerMinorAllelesFrequencies
//	 *   the frequency of dictionary allele 2 in all the alleles for any given marker in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_MINALLELEFRQ
//	 * @throws IOException
//	 */
//	void setMarkerMinorAlleleFrequencies(Collection<Double> markerMinorAllelesFrequencies) throws IOException;

//	/**
//	 * @param markerCensusAll
//	 *   int[4]: allele-AA, allele-Aa, allele-aa, missing-count for each marker in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_CENSUSALL
//	 * @throws IOException
//	 */
////	void setMarkerCensusAll(Collection<int[]> markerCensusAll) throws IOException;
//	void setMarkerCensusAll(Collection<Census> markerCensusAll) throws IOException;
//
//	/**
//	 * @param markerMissingRatios
//	 *   the marker missing ratio values, one per marker in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_MISSINGRAT
//	 * @throws IOException
//	 */
//	void setMarkerMissingRatios(Collection<Double> markerMissingRatios) throws IOException;

	List<Boolean> getMismatchStates() throws IOException;
	List<Byte> getKnownMajorAllele() throws IOException;
	List<Double> getKnownMajorAlleleFrequencies() throws IOException;
	List<Byte> getKnownMinorAllele() throws IOException;
	List<Double> getKnownMinorAlleleFrequencies() throws IOException;
//	List<int[]> getCensusAll() throws IOException;
	List<Integer> getNumAAs() throws IOException;
	List<Integer> getNumAas() throws IOException;
	List<Integer> getNumaas() throws IOException;
	List<Integer> getMissingCounts() throws IOException;
	List<Double> getMissingRatio() throws IOException; // TODO add an 's' at method-name end
//	List<int[]> getAlleleOrdinalCounts() throws IOException;
//	List<int[][]> getGenotypeOrdinalCounts() throws IOException;
//	List<Map<Byte, Integer>> getAlleleCounts() throws IOException;
//	List<Map<Byte, Map<Byte, Integer>>> getGenotypeCounts() throws IOException;
	List<int[]> getAlleleCounts() throws IOException;
	List<int[]> getGenotypeCounts() throws IOException;

	List<Boolean> getMismatchStates(int from, int to) throws IOException;
	List<Byte> getKnownMajorAllele(int from, int to) throws IOException;
	List<Double> getKnownMajorAlleleFrequencies(int from, int to) throws IOException;
	List<Byte> getKnownMinorAllele(int from, int to) throws IOException;
	List<Double> getKnownMinorAlleleFrequencies(int from, int to) throws IOException;
//	List<int[]> getCensusAll(int from, int to) throws IOException;
	List<Integer> getNumAAs(int from, int to) throws IOException;
	List<Integer> getNumAas(int from, int to) throws IOException;
	List<Integer> getNumaas(int from, int to) throws IOException;
	List<Integer> getMissingCounts(int from, int to) throws IOException;
	List<Double> getMissingRatio(int from, int to) throws IOException; // TODO add an 's' at method-name end
//	List<int[]> getAlleleOrdinalCounts(int from, int to) throws IOException;
//	List<int[][]> getGenotypeOrdinalCounts(int from, int to) throws IOException;
//	List<Map<Byte, Integer>> getAlleleCounts(int from, int to) throws IOException;
//	List<Map<Byte, Map<Byte, Integer>>> getGenotypeCounts(int from, int to) throws IOException;
	List<int[]> getAlleleCounts(int from, int to) throws IOException;
	List<int[]> getGenotypeCounts(int from, int to) throws IOException;
}

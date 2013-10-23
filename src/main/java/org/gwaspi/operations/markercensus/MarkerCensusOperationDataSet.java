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

package org.gwaspi.operations.markercensus;

import java.io.IOException;
import java.util.Collection;
import org.gwaspi.model.Census;
import org.gwaspi.operations.OperationDataSet;

public interface MarkerCensusOperationDataSet extends OperationDataSet {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - Variables.VAR_ALLELES: known alleles [Collection<char[]>]
	// - Census.VAR_OP_MARKERS_CENSUSALL: marker census - all [Collection<Census.all<== int[]>>]
	// - Census.VAR_OP_MARKERS_CENSUSCASE: marker census - case [Collection<Census.case>]
	// - Census.VAR_OP_MARKERS_CENSUSCTRL: marker census - control [Collection<Census.control>]
	// - Census.VAR_OP_MARKERS_CENSUSHW: marker census - alternate hardy-weinberg [Collection<Census.altHW>]


	/**
	 * XXX maybe should be moved to super class?
	 * @param markerKnownAlleles
	 *   the marker missing ratio values, one per marker in this operation
	 * NetCDF variable: Variables.VAR_ALLELES
	 */
	void setMarkerKnownAlleles(Collection<byte[]> markerKnownAlleles) throws IOException;

	/**
	 * @param markerMismatchStates
	 *   whether there is a mismatch (true) or not (false), one per marker in this operation
	 * NetCDF variable: Census.VAR_OP_MARKERS_CENSUSALL
	 */
	void setMarkerMismatchStates(Collection<Boolean> markerMismatchStates) throws IOException;

	/**
	 * @param markerKnownAlleles
	 *   the dictionary allele 1 and 2 values and their frequencies, one such set per marker in this operation
	 * NetCDF variable:
	 * - Census.VAR_OP_MARKERS_MAJALLELES
	 * - Census.VAR_OP_MARKERS_MAJALLELEFRQ
	 * - Census.VAR_OP_MARKERS_MINALLELES
	 * - Census.VAR_OP_MARKERS_MINALLELEFRQ
	 */
	void setMarkerKnownAlleles(Collection<OrderedAlleles> markerKnownAlleles) throws IOException;

//	/**
//	 * @param markerMajorAlleles
//	 *   the dictionary allele 1 values, one per marker in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_MAJALLELES
//	 */
//	void setMarkerMajorAlleles(Collection<Byte> markerMajorAlleles) throws IOException;
//
//	/**
//	 * @param markerMajorAllelesFrequencies
//	 *   the frequency of dictionary allele 1 in all the alleles for any given marker  in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_MAJALLELEFRQ
//	 */
//	void setMarkerMajorAlleleFrequencies(Collection<Double> markerMajorAllelesFrequencies) throws IOException;
//
//	/**
//	 * @param markerMinorAlleles
//	 *   the dictionary allele 2 values, one per marker in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_MINALLELES
//	 */
//	void setMarkerMinorAlleles(Collection<Byte> markerMinorAlleles) throws IOException;
//
//	/**
//	 * @param markerMinorAllelesFrequencies
//	 *   the frequency of dictionary allele 2 in all the alleles for any given marker in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_MINALLELEFRQ
//	 */
//	void setMarkerMinorAlleleFrequencies(Collection<Double> markerMinorAllelesFrequencies) throws IOException;

	/**
	 * @param markerCensusAll
	 *   int[4]: allele-AA, allele-Aa, allele-aa, missing-count for each marker in this operation
	 * NetCDF variable: Census.VAR_OP_MARKERS_CENSUSALL
	 */
//	void setMarkerCensusAll(Collection<int[]> markerCensusAll) throws IOException;
	void setMarkerCensusAll(Collection<Census> markerCensusAll) throws IOException;
}

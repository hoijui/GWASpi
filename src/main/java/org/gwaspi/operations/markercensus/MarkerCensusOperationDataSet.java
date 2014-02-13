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
import java.util.List;
import org.gwaspi.model.Census;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;

public interface MarkerCensusOperationDataSet extends OperationDataSet<MarkerCensusOperationEntry> {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - Variables.VAR_ALLELES: known alleles [Collection<char[]>]
	// - Census.VAR_OP_MARKERS_CENSUSALL: marker census - all [Collection<Census.all<== int[]>>]
	// - Census.VAR_OP_MARKERS_CENSUSCASE: marker census - case [Collection<Census.case>]
	// - Census.VAR_OP_MARKERS_CENSUSCTRL: marker census - control [Collection<Census.control>]
	// - Census.VAR_OP_MARKERS_CENSUSHW: marker census - alternate hardy-weinberg [Collection<Census.altHW>]

//	/**
//	 * @param markerCensus
//	 *   int[4]: allele-AA, allele-Aa, allele-aa, missing-count for each marker in this operation
//	 * NetCDF variable: Census.VAR_OP_MARKERS_CENSUSALL
//	 */
////	void setMarkerCensusAll(Collection<int[]> markerCensusAll) throws IOException;
//	void setMarkerCensus(Collection<CensusFull> markerCensus) throws IOException;

	void setParams(final MarkerCensusOperationParams params);

	List<byte[]> getKnownAlleles() throws IOException;

	List<Census> getCensus(Category category) throws IOException;

//	Collection<Integer> getCensusMarkerIndices(Category category) throws IOException;

	List<byte[]> getKnownAlleles(int from, int to) throws IOException;
//	Map<Integer, Census> getCensus(Category category, int from, int to) throws IOException;
	List<Census> getCensus(Category category, int from, int to) throws IOException;

	void addEntry(MarkerCensusOperationEntry entry) throws IOException;
}

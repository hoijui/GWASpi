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

package org.gwaspi.operations.trendtest;

import java.io.IOException;
import java.util.Collection;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.netCDF.operations.NetCdfUtils;

public class NetCdfTrendTestOperationDataSet extends AbstractNetCdfTestOperationDataSet<TrendTestOperationEntry> implements TrendTestOperationDataSet {

	// - Variables.VAR_OPSET: wrMarkerMetadata.keySet() [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: markers RS ID from the rd marker census opertion, sorted by wrMarkerMetadata.keySet() [Collection<String>]
	// - Variables.VAR_IMPLICITSET: "implicit set", rdSampleSetMap.keySet(), original sample keys [Collection<SampleKey>]
	// - Variables.VAR_CHR_IN_MATRIX: chromosomeInfo.keySet() [Collection<ChromosomeKey>]
	// - Variables.VAR_CHR_INFO: chromosomeInfo.values() [Collection<ChromosomeInfo>]
	// - Association.VAR_OP_MARKERS_ASTrendTestTP: {T, P-Value} [Double[2]]

	public NetCdfTrendTestOperationDataSet() {
	}

	public void addEntry(TrendTestOperationEntry entry) throws IOException {

		// NOTE result = double[2];
		int[] boxes = new int[] {0, 1};
		NetCdfUtils.saveDoubleMapD2ToWrMatrix(getNetCdfWriteFile(), result.values(), boxes, cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP);

		throw new UnsupportedOperationException("Not supported yet."); // TODO
	}

	@Override
	public Collection<TrendTestOperationEntry> getEntries(int from, int to) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); // TODO
	}
}

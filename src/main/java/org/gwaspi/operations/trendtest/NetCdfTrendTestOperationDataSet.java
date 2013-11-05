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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;

public class NetCdfTrendTestOperationDataSet extends AbstractNetCdfTestOperationDataSet<TrendTestOperationEntry> implements TrendTestOperationDataSet {

	// - Variables.VAR_OPSET: wrMarkerMetadata.keySet() [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: markers RS ID from the rd marker census opertion, sorted by wrMarkerMetadata.keySet() [Collection<String>]
	// - Variables.VAR_IMPLICITSET: "implicit set", rdSampleSetMap.keySet(), original sample keys [Collection<SampleKey>]
	// - Variables.VAR_CHR_IN_MATRIX: chromosomeInfo.keySet() [Collection<ChromosomeKey>]
	// - Variables.VAR_CHR_INFO: chromosomeInfo.values() [Collection<ChromosomeInfo>]
	// - Association.VAR_OP_MARKERS_ASTrendTestTP: {T, P-Value} [Double[2]]

	public NetCdfTrendTestOperationDataSet() {
	}

	protected void writeEntries(int alreadyWritten, Queue<TrendTestOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten};
		ArrayDouble.D1 netCdfTs = new ArrayDouble.D1(writeBuffer.size());
		ArrayDouble.D1 netCdfPs = new ArrayDouble.D1(writeBuffer.size());
		int index = 0;
		for (TrendTestOperationEntry entry : writeBuffer) {
			netCdfTs.setDouble(netCdfTs.getIndex().set(index), entry.getT());
			netCdfPs.setDouble(netCdfPs.getIndex().set(index), entry.getP());
			index++;
		}
		try {
			getNetCdfWriteFile().write(cNetCDF.Association.VAR_OP_MARKERS_T, origin, netCdfTs);
			getNetCdfWriteFile().write(cNetCDF.Association.VAR_OP_MARKERS_P, origin, netCdfPs);
			alreadyWritten += writeBuffer.size();
			writeBuffer.clear();
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public Collection<TrendTestOperationEntry> getEntries(int from, int to) throws IOException {

		MarkerOperationSet rdMarkersSet = new MarkerOperationSet(getResultOperationKey(), from, to);
		Map<MarkerKey, ?> rdMarkers = rdMarkersSet.getOpSetMap();

		Collection<Double> ts = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Association.VAR_OP_MARKERS_T, from, to, ts, null);
		Collection<Double> ps = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Association.VAR_OP_MARKERS_P, from, to, ps, null);

		Collection<TrendTestOperationEntry> entries
				= new ArrayList<TrendTestOperationEntry>(ts.size());
		Iterator<Double> tsIt = ts.iterator();
		Iterator<Double> psIt = ps.iterator();
		for (MarkerKey markerKey : rdMarkers.keySet()) {
			entries.add(new DefaultTrendTestOperationEntry(markerKey, tsIt.next(), psIt.next()));
		}

		return entries;
	}
}

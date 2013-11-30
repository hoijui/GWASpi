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

package org.gwaspi.operations.genotypicassociationtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.operations.trendtest.AbstractNetCdfTestOperationDataSet;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;

public class NetCdfGenotypicAssociationTestsOperationDataSet extends AbstractNetCdfTestOperationDataSet<GenotypicAssociationTestOperationEntry> implements GenotypicAssociationTestsOperationDataSet {

	// - Variables.VAR_OPSET: wrMarkerMetadata.keySet() [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: markers RS ID from the rd marker census opertion, sorted by wrMarkerMetadata.keySet() [Collection<String>]
	// - Variables.VAR_IMPLICITSET: "implicit set", rdSampleSetMap.keySet(), original sample keys [Collection<SampleKey>]
	// - Variables.VAR_CHR_IN_MATRIX: chromosomeInfo.keySet() [Collection<ChromosomeKey>]
	// - Variables.VAR_CHR_INFO: chromosomeInfo.values() [Collection<ChromosomeInfo>]
	// - Association.VAR_OP_MARKERS_ASTrendTestTP: {T, P-Value, OR} [Double[3]]

	private ArrayDouble.D1 netCdfTs;
	private ArrayDouble.D1 netCdfPs;
	private ArrayDouble.D1 netCdfORs;
	private ArrayDouble.D1 netCdfOR2s;

	public NetCdfGenotypicAssociationTestsOperationDataSet(OperationKey operationKey) {
		super(operationKey);
	}

	public NetCdfGenotypicAssociationTestsOperationDataSet() {
		this(null);
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<GenotypicAssociationTestOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten};
		if (netCdfTs == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfTs = new ArrayDouble.D1(writeBuffer.size());
			netCdfPs = new ArrayDouble.D1(writeBuffer.size());
			netCdfORs = new ArrayDouble.D1(writeBuffer.size());
			netCdfOR2s = new ArrayDouble.D1(writeBuffer.size());
		}
		int index = 0;
		for (GenotypicAssociationTestOperationEntry entry : writeBuffer) {
			netCdfTs.setDouble(netCdfTs.getIndex().set(index), entry.getT());
			netCdfPs.setDouble(netCdfPs.getIndex().set(index), entry.getP());
			netCdfORs.setDouble(netCdfORs.getIndex().set(index), entry.getOR());
			netCdfOR2s.setDouble(netCdfOR2s.getIndex().set(index), entry.getOR2());
			index++;
		}
		try {
			getNetCdfWriteFile().write(cNetCDF.Association.VAR_OP_MARKERS_T, origin, netCdfTs);
			getNetCdfWriteFile().write(cNetCDF.Association.VAR_OP_MARKERS_P, origin, netCdfPs);
			getNetCdfWriteFile().write(cNetCDF.Association.VAR_OP_MARKERS_OR, origin, netCdfORs);
			getNetCdfWriteFile().write(cNetCDF.Association.VAR_OP_MARKERS_OR2, origin, netCdfOR2s);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public Collection<Double> getTs(int from, int to) throws IOException {

		Collection<Double> ts = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Association.VAR_OP_MARKERS_T, from, to, ts, null);

		return ts;
	}

	@Override
	public Collection<Double> getPs(int from, int to) throws IOException {

		Collection<Double> ps = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Association.VAR_OP_MARKERS_P, from, to, ps, null);

		return ps;
	}

	@Override
	public Collection<Double> getORs(int from, int to) throws IOException {

		Collection<Double> ors = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Association.VAR_OP_MARKERS_OR, from, to, ors, null);

		return ors;
	}

	@Override
	public Collection<Double> getOR2s(int from, int to) throws IOException {

		Collection<Double> or2s = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Association.VAR_OP_MARKERS_OR2, from, to, or2s, null);

		return or2s;
	}

	@Override
	public Collection<GenotypicAssociationTestOperationEntry> getEntries(int from, int to) throws IOException {

		MarkerOperationSet rdMarkersSet = new MarkerOperationSet(getOperationKey(), from, to);
		Map<MarkerKey, Integer> rdMarkers = rdMarkersSet.getOpSetMap();

		Collection<Double> ts = getTs(from, to);
		Collection<Double> ps = getPs(from, to);
		Collection<Double> ors = getORs(from, to);
		Collection<Double> or2s = getORs(from, to);

		Collection<GenotypicAssociationTestOperationEntry> entries
				= new ArrayList<GenotypicAssociationTestOperationEntry>(ts.size());
		Iterator<Double> tsIt = ts.iterator();
		Iterator<Double> psIt = ps.iterator();
		Iterator<Double> orsIt = ors.iterator();
		Iterator<Double> or2sIt = or2s.iterator();
		for (Map.Entry<MarkerKey, Integer> markerKeyIndex : rdMarkers.entrySet()) {
			entries.add(new DefaultGenotypicAssociationOperationEntry(
					markerKeyIndex.getKey(),
					markerKeyIndex.getValue(),
					tsIt.next(),
					psIt.next(),
					orsIt.next(),
					or2sIt.next()));
		}

		return entries;
	}
}

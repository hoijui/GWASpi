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

package org.gwaspi.operations.allelicassociationtest;

import org.gwaspi.operations.trendtest.*;
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
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;

public class NetCdfAllelicAssociationTestsOperationDataSet extends AbstractNetCdfTestOperationDataSet<AllelicAssociationTestOperationEntry> implements AllelicAssociationTestsOperationDataSet {

	// - Variables.VAR_OPSET: wrMarkerMetadata.keySet() [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: markers RS ID from the rd marker census opertion, sorted by wrMarkerMetadata.keySet() [Collection<String>]
	// - Variables.VAR_IMPLICITSET: "implicit set", rdSampleSetMap.keySet(), original sample keys [Collection<SampleKey>]
	// - Variables.VAR_CHR_IN_MATRIX: chromosomeInfo.keySet() [Collection<ChromosomeKey>]
	// - Variables.VAR_CHR_INFO: chromosomeInfo.values() [Collection<ChromosomeInfo>]
	// - Association.VAR_OP_MARKERS_ASTrendTestTP: {T, P-Value, OR} [Double[3]]

	private ArrayDouble.D1 netCdfTs;
	private ArrayDouble.D1 netCdfPs;
	private ArrayDouble.D1 netCdfORs;

	public NetCdfAllelicAssociationTestsOperationDataSet(OperationKey operationKey) {
		super(operationKey);
	}

	public NetCdfAllelicAssociationTestsOperationDataSet() {
		this(null);
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<AllelicAssociationTestOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten};
		if (netCdfTs == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfTs = new ArrayDouble.D1(writeBuffer.size());
			netCdfPs = new ArrayDouble.D1(writeBuffer.size());
			netCdfORs = new ArrayDouble.D1(writeBuffer.size());
		}
		int index = 0;
		for (AllelicAssociationTestOperationEntry entry : writeBuffer) {
			netCdfTs.setDouble(netCdfTs.getIndex().set(index), entry.getT());
			netCdfPs.setDouble(netCdfPs.getIndex().set(index), entry.getP());
			netCdfORs.setDouble(netCdfORs.getIndex().set(index), entry.getOR());
			index++;
		}
		try {
			getNetCdfWriteFile().write(cNetCDF.Association.VAR_OP_MARKERS_T, origin, netCdfTs);
			getNetCdfWriteFile().write(cNetCDF.Association.VAR_OP_MARKERS_P, origin, netCdfPs);
			getNetCdfWriteFile().write(cNetCDF.Association.VAR_OP_MARKERS_OR, origin, netCdfORs);
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
	public Collection<AllelicAssociationTestOperationEntry> getEntries(int from, int to) throws IOException {

		Map<Integer, MarkerKey> markersKeys = getMarkers();

		Collection<Double> ts = getTs(from, to);
		Collection<Double> ps = getPs(from, to);
		Collection<Double> ors = getORs(from, to);

		Collection<AllelicAssociationTestOperationEntry> entries
				= new ArrayList<AllelicAssociationTestOperationEntry>(ts.size());
		Iterator<Double> tsIt = ts.iterator();
		Iterator<Double> psIt = ps.iterator();
		Iterator<Double> orsIt = ors.iterator();
		for (Map.Entry<Integer, MarkerKey> origIndicesAndKey : markersKeys.entrySet()) {
			entries.add(new DefaultAllelicAssociationOperationEntry(
					origIndicesAndKey.getValue(),
					origIndicesAndKey.getKey(),
					tsIt.next(),
					psIt.next(),
					orsIt.next()));
		}

		return entries;
	}
}

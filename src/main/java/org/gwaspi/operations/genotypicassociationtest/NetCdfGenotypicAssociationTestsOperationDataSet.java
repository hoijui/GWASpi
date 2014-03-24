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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.NetCdfUtils;
import org.gwaspi.operations.trendtest.AbstractNetCdfTestOperationDataSet;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;

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

	public NetCdfGenotypicAssociationTestsOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(origin, parent, operationKey);
	}

	public NetCdfGenotypicAssociationTestsOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
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
		} else if (writeBuffer.size() < netCdfTs.getShape()[0]) {
			// we end up here at the end of the processing, if, for example,
			// we have a buffer size of 10, but only 7 items are left to be written
			List<Range> reducedRange1D = new ArrayList<Range>(1);
			reducedRange1D.add(new Range(writeBuffer.size()));
			try {
				netCdfTs = (ArrayDouble.D1) netCdfTs.sectionNoReduce(reducedRange1D);
				netCdfPs = (ArrayDouble.D1) netCdfPs.sectionNoReduce(reducedRange1D);
				netCdfORs = (ArrayDouble.D1) netCdfORs.sectionNoReduce(reducedRange1D);
				netCdfOR2s = (ArrayDouble.D1) netCdfOR2s.sectionNoReduce(reducedRange1D);
			} catch (InvalidRangeException ex) {
				throw new IOException(ex);
			}
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
	public List<Double> getTs(int from, int to) throws IOException {

		List<Double> ts = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Association.VAR_OP_MARKERS_T, from, to, ts, null);

		return ts;
	}

	@Override
	public List<Double> getPs(int from, int to) throws IOException {

		List<Double> ps = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Association.VAR_OP_MARKERS_P, from, to, ps, null);

		return ps;
	}

	@Override
	public List<Double> getORs(int from, int to) throws IOException {

		List<Double> ors = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Association.VAR_OP_MARKERS_OR, from, to, ors, null);

		return ors;
	}

	@Override
	public List<Double> getOR2s(int from, int to) throws IOException {

		List<Double> or2s = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Association.VAR_OP_MARKERS_OR2, from, to, or2s, null);

		return or2s;
	}

	@Override
	public List<GenotypicAssociationTestOperationEntry> getEntries(int from, int to) throws IOException {

		Map<Integer, MarkerKey> markersKeys = getMarkersKeysSource().getIndicesMap(from, to);
		List<Double> ts = getTs(from, to);
		List<Double> ps = getPs(from, to);
		List<Double> ors = getORs(from, to);
		List<Double> or2s = getORs(from, to);

		List<GenotypicAssociationTestOperationEntry> entries
				= new ArrayList<GenotypicAssociationTestOperationEntry>(ts.size());
		Iterator<Double> tsIt = ts.iterator();
		Iterator<Double> psIt = ps.iterator();
		Iterator<Double> orsIt = ors.iterator();
		Iterator<Double> or2sIt = or2s.iterator();
		for (Map.Entry<Integer, MarkerKey> origIndicesAndKey : markersKeys.entrySet()) {
			entries.add(new DefaultGenotypicAssociationOperationEntry(
					origIndicesAndKey.getValue(),
					origIndicesAndKey.getKey(),
					tsIt.next(),
					psIt.next(),
					orsIt.next(),
					or2sIt.next()));
		}

		return entries;
	}
}

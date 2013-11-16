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

package org.gwaspi.operations.hardyweinberg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.constants.cNetCDF.HardyWeinberg;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;

public class NetCdfHardyWeinbergOperationDataSet extends AbstractNetCdfOperationDataSet<HardyWeinbergOperationEntry> implements HardyWeinbergOperationDataSet {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL: Control P-Value [Double[1]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL: Control Obs Hetzy & Exp Hetzy [Double[2]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT: Hardy-Weinberg Alternative P-Value [Double[1]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT: Hardy-Weinberg Alternative Obs Hetzy & Exp Hetzy [Double[2]]

	private String hardyWeinbergName;
	private OperationKey markerCensusOperationKey;
	private final Map<HardyWeinbergOperationEntry.Category, EntryBuffer<HardyWeinbergOperationEntry>> writeBuffers;

	public NetCdfHardyWeinbergOperationDataSet(OperationKey operationKey) {
		super(true, operationKey);

		this.hardyWeinbergName = null;
		this.markerCensusOperationKey = null;
		this.writeBuffers = new EnumMap<HardyWeinbergOperationEntry.Category, EntryBuffer<HardyWeinbergOperationEntry>>(HardyWeinbergOperationEntry.Category.class);
		for (HardyWeinbergOperationEntry.Category category : HardyWeinbergOperationEntry.Category.values()) {
			writeBuffers.put(category, new EntryBuffer<HardyWeinbergOperationEntry>());
		}
	}

	public NetCdfHardyWeinbergOperationDataSet() {
		this(null);
	}

	@Override
	protected OperationFactory createOperationFactory() throws IOException {

		try {
			return new OperationFactory(
					markerCensusOperationKey.getParentMatrixKey().getStudyKey(),
					"Hardy-Weinberg_" + hardyWeinbergName, // friendly name
					"Hardy-Weinberg test on Samples marked as controls (only females for the X chromosome)"
						+ "\nMarkers: " + getNumMarkers() + ""
						+ "\nSamples: " + getNumSamples(), // description
					getNumMarkers(),
					getNumSamples(),
					0,
					OPType.HARDY_WEINBERG,
					markerCensusOperationKey.getParentMatrixKey(), // Parent matrixId
					markerCensusOperationKey.getId()); // Parent operationId
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void setHardyWeinbergName(String hardyWeinbergName) {
		this.hardyWeinbergName = hardyWeinbergName;
	}

	@Override
	public void setMarkerCensusOperationKey(OperationKey markerCensusOperationKey) {
		this.markerCensusOperationKey = markerCensusOperationKey;
	}

	private static final class EntryBuffer<ET> {

		private final Queue<ET> entries;
		private int alreadyWritten;

		EntryBuffer() {

			this.entries = new LinkedList<ET>();
			alreadyWritten = 0;
		}

		public Queue<ET> getEntries() {
			return entries;
		}

		public int getAlreadyWritten() {
			return alreadyWritten;
		}

		public void setAlreadyWritten(int alreadyWritten) {
			this.alreadyWritten = alreadyWritten;
		}
	}

	@Override
	public void addEntry(HardyWeinbergOperationEntry entry) throws IOException {

		EntryBuffer<HardyWeinbergOperationEntry> buffer = writeBuffers.get(entry.getCategory());

		buffer.getEntries().add(entry);

		if (buffer.getEntries().size() >= getEntriesWriteBufferSize()) {
			writeEntries(buffer.getAlreadyWritten(), buffer.getEntries());
			buffer.setAlreadyWritten(buffer.getAlreadyWritten() + buffer.getEntries().size());
			buffer.getEntries().clear();
		}
	}

	private ArrayDouble.D1 netCdfPs;
	private ArrayDouble.D1 netCdfObsHetzys;
	private ArrayDouble.D1 netCdfExpHetzys;

	@Override
	protected void writeEntries(int alreadyWritten, Queue<HardyWeinbergOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten};
		if (netCdfPs == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfPs = new ArrayDouble.D1(writeBuffer.size());
			netCdfObsHetzys = new ArrayDouble.D1(writeBuffer.size());
			netCdfExpHetzys = new ArrayDouble.D1(writeBuffer.size());
		}
		int index = 0;
		for (HardyWeinbergOperationEntry entry : writeBuffer) {
			netCdfPs.setDouble(netCdfPs.getIndex().set(index), entry.getP());
			netCdfObsHetzys.setDouble(netCdfObsHetzys.getIndex().set(index), entry.getObsHzy());
			netCdfExpHetzys.setDouble(netCdfExpHetzys.getIndex().set(index), entry.getExpHzy());
			index++;
		}
		String varP;
		String varObsHtz;
		String varExpHtz;
		HardyWeinbergOperationEntry.Category category
				= writeBuffer.element().getCategory();
		switch (category) {
			case ALL:
				varP = HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL;
				varObsHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_ALL;
				varExpHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_ALL;
				break;
			case CASE:
				varP = HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE;
				varObsHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_CASE;
				varExpHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_CASE;
				break;
			case CONTROL:
				varP = HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL;
				varObsHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_CTRL;
				varExpHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_CTRL;
				break;
			case ALTERNATE:
				varP = HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT;
				varObsHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_ALT;
				varExpHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_ALT;
				break;
			default:
				throw new IOException("invalid category: " + category);
		}
		try {
			getNetCdfWriteFile().write(varP, origin, netCdfPs);
			getNetCdfWriteFile().write(varObsHtz, origin, netCdfObsHetzys);
			getNetCdfWriteFile().write(varExpHtz, origin, netCdfExpHetzys);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public Collection<HardyWeinbergOperationEntry> getEntries(int from, int to) throws IOException {
		return getEntries(HardyWeinbergOperationEntry.Category.ALL, from, to);
	}

	public Collection<HardyWeinbergOperationEntry> getEntries(HardyWeinbergOperationEntry.Category category, int from, int to) throws IOException {

		String varP;
		String varObsHtz;
		String varExpHtz;
		switch (category) {
			case ALL:
				varP = HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL;
				varObsHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_ALL;
				varExpHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_ALL;
				break;
			case CASE:
				varP = HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE;
				varObsHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_CASE;
				varExpHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_CASE;
				break;
			case CONTROL:
				varP = HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL;
				varObsHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_CTRL;
				varExpHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_CTRL;
				break;
			case ALTERNATE:
				varP = HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT;
				varObsHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_ALT;
				varExpHtz = HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_ALT;
				break;
			default:
				throw new IOException("invalid category: " + category);
		}

		MarkerOperationSet rdMarkersSet = new MarkerOperationSet(getOperationKey(), from, to);
		Map<MarkerKey, Integer> rdMarkers = rdMarkersSet.getOpSetMap();

		Collection<Double> ps = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), varP, from, to, ps, null);

		Collection<Double> hwObsHetzys = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), varObsHtz, from, to, hwObsHetzys, null);

		Collection<Double> hwExpHetzys = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), varExpHtz, from, to, hwExpHetzys, null);

		Collection<HardyWeinbergOperationEntry> entries
				= new ArrayList<HardyWeinbergOperationEntry>(ps.size());
		Iterator<Double> psIt = ps.iterator();
		Iterator<Double> hwObsHetzysIt = hwObsHetzys.iterator();
		Iterator<Double> hwExpHetzysIt = hwExpHetzys.iterator();
		for (Map.Entry<MarkerKey, Integer> keysIndices : rdMarkers.entrySet()) {
			entries.add(new DefaultHardyWeinbergOperationEntry(
					keysIndices.getKey(),
					keysIndices.getValue(),
					category,
					psIt.next(),
					hwObsHetzysIt.next(),
					hwExpHetzysIt.next()));
		}

		return entries;
	}
}

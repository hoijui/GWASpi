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
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.constants.cNetCDF.HardyWeinberg;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class NetCdfHardyWeinbergOperationDataSet extends AbstractNetCdfOperationDataSet<HardyWeinbergOperationEntry> implements HardyWeinbergOperationDataSet {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL: Control P-Value [Double[1]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL: Control Obs Hetzy & Exp Hetzy [Double[2]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT: Hardy-Weinberg Alternative P-Value [Double[1]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT: Hardy-Weinberg Alternative Obs Hetzy & Exp Hetzy [Double[2]]

	private static final Map<Category, String> netCdfPVars;
	private static final Map<Category, String> netCdfHetzyObsVars;
	private static final Map<Category, String> netCdfHetzyExpVars;

	static {
		Map<Category, String> netCdfPVarsTmp = new EnumMap<Category, String>(Category.class);
		netCdfPVarsTmp.put(Category.ALL, HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL);
		netCdfPVarsTmp.put(Category.CASE, HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE);
		netCdfPVarsTmp.put(Category.CONTROL, HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
		netCdfPVarsTmp.put(Category.ALTERNATE, HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT);
		netCdfPVars = Collections.unmodifiableMap(netCdfPVarsTmp);

		Map<Category, String> netCdfHetzyObsVarsTmp = new EnumMap<Category, String>(Category.class);
		netCdfHetzyObsVarsTmp.put(Category.ALL, HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_ALL);
		netCdfHetzyObsVarsTmp.put(Category.CASE, HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_CASE);
		netCdfHetzyObsVarsTmp.put(Category.CONTROL, HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_CTRL);
		netCdfHetzyObsVarsTmp.put(Category.ALTERNATE, HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_ALT);
		netCdfHetzyObsVars = Collections.unmodifiableMap(netCdfHetzyObsVarsTmp);

		Map<Category, String> netCdfHetzyExpVarsTmp = new EnumMap<Category, String>(Category.class);
		netCdfHetzyExpVarsTmp.put(Category.ALL, HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_ALL);
		netCdfHetzyExpVarsTmp.put(Category.CASE, HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_CASE);
		netCdfHetzyExpVarsTmp.put(Category.CONTROL, HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_CTRL);
		netCdfHetzyExpVarsTmp.put(Category.ALTERNATE, HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_ALT);
		netCdfHetzyExpVars = Collections.unmodifiableMap(netCdfHetzyExpVarsTmp);
	}


	private String hardyWeinbergName;
	private OperationKey markerCensusOperationKey;
	private final Map<HardyWeinbergOperationEntry.Category, EntryBuffer<HardyWeinbergOperationEntry>> writeBuffers;
	private ArrayDouble.D1 netCdfPs;
	private ArrayDouble.D1 netCdfObsHetzys;
	private ArrayDouble.D1 netCdfExpHetzys;

	public NetCdfHardyWeinbergOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(true, operationKey.getParentMatrixKey(), parent, operationKey);

		this.hardyWeinbergName = null;
		this.markerCensusOperationKey = null;
		this.writeBuffers = new EnumMap<HardyWeinbergOperationEntry.Category, EntryBuffer<HardyWeinbergOperationEntry>>(HardyWeinbergOperationEntry.Category.class);
		for (HardyWeinbergOperationEntry.Category category : HardyWeinbergOperationEntry.Category.values()) {
			writeBuffers.put(category, new EntryBuffer<HardyWeinbergOperationEntry>());
		}
	}

	public NetCdfHardyWeinbergOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
	}

	@Override
	protected void supplementNetCdfHandler(
			NetcdfFileWriteable ncFile,
			OperationMetadata operationMetadata,
			List<Dimension> markersSpace,
			List<Dimension> chromosomesSpace,
			List<Dimension> samplesSpace)
			throws IOException
	{
		// Define OP Variables
//		ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL, DataType.DOUBLE, markersSpace);
//		ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE, DataType.DOUBLE, markersSpace);
		ncFile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL, DataType.DOUBLE, markersSpace);
		ncFile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT, DataType.DOUBLE, markersSpace);

//		ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_ALL, DataType.DOUBLE, markersSpace);
//		ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_CASE, DataType.DOUBLE, markersSpace);
		ncFile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_CTRL, DataType.DOUBLE, markersSpace);
		ncFile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZYOBS_ALT, DataType.DOUBLE, markersSpace);

//		ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_ALL, DataType.DOUBLE, markersSpace);
//		ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_CASE, DataType.DOUBLE, markersSpace);
		ncFile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_CTRL, DataType.DOUBLE, markersSpace);
		ncFile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZYEXP_ALT, DataType.DOUBLE, markersSpace);
	}

	@Override
	protected OperationMetadata createOperationMetadata() throws IOException {

		return new OperationMetadata(
				new DataSetKey(markerCensusOperationKey), // parent data set
				"Hardy-Weinberg_" + hardyWeinbergName, // friendly name
				"Hardy-Weinberg test on Samples marked as controls (only females for the X chromosome)"
					+ "\nMarkers: " + getNumMarkers() + ""
					+ "\nSamples: " + getNumSamples(), // description
				OPType.HARDY_WEINBERG, // operationType
				getNumMarkers(),
				getNumSamples(),
				getNumChromosomes(),
				isMarkersOperationSet());
	}

	@Override
	public void setHardyWeinbergName(String hardyWeinbergName) {
		this.hardyWeinbergName = hardyWeinbergName;
	}

	@Override
	public void setMarkerCensusOperationKey(OperationKey markerCensusOperationKey) {
		this.markerCensusOperationKey = markerCensusOperationKey;
	}

	@Override
	public Collection<HardyWeinbergOperationEntry> getEntriesControl() throws IOException {
		return getEntries(Category.CONTROL, -1, -1);
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

		final int maxSingleWriteBufferSize = getEntriesWriteBufferSize() / writeBuffers.size();
		if (buffer.getEntries().size() >= maxSingleWriteBufferSize) {
			buffer.setAlreadyWritten(writeEntriesBuffer(buffer.getAlreadyWritten(), buffer.getEntries(), entry.getCategory().name() + " "));
//			writeEntries(buffer.getAlreadyWritten(), buffer.getEntries());
//			buffer.setAlreadyWritten(buffer.getAlreadyWritten() + buffer.getEntries().size());
//			buffer.getEntries().clear();
		}
	}

	@Override
	public void finnishWriting() throws IOException {

		for (Map.Entry<HardyWeinbergOperationEntry.Category, EntryBuffer<HardyWeinbergOperationEntry>> writeBuffer : writeBuffers.entrySet()) {
			final HardyWeinbergOperationEntry.Category category = writeBuffer.getKey();
			final EntryBuffer<HardyWeinbergOperationEntry> buffer = writeBuffer.getValue();
			buffer.setAlreadyWritten(writeEntriesBuffer(buffer.getAlreadyWritten(), buffer.getEntries(), category.name() + " "));
		}

		super.finnishWriting();
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<HardyWeinbergOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten};
		if (netCdfPs == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfPs = new ArrayDouble.D1(writeBuffer.size());
			netCdfObsHetzys = new ArrayDouble.D1(writeBuffer.size());
			netCdfExpHetzys = new ArrayDouble.D1(writeBuffer.size());
		} else if (writeBuffer.size() < netCdfPs.getShape()[0]) {
			// we end up here at the end of the processing, if, for example,
			// we have a buffer size of 10, but only 7 items are left to be written
			List<Range> reducedRange1D = new ArrayList<Range>(1);
			reducedRange1D.add(new Range(writeBuffer.size()));
			try {
				netCdfPs = (ArrayDouble.D1) netCdfPs.sectionNoReduce(reducedRange1D);
				netCdfObsHetzys = (ArrayDouble.D1) netCdfObsHetzys.sectionNoReduce(reducedRange1D);
				netCdfExpHetzys = (ArrayDouble.D1) netCdfExpHetzys.sectionNoReduce(reducedRange1D);
			} catch (InvalidRangeException ex) {
				throw new IOException(ex);
			}
		}
		int index = 0;
		for (HardyWeinbergOperationEntry entry : writeBuffer) {
			netCdfPs.setDouble(netCdfPs.getIndex().set(index), entry.getP());
			netCdfObsHetzys.setDouble(netCdfObsHetzys.getIndex().set(index), entry.getObsHzy());
			netCdfExpHetzys.setDouble(netCdfExpHetzys.getIndex().set(index), entry.getExpHzy());
			index++;
		}
		HardyWeinbergOperationEntry.Category category
				= writeBuffer.element().getCategory();
		String varP = netCdfPVars.get(category);
		String varObsHtz = netCdfHetzyObsVars.get(category);
		String varExpHtz = netCdfHetzyExpVars.get(category);
		try {
			getNetCdfWriteFile().write(varP, origin, netCdfPs);
			getNetCdfWriteFile().write(varObsHtz, origin, netCdfObsHetzys);
			getNetCdfWriteFile().write(varExpHtz, origin, netCdfExpHetzys);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public Collection<Double> getPs(Category category, int from, int to) throws IOException {

		String varP = netCdfPVars.get(category);
		Collection<Double> values = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), varP, from, to, values, null);

		return values;
	}

	@Override
	public Collection<Double> getHwHetzyObses(Category category, int from, int to) throws IOException {

		String varObsHtz = netCdfHetzyObsVars.get(category);
		Collection<Double> values = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), varObsHtz, from, to, values, null);

		return values;
	}

	@Override
	public Collection<Double> getHwHetzyExps(Category category, int from, int to) throws IOException {

		String varExpHtz = netCdfHetzyExpVars.get(category);
		Collection<Double> values = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), varExpHtz, from, to, values, null);

		return values;
	}

	@Override
	public Collection<HardyWeinbergOperationEntry> getEntries(int from, int to) throws IOException {
		return getEntries(HardyWeinbergOperationEntry.Category.ALL, from, to);
	}

	public Collection<HardyWeinbergOperationEntry> getEntries(Category category, int from, int to) throws IOException {

		Map<Integer, MarkerKey> markersKeys = getMarkersKeysSource().getIndicesMap(from, to);
		Collection<Double> ps = getPs(category, from, to);
		Collection<Double> hwObsHetzys = getHwHetzyObses(category, from, to);
		Collection<Double> hwExpHetzys = getHwHetzyExps(category, from, to);

		Collection<HardyWeinbergOperationEntry> entries
				= new ArrayList<HardyWeinbergOperationEntry>(ps.size());
		Iterator<Double> psIt = ps.iterator();
		Iterator<Double> hwObsHetzysIt = hwObsHetzys.iterator();
		Iterator<Double> hwExpHetzysIt = hwExpHetzys.iterator();
		for (Map.Entry<Integer, MarkerKey> origIndicesAndKey : markersKeys.entrySet()) {
			entries.add(new DefaultHardyWeinbergOperationEntry(
					origIndicesAndKey.getValue(),
					origIndicesAndKey.getKey(),
					category,
					psIt.next(),
					hwObsHetzysIt.next(),
					hwExpHetzysIt.next()));
		}

		return entries;
	}
}

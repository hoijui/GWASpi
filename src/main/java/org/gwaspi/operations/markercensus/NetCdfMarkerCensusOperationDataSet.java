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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.Census;
import org.gwaspi.model.CensusFull;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.NetCdfUtils;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class NetCdfMarkerCensusOperationDataSet
		extends AbstractNetCdfOperationDataSet<MarkerCensusOperationEntry>
		implements MarkerCensusOperationDataSet
{

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - Variables.VAR_ALLELES: known alleles [Collection<byte[]>]
	// - Census.VAR_OP_MARKERS_CENSUSALL: marker census - all [Collection<Census.all<== int[]>>]
	// - Census.VAR_OP_MARKERS_CENSUSCASE: marker census - case [Collection<Census.case>]
	// - Census.VAR_OP_MARKERS_CENSUSCTRL: marker census - control [Collection<Census.control>]
	// - Census.VAR_OP_MARKERS_CENSUSHW: marker census - alternate hardy-weinberg [Collection<Census.altHW>]

	private static final Map<Category, String> categoryNetCdfVarName = new EnumMap<Category, String>(Category.class);
	static {
		categoryNetCdfVarName.put(Category.ALL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
		categoryNetCdfVarName.put(Category.CASE, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
		categoryNetCdfVarName.put(Category.CONTROL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
		categoryNetCdfVarName.put(Category.ALTERNATE, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW);
	}

	private ArrayByte.D2 netCdfKnownAlleles;
	private ArrayInt.D2 netCdfCensusAlls;
	private ArrayInt.D2 netCdfCensusCase;
	private ArrayInt.D2 netCdfCensusCtrl;
	private ArrayInt.D2 netCdfCensusAlt;

	public NetCdfMarkerCensusOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(true, origin, parent, operationKey);
	}

	public NetCdfMarkerCensusOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return MarkerCensusOperationFactory.OPERATION_TYPE_INFO;
	}

	/**
	 * Was used in the ctor before!
	 * @see #getDefaultEntriesWriteBufferSize(boolean)
	 */
	private static int getDefaultEntriesWriteBufferSize() {

		int chunkSize = Math.round((float)org.gwaspi.gui.StartGWASpi.maxProcessMarkers / 4);
		if (chunkSize > 500000) {
			chunkSize = 500000; // We want to keep things manageable for RAM
		}
		if (chunkSize < 10000 && org.gwaspi.gui.StartGWASpi.maxProcessMarkers > 10000) {
			chunkSize = 10000; // But keep Map size sensible
		}

		return chunkSize;
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
		final int gtStride = cNetCDF.Strides.STRIDE_GT;

		// dimensions
		Dimension markersDim = markersSpace.get(0);
		Dimension boxes3Dim = ncFile.addDimension(cNetCDF.Dimensions.DIM_3BOXES, 3);
		Dimension boxes4Dim = ncFile.addDimension(cNetCDF.Dimensions.DIM_4BOXES, 4);
		Dimension gtStrideDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_GTSTRIDE, gtStride);
		Dimension dim4 = ncFile.addDimension(cNetCDF.Dimensions.DIM_4, 4);

		// OP SPACES
		List<Dimension> markers3Space = new ArrayList<Dimension>(2);
		markers3Space.add(markersDim);
		markers3Space.add(boxes3Dim);

		List<Dimension> markers4Space = new ArrayList<Dimension>(2);
		markers4Space.add(markersDim);
		markers4Space.add(boxes4Dim);

		// MARKER SPACES
		List<Dimension> markersPropertySpace4 = new ArrayList<Dimension>(2);
		markersPropertySpace4.add(markersDim);
		markersPropertySpace4.add(dim4);

		// ALLELES SPACES
		List<Dimension> allelesSpace = new ArrayList<Dimension>(2);
		allelesSpace.add(markersDim);
		allelesSpace.add(gtStrideDim);

		// Define OP Variables
//		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL_IDX, DataType.INT, markersSpace);
//		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE_IDX, DataType.INT, markersSpace);
//		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL_IDX, DataType.INT, markersSpace);
//		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW_IDX, DataType.INT, markersSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, DataType.INT, markers4Space);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE, DataType.INT, markers3Space);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL, DataType.INT, markers3Space);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW, DataType.INT, markers3Space);

		// Define Genotype Variables
		ncFile.addVariable(cNetCDF.Variables.VAR_ALLELES, DataType.BYTE, allelesSpace);
		ncFile.addVariable(cNetCDF.Variables.VAR_GT_STRAND, DataType.CHAR, markersPropertySpace4);
	}

	@Override
	public List<byte[]> getKnownAlleles() throws IOException {
		return getKnownAlleles(-1, -1);
	}

//	@Override
////	public void setMarkerCensusAll(Collection<Census> markerCensusAll) throws IOException {
////		NetCdfUtils.saveIntMapD2ToWrMatrix(getNetCdfWriteFile(), markerCensusAll, Census.EXTRACTOR_ALL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
////	}
//	public void setMarkerCensus(Collection<CensusFull> markerCensus) throws IOException {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}

	@Override
	public List<byte[]> getKnownAlleles(int from, int to) throws IOException {

		List<byte[]> knownAlleles = new ArrayList<byte[]>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Variables.VAR_ALLELES, from, to, knownAlleles, null);

		return knownAlleles;
	}

//	public List<Integer> getCensusMarkerIndices(Category category, int from, int to) throws IOException {
//
//		List<Integer> categoryCensusOrigIndices = new ArrayList<Integer>(0);
//		NetCdfUtils.readVariable(getNetCdfReadFile(), categoryNetCdfVarIdx.get(category), from, to, categoryCensusOrigIndices, null);
//
//		return categoryCensusOrigIndices;
//	}
//
//	public List<Integer> getCensusMarkerIndices(Category category) throws IOException {
//		return getCensusMarkerIndices(category, -1, -1);
//	}

	private List<Census> getCensusMarkerData(Category category, int from, int to) throws IOException {

		List<int[]> censusesRaw = new ArrayList<int[]>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), categoryNetCdfVarName.get(category), from, to, censusesRaw, null);

		List<Census> censusesData = new ArrayList<Census>(censusesRaw.size());
		for (int[] censusRaw : censusesRaw) {
			censusesData.add(new Census(censusRaw));
		}

		return censusesData;
	}

	private List<Census> getCensusMarkerData(Category category) throws IOException {
		return getCensusMarkerData(category, -1, -1);
	}

	@Override
	public List<Census> getCensus(Category category) throws IOException {
		return getCensus(category, -1, -1);
	}

	@Override
//	public Map<Integer, Census> getCensus(Category category, int from, int to) throws IOException {
	public List<Census> getCensus(Category category, int from, int to) throws IOException {

//		List<Integer> categoryCensusOrigIndices = getCensusMarkerIndices(category, from, to);
		List<Census> censusesData = getCensusMarkerData(category, from, to);
		return censusesData;

//		Map<Integer, Census> censuses = new LinkedHashMap<Integer, Census>(censusesData.size());
//		Iterator<Integer> categoryCensusOrigIndicesIt = categoryCensusOrigIndices.iterator();
//		for (Census censusData : censusesData) {
//			censuses.put(categoryCensusOrigIndicesIt.next(), censusData);
//		}
//
//		return censuses;
	}

	@Override
	public List<MarkerCensusOperationEntry> getEntries(int from, int to) throws IOException {

		Map<Integer, MarkerKey> markersKeys = getMarkersKeysSource().getIndicesMap(from, to);
		List<byte[]> knownAlleles = getKnownAlleles(from, to);
//		Collection<Census> censusesAll = getCensus(Category.ALL, from, to);
//		Collection<Census> censusesCase = getCensus(Category.CASE, from, to);
//		Map<Integer, Census> censusesControl = getCensus(Category.CONTROL, from, to);
//		Map<Integer, Census> censusesAlternate = getCensus(Category.ALTERNATE, from, to);
		List<Census> censusesControl = getCensus(Category.CONTROL, from, to);
		List<Census> censusesAlternate = getCensus(Category.ALTERNATE, from, to);

		List<MarkerCensusOperationEntry> entries
				= new ArrayList<MarkerCensusOperationEntry>(knownAlleles.size());
		Iterator<byte[]> knownAllelesIt = knownAlleles.iterator();
//		Iterator<Census> censusesControlIt = censusesControl.values().iterator();
//		Iterator<Census> censusesAlternateIt = censusesAlternate.values().iterator();
		Iterator<Census> censusesControlIt = censusesControl.iterator();
		Iterator<Census> censusesAlternateIt = censusesAlternate.iterator();
		for (Map.Entry<Integer, MarkerKey> origIndicesAndKey : markersKeys.entrySet()) {
			Integer origIndex = origIndicesAndKey.getKey();
			entries.add(new DefaultMarkerCensusOperationEntry(
					origIndicesAndKey.getValue(),
					origIndex,
					knownAllelesIt.next(),
					new CensusFull(
							null, // XXX
							null, // XXX
							censusesControlIt.next(),
							censusesAlternateIt.next())));
		}

		return entries;
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<MarkerCensusOperationEntry> writeBuffer) throws IOException {

		final int[] origin = new int[] {alreadyWritten, 0};
		if (netCdfCensusAlls == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfKnownAlleles = new ArrayByte.D2(writeBuffer.size(), cNetCDF.Strides.STRIDE_GT);
			netCdfCensusAlls = new ArrayInt.D2(writeBuffer.size(), 4);
			netCdfCensusCase = new ArrayInt.D2(writeBuffer.size(), 3);
			netCdfCensusCtrl = new ArrayInt.D2(writeBuffer.size(), 3);
			netCdfCensusAlt = new ArrayInt.D2(writeBuffer.size(), 3);
		} else if (writeBuffer.size() < netCdfKnownAlleles.getShape()[0]) {
			// we end up here at the end of the processing, if, for example,
			// we have a buffer size of 10, but only 7 items are left to be written
			List<Range> reducedRange2D = new ArrayList<Range>(2);
			reducedRange2D.add(new Range(writeBuffer.size()));
			reducedRange2D.add(null); // use full range
			try {
				netCdfKnownAlleles = (ArrayByte.D2) netCdfKnownAlleles.sectionNoReduce(reducedRange2D);
				netCdfCensusAlls = (ArrayInt.D2) netCdfCensusAlls.sectionNoReduce(reducedRange2D);
				netCdfCensusCase = (ArrayInt.D2) netCdfCensusCase.sectionNoReduce(reducedRange2D);
				netCdfCensusCtrl = (ArrayInt.D2) netCdfCensusCtrl.sectionNoReduce(reducedRange2D);
				netCdfCensusAlt = (ArrayInt.D2) netCdfCensusAlt.sectionNoReduce(reducedRange2D);
			} catch (InvalidRangeException ex) {
				throw new IOException(ex);
			}
		}
		int index = 0;
		for (MarkerCensusOperationEntry entry : writeBuffer) {
			Index indexObj;

			byte[] knownAlleles = entry.getKnownAlleles();
			indexObj = netCdfKnownAlleles.getIndex().set(index);
			netCdfKnownAlleles.setInt(indexObj.set(index, 0), knownAlleles[0]);
			netCdfKnownAlleles.setInt(indexObj.set(index, 1), knownAlleles[1]);

			Census censusAll = entry.getCensus().getCategoryCensus().get(Category.ALL);
			indexObj = netCdfCensusAlls.getIndex().set(index);
			netCdfCensusAlls.setInt(indexObj.set(index, 0), censusAll.getAA());
			netCdfCensusAlls.setInt(indexObj.set(index, 1), censusAll.getAa());
			netCdfCensusAlls.setInt(indexObj.set(index, 2), censusAll.getaa());
			netCdfCensusAlls.setInt(indexObj.set(index, 3), censusAll.getMissingCount());

			{
				final Census census = entry.getCensus().getCategoryCensus().get(Category.CASE);
				indexObj = netCdfCensusCase.getIndex().set(index);
				netCdfCensusCase.setInt(indexObj.set(index, 0), census.getAA());
				netCdfCensusCase.setInt(indexObj.set(index, 1), census.getAa());
				netCdfCensusCase.setInt(indexObj.set(index, 2), census.getaa());
			}
			{
				final Census census = entry.getCensus().getCategoryCensus().get(Category.CONTROL);
				indexObj = netCdfCensusCtrl.getIndex().set(index);
				netCdfCensusCtrl.setInt(indexObj.set(index, 0), census.getAA());
				netCdfCensusCtrl.setInt(indexObj.set(index, 1), census.getAa());
				netCdfCensusCtrl.setInt(indexObj.set(index, 2), census.getaa());
			}
			{
				final Census census = entry.getCensus().getCategoryCensus().get(Category.ALTERNATE);
				indexObj = netCdfCensusAlt.getIndex().set(index);
				netCdfCensusAlt.setInt(indexObj.set(index, 0), census.getAA());
				netCdfCensusAlt.setInt(indexObj.set(index, 1), census.getAa());
				netCdfCensusAlt.setInt(indexObj.set(index, 2), census.getaa());
			}
			index++;
		}
		try {
			getNetCdfWriteFile().write(cNetCDF.Variables.VAR_ALLELES, origin, netCdfKnownAlleles);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, origin, netCdfCensusAlls);
			getNetCdfWriteFile().write(categoryNetCdfVarName.get(Category.CASE), origin, netCdfCensusCase);
			getNetCdfWriteFile().write(categoryNetCdfVarName.get(Category.CONTROL), origin, netCdfCensusCtrl);
			getNetCdfWriteFile().write(categoryNetCdfVarName.get(Category.ALTERNATE), origin, netCdfCensusAlt);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}
}

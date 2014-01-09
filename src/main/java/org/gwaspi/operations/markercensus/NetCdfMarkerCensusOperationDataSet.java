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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Census;
import org.gwaspi.model.CensusFull;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class NetCdfMarkerCensusOperationDataSet extends AbstractNetCdfOperationDataSet<MarkerCensusOperationEntry> implements MarkerCensusOperationDataSet {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - Variables.VAR_ALLELES: known alleles [Collection<byte[]>]
	// - Census.VAR_OP_MARKERS_CENSUSALL: marker census - all [Collection<Census.all<== int[]>>]
	// - Census.VAR_OP_MARKERS_CENSUSCASE: marker census - case [Collection<Census.case>]
	// - Census.VAR_OP_MARKERS_CENSUSCTRL: marker census - control [Collection<Census.control>]
	// - Census.VAR_OP_MARKERS_CENSUSHW: marker census - alternate hardy-weinberg [Collection<Census.altHW>]

	private String censusName;
	private File phenoFile;
	private double sampleMissingRatio;
	private double sampleHetzygRatio;
	private double markerMissingRatio;
	private boolean discardMismatches;
	private ArrayByte.D2 netCdfKnownAlleles;
	private ArrayInt.D2 netCdfCensusAlls;
	private ArrayInt.D2 netCdfCensusesRest;

	public NetCdfMarkerCensusOperationDataSet(OperationKey operationKey) {
		super(true, operationKey, calculateEntriesWriteBufferSize());
	}

	public NetCdfMarkerCensusOperationDataSet() {
		this(null);
	}

	private static int calculateEntriesWriteBufferSize() {

		int chunkSize = Math.round((float)org.gwaspi.gui.StartGWASpi.maxProcessMarkers / 4);
		if (chunkSize > 500000) {
			chunkSize = 500000; // We want to keep things manageable for RAM
		}
		if (chunkSize < 10000 && org.gwaspi.gui.StartGWASpi.maxProcessMarkers > 10000) {
			chunkSize = 10000; // But keep Map size sensible
		}

		return chunkSize;
	}

	public void setPhenoFile(File phenoFile) {
		this.phenoFile = phenoFile;
	}

	public void setCensusName(String censusName) {
		this.censusName = censusName;
	}

	public void setSampleMissingRatio(double sampleMissingRatio) {
		this.sampleMissingRatio = sampleMissingRatio;
	}

	public void setSampleHetzygRatio(double sampleHetzygRatio) {
		this.sampleHetzygRatio = sampleHetzygRatio;
	}

	public void setMarkerMissingRatio(double markerMissingRatio) {
		this.markerMissingRatio = markerMissingRatio;
	}

	public void setDiscardMismatches(boolean discardMismatches) {
		this.discardMismatches = discardMismatches;
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
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, DataType.INT, markers4Space);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE, DataType.INT, markers3Space);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL, DataType.INT, markers3Space);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW, DataType.INT, markers3Space);

		// Define Genotype Variables
		ncFile.addVariable(cNetCDF.Variables.VAR_ALLELES, DataType.CHAR, allelesSpace);
		ncFile.addVariable(cNetCDF.Variables.VAR_GT_STRAND, DataType.CHAR, markersPropertySpace4);
	}

	@Override
	protected OperationMetadata createOperationMetadata() throws IOException {

		MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(getReadMatrixKey());

		OPType opType = OPType.MARKER_CENSUS_BY_AFFECTION;

		String description = "Genotype frequency count -" + censusName + "- on " + rdMatrixMetadata.getFriendlyName();
		if (phenoFile != null) {
			description += "\nCase/Control status read from file: " + phenoFile.getPath();
			opType = OPType.MARKER_CENSUS_BY_PHENOTYPE;
		}

		return new OperationMetadata(
				getReadMatrixKey(), // parent matrix
				OperationKey.NULL_ID, // parent operation ID
				"Genotypes freq. - " + censusName, // friendly name
				description
					+ "\nSample missing ratio threshold: " + sampleMissingRatio
					+ "\nSample heterozygosity ratio threshold: " + sampleHetzygRatio
					+ "\nMarker missing ratio threshold: " + markerMissingRatio
					+ "\nDiscard mismatching Markers: " + discardMismatches
					+ "\nMarkers: " + getNumMarkers()
					+ "\nSamples: " + getNumSamples(), // description
				opType,
				getNumMarkers(),
				getNumSamples(),
				getNumChromosomes());
	}

//	@Override
////	public void setMarkerCensusAll(Collection<Census> markerCensusAll) throws IOException {
////		NetCdfUtils.saveIntMapD2ToWrMatrix(getNetCdfWriteFile(), markerCensusAll, Census.EXTRACTOR_ALL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
////	}
//	public void setMarkerCensus(Collection<CensusFull> markerCensus) throws IOException {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}

	@Override
	public Collection<byte[]> getKnownAlleles(int from, int to) throws IOException {

		Collection<byte[]> knownAlleles = new ArrayList<byte[]>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Variables.VAR_ALLELES, from, to, knownAlleles, null);

		return knownAlleles;
	}

	public Collection<Integer> getCensusMarkerIndices(Category category, int from, int to) throws IOException {

		Map<Category, String> categoryNetCdfVarIdx = new EnumMap<Category, String>(Category.class);
		categoryNetCdfVarIdx.put(Category.ALL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL_IDX);
		categoryNetCdfVarIdx.put(Category.CASE, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE_IDX);
		categoryNetCdfVarIdx.put(Category.CONTROL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL_IDX);
		categoryNetCdfVarIdx.put(Category.ALTERNATE, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW_IDX);

		Collection<Integer> categoryCensusOrigIndices = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), categoryNetCdfVarIdx.get(category), from, to, categoryCensusOrigIndices, null);

		return categoryCensusOrigIndices;
	}

	public Collection<Integer> getCensusMarkerIndices(Category category) throws IOException {
		return getCensusMarkerIndices(category, -1, -1);
	}

	@Override
	public Map<Integer, Census> getCensus(Category category, int from, int to) throws IOException {

		Map<Category, String> categoryNetCdfVarName = new EnumMap<Category, String>(Category.class);
		categoryNetCdfVarName.put(Category.ALL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
		categoryNetCdfVarName.put(Category.CASE, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
		categoryNetCdfVarName.put(Category.CONTROL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
		categoryNetCdfVarName.put(Category.ALTERNATE, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW);

		Collection<Integer> categoryCensusOrigIndices = getCensusMarkerIndices(category, from, to);

		Collection<int[]> censusesRaw = new ArrayList<int[]>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), categoryNetCdfVarName.get(category), from, to, censusesRaw, null);

		Map<Integer, Census> censuses = new LinkedHashMap<Integer, Census>(censusesRaw.size());
		Iterator<Integer> categoryCensusOrigIndicesIt = categoryCensusOrigIndices.iterator();
		for (int[] censusRaw : censusesRaw) {
			censuses.put(categoryCensusOrigIndicesIt.next(), new Census(censusRaw));
		}

		return censuses;
	}

	@Override
	public Collection<MarkerCensusOperationEntry> getEntries(int from, int to) throws IOException {

		Map<Integer, MarkerKey> markersKeys = getMarkersKeysSource().getIndicesMap(from, to);
		Collection<byte[]> knownAlleles = getKnownAlleles(from, to);
//		Collection<Census> censusesAll = getCensus(Category.ALL, from, to);
//		Collection<Census> censusesCase = getCensus(Category.CASE, from, to);
		Map<Integer, Census> censusesControl = getCensus(Category.CONTROL, from, to);
		Map<Integer, Census> censusesAlternate = getCensus(Category.ALTERNATE, from, to);

		Collection<MarkerCensusOperationEntry> entries
				= new ArrayList<MarkerCensusOperationEntry>(knownAlleles.size());
		Iterator<byte[]> knownAllelesIt = knownAlleles.iterator();
		for (Map.Entry<Integer, MarkerKey> origIndicesAndKey : markersKeys.entrySet()) {
			Integer origIndex = origIndicesAndKey.getKey();
			entries.add(new DefaultMarkerCensusOperationEntry(
					origIndicesAndKey.getValue(),
					origIndex,
					knownAllelesIt.next(),
					new CensusFull(
							null, // XXX
							null, // XXX
							censusesControl.get(origIndex),
							censusesAlternate.get(origIndex))));
		}

		return entries;
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<MarkerCensusOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten, 0};
		if (netCdfCensusAlls == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfKnownAlleles = new ArrayByte.D2(writeBuffer.size(), cNetCDF.Strides.STRIDE_GT);
			netCdfCensusAlls = new ArrayInt.D2(writeBuffer.size(), 4);
			netCdfCensusesRest = new ArrayInt.D2(writeBuffer.size(), 3);
		}
		int index = 0;
		try {
			for (MarkerCensusOperationEntry entry : writeBuffer) {
				Index indexObj;

				byte[] knownAlleles = entry.getKnownAlleles();
				indexObj = netCdfKnownAlleles.getIndex().set(index);
				netCdfKnownAlleles.setInt(indexObj.set(index, 0), knownAlleles[0]);
				netCdfKnownAlleles.setInt(indexObj.set(index, 1), knownAlleles[1]);
				getNetCdfWriteFile().write(cNetCDF.Variables.VAR_ALLELES, origin, netCdfKnownAlleles);

				Census censusAll = entry.getCensus().getCategoryCensus().get(Category.ALL);
				indexObj = netCdfCensusAlls.getIndex().set(index);
				netCdfCensusAlls.setInt(indexObj.set(index, 0), censusAll.getAA());
				netCdfCensusAlls.setInt(indexObj.set(index, 1), censusAll.getAa());
				netCdfCensusAlls.setInt(indexObj.set(index, 2), censusAll.getaa());
				netCdfCensusAlls.setInt(indexObj.set(index, 3), censusAll.getMissingCount());
				getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, origin, netCdfCensusAlls);

				Map<Category, String> categoryNetCdfVarName = new EnumMap<Category, String>(Category.class);
				categoryNetCdfVarName.put(Category.CASE, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
				categoryNetCdfVarName.put(Category.CONTROL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
				categoryNetCdfVarName.put(Category.ALTERNATE, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW);
				for (Map.Entry<Category, String> censusEntry : categoryNetCdfVarName.entrySet()) {
					Census census = entry.getCensus().getCategoryCensus().get(censusEntry.getKey());
					indexObj = netCdfCensusesRest.getIndex().set(index);
					netCdfCensusesRest.setInt(indexObj.set(index, 0), census.getAA());
					netCdfCensusesRest.setInt(indexObj.set(index, 1), census.getAa());
					netCdfCensusesRest.setInt(indexObj.set(index, 2), census.getaa());
					getNetCdfWriteFile().write(censusEntry.getValue(), origin, netCdfCensusesRest);
				}
				index++;
			}
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}
}

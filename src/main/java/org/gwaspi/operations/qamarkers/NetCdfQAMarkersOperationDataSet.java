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

package org.gwaspi.operations.qamarkers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Census;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class NetCdfQAMarkersOperationDataSet extends AbstractNetCdfOperationDataSet<QAMarkersOperationEntry> implements QAMarkersOperationDataSet {

	// - cNetCDF.Variables.VAR_OPSET: (String, key.getId()) marker keys
	// - cNetCDF.Variables.VAR_MARKERS_RSID: (String) marker RS-IDs
	// - cNetCDF.Variables.VAR_IMPLICITSET: (String, key.getSampleId() + " " + key.getFamilyId()) sample keys
	// - cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT: (double) missing ratio for each marker
	// - cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE: (int -> boolean (0->false->no_mismach, 1->true->there_is_mismatch)) mismatch state for each marker
	// - cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES: (byte) dictionary allele 1 for each marker
	// - cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ: (double) frequency of dictionary allele 1 in all the alleles for any given marker
	// - cNetCDF.Census.VAR_OP_MARKERS_MINALLELES: (byte) dictionary allele 2 for each marker
	// - cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ: (double) frequency of dictionary allele 2 in all the alleles for any given marker
	// - cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL: ({int, int, int, int}) allele-AA, allele-Aa, allele-aa, missing-count for each marker

	private ArrayByte.D1 netCdfMajorAlleles;
	private ArrayDouble.D1 netCdfMajorAllelesFrequencies;
	private ArrayByte.D1 netCdfMinorAlleles;
	private ArrayDouble.D1 netCdfMinorAllelesFrequencies;
	private ArrayInt.D2 netCdfCensusAlls;

	public NetCdfQAMarkersOperationDataSet(OperationKey operationKey) {
		super(true, operationKey);
	}

	public NetCdfQAMarkersOperationDataSet() {
		this(null);
	}

	@Override
	public NetcdfFileWriteable generateNetCdfHandler(
			StudyKey studyKey,
			String resultOPName,
			String description,
			OPType opType,
			int markerSetSize,
			int sampleSetSize,
			int chrSetSize)
			throws IOException
	{
		File pathToStudy = new File(Study.constructGTPath(studyKey));
		if (!pathToStudy.exists()) {
			org.gwaspi.global.Utils.createFolder(pathToStudy);
		}

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
		int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

		File writeFile = new File(pathToStudy, resultOPName + ".nc");
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFile.getAbsolutePath(), false);

		// global attributes
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyKey.getId());
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);

		// dimensions
		Dimension markerSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, markerSetSize);
		Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, sampleSetSize);
		Dimension boxes4Dim = ncfile.addDimension(cNetCDF.Dimensions.DIM_4BOXES, 4);
		Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
		Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
		Dimension alleleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_GTSTRIDE, gtStride / 2);
		Dimension dim4 = ncfile.addDimension(cNetCDF.Dimensions.DIM_4, 4);

		// OP SPACES
		List<Dimension> OP1Space = new ArrayList<Dimension>();
		OP1Space.add(markerSetDim);

		List<Dimension> OP2x4Space = new ArrayList<Dimension>();
		OP2x4Space.add(markerSetDim);
		OP2x4Space.add(boxes4Dim);

		// MARKER SPACES
		List<Dimension> markerNameSpace = new ArrayList<Dimension>();
		markerNameSpace.add(markerSetDim);
		markerNameSpace.add(markerStrideDim);

		List<Dimension> markerPropertySpace4 = new ArrayList<Dimension>();
		markerPropertySpace4.add(markerSetDim);
		markerPropertySpace4.add(dim4);

		// SAMPLE SPACES
		List<Dimension> sampleSetSpace = new ArrayList<Dimension>();
		sampleSetSpace.add(implicitSetDim);
		sampleSetSpace.add(sampleStrideDim);

		// ALLELES SPACES
		List<Dimension> allelesSpace = new ArrayList<Dimension>();
		allelesSpace.add(markerSetDim);
		allelesSpace.add(alleleStrideDim);

		// Define OP Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_OPSET, DataType.CHAR, markerNameSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markerNameSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET, DataType.CHAR, sampleSetSpace);
		ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, DataType.INT, OP2x4Space);
		ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT, DataType.DOUBLE, OP1Space);
		ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE, DataType.INT, OP1Space);
		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, markerSetSize);

		// Define Genotype Variables
		//ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_KNOWNALLELES, DataType.CHAR, allelesSpace);
		ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, DataType.BYTE, allelesSpace);
		ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ, DataType.DOUBLE, OP1Space);
		ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, DataType.BYTE, allelesSpace);
		ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ, DataType.DOUBLE, OP1Space);
		ncfile.addVariable(cNetCDF.Variables.VAR_GT_STRAND, DataType.CHAR, markerPropertySpace4);

		return ncfile;
	}

	@Override
	protected OperationFactory createOperationFactory() throws IOException {

		MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(getReadMatrixKey());

		String description = "Marker Quality Assurance on "
				+ rdMatrixMetadata.getFriendlyName()
				+ "\nMarkers: " + getNumMarkers()
				+ "\nStarted at: " + org.gwaspi.global.Utils.getShortDateTimeAsString();
		return new OperationFactory(
				rdMatrixMetadata.getStudyKey(),
				"Marker QA", // friendly name
				description, // description
				getNumMarkers(),
				getNumSamples(),
				0,
				OPType.MARKER_QA,
				getReadMatrixKey(), // Parent matrixId
				-1); // Parent operationId
	}

	@Override
	public void setMarkerMissingRatios(Collection<Double> markerMissingRatios) throws IOException {

		ensureNcFile();
		NetCdfUtils.saveDoubleMapD1ToWrMatrix(getNetCdfWriteFile(), markerMissingRatios, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);
	}

	@Override
	public void setMarkerMismatchStates(Collection<Boolean> markerMismatchStates) throws IOException {

//		Collection<Integer> markerMismatchIntegerStates
//				= new ArrayList<Integer>(markerMismatchStates.size()); // XXX not sooooo nice! maybe use a converter while writing (saves memory)
//		for (boolean mismatch : markerMismatchStates) {
//			markerMismatchIntegerStates.add(mismatch
//					? cNetCDF.Defaults.DEFAULT_MISMATCH_YES
//					: cNetCDF.Defaults.DEFAULT_MISMATCH_NO);
//		}
		ensureNcFile();
		NetCdfUtils.saveBooleansD1ToWrMatrix(getNetCdfWriteFile(), markerMismatchStates, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);
	}

	@Override
	public void setMarkerKnownAlleles(Collection<OrderedAlleles> markerKnownAlleles) throws IOException {

		ensureNcFile();
		//Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, cNetCDF.Census.VAR_OP_MARKERS_KNOWNALLELES, cNetCDF.Strides.STRIDE_GT);
		NetCdfUtils.saveByteMapItemToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, OrderedAlleles.TO_ALLELE_1, cNetCDF.Strides.STRIDE_GT / 2);
		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, OrderedAlleles.TO_ALLELE_1_FREQ, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);
		NetCdfUtils.saveByteMapItemToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, OrderedAlleles.TO_ALLELE_2, cNetCDF.Strides.STRIDE_GT / 2);
		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, OrderedAlleles.TO_ALLELE_2_FREQ, cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);
	}

	@Override
	public void setMarkerCensusAll(Collection<Census> markerCensusAll) throws IOException {

		ensureNcFile();
		NetCdfUtils.saveIntMapD2ToWrMatrix(getNetCdfWriteFile(), markerCensusAll, Census.EXTRACTOR_4, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
	}

	@Override
	public Collection<Boolean> getMismatchStates(int from, int to) throws IOException {

		Collection<Boolean> mismatchStates = new ArrayList<Boolean>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE, from, to, mismatchStates, null);

		return mismatchStates;
	}

	@Override
	public Collection<Double> getMissingRatio(int from, int to) throws IOException {

		Collection<Double> missingRatios = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT, from, to, missingRatios, null);

		return missingRatios;
	}

	@Override
	public Collection<Byte> getKnownMajorAllele(int from, int to) throws IOException {

		Collection<Byte> knownMajorAllele = new ArrayList<Byte>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, from, to, knownMajorAllele, null);

		return knownMajorAllele;
	}

	@Override
	public Collection<Double> getKnownMajorAlleleFrequencies(int from, int to) throws IOException {

		Collection<Double> knownMajorAlleleFreq = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ, from, to, knownMajorAlleleFreq, null);

		return knownMajorAlleleFreq;
	}

	@Override
	public Collection<Byte> getKnownMinorAllele(int from, int to) throws IOException {

		Collection<Byte> knownMinorAllele = new ArrayList<Byte>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, from, to, knownMinorAllele, null);

		return knownMinorAllele;
	}

	@Override
	public Collection<Double> getKnownMinorAlleleFrequencies(int from, int to) throws IOException {

		Collection<Double> knownMinorAlleleFreq = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ, from, to, knownMinorAlleleFreq, null);

		return knownMinorAlleleFreq;
	}

	@Override
	public Collection<int[]> getCensusAll(int from, int to) throws IOException {

		Collection<int[]> censusAll = new ArrayList<int[]>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, from, to, censusAll, null);

		return censusAll;
	}

	@Override
	public Collection<QAMarkersOperationEntry> getEntries(int from, int to) throws IOException {

		MarkerOperationSet rdMarkersSet = new MarkerOperationSet(getOperationKey(), from, to);
		Map<MarkerKey, Integer> rdMarkers = rdMarkersSet.getOpSetMap();

		Collection<Double> missingRatios = getMissingRatio(from, to);
		Collection<Boolean> mismatchStates = getMismatchStates(from, to);

		Collection<Byte> knownMajorAllele = getKnownMajorAllele(from, to);
		Collection<Double> knownMajorAlleleFreq = getKnownMajorAlleleFrequencies(from, to);
		Collection<Byte> knownMinorAllele = getKnownMinorAllele(from, to);
		Collection<Double> knownMinorAlleleFreq = getKnownMinorAlleleFrequencies(from, to);
		Collection<int[]> censusAll = getCensusAll(from, to);

		Collection<QAMarkersOperationEntry> entries
				= new ArrayList<QAMarkersOperationEntry>(missingRatios.size());
		Iterator<Double> missingRatioIt = missingRatios.iterator();
		Iterator<Boolean> mismatchStatesIt = mismatchStates.iterator();
		Iterator<Byte> knownMajorAlleleIt = knownMajorAllele.iterator();
		Iterator<Double> knownMajorAlleleFreqIt = knownMajorAlleleFreq.iterator();
		Iterator<Byte> knownMinorAlleleIt = knownMinorAllele.iterator();
		Iterator<Double> knownMinorAlleleFreqIt = knownMinorAlleleFreq.iterator();
		Iterator<int[]> censusAllIt = censusAll.iterator();
		for (Map.Entry<MarkerKey, Integer> keysIndices : rdMarkers.entrySet()) {
			int[] censusAllValues = censusAllIt.next();
			entries.add(new DefaultQAMarkersOperationEntry(
					keysIndices.getKey(),
					keysIndices.getValue(),
					missingRatioIt.next(),
					mismatchStatesIt.next(),
					knownMajorAlleleIt.next(),
					knownMajorAlleleFreqIt.next(),
					knownMinorAlleleIt.next(),
					knownMinorAlleleFreqIt.next(),
					censusAllValues[0],
					censusAllValues[1],
					censusAllValues[2],
					censusAllValues[3]));
		}

		return entries;
	}

	@Override
	public Collection<Boolean> getMismatchStates() throws IOException {

		Collection<Boolean> mismatchStates = getMismatchStates(-1, -1);
//		// EXCLUDE MARKER BY MISMATCH STATE
//		Map<MarkerKey, Integer> rdQAMarkerSetMapMismatchStates
//				= rdQAMarkerSet.fillOpSetMapWithVariable(rdMarkerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);
//		for (Map.Entry<MarkerKey, Integer> entry : rdQAMarkerSetMapMismatchStates.entrySet()) {
//			MarkerKey key = entry.getKey();
//			Integer value = entry.getValue();
//			if (value.equals(cNetCDF.Defaults.DEFAULT_MISMATCH_YES)) {
//				excludeMarkerSetMap.put(key, value);
//			}
//		}

		return mismatchStates;
	}

	@Override
	public Collection<Double> getMissingRatio() throws IOException {

		Collection<Double> missingRatios = getMissingRatio(-1, -1);
//		// EXCLUDE MARKER BY MISSING RATIO
//		Map<MarkerKey, Double> rdQAMarkerSetMapMissingRat
//				= rdQAMarkerSet.fillOpSetMapWithVariable(rdMarkerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);
//		for (Map.Entry<MarkerKey, Double> entry : rdQAMarkerSetMapMissingRat.entrySet()) {
//			MarkerKey key = entry.getKey();
//			Double value = entry.getValue();
//			if (value > markerMissingRatio) {
//				excludeMarkerSetMap.put(key, value);
//			}
//		}

		return missingRatios;
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<QAMarkersOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten};
		if (netCdfMajorAlleles == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfMajorAlleles = new ArrayByte.D1(writeBuffer.size());
			netCdfMajorAllelesFrequencies = new ArrayDouble.D1(writeBuffer.size());
			netCdfMinorAlleles = new ArrayByte.D1(writeBuffer.size());
			netCdfMinorAllelesFrequencies = new ArrayDouble.D1(writeBuffer.size());
			netCdfCensusAlls = new ArrayInt.D2(writeBuffer.size(), 4);
		}
		int index = 0;
		for (QAMarkersOperationEntry entry : writeBuffer) {
			netCdfMajorAlleles.setByte(netCdfMajorAlleles.getIndex().set(index), entry.getMajorAllele());
			netCdfMajorAllelesFrequencies.setDouble(netCdfMajorAllelesFrequencies.getIndex().set(index), entry.getMajorAlleleFrequency());
			netCdfMinorAlleles.setByte(netCdfMinorAlleles.getIndex().set(index), entry.getMinorAllele());
			netCdfMinorAllelesFrequencies.setDouble(netCdfMinorAllelesFrequencies.getIndex().set(index), entry.getMinorAlleleFrequency());
			netCdfCensusAlls.setInt(netCdfCensusAlls.getIndex().set(index, 0), entry.getAlleleAA());
			netCdfCensusAlls.setInt(netCdfCensusAlls.getIndex().set(index, 1), entry.getAlleleAa());
			netCdfCensusAlls.setInt(netCdfCensusAlls.getIndex().set(index, 1), entry.getAlleleaa());
			netCdfCensusAlls.setInt(netCdfCensusAlls.getIndex().set(index, 2), entry.getMissingCount());
			index++;
		}
		try {
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, origin, netCdfMajorAlleles);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ, origin, netCdfMajorAllelesFrequencies);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, origin, netCdfMinorAlleles);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ, origin, netCdfMinorAllelesFrequencies);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, origin, netCdfCensusAlls);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}
}

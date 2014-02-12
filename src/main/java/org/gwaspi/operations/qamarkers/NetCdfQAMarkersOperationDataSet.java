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
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.qamarkers.QAMarkersOperationEntry.AlleleCounts;
import org.gwaspi.operations.qamarkers.QAMarkersOperationEntry.GenotypeCounts;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
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
//	private ArrayInt.D2 netCdfCensusAlls;
	private ArrayInt.D1 netCdfNumAA;
	private ArrayInt.D1 netCdfNumAa;
	private ArrayInt.D1 netCdfNumaa;
	private ArrayInt.D1 netCdfNumMissing;
	private ArrayInt.D2 netCdfAllelesCount;
	private ArrayInt.D2 netCdfGenotypesCount;

	public NetCdfQAMarkersOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(true, origin, parent, operationKey);
	}

	public NetCdfQAMarkersOperationDataSet(MatrixKey origin, DataSetKey parent) {
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
//		final int gtStride = cNetCDF.Strides.STRIDE_GT;
//		final int maxDifferentAllelePerMarker = AlleleByte.values().length;
//		final int maxDifferentAllelePairsPerMarker = maxDifferentAllelePerMarker * maxDifferentAllelePerMarker;
		final int maxDifferentAllelePerMarker = QAMarkersOperationEntry.AlleleCounts.values().length;
		final int maxDifferentAllelePairsPerMarker = QAMarkersOperationEntry.GenotypeCounts.values().length;

		// dimensions
		Dimension markersDim = markersSpace.get(0);
//		Dimension boxes4Dim = ncFile.addDimension(cNetCDF.Dimensions.DIM_4BOXES, 4);
//		Dimension alleleStrideDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_GTSTRIDE, gtStride / 2);
		Dimension allelesStrideDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_MARKER_ALLELES_STRIDE, maxDifferentAllelePerMarker);
		Dimension genotypesStrideDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_MARKER_GENOTYPES_STRIDE, maxDifferentAllelePairsPerMarker);
		Dimension dim4 = ncFile.addDimension(cNetCDF.Dimensions.DIM_4, 4);

//		// MARKER SPACES
//		List<Dimension> markers4Space = new ArrayList<Dimension>(2);
//		markers4Space.add(markersDim);
//		markers4Space.add(boxes4Dim);

		List<Dimension> markerPropertySpace4 = new ArrayList<Dimension>(2);
		markerPropertySpace4.add(markersDim);
		markerPropertySpace4.add(dim4);

		// ALLELES SPACES
		List<Dimension> allelesSpace = new ArrayList<Dimension>(2);
		allelesSpace.add(markersDim);
		allelesSpace.add(allelesStrideDim);

		List<Dimension> genotypesSpace = new ArrayList<Dimension>(2);
		genotypesSpace.add(markersDim);
		genotypesSpace.add(genotypesStrideDim);

		// Define OP Variables
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE, DataType.INT, markersSpace);
//		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, DataType.INT, markers4Space);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_NUM_AA, DataType.INT, markersSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_NUM_Aa, DataType.INT, markersSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_NUM_aa, DataType.INT, markersSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_NUM_MISSING, DataType.INT, markersSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT, DataType.DOUBLE, markersSpace);

		// Define Genotype Variables
		//ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_KNOWNALLELES, DataType.CHAR, allelesSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, DataType.BYTE, markersSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ, DataType.DOUBLE, markersSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, DataType.BYTE, markersSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ, DataType.DOUBLE, markersSpace);
		ncFile.addVariable(cNetCDF.Variables.VAR_GT_STRAND, DataType.CHAR, markerPropertySpace4);
//		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_APPEARING_ALLELE, DataType.BYTE, allelesSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_APPEARING_ALLELE_COUNT, DataType.INT, allelesSpace);
//		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_APPEARING_GENOTYPE_ALLELE_1, DataType.BYTE, genotypesSpace);
//		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_APPEARING_GENOTYPE_ALLELE_2, DataType.BYTE, genotypesSpace);
		ncFile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_APPEARING_GENOTYPE_COUNT, DataType.INT, genotypesSpace);
	}

	@Override
	protected OperationMetadata createOperationMetadata() throws IOException {

		DataSetMetadata rdDataSetMetadata = MatricesList.getDataSetMetadata(getParent());

		String description = "Marker Quality Assurance on "
				+ rdDataSetMetadata.getFriendlyName()
				+ "\nMarkers: " + getNumMarkers()
				+ "\nStarted at: " + org.gwaspi.global.Utils.getShortDateTimeAsString();

		return new OperationMetadata(
				getParent(), // parent data set
				"Marker QA", // friendly name
				description, // description
				OPType.MARKER_QA,
				getNumMarkers(),
				getNumSamples(),
				getNumChromosomes(),
				isMarkersOperationSet());
	}

	@Override
	public void setMarkerMissingRatios(Collection<Double> markerMissingRatios) throws IOException {
		NetCdfUtils.saveDoubleMapD1ToWrMatrix(getNetCdfWriteFile(), markerMissingRatios, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);
	}

	@Override
	public void setMarkerMismatchStates(Collection<Boolean> markerMismatchStates) throws IOException {

		// we can not use this, as NetCDF does not support writing boolean arrays :/
//		NetCdfUtils.saveBooleansD1ToWrMatrix(getNetCdfWriteFile(), markerMismatchStates, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);

		Collection<Integer> markerMismatchIntegerStates
				= new ArrayList<Integer>(markerMismatchStates.size()); // XXX not sooooo nice! maybe use a converter while writing (saves memory)
		for (boolean mismatch : markerMismatchStates) {
			markerMismatchIntegerStates.add(mismatch
					? cNetCDF.Defaults.DEFAULT_MISMATCH_YES
					: cNetCDF.Defaults.DEFAULT_MISMATCH_NO);
		}
		NetCdfUtils.saveIntMapD1ToWrMatrix(getNetCdfWriteFile(), markerMismatchIntegerStates, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);
	}

	@Override
	public void setMarkerKnownAlleles(Collection<OrderedAlleles> markerKnownAlleles) throws IOException {

		NetcdfFileWriteable netCdfWriteFile = getNetCdfWriteFile();
		//Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, cNetCDF.Census.VAR_OP_MARKERS_KNOWNALLELES, cNetCDF.Strides.STRIDE_GT);
		NetCdfUtils.saveByteMapItemToWrMatrix(netCdfWriteFile, markerKnownAlleles, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, OrderedAlleles.TO_MAJOR_ALLELE, cNetCDF.Strides.STRIDE_GT / 2);
		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(netCdfWriteFile, markerKnownAlleles, OrderedAlleles.TO_MAJOR_ALLELE_FREQ, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);
		NetCdfUtils.saveByteMapItemToWrMatrix(netCdfWriteFile, markerKnownAlleles, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, OrderedAlleles.TO_MINOR_ALLELE, cNetCDF.Strides.STRIDE_GT / 2);
		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(netCdfWriteFile, markerKnownAlleles, OrderedAlleles.TO_MINOR_ALLELE_FREQ, cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);
	}

	@Override
	public void setMarkerCensusAll(Collection<Census> markerCensusAll) throws IOException {
		NetCdfUtils.saveIntMapD2ToWrMatrix(getNetCdfWriteFile(), markerCensusAll, Census.EXTRACTOR_4, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
	}

	@Override
	public List<Boolean> getMismatchStates(int from, int to) throws IOException {

		Collection<Integer> mismatchIntegerStates = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE, from, to, mismatchIntegerStates, null);

		List<Boolean> mismatchStates = new ArrayList<Boolean>(0);
		for (Integer mismatchIntegerState : mismatchIntegerStates) {
			mismatchStates.add(mismatchIntegerState == cNetCDF.Defaults.DEFAULT_MISMATCH_YES);
		}

		return mismatchStates;
	}

	@Override
	public List<Double> getMissingRatio(int from, int to) throws IOException {

		List<Double> missingRatios = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT, from, to, missingRatios, null);

		return missingRatios;
	}

	@Override
	public List<Byte> getKnownMajorAllele(int from, int to) throws IOException {

		List<Byte> knownMajorAllele = new ArrayList<Byte>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, from, to, knownMajorAllele, null);

		return knownMajorAllele;
	}

	@Override
	public List<Double> getKnownMajorAlleleFrequencies(int from, int to) throws IOException {

		List<Double> knownMajorAlleleFreq = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ, from, to, knownMajorAlleleFreq, null);

		return knownMajorAlleleFreq;
	}

	@Override
	public List<Byte> getKnownMinorAllele(int from, int to) throws IOException {

		List<Byte> knownMinorAllele = new ArrayList<Byte>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, from, to, knownMinorAllele, null);

		return knownMinorAllele;
	}

	@Override
	public List<Double> getKnownMinorAlleleFrequencies(int from, int to) throws IOException {

		List<Double> knownMinorAlleleFreq = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ, from, to, knownMinorAlleleFreq, null);

		return knownMinorAlleleFreq;
	}

//	public List<int[]> getCensusAll(int from, int to) throws IOException {
//
//		List<int[]> censusAll = new ArrayList<int[]>(0);
//		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, from, to, censusAll, null);
//
//		return censusAll;
//	}

	public List<Integer> getNumAAs(int from, int to) throws IOException {

		List<Integer> numAAs = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_NUM_AA, from, to, numAAs, null);

		return numAAs;
	}

	public List<Integer> getNumAas(int from, int to) throws IOException {

		List<Integer> numAas = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_NUM_Aa, from, to, numAas, null);

		return numAas;
	}

	public List<Integer> getNumaas(int from, int to) throws IOException {

		List<Integer> numaas = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_NUM_aa, from, to, numaas, null);

		return numaas;
	}

	public List<Integer> getMissingCounts(int from, int to) throws IOException {

		List<Integer> numMissings = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_NUM_MISSING, from, to, numMissings, null);

		return numMissings;
	}

	public List<int[]> getAlleleCounts(int from, int to) throws IOException {

//		List<Byte> appearingAllele = new ArrayList<Byte>(0);
//		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_APPEARING_ALLELE, from, to, appearingAllele, null);
		List<int[]> appearingAllelesCount = new ArrayList<int[]>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_APPEARING_ALLELE_COUNT, from, to, appearingAllelesCount, null);

//		Map<Byte, Integer> alleleCounts = new LinkedHashMap<Byte, Integer>(appearingAllele.size());
//		Iterator<Integer> appearingAllelesCountIt = appearingAllelesCount.iterator();
//		for (Byte allele : appearingAllele) {
//			Integer count = appearingAllelesCountIt.next();
//			alleleCounts.put(allele, count);
//		}

		return appearingAllelesCount;
	}

	public List<int[]> getGenotypeCounts(int from, int to) throws IOException {

		List<int[]> appearingGenotypesCount = new ArrayList<int[]>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_APPEARING_GENOTYPE_COUNT, from, to, appearingGenotypesCount, null);

		return appearingGenotypesCount;
	}

	@Override
	public List<QAMarkersOperationEntry> getEntries(int from, int to) throws IOException {

//		MarkerOperationSet rdMarkersSet = new MarkerOperationSet(getOperationKey(), from, to);
//		Map<MarkerKey, Integer> rdMarkers = rdMarkersSet.getOpSetMap();
		Map<Integer, MarkerKey> markersKeys = getMarkersKeysSource().getIndicesMap(from, to);

		List<Boolean> mismatchStates = getMismatchStates(from, to);
		List<Byte> knownMajorAllele = getKnownMajorAllele(from, to);
		List<Double> knownMajorAlleleFreq = getKnownMajorAlleleFrequencies(from, to);
		List<Byte> knownMinorAllele = getKnownMinorAllele(from, to);
		List<Double> knownMinorAlleleFreq = getKnownMinorAlleleFrequencies(from, to);
//		List<int[]> censusAll = getCensusAll(from, to);
//		List<Integer> numAAs = getNumAAs(from, to);
//		List<Integer> numAas = getNumAas(from, to);
//		List<Integer> numaas = getNumaas(from, to);
		List<Integer> numMissings = getMissingCounts(from, to);
		List<Double> missingRatios = getMissingRatio(from, to);
		List<int[]> alleleCounts = getAlleleCounts(from, to);
		List<int[]> genotypeCounts = getGenotypeCounts(from, to);

		List<QAMarkersOperationEntry> entries
				= new ArrayList<QAMarkersOperationEntry>(missingRatios.size());
		Iterator<Boolean> mismatchStatesIt = mismatchStates.iterator();
		Iterator<Byte> knownMajorAlleleIt = knownMajorAllele.iterator();
		Iterator<Double> knownMajorAlleleFreqIt = knownMajorAlleleFreq.iterator();
		Iterator<Byte> knownMinorAlleleIt = knownMinorAllele.iterator();
		Iterator<Double> knownMinorAlleleFreqIt = knownMinorAlleleFreq.iterator();
//		Iterator<int[]> censusAllIt = censusAll.iterator();
//		Iterator<Integer> numAAIt = numAAs.iterator();
//		Iterator<Integer> numAaIt = numAas.iterator();
//		Iterator<Integer> numaaIt = numaas.iterator();
		Iterator<Integer> numMissingIt = numMissings.iterator();
		Iterator<Double> missingRatioIt = missingRatios.iterator();
		Iterator<int[]> alleleCountsIt = alleleCounts.iterator();
		Iterator<int[]> genotypeCountsIt = genotypeCounts.iterator();
//		for (Map.Entry<MarkerKey, Integer> keysIndices : rdMarkers.entrySet()) {
		for (Map.Entry<Integer, MarkerKey> origIndicesAndKey : markersKeys.entrySet()) {
//			int[] censusAllValues = censusAllIt.next();
			entries.add(new DefaultQAMarkersOperationEntry(
					origIndicesAndKey.getValue(),
					origIndicesAndKey.getKey(),
					mismatchStatesIt.next(),
					knownMajorAlleleIt.next(),
					knownMajorAlleleFreqIt.next(),
					knownMinorAlleleIt.next(),
					knownMinorAlleleFreqIt.next(),
//					censusAllValues[0],
//					censusAllValues[1],
//					censusAllValues[2],
//					censusAllValues[3],
//					numAAIt.next(),
//					numAaIt.next(),
//					numaaIt.next(),
					numMissingIt.next(),
					missingRatioIt.next(),
					alleleCountsIt.next(),
					genotypeCountsIt.next()));
		}

		return entries;
	}

	@Override
	public List<Byte> getKnownMajorAllele() throws IOException {
		return getKnownMajorAllele(-1, -1);
	}

	@Override
	public List<Double> getKnownMajorAlleleFrequencies() throws IOException {
		return getKnownMajorAlleleFrequencies(-1, -1);
	}

	@Override
	public List<Byte> getKnownMinorAllele() throws IOException {
		return getKnownMinorAllele(-1, -1);
	}

	@Override
	public List<Double> getKnownMinorAlleleFrequencies() throws IOException {
		return getKnownMinorAlleleFrequencies(-1, -1);
	}

	@Override
	public List<Boolean> getMismatchStates() throws IOException {

		List<Boolean> mismatchStates = getMismatchStates(-1, -1);
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

	public List<Integer> getNumAAs() throws IOException {
		return getNumAAs(-1, -1);
	}

	public List<Integer> getNumAas() throws IOException {
		return getNumAas(-1, -1);
	}

	public List<Integer> getNumaas() throws IOException {
		return getNumaas(-1, -1);
	}

	public List<Integer> getMissingCounts() throws IOException {
		return getMissingCounts(-1, -1);
	}

	@Override
	public List<Double> getMissingRatio() throws IOException {

		List<Double> missingRatios = getMissingRatio(-1, -1);
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

	public List<int[]> getAlleleCounts() throws IOException {
		return getAlleleCounts(-1, -1);
	}

	public List<int[]> getGenotypeCounts() throws IOException {
		return getGenotypeCounts(-1, -1);
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<QAMarkersOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten};
		int[] origin2D = new int[] {alreadyWritten, 0};
		if (netCdfMajorAlleles == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfMajorAlleles = new ArrayByte.D1(writeBuffer.size());
			netCdfMajorAllelesFrequencies = new ArrayDouble.D1(writeBuffer.size());
			netCdfMinorAlleles = new ArrayByte.D1(writeBuffer.size());
			netCdfMinorAllelesFrequencies = new ArrayDouble.D1(writeBuffer.size());
//			netCdfCensusAlls = new ArrayInt.D2(writeBuffer.size(), 4);
			netCdfNumAA = new ArrayInt.D1(writeBuffer.size());
			netCdfNumAa = new ArrayInt.D1(writeBuffer.size());
			netCdfNumaa = new ArrayInt.D1(writeBuffer.size());
			netCdfNumMissing = new ArrayInt.D1(writeBuffer.size());
			netCdfAllelesCount = new ArrayInt.D2(writeBuffer.size(), AlleleCounts.values().length);
			netCdfGenotypesCount = new ArrayInt.D2(writeBuffer.size(), GenotypeCounts.values().length);
		} else if (writeBuffer.size() < netCdfMajorAlleles.getShape()[0]) {
			// we end up here at the end of the processing, if, for example,
			// we have a buffer size of 10, but only 7 items are left to be written
			List<Range> reducedRange1D = new ArrayList<Range>(1);
			reducedRange1D.add(new Range(writeBuffer.size()));
			List<Range> reducedRange2D = new ArrayList<Range>(2);
			reducedRange2D.add(new Range(writeBuffer.size()));
			reducedRange2D.add(null); // use full range
			try {
				netCdfMajorAlleles = (ArrayByte.D1) netCdfMajorAlleles.sectionNoReduce(reducedRange1D);
				netCdfMajorAllelesFrequencies = (ArrayDouble.D1) netCdfMajorAllelesFrequencies.sectionNoReduce(reducedRange1D);
				netCdfMinorAlleles = (ArrayByte.D1) netCdfMinorAlleles.sectionNoReduce(reducedRange1D);
				netCdfMinorAllelesFrequencies = (ArrayDouble.D1) netCdfMinorAllelesFrequencies.sectionNoReduce(reducedRange1D);
//				netCdfCensusAlls = (ArrayInt.D2) netCdfCensusAlls.sectionNoReduce(reducedRange2D);
				netCdfNumAA = (ArrayInt.D1) netCdfNumAA.sectionNoReduce(reducedRange1D);
				netCdfNumAa = (ArrayInt.D1) netCdfNumAa.sectionNoReduce(reducedRange1D);
				netCdfNumaa = (ArrayInt.D1) netCdfNumaa.sectionNoReduce(reducedRange1D);
				netCdfNumMissing = (ArrayInt.D1) netCdfNumMissing.sectionNoReduce(reducedRange1D);
				netCdfAllelesCount = (ArrayInt.D2) netCdfAllelesCount.sectionNoReduce(reducedRange2D);
				netCdfGenotypesCount = (ArrayInt.D2) netCdfGenotypesCount.sectionNoReduce(reducedRange2D);
			} catch (InvalidRangeException ex) {
				throw new IOException(ex);
			}
		}
		int index = 0;
		for (QAMarkersOperationEntry entry : writeBuffer) {
			netCdfMajorAlleles.setByte(netCdfMajorAlleles.getIndex().set(index), entry.getMajorAllele());
			netCdfMajorAllelesFrequencies.setDouble(netCdfMajorAllelesFrequencies.getIndex().set(index), entry.getMajorAlleleFrequency());
			netCdfMinorAlleles.setByte(netCdfMinorAlleles.getIndex().set(index), entry.getMinorAllele());
			netCdfMinorAllelesFrequencies.setDouble(netCdfMinorAllelesFrequencies.getIndex().set(index), entry.getMinorAlleleFrequency());
//			netCdfCensusAlls.setInt(netCdfCensusAlls.getIndex().set(index, 0), entry.getAlleleAA());
//			netCdfCensusAlls.setInt(netCdfCensusAlls.getIndex().set(index, 1), entry.getAlleleAa());
//			netCdfCensusAlls.setInt(netCdfCensusAlls.getIndex().set(index, 2), entry.getAlleleaa());
//			netCdfCensusAlls.setInt(netCdfCensusAlls.getIndex().set(index, 3), entry.getMissingCount());
			netCdfNumAA.setInt(netCdfNumAA.getIndex().set(index), entry.getAlleleAA());
			netCdfNumAa.setInt(netCdfNumAa.getIndex().set(index), entry.getAlleleAa());
			netCdfNumaa.setInt(netCdfNumaa.getIndex().set(index), entry.getAlleleaa());
			netCdfNumMissing.setInt(netCdfNumMissing.getIndex().set(index), entry.getMissingCount());
			final int[] alleleCounts = entry.getAlleleCounts();
			for (int ai = 0; ai < AlleleCounts.values().length; ai++) {
				netCdfAllelesCount.setInt(netCdfAllelesCount.getIndex().set(index, ai), alleleCounts[ai]);
			}
			final int[] genotypeCounts = entry.getGenotypeCounts();
			for (int gi = 0; gi < GenotypeCounts.values().length; gi++) {
				netCdfGenotypesCount.setInt(netCdfGenotypesCount.getIndex().set(index, gi), genotypeCounts[gi]);
			}
			index++;
		}
		try {
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, origin, netCdfMajorAlleles);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ, origin, netCdfMajorAllelesFrequencies);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, origin, netCdfMinorAlleles);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ, origin, netCdfMinorAllelesFrequencies);
//			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, origin2D, netCdfCensusAlls);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_NUM_AA, origin, netCdfNumAA);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_NUM_Aa, origin, netCdfNumAa);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_NUM_aa, origin, netCdfNumaa);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_NUM_MISSING, origin, netCdfNumMissing);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_APPEARING_ALLELE_COUNT, origin2D, netCdfAllelesCount);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_APPEARING_GENOTYPE_COUNT, origin2D, netCdfGenotypesCount);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}
}

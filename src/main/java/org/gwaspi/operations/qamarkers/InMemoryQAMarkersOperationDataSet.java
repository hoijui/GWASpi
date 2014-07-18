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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.AbstractInMemoryOperationDataSet;
import org.gwaspi.operations.NetCdfUtils;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.qamarkers.QAMarkersOperationEntry.AlleleCounts;
import org.gwaspi.operations.qamarkers.QAMarkersOperationEntry.GenotypeCounts;

public class InMemoryQAMarkersOperationDataSet extends AbstractInMemoryOperationDataSet<QAMarkersOperationEntry> implements QAMarkersOperationDataSet {

	private List<Byte> netCdfMismatchStates;
	private List<Byte> netCdfMajorAlleles;
	private List<Double> netCdfMajorAllelesFrequencies;
	private List<Byte> netCdfMinorAlleles;
	private List<Double> netCdfMinorAllelesFrequencies;
	private List<Integer>netCdfNumAA;
	private List<Integer> netCdfNumAa;
	private List<Integer> netCdfNumaa;
	private List<Integer> netCdfNumMissing;
	private ArrayInt.D2 netCdfAllelesCount;
	private ArrayInt.D2 netCdfGenotypesCount;

	public InMemoryQAMarkersOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(true, origin, parent, operationKey);
	}

	public InMemoryQAMarkersOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
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
	public List<Boolean> getMismatchStates(int from, int to) throws IOException {

		List<Byte> mismatchIntegerStates = new ArrayList<Byte>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE, from, to, mismatchIntegerStates, null);

		List<Boolean> mismatchStates = new ArrayList<Boolean>(0);
		for (Byte mismatchIntegerState : mismatchIntegerStates) {
			mismatchStates.add(mismatchIntegerState == cNetCDF.Defaults.DEFAULT_MISMATCH_YES);
		}

		return mismatchStates;
	}

	@Override
	public List<Double> getMissingRatio(int from, int to) throws IOException {

		List<Integer> missingCounts = getMissingCounts(-1, -1);
		List<Double> missingRatios = new ArrayList<Double>(missingCounts.size());
		final double numSamples = getNumSamples();
		for (Integer missingCount : missingCounts) {
			missingRatios.add(missingCount / numSamples);
		}

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

	@Override
	public List<Integer> getNumAAs(int from, int to) throws IOException {

		List<Integer> numAAs = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_NUM_AA, from, to, numAAs, null);

		return numAAs;
	}

	@Override
	public List<Integer> getNumAas(int from, int to) throws IOException {

		List<Integer> numAas = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_NUM_Aa, from, to, numAas, null);

		return numAas;
	}

	@Override
	public List<Integer> getNumaas(int from, int to) throws IOException {

		List<Integer> numaas = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_NUM_aa, from, to, numaas, null);

		return numaas;
	}

	@Override
	public List<Integer> getMissingCounts(int from, int to) throws IOException {

		List<Integer> numMissings = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_MARKERS_NUM_MISSING, from, to, numMissings, null);

		return numMissings;
	}

	@Override
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

	@Override
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

	@Override
	public List<Integer> getNumAAs() throws IOException {
		return getNumAAs(-1, -1);
	}

	@Override
	public List<Integer> getNumAas() throws IOException {
		return getNumAas(-1, -1);
	}

	@Override
	public List<Integer> getNumaas() throws IOException {
		return getNumaas(-1, -1);
	}

	@Override
	public List<Integer> getMissingCounts() throws IOException {
		return getMissingCounts(-1, -1);
	}

	@Override
	public List<Double> getMissingRatio() throws IOException {
		return getMissingRatio(-1, -1);
	}

	@Override
	public List<int[]> getAlleleCounts() throws IOException {
		return getAlleleCounts(-1, -1);
	}

	@Override
	public List<int[]> getGenotypeCounts() throws IOException {
		return getGenotypeCounts(-1, -1);
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<QAMarkersOperationEntry> writeBuffer) throws IOException {
	}
}

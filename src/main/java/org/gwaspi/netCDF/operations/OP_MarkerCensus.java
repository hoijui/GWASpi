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

package org.gwaspi.netCDF.operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.GWASpi;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes;
import org.gwaspi.global.Text;
import org.gwaspi.model.Census;
import org.gwaspi.model.CensusFull;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.markers.NetCDFDataSetSource;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.markercensus.MarkerCensusOperationDataSet;
import org.gwaspi.operations.markercensus.NetCdfMarkerCensusOperationDataSet;
import org.gwaspi.operations.qamarkers.NetCdfQAMarkersOperationDataSet;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.operations.qasamples.NetCdfQASamplesOperationDataSet;
import org.gwaspi.operations.qasamples.QASamplesOperationDataSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OP_MarkerCensus implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(OP_MarkerCensus.class);

	private final MatrixKey rdMatrixKey;
	private final String censusName;
	private final OperationMetadata sampleQAOP;
	private final double sampleMissingRatio;
	private final double sampleHetzygRatio;
	private final OperationMetadata markerQAOP;
	private final boolean discardMismatches;
	private final double markerMissingRatio;
	private final File phenoFile;

	public OP_MarkerCensus(
			MatrixKey rdMatrixKey,
			String censusName,
			OperationMetadata sampleQAOP,
			double sampleMissingRatio,
			double sampleHetzygRatio,
			OperationMetadata markerQAOP,
			boolean discardMismatches,
			double markerMissingRatio,
			File phenoFile)
	{
		this.rdMatrixKey = rdMatrixKey;
		this.censusName = censusName;
		this.sampleQAOP = sampleQAOP;
		this.sampleMissingRatio = sampleMissingRatio;
		this.sampleHetzygRatio = sampleHetzygRatio;
		this.markerQAOP = markerQAOP;
		this.discardMismatches = discardMismatches;
		this.markerMissingRatio = markerMissingRatio;
		this.phenoFile = phenoFile;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	@Override
	public int processMatrix() throws IOException {
		int resultOpId = Integer.MIN_VALUE;

		Map<SampleKey, Double> excludeSampleSetMap = new LinkedHashMap<SampleKey, Double>();
		boolean dataRemaining = pickingMarkersAndSamplesFromQA(excludeSampleSetMap);

		if (dataRemaining) {
			// THERE IS DATA LEFT TO PROCESS AFTER PICKING

			//<editor-fold defaultstate="expanded" desc="PURGE Maps">
			MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(rdMatrixKey);

			DataSetSource dataSetSource = new NetCDFDataSetSource(rdMatrixKey);

			MarkerSet rdMarkerSet = new MarkerSet(rdMatrixKey);
			rdMarkerSet.initFullMarkerIdSetMap();
			rdMarkerSet.fillWith(cNetCDF.Defaults.DEFAULT_GT);

			SampleSet rdSampleSet = new SampleSet(rdMatrixKey);
//			Map<SampleKey, byte[]> rdSampleSetMap = rdSampleSet.getSampleIdSetMapByteArray();
			Map<Integer, SampleKey> wrSampleKeys = new LinkedHashMap<Integer, SampleKey>(); // XXX Should this be a List instead, to preserve order?
			int sampleIndex = 0;
			for (SampleKey key : rdSampleSetMap.keySet()) {
				if (!excludeSampleSetMap.containsKey(key)) {
					wrSampleKeys.put(sampleIndex, key);
				}
				sampleIndex++;
			}
			//</editor-fold>

//			NetcdfFileWriteable wrNcFile = null;
			try {
				Collection<MarkerKey> wrMarkerKeys = new ArrayList<MarkerKey>(rdMarkerSet.getMarkerKeys()); // XXX should be removable, as wr is same as rd, we could just use rd

				MarkerCensusOperationDataSet dataSet = new NetCdfMarkerCensusOperationDataSet(); // HACK
				((AbstractNetCdfOperationDataSet) dataSet).setReadMatrixKey(rdMatrixKey); // HACK
				((AbstractNetCdfOperationDataSet) dataSet).setNumMarkers(wrMarkerKeys.size()); // HACK
				((AbstractNetCdfOperationDataSet) dataSet).setNumSamples(wrSampleKeys.size()); // HACK
				((NetCdfMarkerCensusOperationDataSet) dataSet).setCensusName(censusName); // HACK
				((NetCdfMarkerCensusOperationDataSet) dataSet).setPhenoFile(phenoFile); // HACK
				((NetCdfMarkerCensusOperationDataSet) dataSet).setSampleMissingRatio(sampleMissingRatio);// HACK
				((NetCdfMarkerCensusOperationDataSet) dataSet).setSampleHetzygRatio(sampleHetzygRatio); // HACK
				((NetCdfMarkerCensusOperationDataSet) dataSet).setMarkerMissingRatio(markerMissingRatio); // HACK
				((NetCdfMarkerCensusOperationDataSet) dataSet).setDiscardMismatches(discardMismatches); // HACK

				((AbstractNetCdfOperationDataSet) dataSet).setUseAllMarkers(rdMatrixKey);
				dataSet.setSamples(wrSampleKeys);
				((AbstractNetCdfOperationDataSet) dataSet).setUseAllChromosomes(rdMatrixKey);

//				// CREATE netCDF-3 FILE
//				cNetCDF.Defaults.OPType opType = cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_AFFECTION;
//
//				String description = "Genotype frequency count -" + censusName + "- on " + rdMatrixMetadata.getMatrixFriendlyName();
//				if (phenoFile != null) {
//					description += "\nCase/Control status read from file: " + phenoFile.getPath();
//					opType = cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_PHENOTYPE;
//				}
//				OperationFactory wrOPHandler = new OperationFactory(
//						rdMatrixMetadata.getStudyKey(),
//						"Genotypes freq. - " + censusName, // friendly name
//						description + "\nSample missing ratio threshold: " + sampleMissingRatio + "\nSample heterozygosity ratio threshold: " + sampleHetzygRatio + "\nMarker missing ratio threshold: " + markerMissingRatio + "\nDiscard mismatching Markers: " + discardMismatches + "\nMarkers: " + wrMarkerKeys.size() + "\nSamples: " + wrSampleKeys.size(), // description
//						wrMarkerKeys.size(),
//						wrSampleKeys.size(),
//						0,
//						opType,
//						rdMatrixKey, // Parent matrixId
//						-1); // Parent operationId

				// what will be written to the operation NetCDF file (wrNcFile):
				// - Variables.VAR_OPSET: [Collection<MarkerKey>]
				// - Variables.VAR_MARKERS_RSID: [Collection<String>]
				// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
				// - Variables.VAR_ALLELES: known alleles [Collection<char[]>]
				// - Census.VAR_OP_MARKERS_CENSUSALL: marker census - all [Collection<Census.all<== int[]>>]
				// - Census.VAR_OP_MARKERS_CENSUSCASE: marker census - case [Collection<Census.case>]
				// - Census.VAR_OP_MARKERS_CENSUSCTRL: marker census - control [Collection<Census.control>]
				// - Census.VAR_OP_MARKERS_CENSUSHW: marker census - alternate hardy-weinberg [Collection<Census.altHW>]

//				wrNcFile = wrOPHandler.getNetCDFHandler();
//				wrNcFile.create();
//				log.trace("Done creating netCDF handle: " + wrNcFile.toString());

//				writeMetadata(dataSet, rdMarkerSet, wrMarkerKeys, wrSampleKeys);

				//<editor-fold defaultstate="expanded" desc="PROCESSOR">
				Map<SampleKey, SampleInfo> samplesInfoMap = fetchSampleInfo(
						rdMatrixMetadata.getStudyKey(), rdMatrixMetadata, wrSampleKeys);

				// Iterate through markerset, take it marker by marker
				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
				// INIT wrSampleSetMap with indexing order and chromosome info
				Map<MarkerKey, Object[]> wrMarkerInfos = new LinkedHashMap<MarkerKey, Object[]>();
				if (rdMarkerSet.getMarkerIdSetMapCharArray() != null) {
					int idx = 0;
					for (Map.Entry<MarkerKey, char[]> entry : rdMarkerSet.getMarkerIdSetMapCharArray().entrySet()) {
						MarkerKey key = entry.getKey();
						if (wrMarkerKeys.contains(key)) {
							String chr = new String(entry.getValue());
							Object[] markerInfo = new Object[] {idx, chr};
							wrMarkerInfos.put(key, markerInfo); // NOTE This value is never used!
						}
						idx++;
					}

					rdMarkerSet.getMarkerIdSetMapCharArray().clear();
				}

				log.info("Start Census testing markers");

				int countMarkers = 0;
				int chunkSize = Math.round((float)org.gwaspi.gui.StartGWASpi.maxProcessMarkers / 4);
				if (chunkSize > 500000) {
					chunkSize = 500000; // We want to keep things manageable for RAM
				}
				if (chunkSize < 10000 && org.gwaspi.gui.StartGWASpi.maxProcessMarkers > 10000) {
					chunkSize = 10000; // But keep Map size sensible
				}
				int countChunks = 0;

				Map<MarkerKey, CensusFull> wrChunkedMarkerCensusMap = new LinkedHashMap<MarkerKey, CensusFull>();
				Map<MarkerKey, byte[]> wrChunkedKnownAllelesMap = new LinkedHashMap<MarkerKey, byte[]>();
				Iterator<GenotypesList> markersGTsIt = dataSetSource.getMarkersGenotypesSource().iterator();
				for (Map.Entry<MarkerKey, ?> entry : wrMarkerInfos.entrySet()) {
					MarkerKey markerKey = entry.getKey();
					if (countMarkers % chunkSize == 0) {
						if (countMarkers > 0) {
							// CENSUS DATA WRITER
							censusDataWriter(
									dataSet,
									wrChunkedMarkerCensusMap,
									wrChunkedKnownAllelesMap,
									countChunks,
									chunkSize);

							countChunks++;
						}
						wrChunkedMarkerCensusMap = new LinkedHashMap<MarkerKey, CensusFull>();
						wrChunkedKnownAllelesMap = new LinkedHashMap<MarkerKey, byte[]>();
						System.gc(); // Try to garbage collect here
					}
					wrChunkedMarkerCensusMap.put(markerKey, new CensusFull()); // XXX This might be unrequired (would only, possibly make sense in case of an exception, but even then still only marginally, and with code modifications)
					countMarkers++;

					Map<Byte, Float> knownAlleles = new LinkedHashMap<Byte, Float>();
					Map<Integer, Float> allSamplesGTsTable = new LinkedHashMap<Integer, Float>();
					Map<Integer, Float> caseSamplesGTsTable = new LinkedHashMap<Integer, Float>();
					Map<Integer, Float> ctrlSamplesGTsTable = new LinkedHashMap<Integer, Float>();
					Map<Integer, Float> hwSamplesGTsTable = new LinkedHashMap<Integer, Float>();
					Map<String, Integer> allSamplesContingencyTable = new LinkedHashMap<String, Integer>();
					Map<String, Integer> caseSamplesContingencyTable = new LinkedHashMap<String, Integer>();
					Map<String, Integer> ctrlSamplesContingencyTable = new LinkedHashMap<String, Integer>();
					Map<String, Integer> hwSamplesContingencyTable = new LinkedHashMap<String, Integer>();
					Integer missingCount = 0;

					// Get a sample-set full of GTs
					Object[] markerInfo = (Object[]) entry.getValue();
					int markerNb = Integer.parseInt(markerInfo[0].toString());
					String markerChr = markerInfo[1].toString();

					Iterator<byte[]> markerGTsIt = markersGTsIt.next().iterator();
					for (SampleKey sampleKey : dataSetSource.getSamplesKeysSource()) {
						SampleInfo sampleInfo = samplesInfoMap.get(sampleKey);

						//<editor-fold defaultstate="expanded" desc="THE DECIDER">
						CensusDecision decision = CensusDecision.getDecisionByChrAndSex(markerChr, sampleInfo.getSex());

						float counter = 1;
//						if (decision == CensusDecision.CountMalesNonAutosomally) {
//							counter = 0.5f;
//						}
						//</editor-fold>

						// SUMMING SAMPLESET GENOTYPES
						byte[] tempGT = markerGTsIt.next();
						missingCount = summingSampleSetGenotypes(
								tempGT,
								decision,
								knownAlleles,
								allSamplesGTsTable,
								sampleInfo.getAffection(),
								caseSamplesGTsTable,
								ctrlSamplesGTsTable,
								hwSamplesGTsTable,
								counter,
								missingCount);
					}

					// AFFECTION ALLELE CENSUS + MISMATCH STATE + MISSINGNESS
					if (knownAlleles.size() <= 2) {
						// Check if there are mismatches in alleles

						List<Integer> AAnumVals = new ArrayList<Integer>();
						List<Integer> AanumVals = new ArrayList<Integer>();
						List<Integer> aanumVals = new ArrayList<Integer>();

						knowYourAlleles(
								knownAlleles,
								AAnumVals,
								AanumVals,
								aanumVals);

						contingencyCensusSamples(
								allSamplesContingencyTable,
								allSamplesGTsTable,
								AAnumVals,
								AanumVals,
								aanumVals);

						contingencyCensusSamples(
								caseSamplesContingencyTable,
								caseSamplesGTsTable,
								AAnumVals,
								AanumVals,
								aanumVals);

						contingencyCensusSamples(
								ctrlSamplesContingencyTable,
								ctrlSamplesGTsTable,
								AAnumVals,
								AanumVals,
								aanumVals);

						contingencyCensusSamples(
								hwSamplesContingencyTable,
								hwSamplesGTsTable,
								AAnumVals,
								AanumVals,
								aanumVals);

						// CENSUS
						int obsAllAA = 0;
						int obsAllAa = 0;
						int obsAllaa = 0;
						int obsCaseAA = 0;
						int obsCaseAa = 0;
						int obsCaseaa = 0;
						int obsCntrlAA = 0;
						int obsCntrlAa = 0;
						int obsCntrlaa = 0;
						int obsHwAA = 0;
						int obsHwAa = 0;
						int obsHwaa = 0;
						if (allSamplesContingencyTable.containsKey("AA")) {
							obsAllAA = allSamplesContingencyTable.get("AA");
						}
						if (allSamplesContingencyTable.containsKey("Aa")) {
							obsAllAa = allSamplesContingencyTable.get("Aa");
						}
						if (allSamplesContingencyTable.containsKey("aa")) {
							obsAllaa = allSamplesContingencyTable.get("aa");
						}
						if (caseSamplesContingencyTable.containsKey("AA")) {
							obsCaseAA = caseSamplesContingencyTable.get("AA");
						}
						if (caseSamplesContingencyTable.containsKey("Aa")) {
							obsCaseAa = caseSamplesContingencyTable.get("Aa");
						}
						if (caseSamplesContingencyTable.containsKey("aa")) {
							obsCaseaa = caseSamplesContingencyTable.get("aa");
						}
						if (ctrlSamplesContingencyTable.containsKey("AA")) {
							obsCntrlAA = ctrlSamplesContingencyTable.get("AA");
						}
						if (ctrlSamplesContingencyTable.containsKey("Aa")) {
							obsCntrlAa = ctrlSamplesContingencyTable.get("Aa");
						}
						if (ctrlSamplesContingencyTable.containsKey("aa")) {
							obsCntrlaa = ctrlSamplesContingencyTable.get("aa");
						}
						if (hwSamplesContingencyTable.containsKey("AA")) {
							obsHwAA = hwSamplesContingencyTable.get("AA");
						}
						if (hwSamplesContingencyTable.containsKey("Aa")) {
							obsHwAa = hwSamplesContingencyTable.get("Aa");
						}
						if (hwSamplesContingencyTable.containsKey("aa")) {
							obsHwaa = hwSamplesContingencyTable.get("aa");
						}

						CensusFull census = new CensusFull(
								new Census(obsAllAA, obsAllAa, obsAllaa, missingCount), // all
								new Census(obsCaseAA, obsCaseAa, obsCaseaa, -1), // case
								new Census(obsCntrlAA, obsCntrlAa, obsCntrlaa, -1), // control
								new Census(obsHwAA, obsHwAa, obsHwaa, -1) // alternate HW samples
								);

						wrChunkedMarkerCensusMap.put(markerKey, census);

						byte[] alleles = cNetCDF.Defaults.DEFAULT_GT;
						Iterator<Byte> knit = knownAlleles.keySet().iterator();
						if (knownAlleles.size() == 2) {
							Byte allele1 = knit.next();
							Byte allele2 = knit.next();
							alleles = new byte[] {allele1, allele2};
						}
						if (knownAlleles.size() == 1) {
							Byte allele1 = knit.next();
							alleles = new byte[] {allele1, allele1};
						}

						wrChunkedKnownAllelesMap.put(markerKey, alleles);
					} else {
						// MISMATCHES FOUND
						wrChunkedMarkerCensusMap.put(markerKey, new CensusFull());
						wrChunkedKnownAllelesMap.put(markerKey, "00".getBytes());
					}

					if (markerNb != 0 && markerNb % 100000 == 0) {
						log.info("Processed markers: {}", markerNb);
					}
				}
				//</editor-fold>

				// LAST CENSUS DATA WRITER
				censusDataWriter(
						dataSet,
						wrChunkedMarkerCensusMap,
						wrChunkedKnownAllelesMap,
						countChunks,
						chunkSize);

			resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK
//			} catch (InvalidRangeException ex) {
//				throw new IOException(ex);
			} finally {
				org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
			}
		} else {
			// NO DATA LEFT AFTER THRESHOLD FILTER PICKING
			log.warn(Text.Operation.warnNoDataLeftAfterPicking);
		}

		return resultOpId;
	}

	private void knowYourAlleles(
			Map<Byte, Float> knownAlleles,
			List<Integer> AAnumValsAL,
			List<Integer> AanumValsAL,
			List<Integer> aanumValsAL)
	{
		Iterator<Byte> itKnAll = knownAlleles.keySet().iterator();
		if (knownAlleles.size() == 1) {
			// Homozygote (AA or aa)
			byte key = itKnAll.next();
			int intAllele1 = (int) key;
			AAnumValsAL.add(intAllele1); // Single A
			AAnumValsAL.add(intAllele1 * 2); // Double AA
		}
		if (knownAlleles.size() == 2) {
			// Heterezygote (AA, Aa or aa)
			byte key = itKnAll.next();
			int countA = Math.round(knownAlleles.get(key));
			int intAllele1 = (int) key;
			key = itKnAll.next();
			int countB = Math.round(knownAlleles.get(key));
			int intAllele2 = (int) key;

			if (countA >= countB) {
				// Finding out what allele is major and minor
				AAnumValsAL.add(intAllele1);
				AAnumValsAL.add(intAllele1 * 2);

				aanumValsAL.add(intAllele2);
				aanumValsAL.add(intAllele2 * 2);

				AanumValsAL.add(intAllele1 + intAllele2);
			} else {
				AAnumValsAL.add(intAllele2);
				AAnumValsAL.add(intAllele2 * 2);

				aanumValsAL.add(intAllele1);
				aanumValsAL.add(intAllele1 * 2);

				AanumValsAL.add(intAllele1 + intAllele2);
			}
		}
	}

	private static void contingencyCensusSamples(
			Map<String, Integer> hwSamplesContingencyTable,
			Map<Integer, Float> hwSamplesGTsTable,
			List<Integer> AAnumValsAL,
			List<Integer> AanumValsAL,
			List<Integer> aanumValsAL)
	{
		for (Map.Entry<Integer, Float> samplesEntry : hwSamplesGTsTable.entrySet()) {
			Integer key = samplesEntry.getKey();
			Integer value = Math.round(samplesEntry.getValue());

			if (AAnumValsAL.contains(key)) {
				// compare to all possible character values of AA
				// HW CENSUS
				int tempCount = 0;
				if (hwSamplesContingencyTable.containsKey("AA")) {
					tempCount = hwSamplesContingencyTable.get("AA");
				}
				hwSamplesContingencyTable.put("AA", tempCount + value);
			}
			if (AanumValsAL.contains(key)) {
				// compare to all possible character values of Aa
				// HW CENSUS
				int tempCount = 0;
				if (hwSamplesContingencyTable.containsKey("Aa")) {
					tempCount = hwSamplesContingencyTable.get("Aa");
				}
				hwSamplesContingencyTable.put("Aa", tempCount + value);
			}
			if (aanumValsAL.contains(key)) {
				// compare to all possible character values of aa
				// HW CENSUS
				int tempCount = 0;
				if (hwSamplesContingencyTable.containsKey("aa")) {
					tempCount = hwSamplesContingencyTable.get("aa");
				}
				hwSamplesContingencyTable.put("aa", tempCount + value);
			}
		}
	}

	/**
	 * PICKING CLEAN MARKERS AND SAMPLES FROM QA.
	 * @return true if there is data left after picking, false otherwise
	 */
	private boolean pickingMarkersAndSamplesFromQA(
			Map<SampleKey, Double> excludeSampleSetMap)
			throws IOException
	{
		OperationKey markerQAOPKey = OperationKey.valueOf(markerQAOP);
		OperationKey sampleQAOPKey = OperationKey.valueOf(sampleQAOP);

		QAMarkersOperationDataSet qaMarkersOperationDataSet = new NetCdfQAMarkersOperationDataSet(markerQAOPKey);
		QASamplesOperationDataSet qaSamplesOperationDataSet = new NetCdfQASamplesOperationDataSet(sampleQAOPKey);

//		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markerQAOP.getId());
//		NetcdfFile rdMarkerQANcFile = NetcdfFile.open(markerQAMetadata.getPathToMatrix());

//		OperationMetadata sampleQAMetadata = OperationsList.getOperationMetadata(sampleQAOP.getId());
//		NetcdfFile rdSampleQANcFile = NetcdfFile.open(sampleQAMetadata.getPathToMatrix());

//		MarkerOperationSet rdQAMarkerSet = new MarkerOperationSet(OperationKey.valueOf(markerQAMetadata));
//		SampleOperationSet rdQASampleSet = new SampleOperationSet(OperationKey.valueOf(sampleQAMetadata));
//		Map<MarkerKey, ?> rdQAMarkerSetMap = rdQAMarkerSet.getOpSetMap();
//		Map<SampleKey, ?> rdQASampleSetMap = rdQASampleSet.getOpSetMap();
		Map<MarkerKey, Object> excludeMarkerSetMap = new LinkedHashMap<MarkerKey, Object>();
		excludeSampleSetMap.clear();

		int totalSampleNb = rdQASampleSetMap.size();
		int totalMarkerNb = rdQAMarkerSetMap.size();

		// EXCLUDE MARKER BY MISMATCH STATE
		if (discardMismatches) {
			Iterator<Boolean> mismatchStatesIt = qaMarkersOperationDataSet.getMismatchStates().iterator();
//			Map<MarkerKey, Integer> rdQAMarkerSetMapMismatchStates = rdQAMarkerSet.fillOpSetMapWithVariable(rdMarkerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);
			for (Map.Entry<MarkerKey, Integer> entry : rdQAMarkerSetMapMismatchStates.entrySet()) {
				MarkerKey key = entry.getKey();
				Integer value = entry.getValue();
				if (mismatchStatesIt.next()) {
					excludeMarkerSetMap.put(key, value);
				}
			}
		}

		// EXCLUDE MARKER BY MISSING RATIO
		Map<MarkerKey, Double> rdQAMarkerSetMapMissingRat = rdQAMarkerSet.fillOpSetMapWithVariable(rdMarkerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);
		for (Map.Entry<MarkerKey, Double> entry : rdQAMarkerSetMapMissingRat.entrySet()) {
			MarkerKey key = entry.getKey();
			Double value = entry.getValue();
			if (value > markerMissingRatio) {
				excludeMarkerSetMap.put(key, value);
			}
		}

		// EXCLUDE SAMPLE BY MISSING RATIO
		Map<SampleKey, Double> rdQASampleSetMapMissingRat = rdQASampleSet.fillOpSetMapWithVariable(rdSampleQANcFile, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);
		if (rdQASampleSetMapMissingRat != null) {
			for (Map.Entry<SampleKey, Double> entry : rdQASampleSetMapMissingRat.entrySet()) {
				SampleKey key = entry.getKey();
				double value = entry.getValue();
				if (value > sampleMissingRatio) {
					excludeSampleSetMap.put(key, value);
				}
			}
		}

		// EXCLUDE SAMPLE BY HETEROZYGOSITY RATIO
		Map<SampleKey, Double> rdQASampleSetMapHetzyRat = rdQASampleSet.fillOpSetMapWithVariable(rdSampleQANcFile, cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);
		if (rdQASampleSetMapHetzyRat != null) {
			for (Map.Entry<SampleKey, Double> entry : rdQASampleSetMapHetzyRat.entrySet()) {
				SampleKey key = entry.getKey();
				double value = entry.getValue();
				if (value > sampleHetzygRatio) {
					excludeSampleSetMap.put(key, value);
				}
			}
		}

		rdSampleQANcFile.close();
		rdMarkerQANcFile.close();

		return ((excludeSampleSetMap.size() < totalSampleNb)
				&& (excludeMarkerSetMap.size() < totalMarkerNb));
	}

	static int summingSampleSetGenotypes(
			byte[] tempGT,
			CensusDecision decision,
			Map<Byte, Float> knownAlleles,
			Map<Integer, Float> allSamplesGTsTable,
			Affection affection,
			Map<Integer, Float> caseSamplesGTsTable,
			Map<Integer, Float> ctrlSamplesGTsTable,
			Map<Integer, Float> hwSamplesGTsTable,
			float counter,
			int missingCount)
	{
		int newMissingCount = missingCount;

		// Gather alleles different from 0 into a list of known alleles
		// and count the number of appearences
		if (tempGT[0] != AlleleBytes._0) {
			float tempCount = 0;
			if (knownAlleles.containsKey(tempGT[0])) {
				tempCount = knownAlleles.get(tempGT[0]);
			}
			knownAlleles.put(tempGT[0], tempCount + counter);
		}
		if (tempGT[1] != AlleleBytes._0) {
			float tempCount = 0;
			if (knownAlleles.containsKey(tempGT[1])) {
				tempCount = knownAlleles.get(tempGT[1]);
			}
			knownAlleles.put(tempGT[1], tempCount + counter);
		}
		if (tempGT[0] == AlleleBytes._0 && tempGT[1] == AlleleBytes._0) {
			newMissingCount++;
		}

		int intAllele1 = tempGT[0];
		int intAllele2 = tempGT[1];
		Integer intAlleleSum = intAllele1 + intAllele2; // 2 alleles per GT

		// CASE/CONTROL CENSUS
		float tempCount = 0;
		if (allSamplesGTsTable.containsKey(intAlleleSum)) {
			tempCount = allSamplesGTsTable.get(intAlleleSum);
		}
		allSamplesGTsTable.put(intAlleleSum, tempCount + counter);

		if (affection == Affection.AFFECTED) {
			tempCount = 0;
			if (caseSamplesGTsTable.containsKey(intAlleleSum)) {
				tempCount = caseSamplesGTsTable.get(intAlleleSum);
			}
			caseSamplesGTsTable.put(intAlleleSum, tempCount + counter);
		} else if (affection == Affection.UNAFFECTED) {
			tempCount = 0;
			if (ctrlSamplesGTsTable.containsKey(intAlleleSum)) {
				tempCount = ctrlSamplesGTsTable.get(intAlleleSum);
			}
			ctrlSamplesGTsTable.put(intAlleleSum, tempCount + counter);

			// HARDY WEINBERG COUNTER
			if (hwSamplesGTsTable.containsKey(intAlleleSum)) {
				tempCount = hwSamplesGTsTable.get(intAlleleSum);
			}
			if (decision == CensusDecision.CountMalesNonAutosomally) {
				hwSamplesGTsTable.put(intAlleleSum, tempCount);
			}
			if (decision == CensusDecision.CountFemalesNonAutosomally) {
				hwSamplesGTsTable.put(intAlleleSum, tempCount);
			}
			if (decision == CensusDecision.CountAutosomally) {
				hwSamplesGTsTable.put(intAlleleSum, tempCount + counter);
			}
		}

		return newMissingCount;
	}

	private Map<SampleKey, SampleInfo> fetchSampleInfo(
			StudyKey studyKey,
			MatrixMetadata rdMatrixMetadata,
			Collection<SampleKey> wrSampleKeys)
			throws IOException
	{
		Map<SampleKey, SampleInfo> samplesInfoMap = new LinkedHashMap<SampleKey, SampleInfo>();
		List<SampleInfo> sampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(rdMatrixMetadata.getStudyKey());
		if (phenoFile == null) {
			for (SampleInfo sampleInfo : sampleInfos) {
				SampleKey tempSampleKey = sampleInfo.getKey();
				if (wrSampleKeys.contains(tempSampleKey)) {
					samplesInfoMap.put(tempSampleKey, sampleInfo);
				}
			}
		} else {
			FileReader phenotypeFR = new FileReader(phenoFile); // Pheno file has SampleInfo format!
			BufferedReader phenotypeBR = new BufferedReader(phenotypeFR);

			/*String header = */phenotypeBR.readLine(); // ignore header block
			String l;
			while ((l = phenotypeBR.readLine()) != null) {
				String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
				SampleInfo info = new SampleInfo(
						studyKey,
						cVals[GWASpi.sampleId],
						cVals[GWASpi.familyId],
						Sex.parse(cVals[GWASpi.sex]),
						Affection.parse(cVals[GWASpi.affection]));
				samplesInfoMap.put(info.getKey(), info);
			}
			phenotypeBR.close();
			// CHECK IF THERE ARE MISSING SAMPLES IN THE PHENO PHILE
			for (SampleKey sampleKey : wrSampleKeys) {
				if (!samplesInfoMap.containsKey(sampleKey)) {
					String sex = "0";
					SampleInfo info = new SampleInfo();
					for (SampleInfo sampleInfo : sampleInfos) {
						SampleKey tmpSampleKey = sampleInfo.getKey();
						if (tmpSampleKey.equals(sampleKey)) {
							info = sampleInfo;
							break;
						}
					}
					samplesInfoMap.put(sampleKey, info);
				}
			}
		}

		return samplesInfoMap;
	}

//	private void writeMetadata(
//			MarkerCensusOperationDataSet dataSet,
//			MarkerSet rdMarkerSet,
//			Collection<MarkerKey> wrMarkerKeys,
//			Collection<SampleKey> wrSampleKeys)
//			throws IOException
//	{
//		// MARKERSET MARKERID
//		ArrayChar.D2 markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(wrMarkerKeys, cNetCDF.Strides.STRIDE_MARKER_NAME);
//		int[] markersOrig = new int[]{0, 0};
//		wrNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
//
//		// MARKERSET RSID
//		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
//		Map<MarkerKey, char[]> wrSortedMarkerRsIds = org.gwaspi.global.Utils.createOrderedMap(wrMarkerKeys, rdMarkerSet.getMarkerIdSetMapCharArray());
//		NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, wrSortedMarkerRsIds.values(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);
//
//		// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
//		ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(wrSampleKeys, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
//		int[] sampleOrig = new int[]{0, 0};
//		wrNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
//		log.info("Done writing Sample Set to operation");
//	}

	private static void censusDataWriter(
			MarkerCensusOperationDataSet dataSet,
			Map<MarkerKey, CensusFull> wrChunkedMarkerCensusMap,
			Map<MarkerKey, byte[]> wrChunkedKnownAllelesMap,
			int countChunks,
			int chunkSize)
			throws IOException
	{
		// KNOWN ALLELES
		NetCdfUtils.saveCharMapToWrMatrix(
				wrNcFile,
				wrChunkedKnownAllelesMap.values(),
				cNetCDF.Variables.VAR_ALLELES,
				cNetCDF.Strides.STRIDE_GT,
				countChunks * chunkSize);

		// ALL CENSUS
		NetCdfUtils.saveIntMapD2ToWrMatrix(
				wrNcFile,
				wrChunkedMarkerCensusMap.values(),
				CensusFull.EXTRACTOR_ALL,
				cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL,
				countChunks * chunkSize);

		// CASE CENSUS
		NetCdfUtils.saveIntMapD2ToWrMatrix(
				wrNcFile,
				wrChunkedMarkerCensusMap.values(),
				CensusFull.EXTRACTOR_CASE,
				cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE,
				countChunks * chunkSize);

		// CONTROL CENSUS
		NetCdfUtils.saveIntMapD2ToWrMatrix(
				wrNcFile,
				wrChunkedMarkerCensusMap.values(),
				CensusFull.EXTRACTOR_CONTROL,
				cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL,
				countChunks * chunkSize);

		// ALTERNATE HW CENSUS
		NetCdfUtils.saveIntMapD2ToWrMatrix(
				wrNcFile,
				wrChunkedMarkerCensusMap.values(),
				CensusFull.EXTRACTOR_ALTERNATE_HW,
				cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW,
				countChunks * chunkSize);
	}
}

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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import ucar.ma2.ArrayChar;
//import ucar.ma2.Index;
//import ucar.ma2.InvalidRangeException;
//import ucar.nc2.NetcdfFile;
//import ucar.nc2.NetcdfFileWriteable;

/**
 * Extracts Genotypes to a new matrix.
 */
public class MatrixDataExtractor implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(MatrixDataExtractor.class);

	private MatrixKey rdMatrixKey;
	/**
	 * All the criteria to pick markers, including the directly supplied ones,
	 * and the ones read from the marker criteria file.
	 */
	private Set fullMarkerCriteria;
	private File markerCriteriaFile;
	private SetMarkerPickCase markerPickCase;
	private String markerPickerVar;
	/**
	 * All the criteria to pick samples, including the directly supplied ones,
	 * and the ones read from the sample criteria file.
	 */
	private Set fullSampleCriteria;
	private File sampleCriteriaFile;
	private SetSamplePickCase samplePickCase;
	private String samplePickerVar;
	private final int sampleFilterPos;

	private final DataSetSource dataSetSource;
	private final DataSetDestination dataSetDestination;

	/**
	 * This constructor to extract data from Matrix a by passing a variable and
	 * the criteria to filter items by.
	 *
	 * @param rdMatrixKey
	 * @param wrMatrixFriendlyName
	 * @param wrMatrixDescription
	 * @param markerPickCase
	 * @param samplePickCase
	 * @param markerPickerVar
	 * @param samplePickerVar
	 * @param markerCriteria
	 * @param sampleCriteria
	 * @param sampleFilterPos
	 * @param markerPickerFile
	 * @param samplePickerFile
	 * @throws IOException
	 */
	public MatrixDataExtractor(
			MatrixKey rdMatrixKey,
			String wrMatrixFriendlyName,
			String wrMatrixDescription,
			SetMarkerPickCase markerPickCase,
			SetSamplePickCase samplePickCase,
			String markerPickerVar,
			String samplePickerVar,
			Set markerCriteria,
			Set sampleCriteria,
			int sampleFilterPos,
			File markerPickerFile,
			File samplePickerFile)
			throws IOException
	{
		// INIT EXTRACTOR OBJECTS
		this.markerPickCase = markerPickCase;
		this.markerPickerVar = markerPickerVar;
		this.samplePickCase = samplePickCase;
		this.samplePickerVar = samplePickerVar;
		this.markerCriteriaFile = markerPickerFile;
		this.sampleCriteriaFile = samplePickerFile;
		this.sampleFilterPos = sampleFilterPos;

		this.rdMatrixKey = rdMatrixKey;

		this.fullMarkerCriteria = new HashSet();
		this.fullMarkerCriteria.addAll(markerCriteria);
		// Pick markerId by criteria file
		this.fullMarkerCriteria.addAll(parseMarkerPickerFile(markerPickerFile, markerPickCase));

		this.fullSampleCriteria = new HashSet();
		this.fullSampleCriteria.addAll(sampleCriteria);
		this.fullSampleCriteria.addAll(parseSamplePickerFile(samplePickerFile, samplePickCase, samplePickerVar, rdMatrixKey.getStudyKey()));
	}

	public static Collection<?> parseMarkerPickerFile(File markerPickerFile, SetMarkerPickCase markerPickCase) throws IOException {

		Collection<?> markerCriteria = new LinkedList();

		// Pick markerId by criteria file
		if (!markerPickerFile.toString().isEmpty() && markerPickerFile.isFile()) {
			FileReader fr = new FileReader(markerPickerFile);
			BufferedReader br = new BufferedReader(fr);
			String l;
			while ((l = br.readLine()) != null) {
				if ((markerPickCase == SetMarkerPickCase.MARKERS_INCLUDE_BY_ID)
						|| (markerPickCase == SetMarkerPickCase.MARKERS_EXCLUDE_BY_ID))
				{
					((Set<MarkerKey>) markerCriteria).add(MarkerKey.valueOf(l));
				} else {
					// markerPickerVar is one of:
					// - marker Chromosome  (cNetCDF.Variables.VAR_MARKERS_CHR)
					// - marker ID          (cNetCDF.Variables.VAR_MARKERSET)
					// - marker RS-ID       (cNetCDF.Variables.VAR_MARKERS_RSID)
					// which are all String types, and thus we can
					// always use char[] here
					((Set<char[]>) markerCriteria).add(l.toCharArray());
				}
			}
			br.close();
		}

		return markerCriteria;
	}

	public static Collection<?> parseSamplePickerFile(File samplePickerFile, SetSamplePickCase samplePickCase, String samplePickerVar, StudyKey studyKey) throws IOException {

		Collection<?> sampleCriteria = new LinkedList();

		// USE cNetCDF Key and criteria or list file
		if (!samplePickerFile.toString().isEmpty() && samplePickerFile.isFile()) {
			FileReader fr = new FileReader(samplePickerFile);
			BufferedReader br = new BufferedReader(fr);
			String l;
			while ((l = br.readLine()) != null) {
				if ((samplePickCase == SetSamplePickCase.SAMPLES_INCLUDE_BY_ID)
						|| (samplePickCase == SetSamplePickCase.SAMPLES_EXCLUDE_BY_ID))
				{
					((Set<SampleKey>) sampleCriteria).add(SampleKey.valueOf(studyKey, l));
				} else {
					// samplePickerVar is one of:
					// - sample affection   (cDBSamples.f_AFFECTION)
					// - sample age         (cDBSamples.f_AGE)
					// - sample category    (cDBSamples.f_CATEGORY)
					// - sample disease     (cDBSamples.f_DISEASE)
					// - sample family ID   (cDBSamples.f_FAMILY_ID)
					// - sample population  (DBSamples.f_POPULATION)
					// - sample ID          (cDBSamples.f_SAMPLE_ID)
					// - sample sex         (cDBSamples.f_SEX)
					// which use different types (String, Sex, Affection, int),
					// and thus we have to support multiple types here
					((Set<Object>) sampleCriteria).add(cDBSamples.parseFromField(samplePickerVar, l));
				}
			}
			br.close();
		}

		return sampleCriteria;
	}

	/**
	 * @return key & index in the original set for all picked markers.
	 */
	private static Map<MarkerKey, Integer> pickMarkers(SetMarkerPickCase markerPickCase, MarkerSet rdMarkerSet, Set markerCriteria, String markerPickerVar) {

		Map<MarkerKey, Integer> wrMarkers;

		Collection<MarkerKey> wrMarkerKeys;
		switch (markerPickCase) {
			case MARKERS_INCLUDE_BY_NETCDF_CRITERIA:
				// Pick by netCDF field value and criteria
				wrMarkerKeys = rdMarkerSet.pickValidMarkerSetItemsByValue(markerPickerVar, (Set<char[]>) markerCriteria, true).keySet();
				break;
			case MARKERS_EXCLUDE_BY_NETCDF_CRITERIA:
				// Exclude by netCDF field value and criteria
				wrMarkerKeys = rdMarkerSet.pickValidMarkerSetItemsByValue(markerPickerVar, (Set<char[]>) markerCriteria, false).keySet();
				break;
			case MARKERS_INCLUDE_BY_ID:
				wrMarkerKeys = rdMarkerSet.pickValidMarkerSetItemsByKey((Set<MarkerKey>) markerCriteria, true).keySet();
				break;
			case MARKERS_EXCLUDE_BY_ID:
				wrMarkerKeys = rdMarkerSet.pickValidMarkerSetItemsByKey((Set<MarkerKey>) markerCriteria, false).keySet();
				break;
			case ALL_MARKERS:
			default:
				// Get all markers
				wrMarkerKeys = rdMarkerSet.getMarkerKeys();
		}
		wrMarkers = new LinkedHashMap<MarkerKey, Integer>(wrMarkerKeys.size());
		List<MarkerKey> rdMarkersKeysList = new ArrayList<MarkerKey>(rdMarkerSet.getMarkerKeys());
		for (MarkerKey markerKey : wrMarkerKeys) {
			wrMarkers.put(markerKey, rdMarkersKeysList.indexOf(markerKey));
		}

		return wrMarkers;
	}

	/**
	 * @return key & index in the original set for all picked markers.
	 */
	private static Map<SampleKey, Integer> pickSamples(SetSamplePickCase samplePickCase, SampleSet rdSampleSet, Set sampleCriteria, String samplePickerVar, StudyKey studyKey, int sampleFilterPos) throws IOException {

		Map<SampleKey, ?> rdSampleSetMap = rdSampleSet.getSampleIdSetMapCharArray();

		Map<SampleKey, Integer> pickedSamples = new LinkedHashMap<SampleKey, Integer>(rdSampleSetMap.size());
		switch (samplePickCase) {
			case ALL_SAMPLES:
				// Get all samples
				int i = 0;
				for (SampleKey key : rdSampleSetMap.keySet()) {
					pickedSamples.put(key, i);
					i++;
				}
				break;
			case SAMPLES_INCLUDE_BY_NETCDF_FILTER:
				// USE cNetCDF Filter Data and criteria
				pickedSamples = rdSampleSet.pickValidSampleSetItemsByNetCDFFilter((Map<SampleKey, char[]>) rdSampleSetMap, samplePickerVar, sampleFilterPos, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_NETCDF_FILTER:
				// USE cNetCDF Filter Data and criteria
				pickedSamples = rdSampleSet.pickValidSampleSetItemsByNetCDFFilter((Map<SampleKey, char[]>) rdSampleSetMap, samplePickerVar, sampleFilterPos, sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_NETCDF_CRITERIA:
				// USE cNetCDF Value and criteria
				pickedSamples = rdSampleSet.pickValidSampleSetItemsByNetCDFValue(rdSampleSetMap, samplePickerVar, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_NETCDF_CRITERIA:
				// USE cNetCDF Value and criteria
				pickedSamples = rdSampleSet.pickValidSampleSetItemsByNetCDFValue(rdSampleSetMap, samplePickerVar, sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_ID:
				pickedSamples = SampleSet.pickValidSampleSetItemsByNetCDFKey(rdSampleSetMap.keySet(), (Set<SampleKey>) sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_ID:
				pickedSamples = SampleSet.pickValidSampleSetItemsByNetCDFKey(rdSampleSetMap.keySet(), (Set<SampleKey>) sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_DB_FIELD:
				// USE DB DATA
				pickedSamples = SampleSet.pickValidSampleSetItemsByDBField(studyKey, rdSampleSetMap.keySet(), samplePickerVar, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_DB_FIELD:
				// USE DB DATA
				pickedSamples = SampleSet.pickValidSampleSetItemsByDBField(studyKey, rdSampleSetMap.keySet(), samplePickerVar, sampleCriteria, false);
				break;
			default:
				int j = 0;
				for (Map.Entry<SampleKey, Integer> entry : pickedSamples.entrySet()) {
					entry.setValue(j);
					j++;
				}
		}

		return pickedSamples;
	}

	public Set<?> getFullMarkerCriteria() {
		return fullMarkerCriteria;
	}

	public Set<?> getFullSampleCriteria() {
		return fullSampleCriteria;
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

		int resultMatrixId = Integer.MIN_VALUE;

		// MARKERSET PICKING
		MarkerSet rdMarkerSet = new MarkerSet(this.rdMatrixKey);
		rdMarkerSet.initFullMarkerIdSetMap();
		// Contains key & index in the original set for all to be extracted markers.
		Map<MarkerKey, Integer> wrMarkers = pickMarkers(markerPickCase, rdMarkerSet, fullMarkerCriteria, markerPickerVar);
		if (wrMarkers.isEmpty()) {
			// XXX maybe we should instead trhow an IOException?
			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
			return resultMatrixId;
		}

//		// RETRIEVE CHROMOSOMES INFO
//		Map<ChromosomeKey, ChromosomeInfo> rdChromosomeInfo;
//		this.rdMarkerSet.fillMarkerSetMapWithChrAndPos();
//		Map<MarkerKey, MarkerMetadata> sortedChrAndPos = org.gwaspi.global.Utils.createOrderedMap(wrMarkers.keySet(), this.rdMarkerSet.getMarkerMetadata());
//		this.rdChromosomeInfo = ChromosomeUtils.aggregateChromosomeInfo(sortedChrAndPos, 0, 1);

		// SAMPLESET PICKING
		SampleSet rdSampleSet = new SampleSet(this.rdMatrixKey);
		// Contains key & index in the original set for all to be extracted samples.
		Map<SampleKey, Integer> wrSampleSetMap = pickSamples(samplePickCase, rdSampleSet, fullSampleCriteria, samplePickerVar, rdMatrixKey.getStudyKey(), sampleFilterPos);
		if (wrSampleSetMap.isEmpty()) {
			// XXX maybe we should instead trhow an IOException?
			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
			return resultMatrixId;
		}

		if (wrSampleSetMap.size() > 0 && wrMarkers.size() > 0) {
//				// CREATE netCDF-3 FILE
//				StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
//				descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
//				descSB.append("\nThrough Matrix extraction from parent Matrix MX: ").append(rdMatrixMetadata.getMatrixId()).append(" - ").append(rdMatrixMetadata.getMatrixFriendlyName());
//
//				descSB.append("\nMarker Filter Variable: ");
//				String pickPrefix = "All Markers";
//				if (markerPickCase.toString().contains("EXCLUDE")) {
//					pickPrefix = "Exclude by ";
//				} else if (markerPickCase.toString().contains("INCLUDE")) {
//					pickPrefix = "Include by ";
//				}
//				descSB.append(pickPrefix).append(markerPickerVar.replaceAll("_", " ").toUpperCase());
//				if (markerCriteriaFile.isFile()) {
//					descSB.append("\nMarker Criteria File: ");
//					descSB.append(markerCriteriaFile.getPath());
//				} else if (!pickPrefix.equals("All Markers")) {
//					descSB.append("\nMarker Criteria: ");
//					descSB.append(markerPickerCriteria.deleteCharAt(markerPickerCriteria.length() - 1));
//				}
//
//				descSB.append("\nSample Filter Variable: ");
//				pickPrefix = "All Samples";
//				if (samplePickCase.toString().contains("EXCLUDE")) {
//					pickPrefix = "Exclude by ";
//				} else if (samplePickCase.toString().contains("INCLUDE")) {
//					pickPrefix = "Include by ";
//				}
//				descSB.append(pickPrefix).append(samplePickerVar.replaceAll("_", " ").toUpperCase());
//				if (sampleCriteriaFile.isFile()) {
//					descSB.append("\nSample Criteria File: ");
//					descSB.append(sampleCriteriaFile.getPath());
//				} else if (!pickPrefix.equals("All Samples")) {
//					descSB.append("\nSample Criteria: ");
//					descSB.append(samplePickerCriteria.deleteCharAt(samplePickerCriteria.length() - 1));
//				}
//
//				if (!wrMatrixDescription.isEmpty()) {
//					descSB.append("\n\nDescription: ");
//					descSB.append(wrMatrixDescription);
//					descSB.append("\n");
//				}
////				descSB.append("\nGenotype encoding: ");
////				descSB.append(rdMatrixMetadata.getGenotypeEncoding());
//				descSB.append("\n");
//				descSB.append("Markers: ").append(wrMarkers.size()).append(", Samples: ").append(wrSampleSetMap.size());
//
//				MatrixFactory wrMatrixHandler = new MatrixFactory(
//						rdMatrixMetadata.getTechnology(), // technology
//						wrMatrixFriendlyName,
//						descSB.toString(), // description
//						rdMatrixMetadata.getGenotypeEncoding(), // Matrix genotype encoding from orig matrix genotype encoding
//						rdMatrixMetadata.getStrand(),
//						rdMatrixMetadata.getHasDictionray(), // has dictionary?
//						wrSampleSetMap.size(),
//						wrMarkers.size(),
//						rdChromosomeInfo.size(),
//						rdMatrixKey, // Orig matrixId 1
//						null); // Orig matrixId 2
//
//				resultMatrixId = wrMatrixHandler.getResultMatrixId();





			dataSetDestination.init();

			// use only the selected sample infos
			dataSetDestination.startLoadingSampleInfos(true);
			for (SampleKey sampleKey : wrSampleSetMap.keySet()) {
				dataSetDestination.addSampleKey(sampleKey);
			}
			dataSetDestination.finishedLoadingSampleInfos();

			// use only the selected markers metadata
			dataSetDestination.startLoadingMarkerMetadatas(true); // FIXME This is not yet supported. we may have to read the whole marker metadatas from dataSetSource, and wrtie them to dataSetDestination
			for (MarkerKey markerKey : wrMarkers.keySet()) {
				dataSetDestination.addMarkerKey(markerKey);
			}
			dataSetDestination.finishedLoadingMarkerMetadatas();

			// chromosomes infos
			// NOTE just let the auto-extraction of chromosomes from the markers metadatas kick in






//				NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
//				wrNcFile.create();
//				log.trace("Done creating netCDF handle: " + wrNcFile.toString());

				//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
				// WRITING METADATA TO MATRIX

//				// SAMPLESET
//				ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(wrSampleSetMap.keySet(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);
//				int[] sampleOrig = new int[]{0, 0};
//				wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
//				log.info("Done writing SampleSet to matrix");

//				// MARKERSET MARKERID
//				ArrayChar.D2 markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(wrMarkerKeys, cNetCDF.Strides.STRIDE_MARKER_NAME);
//				int[] markersOrig = new int[]{0, 0};
//				wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
//
//				// MARKERSET RSID
//				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
//				Map<MarkerKey, char[]> sortedMarkerRSIDs = org.gwaspi.global.Utils.createOrderedMap(wrMarkerKeys, rdMarkerSet.getMarkerIdSetMapCharArray());
//				NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerRSIDs.values(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);
//
//				// MARKERSET CHROMOSOME
//				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
//				Map<MarkerKey, char[]> sortedMarkerChrs = org.gwaspi.global.Utils.createOrderedMap(wrMarkerKeys, rdMarkerSet.getMarkerIdSetMapCharArray());
//				NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerChrs.values(), cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);
//
////				// Set of chromosomes found in matrix along with number of markersinfo
////				NetCdfUtils.saveObjectsToStringToMatrix(wrNcFile, rdChromosomeInfo.keySet(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
////				// Number of marker per chromosome & max pos for each chromosome
////				int[] columns = new int[] {0, 1, 2, 3};
////				NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(wrNcFile, rdChromosomeInfo.values(), columns, cNetCDF.Variables.VAR_CHR_INFO);
//
//				// MARKERSET POSITION
//				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
//				Map<MarkerKey, Integer> sortedMarkerPos = org.gwaspi.global.Utils.createOrderedMap(wrMarkerKeys, rdMarkerSet.getMarkerIdSetMapInteger());
//				//Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
//				NetCdfUtils.saveIntMapD1ToWrMatrix(wrNcFile, sortedMarkerPos.values(), cNetCDF.Variables.VAR_MARKERS_POS);
//
//				// MARKERSET DICTIONARY ALLELES
//				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
//				Map<MarkerKey, char[]> sortedMarkerBasesDicts = org.gwaspi.global.Utils.createOrderedMap(wrMarkerKeys, rdMarkerSet.getMarkerIdSetMapCharArray());
//				NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerBasesDicts.values(), cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);
//
//				// GENOTYPE STRAND
//				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
//				Map<MarkerKey, char[]> sortedMarkerGTStrands = org.gwaspi.global.Utils.createOrderedMap(wrMarkerKeys, rdMarkerSet.getMarkerIdSetMapCharArray());
//				NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, sortedMarkerGTStrands.values(), cNetCDF.Variables.VAR_GT_STRAND, 3);
//				//</editor-fold>



			// GENOTYPES WRITER
			// Iterate through wrSampleSetMap, use item position to read correct sample GTs into rdMarkerIdSetMap.
//				int sampleWrIndex = 0;
//				for (Integer rdSampleIndices : wrSampleSetMap.values()) {
//					// Iterate through wrMarkerIdSetMap, get the correct GT from rdMarkerIdSetMap
//					rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(rdSampleIndices);
////					rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleWrPos);
//					Map<MarkerKey, byte[]> sortedRdPos = org.gwaspi.global.Utils.createOrderedMap(wrMarkerKeys, rdMarkerSet.getMarkerIdSetMapByteArray());
//
//					// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
//					NetCdfUtils.saveSingleSampleGTsToMatrix(wrNcFile, sortedRdPos.values(), sampleWrIndex);
//					sampleWrIndex++;
//					if ((sampleWrIndex == 1) || ((sampleWrIndex % 100) == 0)) {
//						log.info("Samples copied: {} / {}", sampleWrIndex, wrSampleSetMap.size());
//					}
//				}

			SamplesGenotypesSource samplesGenotypesSource = dataSetSource.getSamplesGenotypesSource();
			int sampleWrIndex = 0;
			for (int rdSampleIndex : wrSampleSetMap.values()) {
				GenotypesList sampleGenotypes = samplesGenotypesSource.get(rdSampleIndex);
				List<byte[]> wrSampleGenotypes = new ArrayList<byte[]>(wrMarkers.size());
				for (int rdMarkerIndex : wrMarkers.values()) {
					wrSampleGenotypes.add(sampleGenotypes.get(rdMarkerIndex));
				}

				dataSetDestination.addSampleGTAlleles(sampleWrIndex, wrSampleGenotypes);
				sampleWrIndex++;
				if ((sampleWrIndex == 1) || ((sampleWrIndex % 100) == 0)) {
					log.info("Samples copied: {} / {}", sampleWrIndex, wrSampleSetMap.size());
				}
			}

			dataSetDestination.done();



//				// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
//				// GENOTYPE ENCODING
//				ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
//				Index index = guessedGTCodeAC.getIndex();
//				guessedGTCodeAC.setString(index.set(0, 0), rdMatrixMetadata.getGenotypeEncoding().toString());
//				int[] origin = new int[] {0, 0};
//				wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);
//
//				descSB.append("\nGenotype encoding: ");
//				descSB.append(rdMatrixMetadata.getGenotypeEncoding());
//
//				MatrixMetadata resultMatrixMetadata = wrMatrixHandler.getResultMatrixMetadata();
//				resultMatrixMetadata.setDescription(descSB.toString());
//				MatricesList.updateMatrix(resultMatrixMetadata);

//				wrNcFile.close();

			org.gwaspi.global.Utils.sysoutCompleted("Extraction to new Matrix");
		} else {
			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
		}

		return resultMatrixId;
	}
}

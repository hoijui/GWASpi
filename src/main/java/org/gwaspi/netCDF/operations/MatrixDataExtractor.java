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
	private static Map<SampleKey, Integer> pickSamples(SetSamplePickCase samplePickCase, DataSetSource dataSetSource, SampleSet rdSampleSet, Set sampleCriteria, String samplePickerVar, StudyKey studyKey, int sampleFilterPos) throws IOException {

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
				rdSampleSet.fillSampleIdSetMapWithFilterVariable((Map<SampleKey, char[]>) rdSampleSetMap, samplePickerVar, sampleFilterPos);
				pickedSamples = pickValidSampleSetItemsByNetCDFFilter((Map<SampleKey, char[]>) rdSampleSetMap, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_NETCDF_FILTER:
				// USE cNetCDF Filter Data and criteria
				rdSampleSet.fillSampleIdSetMapWithFilterVariable((Map<SampleKey, char[]>) rdSampleSetMap, samplePickerVar, sampleFilterPos);
				pickedSamples = pickValidSampleSetItemsByNetCDFFilter((Map<SampleKey, char[]>) rdSampleSetMap, sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_NETCDF_CRITERIA:
				// USE cNetCDF Value and criteria
				rdSampleSet.fillSampleIdSetMapWithVariable(rdSampleSetMap, samplePickerVar);
				pickedSamples = pickValidSampleSetItemsByNetCDFValue(rdSampleSetMap, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_NETCDF_CRITERIA:
				// USE cNetCDF Value and criteria
				rdSampleSet.fillSampleIdSetMapWithVariable(rdSampleSetMap, samplePickerVar);
				pickedSamples = pickValidSampleSetItemsByNetCDFValue(rdSampleSetMap, sampleCriteria, false);
				break;
			case SAMPLES_INCLUDE_BY_ID:
				pickedSamples = pickValidSampleSetItemsByNetCDFKey(rdSampleSetMap.keySet(), (Set<SampleKey>) sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_ID:
				pickedSamples = pickValidSampleSetItemsByNetCDFKey(rdSampleSetMap.keySet(), (Set<SampleKey>) sampleCriteria, false);
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
		Map<SampleKey, Integer> wrSampleSetMap = pickSamples(samplePickCase, dataSetSource, rdSampleSet, fullSampleCriteria, samplePickerVar, rdMatrixKey.getStudyKey(), sampleFilterPos);
		if (wrSampleSetMap.isEmpty()) {
			// XXX maybe we should instead trhow an IOException?
			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
			return resultMatrixId;
		}

		if (wrSampleSetMap.size() > 0 && wrMarkers.size() > 0) {
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


			// GENOTYPES WRITER
			// Iterate through wrSampleSetMap, use item position to read correct sample GTs into rdMarkerIdSetMap.

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

			org.gwaspi.global.Utils.sysoutCompleted("Extraction to new Matrix");
		} else {
			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
		}

		return resultMatrixId;
	}

	private static Map<SampleKey, Integer> pickValidSampleSetItemsByNetCDFKey(Set<SampleKey> sampleKeys, Set<SampleKey> criteria, boolean include) throws IOException {
		Map<SampleKey, Integer> returnMap = new LinkedHashMap<SampleKey, Integer>();

		int pickCounter = 0;
		if (include) {
			for (SampleKey key : sampleKeys) {
				if (criteria.contains(key)) {
					returnMap.put(key, pickCounter);
				}
				pickCounter++;
			}
		} else {
			for (SampleKey key : sampleKeys) {
				if (!criteria.contains(key)) {
					returnMap.put(key, pickCounter);
				}
				pickCounter++;
			}
		}

		return returnMap;
	}

	private static <V> Map<SampleKey, Integer> pickValidSampleSetItemsByNetCDFValue(Map<SampleKey, V> map, Set<V> criteria, boolean include) throws IOException {

		Map<SampleKey, Integer> returnMap = new LinkedHashMap<SampleKey, Integer>();

		int pickCounter = 0;
		if (include) {
			for (Map.Entry<SampleKey, V> entry : map.entrySet()) {
				if (criteria.contains(entry.getValue())) {
					returnMap.put(entry.getKey(), pickCounter);
				}
				pickCounter++;
			}
		} else {
			for (Map.Entry<SampleKey, V> entry : map.entrySet()) {
				if (!criteria.contains(entry.getValue())) {
					returnMap.put(entry.getKey(), pickCounter);
				}
				pickCounter++;
			}
		}

		return returnMap;
	}

	private static Map<SampleKey, Integer> pickValidSampleSetItemsByNetCDFFilter(Map<SampleKey, char[]> map, Set<char[]> criteria, boolean include) throws IOException {

		Map<SampleKey, Integer> returnMap = new LinkedHashMap<SampleKey, Integer>();

		int pickCounter = 0;
		if (include) {
			for (Map.Entry<SampleKey, char[]> entry : map.entrySet()) {
				if (criteria.contains(entry.getValue())) { // FIXME bad comparison of arrays (should check individual entries)
					returnMap.put(entry.getKey(), pickCounter);
				}
				pickCounter++;
			}
		} else {
			for (Map.Entry<SampleKey, char[]> entry : map.entrySet()) {
				if (!criteria.contains(entry.getValue())) { // FIXME bad comparison of arrays (should check individual entries)
					returnMap.put(entry.getKey(), pickCounter);
				}
				pickCounter++;
			}
		}

		return returnMap;
	}
}

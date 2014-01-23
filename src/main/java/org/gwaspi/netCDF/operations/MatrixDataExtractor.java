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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.constants.cNetCDF.Variables;
import org.gwaspi.global.Text;
import org.gwaspi.global.Extractor;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final Set fullMarkerCriteria;
	private final SetMarkerPickCase markerPickCase;
	private final String markerPickerVar;
	/**
	 * All the criteria to pick samples, including the directly supplied ones,
	 * and the ones read from the sample criteria file.
	 */
	private final Set fullSampleCriteria;
	private final SetSamplePickCase samplePickCase;
	private final String samplePickerVar;
	private final int sampleFilterPos;

	private final DataSetSource dataSetSource;
	private final DataSetDestination dataSetDestination;

	private static interface Picker<K> {

		Map<K, Integer> pick(DataSetSource dataSetSource) throws IOException;
	}

	private static abstract class AbstractKeyPicker<K> implements Picker<K> {

		private final Collection<K> criteria;
		private final boolean include;

		/**
		 * @param include whether this is an include or an exclude picker.
		 */
		AbstractKeyPicker(Collection<K> criteria, boolean include) {

			this.criteria = criteria;
			this.include = include;
		}

		abstract Collection<K> getInputKeys(DataSetSource dataSetSource) throws IOException;

		@Override
		public Map<K, Integer> pick(DataSetSource dataSetSource) throws IOException {

			Map<K, Integer> result = new LinkedHashMap<K, Integer>();

			int originalIndex = 0;
			if (include) {
				for (K inputKey : getInputKeys(dataSetSource)) {
					if (criteria.contains(inputKey)) {
						result.put(inputKey, originalIndex);
					}
					originalIndex++;
				}
			} else {
				for (K inputKey : getInputKeys(dataSetSource)) {
					if (!criteria.contains(inputKey)) {
						result.put(inputKey, originalIndex);
					}
					originalIndex++;
				}
			}

			return result;
		}
	}

	private static class SampleKeyPicker extends AbstractKeyPicker<SampleKey> {

		SampleKeyPicker(Collection<SampleKey> criteria, boolean include) {
			super(criteria, include);
		}

		@Override
		public Collection<SampleKey> getInputKeys(DataSetSource dataSetSource) throws IOException {
			return dataSetSource.getSamplesKeysSource();
		}
	}

	private static class MarkerKeyPicker extends AbstractKeyPicker<MarkerKey> {

		MarkerKeyPicker(Collection<MarkerKey> criteria, boolean include) {
			super(criteria, include);
		}

		@Override
		public Collection<MarkerKey> getInputKeys(DataSetSource dataSetSource) throws IOException {
			return dataSetSource.getMarkersKeysSource();
		}
	}

	private static abstract class AbstractValuePicker<K, V, M> implements Picker<K> {

		private final Collection<M> criteria;
		private final Extractor<V, M> typeConverter;
		private final boolean include;

		/**
		 * @param include whether this is an include or an exclude picker.
		 */
		AbstractValuePicker(Collection<M> criteria, Extractor<V, M> typeConverter, boolean include) {

			this.criteria = criteria;
			this.typeConverter = typeConverter;
			this.include = include;
		}

		abstract Collection<K> getInputKeys(DataSetSource dataSetSource) throws IOException;
		abstract Collection<V> getInputValues(DataSetSource dataSetSource) throws IOException;

		@Override
		public Map<K, Integer> pick(DataSetSource dataSetSource) throws IOException {

			Map<K, Integer> result = new LinkedHashMap<K, Integer>();

			int originalIndex = 0;
			Iterator<K> keysIt = getInputKeys(dataSetSource).iterator();
			if (include) {
				for (V value : getInputValues(dataSetSource)) {
					K key = keysIt.next();
					if (criteria.contains(typeConverter.extract(value))) {
						result.put(key, originalIndex);
					}
					originalIndex++;
				}
			} else {
				for (V value : getInputValues(dataSetSource)) {
					K key = keysIt.next();
					if (!criteria.contains(typeConverter.extract(value))) {
						result.put(key, originalIndex);
					}
					originalIndex++;
				}
			}

			return result;
		}
	}

	private abstract static class AbstractMarkerValuePicker<M> extends AbstractValuePicker<MarkerKey, MarkerMetadata, M> {

		AbstractMarkerValuePicker(Collection<M> criteria, Extractor<MarkerMetadata, M> typeConverter, boolean include) {
			super(criteria, typeConverter, include);
		}

		@Override
		public Collection<MarkerKey> getInputKeys(DataSetSource dataSetSource) throws IOException {
			return dataSetSource.getMarkersKeysSource();
		}

		@Override
		public Collection<MarkerMetadata> getInputValues(DataSetSource dataSetSource) throws IOException {
			return dataSetSource.getMarkersMetadatasSource();
		}
	}

	private static class NetCdfVariableMarkerValuePicker<M> extends AbstractMarkerValuePicker<M> {

		private static final Map<String, Extractor<MarkerMetadata, ?>> typeConverters;
		static {
			typeConverters = new HashMap<String, Extractor<MarkerMetadata, ?>>();
			typeConverters.put(Variables.VAR_MARKERSET, MarkerMetadata.TO_MARKER_ID);
			typeConverters.put(Variables.VAR_MARKERS_RSID, MarkerMetadata.TO_RS_ID);
			typeConverters.put(Variables.VAR_MARKERS_BASES_DICT, MarkerMetadata.TO_ALLELES);
			typeConverters.put(Variables.VAR_MARKERS_CHR, MarkerMetadata.TO_CHR);
			typeConverters.put(Variables.VAR_MARKERS_POS, MarkerMetadata.TO_POS);
		}

		NetCdfVariableMarkerValuePicker(Collection<M> criteria, String variable, boolean include) {
			super(criteria, (Extractor<MarkerMetadata, M>) typeConverters.get(variable), include);
		}
	}

	private abstract static class AbstractSampleValuePicker<M> extends AbstractValuePicker<SampleKey, SampleInfo, M> {

		AbstractSampleValuePicker(Collection<M> criteria, Extractor<SampleInfo, M> typeConverter, boolean include) {
			super(criteria, typeConverter, include);
		}

		@Override
		public Collection<SampleKey> getInputKeys(DataSetSource dataSetSource) throws IOException {
			return dataSetSource.getSamplesKeysSource();
		}

		@Override
		public Collection<SampleInfo> getInputValues(DataSetSource dataSetSource) throws IOException {
			return dataSetSource.getSamplesInfosSource();
		}
	}

//	private abstract static class NetCdfVariableSampleValuePicker<M> extends AbstractSampleValuePicker<M> {
//
//		private static final Map<String, Extractor<SampleInfo, ?>> typeConverters;
//		static {
//			typeConverters = new HashMap<String, Extractor<SampleInfo, ?>>();
//			typeConverters.put(Variables.VAR_SAMPLE_KEY, SampleInfo.TO_SAMPLE_ID);
//			typeConverters.put(Variables.VAR_SAMPLES_AFFECTION, SampleInfo.TO_AFFECTION);
//			typeConverters.put(Variables.VAR_SAMPLES_SEX, SampleInfo.TO_SEX);
//		}
//
//		NetCdfVariableSampleValuePicker(Collection<M> criteria, String variable, boolean include) {
//			super(criteria, (Extractor<SampleInfo, M>) typeConverters.get(variable), include);
//		}
//	}

	/**
	 * This constructor to extract data from Matrix a by passing a variable and
	 * the criteria to filter items by.
	 */
	public MatrixDataExtractor(
			DataSetSource dataSetSource,
			DataSetDestination dataSetDestination,
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
		this.dataSetSource = dataSetSource;
		this.dataSetDestination = dataSetDestination;

		// INIT EXTRACTOR OBJECTS
		this.markerPickCase = markerPickCase;
		this.markerPickerVar = markerPickerVar;
		this.samplePickCase = samplePickCase;
		this.samplePickerVar = samplePickerVar;
		this.sampleFilterPos = sampleFilterPos;

		this.fullMarkerCriteria = new HashSet();
		this.fullMarkerCriteria.addAll(markerCriteria);
		// Pick markerId by criteria file
		this.fullMarkerCriteria.addAll(parseMarkerPickerFile(markerPickerFile, markerPickCase));

		this.fullSampleCriteria = new HashSet();
		this.fullSampleCriteria.addAll(sampleCriteria);
		this.fullSampleCriteria.addAll(parseSamplePickerFile(samplePickerFile, samplePickCase, samplePickerVar, rdMatrixKey.getStudyKey()));
	}

	@Override
	public boolean isCreatingResultMatrix() {
		return true; // XXX We might want to change this in the future, as this could be converted to create only an operation, instead of a full new matrix
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
//				if ((samplePickCase == SetSamplePickCase.SAMPLES_INCLUDE_BY_ID)
//						|| (samplePickCase == SetSamplePickCase.SAMPLES_EXCLUDE_BY_ID))
//				{
//					((Set<SampleKey>) sampleCriteria).add(SampleKey.valueOf(studyKey, l));
//				} else {
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
//				}
			}
			br.close();
		}

		return sampleCriteria;
	}

	/**
	 * @return key & index in the original set for all picked markers.
	 */
	private static Map<MarkerKey, Integer> pickMarkers(SetMarkerPickCase markerPickCase, DataSetSource dataSetSource/*, MarkerSet rdMarkerSet*/, Set markerCriteria, String markerPickerVar) throws IOException {

		Map<MarkerKey, Integer> wrMarkers;
		switch (markerPickCase) {
			case MARKERS_INCLUDE_BY_NETCDF_CRITERIA: {
				// Pick by netCDF field value and criteria
				NetCdfVariableMarkerValuePicker variableMarkerValuePicker
						= new NetCdfVariableMarkerValuePicker(markerCriteria, markerPickerVar, true);
				wrMarkers = variableMarkerValuePicker.pick(dataSetSource);
			} break;
			case MARKERS_EXCLUDE_BY_NETCDF_CRITERIA: {
				// Exclude by netCDF field value and criteria
				NetCdfVariableMarkerValuePicker variableMarkerValuePicker
						= new NetCdfVariableMarkerValuePicker(markerCriteria, markerPickerVar, false);
				wrMarkers = variableMarkerValuePicker.pick(dataSetSource);
			} break;
			case MARKERS_INCLUDE_BY_ID:
				wrMarkers = pickValidMarkerSetItemsByKey(dataSetSource.getMarkersKeysSource(), (Set<MarkerKey>) markerCriteria, true);
				break;
			case MARKERS_EXCLUDE_BY_ID:
				wrMarkers = pickValidMarkerSetItemsByKey(dataSetSource.getMarkersKeysSource(), (Set<MarkerKey>) markerCriteria, false);
				break;
			case ALL_MARKERS:
			default:
				// Get all markers
				MarkersKeysSource wrMarkerKeys = dataSetSource.getMarkersKeysSource();
				wrMarkers = new LinkedHashMap<MarkerKey, Integer>(wrMarkerKeys.size());
				int markerIndex = 0;
				for (MarkerKey markerKey : wrMarkerKeys) {
					wrMarkers.put(markerKey, markerIndex);
					markerIndex++;
				}
		}

		return wrMarkers;
	}

	/**
	 * @return key & index in the original set for all picked markers.
	 */
	private static Map<SampleKey, Integer> pickSamples(SetSamplePickCase samplePickCase, DataSetSource dataSetSource/*, SampleSet rdSampleSet*/, Set sampleCriteria, String samplePickerVar, StudyKey studyKey, int sampleFilterPos) throws IOException {

//		Map<SampleKey, ?> rdSampleSetMap = rdSampleSet.getSampleIdSetMapCharArray();

		Map<SampleKey, Integer> pickedSamples;
		switch (samplePickCase) {
			case ALL_SAMPLES:
				// Get all samples
				pickedSamples = new LinkedHashMap<SampleKey, Integer>(dataSetSource.getSamplesKeysSource().size());
				int i = 0;
				for (SampleKey key : dataSetSource.getSamplesKeysSource()) {
					pickedSamples.put(key, i);
					i++;
				}
				break;
//			case SAMPLES_INCLUDE_BY_NETCDF_FILTER:
//				// USE cNetCDF Filter Data and criteria
//				rdSampleSet.fillSampleIdSetMapWithFilterVariable((Map<SampleKey, char[]>) rdSampleSetMap, samplePickerVar, sampleFilterPos);
//				pickedSamples = pickValidSampleSetItemsByNetCDFValue((Map<SampleKey, char[]>) rdSampleSetMap, sampleCriteria, true);
//				break;
//			case SAMPLES_EXCLUDE_BY_NETCDF_FILTER:
//				// USE cNetCDF Filter Data and criteria
//				rdSampleSet.fillSampleIdSetMapWithFilterVariable((Map<SampleKey, char[]>) rdSampleSetMap, samplePickerVar, sampleFilterPos);
//				pickedSamples = pickValidSampleSetItemsByNetCDFValue((Map<SampleKey, char[]>) rdSampleSetMap, sampleCriteria, false);
//				break;
//			case SAMPLES_INCLUDE_BY_NETCDF_CRITERIA:
//				// USE cNetCDF Value and criteria
//				rdSampleSet.fillSampleIdSetMapWithVariable(rdSampleSetMap, samplePickerVar);
//				pickedSamples = pickValidSampleSetItemsByNetCDFValue(rdSampleSetMap, sampleCriteria, true);
//				break;
//			case SAMPLES_EXCLUDE_BY_NETCDF_CRITERIA:
//				// USE cNetCDF Value and criteria
//				rdSampleSet.fillSampleIdSetMapWithVariable(rdSampleSetMap, samplePickerVar);
//				pickedSamples = pickValidSampleSetItemsByNetCDFValue(rdSampleSetMap, sampleCriteria, false);
//				break;
//			case SAMPLES_INCLUDE_BY_ID:
//				pickedSamples = pick(rdSampleSetMap.keySet(), (Set<SampleKey>) sampleCriteria, true);
//				break;
//			case SAMPLES_EXCLUDE_BY_ID:
//				pickedSamples = pick(rdSampleSetMap.keySet(), (Set<SampleKey>) sampleCriteria, false);
//				break;
			case SAMPLES_INCLUDE_BY_DB_FIELD:
				// USE DB DATA
				pickedSamples= SampleInfoList.pickSamples(studyKey, samplePickerVar, sampleCriteria, true);
//				pickedSamples = pickValidSampleSetItemsByDBField(studyKey, rdSampleSetMap.keySet(), samplePickerVar, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_DB_FIELD:
				// USE DB DATA
				pickedSamples= SampleInfoList.pickSamples(studyKey, samplePickerVar, sampleCriteria, false);
//				pickedSamples = pickValidSampleSetItemsByDBField(studyKey, rdSampleSetMap.keySet(), samplePickerVar, sampleCriteria, false);
				break;
			default:
				throw new IOException("Invalid sample pick case: " + samplePickCase.toString());
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

		int resultMatrixId = MatrixKey.NULL_ID;

		// MARKERSET PICKING
//		MarkerSet rdMarkerSet = new MarkerSet(this.rdMatrixKey);
//		rdMarkerSet.initFullMarkerIdSetMap();
		// Contains key & index in the original set for all to be extracted markers.
		Map<MarkerKey, Integer> wrMarkers = pickMarkers(markerPickCase, dataSetSource, fullMarkerCriteria, markerPickerVar);
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
//		SampleSet rdSampleSet = new SampleSet(this.rdMatrixKey);
		// Contains key & index in the original set for all to be extracted samples.
		Map<SampleKey, Integer> wrSampleSetMap = pickSamples(samplePickCase, dataSetSource, fullSampleCriteria, samplePickerVar, rdMatrixKey.getStudyKey(), sampleFilterPos);
		if (wrSampleSetMap.isEmpty()) {
			// XXX maybe we should instead throw an IOException?
			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
			return resultMatrixId;
		}

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

		return resultMatrixId;
	}

	private static <V> Map<SampleKey, Integer> pickValidSampleSetItemsByNetCDFValue(Map<SampleKey, V> map, Set<V> criteria, boolean include) throws IOException {

		Map<SampleKey, Integer> returnMap = new LinkedHashMap<SampleKey, Integer>();

		int pickCounter = 0;
		if (include) {
			for (Map.Entry<SampleKey, V> entry : map.entrySet()) {
				if (criteria.contains(entry.getValue())) { // FIXME bad comparison, in case of arrays, for example char[] (should check individual entries)
					returnMap.put(entry.getKey(), pickCounter);
				}
				pickCounter++;
			}
		} else {
			for (Map.Entry<SampleKey, V> entry : map.entrySet()) {
				if (!criteria.contains(entry.getValue())) { // FIXME bad comparison, in case of arrays, for example char[] (should check individual entries)
					returnMap.put(entry.getKey(), pickCounter);
				}
				pickCounter++;
			}
		}

		return returnMap;
	}

//	public static Map<SampleKey, Integer> pickValidSampleSetItemsByDBField(StudyKey studyKey, Set<SampleKey> sampleKeys, String dbField, Set<?> criteria, boolean include) throws IOException {
//
//		Map<SampleKey, Integer> returnMap = new LinkedHashMap<SampleKey, Integer>();
//		List<SampleInfo> sampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(studyKey);
//
//		int pickCounter = 0;
//		if (include) {
//			for (SampleKey key : sampleKeys) {
//				// loop through rows of result set
//				for (SampleInfo sampleInfo : sampleInfos) {
//					if (sampleInfo.getKey().equals(key)
//							&& criteria.contains(sampleInfo.getField(dbField).toString()))
//					{
//						returnMap.put(key, pickCounter);
//					}
//				}
//				pickCounter++;
//			}
//		} else {
//			for (SampleKey key : sampleKeys) {
//				// loop through rows of result set
//				for (SampleInfo sampleInfo : sampleInfos) {
//					if (sampleInfo.getKey().equals(key)
//							&& !criteria.contains(sampleInfo.getField(dbField).toString()))
//					{
//						returnMap.put(key, pickCounter);
//					}
//				}
//				pickCounter++;
//			}
//		}
//
//		return returnMap;
//	}

	/**
	 * THESE Maps DO NOT CONTAIN SAME ITEMS AS INIT Map.
	 * RETURN Map OK
	 */
	private static <V> Map<MarkerKey, Integer> pickValidMarkerSetItemsByValue(Map<MarkerKey, V> markerKeyValues, Set<V> criteria, boolean includes) {

		Map<MarkerKey, Integer> returnMap = new LinkedHashMap<MarkerKey, Integer>();

		int markerIndex = 0;
		if (includes) {
			for (Map.Entry<MarkerKey, V> entry : markerKeyValues.entrySet()) {
				MarkerKey key = entry.getKey();
				V value = entry.getValue();
				if (criteria.contains(value)) {
					returnMap.put(key, markerIndex);
				}
				markerIndex++;
			}
		} else {
			for (Map.Entry<MarkerKey, V> entry : markerKeyValues.entrySet()) {
				MarkerKey key = entry.getKey();
				V value = entry.getValue();
				if (!criteria.contains(value)) {
					returnMap.put(key, markerIndex);
				}
				markerIndex++;
			}
		}

		return returnMap;
	}

	private static <V> Map<MarkerKey, Integer> pickValidMarkerSetItemsByKey(Collection<MarkerKey> markerKeys, Set<MarkerKey> criteria, boolean includes) {

		Map<MarkerKey, Integer> returnMap = new LinkedHashMap<MarkerKey, Integer>();

		int markerIndex = 0;
		if (includes) {
			for (MarkerKey key : markerKeys) {
				if (criteria.contains(key)) {
					returnMap.put(key, markerIndex);
				}
				markerIndex++;
			}
		} else {
			for (MarkerKey key : markerKeys) {
				if (!criteria.contains(key)) {
					returnMap.put(key, markerIndex);
				}
				markerIndex++;
			}
		}

		return returnMap;
	}


	/**
	 * @param include whether this is an include or an exclude picker.
	 */
	public static <K> Map<K, Integer> pick(Collection<K> input, Collection<K> criteria, boolean include) {

		Map<K, Integer> result = new LinkedHashMap<K, Integer>();

		int originalIndex = 0;
		if (include) {
			for (K key : input) {
				if (criteria.contains(key)) {
					result.put(key, originalIndex);
				}
				originalIndex++;
			}
		} else {
			for (K key : input) {
				if (!criteria.contains(key)) {
					result.put(key, originalIndex);
				}
				originalIndex++;
			}
		}

		return result;
	}

	/**
	 * @param include whether this is an include or an exclude picker.
	 */
	public <K, V> Map<K, Integer> pick(Map<K, V> input, Collection<V> criteria, boolean include) {

		Map<K, Integer> result = new LinkedHashMap<K, Integer>();

		int originalIndex = 0;
		if (include) {
			for (Map.Entry<K, V> entry : input.entrySet()) {
				if (criteria.contains(entry.getValue())) {
					result.put(entry.getKey(), originalIndex);
				}
				originalIndex++;
			}
		} else {
			for (Map.Entry<K, V> entry : input.entrySet()) {
				if (!criteria.contains(entry.getValue())) {
					result.put(entry.getKey(), originalIndex);
				}
				originalIndex++;
			}
		}

		return result;
	}
}

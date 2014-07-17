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

package org.gwaspi.operations;

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
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.ProcessInfo;

/**
 * Extracts Genotypes to a new matrix.
 */
public class MatrixDataExtractor extends AbstractMatrixCreatingOperation {

	private static final ProcessInfo PROCESS_INFO = new DefaultProcessInfo(
			Text.Trafo.extractData,
			Text.Trafo.extractToNewMatrix);

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					true, // XXX We might want to change this in the future, as this could be converted to create only an operation, instead of a full new matrix
					Text.Trafo.extractData,
					Text.Trafo.extractToNewMatrix,
					null);
	static {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new MatrixOperationFactory(
				MatrixDataExtractor.class, OPERATION_TYPE_INFO));
	}

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

	private static interface Picker<K> {

		/**
		 * @return original indices of picked keys
		 */
		List<Integer> pick(final DataSetDestination dataSetDestination, DataSetSource dataSetSource) throws IOException;
	}

//	private abstract static class AbstractKeyPicker<K> implements Picker<K> {
//
//		private final Collection<K> criteria;
//		private final boolean include;
//
//		/**
//		 * @param include whether this is an include or an exclude picker.
//		 */
//		AbstractKeyPicker(Collection<K> criteria, boolean include) {
//
//			this.criteria = criteria;
//			this.include = include;
//		}
//
//		abstract Map<Integer, K> getInputKeys(DataSetSource dataSetSource) throws IOException;
//
//		@Override
//		public List<Integer> pick(final DataSetDestination dataSetDestination, DataSetSource dataSetSource) throws IOException {
//
//			List<Integer> pickedOrigIndices = new LinkedList<Integer>();
//
//			int originalIndex = 0;
//			if (include) {
//				for (K inputKey : getInputKeys(dataSetSource)) {
//					if (criteria.contains(inputKey)) {
//						pickedOrigIndices.add(originalIndex);
//						dataSetDestination.addMarkerKey(inputKey);
//					}
//					originalIndex++;
//				}
//			} else {
//				for (K inputKey : getInputKeys(dataSetSource)) {
//					if (!criteria.contains(inputKey)) {
//						pickedOrigIndices.add(originalIndex);
//						dataSetDestination.addMarkerKey(inputKey);
//					}
//					originalIndex++;
//				}
//			}
//
//			return pickedOrigIndices;
//		}
//	}

//	private static class SampleKeyPicker extends AbstractKeyPicker<SampleKey> {
//
//		SampleKeyPicker(Collection<SampleKey> criteria, boolean include) {
//			super(criteria, include);
//		}
//
//		@Override
//		public Map<Integer, SampleKey> getInputKeys(DataSetSource dataSetSource) throws IOException {
//			return dataSetSource.getSamplesKeysSource().getIndicesMap();
//		}
//	}
//
//	private static class MarkerKeyPicker extends AbstractKeyPicker<MarkerKey> {
//
//		MarkerKeyPicker(Collection<MarkerKey> criteria, boolean include) {
//			super(criteria, include);
//		}
//
//		@Override
//		public Map<Integer, MarkerKey> getInputKeys(DataSetSource dataSetSource) throws IOException {
//			return dataSetSource.getMarkersKeysSource().getIndicesMap();
//		}
//	}

	private abstract static class AbstractValuePicker<K, V, M> implements Picker<K> {

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

		abstract Map<Integer, MarkerKey> getInputKeys(DataSetSource dataSetSource) throws IOException;
		abstract List<V> getInputValues(DataSetSource dataSetSource) throws IOException;

		@Override
		public List<Integer> pick(final DataSetDestination dataSetDestination, DataSetSource dataSetSource) throws IOException {

//			Map<K, Integer> result = new LinkedHashMap<K, Integer>();
			List<Integer> pickedOrigIndices = new LinkedList<Integer>();

//			int originalIndex = 0;
			Iterator<Map.Entry<Integer, MarkerKey>> keysIt = getInputKeys(dataSetSource).entrySet().iterator();
			if (include) {
				for (V value : getInputValues(dataSetSource)) {
					final Map.Entry<Integer, MarkerKey> keyEntry = keysIt.next();
					if (criteria.contains(typeConverter.extract(value))) {
						pickedOrigIndices.add(keyEntry.getKey());
						dataSetDestination.addMarkerKey(keyEntry.getValue());
					}
				}
			} else {
				for (V value : getInputValues(dataSetSource)) {
					final Map.Entry<Integer, MarkerKey> keyEntry = keysIt.next();
					if (!criteria.contains(typeConverter.extract(value))) {
						pickedOrigIndices.add(keyEntry.getKey());
						dataSetDestination.addMarkerKey(keyEntry.getValue());
					}
				}
			}

			return pickedOrigIndices;
		}
	}

	private abstract static class AbstractMarkerValuePicker<M> extends AbstractValuePicker<MarkerKey, MarkerMetadata, M> {

		AbstractMarkerValuePicker(Collection<M> criteria, Extractor<MarkerMetadata, M> typeConverter, boolean include) {
			super(criteria, typeConverter, include);
		}

		@Override
		public Map<Integer, MarkerKey> getInputKeys(DataSetSource dataSetSource) throws IOException {
			return dataSetSource.getMarkersKeysSource().getIndicesMap();
		}

		@Override
		public List<MarkerMetadata> getInputValues(DataSetSource dataSetSource) throws IOException {
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

//	private abstract static class AbstractSampleValuePicker<M> extends AbstractValuePicker<SampleKey, SampleInfo, M> {
//
//		AbstractSampleValuePicker(Collection<M> criteria, Extractor<SampleInfo, M> typeConverter, boolean include) {
//			super(criteria, typeConverter, include);
//		}
//
//		@Override
//		public List<SampleKey> getInputKeys(DataSetSource dataSetSource) throws IOException {
//			return dataSetSource.getSamplesKeysSource();
//		}
//
//		@Override
//		public List<SampleInfo> getInputValues(DataSetSource dataSetSource) throws IOException {
//			return dataSetSource.getSamplesInfosSource();
//		}
//	}

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
	 * This constructor allows to extract data from a Matrix,
	 * by passing a variable, and the criteria to filter items by.
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
		super(dataSetDestination);

		this.dataSetSource = dataSetSource;

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
	public OperationTypeInfo getTypeInfo() {
		return OPERATION_TYPE_INFO;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return PROCESS_INFO;
	}

	private static List<?> parseMarkerPickerFile(File markerPickerFile, SetMarkerPickCase markerPickCase) throws IOException {

		List<?> markerCriteria = new LinkedList();

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

	private static List<?> parseSamplePickerFile(File samplePickerFile, SetSamplePickCase samplePickCase, String samplePickerVar, StudyKey studyKey) throws IOException {

		List<?> sampleCriteria = new LinkedList();

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
	 * @return marker indices in the original set for all picked markers.
	 */
	private static List<Integer> pickMarkers(final DataSetDestination dataSetDestination, SetMarkerPickCase markerPickCase, DataSetSource dataSetSource/*, MarkerSet rdMarkerSet*/, Set markerCriteria, String markerPickerVar) throws IOException {

		final List<Integer> pickedMarkersOrigIndices;
		dataSetDestination.startLoadingMarkerMetadatas(true);
		switch (markerPickCase) {
			case MARKERS_INCLUDE_BY_NETCDF_CRITERIA: {
				// Pick by netCDF field value and criteria
				NetCdfVariableMarkerValuePicker variableMarkerValuePicker
						= new NetCdfVariableMarkerValuePicker(markerCriteria, markerPickerVar, true);
				pickedMarkersOrigIndices = variableMarkerValuePicker.pick(dataSetDestination, dataSetSource);
			} break;
			case MARKERS_EXCLUDE_BY_NETCDF_CRITERIA: {
				// Exclude by netCDF field value and criteria
				NetCdfVariableMarkerValuePicker variableMarkerValuePicker
						= new NetCdfVariableMarkerValuePicker(markerCriteria, markerPickerVar, false);
				pickedMarkersOrigIndices = variableMarkerValuePicker.pick(dataSetDestination, dataSetSource);
			} break;
			case MARKERS_INCLUDE_BY_ID:
				pickedMarkersOrigIndices = pickValidMarkerSetItemsByKey(dataSetDestination, dataSetSource.getMarkersKeysSource().getIndicesMap(), (Set<MarkerKey>) markerCriteria, true);
				break;
			case MARKERS_EXCLUDE_BY_ID:
				pickedMarkersOrigIndices = pickValidMarkerSetItemsByKey(dataSetDestination, dataSetSource.getMarkersKeysSource().getIndicesMap(), (Set<MarkerKey>) markerCriteria, false);
				break;
			case ALL_MARKERS:
			default:
				// Get all markers
				pickedMarkersOrigIndices = new ArrayList<Integer>(dataSetSource.getNumMarkers());
				for (Map.Entry<Integer, MarkerKey> origIndexAndKey
						: dataSetSource.getMarkersKeysSource().getIndicesMap().entrySet())
				{
					dataSetDestination.addMarkerKey(origIndexAndKey.getValue());
					pickedMarkersOrigIndices.add(origIndexAndKey.getKey());
				}
		}
		dataSetDestination.finishedLoadingMarkerMetadatas();

		return pickedMarkersOrigIndices;
	}

	/**
	 * @return sample indices in the original set for all picked markers.
	 */
	private static List<Integer> pickSamples(final DataSetDestination dataSetDestination, SetSamplePickCase samplePickCase, DataSetSource dataSetSource/*, SampleSet rdSampleSet*/, Set sampleCriteria, String samplePickerVar, StudyKey studyKey, int sampleFilterPos) throws IOException {

//		Map<SampleKey, ?> rdSampleSetMap = rdSampleSet.getSampleIdSetMapCharArray();

		Map<SampleKey, Integer> pickedSamples;
		boolean addedToDestination = false;
		switch (samplePickCase) {
			case ALL_SAMPLES:
				// Get all samples
				pickedSamples = new LinkedHashMap<SampleKey, Integer>(dataSetSource.getSamplesKeysSource().size());
				dataSetDestination.startLoadingSampleInfos(true);
				for (Map.Entry<Integer, SampleKey> origIndexAndKey : dataSetSource.getSamplesKeysSource().getIndicesMap().entrySet()) {
					pickedSamples.put(origIndexAndKey.getValue(), origIndexAndKey.getKey());
					dataSetDestination.addSampleKey(origIndexAndKey.getValue());
				}
				addedToDestination = true;
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
				pickedSamples = SampleInfoList.pickSamples(studyKey, samplePickerVar, sampleCriteria, true);
//				pickedSamples = pickValidSampleSetItemsByDBField(studyKey, rdSampleSetMap.keySet(), samplePickerVar, sampleCriteria, true);
				break;
			case SAMPLES_EXCLUDE_BY_DB_FIELD:
				// USE DB DATA
				pickedSamples = SampleInfoList.pickSamples(studyKey, samplePickerVar, sampleCriteria, false);
//				pickedSamples = pickValidSampleSetItemsByDBField(studyKey, rdSampleSetMap.keySet(), samplePickerVar, sampleCriteria, false);
				break;
			default:
				throw new IOException("Invalid sample pick case: " + samplePickCase.toString());
		}
		if (!addedToDestination) {
			dataSetDestination.startLoadingSampleInfos(true);
			for (SampleKey key : pickedSamples.keySet()) {
				dataSetDestination.addSampleKey(key);
			}
		}
		dataSetDestination.finishedLoadingSampleInfos();

		return new ArrayList<Integer>(pickedSamples.values());
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
	public OperationParams getParams() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int processMatrix() throws IOException {

		int resultMatrixId = MatrixKey.NULL_ID;

		final DataSetDestination dataSetDestination = getDataSetDestination();

		dataSetDestination.init();

		final List<Integer> pickedSamplesOrigIndices = pickSamples(
				dataSetDestination,
				samplePickCase,
				dataSetSource,
				fullSampleCriteria,
				samplePickerVar,
				rdMatrixKey.getStudyKey(),
				sampleFilterPos);
		if (pickedSamplesOrigIndices.isEmpty()) {
			// XXX maybe we should instead throw an IOException?
			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
			return resultMatrixId;
		}

		final List<Integer> pickedMarkersOrigIndices = pickMarkers(
				dataSetDestination,
				markerPickCase,
				dataSetSource,
				fullMarkerCriteria,
				markerPickerVar);
		if (pickedMarkersOrigIndices.isEmpty()) {
			// XXX maybe we should instead throw an IOException?
			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
			return resultMatrixId;
		}


		// MARKERSET PICKING
//		MarkerSet rdMarkerSet = new MarkerSet(this.rdMatrixKey);
//		rdMarkerSet.initFullMarkerIdSetMap();
		// Contains key & index in the original set for all to be extracted markers.
//		Map<MarkerKey, Integer> wrMarkers = pickMarkers(dataSetDestination, markerPickCase, dataSetSource, fullMarkerCriteria, markerPickerVar);
//		if (wrMarkers.isEmpty()) {
//			// XXX maybe we should instead throw an IOException?
//			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
//			return resultMatrixId;
//		}

//		// RETRIEVE CHROMOSOMES INFO
//		Map<ChromosomeKey, ChromosomeInfo> rdChromosomeInfo;
//		this.rdMarkerSet.fillMarkerSetMapWithChrAndPos();
//		Map<MarkerKey, MarkerMetadata> sortedChrAndPos = org.gwaspi.global.Utils.createOrderedMap(wrMarkers.keySet(), this.rdMarkerSet.getMarkerMetadata());
//		this.rdChromosomeInfo = ChromosomeUtils.aggregateChromosomeInfo(sortedChrAndPos, 0, 1);

		// SAMPLESET PICKING
//		SampleSet rdSampleSet = new SampleSet(this.rdMatrixKey);
//		// Contains key & index in the original set for all to be extracted samples.
//		Map<SampleKey, Integer> wrSampleSetMap = pickSamples(dataSetDestination, samplePickCase, dataSetSource, fullSampleCriteria, samplePickerVar, rdMatrixKey.getStudyKey(), sampleFilterPos);
//		if (wrSampleSetMap.isEmpty()) {
//			// XXX maybe we should instead throw an IOException?
//			Dialogs.showWarningDialogue(Text.Trafo.criteriaReturnsNoResults);
//			return resultMatrixId;
//		}

//		// use only the selected sample infos
//		dataSetDestination.startLoadingSampleInfos(true);
//		for (SampleKey sampleKey : wrSampleSetMap.keySet()) {
//			dataSetDestination.addSampleKey(sampleKey); XXX; // do this in the pickSamples() method
//		}
//		dataSetDestination.finishedLoadingSampleInfos();

//		// use only the selected markers metadata
//		dataSetDestination.startLoadingMarkerMetadatas(true); // FIXME This is not yet supported. we may have to read the whole marker metadatas from dataSetSource, and wrtie them to dataSetDestination
//		for (MarkerKey markerKey : wrMarkers.keySet()) {
//			dataSetDestination.addMarkerKey(markerKey); XXX; // do this in the pickMarkers() method
//		}
//		dataSetDestination.finishedLoadingMarkerMetadatas();

		// chromosomes infos
		// NOTE just let the auto-extraction of chromosomes from the markers metadatas kick in

		// GENOTYPES WRITER
		// Iterate through wrSampleSetMap, use item position to read correct sample GTs into the dataSetDestination.

		SamplesGenotypesSource samplesGenotypesSource = dataSetSource.getSamplesGenotypesSource();
		int sampleWrIndex = 0;
		for (int rdSampleIndex : pickedSamplesOrigIndices) {
			GenotypesList sampleGenotypes = samplesGenotypesSource.get(rdSampleIndex);
			List<byte[]> wrSampleGenotypes = new ArrayList<byte[]>(pickedMarkersOrigIndices.size());
			for (int rdMarkerIndex : pickedMarkersOrigIndices) {
				wrSampleGenotypes.add(sampleGenotypes.get(rdMarkerIndex));
			}

			dataSetDestination.addSampleGTAlleles(sampleWrIndex, wrSampleGenotypes);
			sampleWrIndex++;
		}

		dataSetDestination.done();

		org.gwaspi.global.Utils.sysoutCompleted("Extraction to new Matrix");

		return resultMatrixId;
	}

	private static <V> List<Integer> pickValidMarkerSetItemsByKey(DataSetDestination dataSetDestination, Map<Integer, MarkerKey> markerKeys, Set<MarkerKey> criteria, boolean includes) throws IOException {

		final List<Integer> pickedOrigIndices = new LinkedList<Integer>();

//		int markerIndex = 0;
		if (includes) {
			for (Map.Entry<Integer, MarkerKey> keyEntry : markerKeys.entrySet()) {
				if (criteria.contains(keyEntry.getValue())) {
					pickedOrigIndices.add(keyEntry.getKey());
					dataSetDestination.addMarkerKey(keyEntry.getValue());
				}
			}
		} else {
			for (Map.Entry<Integer, MarkerKey> keyEntry : markerKeys.entrySet()) {
				if (!criteria.contains(keyEntry.getValue())) {
					pickedOrigIndices.add(keyEntry.getKey());
					dataSetDestination.addMarkerKey(keyEntry.getValue());
				}
			}
		}

		return pickedOrigIndices;
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

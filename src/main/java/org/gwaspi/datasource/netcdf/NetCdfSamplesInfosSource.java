/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.datasource.netcdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Extractor;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import ucar.nc2.NetcdfFile;

public class NetCdfSamplesInfosSource extends AbstractNetCdfListSource<SampleInfo> implements SamplesInfosSource {

	private static final int DEFAULT_CHUNK_SIZE = 50;
	private static final int DEFAULT_CHUNK_SIZE_SHATTERED = 1;

	private final DataSetSource dataSetSource;
	private final StudyKey studyKey;
	private final MatrixKey origin;
	private DataSetSource originDataSetSource;
	private SamplesInfosSource originSource;

	private NetCdfSamplesInfosSource(
			final DataSetSource dataSetSource,
			final StudyKey studyKey,
			final MatrixKey origin,
			final NetcdfFile rdNetCdfFile)
	{
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_SAMPLESET);

		this.dataSetSource = dataSetSource;
		this.studyKey = studyKey;
		this.origin = origin;
		this.originSource = null;
	}

	private NetCdfSamplesInfosSource(
			final DataSetSource dataSetSource,
			final StudyKey studyKey,
			final MatrixKey origin,
			final NetcdfFile rdNetCdfFile,
			final List<Integer> originalIndices)
	{
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE_SHATTERED, cNetCDF.Dimensions.DIM_SAMPLESET, originalIndices);

		this.dataSetSource = dataSetSource;
		this.studyKey = studyKey;
		this.origin = origin;
		this.originSource = null;
	}

	private DataSetSource getDataSetSource() {
		return dataSetSource;
	}

	private DataSetSource getOrigDataSetSource() throws IOException {

		if (originDataSetSource == null) {
			originDataSetSource = MatrixFactory.generateMatrixDataSetSource(origin);
		}

		return originDataSetSource;
	}

	private SamplesInfosSource getOrigSource() throws IOException {

		if (originSource == null) {
			originSource = getOrigDataSetSource().getSamplesInfosSource();
		}

		return originSource;
	}

	public static SamplesInfosSource createForMatrix(DataSetSource dataSetSource, StudyKey studyKey, NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfSamplesInfosSource(dataSetSource, studyKey, null, rdNetCdfFile);
	}

	public static SamplesInfosSource createForOperation(DataSetSource dataSetSource, StudyKey studyKey, MatrixKey origin, NetcdfFile rdNetCdfFile, List<Integer> originalIndices) throws IOException {
		return new NetCdfSamplesInfosSource(dataSetSource, studyKey, origin, rdNetCdfFile, originalIndices);
	}

	@Override
	public SamplesKeysSource getKeysSource() throws IOException {
		return getDataSetSource().getSamplesKeysSource();
	}

	@Override
	public List<SampleInfo> getRange(int from, int to) throws IOException {

		List<SampleInfo> values = new ArrayList<SampleInfo>(to - from);

		List<SampleKey> sampleKeys = getKeysSource().subList(from, to);
		Iterator<Integer> orderIdsIt = getOrderIds(from, to).iterator();
		Iterator<String> fathersIt = getFathers(from, to).iterator();
		Iterator<String> mothersIt = getMothers(from, to).iterator();
		Iterator<Sex> sexesIt = getSexes(from, to).iterator();
		Iterator<Affection> affectionsIt = getAffections(from, to).iterator();
		Iterator<String> categoriesIt = getCategories(from, to).iterator();
		Iterator<String> diseasesIt = getDiseases(from, to).iterator();
		Iterator<String> populationsIt = getPopulations(from, to).iterator();
		Iterator<Integer> agesIt = getAges(from, to).iterator();
		Iterator<String> filtersIt = getFilters(from, to).iterator();
		Iterator<Integer> approvedsIt = getApproveds(from, to).iterator();
		Iterator<Integer> statusesIt = getStatuses(from, to).iterator();
		for (SampleKey sampleKey : sampleKeys) {
			values.add(new SampleInfo(
					studyKey,
					sampleKey.getSampleId(),
					sampleKey.getFamilyId(),
					orderIdsIt.next(),
					fathersIt.next(),
					mothersIt.next(),
					sexesIt.next(),
					affectionsIt.next(),
					categoriesIt.next(),
					diseasesIt.next(),
					populationsIt.next(),
					agesIt.next(),
					filtersIt.next(),
					approvedsIt.next(),
					statusesIt.next()
			));
		}

		return values;
	}

//	@Override
//	public List<Integer> getSampleOrigIndices() throws IOException {
//		return getSampleOrigIndices(-1, -1);
//	}

//	@Override
//	public List<SampleKey> getSampleKeys() throws IOException {
//		return getSampleKeys(-1, -1);
//	}

	@Override
	public List<Integer> getOrderIds() throws IOException {
		return getOrderIds(-1, -1);
	}

	@Override
	public List<String> getFathers() throws IOException {
		return getFathers(-1, -1);
	}

	@Override
	public List<String> getMothers() throws IOException {
		return getMothers(-1, -1);
	}

	@Override
	public List<SampleInfo.Sex> getSexes() throws IOException {
		return getSexes(-1, -1);
	}

	@Override
	public List<SampleInfo.Affection> getAffections() throws IOException {
		return getAffections(-1, -1);
	}

	@Override
	public List<String> getCategories() throws IOException {
		return getCategories(-1, -1);
	}

	@Override
	public List<String> getDiseases() throws IOException {
		return getDiseases(-1, -1);
	}

	@Override
	public List<String> getPopulations() throws IOException {
		return getPopulations(-1, -1);
	}

	@Override
	public List<Integer> getAges() throws IOException {
		return getAges(-1, -1);
	}

	@Override
	public List<String> getFilters() throws IOException {
		return getFilters(-1, -1);
	}

	@Override
	public List<Integer> getApproveds() throws IOException {
		return getApproveds(-1, -1);
	}

	@Override
	public List<Integer> getStatuses() throws IOException {
		return getStatuses(-1, -1);
	}

//	@Override
//	public List<Integer> getSampleOrigIndices(int from, int to) throws IOException {
//
//		if (from == -1) {
//			from = 0;
//		}
//		if (to == -1) {
//			to = size() - 1;
//		}
//		return new NoStorageSuccessiveIndicesList(to - from + 1, from);
//	}

//	@Override
//	public List<SampleKey> getSampleKeys(int from, int to) throws IOException {
//		return readVar(cNetCDF.Variables.VAR_SAMPLE_KEY, from, to);
//	}

	public List<Integer> getOrderIds(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLE_ORDER_ID, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<Integer> allOriginOrderIds = origSource.getOrderIds();
			final List<Integer> localOrderIds = extractValuesByOrigIndices(allOriginIndices, allOriginOrderIds, toExtractSampleOrigIndices);
			return localOrderIds;
		}
	}

	public List<String> getFathers(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLE_FATHER, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<String> allOriginFathers = origSource.getFathers();
			final List<String> localFathers = extractValuesByOrigIndices(allOriginIndices, allOriginFathers, toExtractSampleOrigIndices);
			return localFathers;
		}
	}

	public List<String> getMothers(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLE_MOTHER, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<String> allOriginMothers = origSource.getMothers();
			final List<String> localMothers = extractValuesByOrigIndices(allOriginIndices, allOriginMothers, toExtractSampleOrigIndices);
			return localMothers;
		}
	}

	public List<Sex> getSexes(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLES_SEX, new Extractor.IntToEnumExtractor(Sex.values()), from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<Sex> allOriginSexes = origSource.getSexes();
			final List<Sex> localSexes = extractValuesByOrigIndices(allOriginIndices, allOriginSexes, toExtractSampleOrigIndices);
			return localSexes;
		}
	}

	public List<Affection> getAffections(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLES_AFFECTION, new Extractor.IntToEnumExtractor(Affection.values()), from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<Affection> allOriginAffections = origSource.getAffections();
			final List<Affection> localAffections = extractValuesByOrigIndices(allOriginIndices, allOriginAffections, toExtractSampleOrigIndices);
			return localAffections;
		}
	}

	public List<String> getCategories(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLE_CATEGORY, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<String> allOriginCategories = origSource.getCategories();
			final List<String> localCategories = extractValuesByOrigIndices(allOriginIndices, allOriginCategories, toExtractSampleOrigIndices);
			return localCategories;
		}
	}

	public List<String> getDiseases(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLE_DISEASE, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<String> allOriginDiseases = origSource.getDiseases();
			final List<String> localDiseases = extractValuesByOrigIndices(allOriginIndices, allOriginDiseases, toExtractSampleOrigIndices);
			return localDiseases;
		}
	}

	public List<String> getPopulations(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLE_POPULATION, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<String> allOriginPopulations = origSource.getPopulations();
			final List<String> localPopulations = extractValuesByOrigIndices(allOriginIndices, allOriginPopulations, toExtractSampleOrigIndices);
			return localPopulations;
		}
	}

	public List<Integer> getAges(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLE_AGE, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<Integer> allOriginAges = origSource.getAges();
			final List<Integer> localAges = extractValuesByOrigIndices(allOriginIndices, allOriginAges, toExtractSampleOrigIndices);
			return localAges;
		}
	}

	public List<String> getFilters(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLE_FILTER, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<String> allOriginFilters = origSource.getFilters();
			final List<String> localFilters = extractValuesByOrigIndices(allOriginIndices, allOriginFilters, toExtractSampleOrigIndices);
			return localFilters;
		}
	}

	public List<Integer> getApproveds(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLE_APPROVED, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<Integer> allOriginApproveds = origSource.getApproveds();
			final List<Integer> localApproveds = extractValuesByOrigIndices(allOriginIndices, allOriginApproveds, toExtractSampleOrigIndices);
			return localApproveds;
		}
	}

	public List<Integer> getStatuses(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all sample info attributes
			return readVar(cNetCDF.Variables.VAR_SAMPLE_STATUS, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final SamplesInfosSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getSamplesKeysSource().getIndices();
			final List<Integer> allOriginStatuses = origSource.getStatuses();
			final List<Integer> localStatuses = extractValuesByOrigIndices(allOriginIndices, allOriginStatuses, toExtractSampleOrigIndices);
			return localStatuses;
		}
	}
}

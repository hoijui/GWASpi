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
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.markers.NoStorageSuccessiveIndicesList;
import ucar.nc2.NetcdfFile;

public class NetCdfSamplesInfosSource extends AbstractNetCdfListSource<SampleInfo> implements SamplesInfosSource {

	private static final int DEFAULT_CHUNK_SIZE = 50;
	private static final int DEFAULT_CHUNK_SIZE_SHATTERED = 1;

	private final StudyKey studyKey;

	private NetCdfSamplesInfosSource(StudyKey studyKey, NetcdfFile rdNetCdfFile) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_SAMPLESET);

		this.studyKey = studyKey;
	}

	private NetCdfSamplesInfosSource(StudyKey studyKey, NetcdfFile rdNetCdfFile, List<Integer> originalIndices) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE_SHATTERED, originalIndices);

		this.studyKey = studyKey;
	}

	private SamplesInfosSource getOrigSource() {

		new NetCDFDataSetSource(getO)
		new NetCdfSamplesInfosSource(studyKey, null)
	}

	public static SamplesInfosSource createForMatrix(StudyKey studyKey, NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfSamplesInfosSource(studyKey, rdNetCdfFile);
	}

	public static SamplesInfosSource createForOperation(StudyKey studyKey, NetcdfFile rdNetCdfFile, List<Integer> originalIndices) throws IOException {
		return new NetCdfSamplesInfosSource(studyKey, rdNetCdfFile, originalIndices);
	}

	@Override
	public List<SampleInfo> getRange(int from, int to) throws IOException {

		List<SampleInfo> values = new ArrayList<SampleInfo>(to - from);

		List<SampleKey> sampleKeys = getSampleKeys(from, to);
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

	@Override
	public List<Integer> getSampleOrigIndices() throws IOException {
		return getSampleOrigIndices(-1, -1);
	}

	@Override
	public List<SampleKey> getSampleKeys() throws IOException {
		return getSampleKeys(-1, -1);
	}

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
//		return getAffections(-1, -1);

		// if we had direct storage of all sample info attributes
//		return readVar(cNetCDF.Variables.VAR_SAMPLES_AFFECTION, new Extractor.IntToEnumExtractor(Affection.values()), from, to);

		// ... as we do not, we extract it from the origin
		// HACK This will be very inefficient, for example if we use
		//   many small intervalls to get the whole range.
		final List<Integer> toExtractSampleOrigIndices = getSampleOrigIndices(from, to);
		final SamplesInfosSource origSource = getOrigSource();
		final List<Integer> allOriginIndices = origSource.getSampleOrigIndices();
		final List<Affection> allOriginAffections = origSource.getAffections();
		final List<Affection> localAffections = extractValuesByOrigIndices(allOriginIndices, allOriginAffections, toExtractSampleOrigIndices);
//		final Iterator<Affection> allOriginAffectionsIt = allOriginAffections.iterator();
//		final List<Affection> localAffections = new ArrayList<Affection>(toExtractSampleOrigIndices.size());
//		for (Integer originIndex : allOriginIndices) {
//			Affection originAffection = allOriginAffectionsIt.next();
//			if (toExtractSampleOrigIndices.contains(originIndex)) {
//				localAffections.add(originAffection);
//			}
//		}
		return localAffections;
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

	@Override
	public List<Integer> getSampleOrigIndices(int from, int to) throws IOException {
		return new NoStorageSuccessiveIndicesList(from, to - from);
	}

	@Override
	public List<SampleKey> getSampleKeys(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_KEY, from, to);
	}

	public List<Integer> getOrderIds(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_ORDER_ID, from, to);
	}

	public List<String> getFathers(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_FATHER, from, to);
	}

	public List<String> getMothers(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_MOTHER, from, to);
	}

	public List<Sex> getSexes(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLES_SEX, new Extractor.IntToEnumExtractor(Sex.values()), from, to);
	}

	public List<Affection> getAffections(int from, int to) throws IOException {

		// if we had direct storage of all sample info attributes
//		return readVar(cNetCDF.Variables.VAR_SAMPLES_AFFECTION, new Extractor.IntToEnumExtractor(Affection.values()), from, to);

		// ... as we do not, we extract it from the origin
		// HACK This will be very inefficient, for example if we use
		//   many small intervalls to get the whole range.
		final List<Integer> toExtractSampleOrigIndices = getSampleOrigIndices(from, to);
		final SamplesInfosSource origSource = getOrigSource();
		final List<Integer> allOriginIndices = origSource.getSampleOrigIndices();
		final List<Affection> allOriginAffections = origSource.getAffections();
		final List<Affection> localAffections = extractValuesByOrigIndices(allOriginIndices, allOriginAffections, toExtractSampleOrigIndices);
//		final Iterator<Affection> allOriginAffectionsIt = allOriginAffections.iterator();
//		final List<Affection> localAffections = new ArrayList<Affection>(toExtractSampleOrigIndices.size());
//		for (Integer originIndex : allOriginIndices) {
//			Affection originAffection = allOriginAffectionsIt.next();
//			if (toExtractSampleOrigIndices.contains(originIndex)) {
//				localAffections.add(originAffection);
//			}
//		}
		return localAffections;
	}

	public List<String> getCategories(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_CATEGORY, from, to);
	}

	public List<String> getDiseases(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_DISEASE, from, to);
	}

	public List<String> getPopulations(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_POPULATION, from, to);
	}

	public List<Integer> getAges(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_AGE, from, to);
	}

	public List<String> getFilters(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_FILTER, from, to);
	}

	public List<Integer> getApproveds(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_APPROVED, from, to);
	}

	public List<Integer> getStatuses(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_SAMPLE_STATUS, from, to);
	}
}

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
import ucar.nc2.NetcdfFile;

public class NetCdfSamplesInfosSource extends AbstractNetCdfListSource<SampleInfo> implements SamplesInfosSource {

	private static final int DEFAULT_CHUNK_SIZE = 50;
	private static final int DEFAULT_CHUNK_SIZE_SHATTERED = 1;

	private final DataSetSource dataSetSource;
	private final StudyKey studyKey;
	private SamplesInfosSource originSource;

	private NetCdfSamplesInfosSource(
			final MatrixKey origin,
			final DataSetSource dataSetSource,
			final StudyKey studyKey,
			final NetcdfFile rdNetCdfFile)
	{
		super(origin, rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_SAMPLESET);

		this.dataSetSource = dataSetSource;
		this.studyKey = studyKey;
		this.originSource = null;
	}

	public static SamplesInfosSource createForMatrix(
			DataSetSource dataSetSource,
			StudyKey studyKey,
			NetcdfFile rdNetCdfFile)
			throws IOException
	{
		return new NetCdfSamplesInfosSource(null, dataSetSource, studyKey, rdNetCdfFile);
	}

	private DataSetSource getDataSetSource() {
		return dataSetSource;
	}

	@Override
	public SamplesInfosSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getSamplesInfosSource();
			}
		}

		return originSource;
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

	public List<Integer> getOrderIds(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLE_ORDER_ID, from, to);
	}

	public List<String> getFathers(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLE_FATHER, from, to);
	}

	public List<String> getMothers(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLE_MOTHER, from, to);
	}

	public List<Sex> getSexes(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLES_SEX, new Extractor.IntToEnumExtractor(Sex.values()), from, to);
	}

	public List<Affection> getAffections(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLES_AFFECTION, new Extractor.IntToEnumExtractor(Affection.values()), from, to);
	}

	public List<String> getCategories(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLE_CATEGORY, from, to);
	}

	public List<String> getDiseases(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLE_DISEASE, from, to);
	}

	public List<String> getPopulations(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLE_POPULATION, from, to);
	}

	public List<Integer> getAges(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLE_AGE, from, to);
	}

	public List<String> getFilters(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLE_FILTER, from, to);
	}

	public List<Integer> getApproveds(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLE_APPROVED, from, to);
	}

	public List<Integer> getStatuses(int from, int to) throws IOException {

		// we are the origin
		// we have direct storage of all sample info attributes
		return readVar(cNetCDF.Variables.VAR_SAMPLE_STATUS, from, to);
	}
}

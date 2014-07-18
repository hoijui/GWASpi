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

package org.gwaspi.datasource.inmemory;

import java.io.IOException;
import java.util.List;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.model.StudyKey;

public class InMemorySamplesInfosSource extends AbstractInMemoryListSource<SampleInfo> implements SamplesInfosSource {

	private final StudyKey studyKey;
	private SamplesInfosSource originSource;
	private final DataSetSource dataSetSource;

	private InMemorySamplesInfosSource(final DataSetSource dataSetSource, MatrixKey origin, StudyKey studyKey, final List<SampleInfo> items) {
		super(origin, items);

		this.studyKey = studyKey;
		this.dataSetSource = dataSetSource;
	}

	private InMemorySamplesInfosSource(final DataSetSource dataSetSource, MatrixKey origin, StudyKey studyKey, final List<SampleInfo> items, List<Integer> originalIndices) {
		super(origin, items, originalIndices);

		this.studyKey = studyKey;
		this.dataSetSource = dataSetSource;
	}

	public static SamplesInfosSource createForMatrix(final DataSetSource dataSetSource, MatrixKey origin, StudyKey studyKey, final List<SampleInfo> items) throws IOException {
		return new InMemorySamplesInfosSource(dataSetSource, origin, studyKey, items);
	}

	public static SamplesInfosSource createForOperation(final DataSetSource dataSetSource, MatrixKey origin, StudyKey studyKey, final List<SampleInfo> items, List<Integer> originalIndices) throws IOException {
		return new InMemorySamplesInfosSource(dataSetSource, origin, studyKey, items, originalIndices);
	}

	// XXX same code as in the NetCDF counterpart!
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

	private DataSetSource getDataSetSource() {
		return dataSetSource;
	}

	@Override
	public SamplesKeysSource getKeysSource() throws IOException {
		return getDataSetSource().getSamplesKeysSource();
	}

	@Override
	public List<Integer> getOrderIds() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_ORDER_ID);
	}

	@Override
	public List<String> getFathers() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_FATHER_ID);
	}

	@Override
	public List<String> getMothers() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_MOTHER_ID);
	}

	@Override
	public List<SampleInfo.Sex> getSexes() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_SEX);
	}

	@Override
	public List<SampleInfo.Affection> getAffections() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_AFFECTION);
	}

	@Override
	public List<String> getCategories() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_CATEGORY);
	}

	@Override
	public List<String> getDiseases() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_DISEASE);
	}

	@Override
	public List<String> getPopulations() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_POPULATION);
	}

	@Override
	public List<Integer> getAges() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_AGE);
	}

	@Override
	public List<String> getFilters() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_FILTER);
	}

	@Override
	public List<Integer> getApproveds() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_APPROVED);
	}

	@Override
	public List<Integer> getStatuses() throws IOException {
		return extractProperty(getItems(), SampleInfo.TO_STATUS);
	}
}

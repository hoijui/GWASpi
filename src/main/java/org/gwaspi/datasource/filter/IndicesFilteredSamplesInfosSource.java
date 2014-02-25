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

package org.gwaspi.datasource.filter;

import java.io.IOException;
import java.util.List;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;

public class IndicesFilteredSamplesInfosSource extends IndicesFilteredList<SampleInfo> implements SamplesInfosSource {

	private final SamplesInfosSource wrapped;

	public IndicesFilteredSamplesInfosSource(final SamplesInfosSource wrapped, final List<Integer> includeIndices) {
		super(wrapped, includeIndices);

		this.wrapped = wrapped;
	}

	@Override
	public SamplesKeysSource getKeysSource() throws IOException {
		XXX;
	}

//	@Override
//	public List<Integer> getSampleOrigIndices() throws IOException {
//		return new IndicesFilteredList<Integer>(wrapped.getSampleOrigIndices(), getIncludeIndices());
//	}
//
//	@Override
//	public List<SampleKey> getSampleKeys() throws IOException {
//		return new IndicesFilteredList<SampleKey>(wrapped.getSampleKeys(), getIncludeIndices());
//	}

	@Override
	public List<Integer> getOrderIds() throws IOException {
		return new IndicesFilteredList<Integer>(wrapped.getOrderIds(), getIncludeIndices());
	}

	@Override
	public List<String> getFathers() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getFathers(), getIncludeIndices());
	}

	@Override
	public List<String> getMothers() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getMothers(), getIncludeIndices());
	}

	@Override
	public List<Sex> getSexes() throws IOException {
		return new IndicesFilteredList<Sex>(wrapped.getSexes(), getIncludeIndices());
	}

	@Override
	public List<Affection> getAffections() throws IOException {
		return new IndicesFilteredList<Affection>(wrapped.getAffections(), getIncludeIndices());
	}

	@Override
	public List<String> getCategories() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getCategories(), getIncludeIndices());
	}

	@Override
	public List<String> getDiseases() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getDiseases(), getIncludeIndices());
	}

	@Override
	public List<String> getPopulations() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getPopulations(), getIncludeIndices());
	}

	@Override
	public List<Integer> getAges() throws IOException {
		return new IndicesFilteredList<Integer>(wrapped.getAges(), getIncludeIndices());
	}

	@Override
	public List<String> getFilters() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getFilters(), getIncludeIndices());
	}

	@Override
	public List<Integer> getApproveds() throws IOException {
		return new IndicesFilteredList<Integer>(wrapped.getApproveds(), getIncludeIndices());
	}

	@Override
	public List<Integer> getStatuses() throws IOException {
		return new IndicesFilteredList<Integer>(wrapped.getStatuses(), getIncludeIndices());
	}

//	@Override
//	public List<Integer> getSampleOrigIndices(int from, int to) throws IOException {
//		return IndicesFilteredList.getWrappedRange(wrapped.getSampleOrigIndices(), getIncludeIndices(), from, to);
//	}
//
//	@Override
//	public List<SampleKey> getSampleKeys(int from, int to) throws IOException {
//		return IndicesFilteredList.getWrappedRange(wrapped.getSampleKeys(), getIncludeIndices(), from, to);
//	}

//	@Override
	public List<Integer> getOrderIds(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getOrderIds(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<String> getFathers(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getFathers(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<String> getMothers(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getMothers(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<SampleInfo.Sex> getSexes(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getSexes(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<SampleInfo.Affection> getAffections(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getAffections(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<String> getCategories(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getCategories(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<String> getDiseases(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getDiseases(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<String> getPopulations(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getPopulations(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<Integer> getAges(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getAges(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<String> getFilters(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getFilters(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<Integer> getApproveds(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getApproveds(), getIncludeIndices(), from, to);
	}

//	@Override
	public List<Integer> getStatuses(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getStatuses(), getIncludeIndices(), from, to);
	}
}

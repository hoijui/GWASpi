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
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;

public class IndicesFilteredMarkersMetadataSource extends IndicesFilteredList<MarkerMetadata> implements MarkersMetadataSource {

	private final DataSetSource dataSetSource;
	private final MarkersMetadataSource wrapped;

	public IndicesFilteredMarkersMetadataSource(final DataSetSource dataSetSource, final MarkersMetadataSource wrapped, final List<Integer> includeIndices) {
		super(wrapped, includeIndices);

		this.dataSetSource = dataSetSource;
		this.wrapped = wrapped;
	}

	@Override
	public MarkersKeysSource getKeysSource() throws IOException {
		return dataSetSource.getMarkersKeysSource();
	}

	@Override
	public List<String> getMarkerIds() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getMarkerIds(), getIncludeIndices());
	}

	@Override
	public List<String> getRsIds() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getRsIds(), getIncludeIndices());
	}

	@Override
	public List<String> getChromosomes() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getChromosomes(), getIncludeIndices());
	}

	@Override
	public List<Integer> getPositions() throws IOException {
		return new IndicesFilteredList<Integer>(wrapped.getPositions(), getIncludeIndices());
	}

	@Override
	public List<String> getAlleles() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getAlleles(), getIncludeIndices());
	}

	@Override
	public List<String> getStrands() throws IOException {
		return new IndicesFilteredList<String>(wrapped.getStrands(), getIncludeIndices());
	}

//	@Override
//	public List<String> getMarkerIds(int from, int to) throws IOException {
//		return IndicesFilteredList.getWrappedRange(wrapped.getMarkerIds(), getIncludeIndices(), from, to);
//	}
//
//	@Override
//	public List<String> getRsIds(int from, int to) throws IOException {
//		return IndicesFilteredList.getWrappedRange(wrapped.getRsIds(), getIncludeIndices(), from, to);
//	}
//
//	@Override
//	public List<String> getChromosomes(int from, int to) throws IOException {
//		return IndicesFilteredList.getWrappedRange(wrapped.getChromosomes(), getIncludeIndices(), from, to);
//	}
//
//	@Override
//	public List<Integer> getPositions(int from, int to) throws IOException {
//		return IndicesFilteredList.getWrappedRange(wrapped.getPositions(), getIncludeIndices(), from, to);
//	}
//
//	@Override
//	public List<String> getAlleles(int from, int to) throws IOException {
//		return IndicesFilteredList.getWrappedRange(wrapped.getAlleles(), getIncludeIndices(), from, to);
//	}
//
//	@Override
//	public List<String> getStrands(int from, int to) throws IOException {
//		return IndicesFilteredList.getWrappedRange(wrapped.getStrands(), getIncludeIndices(), from, to);
//	}
}

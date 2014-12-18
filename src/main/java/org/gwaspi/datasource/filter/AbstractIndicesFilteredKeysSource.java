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
import java.util.Map;
import org.gwaspi.model.AbstractKeysSource;

public class AbstractIndicesFilteredKeysSource<K> extends IndicesFilteredListSource<K> {

	private final AbstractKeysSource<K> wrapped;

	public AbstractIndicesFilteredKeysSource(final AbstractKeysSource<K> wrapped, final List<Integer> includeIndices) {
		super(wrapped, includeIndices);

		this.wrapped = wrapped;
	}

	public List<Integer> getIndices() throws IOException {
		return new IndicesFilteredList<Integer>(wrapped.getIndices(), getIncludeIndices());
	}

	public List<Integer> getIndices(int from, int to) throws IOException {
		return IndicesFilteredList.getWrappedRange(wrapped.getIndices(), getIncludeIndices(), from, to);
	}

	public Map<Integer, K> getIndicesMap() throws IOException {
		return new IndicesFilteredMap<Integer, K>(wrapped.getIndicesMap(), getIncludeIndices());
	}

	public Map<Integer, K> getIndicesMap(int from, int to) throws IOException {
		return IndicesFilteredMap.getWrappedRange(wrapped.getIndicesMap(), getIncludeIndices(), from, to);
	}
}

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

import java.util.AbstractList;
import java.util.List;

public class IndicesFilteredList<T> extends AbstractList<T> implements List<T> {

	private final List<T> wrapped;
	private final List<Integer> includeIndices;

	public IndicesFilteredList(final List<T> wrapped, final List<Integer> includeIndices) {

		this.wrapped = wrapped;
		this.includeIndices = includeIndices;
	}

	public static <LT> List<LT> getWrappedRange(final List<LT> wrapped, final List<Integer> includeIndices, int from, int to) {
		return new IndicesFilteredList(wrapped, includeIndices.subList(from, to));
	}

	protected List<T> getWrapped() {
		return wrapped;
	}

	protected List<Integer> getIncludeIndices() {
		return includeIndices;
	}

	@Override
	public T get(int index) {
		return wrapped.get(includeIndices.get(index));
	}

	@Override
	public int size() {
		return includeIndices.size();
	}
}

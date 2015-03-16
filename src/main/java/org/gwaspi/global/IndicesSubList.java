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

package org.gwaspi.global;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

/**
 * A {@link List} implementation presenting only a given sub-list of indices
 * of a certain backend list.
 * NOTE If the indices list is much smaller then the backend list,
 *   and the backend list is not a {@link java.util.RandomAccess},
 *   then this list will be very inefficient!
 */
public class IndicesSubList<T> extends AbstractList<T> implements Serializable {

	private final List<T> backend;
	private final List<Integer> indices;

	public IndicesSubList(final List<T> backend, final List<Integer> indices) {

		this.backend = backend;
		this.indices = indices;
	}

	@Override
	public T get(int index) {
		return backend.get(indices.get(index));
	}

	@Override
	public int size() {
		return indices.size();
	}
}

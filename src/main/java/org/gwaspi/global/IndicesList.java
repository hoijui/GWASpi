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

import java.util.AbstractList;

/**
 * A List implementation returning a given range of indices,
 * using only constant (O(1)) amount of storage.
 */
public class IndicesList extends AbstractList<Integer> {

	private final int size;
	private final int start;

	public IndicesList(final int size, final int start) {

		this.size = size;
		this.start = start;
	}

	public IndicesList(final int size) {
		this(size, 0);
	}

	@Override
	public Integer get(int index) {
		return start + index;
	}

	@Override
	public int size() {
		return size;
	}
}

/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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
 * TODO
 */
public class GeneratedList<T> extends AbstractList<T> {

	private final int size;
	private final Generator<T> generator;

	public GeneratedList(final int size, final Generator<T> generator) {

		this.size = size;
		this.generator = generator;
	}

	@Override
	public T get(final int index) {

		if ((index < 0) || (index >= size)) {
			throw new IndexOutOfBoundsException("tried to fetch index "
					+ index + " from list of size " + size);
		}
		return generator.generate(index);
	}

	@Override
	public int size() {
		return size;
	}
}

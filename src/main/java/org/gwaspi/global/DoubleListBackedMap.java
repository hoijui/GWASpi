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

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

/**
 * TODO add class description
 * @deprecated this class is currently unused, and also not implemented; delete it?
 * @param <KT>
 * @param <VT>
 */
public class DoubleListBackedMap<KT, VT> extends AbstractMap<KT, VT> {

	private final List<KT> keys;
	private final List<VT> values;

	public DoubleListBackedMap(final List<KT> keys, final List<VT> values) {

		if (keys.size() != values.size()) {
			throw new IllegalArgumentException("We need the same amount of keys and values");
		}

		this.keys = keys;
		this.values = values;
	}

	@Override
	public int size() {
		return keys.size();
	}

	@Override
	public Set<Entry<KT, VT>> entrySet() {
		throw new UnsupportedOperationException("Not supported yet."); // TODO implement this!
	}
}

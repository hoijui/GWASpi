/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
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

package org.gwaspi.reports;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * @deprecated unused TODO remove this class(?)
 */
class ComparatorPvalAsc implements Comparator<Map.Entry>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(final Map.Entry entry1, final Map.Entry entry2) {
		int diff = ((Comparable) entry1.getValue()).compareTo(entry2.getValue());
		if (diff == 0) {
			diff = ((Comparable) entry1.getKey()).compareTo(entry2.getKey());
		}
		return diff;
	}
}

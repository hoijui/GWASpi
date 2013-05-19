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

import java.util.Comparator;
import java.util.Map;

class ComparatorPvalAsc implements Comparator<Map.Entry> {

	public int compare(Map.Entry e1, Map.Entry e2) {
		int cf = ((Comparable) e1.getValue()).compareTo(e2.getValue());
		if (cf == 0) {
			cf = ((Comparable) e1.getKey()).compareTo(e2.getKey());
		}
		return cf;
	}
}

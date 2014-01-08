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

package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * TODO
 */
public interface AbstractKeysSource<VT> extends List<VT> {

	List<VT> getRange(int from, int to) throws IOException;

	/**
	 * Returns the indices of the elements in the original matrix
	 * @return original indices
	 * @throws IOException implementation dependent
	 */
	List<Integer> getIndices() throws IOException;

	/**
	 * Returns the indices of the elements in the original matrix
	 * @return original indices
	 * @throws IOException implementation dependent
	 */
	List<Integer> getIndices(int from, int to) throws IOException;

	Map<Integer, VT> getIndicesMap() throws IOException;

	Map<Integer, VT> getIndicesMap(int from, int to) throws IOException;
}

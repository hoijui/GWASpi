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

package org.gwaspi.datasource.inmemory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MatrixKey;

public class InMemoryMarkersGenotypesSource extends AbstractInMemoryListSource<GenotypesList> implements MarkersGenotypesSource {

	private MarkersGenotypesSource originSource;
	private static final Map<MatrixKey, MarkersGenotypesSource> KEY_TO_DATA
			= new HashMap<MatrixKey, MarkersGenotypesSource>();

	private InMemoryMarkersGenotypesSource(MatrixKey origin, final List<GenotypesList> items, List<Integer> originalIndices) {
		super(origin, items, originalIndices);
	}

	public static MarkersGenotypesSource createForMatrix(MatrixKey key, final List<GenotypesList> items, List<Integer> originalIndices) throws IOException {

		MarkersGenotypesSource data = KEY_TO_DATA.get(key);
		if (data == null) {
			if (items == null) {
				throw new IllegalStateException("Tried to fetch data that is not available, or tried to create a data-set without giving data");
			}
			data = new InMemoryMarkersGenotypesSource(key, items, originalIndices);
			KEY_TO_DATA.put(key, data);
		}

		return data;
	}

	@Override
	public MarkersGenotypesSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getMarkersGenotypesSource();
			}
		}

		return originSource;
	}
}

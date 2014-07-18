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
import java.util.List;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MatrixKey;

public class InMemoryMarkersGenotypesSource extends AbstractInMemoryListSource<GenotypesList> implements MarkersGenotypesSource {

	private MarkersGenotypesSource originSource;

	private InMemoryMarkersGenotypesSource(MatrixKey origin, final List<GenotypesList> items, List<Integer> originalIndices) {
		super(origin, items, originalIndices);
	}

	public static MarkersGenotypesSource createForMatrix(MatrixKey origin, final List<GenotypesList> items, List<Integer> originalIndices) throws IOException {
		return new InMemoryMarkersGenotypesSource(origin, items, originalIndices);
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

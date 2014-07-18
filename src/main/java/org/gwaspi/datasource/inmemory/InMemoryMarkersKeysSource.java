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

package org.gwaspi.datasource.inmemory;

import java.io.IOException;
import java.util.List;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerKeyFactory;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MatrixKey;

public class InMemoryMarkersKeysSource extends AbstractInMemoryKeysSource<MarkerKey> implements MarkersKeysSource {

	private MarkersKeysSource originSource;

	private InMemoryMarkersKeysSource(MatrixKey origin, List<MarkerKey> items, List<Integer> originalIndices) {
		super(origin, items, originalIndices);

		this.originSource = null;
	}

	public static MarkersKeysSource createForMatrix(MatrixKey origin, List<MarkerKey> items) throws IOException {
		return new InMemoryMarkersKeysSource(origin, items, null);
	}

	public static MarkersKeysSource createForOperation(MatrixKey origin, List<MarkerKey> items, List<Integer> originalIndices) throws IOException {
		return new InMemoryMarkersKeysSource(origin, items, originalIndices);
	}

	@Override
	public MarkersKeysSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getMarkersKeysSource();
			}
		}

		return originSource;
	}

	@Override
	protected KeyFactory<MarkerKey> createKeyFactory() {
		return new MarkerKeyFactory();
	}
}

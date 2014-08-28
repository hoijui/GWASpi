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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerKeyFactory;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;

public class InMemoryMarkersKeysSource extends AbstractInMemoryKeysSource<MarkerKey> implements MarkersKeysSource {

	private static final Map<MatrixKey, MarkersKeysSource> KEY_TO_DATA
			= new HashMap<MatrixKey, MarkersKeysSource>();
	private static final Map<OperationKey, MarkersKeysSource> KEY2_TO_DATA
			= new HashMap<OperationKey, MarkersKeysSource>();

	private MarkersKeysSource originSource;

	private InMemoryMarkersKeysSource(MatrixKey key, List<MarkerKey> items, List<Integer> originalIndices) {
		super(key, items, originalIndices);

		this.originSource = null;
	}

	public static MarkersKeysSource createForMatrix(MatrixKey key, List<MarkerKey> items) throws IOException {
//		return createForOperation(key, items, null);

		MarkersKeysSource data = KEY_TO_DATA.get(key);
		if (data == null) {
			if (items == null) {
				throw new IllegalStateException("Tried to fetch data that is not available, or tried to create a data-set without giving data");
			}
			data = new InMemoryMarkersKeysSource(key, items, null);
			KEY_TO_DATA.put(key, data);
		} else if (items != null) {
			throw new IllegalStateException("Tried to store data under a key that is already present. key: " + key.toRawIdString());
		}

		return data;
	}

	public static MarkersKeysSource createForOperation(OperationKey key, List<MarkerKey> items, List<Integer> originalIndices) throws IOException {

		MarkersKeysSource data = KEY2_TO_DATA.get(key);
		if (data == null) {
			if (items == null) {
				throw new IllegalStateException("Tried to fetch data that is not available, or tried to create a data-set without giving data");
			}
			data = new InMemoryMarkersKeysSource(key.getParentMatrixKey(), items, originalIndices);
			KEY2_TO_DATA.put(key, data);
		} else if (items != null) {
			throw new IllegalStateException("Tried to store data under a key that is already present. key: " + key.toRawIdString());
		}

		return data;
	}

	public static void clearStorage() {

		KEY_TO_DATA.clear();
		KEY2_TO_DATA.clear();
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

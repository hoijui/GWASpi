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
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.ChromosomeKeyFactory;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;

public class InMemoryChromosomesKeysSource extends AbstractInMemoryKeysSource<ChromosomeKey> implements ChromosomesKeysSource {

	private static final Map<MatrixKey, ChromosomesKeysSource> KEY_TO_DATA
			= new HashMap<MatrixKey, ChromosomesKeysSource>();
	private static final Map<OperationKey, ChromosomesKeysSource> KEY2_TO_DATA
			= new HashMap<OperationKey, ChromosomesKeysSource>();

	private ChromosomesKeysSource originSource;

	private InMemoryChromosomesKeysSource(MatrixKey origin, List<ChromosomeKey> items, List<Integer> originalIndices) {
		super(origin, items, originalIndices);

		this.originSource = null;
	}

	public static ChromosomesKeysSource createForMatrix(MatrixKey key, List<ChromosomeKey> items) throws IOException {
//		return createForOperation(key, items, null);

		ChromosomesKeysSource data = KEY_TO_DATA.get(key);
		if (data == null) {
			if (items == null) {
				throw new IllegalStateException("Tried to fetch data that is not available, or tried to create a data-set without giving data");
			}
			data = new InMemoryChromosomesKeysSource(key, items, null);
			KEY_TO_DATA.put(key, data);
		} else if (items != null) {
			throw new IllegalStateException("Tried to store data under a key that is already present. key: " + key.toRawIdString());
		}

		return data;
	}

	public static ChromosomesKeysSource createForOperation(OperationKey key, List<ChromosomeKey> items, List<Integer> originalIndices) throws IOException {

		ChromosomesKeysSource data = KEY2_TO_DATA.get(key);
		if (data == null) {
			if (items == null) {
				throw new IllegalStateException("Tried to fetch data that is not available, or tried to create a data-set without giving data");
			}
			data = new InMemoryChromosomesKeysSource(key.getParentMatrixKey(), items, originalIndices);
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
	public ChromosomesKeysSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getChromosomesKeysSource();
			}
		}

		return originSource;
	}

	@Override
	protected KeyFactory<ChromosomeKey> createKeyFactory() {
		return new ChromosomeKeyFactory();
	}
}

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

import java.awt.Dimension;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.operations.NetCdfUtils;

public abstract class AbstractInMemoryKeysSource<K> extends AbstractInMemoryListSource<K> {

	private final Map<Integer, K> indicesMap;

	public AbstractInMemoryKeysSource(MatrixKey origin, List<K> items, List<Integer> originalIndices) {
		super(origin, items, originalIndices);

		this.indicesMap = mergeListsIntoMap(getOriginalIndices(), items);
	}

	private static <KT, VT> Map<KT, VT> mergeListsIntoMap(final List<KT> keys, final List<VT> values) {

		Map<KT, VT> mergedMap = new LinkedHashMap<KT, VT>(keys.size());

		final Iterator<KT> keysIt = keys.iterator();
		final Iterator<VT> valuesIt = values.iterator();
		while(keysIt.hasNext()) {
			mergedMap.put(keysIt.next(), valuesIt.next());
		}

		return mergedMap;
	}

	protected abstract KeyFactory<K> createKeyFactory();

	public List<Integer> getIndices(int from, int to) throws IOException {

		final List<Integer> indices;

		final Dimension fromTo = new Dimension(from, to);
		NetCdfUtils.checkDimensions(size(), fromTo);
		final int fromClean = fromTo.width;
		final int toClean = fromTo.height;

		indices = getOriginalIndices(fromClean, toClean - fromClean + 1);

		return indices;
	}

	public List<Integer> getIndices() throws IOException {
		return getIndices(-1, -1);
	}

//	@Override
//	public List<KT> getRange(int from, int to) throws IOException {
//
//		List<KT> entries;
//
//		List<String> keys = readVar(varKeys, from, to);
//
//		entries = new ArrayList<KT>(keys.size());
//		KeyFactory<KT> keyFactory = createKeyFactory();
//		for (String encodedKey : keys) {
//			entries.add(keyFactory.decode(encodedKey));
//		}
//
//		return getItems().subList(from, to). ???;
//	}

	public Map<Integer, K> getIndicesMap() throws IOException {
		return indicesMap;
	}

	public Map<Integer, K> getIndicesMap(int from, int to) throws IOException {
		return mergeListsIntoMap(getIndices(from, to), getRange(from, to));
	}
}

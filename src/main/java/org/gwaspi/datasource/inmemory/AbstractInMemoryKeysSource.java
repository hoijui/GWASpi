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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.global.IndicesList;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.MatrixKey;
import static org.gwaspi.operations.NetCdfUtils.checkDimensions;

public abstract class AbstractInMemoryKeysSource<KT> extends AbstractInMemoryListSource<KT> {

	public AbstractInMemoryKeysSource(MatrixKey origin, List<KT> items, List<Integer> originalIndices) {
		super(origin, items, originalIndices);
	}

	protected abstract KeyFactory<KT> createKeyFactory();

	public List<Integer> getIndices(int from, int to) throws IOException {

		final List<Integer> indices;

		final Dimension fromTo = new Dimension(from, to);
		checkDimensions(size(), fromTo);
		final int fromClean = fromTo.width;
		final int toClean = fromTo.height;

		if (getOriginalIndices() == null) {
			indices = new IndicesList(toClean - fromClean, fromClean);
		} else {
			indices = getOriginalIndices().subList(fromClean, toClean);
		}

		return indices;
	}

	public List<Integer> getIndices() throws IOException {
		return getIndices(-1, -1);
	}

	@Override
	public List<KT> getRange(int from, int to) throws IOException {

		List<KT> entries;

		List<String> keys = readVar(varKeys, from, to);

		entries = new ArrayList<KT>(keys.size());
		KeyFactory<KT> keyFactory = createKeyFactory();
		for (String encodedKey : keys) {
			entries.add(keyFactory.decode(encodedKey));
		}

		return getItems().subList(from, to). ???;
	}

	public Map<Integer, KT> getIndicesMap() throws IOException {
		return getIndicesMap(-1, -1);
	}

	public Map<Integer, KT> getIndicesMap(int from, int to) throws IOException {

		Map<Integer, KT> entries;

		List<String> keys = readVar(varKeys, from, to);

		entries = new LinkedHashMap<Integer, KT>(keys.size());
		KeyFactory<KT> keyFactory = createKeyFactory();
		if (varOriginalIndices == null) {
			int index = (from >= 0) ? from : 0;
			for (String encodedKey : keys) {
				entries.put(index++, keyFactory.decode(encodedKey));
			}
		} else {
			List<Integer> originalIndices = readVar(varOriginalIndices, from, to);
			Iterator<Integer> originalIndicesIt = originalIndices.iterator();
			for (String encodedKey : keys) {
				entries.put(originalIndicesIt.next(), keyFactory.decode(encodedKey));
			}
		}

		return entries;
	}
}

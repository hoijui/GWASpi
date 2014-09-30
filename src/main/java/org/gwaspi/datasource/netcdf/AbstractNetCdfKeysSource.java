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

package org.gwaspi.datasource.netcdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.KeyFactory;
import ucar.nc2.NetcdfFile;

public abstract class AbstractNetCdfKeysSource<KT> extends AbstractNetCdfListSource<KT> {

	private final String varOriginalIndices;
	private final String varKeys;

	public AbstractNetCdfKeysSource(NetcdfFile rdNetCdfFile, int chunkSize, String varDimension, String varOriginalIndices, String varKeys) {
		super(rdNetCdfFile, chunkSize, varDimension);

		this.varOriginalIndices = varOriginalIndices;
		this.varKeys = varKeys;
	}

	protected abstract KeyFactory<KT> createKeyFactory();

	public List<Integer> getIndices(int from, int to) throws IOException {

		List<Integer> originalIndices;

		if (varOriginalIndices == null) {
			// FIXME We can make a special implementation that uses no storage for this!
			originalIndices = new ArrayList<Integer>(size());
			for (int oi = 0; oi < size(); oi++) {
				originalIndices.add(oi);
			}
		} else {
			originalIndices = readVar(varOriginalIndices, from, to);
		}

		return originalIndices;
	}

	public List<Integer> getIndices() throws IOException {
		return getIndices(-1, -1);
	}

	public List<KT> getRange(int from, int to) throws IOException {

		List<KT> entries;

		List<String> keys = readVar(varKeys, from, to);

		entries = new ArrayList<KT>(keys.size());
		KeyFactory<KT> keyFactory = createKeyFactory();
		for (String encodedKey : keys) {
			entries.add(keyFactory.decode(encodedKey));
		}

		return entries;
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

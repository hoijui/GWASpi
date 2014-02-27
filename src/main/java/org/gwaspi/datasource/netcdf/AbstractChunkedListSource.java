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

package org.gwaspi.datasource.netcdf;

import java.io.IOException;
import java.util.AbstractList;
import java.util.List;

/**
 * TODO
 * @param <VT> list value type
 */
public abstract class AbstractChunkedListSource<VT> extends AbstractList<VT> {

	private final int chunkSize;
	private int loadedChunkNumber;
	private List<VT> loadedChunk;

	protected AbstractChunkedListSource(int chunkSize) {

		this.chunkSize = chunkSize;
		this.loadedChunkNumber = -1;
		this.loadedChunk = null;
	}

	@Override
	public VT get(int index) {

		final int chunkNumber = index / chunkSize;
		final int inChunkPosition = index % chunkSize;

		if (chunkNumber != loadedChunkNumber) {
			try {
//				if (index >= size()) {
//					throw new IndexOutOfBoundsException();
//				}
				final int itemsBefore = chunkNumber * chunkSize;
				final int itemsInAndAfter = sizeInternal() - itemsBefore;
				final int curChunkSize = Math.min(chunkSize, itemsInAndAfter);
				loadedChunk = getRange(itemsBefore, itemsBefore + curChunkSize - 1);
				loadedChunkNumber = chunkNumber;
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		return loadedChunk.get(inChunkPosition);
	}

	/**
	 * We need this, cause we have to know the "inner" size, as in,
	 * the low-level storage size here, instead of the final one,
	 * which might be filtered, and thus less.
	 * @return
	 */
	protected abstract int sizeInternal();

	@Override
	public int size() {
		return sizeInternal();
	}

	protected abstract List<VT> getRange(int from, int to) throws IOException;
}
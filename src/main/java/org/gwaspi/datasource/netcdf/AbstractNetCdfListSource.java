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

package org.gwaspi.netCDF.markers;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;

/**
 * TODO
 */
public abstract class AbstractNetCdfListSource<VT> extends AbstractList<VT> {

	private final int chunkSize;
	private final String varNameDimension;
	private List<Integer> originalIndices;
	private Integer size;
	private final NetcdfFile rdNetCdfFile;
	private int loadedChunkNumber;
	private List<VT> loadedChunk;

	private AbstractNetCdfListSource(NetcdfFile rdNetCdfFile, int chunkSize, List<Integer> originalIndices, String varNameDimension) {

		this.chunkSize = chunkSize;
		this.varNameDimension = varNameDimension;
		this.originalIndices = originalIndices;
		this.size = (originalIndices == null) ? null : originalIndices.size();
		this.rdNetCdfFile = rdNetCdfFile;
		this.loadedChunkNumber = -1;
		this.loadedChunk = null;
	}

	AbstractNetCdfListSource(NetcdfFile rdNetCdfFile, int chunkSize, String varNameDimension) {
		this(rdNetCdfFile, chunkSize, null, varNameDimension);
	}

	AbstractNetCdfListSource(NetcdfFile rdNetCdfFile, int chunkSize, List<Integer> originalIndices) {
		this(rdNetCdfFile, chunkSize, originalIndices, null);
	}

	protected NetcdfFile getReadNetCdfFile() {
		return rdNetCdfFile;
	}

	protected <LVT> List<LVT> readVar(String varName, int from, int to) throws IOException {

		List<LVT> values = new ArrayList<LVT>(0);
		NetCdfUtils.readVariable(rdNetCdfFile, varName, from, to, values, null);
		return values;
	}

	@Override
	public VT get(int index) {

		final int rawIndex;
		if (originalIndices == null) {
			rawIndex = index;
		} else {
			rawIndex = originalIndices.get(index);
		}

		return getRaw(rawIndex);
	}

	private VT getRaw(int index) {

		final int chunkNumber = index / chunkSize;
		final int inChunkPosition = index % chunkSize;

		if (chunkNumber != loadedChunkNumber) {
			try {
//				if (index >= size()) {
//					throw new IndexOutOfBoundsException();
//				}
				final int itemsBefore = chunkNumber * chunkSize;
				final int itemsInAndAfter = size() - itemsBefore;
				final int curChunkSize = Math.min(chunkSize, itemsInAndAfter);
				loadedChunk = getRange(itemsBefore, itemsBefore + curChunkSize - 1);
				loadedChunkNumber = chunkNumber;
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		return loadedChunk.get(inChunkPosition);
	}

	@Override
	public int size() {

		if (size == null) {
			Dimension dim = rdNetCdfFile.findDimension(varNameDimension);
			size =  dim.getLength();
		}

		return size;
	}

	protected abstract List<VT> getRange(int from, int to) throws IOException;
}
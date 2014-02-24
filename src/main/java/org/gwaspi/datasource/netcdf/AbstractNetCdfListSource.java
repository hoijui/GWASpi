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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gwaspi.global.Extractor;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;

/**
 * TODO
 * @param <VT> list value type
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

	protected <ST, LVT> List<LVT> readVar(String varName, Extractor<ST, LVT> storageToFinalValueExtractor, int from, int to) throws IOException {

		List<ST> storageValues = new ArrayList<ST>(0);
		NetCdfUtils.readVariable(rdNetCdfFile, varName, from, to, storageValues, null);

		List<LVT> values = new ArrayList<LVT>(storageValues.size());
		for (ST st : storageValues) {
			values.add(storageToFinalValueExtractor.extract(st));
		}

		return values;
	}

	protected static <VT> List<VT> extractValuesByOrigIndices(final List<Integer> allOriginIndices, final List<VT> allOriginValues, final List<Integer> toExtractOrigIndices) throws IOException {

		// if we had direct storage of all sample info attributes
//		return readVar(cNetCDF.Variables.VAR_SAMPLES_AFFECTION, new Extractor.IntToEnumExtractor(Affection.values()), from, to);

		// ... as we do not, we extract it from the origin
		// HACK This will be very inefficient, for example if we use
		//   many small intervalls to get the whole range.

//		final List<Integer> toExtractSampleOrigIndices = getSampleOrigIndices(from, to);
//		final SamplesInfosSource origSource = getOrigSource();
//		final List<Integer> allOriginIndices = origSource.getSampleOrigIndices();
		final Iterator<VT> allOriginValuesIt = allOriginValues.iterator();
		final List<VT> localValues = new ArrayList<VT>(toExtractOrigIndices.size());
		for (Integer originIndex : allOriginIndices) {
			VT originValues = allOriginValuesIt.next();
			if (toExtractOrigIndices.contains(originIndex)) {
				localValues.add(originValues);
			}
		}

		return localValues;
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
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

package org.gwaspi.datasource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gwaspi.global.IndicesList;

/**
 * TODO
 * @param <VT> list value type
 */
public abstract class AbstractOrigIndicesFilteredChunkedListSource<VT> extends AbstractChunkedListSource<VT> {

	private final List<Integer> originalIndices;
	private Integer sizeFiltered;

	protected AbstractOrigIndicesFilteredChunkedListSource(int chunkSize, List<Integer> originalIndices) {
		super(chunkSize);

		this.originalIndices = originalIndices;
		this.sizeFiltered = (originalIndices == null) ? null : originalIndices.size();
	}

	protected static <VT> List<VT> extractValuesByOrigIndices(final List<Integer> allOriginIndices, final List<VT> allOriginValues, final List<Integer> toExtractOrigIndices) throws IOException {

		// if we had direct storage of all sample info attributes
//		return readVar(cNetCDF.Variables.VAR_SAMPLES_AFFECTION, new Extractor.IntToEnumExtractor(Affection.values()), from, to);

		// ... as we do not, we extract it from the origin
		// HACK This will be very inefficient, for example if we use
		//   many small intervalls to get the whole range.
		//   maybe see java.util.RandomAccess for a possible solution;
		//   it is implemented by ArrayList, but not LinkedList, for example.

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

	protected List<Integer> getOriginalIndicesRaw() {
		return originalIndices;
	}

	protected List<Integer> getOriginalIndices() {

		if (getOriginalIndicesRaw() == null) {
			return new IndicesList(size());
		} else {
			return getOriginalIndicesRaw();
		}
	}

	protected List<Integer> getOriginalIndices(final int fromClean, final int toClean) {

		if (getOriginalIndicesRaw() == null) {
			return new IndicesList(toClean - fromClean, fromClean);
		} else {
			return getOriginalIndicesRaw().subList(fromClean, toClean);
		}
	}

	@Override
	public VT get(int index) {

		final int rawIndex;
		if (originalIndices == null) {
			rawIndex = index;
		} else {
			rawIndex = originalIndices.get(index);
		}

		return super.get(rawIndex);
	}

	@Override
	public int size() {

		if (sizeFiltered == null) {
			sizeFiltered = sizeInternal();
		}

		return sizeFiltered;
	}
}

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

package org.gwaspi.operations.combi;

import java.util.Map;

class InMemorySamplesMarkersStorage<ST> extends AbstractSamplesMarkersStorage<ST> {

	private static final int NO_INDEX = -1;

	private final ST[][] storage;
	private int curSampleIndex;
	private int curMarkerIndex;

	InMemorySamplesMarkersStorage(int numSamples, int numMarkers, Map<String, Object> cache) {
		super(numSamples, numMarkers, cache);

		this.storage = (ST[][]) new Object[numSamples][numMarkers];
		this.curSampleIndex = NO_INDEX;
		this.curMarkerIndex = NO_INDEX;
	}

	InMemorySamplesMarkersStorage(int numSamples, int numMarkers) {
		this(numSamples, numMarkers, null);
	}

	public ST[][] getStorage() {
		return storage;
	}

	@Override
	public void startStoringSample(int sampleIndex) {
		curSampleIndex = sampleIndex;
	}

	@Override
	public void setMarkerValue(int markerIndex, ST value) {
		storage[curSampleIndex][markerIndex] = value;
	}

	@Override
	public void endStoringSample() {
		curSampleIndex = NO_INDEX;
	}

	@Override
	public void startStoringMarker(int markerIndex) {
		curMarkerIndex = markerIndex;
	}

	@Override
	public void setSampleValue(int sampleIndex, ST value) {
		storage[sampleIndex][curMarkerIndex] = value;
	}

	@Override
	public void endStoringMarker() {
		curMarkerIndex = NO_INDEX;
	}
}

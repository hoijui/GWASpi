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

class PartialMarkersInMemorySamplesMarkersStorage<ST> extends AbstractSamplesMarkersStorage<ST> {

	private final int firstMarker;
//	private final int maxMarkers;
	private final SamplesMarkersStorage<ST> backEndStorage;

	PartialMarkersInMemorySamplesMarkersStorage(int numSamples, int numMarkers, Map<String, Object> cache, int firstMarker, SamplesMarkersStorage<ST> backEndStorage) {
		super(numSamples, numMarkers, cache);

		this.firstMarker = firstMarker;
//		this.maxMarkers = backEndStorage.getNumMarkers();
		this.backEndStorage = backEndStorage;
	}

	public int getFirstMarker() {
		return firstMarker;
	}

//	public int getMaxMarkers() {
//		return maxMarkers;
//	}

	public SamplesMarkersStorage<ST> getBackEndStorage() {
		return backEndStorage;
	}

	@Override
	public void startStoringSample(int sampleIndex) {
		backEndStorage.startStoringSample(sampleIndex);
	}

	@Override
	public void setMarkerValue(int markerIndex, ST value) {

		final int backEndMarkerIndex = markerIndex - firstMarker;
		// no need to check this here, as in case of bad parameters,
		// the backend will (or at least should) choke anyway
//		if ((backEndMarkerIndex < 0) || (backEndMarkerIndex >= maxMarkers)) {
//			throw new IllegalArgumentException("Given markerIndex " + markerIndex + " is out of bounds [" + firstMarker + ", " + maxMarkers + ")");
//		}
		backEndStorage.setMarkerValue(backEndMarkerIndex, value);
	}

	@Override
	public void endStoringSample() {
		backEndStorage.endStoringSample();
	}

	@Override
	public void startStoringMarker(int markerIndex) {

		final int backEndMarkerIndex = markerIndex - firstMarker;
		backEndStorage.startStoringMarker(backEndMarkerIndex);
	}

	@Override
	public void setSampleValue(int sampleIndex, ST value) {
		backEndStorage.setSampleValue(sampleIndex, value);
	}

	@Override
	public void endStoringMarker() {
		backEndStorage.endStoringMarker();
	}
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO
 */
class MeanCalculatorSamplesMarkersStorage extends AbstractSamplesMarkersStorage<Float> {

	private final String sampleMeansCacheKey;
	private final String markerMeansCacheKey;
	private final List<Double> sampleSums;
	private final List<Double> markerSums;
	private int currentSampleIndex;
	private int currentMarkerIndex;
	private double currentSampleSum;
	private double currentMarkerSum;

	public MeanCalculatorSamplesMarkersStorage(
			int numSamples,
			int numMarkers,
			final Map<String, Object> cache,
			String sampleMeansCacheKey,
			String markerMeansCacheKey)
	{
		super(numSamples, numMarkers, cache);

		XXX;
		// we don't need any of this (storage stuff? at least the whitening thing)
		// we will "whiten" the encoding table directly, using statistics of how many AA, Aa, aA, aa we have
		// these statistics come either directly from the QAMarkers operation, if we are using all samples,
		// or we first have to generate them separately
		// (maybe by using a QAMarkers operation on a subset of the data? -> NO! too much trouble! create statistics "manually")

		this.sampleMeansCacheKey = sampleMeansCacheKey;
		this.markerMeansCacheKey = markerMeansCacheKey;
		this.sampleSums = (sampleMeansCacheKey == null) ? null : new ArrayList<Double>(numSamples);
		this.markerSums = (markerMeansCacheKey == null) ? null : new ArrayList<Double>(numMarkers);
		this.currentSampleIndex = -1;
		this.currentMarkerIndex = -1;
	}

	@Override
	public void startStoringSample(int sampleIndex) {
		currentSampleIndex = sampleIndex;
	}

	@Override
	public void setMarkerValue(int markerIndex, Float value) {

		if (sampleSums != null) {
			sampleSums.set(currentSampleIndex, sampleSums.get(currentSampleIndex) + value);
		}
		if (sampleSums != null) {
			sampleSums.set(currentSampleIndex, sampleSums.get(currentSampleIndex) + value);
		}
	}

	@Override
	public void endStoringSample() {
		throwSampleStorageNotSupported();
	}

	@Override
	public void startStoringMarker(int markerIndex) {

		currentMarkerIndex = markerIndex;
		currentValues.clear();
	}

	@Override
	public void setSampleValue(int sampleIndex, byte[] value) {
		currentValues.set(sampleIndex, value);
	}

	@Override
	public void endStoringMarker() {
		encoder.encodeGenotypes(currentValues, null, receiver, currentMarkerIndex);
	}
}

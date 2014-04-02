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

class InMemorySamplesFeaturesStorage extends AbstractSamplesFeaturesStorage<Float> {

	private static final int NO_INDEX = -1;

	private final float[][] storage;
	private int curSampleIndex;
	private int curFeatureIndex;

	InMemorySamplesFeaturesStorage(int numSamples, int numFeatures, Map<String, Object> cache) {
		super(numSamples, numFeatures, cache);

		this.storage = new float[numSamples][numFeatures];
		this.curSampleIndex = NO_INDEX;
		this.curFeatureIndex = NO_INDEX;
	}

	InMemorySamplesFeaturesStorage(int numSamples, int numFeatures) {
		this(numSamples, numFeatures, null);
	}

	public float[][] getStorage() {
		return storage;
	}

	@Override
	public void startStoringSample(int sampleIndex) {
		curSampleIndex = sampleIndex;
	}

	@Override
	public void setFeatureValue(int featuresIndex, Float value) {
		storage[curSampleIndex][featuresIndex] = value;
	}

	@Override
	public void endStoringSample() {
		curSampleIndex = NO_INDEX;
	}

	@Override
	public void startStoringFeature(int featuresIndex) {
		curFeatureIndex = featuresIndex;
	}

	@Override
	public void setSampleValue(int sampleIndex, Float value) {
		storage[sampleIndex][curFeatureIndex] = value;
	}

	@Override
	public void endStoringFeature() {
		curFeatureIndex = NO_INDEX;
	}
}

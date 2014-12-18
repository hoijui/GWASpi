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

class PartialFeaturesInMemorySamplesFeaturesStorage<S> extends AbstractSamplesFeaturesStorage<S> {

	private final int firstFeature;
//	private final int maxFeatures;
	private final SamplesFeaturesStorage<S> backEndStorage;

	PartialFeaturesInMemorySamplesFeaturesStorage(int numSamples, int numFeatures, Map<String, Object> cache, int firstFeature, SamplesFeaturesStorage<S> backEndStorage) {
		super(numSamples, numFeatures, cache);

		this.firstFeature = firstFeature;
//		this.maxFeatures = backEndStorage.getNumFeatures();
		this.backEndStorage = backEndStorage;
	}

	public int getFirstMarker() {
		return firstFeature;
	}

//	public int getMaxFeatures() {
//		return maxFeatures;
//	}

	public SamplesFeaturesStorage<S> getBackEndStorage() {
		return backEndStorage;
	}

	@Override
	public void startStoringSample(int sampleIndex) {
		backEndStorage.startStoringSample(sampleIndex);
	}

	@Override
	public void setFeatureValue(int featureIndex, S value) {

		final int backEndFeatureIndex = featureIndex - firstFeature;
		// no need to check this here, as in case of bad parameters,
		// the backend will (or at least should) choke anyway
//		if ((backEndFeatureIndex < 0) || (backEndFeatureIndex >= maxFeatures)) {
//			throw new IllegalArgumentException("Given featureIndex " + featureIndex + " is out of bounds [" + firstFeature + ", " + maxMarkers + ")");
//		}
		backEndStorage.setFeatureValue(backEndFeatureIndex, value);
	}

	@Override
	public void endStoringSample() {
		backEndStorage.endStoringSample();
	}

	@Override
	public void startStoringFeature(int featureIndex) {

		final int backEndFeatureIndex = featureIndex - firstFeature;
		backEndStorage.startStoringFeature(backEndFeatureIndex);
	}

	@Override
	public void setSampleValue(int sampleIndex, S value) {
		backEndStorage.setSampleValue(sampleIndex, value);
	}

	@Override
	public void endStoringFeature() {
		backEndStorage.endStoringFeature();
	}
}

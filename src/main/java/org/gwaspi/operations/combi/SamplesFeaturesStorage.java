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

/**
 * TODO
 */
public interface SamplesFeaturesStorage<ST> {

	/**
	 * This can be used to store data associated with the data-set.
	 * For example, it may hold summary data,
	 * like sum or variance over the samples or markers,
	 * which might be used to speed up later processes.
	 * A single cache will likely be (re)used in multiple storages.
	 */
	Map<String, Object> getCache();

	int getNumSamples();
	int getNumFeatures();

	void startStoringSample(int sampleIndex);
	void setFeatureValue(int featureIndex, ST value);
	void endStoringSample();

	void startStoringFeature(int featureIndex);
	void setSampleValue(int sampleIndex, ST value);
	void endStoringFeature();
}

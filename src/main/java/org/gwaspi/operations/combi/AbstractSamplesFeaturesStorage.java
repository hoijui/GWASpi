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

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 */
abstract class AbstractSamplesFeaturesStorage<S> implements SamplesFeaturesStorage<S> {

	private final Map<String, Object> cache;
	private final int numSamples;
	private final int numFeatures;

	protected AbstractSamplesFeaturesStorage(int numSamples, int numFeatures, Map<String, Object> cache) {

		this.cache = (cache == null) ? new HashMap<String, Object>() : cache;
		this.numSamples = numSamples;
		this.numFeatures = numFeatures;
	}

	protected AbstractSamplesFeaturesStorage(int numSamples, int numFeatures) {
		this(numSamples, numFeatures, null);
	}

	@Override
	public Map<String, Object> getCache() {
		return cache;
	}

	@Override
	public int getNumSamples() {
		return numSamples;
	}

	@Override
	public int getNumFeatures() {
		return numFeatures;
	}
}

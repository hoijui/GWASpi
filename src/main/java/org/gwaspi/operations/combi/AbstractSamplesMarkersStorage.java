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
abstract class AbstractSamplesMarkersStorage<ST> implements SamplesMarkersStorage<ST> {

	private final Map<String, Object> cache;
	private final int numSamples;
	private final int numMarkers;

	protected AbstractSamplesMarkersStorage(int numSamples, int numMarkers, Map<String, Object> cache) {

		this.cache = cache;
		this.numSamples = numSamples;
		this.numMarkers = numMarkers;
	}

	protected AbstractSamplesMarkersStorage(int numSamples, int numMarkers) {
		this(numSamples, numMarkers, new HashMap<String, Object>());
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
	public int getNumMarkers() {
		return numMarkers;
	}
}
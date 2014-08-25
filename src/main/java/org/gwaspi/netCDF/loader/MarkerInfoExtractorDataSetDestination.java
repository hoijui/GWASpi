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

package org.gwaspi.netCDF.loader;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;

/**
 * Forwards all method calls to the internally stored DataSetDestination,
 * and keeps a log of the stored markers.
 */
public class MarkerInfoExtractorDataSetDestination extends ForwardingDataSetDestination {

	private final Map<MarkerKey, MarkerMetadata> markerInfos;

	public MarkerInfoExtractorDataSetDestination(final DataSetDestination internalDataSetDestination) {
		super(internalDataSetDestination);

		this.markerInfos = new LinkedHashMap<MarkerKey, MarkerMetadata>();
	}

	public Map<MarkerKey, MarkerMetadata> getMarkerInfos() {
		return markerInfos;
	}

	@Override
	public void addMarkerMetadata(MarkerMetadata markerMetadata) throws IOException {
		getInternalDataSetDestination().addMarkerMetadata(markerMetadata);

		markerInfos.put(MarkerKey.valueOf(markerMetadata), markerMetadata);
	}
}

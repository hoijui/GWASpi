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
package org.gwaspi.netCDF.markers;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

/**
 * TODO
 */
public class MarkersIterator implements
		Iterator<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>>
{
	private final MarkersIterable markersIterable;
	private final NetcdfFile netCdfFile;
	private int nextMarker;
	private final int totalMarkers;
	private final int totalExcluded;
	/** Number of items excluded so far */
	private int excluded;

	/**
	 * Allows to iterate over the unfiltered MarkerKeys of a matrix.
	 * @param markersIterable
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public MarkersIterator(MarkersIterable markersIterable) throws IOException, InvalidRangeException {

		this.markersIterable = markersIterable;

		MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(
				this.markersIterable.getMatrixKey());
		netCdfFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

		totalMarkers = markersIterable.getMarkerKeys().size();
		MarkersIterable.Excluder<MarkerKey> excluder = markersIterable.getExcluder();
		totalExcluded = (excluder == null) ? 0 : excluder.getTotalExcluded();

		nextMarker = 0;
		excluded = 0;
	}

	@Override
	public boolean hasNext() {
		return (totalMarkers - nextMarker - (totalExcluded - excluded)) > 0;
	}

	@Override
	public Map.Entry<MarkerKey, Map<SampleKey, byte[]>> next() {

		Map<SampleKey, byte[]> samples
				= new LinkedHashMap<SampleKey, byte[]>(this.markersIterable.getSampleKeys().size());
		for (SampleKey sampleKey : this.markersIterable.getSampleKeys()) {
			samples.put(sampleKey, null);
		}
		MarkersIterable.Excluder<MarkerKey> excluder = markersIterable.getExcluder();
		MarkerKey curMarkerKey = markersIterable.getMarkerKeys().get(nextMarker);
		if (excluder != null) {
			while (excluder.isExcluded(curMarkerKey)) {
				excluded++;
				nextMarker++;
				curMarkerKey = markersIterable.getMarkerKeys().get(nextMarker);
			}
		}
		try {
			this.markersIterable.getSampleSet().readAllSamplesGTsFromCurrentMarkerToMap(netCdfFile, samples, nextMarker);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		Map.Entry<MarkerKey, Map<SampleKey, byte[]>> next
				= Collections.singletonMap(curMarkerKey, samples).entrySet().iterator().next();
		nextMarker++;

		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"We do not support removing elements (from the persistent storage) through this iterator.");
	}
}

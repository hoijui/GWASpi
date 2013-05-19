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

package org.gwaspi.netCDF;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.samples.SampleSet;
import ucar.nc2.NetcdfFile;

public class IterateThroughMarkerMap {

	private Map<MarkerKey, Object> basesMap = new LinkedHashMap<MarkerKey, Object>();
	private Map<SampleKey, byte[]> rdSampleSetMap = new LinkedHashMap<SampleKey, byte[]>();
	private Map<SampleKey, Object> wrSampleSetMap = new LinkedHashMap<SampleKey, Object>();
	private SampleSet rdSampleSet = null;

	public IterateThroughMarkerMap() throws IOException {
		// Iterate through pmAllelesAndStrandsMap, use marker item position to read correct GTs from all Samples into rdMarkerIdSetMap.
		int markerNb = 0;
		NetcdfFile rdNcFile = NetcdfFile.open("pathToMatrix");
		for (Map.Entry<MarkerKey, Object> entry : basesMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			String bases = entry.getValue().toString();

			// Get alleles from read matrix
			rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerNb);

			markerNb++;
		}
	}
}

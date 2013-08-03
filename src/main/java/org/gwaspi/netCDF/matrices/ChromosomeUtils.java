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

package org.gwaspi.netCDF.matrices;

import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;

public class ChromosomeUtils {

	private ChromosomeUtils() {
	}

	/**
	 * Map to be aggregated, where is the chromosome, where is the position.
	 */
	public static Map<ChromosomeKey, ChromosomeInfo> aggregateChromosomeInfo(Map<MarkerKey, MarkerMetadata> wrMarkerSetMap, int chrIdx, int posIdx) {
		// RETRIEVE CHROMOSOMES INFO
		Map<ChromosomeKey, ChromosomeInfo> chrSetMap = new LinkedHashMap<ChromosomeKey, ChromosomeInfo>();
		String curChr = "";
		int firstPos = 0;
		int markerCount = 0;
		int idx = 0;
		ChromosomeInfo chrInfo = new ChromosomeInfo();
		for (MarkerMetadata metaInfo : wrMarkerSetMap.values()) {
			// value: markerid, rsId, chr, pos
			if (!curChr.equals(metaInfo.getChr())) {
				if (markerCount != 0) { // Not first time round
					chrSetMap.put(ChromosomeKey.valueOf(curChr), chrInfo);
				}
				firstPos = metaInfo.getPos(); // First physical position in chromosome
				curChr = metaInfo.getChr();
				markerCount = 1;
			}
			chrInfo = new ChromosomeInfo(markerCount, firstPos, metaInfo.getPos(), idx);
			markerCount++;
			idx++;
		}
		chrSetMap.put(ChromosomeKey.valueOf(curChr), chrInfo); // Store last chromosome info

		return chrSetMap;
	}
}

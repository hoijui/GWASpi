package org.gwaspi.netCDF.matrices;

import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

	private Utils() {
	}

	/**
	 * Map to be aggregated, where is the chr, where is the position.
	 */
	public static Map<MarkerKey, int[]> aggregateChromosomeInfo(Map<MarkerKey, MarkerMetadata> wrMarkerSetMap, int chrIdx, int posIdx) {
		// RETRIEVE CHROMOSOMES INFO
		Map<MarkerKey, int[]> chrSetMap = new LinkedHashMap<MarkerKey, int[]>();
		String curChr = "";
		int firstPos = 0;
		int markerCount = 0;
		int idx = 0;
		int[] chrInfo = new int[4];
		for (MarkerMetadata metaInfo : wrMarkerSetMap.values()) {
			// value: markerid, rsId, chr, pos
			if (!curChr.equals(metaInfo.getChr())) {
				if (markerCount != 0) { // Not first time round
					chrSetMap.put(MarkerKey.valueOf(curChr), chrInfo);
					chrInfo = new int[4];
				}
				firstPos = metaInfo.getPos(); // First physical position in chromosome
				curChr = metaInfo.getChr();
				markerCount = 1;
			}
			chrInfo[0] = markerCount; // How many markers in current chromosome
			chrInfo[1] = firstPos; // First physical position in chromosome
			chrInfo[2] = metaInfo.getPos(); // Last physical position in current chromosome
			chrInfo[3] = idx; // Last set index for current chromosome
			markerCount++;
			idx++;
		}
		chrSetMap.put(MarkerKey.valueOf(curChr), chrInfo); // Store last chromosome info

		return chrSetMap;
	}
}

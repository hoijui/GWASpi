package org.gwaspi.netCDF.matrices;

import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.MarkerKey;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

	private Utils() {
	}

	public static Map<MarkerKey, Object> aggregateChromosomeInfo(Map<MarkerKey, Object> wrMarkerSetMap, int chrIdx, int posIdx) { //Map to be aggregated, where is the chr, where is the position
		// RETRIEVE CHROMOSOMES INFO
		Map<MarkerKey, Object> chrSetMap = new LinkedHashMap<MarkerKey, Object>();
		String tmpChr = "";
		int firstPos = 0;
		int markerCount = 0;
		int idx = 0;
		int[] chrInfo = new int[4];
		for (Object valueObj : wrMarkerSetMap.values()) {
			Object[] value = (Object[]) valueObj; // markerid, rsId, chr, pos
			if (!tmpChr.equals(value[chrIdx])) {
				if (markerCount != 0) { // Not first time round
					chrSetMap.put(MarkerKey.valueOf(tmpChr), chrInfo);
					chrInfo = new int[4];
				}
				firstPos = (Integer) value[posIdx]; // First physical position in chromosome
				tmpChr = value[chrIdx].toString();
				markerCount = 1;
			}
			chrInfo[0] = markerCount; // How many markers in current chromosome
			int tmpPos = (Integer) value[posIdx];
			chrInfo[1] = firstPos; // First physical position in chromosome
			chrInfo[2] = tmpPos; // Last physical position in current chromosome
			chrInfo[3] = idx; // Last set index for current chromosome
			markerCount++;
			idx++;
		}
		chrSetMap.put(MarkerKey.valueOf(tmpChr), chrInfo); // Store last chromosome info

		return chrSetMap;
	}
}

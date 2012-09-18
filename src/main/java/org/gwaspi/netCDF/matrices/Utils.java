package org.gwaspi.netCDF.matrices;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

	private Utils() {
	}

	public static Map<String, Object> aggregateChromosomeInfo(Map<String, Object> wrMarkerSetMap, int chrIdx, int posIdx) { //Map to be aggregated, where is the chr, where is the position
		// RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetMap = new LinkedHashMap<String, Object>();
		String tmpChr = "";
		int firstPos = 0;
		int markerCount = 0;
		int idx = 0;
		int[] chrInfo = new int[4];
		for (Map.Entry<String, Object> entry : wrMarkerSetMap.entrySet()) {
			Object[] value = (Object[]) entry.getValue(); // markerid, rsId, chr, pos
			if (!tmpChr.equals(value[chrIdx])) {
				if (markerCount != 0) { // Not first time round
					chrSetMap.put(tmpChr, chrInfo);
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
		chrSetMap.put(tmpChr, chrInfo); // Store last chromosome info

		return chrSetMap;
	}
}

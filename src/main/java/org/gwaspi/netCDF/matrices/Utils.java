package org.gwaspi.netCDF.matrices;

import java.util.Iterator;
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

	public static Map<String, Object> aggregateChromosomeInfo(Map<String, Object> wrMarkerSetLHM, int chrIdx, int posIdx) { //LHM to be aggregated, where is the chr, where is the position
		// RETRIEVE CHROMOSOMES INFO
		LinkedHashMap chrSetLHM = new LinkedHashMap();
		String tmpChr = "";
		int firstPos = 0;
		int markerCount = 0;
		int idx = 0;
		int[] chrInfo = new int[4];
		for (Iterator<String> it = wrMarkerSetLHM.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			Object[] value = (Object[]) wrMarkerSetLHM.get(key); // markerid, rsId, chr, pos
			if (!tmpChr.equals(value[chrIdx])) {
				if (markerCount != 0) { // Not first time round
					chrSetLHM.put(tmpChr, chrInfo);
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
		chrSetLHM.put(tmpChr, chrInfo); // Store last chromosome info

		return chrSetLHM;
	}
}

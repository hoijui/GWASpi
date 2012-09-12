package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Standard;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MetadataLoaderPlink implements MetadataLoader {

	private String mapPath;
	private String pedPath;
	private String strand;
	private int studyId;

	private enum Bases {

		A, C, T, G;
	}
	private static String tabulator = cNetCDF.Defaults.TMP_SEPARATOR;

	public MetadataLoaderPlink(String _mapPath, String _pedPath, String _strand, int _studyId) throws FileNotFoundException {

		mapPath = _mapPath;
		pedPath = _pedPath;
		studyId = _studyId;
		strand = _strand;

	}

	// ACCESSORS
	public Map<String, Object> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAndSortMapFile(mapPath); // chr, markerId, genetic distance, position

		org.gwaspi.global.Utils.sysoutStart("initilaizing marker info");
		System.out.println(Text.All.processing);

		Map<String, Object> markerMetadataLHM = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			// chr;pos;markerId
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos = 0;
			try {
				pos = Integer.parseInt(keyValues[1]);
			} catch (Exception ex) {
				pos = 0;
			}

			// rsId
			String[] valValues = new String[]{entry.getValue()};
			valValues = Utils.fixXYMTChrData(valValues, 0);
//			values = fixPlusAlleles(values);

			Object[] markerInfo = new Object[4];
			markerInfo[0] = keyValues[2]; // 0 => markerid
			markerInfo[1] = valValues[0]; // 1 => rsId
			markerInfo[2] = fixChrData(keyValues[0]);  // 2 => chr
			markerInfo[3] = pos; // 3 => pos

			markerMetadataLHM.put(keyValues[2], markerInfo);
		}

		String description = "Generated sorted MarkerIdSet LHM sorted by chromosome and position";
		logAsWhole(startTime, mapPath, description, studyId);
		return markerMetadataLHM;
	}

	public static SortedMap<String, String> parseAndSortMapFile(String path) throws IOException {

		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());

		String l;
		int count = 0;
		while ((l = inputMapBR.readLine()) != null) {

			String[] mapVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = mapVals[Plink_Standard.map_markerId].trim();
			String rsId = "";
			if (markerId.startsWith("rs")) {
				rsId = markerId;
			}
			String chr = mapVals[Plink_Standard.map_chr].trim();

			//chr;pos;markerId
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(mapVals[Plink_Standard.map_pos].trim());
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerId);

			//rsId
			StringBuilder sbVal = new StringBuilder(rsId); //0 => markerid

			sortedMetadataTM.put(sbKey.toString(), sbVal.toString());

			count++;

			if (count == 1) {
				System.out.println(Text.All.processing);
			} else if (count % 100000 == 0) {
				System.out.println("Parsed annotation lines: " + count);
			}
		}
		System.out.println("Parsed annotation lines: " + count);
		inputMapBR.close();
		fr.close();
		return sortedMetadataTM;
	}

	public Map<String, Object> parseOrigMapFile(String path) throws IOException {
		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		Map<String, Object> origMarkerIdSetLHM = new LinkedHashMap<String, Object>();

		String l;
		while ((l = inputMapBR.readLine()) != null) {

			String[] mapVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = mapVals[Plink_Standard.map_markerId].trim();
			origMarkerIdSetLHM.put(markerId, cNetCDF.Defaults.DEFAULT_GT);
		}
		inputMapBR.close();
		fr.close();
		return origMarkerIdSetLHM;
	}

	public static String fixChrData(String chr) throws IOException {
		if (chr.equals("23")) {
			chr = "X";
		}
		if (chr.equals("24")) {
			chr = "Y";
		}
		if (chr.equals("25")) {
			chr = "XY";
		}
		if (chr.equals("26")) {
			chr = "MT";
		}
		return chr;
	}

	// METHODS
	private static void logAsWhole(String startTime, String dirPath, String description, int studyId) throws IOException {
		// LOG OPERATION IN STUDY HISTORY
		StringBuilder operation = new StringBuilder("\nLoaded MAP metadata in path " + dirPath + ".\n");
		operation.append("Start Time: ").append(startTime).append("\n");
		operation.append("End Time: ").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append(".\n");
		operation.append("Description: ").append(description).append(".\n");
		org.gwaspi.global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
		////////////////////////////////
	}
}

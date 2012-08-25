package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Beagle_Standard;
import org.gwaspi.constants.cNetCDF;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MetadataLoaderBeagle {

	private String markerFilePath;
	private static String chr;
	private String strand;
	private int studyId;

	private enum Bases {

		A, C, T, G;
	}
	private static String tabulator = cNetCDF.Defaults.TMP_SEPARATOR;

	public MetadataLoaderBeagle(String _mapPath, String _chr, String _strand, int _studyId) {

		markerFilePath = _mapPath;
		studyId = _studyId;
		chr = _chr;
		strand = _strand;
	}

	// ACCESSORS
	public Map<String, Object> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		TreeMap tempTM = parseAndSortMarkerFile(markerFilePath); // chr, markerId, genetic distance, position

		org.gwaspi.global.Utils.sysoutStart("initilaizing Marker info");

		Map<String, Object> markerMetadataLHM = new LinkedHashMap<String, Object>();
		for (Iterator it = tempTM.keySet().iterator(); it.hasNext();) {
			String key = it.next().toString();

			// chr;pos;markerId
			String[] keyValues = key.split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos;
			try {
				pos = Integer.parseInt(keyValues[1]);
			} catch (Exception ex) {
				pos = 0;
			}

			// rsId;alleles
			String[] valValues = tempTM.get(key).toString().split(cNetCDF.Defaults.TMP_SEPARATOR);

			Object[] markerInfo = new Object[5];
			markerInfo[0] = keyValues[2]; // 0 => markerid
			markerInfo[1] = valValues[0]; // 1 => rsId
			markerInfo[2] = fixChrData(keyValues[0]); // 2 => chr
			markerInfo[3] = pos; // 3 => pos
			markerInfo[4] = valValues[1]; // 4 => alleles

			markerMetadataLHM.put(keyValues[2], markerInfo);
		}

		String description = "Generated sorted MarkerIdSet LHM sorted by chromosome and position";
		logAsWhole(startTime, markerFilePath, description, studyId);
		return markerMetadataLHM;
	}

	public static TreeMap parseAndSortMarkerFile(String path) throws IOException {
		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		TreeMap sortedMetadataTM = new TreeMap(new ComparatorChrAutPosMarkerIdAsc());

		String l;
		int count = 0;
		while ((l = inputMapBR.readLine()) != null) {

			String[] markerVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = markerVals[Beagle_Standard.rsId].trim();
			String rsId = "";
			if (markerId.startsWith("rs")) {
				rsId = markerId;
			}

			//chr;pos;markerId
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerVals[Beagle_Standard.pos].trim());
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerId);

			//rsId;alleles
			StringBuilder sbVal = new StringBuilder(rsId); //0 => rsId
			sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbVal.append(markerVals[Beagle_Standard.allele1].trim()).append(markerVals[Beagle_Standard.allele2].trim()); //1 => alleles

			sortedMetadataTM.put(sbKey.toString(), sbVal.toString());

			count++;

			if (count == 1) {
				System.out.println(org.gwaspi.global.Text.All.processing);
			} else if (count % 100000 == 0) {
				System.out.println("Parsed annotation lines: " + count);
			}
		}
		System.out.println("Parsed annotation lines: " + count);
		inputMapBR.close();
		fr.close();
		return sortedMetadataTM;
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

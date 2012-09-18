package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Beagle_Standard;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import java.io.BufferedReader;
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
public class MetadataLoaderBeagle implements MetadataLoader {

	private String markerFilePath;
	private String chr;
	private String strand;
	private int studyId;

	public MetadataLoaderBeagle(String _mapPath, String _chr, String _strand, int _studyId) {

		markerFilePath = _mapPath;
		studyId = _studyId;
		chr = _chr;
		strand = _strand;
	}

	public Map<String, Object> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAndSortMarkerFile(markerFilePath); // chr, markerId, genetic distance, position

		org.gwaspi.global.Utils.sysoutStart("initilaizing Marker info");

		Map<String, Object> markerMetadataMap = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			// chr;pos;markerId
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos;
			try {
				pos = Integer.parseInt(keyValues[1]);
			} catch (Exception ex) {
				pos = 0;
			}

			// rsId;alleles
			String[] valValues = entry.getValue().split(cNetCDF.Defaults.TMP_SEPARATOR);

			Object[] markerInfo = new Object[5];
			markerInfo[0] = keyValues[2]; // 0 => markerid
			markerInfo[1] = valValues[0]; // 1 => rsId
			markerInfo[2] = fixChrData(keyValues[0]); // 2 => chr
			markerInfo[3] = pos; // 3 => pos
			markerInfo[4] = valValues[1]; // 4 => alleles

			markerMetadataMap.put(keyValues[2], markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		MetadataLoaderPlink.logAsWhole(startTime, markerFilePath, description, studyId);
		return markerMetadataMap;
	}

	private SortedMap<String, String> parseAndSortMarkerFile(String path) throws IOException {
		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());

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

	private static String fixChrData(String chr) {

		String chrFixed = chr;

		if (chrFixed.equals("23")) {
			chrFixed = "X";
		}
		if (chrFixed.equals("24")) {
			chrFixed = "Y";
		}
		if (chrFixed.equals("25")) {
			chrFixed = "XY";
		}
		if (chrFixed.equals("26")) {
			chrFixed = "MT";
		}

		return chrFixed;
	}
}

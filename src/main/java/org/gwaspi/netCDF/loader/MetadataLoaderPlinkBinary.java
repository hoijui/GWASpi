package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Binary;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
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
public class MetadataLoaderPlinkBinary implements MetadataLoader {

	private String bimPath;
	private StrandType strand;
	private int studyId;

	public MetadataLoaderPlinkBinary(String bimPath, StrandType strand, int studyId) {

		this.bimPath = bimPath;
		this.studyId = studyId;
		this.strand = strand;
	}

	public Map<String, Object> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAndSortBimFile(bimPath); // chr, markerId, genetic distance, position

		org.gwaspi.global.Utils.sysoutStart("initilaizing marker info");
		System.out.println(Text.All.processing);

		Map<String, Object> markerMetadataMap = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			//chr;pos;markerId
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos;
			try {
				pos = Integer.parseInt(keyValues[1]);
			} catch (Exception ex) {
				pos = 0;
			}

			//rsId
			String valValues = entry.getValue();
//			values = fixPlusAlleles(values);

			Object[] markerInfo = new Object[5];
			markerInfo[0] = keyValues[2]; // 0 => markerid
			String rsId = "";
			if (keyValues[2].startsWith("rs")) {
				rsId = keyValues[2];
			}
			markerInfo[1] = rsId; // 1 => rsId
			markerInfo[2] = fixChrData(keyValues[0]); // 2 => chr
			markerInfo[3] = pos; // 3 => pos
			markerInfo[4] = valValues; // 4 => alleles

			markerMetadataMap.put(keyValues[2], markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		return markerMetadataMap;
	}

	private static SortedMap<String, String> parseAndSortBimFile(String path) throws IOException {

		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());

		String l;
		int count = 0;
		while ((l = inputMapBR.readLine()) != null) {

			String[] bimVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = bimVals[Plink_Binary.bim_markerId].trim();
			String rsId = "";
			if (markerId.startsWith("rs")) {
				rsId = markerId;
			}
			String chr = bimVals[Plink_Binary.bim_chr].trim();

			// chr;pos;markerId
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(bimVals[Plink_Binary.bim_pos].trim());
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerId);

			// rsId
			StringBuilder sbVal = new StringBuilder(); //0 => markerid
			sbVal.append(bimVals[Plink_Binary.bim_allele1].trim());
			sbVal.append(bimVals[Plink_Binary.bim_allele2].trim());

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

	public Map<String, Object> parseOrigBimFile(String path) throws IOException {
		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		Map<String, Object> origMarkerIdSetMap = new LinkedHashMap<String, Object>();

		String l;


		while ((l = inputMapBR.readLine()) != null) {
			String[] alleles = new String[2];
			String[] mapVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = mapVals[Plink_Binary.bim_markerId].trim();
			alleles[0] = mapVals[Plink_Binary.bim_allele1].trim();
			alleles[1] = mapVals[Plink_Binary.bim_allele2].trim();
			origMarkerIdSetMap.put(markerId, alleles);
		}
		inputMapBR.close();
		fr.close();
		return origMarkerIdSetMap;
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

package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Binary;
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
public class MetadataLoaderPlinkBinary implements MetadataLoader {

	private String bimPath;
	private String strand;
	private int studyId;

	private enum Bases {

		A, C, T, G;
	}
	private static String tabulator = cNetCDF.Defaults.TMP_SEPARATOR;

	public MetadataLoaderPlinkBinary(String _bimPath, String _strand, int _studyId) throws FileNotFoundException {

		bimPath = _bimPath;
		studyId = _studyId;
		strand = _strand;

	}

	// ACCESSORS
	public Map<String, Object> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAndSortBimFile(bimPath); // chr, markerId, genetic distance, position

		org.gwaspi.global.Utils.sysoutStart("initilaizing marker info");
		System.out.println(Text.All.processing);

		Map<String, Object> markerMetadataLHM = new LinkedHashMap<String, Object>();
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

			markerMetadataLHM.put(keyValues[2], markerInfo);
		}

		String description = "Generated sorted MarkerIdSet LHM sorted by chromosome and position";
		return markerMetadataLHM;
	}

	public static SortedMap<String, String> parseAndSortBimFile(String path) throws IOException {

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
		Map<String, Object> origMarkerIdSetLHM = new LinkedHashMap<String, Object>();

		String l;


		while ((l = inputMapBR.readLine()) != null) {
			String[] alleles = new String[2];
			String[] mapVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = mapVals[Plink_Binary.bim_markerId].trim();
			alleles[0] = mapVals[Plink_Binary.bim_allele1].trim();
			alleles[1] = mapVals[Plink_Binary.bim_allele2].trim();
			origMarkerIdSetLHM.put(markerId, alleles);
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
}

package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Binary;
import org.gwaspi.constants.cNetCDF;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MetadataLoaderPlinkBinary {

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
	public LinkedHashMap getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		TreeMap tempTM = parseAndSortBimFile(bimPath); // chr, markerId, genetic distance, position

		org.gwaspi.global.Utils.sysoutStart("initilaizing marker info");
		System.out.println(org.gwaspi.global.Text.All.processing);

		LinkedHashMap markerMetadataLHM = new LinkedHashMap();
		for (Iterator it = tempTM.keySet().iterator(); it.hasNext();) {
			String key = it.next().toString();
			//chr;pos;markerId
			String[] keyValues = key.split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos;
			try {
				pos = Integer.parseInt(keyValues[1]);
			} catch (Exception ex) {
				pos = 0;
			}

			//rsId
			String valValues = tempTM.get(key).toString();
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

	public static TreeMap parseAndSortBimFile(String path) throws FileNotFoundException, IOException {

		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		TreeMap sortedMetadataTM = new TreeMap(new ComparatorChrAutPosMarkerIdAsc());

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

	public LinkedHashMap parseOrigBimFile(String path) throws FileNotFoundException, IOException {
		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		LinkedHashMap origMarkerIdSetLHM = new LinkedHashMap();

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

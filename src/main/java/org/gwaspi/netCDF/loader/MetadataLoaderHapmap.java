package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.HapmapGT_Standard;
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

/* Imports Hapmap genotype files as found on
 * http://hapmap.ncbi.nlm.nih.gov/downloads/genotypes/?N=D
 */

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MetadataLoaderHapmap implements MetadataLoader {

	private String hapmapPath;
	private String format;
	private int studyId;

	private enum Bases {

		A, C, T, G;
	}
	private static String tabulator = cNetCDF.Defaults.TMP_SEPARATOR;

	public MetadataLoaderHapmap(String _hapmapPath, String _format, int _studyId) throws FileNotFoundException {

		hapmapPath = _hapmapPath;
		studyId = _studyId;
		format = _format;

	}

	// ACCESSORS
	public Map<String, Object> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAnnotationBRFile(hapmapPath); // rsId, alleles [A/T], chr, pos, strand, genome_build, center, protLSID, assayLSID, panelLSID, QC_code, ensue GTs by SampleId

		org.gwaspi.global.Utils.sysoutStart("initilaizing marker info");
		System.out.println(Text.All.processing);

		Map<String, Object> markerMetadataLHM = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR); // chr;pos;markerId
			String[] valValues = entry.getValue().split(cNetCDF.Defaults.TMP_SEPARATOR);  //rsId;strand;alleles
			int pos;
			try {
				pos = Integer.parseInt(keyValues[1]);
			} catch (Exception ex) {
				pos = 0;
			}

			Object[] markerInfo = new Object[6];
			markerInfo[0] = keyValues[2]; // 0 => markerid
			markerInfo[1] = valValues[0]; // 1 => rsId
			markerInfo[2] = fixChrData(keyValues[0]); // 2 => chr
			markerInfo[3] = pos; // 3 => pos
			markerInfo[4] = valValues[1]; // 4 => strand
			markerInfo[5] = valValues[2]; // 5 => alleles

			markerMetadataLHM.put(keyValues[2], markerInfo);
		}

		String description = "Generated sorted MarkerIdSet LHM sorted by chromosome and position";
		MetadataLoaderPlink.logAsWhole(startTime, hapmapPath, description, studyId);
		return markerMetadataLHM;
	}

	public static SortedMap<String, String> parseAnnotationBRFile(String path) throws IOException {
		FileReader fr = new FileReader(path);
		BufferedReader inputAnnotationBr = new BufferedReader(fr);
		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());

		String header = inputAnnotationBr.readLine();

		String l;
		int count = 0;
		while ((l = inputAnnotationBr.readLine()) != null) {

			String[] hapmapVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String alleles = hapmapVals[HapmapGT_Standard.alleles].replace("/", "");

			// chr;pos;markerId
			String chr = hapmapVals[HapmapGT_Standard.chr];
			if (chr.length() > 3) {
				chr = chr.substring(3);
			} // Probably contains "chr" in front of number
			StringBuilder sbKey = new StringBuilder(chr); // 0 => chr
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(hapmapVals[HapmapGT_Standard.pos]); // 1 => pos
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(hapmapVals[HapmapGT_Standard.rsId]); // 2 => markerId

			//rsId;strand;alleles
			StringBuilder sbVal = new StringBuilder(hapmapVals[HapmapGT_Standard.rsId]); // 0 => markerId = rsId
			sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbVal.append(hapmapVals[HapmapGT_Standard.strand]); // 1 => strand
			sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbVal.append(alleles); // 2 => alleles


			sortedMetadataTM.put(sbKey.toString(), sbVal.toString());

			count++;
			if (count == 1) {
				System.out.println(Text.All.processing);
			} else if (count % 100000 == 0) {
				System.out.println("Parsed annotation lines: " + count);
			}
		}
		System.out.println("Parsed annotation lines: " + count);
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
}

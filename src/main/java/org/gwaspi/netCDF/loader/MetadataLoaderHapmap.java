package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.HapmapGT_Standard;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final Logger log
			= LoggerFactory.getLogger(MetadataLoaderHapmap.class);

	private String hapmapPath;
	private int studyId;
	private ImportFormat format;

	public MetadataLoaderHapmap(String hapmapPath, ImportFormat format, int studyId) {

		this.hapmapPath = hapmapPath;
		this.studyId = studyId;
		this.format = format;
	}

	public Map<String, Object> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAnnotationBRFile(); // rsId, alleles [A/T], chr, pos, strand, genome_build, center, protLSID, assayLSID, panelLSID, QC_code, ensue GTs by SampleId

		org.gwaspi.global.Utils.sysoutStart("initilaizing Marker info");
		log.info(Text.All.processing);

		Map<String, Object> markerMetadataMap = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR); // chr;pos;markerId
			String[] valValues = entry.getValue().split(cNetCDF.Defaults.TMP_SEPARATOR);  // rsId;strand;alleles
			int pos;
			try {
				pos = Integer.parseInt(keyValues[1]);
			} catch (Exception ex) {
				pos = 0;
				log.warn(null, ex);
			}

			Object[] markerInfo = new Object[6];
			markerInfo[0] = keyValues[2]; // 0 => markerid
			markerInfo[1] = valValues[0]; // 1 => rsId
			markerInfo[2] = MetadataLoaderBeagle.fixChrData(keyValues[0]); // 2 => chr
			markerInfo[3] = pos; // 3 => pos
			markerInfo[4] = valValues[1]; // 4 => strand
			markerInfo[5] = valValues[2]; // 5 => alleles

			markerMetadataMap.put(keyValues[2], markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		MetadataLoaderPlink.logAsWhole(startTime, hapmapPath, description, studyId);
		return markerMetadataMap;
	}

	private SortedMap<String, String> parseAnnotationBRFile() throws IOException {
		FileReader fr = new FileReader(hapmapPath);
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

			// rsId;strand;alleles
			StringBuilder sbVal = new StringBuilder(hapmapVals[HapmapGT_Standard.rsId]); // 0 => markerId = rsId
			sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbVal.append(hapmapVals[HapmapGT_Standard.strand]); // 1 => strand
			sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbVal.append(alleles); // 2 => alleles


			sortedMetadataTM.put(sbKey.toString(), sbVal.toString());

			count++;

			if (count == 1) {
				log.info(Text.All.processing);
			} else if (count % 100000 == 0) {
				log.info("Parsed annotation lines: {}", count);
			}
		}
		log.info("Parsed annotation lines: {}", count);

		inputAnnotationBr.close();

		return sortedMetadataTM;
	}
}

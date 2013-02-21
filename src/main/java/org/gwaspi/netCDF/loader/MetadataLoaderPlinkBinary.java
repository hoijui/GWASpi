package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Binary;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MetadataLoaderPlinkBinary implements MetadataLoader {

	private final Logger log
			= LoggerFactory.getLogger(MetadataLoaderPlinkBinary.class);

	private String bimPath;
	private StrandType strand;
	private int studyId;

	public MetadataLoaderPlinkBinary(String bimPath, StrandType strand, int studyId) {

		this.bimPath = bimPath;
		this.studyId = studyId;
		this.strand = strand;
	}

	@Override
	public Map<MarkerKey, Object> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAndSortBimFile(); // chr, markerId, genetic distance, position

		org.gwaspi.global.Utils.sysoutStart("initilaizing Marker info");
		log.info(Text.All.processing);

		Map<MarkerKey, Object> markerMetadataMap = new LinkedHashMap<MarkerKey, Object>();
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			// chr;pos;markerId
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos;
			try {
				pos = Integer.parseInt(keyValues[1]);
			} catch (Exception ex) {
				pos = 0;
				log.warn(null, ex);
			}

			// rsId
			String valValues = entry.getValue();
//			values = fixPlusAlleles(values);

			Object[] markerInfo = new Object[5];
			markerInfo[0] = keyValues[2]; // 0 => markerid
			String rsId = "";
			if (keyValues[2].startsWith("rs")) {
				rsId = keyValues[2];
			}
			markerInfo[1] = rsId; // 1 => rsId
			markerInfo[2] = MetadataLoaderBeagle.fixChrData(keyValues[0]); // 2 => chr
			markerInfo[3] = pos; // 3 => pos
			markerInfo[4] = valValues; // 4 => alleles

			markerMetadataMap.put(MarkerKey.valueOf(keyValues[2]), markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		return markerMetadataMap;
	}

	private SortedMap<String, String> parseAndSortBimFile() throws IOException {

		FileReader fr = new FileReader(bimPath);
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
			StringBuilder sbVal = new StringBuilder(); // 0 => markerid
			sbVal.append(bimVals[Plink_Binary.bim_allele1].trim());
			sbVal.append(bimVals[Plink_Binary.bim_allele2].trim());

			sortedMetadataTM.put(sbKey.toString(), sbVal.toString());

			count++;

			if (count == 1) {
				log.info(Text.All.processing);
			} else if (count % 100000 == 0) {
				log.info("Parsed annotation lines: {}", count);
			}
		}
		log.info("Parsed annotation lines: {}", count);

		inputMapBR.close();

		return sortedMetadataTM;
	}

	public Map<SampleKey, Object> parseOrigBimFile(String path) throws IOException {
		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		Map<SampleKey, Object> origMarkerIdSetMap = new LinkedHashMap<SampleKey, Object>();

		String l;
		while ((l = inputMapBR.readLine()) != null) {
			String[] alleles = new String[2];
			String[] mapVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = mapVals[Plink_Binary.bim_markerId].trim();
			alleles[0] = mapVals[Plink_Binary.bim_allele1].trim();
			alleles[1] = mapVals[Plink_Binary.bim_allele2].trim();
			origMarkerIdSetMap.put(SampleKey.valueOf(markerId), alleles);
		}

		inputMapBR.close();

		return origMarkerIdSetMap;
	}
}

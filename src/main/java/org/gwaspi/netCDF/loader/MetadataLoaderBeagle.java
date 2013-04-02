package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Beagle_Standard;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MetadataLoaderBeagle implements MetadataLoader {

	private final Logger log
			= LoggerFactory.getLogger(MetadataLoaderBeagle.class);

	private String markerFilePath;
	private String chr;
	private StrandType strand;
	private int studyId;

	public MetadataLoaderBeagle(String mapPath, String chr, StrandType strand, int studyId) {

		this.markerFilePath = mapPath;
		this.chr = chr;
		this.strand = strand;
		this.studyId = studyId;
	}

	@Override
	public Map<MarkerKey, MarkerMetadata> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAndSortMarkerFile(); // chr, markerId, genetic distance, position

		org.gwaspi.global.Utils.sysoutStart("initilaizing Marker info");
		log.info(Text.All.processing);

		Map<MarkerKey, MarkerMetadata> markerMetadata = new LinkedHashMap<MarkerKey, MarkerMetadata>();
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

			// rsId;alleles
			String[] valValues = entry.getValue().split(cNetCDF.Defaults.TMP_SEPARATOR);

			MarkerMetadata markerInfo = new MarkerMetadata(
					keyValues[2], // markerid
					valValues[0], // rsId
					fixChrData(keyValues[0]), // chr
					pos, // pos
					valValues[1]); // alleles

			markerMetadata.put(MarkerKey.valueOf(keyValues[2]), markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		MetadataLoaderPlink.logAsWhole(startTime, markerFilePath, description, studyId);
		return markerMetadata;
	}

	private SortedMap<String, String> parseAndSortMarkerFile() throws IOException {
		FileReader fr = new FileReader(markerFilePath);
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

			// chr;pos;markerId
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerVals[Beagle_Standard.pos].trim());
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerId);

			// rsId;alleles
			StringBuilder sbVal = new StringBuilder(rsId); // 0 => rsId
			sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbVal.append(markerVals[Beagle_Standard.allele1].trim()).append(markerVals[Beagle_Standard.allele2].trim()); //1 => alleles

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

	static String fixChrData(String chr) {

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

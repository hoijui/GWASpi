package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.HGDP1_Standard;
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
public class MetadataLoaderHGDP1 implements MetadataLoader {

	private final Logger log
			= LoggerFactory.getLogger(MetadataLoaderHGDP1.class);

	private String markerFilePath;
	private StrandType strand;
	private int studyId;

	public MetadataLoaderHGDP1(String mapPath, StrandType strand, int studyId) {

		this.markerFilePath = mapPath;
		this.studyId = studyId;
		this.strand = strand;
	}

	@Override
	public Map<MarkerKey, MarkerMetadata> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAndSortMarkerFile(); // chr, markerId, genetic distance, position

		org.gwaspi.global.Utils.sysoutStart("initilaizing Marker info");
		log.info(Text.All.processing);

		Map<MarkerKey, MarkerMetadata> markerMetadata = new LinkedHashMap<MarkerKey, MarkerMetadata>();
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			// chr; pos; markerId
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos;
			try {
				pos = Integer.parseInt(keyValues[1]);
			} catch (Exception ex) {
				pos = 0;
				log.warn(null, ex);
			}

			String valValues = entry.getValue();

			MarkerMetadata markerInfo = new MarkerMetadata(
					keyValues[2], // markerid
					valValues, // rsId
					MetadataLoaderBeagle.fixChrData(keyValues[0]), // chr
					pos); // pos

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
			String[]markerVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = markerVals[HGDP1_Standard.rsId].trim();
			String rsId = "";
			if (markerId.startsWith("rs")) {
				rsId = markerId;
			}

			// chr;pos;markerId
			StringBuilder sbKey = new StringBuilder(markerVals[HGDP1_Standard.chr]);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerVals[HGDP1_Standard.pos].trim());
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerId);

			// rsId;
			StringBuilder sbVal = new StringBuilder(rsId); // 0 => rsId

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
}

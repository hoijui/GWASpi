/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Standard;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataLoaderPlink implements MetadataLoader {

	private final Logger log
			= LoggerFactory.getLogger(MetadataLoaderPlink.class);

	private final String mapPath;
	private final StudyKey studyKey;

	public MetadataLoaderPlink(String mapPath, StudyKey studyKey) {

		this.mapPath = mapPath;
		this.studyKey = studyKey;
	}

	@Override
	public Map<MarkerKey, MarkerMetadata> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAndSortMapFile(); // chr, markerId, genetic distance, position

		org.gwaspi.global.Utils.sysoutStart("initilaizing Marker info");
		log.info(Text.All.processing);

		Map<MarkerKey, MarkerMetadata> markerMetadata = new LinkedHashMap<MarkerKey, MarkerMetadata>();
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			// "chr;pos;markerId"
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos;
			try {
				pos = Integer.parseInt(keyValues[1]);
			} catch (Exception ex) {
				pos = 0;
				log.warn(null, ex);
			}

			// rsId
			String[] valValues = new String[] {entry.getValue()};
			valValues = Utils.fixXYMTChrData(valValues, 0);
//			values = fixPlusAlleles(values);

			MarkerMetadata markerInfo = new MarkerMetadata(
					keyValues[2], // markerid
					valValues[0], // rsId
					MetadataLoaderBeagle.fixChrData(keyValues[0]), // chr
					pos); // pos

			markerMetadata.put(MarkerKey.valueOf(keyValues[2]), markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		logAsWhole(startTime, mapPath, description, studyKey.getId());
		return markerMetadata;
	}

	private SortedMap<String, String> parseAndSortMapFile() throws IOException {

		FileReader fr = new FileReader(mapPath);
		BufferedReader inputMapBR = new BufferedReader(fr);
		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());

		String l;
		int count = 0;
		while ((l = inputMapBR.readLine()) != null) {
			String[] markerVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = markerVals[Plink_Standard.map_markerId].trim();
			String rsId = "";
			if (markerId.startsWith("rs")) {
				rsId = markerId;
			}
			String chr = markerVals[Plink_Standard.map_chr].trim();
			String pos = markerVals[Plink_Standard.map_pos].trim();

			// "chr;pos;markerId"
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(pos);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerId);

			// rsId
			StringBuilder sbVal = new StringBuilder(rsId); // 0 => markerid

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

	public static Map<MarkerKey, byte[]> parseOrigMapFile(String path) throws IOException {
		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		Map<MarkerKey, byte[]> origMarkerIdSetMap = new LinkedHashMap<MarkerKey, byte[]>();

		String l;
		while ((l = inputMapBR.readLine()) != null) {
			String[] mapVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = mapVals[Plink_Standard.map_markerId].trim();
			origMarkerIdSetMap.put(MarkerKey.valueOf(markerId), cNetCDF.Defaults.DEFAULT_GT);
		}

		inputMapBR.close();

		return origMarkerIdSetMap;
	}

	static void logAsWhole(String startTime, String dirPath, String description, int studyId) throws IOException {
		// LOG OPERATION IN STUDY HISTORY
		StringBuilder operation = new StringBuilder("\nLoaded MAP metadata in path " + dirPath + ".\n");
		operation.append("Start Time: ").append(startTime).append("\n");
		operation.append("End Time: ").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append(".\n");
		operation.append("Description: ").append(description).append(".\n");
		org.gwaspi.global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
	}
}

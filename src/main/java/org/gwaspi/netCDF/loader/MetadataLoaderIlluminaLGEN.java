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
import org.gwaspi.constants.cImport.Annotation.Plink_LGEN;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataLoaderIlluminaLGEN implements MetadataLoader {

	private final Logger log
			= LoggerFactory.getLogger(MetadataLoaderIlluminaLGEN.class);

	private String mapPath;
	private int studyId;

	public MetadataLoaderIlluminaLGEN(String mapPath, int studyId) {

		this.mapPath = mapPath;
		this.studyId = studyId;
	}

	@Override
	public Map<MarkerKey, MarkerMetadata> getSortedMarkerSetWithMetaData() throws IOException {
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		SortedMap<String, String> tempTM = parseAndSortMapFile(); // chr, markerId, genetic distance, position

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

			// rsId
			String[] valValues = new String[]{entry.getValue()};
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
		MetadataLoaderPlink.logAsWhole(startTime, mapPath, description, studyId);
		return markerMetadata;
	}

	private SortedMap<String, String> parseAndSortMapFile() throws IOException {

		FileReader fr = new FileReader(mapPath);
		BufferedReader inputMapBR = new BufferedReader(fr);
		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());

		String header;
		boolean gotHeader = false;
		while (!gotHeader && inputMapBR.ready()) {
			header = inputMapBR.readLine();
			if (header.startsWith("[Data]")) {
				/*header = */inputMapBR.readLine(); // Get next line which is real header
				gotHeader = true;
			}
		}


		String l;
		int count = 0;
		while ((l = inputMapBR.readLine()) != null) {
			String[] mapVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = mapVals[Plink_LGEN.map_markerId].trim();
			String rsId = "";
			if (markerId.startsWith("rs")) {
				rsId = markerId;
			}
			String chr = mapVals[Plink_LGEN.map_chr].trim();

			// chr;pos;markerId
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(mapVals[Plink_LGEN.map_pos].trim());
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerId);

			// rsId
			StringBuilder sbVal = new StringBuilder(rsId); // 0 => markerid

			sortedMetadataTM.put(sbKey.toString(), sbVal.toString());

			count++;

			if (count == 1) {
				log.info(Text.All.processing);
			} else if (count % 500000 == 0) {
				log.info("Parsed annotation lines: {}", count);
			}
		}
		log.info("Parsed annotation lines: {}", count);

		inputMapBR.close();

		return sortedMetadataTM;
	}
}

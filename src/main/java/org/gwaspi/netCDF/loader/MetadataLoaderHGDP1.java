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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.HGDP1_Standard;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataLoaderHGDP1 implements MetadataLoader {

	private final Logger log
			= LoggerFactory.getLogger(MetadataLoaderHGDP1.class);

	public MetadataLoaderHGDP1() {
	}

	@Override
	public boolean isHasStrandInfo() {
		return false;
	}

	@Override
	public StrandType getFixedStrandFlag() {
		return null;
	}

	@Override
	public void loadMarkers(DataSetDestination samplesReceiver, GenotypesLoadDescription loadDescription) throws IOException {
		loadMarkers(
				samplesReceiver,
				loadDescription.getAnnotationFilePath(),
				loadDescription.getStudyKey());
	}

	private void loadMarkers(DataSetDestination samplesReceiver, String markerFilePath, StudyKey studyKey) throws IOException {

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		log.info("read and pre-parse raw marker info");
		// chr, markerId, genetic distance, position
		SortedMap<String, String> tempTM = parseAndSortMarkerFile(markerFilePath);

		log.info("parse and fixup raw marker info");
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			// "chr;pos;markerId"
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos = MetadataLoaderPlink.fixPosIfRequired(keyValues[1]);

			String valValues = entry.getValue();

			MarkerMetadata markerInfo = new MarkerMetadata(
					keyValues[2], // markerid
					valValues, // rsId
					MetadataLoaderBeagle.fixChrData(keyValues[0]), // chr
					pos); // pos

			samplesReceiver.addMarkerMetadata(markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		MetadataLoaderPlink.logAsWhole(log, startTime, markerFilePath, description, studyKey.getId());
	}

	/**
	 * Parses the marker file.
	 */
	private SortedMap<String, String> parseAndSortMarkerFile(String markerFilePath) throws IOException {

		FileReader fr = new FileReader(markerFilePath);
		BufferedReader inputMapBR = new BufferedReader(fr);
		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());

		String l;
		int count = 0;
		while ((l = inputMapBR.readLine()) != null) {
			String[] markerVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String markerId = markerVals[HGDP1_Standard.rsId].trim();
			String rsId = "";
			if (markerId.startsWith("rs")) {
				rsId = markerId;
			}
			String chr = markerVals[HGDP1_Standard.chr].trim();
			String pos = markerVals[HGDP1_Standard.pos].trim();

			// "chr;pos;markerId"
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(pos);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerId);

			// rsId
			StringBuilder sbVal = new StringBuilder(rsId); // 0 => rsId

			sortedMetadataTM.put(sbKey.toString(), sbVal.toString());

			count++;

			if ((count == 1) || (count % 100000 == 0)) {
				log.info("read and pre-parse marker metadat from file(s); lines: {}", count);
			}
		}
		log.info("read and pre-parse marker metadat from file(s); lines: {}", count);

		inputMapBR.close();

		return sortedMetadataTM;
	}
}

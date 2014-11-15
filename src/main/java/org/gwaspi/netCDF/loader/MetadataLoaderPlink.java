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
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataLoaderPlink implements MetadataLoader {

	private final Logger log
			= LoggerFactory.getLogger(MetadataLoaderPlink.class);

	public MetadataLoaderPlink() {
	}

	@Override
	public boolean isHasStrandInfo() {
		return false;
	}

	@Override
	public StrandType getFixedStrandFlag() {
		return null;
	}

//	@Override
//	public Iterator<MarkerMetadata> iterator() {
//		return new PlinkFlatMarkerParseIterator();
//	}
//
//	private static class PlinkFlatMarkerParseIterator implements Iterator<MarkerMetadata> {
//
//		public boolean hasNext() {
//			throw new UnsupportedOperationException("Not supported yet.");
//		}
//
//		public MarkerMetadata next() {
//			throw new UnsupportedOperationException("Not supported yet.");
//		}
//
//		public void remove() {
//			throw new UnsupportedOperationException();
//		}
//
//	}

	@Override
	public void loadMarkers(DataSetDestination samplesReceiver, GenotypesLoadDescription loadDescription) throws IOException {
		loadMarkers(
				samplesReceiver,
				loadDescription.getGtDirPath(),
				loadDescription.getStudyKey());
	}

	private void loadMarkers(DataSetDestination samplesReceiver, String mapPath, StudyKey studyKey) throws IOException {

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		log.info("read and pre-parse raw marker info");
		// chr, markerId, genetic distance, position
		SortedMap<String, String> tempTM = parseAndSortMapFile(mapPath);

		log.info("parse and fixup raw marker info");
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			// "chr;pos;markerId"
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos = fixPosIfRequired(keyValues[1]);

			// rsId
			String[] valValues = new String[] {entry.getValue()};
			valValues = Utils.fixXYMTChrData(valValues, 0);
//			values = fixPlusAlleles(values);

			MarkerMetadata markerInfo = new MarkerMetadata(
					keyValues[2], // markerid
					valValues[0], // rsId
					MetadataLoaderBeagle.fixChrData(keyValues[0]), // chr
					pos); // pos

			samplesReceiver.addMarkerMetadata(markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		logAsWhole(log, startTime, mapPath, description, studyKey.getId());
	}

	private SortedMap<String, String> parseAndSortMapFile(String mapPath) throws IOException {

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

			if ((count == 1) || (count % 100000 == 0)) {
				log.info("read and pre-parse marker metadat from file(s); lines: {}", count);
			}
		}
		log.info("read and pre-parse marker metadat from file(s); lines: {}", count);

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

	static int fixPosIfRequired(String posStr) throws IOException {

		int pos;
		try {
			pos = Integer.parseInt(posStr);
		} catch (Exception ex) {
			pos = 0;
//			log.warn("Bad marker position " + posStr + ", using " + pos, ex);
		}

		return pos;
	}

	/**
	 * LOG OPERATION IN STUDY HISTORY
	 * @param log
	 * @param startTime
	 * @param dirPath
	 * @param description
	 * @param studyId
	 * @throws IOException
	 */
	static void logAsWhole(Logger log, String startTime, String dirPath, String description, int studyId) throws IOException {

		if (log.isDebugEnabled()) {
			final StringBuilder operation = new StringBuilder("\nLoaded MAP metadata in path " + dirPath + ".\n");
			operation.append("Start Time: ").append(startTime).append("\n");
			operation.append("End Time: ").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append(".\n");
			operation.append("Description: ").append(description).append(".\n");
			org.gwaspi.global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
			log.debug("Study {}:\n{}", studyId, operation.toString());
		}
	}
}

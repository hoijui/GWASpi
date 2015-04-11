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
import org.gwaspi.constants.ImportConstants;
import org.gwaspi.constants.ImportConstants.Annotation.Plink_Binary;
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.StrandType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataLoaderPlinkBinary implements MetadataLoader {

	private final Logger log = LoggerFactory.getLogger(MetadataLoaderPlinkBinary.class);

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

	private void loadMarkers(DataSetDestination samplesReceiver, String bimPath, StudyKey studyKey) throws IOException {

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		log.info("read and pre-parse raw marker info");
		// chr, markerId, genetic distance, position
		SortedMap<String, String> tempTM = parseAndSortBimFile(bimPath);

		log.info("parse and fixup raw marker info");
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			// "chr;pos;markerId"
			String[] keyValues = entry.getKey().split(NetCDFConstants.Defaults.TMP_SEPARATOR);
			int pos = MetadataLoaderPlink.fixPosIfRequired(keyValues[1]);

			// alleles (bases dictionary)
			String valValues = entry.getValue();
//			values = fixPlusAlleles(values);

			String rsId = "";
			if (keyValues[2].startsWith("rs")) {
				rsId = keyValues[2];
			}
			MarkerMetadata markerInfo = new MarkerMetadata(
					keyValues[2], // markerid
					rsId, // rsId
					MetadataLoaderBeagle.fixChrData(keyValues[0]), // chr
					pos, // pos
					valValues); // alleles

			samplesReceiver.addMarkerMetadata(markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		MetadataLoaderPlink.logAsWhole(log, startTime, bimPath, description, studyKey.getId());
	}

	private SortedMap<String, String> parseAndSortBimFile(String bimPath) throws IOException {

		FileReader fr = new FileReader(bimPath);
		BufferedReader inputMapBR = new BufferedReader(fr);
		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());

		String l;
		int count = 0;
		while ((l = inputMapBR.readLine()) != null) {
			String[] markerVals = l.split(ImportConstants.Separators.separators_SpaceTab_rgxp);
			String markerId = markerVals[Plink_Binary.bim_markerId].trim();
			String rsId = "";
			if (markerId.startsWith("rs")) {
				rsId = markerId;
			}
			String chr = markerVals[Plink_Binary.bim_chr].trim();
			String pos = markerVals[Plink_Binary.bim_pos].trim();

			// "chr;pos;markerId"
			final StringBuilder sbKey = new StringBuilder();
			sbKey
					.append(chr)
					.append(NetCDFConstants.Defaults.TMP_SEPARATOR)
					.append(pos)
					.append(NetCDFConstants.Defaults.TMP_SEPARATOR)
					.append(markerId);

			// alleles (bases dictionary)
			final StringBuilder sbVal = new StringBuilder();
			sbVal
					.append(markerVals[Plink_Binary.bim_allele1].trim())
					.append(markerVals[Plink_Binary.bim_allele2].trim());

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

	public static Map<MarkerKey, String[]> parseOrigBimFile(String path, StudyKey studyKey) throws IOException {

		FileReader fr = new FileReader(path);
		BufferedReader inputMapBR = new BufferedReader(fr);
		Map<MarkerKey, String[]> origMarkerIdSetMap = new LinkedHashMap<MarkerKey, String[]>();

		String l;
		while ((l = inputMapBR.readLine()) != null) {
			String[] alleles = new String[2];
			String[] mapVals = l.split(ImportConstants.Separators.separators_SpaceTab_rgxp);
			String markerId = mapVals[Plink_Binary.bim_markerId].trim();
			alleles[0] = mapVals[Plink_Binary.bim_allele1].trim();
			alleles[1] = mapVals[Plink_Binary.bim_allele2].trim();
			origMarkerIdSetMap.put(new MarkerKey(markerId), alleles);
		}

		inputMapBR.close();

		return origMarkerIdSetMap;
	}
}

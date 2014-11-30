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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.constants.ImportConstants;
import org.gwaspi.constants.ImportConstants.Annotation.HapmapGT_Standard;
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.StrandType;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Imports Hapmap genotype files as found on
 * http://hapmap.ncbi.nlm.nih.gov/downloads/genotypes/?N=D
 */

public class MetadataLoaderHapmap implements MetadataLoader {

	private final Logger log
			= LoggerFactory.getLogger(MetadataLoaderHapmap.class);

	public MetadataLoaderHapmap() {
	}

	@Override
	public boolean isHasStrandInfo() {
		return true;
	}

	@Override
	public StrandType getFixedStrandFlag() {
		return null;
	}

	@Override
	public void loadMarkers(DataSetDestination samplesReceiver, GenotypesLoadDescription loadDescription) throws IOException {

		File[] gtFilesToImport = LoadGTFromHapmapFiles.extractGTFilesToImport(loadDescription);
		for (int i = 0; i < gtFilesToImport.length; i++) {
			loadMarkers(
					samplesReceiver,
					gtFilesToImport[i].getPath(),
					loadDescription.getFormat(),
					loadDescription.getStudyKey());
		}
	}

	private void loadMarkers(DataSetDestination samplesReceiver, String hapmapPath, ImportFormat format, StudyKey studyKey) throws IOException {

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		log.info("read and pre-parse raw marker info");
		// rsId, alleles [A/T], chr, pos, strand, genome_build, center, protLSID, assayLSID, panelLSID, QC_code, ensue GTs by SampleId
		SortedMap<String, String> tempTM = parseAnnotationBRFile(hapmapPath);

		log.info("parse and fixup raw marker info");
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			String[] keyValues = entry.getKey().split(NetCDFConstants.Defaults.TMP_SEPARATOR); // chr;pos;markerId
			String[] valValues = entry.getValue().split(NetCDFConstants.Defaults.TMP_SEPARATOR);  // rsId;strand;alleles
			int pos = MetadataLoaderPlink.fixPosIfRequired(keyValues[1]);

			MarkerMetadata markerInfo = new MarkerMetadata(
					keyValues[2], // markerid
					valValues[0], // rsId
					MetadataLoaderBeagle.fixChrData(keyValues[0]), // chr
					pos, // pos
					valValues[2], // alleles
					valValues[1]); // strand

			samplesReceiver.addMarkerMetadata(markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		MetadataLoaderPlink.logAsWhole(log, startTime, hapmapPath, description, studyKey.getId());
	}

	private SortedMap<String, String> parseAnnotationBRFile(String hapmapPath) throws IOException {

		FileReader fr = new FileReader(hapmapPath);
		BufferedReader inputAnnotationBr = new BufferedReader(fr);
		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());

		String header = inputAnnotationBr.readLine();

		String l;
		int count = 0;
		while ((l = inputAnnotationBr.readLine()) != null) {
			String[] hapmapVals = l.split(ImportConstants.Separators.separators_SpaceTab_rgxp);
			String alleles = hapmapVals[HapmapGT_Standard.alleles].replace("/", "");

			// "chr;pos;markerId"
			String chr = hapmapVals[HapmapGT_Standard.chr];
			if (chr.length() > 3) {
				chr = chr.substring(3);
			} // Probably contains "chr" in front of number
			String pos = hapmapVals[HapmapGT_Standard.pos];
			String rsId = hapmapVals[HapmapGT_Standard.rsId];

			StringBuilder sbKey = new StringBuilder(chr); // 0 => chr
			sbKey.append(NetCDFConstants.Defaults.TMP_SEPARATOR);
			sbKey.append(pos); // 1 => pos
			sbKey.append(NetCDFConstants.Defaults.TMP_SEPARATOR);
			sbKey.append(rsId); // 2 => markerId

			// rsId;strand;alleles
			StringBuilder sbVal = new StringBuilder(hapmapVals[HapmapGT_Standard.rsId]); // 0 => markerId = rsId
			sbVal.append(NetCDFConstants.Defaults.TMP_SEPARATOR);
			sbVal.append(hapmapVals[HapmapGT_Standard.strand]); // 1 => strand
			sbVal.append(NetCDFConstants.Defaults.TMP_SEPARATOR);
			sbVal.append(alleles); // 2 => alleles

			sortedMetadataTM.put(sbKey.toString(), sbVal.toString());

			count++;

			if ((count == 1) || (count % 100000 == 0)) {
				log.info("read and pre-parse marker metadat from file(s); lines: {}", count);
			}
		}
		log.info("read and pre-parse marker metadat from file(s); lines: {}", count);

		inputAnnotationBr.close();

		return sortedMetadataTM;
	}
}

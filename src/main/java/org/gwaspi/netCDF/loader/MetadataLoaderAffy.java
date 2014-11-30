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
import org.gwaspi.constants.ImportConstants.Annotation.Affymetrix_GenomeWide6;
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataLoaderAffy implements MetadataLoader {

	private final Logger log
			= LoggerFactory.getLogger(MetadataLoaderAffy.class);

	/** Duplicate SNPs to be removed */
	private static final SNPBlacklist snpBlackList = new SNPBlacklist();

	public MetadataLoaderAffy() {
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
		loadMarkers(
				samplesReceiver,
				loadDescription.getAnnotationFilePath(),
				loadDescription.getFormat(),
				loadDescription.getStudyKey());
	}

	private void loadMarkers(DataSetDestination samplesReceiver, String annotationPath, ImportFormat format, StudyKey studyKey) throws IOException {

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		log.info("read and pre-parse raw marker info");
		// affyId, rsId,chr,pseudo-autosomal,pos, strand, alleles, plus-alleles
		SortedMap<String, String> tempTM = parseAnnotationBRFile(annotationPath);

		log.info("parse and fixup raw marker info");
		for (Map.Entry<String, String> entry : tempTM.entrySet()) {
			// keyValues = chr;pseudo-autosomal1;pseudo-autosomal2;pos;markerId"
			String[] keyValues = entry.getKey().split(cNetCDF.Defaults.TMP_SEPARATOR);
			int pos = MetadataLoaderPlink.fixPosIfRequired(keyValues[3]);

			// valValues = rsId;strand;alleles
			String[] valValues = entry.getValue().split(cNetCDF.Defaults.TMP_SEPARATOR);
			valValues = fixRsId(keyValues, valValues);
			keyValues = fixChrData(keyValues);

			MarkerMetadata markerInfo = new MarkerMetadata(
					keyValues[4], // affyid
					valValues[0], // rsId
					keyValues[0], // chr
					pos, // pos
					valValues[2], // alleles
					valValues[1]); // strand
//					keyValues[1], // pseudo-autosomal1
//					keyValues[2], // pseudo-autosomal2

			samplesReceiver.addMarkerMetadata(markerInfo);
		}

		String description = "Generated sorted MarkerIdSet Map sorted by chromosome and position";
		MetadataLoaderPlink.logAsWhole(log, startTime, annotationPath, description, studyKey.getId());
	}

	private SortedMap<String, String> parseAnnotationBRFile(String annotationPath) throws IOException {
		FileReader fr = new FileReader(annotationPath);
		BufferedReader inputAnnotationBr = new BufferedReader(fr);
		SortedMap<String, String> sortedMetadataTM = new TreeMap<String, String>(new ComparatorChrAutPosMarkerIdAsc());

		String header = "";
		while ((header != null) && !header.startsWith("\"Probe Set ID")) {
			header = inputAnnotationBr.readLine();
		}

		String l;
		int count = 0;
		while ((l = inputAnnotationBr.readLine()) != null) {
			String[] affy6Vals = l.split("\",\"");
			Affymetrix_GenomeWide6.init(annotationPath);
			String affyId = affy6Vals[Affymetrix_GenomeWide6.markerId].replace("\"", "");
			String chr = affy6Vals[Affymetrix_GenomeWide6.chr].replace("\"", "");
			String inFinalList = affy6Vals[Affymetrix_GenomeWide6.in_final_list].replace("\"", "");

			if (!affyId.startsWith("AFFX-") && !inFinalList.equals("NO") && !chr.equals("---") && !snpBlackList.getAffyIdBlacklist().contains(affyId)) {
				// chr;pseudo-autosomal1;pseudo-autosomal2;pos;markerId"
				StringBuilder sbKey = new StringBuilder(chr); // 0 => chr
				sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
				sbKey.append(affy6Vals[Affymetrix_GenomeWide6.pseudo_a1]); // 1 => pseudo-autosomal1
				sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
				sbKey.append(affy6Vals[Affymetrix_GenomeWide6.pseudo_a2]); // 2 => pseudo-autosomal2
				sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
				sbKey.append(affy6Vals[Affymetrix_GenomeWide6.pos]); // 3 => pos
				sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
				sbKey.append(affyId); // 4 => markerId

				// rsId;strand;alleles
				StringBuilder sbVal = new StringBuilder(affy6Vals[Affymetrix_GenomeWide6.rsId]); // 0 => rsId
				sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
				sbVal.append(affy6Vals[Affymetrix_GenomeWide6.strand]); // 1 => strand
				sbVal.append(cNetCDF.Defaults.TMP_SEPARATOR);
				sbVal.append(affy6Vals[Affymetrix_GenomeWide6.alleleA]);
				sbVal.append(affy6Vals[Affymetrix_GenomeWide6.alleleB]); // 2 => alleles

				sortedMetadataTM.put(sbKey.toString(), sbVal.toString());
			}
			count++;

			if ((count == 1) || (count % 100000 == 0)) {
				log.info("read and pre-parse marker metadat from file(s); lines: {}", count);
			}
		}
		log.info("read and pre-parse marker metadat from file(s); lines: {}", count);

		inputAnnotationBr.close();

		return sortedMetadataTM;
	}

	private static String[] fixRsId(String[] keyValues, String[] valValues) throws IOException {
		// CHECK IF RSID EXISTS, USE AFFYID IF NOT

		// valValues rsId;strand;alleles
		// keyValues chr;pseudo-autosomal1;pseudo-autosomal2;pos;markerId
		if (valValues[0].equals("---")) {
			valValues[0] = keyValues[4];
		}
		return valValues;
	}

	private static String[] fixChrData(String[] keyValues) throws IOException {
		// CHECK FOR PSEUDO-AUTOSOMAL FLAG => XY

		//keyValues chr;pseudo-autosomal1;pseudo-autosomal2;pos;markerId
		if (keyValues[1].equals("1")) {
			keyValues[0] = "XY";
		}
		if (keyValues[2].equals("1")) {
			keyValues[0] = "XY";
		}
		return keyValues;
	}
}

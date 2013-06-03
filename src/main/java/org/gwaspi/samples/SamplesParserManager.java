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

package org.gwaspi.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.GWASpi;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

public class SamplesParserManager {

	private static final Logger log
			= LoggerFactory.getLogger(SamplesParserManager.class);

	private static final Map<ImportFormat, SamplesParser> sampleParsers;

	static {
		sampleParsers = new EnumMap<ImportFormat, SamplesParser>(ImportFormat.class);
		sampleParsers.put(ImportFormat.Affymetrix_GenomeWide6, new AffymetrixSamplesParser());
		sampleParsers.put(ImportFormat.PLINK, new PlinkStandardSamplesParser());
		sampleParsers.put(ImportFormat.PLINK_Binary, new PlinkFAMSamplesParser());
		sampleParsers.put(ImportFormat.HAPMAP, new HapmapSamplesParser());
		sampleParsers.put(ImportFormat.BEAGLE, new BeagleSamplesParser());
		sampleParsers.put(ImportFormat.HGDP1, new HGDP1SamplesParser());
		sampleParsers.put(ImportFormat.Illumina_LGEN, new IlluminaLGENSamplesParser());
		sampleParsers.put(ImportFormat.GWASpi, new GwaspiSamplesParser());
		sampleParsers.put(ImportFormat.Sequenom, new SequenomSamplesParser());
	}

	private SamplesParserManager() {
	}

	//<editor-fold defaultstate="expanded" desc="DB SAMPLE INFO PROVIDERS">
	public static Set<SampleInfo.Affection> getDBAffectionStates(int matrixId) {
		Set<SampleInfo.Affection> resultHS = EnumSet.noneOf(SampleInfo.Affection.class);
		try {
			MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(matrixId);
			log.info("Getting Sample Affection info for: {}",
					rdMatrixMetadata.getMatrixFriendlyName());
//			NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
			SampleSet rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), matrixId);
			for (SampleKey key : rdSampleSet.getSampleKeys()) {
				SampleInfo sampleInfo = SampleInfoList.getSample(key);
				if (sampleInfo != null) {
					resultHS.add(sampleInfo.getAffection());
				}
			}
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return resultHS;
	}
	//</editor-fold>

	public static Collection<SampleInfo> scanSampleInfo(int studyId, ImportFormat importFormat, String genotypePath) throws IOException {
		return sampleParsers.get(importFormat).scanSampleInfo(studyId, genotypePath);
	}

	public static Set<SampleInfo.Affection> scanSampleInfoAffectionStates(String sampleInfoPath) throws IOException {
		Set<SampleInfo.Affection> resultHS = EnumSet.noneOf(SampleInfo.Affection.class);

		File sampleFile = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String header = inputBufferReader.readLine(); // ignore header block
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
			resultHS.add(SampleInfo.Affection.parse(cVals[GWASpi.affection]));
		}

		inputBufferReader.close();

		return resultHS;
	}
}

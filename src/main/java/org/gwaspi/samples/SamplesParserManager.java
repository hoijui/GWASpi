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
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.GWASpi;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.global.ExtractorList;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamplesParserManager {

	private static final Logger log
			= LoggerFactory.getLogger(SamplesParserManager.class);
	private static final boolean USE_DB = false;

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

	public static Set<Affection> collectAffectionStates(Collection<SampleInfo> sampleInfos) {

		Set<Affection> affectionStates = EnumSet.noneOf(Affection.class);

		for (SampleInfo sampleInfo : sampleInfos) {
			affectionStates.add(sampleInfo.getAffection());
		}

		return affectionStates;
	}

	public static Set<Affection> collectAffectionStates(final DataSetKey dataSetKey) {

		try {
			DataSetMetadata dataSetMetadata = MatricesList.getDataSetMetadata(dataSetKey);
			DataSetSource dataSetSource = MatrixFactory.generateDataSetSource(dataSetKey);
			if (USE_DB) {
				return collectAffectionStatesFromDB(dataSetSource, dataSetMetadata.getFriendlyName());
			} else {
				return collectAffectionStatesFromBackendStorage(dataSetSource, dataSetMetadata.getFriendlyName());
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return EnumSet.noneOf(Affection.class);
	}

	private static Set<Affection> collectAffectionStatesFromBackendStorage(
			final DataSetSource dataSetSource,
			String dataSourceFriendlyName)
			throws IOException
	{
		Set<Affection> affections = EnumSet.noneOf(Affection.class);

		log.info("Getting Sample Affection info from backend-storage for: {}",
				dataSourceFriendlyName);
		affections.addAll(new ExtractorList<SampleInfo, SampleInfo.Affection>(
				dataSetSource.getSamplesInfosSource(),
				SampleInfo.TO_AFFECTION));

		return affections;
	}

	private static Set<Affection> collectAffectionStatesFromDB(
			final DataSetSource dataSetSource,
			String dataSourceFriendlyName)
			throws IOException
	{
		Set<Affection> affections = EnumSet.noneOf(Affection.class);

		log.info("Getting Sample Affection info from DB for: {}",
				dataSourceFriendlyName);
		for (SampleKey key : dataSetSource.getSamplesKeysSource()) {
			SampleInfo sampleInfo = SampleInfoList.getSample(key);
			if (sampleInfo != null) {
				affections.add(sampleInfo.getAffection());
			}
		}

		return affections;
	}

	public static void scanSampleInfo(StudyKey studyKey, ImportFormat importFormat, String genotypePath, DataSetDestination samplesReceiver) throws Exception {
		sampleParsers.get(importFormat).scanSampleInfo(studyKey, genotypePath, samplesReceiver);
	}

	public static Set<Affection> scanSampleInfoAffectionStates(String sampleInfoPath) throws IOException {

		Set<Affection> result = EnumSet.noneOf(Affection.class);

		File sampleFile = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String header = inputBufferReader.readLine(); // ignore header block
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
			result.add(Affection.parse(cVals[GWASpi.affection]));
		}

		inputBufferReader.close();

		return result;
	}
}

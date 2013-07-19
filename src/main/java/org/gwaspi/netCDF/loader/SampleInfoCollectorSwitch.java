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
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.global.Text;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.StudyKey;
import org.gwaspi.samples.SamplesParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleInfoCollectorSwitch {

	private static final Logger log = LoggerFactory.getLogger(SampleInfoCollectorSwitch.class);

	private SampleInfoCollectorSwitch() {
	}

	private static void checkMissingSampleInfo(
			StudyKey studyKey,
			Collection<SampleInfo> dummySampleInfos,
			Collection<SampleInfo> sampleInfos)
	{
		for (SampleInfo dummySampleInfo : dummySampleInfos) {
			if (!sampleInfos.contains(dummySampleInfo)) {
				SampleInfo dummySampleInfoCopy = new SampleInfo(
						studyKey, dummySampleInfo.getSampleId());
				sampleInfos.add(dummySampleInfoCopy);
				log.warn(Text.Study.warnMissingSampleInfo);
				log.warn("SampleID: {}", dummySampleInfo.getSampleId());
			}
		}
	}

	private static boolean checkIsPlinkFAMFile(String sampleInfoPath)
			throws IOException
	{
		FileReader inputFileReader = new FileReader(new File(sampleInfoPath));
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);
		String header = inputBufferReader.readLine();
		inputBufferReader.close();
		String[] cVals = header.split(cImport.Separators.separators_CommaSpaceTab_rgxp);

		return (cVals.length == 6);
	}

	public static void collectSampleInfo(
			StudyKey studyKey,
			ImportFormat format,
			boolean dummySamples,
			String sampleInfoPath,
			String altSampleInfoPath1,
			String altSampleInfoPath2,
			SamplesReceiver samplesReceiver)
			throws Exception
	{
		switch (format) {
			case Affymetrix_GenomeWide6:
			case PLINK:
			case Illumina_LGEN:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					samplesReceiver.startLoadingDummySampleInfos();
					SamplesParserManager.scanSampleInfo(studyKey, format, altSampleInfoPath2, samplesReceiver);
					samplesReceiver.finishedLoadingDummySampleInfos();
				} else {
					samplesReceiver.startLoadingDummySampleInfos();
					SamplesParserManager.scanSampleInfo(studyKey, format, altSampleInfoPath2, samplesReceiver);
					samplesReceiver.finishedLoadingDummySampleInfos();

					samplesReceiver.startLoadingSampleInfos();
					SamplesParserManager.scanSampleInfo(studyKey, ImportFormat.GWASpi, sampleInfoPath, samplesReceiver);
					samplesReceiver.finishedLoadingSampleInfos();

					// NOTE this is done in DataSet, by using LinkedHashSet for the sampleInfos
//					checkMissingSampleInfo(studyKey, dummySamplesInfos, sampleInfos);
				}
				break;
			case PLINK_Binary:
				log.info(Text.Matrix.scanAffectionStandby);
				samplesReceiver.startLoadingSampleInfos();
				if (checkIsPlinkFAMFile(sampleInfoPath)) {
					SamplesParserManager.scanSampleInfo(studyKey, format, sampleInfoPath, samplesReceiver);
				} else {
					// It is a SampleInfo file
					SamplesParserManager.scanSampleInfo(studyKey, ImportFormat.GWASpi, sampleInfoPath, samplesReceiver);
				}
				samplesReceiver.finishedLoadingSampleInfos();
				break;
			case HAPMAP:
			case BEAGLE:
			case HGDP1:
				log.info(Text.Matrix.scanAffectionStandby);
				if (dummySamples) {
					samplesReceiver.startLoadingDummySampleInfos();
					SamplesParserManager.scanSampleInfo(studyKey, format, altSampleInfoPath1, samplesReceiver);
					samplesReceiver.finishedLoadingDummySampleInfos();
				} else {
					samplesReceiver.startLoadingDummySampleInfos();
					SamplesParserManager.scanSampleInfo(studyKey, format, altSampleInfoPath1, samplesReceiver);
					samplesReceiver.finishedLoadingDummySampleInfos();

					samplesReceiver.startLoadingSampleInfos();
					SamplesParserManager.scanSampleInfo(studyKey, ImportFormat.GWASpi, sampleInfoPath, samplesReceiver);
					samplesReceiver.finishedLoadingSampleInfos();

					// NOTE this is done in DataSet, by using LinkedHashSet for the sampleInfos
//					checkMissingSampleInfo(studyKey, dummySamplesInfos, sampleInfos);
				}
				break;
			case GWASpi:
				samplesReceiver.startLoadingSampleInfos();
				SamplesParserManager.scanSampleInfo(studyKey, format, sampleInfoPath, samplesReceiver);
				samplesReceiver.finishedLoadingSampleInfos();
				break;
			case Sequenom:
				samplesReceiver.startLoadingSampleInfos();
				SamplesParserManager.scanSampleInfo(studyKey, ImportFormat.GWASpi, sampleInfoPath, samplesReceiver); // FIXME why not format instead of ImportFormat.GWASpi?
				samplesReceiver.finishedLoadingSampleInfos();
				break;
			default:
//				sampleInfos = new ArrayList<SampleInfo>();
		}
//
//		return sampleInfos;
	}

	public static Set<SampleInfo.Affection> collectAffectionStates(Collection<SampleInfo> sampleInfos) {

		Set<SampleInfo.Affection> affectionStates = EnumSet.noneOf(SampleInfo.Affection.class);

		for (SampleInfo sampleInfo : sampleInfos) {
			affectionStates.add(sampleInfo.getAffection());
		}

		return affectionStates;
	}
}

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
import org.gwaspi.constants.ImportConstants;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleIlluminaLGENSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(MultipleIlluminaLGENSamplesParser.class);

	@Override
	public void scanSampleInfo(StudyKey studyKey, String sampleInfoPath, DataSetDestination samplesReceiver) throws IOException {

		File[] lgenFilesToScan = org.gwaspi.global.Utils.listFiles(sampleInfoPath);

		int numSamples = 0;
		for (File currentLGENFile : lgenFilesToScan) {
			File sampleFile = currentLGENFile;

			FileReader inputFileReader = null;
			BufferedReader inputBufferReader = null;
			try {
				inputFileReader = new FileReader(sampleFile);
				inputBufferReader = new BufferedReader(inputFileReader);
				boolean gotHeader = false;
				while (!gotHeader) {
					String header = inputBufferReader.readLine();
					if (header == null) {
						break;
					}
					if (header.startsWith("[Data]")) {
						/*header = */inputBufferReader.readLine(); // get the next line, which is the real header
						gotHeader = true;
					}
				}

				String l;
				while (inputBufferReader.ready()) {
					l = inputBufferReader.readLine();
					String[] cVals = l.split(ImportConstants.Separators.separators_CommaSpaceTab_rgxp);
					SampleInfo sampleInfo = new SampleInfo(
							studyKey,
							cVals[ImportConstants.Annotation.Plink_LGEN.lgen_sampleId],
							cVals[ImportConstants.Annotation.Plink_LGEN.lgen_familyId],
							"0",
							"0",
							SampleInfo.Sex.UNKNOWN,
							SampleInfo.Affection.UNKNOWN
							);
					samplesReceiver.addSampleInfo(sampleInfo);
					numSamples++;
				}
				log.info("Parsed {} Samples in LGEN file {}...",
						numSamples, currentLGENFile.getName());
			} finally {
				if (inputBufferReader != null) {
					try {
						inputBufferReader.close();
					} catch (IOException ex) {
						log.warn("Failed to close buffered file input stream when scanning samples: " + String.valueOf(sampleFile), ex);
					}
				} else if (inputFileReader != null) {
					try {
						inputFileReader.close();
					} catch (IOException ex) {
						log.warn("Failed to close file input stream when scanning samples: " + String.valueOf(sampleFile), ex);
					}
				}
			}
		}
	}
}

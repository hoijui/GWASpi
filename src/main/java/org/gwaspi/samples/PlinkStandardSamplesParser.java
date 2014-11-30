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

public class PlinkStandardSamplesParser implements SamplesParser {

	private static final Logger LOG
			= LoggerFactory.getLogger(PlinkStandardSamplesParser.class);

	@Override
	public void scanSampleInfo(StudyKey studyKey, String sampleInfoPath, DataSetDestination samplesReceiver) throws IOException {

		File sampleFile = new File(sampleInfoPath);

		FileReader inputFileReader = null;
		BufferedReader inputBufferReader = null;
		try {
			inputFileReader = new FileReader(sampleFile);
			inputBufferReader = new BufferedReader(inputFileReader);

			// First, we want to find out whether we have long lines
			// in our PED file (we have one sample per line)
			final int maxNumCharsShortLine = 300;
			char[] chunker = new char[maxNumCharsShortLine];
			int readChars = inputBufferReader.read(chunker, 0, chunker.length);
			String line = String.valueOf(chunker, 0, readChars);
			final boolean shortPed = line.contains("\n");

			if (shortPed) {
				// Start reading the file from the beginning again
				inputBufferReader.close();
				inputFileReader = new FileReader(sampleFile);
				inputBufferReader = new BufferedReader(inputFileReader);
			}

			int sampleIndex = 0;
			while (inputBufferReader.ready()) {
				// Read a line.
				// If we have short lines, we simply always read the whole line.
				// If we have long lines, we just read the first NUM_CHARS_SHORT_LINE
				// characters of each line.
				// NUM_CHARS_SHORT_LINE has to be large enough to contain
				// all the sample info, but not too large, so we will not read too much.
				if (shortPed) {
					line = inputBufferReader.readLine();
				} else {
					// This has sucked out 1 week of my life and caused many grey hairs! (by Fernando(?))
					if (sampleIndex != 0) { // make sure we also process the first line
						readChars = inputBufferReader.read(chunker, 0, chunker.length);
						line = (readChars == -1) ? null : String.valueOf(chunker, 0, readChars);
					}
					if (readChars == chunker.length) {
						// There is stuff left at the end of the line
						// -> Read rest of line and discard it
						inputBufferReader.readLine();
					}
				}

				// Parse the line.
				SampleInfo sampleInfo;
				if (line != null) {
					String[] cVals = line.split(ImportConstants.Separators.separators_CommaSpaceTab_rgxp, 10);
					String sexStr = cVals[ImportConstants.Annotation.Plink_Standard.ped_sex];
					sexStr = sexStr.equals("-9") ? "0" : sexStr;
					String affectionStr = cVals[ImportConstants.Annotation.Plink_Standard.ped_affection];
					affectionStr = affectionStr.equals("-9") ? "0" : affectionStr;
					SampleInfo.Sex sex = SampleInfo.Sex.parse(sexStr);
					SampleInfo.Affection affection = SampleInfo.Affection.parse(affectionStr);
					sampleInfo = new SampleInfo(
							studyKey,
							cVals[ImportConstants.Annotation.Plink_Standard.ped_sampleId],
							cVals[ImportConstants.Annotation.Plink_Standard.ped_familyId],
							cVals[ImportConstants.Annotation.Plink_Standard.ped_fatherId],
							cVals[ImportConstants.Annotation.Plink_Standard.ped_motherId],
							sex,
							affection
							);
				} else {
					sampleInfo = new SampleInfo();
				}

				samplesReceiver.addSampleInfo(sampleInfo);
				sampleIndex++;
			}
		} finally {
			if (inputBufferReader != null) {
				try {
					inputBufferReader.close();
				} catch (IOException ex) {
					LOG.warn("Failed to close buffered file input stream when scanning samples: " + String.valueOf(sampleFile), ex);
				}
			} else if (inputFileReader != null) {
				try {
					inputFileReader.close();
				} catch (IOException ex) {
					LOG.warn("Failed to close file input stream when scanning samples: " + String.valueOf(sampleFile), ex);
				}
			}
		}
	}
}

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

public class SequenomSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(SequenomSamplesParser.class);

	@Override
	public void scanSampleInfo(StudyKey studyKey, String sampleInfoPath, DataSetDestination samplesReceiver) throws IOException {

		File gtFileToImport = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(gtFileToImport);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String l;
		int numSamples = 0;
		while (inputBufferReader.ready()) {
			l = inputBufferReader.readLine();
			if (!l.contains("SAMPLE_ID")) { // SKIP ALL HEADER LINES
				String[] cVals = l.split(ImportConstants.Separators.separators_CommaSpaceTab_rgxp);
				// TODO maybe use more then just the sampleId read from the Sequenom file?
				SampleInfo sampleInfo = new SampleInfo(
						studyKey, cVals[ImportConstants.Annotation.Sequenom.sampleId]);
				// NOTE this is done in DataSet, by using LinkedHashSet for the sampleInfos
//				if (!sampleInfos.contains(sampleInfo)) {
					samplesReceiver.addSampleInfo(sampleInfo);
//				}
				numSamples++;

				if (numSamples % 100 == 0) {
					log.info("Parsed {} lines...", numSamples);
				}
			}

		}
		log.info("Parsed {} Samples in Sequenom file {}...",
				numSamples, gtFileToImport);

		inputBufferReader.close();
		inputFileReader.close();
	}
}

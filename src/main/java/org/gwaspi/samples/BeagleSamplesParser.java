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
import org.gwaspi.constants.cImport;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeagleSamplesParser implements SamplesParser {

	private static final Logger LOG = LoggerFactory.getLogger(BeagleSamplesParser.class);

	@Override
	public void scanSampleInfo(StudyKey studyKey, String sampleInfoPath, DataSetDestination samplesReceiver) throws Exception {

		File sampleFile = new File(sampleInfoPath);

		FileReader inputFileReader = null;
		BufferedReader inputBufferReader = null;
		try {
			inputFileReader = new FileReader(sampleFile);
			inputBufferReader = new BufferedReader(inputFileReader);

			String sampleIdHeader = "";
			String affectionHeader = "";
			boolean gotAffection = false;
			while (!gotAffection) {
				String l = inputBufferReader.readLine();
				if (l == null) {
					break;
				}
				if (l.startsWith("I")) {
					sampleIdHeader = l;
				}
				if (l.startsWith("A")) {
					affectionHeader = l;
					gotAffection = true;
				}
			}

			String[] sampleIds = sampleIdHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
			String[] beagleAffections = affectionHeader.split(cImport.Separators.separators_SpaceTab_rgxp);

			for (int i = 2; i < beagleAffections.length; i++) {
				SampleInfo sampleInfo = new SampleInfo(
						studyKey,
						sampleIds[i],
						"0",
						"0",
						"0",
						SampleInfo.Sex.UNKNOWN,
						SampleInfo.Affection.parse(beagleAffections[i])
						);
				samplesReceiver.addSampleInfo(sampleInfo);
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

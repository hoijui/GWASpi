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
import org.gwaspi.netCDF.loader.SamplesReceiver;

public class BeagleSamplesParser implements SamplesParser {

	@Override
	public void scanSampleInfo(StudyKey studyKey, String sampleInfoPath, SamplesReceiver samplesReceiver) throws Exception {

		FileReader inputFileReader;
		File sampleFile = new File(sampleInfoPath);
		inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

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

		inputFileReader.close();
	}
}

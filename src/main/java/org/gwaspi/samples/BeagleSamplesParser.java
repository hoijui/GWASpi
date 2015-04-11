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
import java.io.IOException;
import org.gwaspi.constants.ImportConstants;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;

public class BeagleSamplesParser extends AbstractSamplesParser {

	@Override
	protected void scanSampleInfoFile(
			final StudyKey studyKey,
			final String sampleInfoPath,
			final BufferedReader sampleInfoBR,
			final DataSetDestination samplesReceiver)
			throws IOException
	{
		String sampleIdHeader = "";
		String affectionHeader = "";
		boolean gotAffection = false;
		while (!gotAffection) {
			final String line = sampleInfoBR.readLine();
			if (line == null) {
				break;
			}
			if (line.startsWith("I")) {
				sampleIdHeader = line;
			}
			if (line.startsWith("A")) {
				affectionHeader = line;
				gotAffection = true;
			}
		}

		String[] sampleIds = sampleIdHeader.split(ImportConstants.Separators.separators_SpaceTab_rgxp);
		String[] beagleAffections = affectionHeader.split(ImportConstants.Separators.separators_SpaceTab_rgxp);

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
	}
}

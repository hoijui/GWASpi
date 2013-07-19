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
import java.util.LinkedList;
import org.gwaspi.constants.cImport;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.SamplesReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlinkLGENSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(PlinkLGENSamplesParser.class);

	@Override
	public void scanSampleInfo(StudyKey studyKey, String sampleInfoPath, SamplesReceiver samplesReceiver) throws Exception {

		File sampleFile = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		int numSamples = 0;
		while (inputBufferReader.ready()) {
			String l = inputBufferReader.readLine();
			String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
			SampleInfo sampleInfo = new SampleInfo(
					studyKey,
					cVals[cImport.Annotation.Plink_LGEN.lgen_sampleId],
					cVals[cImport.Annotation.Plink_LGEN.lgen_familyId],
					"0",
					"0",
					SampleInfo.Sex.UNKNOWN,
					SampleInfo.Affection.UNKNOWN
					);
			samplesReceiver.addSampleInfo(sampleInfo);
			numSamples++;
		}
		log.info("Parsed {} Samples in LGEN file {}...",
				numSamples, sampleFile.getName());

		inputBufferReader.close();
		inputFileReader.close();
	}
}

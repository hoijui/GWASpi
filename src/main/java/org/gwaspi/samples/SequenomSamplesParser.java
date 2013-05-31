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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenomSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(SequenomSamplesParser.class);

	@Override
	public Collection<SampleInfo> scanSampleInfo(int studyId, String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();

		File gtFileToImport = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(gtFileToImport);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String l;
		while (inputBufferReader.ready()) {
			l = inputBufferReader.readLine();
			if (!l.contains("SAMPLE_ID")) { // SKIP ALL HEADER LINES
				String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
				// TODO maybe use more then just the sampleId read from the Sequenom file?
				SampleInfo sampleInfo = new SampleInfo(
						studyId, cVals[cImport.Annotation.Sequenom.sampleId]);
				if (!sampleInfos.contains(sampleInfo)) {
					sampleInfos.add(sampleInfo);
				}

				if (sampleInfos.size() % 100 == 0) {
					log.info("Parsed {} lines...", sampleInfos.size());
				}
			}

		}
		log.info("Parsed {} Samples in Sequenom file {}...",
				sampleInfos.size(), gtFileToImport);

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfos;
	}
}

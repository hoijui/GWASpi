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

public class PlinkStandardSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(PlinkStandardSamplesParser.class);

	@Override
	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();
		FileReader inputFileReader;
		BufferedReader inputBufferReader;

		File sampleFile = new File(sampleInfoPath);
		inputFileReader = new FileReader(sampleFile);
		inputBufferReader = new BufferedReader(inputFileReader);

		char[] chunker = new char[300];
		inputBufferReader.read(chunker, 0, 300);
		if (String.valueOf(chunker).contains("\n")) { // SHORT PED FILE
			inputBufferReader.close();
			inputFileReader.close();
			inputFileReader = new FileReader(sampleFile);
			inputBufferReader = new BufferedReader(inputFileReader);

			int count = 0;
			while (inputBufferReader.ready()) {
				String l = inputBufferReader.readLine();
				SampleInfo sampleInfo;
				if (chunker.length > 0) {
					String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10);
					String sexStr = cVals[cImport.Annotation.Plink_Standard.ped_sex];
					sexStr = sexStr.equals("-9") ? "0" : sexStr;
					String affectionStr = cVals[cImport.Annotation.Plink_Standard.ped_affection];
					affectionStr = affectionStr.equals("-9") ? "0" : affectionStr;
					SampleInfo.Sex sex = SampleInfo.Sex.parse(sexStr);
					SampleInfo.Affection affection = SampleInfo.Affection.parse(affectionStr);
					sampleInfo = new SampleInfo(
							cVals[cImport.Annotation.Plink_Standard.ped_sampleId],
							cVals[cImport.Annotation.Plink_Standard.ped_familyId],
							cVals[cImport.Annotation.Plink_Standard.ped_fatherId],
							cVals[cImport.Annotation.Plink_Standard.ped_motherId],
							sex,
							affection
							);
				} else {
					sampleInfo = new SampleInfo();
				}

				sampleInfos.add(sampleInfo);

				count++;
				if (count % 100 == 0) {
					log.info("Parsed {} Samples for info...", count);
				}
			}
		} else { // LONG PED FILE
			// This has sucked out 1 week of my life and caused many grey hairs!
			int count = 0;
			while (inputBufferReader.ready()) {
				if (count != 0) {
					chunker = new char[300];
					inputBufferReader.read(chunker, 0, 300); // Read a sizable but conrolled chunk of data into memory
				}

				SampleInfo sampleInfo;
				if (chunker.length > 0) {
					String[] cVals = String.valueOf(chunker).split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10);
					String sexStr = cVals[cImport.Annotation.Plink_Standard.ped_sex];
					sexStr = sexStr.equals("-9") ? "0" : sexStr;
					String affectionStr = cVals[cImport.Annotation.Plink_Standard.ped_affection];
					affectionStr = affectionStr.equals("-9") ? "0" : affectionStr;
					SampleInfo.Sex sex = SampleInfo.Sex.parse(sexStr);
					SampleInfo.Affection affection = SampleInfo.Affection.parse(affectionStr);
					sampleInfo = new SampleInfo(
							cVals[cImport.Annotation.Plink_Standard.ped_sampleId],
							cVals[cImport.Annotation.Plink_Standard.ped_familyId],
							cVals[cImport.Annotation.Plink_Standard.ped_fatherId],
							cVals[cImport.Annotation.Plink_Standard.ped_motherId],
							sex,
							affection
							);
				} else {
					sampleInfo = new SampleInfo();
				}
				inputBufferReader.readLine(); // Read rest of line and discard it...

				sampleInfos.add(sampleInfo);

				count++;
				if (count % 100 == 0) {
					log.info("Parsed {} Samples for info...", count);
				}
			}
		}

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfos;
	}
}

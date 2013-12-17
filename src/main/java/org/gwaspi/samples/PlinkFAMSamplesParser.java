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
import org.gwaspi.constants.cImport;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;

public class PlinkFAMSamplesParser implements SamplesParser {

	@Override
	public void scanSampleInfo(StudyKey studyKey, String sampleInfoPath, DataSetDestination samplesReceiver) throws Exception {

		FileReader inputFileReader = new FileReader(new File(sampleInfoPath));
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		while (inputBufferReader.ready()) {
			String l = inputBufferReader.readLine();
			String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);

			String sexStr = cVals[cImport.Annotation.Plink_Binary.ped_sex];
			sexStr = sexStr.equals("-9") ? "0" : sexStr;
			String affectionStr = cVals[cImport.Annotation.Plink_Binary.ped_affection];
			affectionStr = affectionStr.equals("-9") ? "0" : affectionStr;
			SampleInfo.Sex sex = SampleInfo.Sex.parse(sexStr);
			SampleInfo.Affection affection = SampleInfo.Affection.parse(affectionStr);
			SampleInfo sampleInfo = new SampleInfo(
					studyKey,
					cVals[cImport.Annotation.Plink_Binary.ped_sampleId],
					cVals[cImport.Annotation.Plink_Binary.ped_familyId],
					cVals[cImport.Annotation.Plink_Binary.ped_fatherId],
					cVals[cImport.Annotation.Plink_Binary.ped_motherId],
					sex,
					affection
					);
			samplesReceiver.addSampleInfo(sampleInfo);
		}

		inputBufferReader.close();
	}
}

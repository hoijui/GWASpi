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
import org.gwaspi.netCDF.loader.LoadGTFromHapmapFiles;
import org.gwaspi.netCDF.loader.DataSetDestination;

public class HapmapSamplesParser implements SamplesParser {

	/**
	 * NOTE No affection state available
	 */
	@Override
	public void scanSampleInfo(StudyKey studyKey, String sampleInfoPath, DataSetDestination samplesReceiver) throws Exception {

		FileReader fr = null;
		BufferedReader inputAnnotationBr = null;
		File hapmapGTFile = new File(sampleInfoPath);
		if (hapmapGTFile.isDirectory()) {
			File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(sampleInfoPath);
			for (File gtFilesToImport1 : gtFilesToImport) {
				fr = new FileReader(gtFilesToImport1);
				inputAnnotationBr = new BufferedReader(fr);
				String header = inputAnnotationBr.readLine();
				String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);
				for (int j = LoadGTFromHapmapFiles.Standard.sampleId; j < hapmapVals.length; j++) {
					SampleInfo sampleInfo = new SampleInfo(
							studyKey, hapmapVals[j]);
					samplesReceiver.addSampleInfo(sampleInfo);
				}
			}
		} else {
			fr = new FileReader(sampleInfoPath);
			inputAnnotationBr = new BufferedReader(fr);

			String header = inputAnnotationBr.readLine();

			String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);
			for (int i = LoadGTFromHapmapFiles.Standard.sampleId; i < hapmapVals.length; i++) {
				SampleInfo sampleInfo = new SampleInfo(
							studyKey, hapmapVals[i]);
				samplesReceiver.addSampleInfo(sampleInfo);
			}
		}

		inputAnnotationBr.close();
		fr.close();
	}
}

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
import org.gwaspi.netCDF.loader.LoadGTFromHapmapFiles;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HapmapSamplesParser implements SamplesParser {

	private static final Logger LOG = LoggerFactory.getLogger(HapmapSamplesParser.class);

	/**
	 * NOTE No affection state available
	 */
	@Override
	public void scanSampleInfo(StudyKey studyKey, String sampleInfoPath, DataSetDestination samplesReceiver) throws IOException {

		FileReader inputFileReader = null;
		BufferedReader inputBufferReader = null;
		File hapmapGTFile = new File(sampleInfoPath);
		if (hapmapGTFile.isDirectory()) {
			File[] sampleFiles = org.gwaspi.global.Utils.listFiles(sampleInfoPath);
			for (File sampleFile : sampleFiles) {
				try {
					inputFileReader = new FileReader(sampleFile);
					inputBufferReader = new BufferedReader(inputFileReader);
					String header = inputBufferReader.readLine();
					String[] hapmapVals = header.split(ImportConstants.Separators.separators_SpaceTab_rgxp);
					for (int j = LoadGTFromHapmapFiles.Standard.sampleId; j < hapmapVals.length; j++) {
						SampleInfo sampleInfo = new SampleInfo(
								studyKey, hapmapVals[j]);
						samplesReceiver.addSampleInfo(sampleInfo);
					}
				} finally {
					if (inputBufferReader != null) {
						try {
							inputBufferReader.close();
						} catch (IOException ex) {
							LOG.warn(
									"Failed to close buffered file input stream when scanning samples: "
											+ sampleFile.getCanonicalPath(), ex);
						}
					} else if (inputFileReader != null) {
						try {
							inputFileReader.close();
						} catch (IOException ex) {
							LOG.warn("Failed to close file input stream when scanning samples: "
									+ sampleFile.getCanonicalPath(), ex);
						}
					}
				}
			}
		} else {
			File sampleFile = hapmapGTFile;
			try {
				inputFileReader = new FileReader(sampleFile);
				inputBufferReader = new BufferedReader(inputFileReader);

				String header = inputBufferReader.readLine();

				String[] hapmapVals = header.split(ImportConstants.Separators.separators_SpaceTab_rgxp);
				for (int i = LoadGTFromHapmapFiles.Standard.sampleId; i < hapmapVals.length; i++) {
					SampleInfo sampleInfo = new SampleInfo(
								studyKey, hapmapVals[i]);
					samplesReceiver.addSampleInfo(sampleInfo);
				}
			} finally {
				if (inputBufferReader != null) {
					try {
						inputBufferReader.close();
					} catch (IOException ex) {
						LOG.warn(
								"Failed to close buffered file input stream when scanning samples: "
										+ sampleFile.getCanonicalPath(), ex);
					}
				} else if (inputFileReader != null) {
					try {
						inputFileReader.close();
					} catch (IOException ex) {
						LOG.warn("Failed to close file input stream when scanning samples: "
								+ sampleFile.getCanonicalPath(), ex);
					}
				}
			}
		}
	}
}

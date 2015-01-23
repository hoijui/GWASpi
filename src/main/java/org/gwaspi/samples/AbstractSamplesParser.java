/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSamplesParser implements SamplesParser {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractSamplesParser.class);

	protected AbstractSamplesParser() {
	}

	protected int getNumHeaderLines() {
		return 0;
	}

	protected void parseSampleInfoFileLine(
			final StudyKey studyKey,
			final int lineIndex,
			final String line,
			final DataSetDestination samplesReceiver)
			throws IOException
	{
	}

	protected void scanSampleInfoFile(
			final StudyKey studyKey,
			final String sampleInfoPath,
			final BufferedReader sampleInfoBR,
			final DataSetDestination samplesReceiver)
			throws IOException
	{
		int lineIndex = 0;
		String line = sampleInfoBR.readLine();
		for (int hli = 0; hli < getNumHeaderLines(); hli++) {
			line = sampleInfoBR.readLine();
		}
		while (line != null) {
			parseSampleInfoFileLine(studyKey, lineIndex, line, samplesReceiver);
			line = sampleInfoBR.readLine();
			lineIndex++;
		}
	}

	@Override
	public void scanSampleInfo(StudyKey studyKey, String sampleInfoPath, DataSetDestination samplesReceiver) throws IOException {

		File sampleFile = new File(sampleInfoPath);

		FileReader inputFileReader = null;
		BufferedReader inputBufferReader = null;
		try {
			inputFileReader = new FileReader(sampleFile);
			inputBufferReader = new BufferedReader(inputFileReader);

			scanSampleInfoFile(studyKey, sampleInfoPath, inputBufferReader, samplesReceiver);
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

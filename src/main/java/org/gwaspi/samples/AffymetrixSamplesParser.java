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

import java.io.File;
import java.io.IOException;
import org.gwaspi.global.Utils;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;

public class AffymetrixSamplesParser implements SamplesParser {

	@Override
	public void scanSampleInfo(
			final StudyKey studyKey,
			final String sampleInfoPath,
			final DataSetDestination samplesReceiver)
			throws IOException
	{
		final File[] sampleFiles = Utils.listFiles(sampleInfoPath);

		for (final File sampleFile : sampleFiles) {
			final String line = sampleFile.getName();
			final String sampleId;
			final int end = line.lastIndexOf(".birdseed-v2");
			if (end != -1) {
				sampleId = line.substring(0, end);
			} else {
				sampleId = line.substring(0, line.lastIndexOf('.'));
			}
			final SampleInfo sampleInfo = new SampleInfo(studyKey, sampleId);
			samplesReceiver.addSampleInfo(sampleInfo);
		}
	}
}

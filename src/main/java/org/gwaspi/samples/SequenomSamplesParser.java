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

import java.io.IOException;
import org.gwaspi.constants.ImportConstants;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;

public class SequenomSamplesParser extends AbstractSamplesParser {

	@Override
	protected void parseSampleInfoFileLine(
			final StudyKey studyKey,
			final int lineIndex,
			final String line,
			final DataSetDestination samplesReceiver)
			throws IOException
	{
		if (!line.contains("SAMPLE_ID")) { // SKIP ALL HEADER LINES
			String[] cVals = line.split(ImportConstants.Separators.separators_CommaSpaceTab_rgxp);
			// TODO maybe use more then just the sampleId read from the Sequenom file?
			SampleInfo sampleInfo = new SampleInfo(
					studyKey, cVals[ImportConstants.Annotation.Sequenom.sampleId]);
			samplesReceiver.addSampleInfo(sampleInfo);
		}
	}
}

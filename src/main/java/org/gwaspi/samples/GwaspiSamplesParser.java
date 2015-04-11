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

public class GwaspiSamplesParser extends AbstractSamplesParser {

	@Override
	protected int getNumHeaderLines() {
		return 1;
	}

	@Override
	protected void parseSampleInfoFileLine(
			final StudyKey studyKey,
			final int lineIndex,
			final String line,
			final DataSetDestination samplesReceiver)
			throws IOException
	{
		final String[] cVals = new String[10];
		int valIndex = 0;
		for (final String field : line.split(ImportConstants.Separators.separators_CommaSpaceTab_rgxp, 10)) {
			cVals[valIndex] = field;
			valIndex++;
		}
		final SampleInfo sampleInfo = new SampleInfo(
				studyKey,
				cVals[ImportConstants.Annotation.GWASpi.sampleId],
				cVals[ImportConstants.Annotation.GWASpi.familyId],
				SampleInfo.ORDER_NULL_ID,
				cVals[ImportConstants.Annotation.GWASpi.fatherId],
				cVals[ImportConstants.Annotation.GWASpi.motherId],
				SampleInfo.Sex.parse(cVals[ImportConstants.Annotation.GWASpi.sex]),
				SampleInfo.Affection.parse(cVals[ImportConstants.Annotation.GWASpi.affection]),
				cVals[ImportConstants.Annotation.GWASpi.category],
				cVals[ImportConstants.Annotation.GWASpi.disease],
				cVals[ImportConstants.Annotation.GWASpi.population],
				Integer.parseInt(cVals[ImportConstants.Annotation.GWASpi.age]),
				"",
				Integer.MIN_VALUE,
				Integer.MIN_VALUE
				);
		samplesReceiver.addSampleInfo(sampleInfo);
	}
}

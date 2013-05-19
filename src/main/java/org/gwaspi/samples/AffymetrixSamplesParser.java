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
import java.util.Collection;
import java.util.LinkedList;
import org.gwaspi.model.SampleInfo;

public class AffymetrixSamplesParser implements SamplesParser {

	@Override
	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();

		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(sampleInfoPath);

		for (int i = 0; i < gtFilesToImport.length; i++) {
			String l = gtFilesToImport[i].getName();
			String sampleId;
			int end = l.lastIndexOf(".birdseed-v2");
			if (end != -1) {
				sampleId = l.substring(0, end);
			} else {
				sampleId = l.substring(0, l.lastIndexOf('.'));
			}
			SampleInfo sampleInfo = new SampleInfo(sampleId);
			sampleInfos.add(sampleInfo);
		}

		return sampleInfos;
	}
}

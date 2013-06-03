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

package org.gwaspi.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.gwaspi.dao.SampleInfoService;
import org.gwaspi.dao.jpa.JPASampleInfoService;

/**
 * @deprecated use SampleInfoService directly
 */
public class SampleInfoList {

	private static final SampleInfoService sampleInfoService
			= new JPASampleInfoService(StudyList.getEntityManagerFactory());

	private SampleInfoList() {
	}

	public static List<SampleInfo> getAllSampleInfoFromDB() throws IOException {
		return sampleInfoService.getSamples();
	}

	public static List<SampleInfo> getAllSampleInfoFromDBByPoolID(StudyKey studyKey) throws IOException {
		return sampleInfoService.getSamples(studyKey);
	}

	public static SampleInfo getSample(SampleKey key) throws IOException {
		return sampleInfoService.getSample(key);
	}

	public static void deleteSamples(StudyKey studyKey) throws IOException {
		sampleInfoService.deleteSamples(studyKey);
	}

	public static void insertSampleInfos(Collection<SampleInfo> sampleInfos) throws IOException {
		sampleInfoService.insertSamples(sampleInfos);
	}
}

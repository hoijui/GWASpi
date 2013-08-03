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

package org.gwaspi.dao;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;

public interface SampleInfoService {

	SampleInfo getSample(SampleKey key) throws IOException;

	List<SampleKey> getSampleKeys() throws IOException;

	List<SampleKey> getSampleKeys(StudyKey studyKey) throws IOException;

	List<SampleInfo> getSamples() throws IOException;

	List<SampleInfo> getSamples(StudyKey studyKey) throws IOException;

	/**
	 * @return picked samples keys and indices in the original/full set of samples of the study
	 */
	<T> Map<SampleKey, Integer> pickSamples(StudyKey studyKey, String variable, Collection<T> criteria, boolean include) throws IOException;

	void deleteSamples(StudyKey studyKey) throws IOException;

	void insertSamples(Collection<SampleInfo> sampleInfos) throws IOException;
}

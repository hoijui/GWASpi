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
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;

public interface SampleInfoService {

	List<SampleInfo> getAllSampleInfoFromDB() throws IOException;

	List<SampleInfo> getAllSampleInfoFromDBByPoolID(Integer studyId) throws IOException;

	List<SampleInfo> getCurrentSampleInfoFromDB(SampleKey key, Integer studyId) throws IOException;

	void deleteSamplesByPoolId(Integer studyId) throws IOException;

	void insertSampleInfos(Integer studyId, Collection<SampleInfo> sampleInfos) throws IOException;
}

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
import java.util.List;
import org.gwaspi.model.Study;

public interface StudyService {

	Study getById(int studyId) throws IOException;

	List<Study> getAll() throws IOException;

	String createStudyManagementTable(Object[] insertValues);

	int insertNewStudy(String studyName, String description);

	void deleteStudy(int studyId, boolean deleteReports) throws IOException;

	String createStudyLogFile(Integer studyId) throws IOException;
}

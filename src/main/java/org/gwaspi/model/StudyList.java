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
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.gwaspi.dao.StudyService;
import org.gwaspi.dao.jpa.JPAStudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated use StudyService directly
 */
public class StudyList {

	private static EntityManagerFactory emf = null;
	private static final Logger log
			= LoggerFactory.getLogger(JPAStudyService.class);
	private static final StudyService studyService
			= new JPAStudyService(getEntityManagerFactory());

	private StudyList() {
	}

	static EntityManagerFactory getEntityManagerFactory() {

		if (emf == null) {
			try {
				emf = Persistence.createEntityManagerFactory("gwaspi");
			} catch (PersistenceException ex) {
				log.error("Failed to initialize database storage", ex);
			}
		}

		return emf;
	}

	public static Study getStudy(StudyKey studyKey) throws IOException {
		return studyService.getStudy(studyKey);
	}

	public static List<StudyKey> getStudies() throws IOException {
		return studyService.getStudies();
	}

	public static List<Study> getStudyList() throws IOException {
		return studyService.getStudiesInfos();
	}

	public static StudyKey insertNewStudy(Study study) {
		return studyService.insertStudy(study);
	}

	public static void deleteStudy(StudyKey studyKey, boolean deleteReports) throws IOException {
		studyService.deleteStudy(studyKey, deleteReports);
	}

	public static void updateStudy(Study study) throws IOException {
		studyService.updateStudy(study);
	}
}

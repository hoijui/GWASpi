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
import org.gwaspi.global.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated use StudyService directly
 */
public class StudyList {

	private static final Logger log
			= LoggerFactory.getLogger(JPAStudyService.class);

	/**
	 * The name of the default, file-system based persistence unit.
	 */
	public static final String PERSISTENCE_UNIT_NAME_DEFAULT = "gwaspi";
	/**
	 * The name of the non-persistent (in-memory only) persistence unit.
	 */
	public static final String PERSISTENCE_UNIT_NAME_IN_MEMORY = "gwaspiInMemory";

	private static EntityManagerFactory emf = null;
	private static StudyService studyService = null;

	private StudyList() {
	}

	public static void clearListsInternalServices() {

		clearInternalService();
		MatricesList.clearInternalService();
		SampleInfoList.clearInternalService();
		ReportsList.clearInternalService();
		OperationsList.clearInternalService();

		clearEntityManagerFactory();
	}

	static void clearInternalService() {
		studyService = null;
	}

	static EntityManagerFactory getEntityManagerFactory() {

		if (emf == null) {
			try {
				final String persistenceUnitName
						= Config.getSingleton().getBoolean(Config.PROPERTY_STORAGE_IN_MEMORY, false)
						? PERSISTENCE_UNIT_NAME_IN_MEMORY
						: PERSISTENCE_UNIT_NAME_DEFAULT;
				emf = Persistence.createEntityManagerFactory(persistenceUnitName);
				log.info("NOTE: The above warning (HHH015016) can be neglected.");
				// ... as we do actually use hte correct/new provider,
				// but due to the way JPA works, it issues this warning anyway.
				// For more details, see:
				// https://hibernate.atlassian.net/browse/HHH-8735
			} catch (PersistenceException ex) {
				log.error("Failed to initialize database storage", ex);
			}
		}

		return emf;
	}

	private static void clearEntityManagerFactory() {

		if ((emf != null) && emf.isOpen()) {
			emf.close();
		}
		emf = null;
	}

	private static StudyService getStudyService() {

		if (studyService == null) {
			studyService = new JPAStudyService(getEntityManagerFactory());
		}

		return studyService;
	}

	public static Study getStudy(StudyKey studyKey) throws IOException {
		return getStudyService().getStudy(studyKey);
	}

	public static List<StudyKey> getStudies() throws IOException {
		return getStudyService().getStudies();
	}

	public static StudyKey getStudyByName(final String name) throws IOException {
		return getStudyService().getStudyByName(name);
	}

	public static List<Study> getStudyList() throws IOException {
		return getStudyService().getStudiesInfos();
	}

	public static StudyKey insertNewStudy(Study study) {
		return getStudyService().insertStudy(study);
	}

	public static void deleteStudy(StudyKey studyKey, boolean deleteReports) throws IOException {
		getStudyService().deleteStudy(studyKey, deleteReports);
	}

	public static void updateStudy(Study study) throws IOException {
		getStudyService().updateStudy(study);
	}
}

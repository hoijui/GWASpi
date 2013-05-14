package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.gwaspi.dao.StudyService;
import org.gwaspi.dao.jpa.JPAStudyService;
import org.gwaspi.dao.sql.StudyServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated use StudyService directly
 */
public class StudyList {

	private static final Logger log
			= LoggerFactory.getLogger(JPAStudyService.class);
	private static final StudyService studyService
			= MatricesList.USE_JPA
			? new JPAStudyService(getEntityManagerFactory())
			: new StudyServiceImpl();
	private static EntityManagerFactory emf = null;

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

	public static Study getStudy(int studyId) throws IOException {
		return studyService.getById(studyId);
	}

	public static List<Study> getStudyList() throws IOException {
		return studyService.getAll();
	}

	public static String createStudyManagementTable(Object[] insertValues) {
		return studyService.createStudyManagementTable(insertValues);
	}

	public static int insertNewStudy(String studyName, String description) {
		return studyService.insertNewStudy(studyName, description);
	}

	public static void deleteStudy(int studyId, boolean deleteReports) throws IOException {
		studyService.deleteStudy(studyId, deleteReports);
	}

	public static String createStudyLogFile(Integer studyId) throws IOException {
		return studyService.createStudyLogFile(studyId);
	}
}

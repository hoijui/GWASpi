package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import org.gwaspi.dao.StudyService;
import org.gwaspi.dao.sql.StudyServiceImpl;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated use StudyService directly
 */
public class StudyList {

	private static final StudyService studyService = new StudyServiceImpl();

	private StudyList() {
	}

	public static Study getStudy(int studyId) throws IOException {
		return studyService.getById(studyId);
	}

	public static List<Study> getStudyList() throws IOException {
		return studyService.getAll();
	}

	public static Object[][] getStudyTable() throws IOException {
		return studyService.getAllAsTable();
	}

	public static String createStudyManagementTable(Object[] insertValues) {
		return studyService.createStudyManagementTable(insertValues);
	}

	public static void insertNewStudy(String studyName, String description) {
		studyService.insertNewStudy(studyName, description);
	}

	public static void deleteStudy(int studyId, boolean deleteReports) throws IOException {
		studyService.deleteStudy(studyId, deleteReports);
	}

	public static String createStudyLogFile(Integer studyId) throws IOException {
		return studyService.createStudyLogFile(studyId);
	}
}

package org.gwaspi.model;

import org.gwaspi.dao.StudyService;
import org.gwaspi.dao.sql.StudyServiceImpl;
import java.io.IOException;
import java.util.List;

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
}

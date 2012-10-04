package org.gwaspi.model;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBOperations;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class StudyList {

	private static final Logger log
			= LoggerFactory.getLogger(StudyList.class);

	private StudyList() throws IOException {
	}

	public static List<Study> getStudyList() throws IOException {

		List<Study> studyList;

		List<Map<String, Object>> rsStudyList = getStudyListRaw();
		studyList = new ArrayList<Study>(rsStudyList.size());

		int rowcount = rsStudyList.size();
		if (rowcount > 0) {
			// loop through the rows of the result set
			for (int i = rowcount - 1; i >= 0; i--) {
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsStudyList.isEmpty() && rsStudyList.get(i).size() == cDBGWASpi.T_CREATE_STUDIES.length) {
					int currentStudyId = (Integer) rsStudyList.get(i).get(cDBGWASpi.f_ID);
					Study currentStudy = new Study(currentStudyId);
					studyList.add(currentStudy);
				}
			}
		}

		return studyList;
	}

	private static List<Map<String, Object>> getStudyListRaw() throws IOException {

		List<Map<String, Object>> rs = null;

		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_APP + "." + cDBGWASpi.T_STUDIES + " ORDER BY " + cDBGWASpi.f_ID + " DESC WITH RR");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		return rs;
	}

	public static Object[][] getStudyTable() throws IOException {

		Object[][] studyTable = null;

		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>> rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_APP + "." + cDBGWASpi.T_STUDIES + " ORDER BY " + cDBGWASpi.f_ID + " ASC WITH RR");

			studyTable = new Object[rs.size()][4];
			for (int i = 0; i < rs.size(); i++) {
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == cDBGWASpi.T_CREATE_STUDIES.length) {
					studyTable[i][0] = (Integer) rs.get(i).get(cDBGWASpi.f_ID);
					studyTable[i][1] = rs.get(i).get(cDBGWASpi.f_NAME).toString();
					studyTable[i][2] = rs.get(i).get(cDBGWASpi.f_STUDY_DESCRIPTION).toString();
					String timestamp = rs.get(i).get(cDBOperations.f_CREATION_DATE).toString();
					studyTable[i][3] = timestamp.substring(0, timestamp.lastIndexOf('.'));
				}
			}
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return studyTable;
	}
}

package org.gwaspi.model;

import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class StudyList {

	ArrayList<model.Study> studyList = new ArrayList();

	public StudyList() throws IOException {

		List<Map<String, Object>> rsStudyList = getStudyList();

		int rowcount = rsStudyList.size();
		if (rowcount > 0) {
			for (int i = rowcount - 1; i >= 0; i--) // loop through rows of result set
			{
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsStudyList.isEmpty() && rsStudyList.get(i).size() == org.gwaspi.constants.cDBGWASpi.T_CREATE_STUDIES.length) {
					int currentStudyId = (Integer) rsStudyList.get(i).get(org.gwaspi.constants.cDBGWASpi.f_ID);
					Study currentStudy = new Study(currentStudyId);
					studyList.add(currentStudy);
				}
			}
		}
	}

	public List<Map<String, Object>> getStudyList() throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_APP + "." + org.gwaspi.constants.cDBGWASpi.T_STUDIES + " ORDER BY " + org.gwaspi.constants.cDBGWASpi.f_ID + " DESC WITH RR");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return rs;
	}

	public static Object[][] getStudyTable() throws IOException {
		Object[][] studyTable = null;

		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>> rs = studyDbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_APP + "." + org.gwaspi.constants.cDBGWASpi.T_STUDIES + " ORDER BY " + org.gwaspi.constants.cDBGWASpi.f_ID + " ASC WITH RR");

			studyTable = new Object[rs.size()][4];
			for (int i = 0; i < rs.size(); i++) {
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == org.gwaspi.constants.cDBGWASpi.T_CREATE_STUDIES.length) {
					studyTable[i][0] = (Integer) rs.get(i).get(org.gwaspi.constants.cDBGWASpi.f_ID);
					studyTable[i][1] = rs.get(i).get(org.gwaspi.constants.cDBGWASpi.f_NAME).toString();
					studyTable[i][2] = rs.get(i).get(org.gwaspi.constants.cDBGWASpi.f_STUDY_DESCRIPTION).toString();
					String timestamp = rs.get(i).get(org.gwaspi.constants.cDBOperations.f_CREATION_DATE).toString();
					studyTable[i][3] = timestamp.substring(0, timestamp.lastIndexOf('.'));
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return studyTable;
	}
}

package org.gwaspi.dao.sql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cDBOperations;
import org.gwaspi.dao.StudyService;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.Matrix;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudyServiceImpl implements StudyService {

	private static final Logger log
			= LoggerFactory.getLogger(StudyServiceImpl.class);

	/**
	 * This will init the Study object requested from the DB
	 */
	@Override
	public Study getById(int studyId) throws IOException {

		Study study = null;

		List<Map<String, Object>> rs = getStudy(studyId);

		// PREVENT PHANTOM-DB READS EXCEPTIONS
		if (!rs.isEmpty() && rs.get(0).size() == cDBGWASpi.T_CREATE_STUDIES.length) {
			int id = Integer.parseInt(rs.get(0).get("id").toString());
			String name = rs.get(0).get("name").toString();
			String description = rs.get(0).get("study_description").toString();
			String studyType = rs.get(0).get("study_type").toString();
			String validity = rs.get(0).get("validity").toString();
			Date creationDate = org.gwaspi.global.Utils.stringToDate(rs.get(0).get("creation_date").toString(), "yyyy-MM-dd hh:mm:ss.SSS");

//			List<Integer> studyMatrices = getStudyMatricesId(studyId);

			study = new Study(id, name, description, studyType, validity, creationDate);
		}

		return study;
	}

	private static List<Map<String, Object>> getStudy(int studyId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_APP + "." + cDBGWASpi.T_STUDIES + " WHERE id=" + studyId + "  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return rs;
	}

	private static List<Integer> getStudyMatricesId(int studyId) throws IOException {
		List<Integer> studyMatricesList = new ArrayList<Integer>();
		List<Map<String, Object>> rs = null;
		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " WHERE " + cDBMatrix.f_STUDYID + "=" + studyId + "  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		int rowcount = rs.size();
		if (rowcount > 0) {
			for (int i = rowcount - 1; i >= 0; i--) // loop through rows of result set
			{
				int currentMatrixId = (Integer) rs.get(i).get(cDBMatrix.f_ID);
				studyMatricesList.add(currentMatrixId);
			}
		}

		return studyMatricesList;
	}

	@Override
	public List<Study> getAll() throws IOException {

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
					Study currentStudy = getById(currentStudyId);
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

	@Override
	public Object[][] getAllAsTable() throws IOException {

		Object[][] studyTable = null;

		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>> rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_APP + "." + cDBGWASpi.T_STUDIES + " ORDER BY " + cDBGWASpi.f_ID + " ASC WITH RR");

			studyTable = new Object[rs.size()][4];
			for (int i = 0; i < rs.size(); i++) {
				// PREVENT PHANTOM-DB READS EXCEPTIONS
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

	@Override
	public String createStudyManagementTable(Object[] insertValues) {
		boolean result = false;
		try {
			DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			// CREATE STUDIES table in APP SCHEMA and fill with data
			db.createTable(cDBGWASpi.SCH_APP,
					cDBGWASpi.T_STUDIES,
					cDBGWASpi.T_CREATE_STUDIES);

			result = db.insertValuesInTable(cDBGWASpi.SCH_APP,
					cDBGWASpi.T_STUDIES,
					cDBGWASpi.F_INSERT_STUDIES,
					insertValues);
		} catch (Exception ex) {
			log.error("Failed creating Schema or Studies table", ex);
		}
		return (result) ? "1" : "0";
	}

	@Override
	public void insertNewStudy(String studyName, String description) {
		try {
			DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

			// INSERT study data into study management table
			boolean result = db.insertValuesInTable(cDBGWASpi.SCH_APP,
					cDBGWASpi.T_STUDIES,
					cDBGWASpi.F_INSERT_STUDIES,
					new Object[]{studyName, // name
						description, // description
						"external", // stydy_type
						"1"}); // validity

		} catch (Exception ex) {
			log.error("Failed creating management database", ex);
		}
	}

	@Override
	public void deleteStudy(int studyId, boolean deleteReports) throws IOException {

		List<Matrix> matrixList = MatricesList.getMatrixList(studyId);

		for (int i = 0; i < matrixList.size(); i++) {
			try {
				MatricesList.deleteMatrix(matrixList.get(i).getId(), deleteReports);
				GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
			} catch (IOException ex) {
				log.warn(null, ex);
			}
		}

		// DELETE METADATA INFO FROM DB
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		String statement = "DELETE FROM " + cDBGWASpi.SCH_APP + "." + cDBGWASpi.T_STUDIES + " WHERE " + cDBGWASpi.f_ID + "=" + studyId;
		dBManager.executeStatement(statement);

		// DELETE STUDY FOLDERS
		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
		File gtStudyFolder = new File(genotypesFolder + "/STUDY_" + studyId);
		org.gwaspi.global.Utils.deleteFolder(gtStudyFolder);

		if (deleteReports) {
			String reportsFolder = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "");
			File repStudyFolder = new File(reportsFolder + "/STUDY_" + studyId);
			org.gwaspi.global.Utils.deleteFolder(repStudyFolder);
		}

		// DELETE STUDY POOL SAMPLES
		SampleInfoList.deleteSamplesByPoolId(studyId);
	}

	@Override
	public String createStudyLogFile(Integer studyId) throws IOException {
		String result = "";

		// Create log file containing study history
		FileWriter fw = new FileWriter(Config.getConfigValue(Config.PROPERTY_LOG_DIR, "") + "/" + studyId + ".log");
		return result;
	}
}

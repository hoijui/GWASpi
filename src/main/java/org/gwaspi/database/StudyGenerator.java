package org.gwaspi.database;

import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class StudyGenerator {

	private static final Logger log = LoggerFactory.getLogger(StudyGenerator.class);

	private StudyGenerator() {
	}

	public static String createStudyManagementTable(DbManager db, Object[] insertValues) {
		boolean result = false;
		try {
			// CREATE STUDIES table in APP SCHEMA and fill with data
			db.createTable(org.gwaspi.constants.cDBGWASpi.SCH_APP,
					org.gwaspi.constants.cDBGWASpi.T_STUDIES,
					org.gwaspi.constants.cDBGWASpi.T_CREATE_STUDIES);

			result = db.insertValuesInTable(org.gwaspi.constants.cDBGWASpi.SCH_APP,
					org.gwaspi.constants.cDBGWASpi.T_STUDIES,
					org.gwaspi.constants.cDBGWASpi.F_INSERT_STUDIES,
					insertValues);
		} catch (Exception ex) {
			log.error("Failed creating Schema or Studies table", ex);
		}
		return (result) ? "1" : "0";
	}

	public static void insertNewStudy(String studyName, String description) {
		try {
			DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

			// INSERT study data into study management table
			boolean result = db.insertValuesInTable(org.gwaspi.constants.cDBGWASpi.SCH_APP,
					org.gwaspi.constants.cDBGWASpi.T_STUDIES,
					org.gwaspi.constants.cDBGWASpi.F_INSERT_STUDIES,
					new Object[]{studyName, // name
						description, // description
						"external", // stydy_type
						"1"}); // validity

		} catch (Exception ex) {
			log.error("Failed creating management database", ex);
		}
	}

	public static void deleteStudy(int studyId, boolean deleteReports) throws IOException {

		org.gwaspi.model.MatricesList matrixMod = new org.gwaspi.model.MatricesList(studyId);

		for (int i = 0; i < matrixMod.matrixList.size(); i++) {
			try {
				org.gwaspi.netCDF.matrices.MatrixManager.deleteMatrix(matrixMod.matrixList.get(i).getMatrixId(), deleteReports);
				org.gwaspi.gui.GWASpiExplorerPanel.updateTreePanel(true);
			} catch (IOException ex) {
			}
		}

		// DELETE METADATA INFO FROM DB
		DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
		String statement = "DELETE FROM " + org.gwaspi.constants.cDBGWASpi.SCH_APP + "." + org.gwaspi.constants.cDBGWASpi.T_STUDIES + " WHERE " + org.gwaspi.constants.cDBGWASpi.f_ID + "=" + studyId;
		dBManager.executeStatement(statement);

		// DELETE STUDY FOLDERS
		String genotypesFolder = org.gwaspi.global.Config.getConfigValue("GTdir", "");
		File gtStudyFolder = new File(genotypesFolder + "/STUDY_" + studyId);
		org.gwaspi.global.Utils.deleteFolder(gtStudyFolder);

		if (deleteReports) {
			String reportsFolder = org.gwaspi.global.Config.getConfigValue("ReportsDir", "");
			File repStudyFolder = new File(reportsFolder + "/STUDY_" + studyId);
			org.gwaspi.global.Utils.deleteFolder(repStudyFolder);
		}

		// DELETE STUDY POOL SAMPLES
		org.gwaspi.samples.SampleManager.deleteSamplesByPoolId(studyId);
	}

	public static String createStudyLogFile(Integer studyId) throws IOException {
		String result = "";

		// Create log file containing study history
		FileWriter fw = new FileWriter(org.gwaspi.global.Config.getConfigValue("LogDir", "") + "/" + studyId + ".log");
		return result;
	}
}

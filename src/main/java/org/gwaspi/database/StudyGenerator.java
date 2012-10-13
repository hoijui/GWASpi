package org.gwaspi.database;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.gui.GWASpiExplorerPanel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.Matrix;
import org.gwaspi.netCDF.matrices.MatrixManager;
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

	public static void insertNewStudy(String studyName, String description) {
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

	public static void deleteStudy(int studyId, boolean deleteReports) throws IOException {

		List<Matrix> matrixList = MatricesList.getMatrixList(studyId);

		for (int i = 0; i < matrixList.size(); i++) {
			try {
				MatrixManager.deleteMatrix(matrixList.get(i).getId(), deleteReports);
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
		org.gwaspi.samples.SampleManager.deleteSamplesByPoolId(studyId);
	}

	public static String createStudyLogFile(Integer studyId) throws IOException {
		String result = "";

		// Create log file containing study history
		FileWriter fw = new FileWriter(Config.getConfigValue(Config.PROPERTY_LOG_DIR, "") + "/" + studyId + ".log");
		return result;
	}
}

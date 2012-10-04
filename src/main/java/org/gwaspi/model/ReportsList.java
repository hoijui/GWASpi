package org.gwaspi.model;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cDBReports;
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
public class ReportsList {

	private static final Logger log
			= LoggerFactory.getLogger(ReportsList.class);

	private ReportsList() {
	}

	public static List<Report> getReportsList(int opId, int matrixId) throws IOException {

		List<Report> reportsList = new ArrayList<Report>();

		List<Map<String, Object>> rs;

		if (opId != Integer.MIN_VALUE) {
			rs = getReportListByOperationId(opId);
		} else {
			rs = getReportListByMatrixId(matrixId);
		}

		int rowcount = rs.size();
		if (rowcount > 0) {
			for (int i = rowcount - 1; i >= 0; i--) // loop through rows of result set
			{
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == cDBReports.T_CREATE_REPORTS.length) {
					int currentRPId = (Integer) rs.get(i).get(cDBMatrix.f_ID);
					Report currentRP = new Report(currentRPId);
					reportsList.add(currentRP);
				}
			}
		}

		return reportsList;
	}

	private static List<Map<String, Object>> getReportListByOperationId(int opId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBReports.T_REPORTS + " WHERE " + cDBReports.f_PARENT_OPID + "=" + opId + " ORDER BY " + cDBMatrix.f_ID + " DESC  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return rs;
	}

	private static List<Map<String, Object>> getReportListByMatrixId(int matrixId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBReports.T_REPORTS + " WHERE " + cDBReports.f_PARENT_MATRIXID + "=" + matrixId + " ORDER BY " + cDBMatrix.f_ID + " DESC  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return rs;
	}

	private static Object[][] getReportsTable(int opId) throws IOException {
		Object[][] reportsTable = null;

		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager dbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>> rs = dbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBReports.T_REPORTS + " WHERE " + cDBReports.f_ID + "=" + opId + "  WITH RR");

			reportsTable = new Object[rs.size()][3];
			for (int i = 0; i < rs.size(); i++) {
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == cDBReports.T_CREATE_REPORTS.length) {
					reportsTable[i][0] = (Integer) rs.get(i).get(cDBReports.f_ID);
					reportsTable[i][1] = rs.get(i).get(cDBReports.f_RP_NAME).toString();
					reportsTable[i][2] = rs.get(i).get(cDBReports.f_DESCRIPTION).toString();
				}
			}
		} catch (Exception ex) {
			log.error(null, ex);
		}
		return reportsTable;
	}
}

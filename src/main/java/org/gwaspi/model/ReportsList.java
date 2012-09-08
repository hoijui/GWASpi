package org.gwaspi.model;

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

	private final static Logger log
			= LoggerFactory.getLogger(ReportsList.class);

	public List<model.Report> reportsListAL = new ArrayList<model.Report>();

	public ReportsList(int opId, int matrixId) throws IOException {

		List<Map<String, Object>> rs = null;

		if (opId != Integer.MIN_VALUE) {
			rs = getReportListByOperationId(opId);
		} else {
			rs = getReportListByMatrixId(matrixId);
		}

		int rowcount = rs.size();
		if (rowcount > 0) {
			for (int i = rowcount - 1; i >= 0; i--) // loop through rows of result set
			{
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == org.gwaspi.constants.cDBReports.T_CREATE_REPORTS.length) {
					int currentRPId = (Integer) rs.get(i).get(org.gwaspi.constants.cDBMatrix.f_ID);
					Report currentRP = new Report(currentRPId);
					reportsListAL.add(currentRP);
				}
			}
		}
	}

	public List<Map<String, Object>> getReportListByOperationId(int opId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBReports.T_REPORTS + " WHERE " + org.gwaspi.constants.cDBReports.f_PARENT_OPID + "=" + opId + " ORDER BY " + org.gwaspi.constants.cDBMatrix.f_ID + " DESC  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return rs;
	}

	public List<Map<String, Object>> getReportListByMatrixId(int matrixId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBReports.T_REPORTS + " WHERE " + org.gwaspi.constants.cDBReports.f_PARENT_MATRIXID + "=" + matrixId + " ORDER BY " + org.gwaspi.constants.cDBMatrix.f_ID + " DESC  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return rs;
	}

	public static Object[][] getReportsTable(int opId) throws IOException {
		Object[][] reportsTable = null;

		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager dbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>> rs = dbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBReports.T_REPORTS + " WHERE " + org.gwaspi.constants.cDBReports.f_ID + "=" + opId + "  WITH RR");

			reportsTable = new Object[rs.size()][3];
			for (int i = 0; i < rs.size(); i++) {
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == org.gwaspi.constants.cDBReports.T_CREATE_REPORTS.length) {
					reportsTable[i][0] = (Integer) rs.get(i).get(org.gwaspi.constants.cDBReports.f_ID);
					reportsTable[i][1] = rs.get(i).get(org.gwaspi.constants.cDBReports.f_RP_NAME).toString();
					reportsTable[i][2] = rs.get(i).get(org.gwaspi.constants.cDBReports.f_DESCRIPTION).toString();
				}
			}
		} catch (Exception ex) {
			//log.error(null, ex);
		}
		return reportsTable;
	}
}

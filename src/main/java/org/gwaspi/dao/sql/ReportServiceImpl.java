package org.gwaspi.dao.sql;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cDBReports;
import org.gwaspi.dao.ReportService;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportServiceImpl implements ReportService {

	private static final Logger log
			= LoggerFactory.getLogger(ReportServiceImpl.class);

	@Override
	public Report getById(int reportId) throws IOException {

		Report report = null;

		List<Map<String, Object>> rs = getReportMetadata(reportId);

		// PREVENT PHANTOM-DB READS EXCEPTIONS
		if (!rs.isEmpty() && rs.get(0).size() == cDBReports.T_CREATE_REPORTS.length) {
			String friendlyName = (rs.get(0).get(cDBReports.f_RP_NAME) != null) ? rs.get(0).get(cDBReports.f_RP_NAME).toString() : "";
			String fileName = (rs.get(0).get(cDBReports.f_RP_FILE_NAME) != null) ? rs.get(0).get(cDBReports.f_RP_FILE_NAME).toString() : "";
			String type = (rs.get(0).get(cDBReports.f_RP_TYPE) != null) ? rs.get(0).get(cDBReports.f_RP_TYPE).toString() : "";
			int parentMatrixId = (rs.get(0).get(cDBReports.f_PARENT_MATRIXID) != null) ? Integer.parseInt(rs.get(0).get(cDBReports.f_PARENT_MATRIXID).toString()) : -1;
			int parentOpId = (rs.get(0).get(cDBReports.f_PARENT_OPID) != null) ? Integer.parseInt(rs.get(0).get(cDBReports.f_PARENT_OPID).toString()) : -1;
			String description = (rs.get(0).get(cDBReports.f_DESCRIPTION) != null) ? rs.get(0).get(cDBReports.f_DESCRIPTION).toString() : "";
			int studyId = (rs.get(0).get(cDBReports.f_STUDYID) != null) ? Integer.parseInt(rs.get(0).get(cDBReports.f_STUDYID).toString()) : 0;

			report = new Report(
					reportId,
					friendlyName,
					fileName,
					type,
					parentMatrixId,
					parentOpId,
					description,
					studyId
					);
		}

		return report;
	}

	private static List<Map<String, Object>> getReportMetadata(int rpId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBReports.T_REPORTS + " WHERE " + cDBReports.f_ID + "=" + rpId + "  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return rs;
	}

	@Override
	public List<Report> getReportsList(int opId, int matrixId) throws IOException {

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
					Report currentRP = getById(currentRPId);
					reportsList.add(currentRP);
				}
			}
		}

		return reportsList;
	}

	@Override
	public List<Map<String, Object>> getReportListByOperationId(int opId) throws IOException {
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

	@Override
	public List<Map<String, Object>> getReportListByMatrixId(int matrixId) throws IOException {
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

	@Override
	public Object[][] getReportsTable(int opId) throws IOException {
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

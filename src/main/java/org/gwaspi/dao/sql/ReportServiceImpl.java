package org.gwaspi.dao.sql;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cDBReports;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.dao.ReportService;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
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

	//<editor-fold defaultstate="collapsed" desc="UTILS">
	@Override
	public Map<String, Object> getSortedMarkerSetByDoubleValue(Map<String, Object> map) {
		List<Map.Entry<String, Object>> list = new LinkedList<Map.Entry<String, Object>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
			public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
				return ((Comparable) o1.getValue()).compareTo(o2.getValue());
			}
		});
		// logger.info(list);
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, Object> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	@Override
	public Map<String, Object> getSortedDescendingMarkerSetByDoubleValue(Map<String, Object> map) {
		List<Map.Entry<String, Object>> list = new LinkedList<Map.Entry<String, Object>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
			public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
				return -1 * ((Comparable) o1.getValue()).compareTo(o2.getValue());
			}
		});
		// logger.info(list);
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, Object> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="OPERATIONS METADATA">
	@Override
	public String getReportNamePrefix(Operation op) {
		StringBuilder prefix = new StringBuilder();
		prefix.append("mx-");
		prefix.append(op.getParentMatrixId());

		prefix.append("_").append(op.getOperationType().toString()).append("-");
		prefix.append(op.getId());

		// Get Genotype Freq. assigned name. Pry out the part inserted by user only
		try {
			if (op.getOperationType().equals(cNetCDF.Defaults.OPType.ALLELICTEST.toString())
					|| op.getOperationType().equals(cNetCDF.Defaults.OPType.GENOTYPICTEST.toString())
					|| op.getOperationType().equals(cNetCDF.Defaults.OPType.TRENDTEST.toString())) {
				Operation parentOp = OperationsList.getById(op.getParentOperationId());
				String[] tmp = parentOp.getFriendlyName().split("-", 2);
				tmp = tmp[1].split("using");
				prefix.append("_");
				prefix.append(org.gwaspi.global.Utils.stripNonAlphaNumericDashUndscr(tmp[0].trim()));
				prefix.append("_");
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return prefix.toString();
	}

	@Override
	public String createReportsMetadataTable() {
		boolean result = false;
		try {
			DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			// CREATE SAMPLESET_METADATA table in given SCHEMA
			db.createTable(cDBGWASpi.SCH_MATRICES,
					cDBReports.T_REPORTS,
					cDBReports.T_CREATE_REPORTS);
		} catch (Exception ex) {
			log.error("Failed creating management database", ex);
		}

		return (result) ? "1" : "0";
	}

	@Override
	public void insertRPMetadata(
			String reportName,
			String fileName,
			String RPType,
			int parentMatrixId,
			int parentOPId,
			String description,
			int studyId)
			throws IOException
	{
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		Object[] rpMetaData = new Object[]{reportName,
			description,
			fileName,
			RPType,
			parentMatrixId,
			parentOPId,
			studyId};

		dBManager.insertValuesInTable(cDBGWASpi.SCH_MATRICES,
				cDBReports.T_REPORTS,
				cDBReports.F_INSERT_REPORT,
				rpMetaData);
	}

	@Override
	public void deleteReportByMatrixId(int matrixId) throws IOException {
		MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataById(matrixId);
		List<Report> reportsList = ReportsList.getReportsList(Integer.MIN_VALUE, matrixId);
		String reportsFolder = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "");

		for (Report rp : reportsList) {
			File reportFile = new File(reportsFolder + "/STUDY_" + matrixMetadata.getStudyId() + "/" + rp.getFileName());
			org.gwaspi.global.Utils.tryToDeleteFile(reportFile);
		}

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBReports.T_REPORTS + " WHERE " + cDBReports.f_PARENT_MATRIXID + "=" + matrixId;
		dBManager.executeStatement(statement);
	}

	@Override
	public void deleteReportByOperationId(int opId) throws IOException {
		OperationMetadata operationMetadata = OperationsList.getOperationMetadata(opId);
		List<Report> reportsList = ReportsList.getReportsList(opId, operationMetadata.getParentMatrixId());
		String reportsFolder = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "");

		for (Report rp : reportsList) {
			File reportFile = new File(reportsFolder + "/STUDY_" + operationMetadata.getStudyId() + "/" + rp.getFileName());
			org.gwaspi.global.Utils.tryToDeleteFile(reportFile);
		}

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBReports.T_REPORTS + " WHERE " + cDBReports.f_PARENT_MATRIXID + "=" + opId;
		dBManager.executeStatement(statement);
	}
	//</editor-fold>
}

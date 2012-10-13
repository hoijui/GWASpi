package org.gwaspi.reports;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBReports;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Operation;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class ReportManager {

	private static final Logger log = LoggerFactory.getLogger(ReportManager.class);

	private ReportManager() {
	}

	//<editor-fold defaultstate="collapsed" desc="UTILS">
	static Map<String, Object> getSortedMarkerSetByDoubleValue(Map<String, Object> map) {
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

	static Map<String, Object> getSortedDescendingMarkerSetByDoubleValue(Map<String, Object> map) {
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
	public static String getreportNamePrefix(Operation op) {
		StringBuilder prefix = new StringBuilder();
		prefix.append("mx-");
		prefix.append(op.getParentMatrixId());

		prefix.append("_").append(op.getOperationType().toString()).append("-");
		prefix.append(op.getId());

		// Get Genotype Freq. assigned name. Pry out the part inserted by user only
		try {
			if (op.getOperationType().equals(OPType.ALLELICTEST.toString())
					|| op.getOperationType().equals(OPType.GENOTYPICTEST.toString())
					|| op.getOperationType().equals(OPType.TRENDTEST.toString())) {
				Operation parentOp = new Operation(op.getParentOperationId());
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

	public static String createReportsMetadataTable(DbManager db) {
		boolean result = false;
		try {
			// CREATE SAMPLESET_METADATA table in given SCHEMA
			db.createTable(cDBGWASpi.SCH_MATRICES,
					cDBReports.T_REPORTS,
					cDBReports.T_CREATE_REPORTS);
		} catch (Exception ex) {
			log.error("Failed creating management database", ex);
		}

		return (result) ? "1" : "0";
	}

	static void insertRPMetadata(DbManager dBManager,
			String reportName,
			String fileName,
			String RPType,
			int parentMatrixId,
			int parentOPId,
			String description,
			int studyId) {

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

	public static void deleteReportByMatrixId(int matrixId) throws IOException {
		MatrixMetadata matrixMetadata = new MatrixMetadata(matrixId);
		List<Report> reportsList = ReportsList.getReportsList(Integer.MIN_VALUE, matrixId);
		String reportsFolder = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "");

		for (Report rp : reportsList) {
			File reportFile = new File(reportsFolder + "/STUDY_" + matrixMetadata.getStudyId() + "/" + rp.getFileName());

			if (reportFile.exists()) {
				if (!reportFile.canWrite()) {
					throw new IllegalArgumentException("Delete: write protected: " + reportFile.getPath());
				}
				boolean success = reportFile.delete();
			}
		}

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBReports.T_REPORTS + " WHERE " + cDBReports.f_PARENT_MATRIXID + "=" + matrixId;
		dBManager.executeStatement(statement);
	}

	public static void deleteReportByOperationId(int opId) throws IOException {
		OperationMetadata operationMetadata = new OperationMetadata(opId);
		List<Report> reportsList = ReportsList.getReportsList(opId, operationMetadata.getParentMatrixId());
		String reportsFolder = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "");

		for (Report rp : reportsList) {
			File reportFile = new File(reportsFolder + "/STUDY_" + operationMetadata.getStudyId() + "/" + rp.getFileName());

			if (reportFile.exists()) {
				if (!reportFile.canWrite()) {
					throw new IllegalArgumentException("Delete: write protected: " + reportFile.getPath());
				}
				boolean success = reportFile.delete();
			}
		}

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBReports.T_REPORTS + " WHERE " + cDBReports.f_PARENT_MATRIXID + "=" + opId;
		dBManager.executeStatement(statement);
	}
	//</editor-fold>
}

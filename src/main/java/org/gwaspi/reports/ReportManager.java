package org.gwaspi.reports;

import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.IOException;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.model.Operation;
import org.gwaspi.model.Report;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.netCDF.operations.OperationMetadata;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class ReportManager {

	//<editor-fold defaultstate="collapsed" desc="UTILS">
	static LinkedHashMap getSortedMarkerSetByDoubleValue(LinkedHashMap map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		// logger.info(list);
		LinkedHashMap result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	static LinkedHashMap getSortedDescendingMarkerSetByDoubleValue(LinkedHashMap map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return -1 * ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		// logger.info(list);
		LinkedHashMap result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
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
		prefix.append(op.getOperationId());

		//Get Genotype Freq. assigned name. Pry out the part inserted by user only
		try {
			if (op.getOperationType().equals(org.gwaspi.constants.cNetCDF.Defaults.OPType.ALLELICTEST.toString())
					|| op.getOperationType().equals(org.gwaspi.constants.cNetCDF.Defaults.OPType.GENOTYPICTEST.toString())
					|| op.getOperationType().equals(org.gwaspi.constants.cNetCDF.Defaults.OPType.TRENDTEST.toString())) {
				Operation parentOp = new Operation(op.getParentOperationId());
				String[] tmp = parentOp.getOperationFriendlyName().split("-", 2);
				tmp = tmp[1].split("using");
				prefix.append("_");
				prefix.append(org.gwaspi.global.Utils.stripNonAlphaNumericDashUndscr(tmp[0].trim()));
				prefix.append("_");
			}

		} catch (IOException ex) {
		}

		return prefix.toString();
	}

	public static String createReportsMetadataTable(DbManager db) {
		boolean result = false;
		try {
			//CREATE SAMPLESET_METADATA table in given SCHEMA
			db.createTable(org.gwaspi.constants.cDBGWASpi.SCH_MATRICES,
					org.gwaspi.constants.cDBReports.T_REPORTS,
					org.gwaspi.constants.cDBReports.T_CREATE_REPORTS);

		} catch (Exception e) {
			System.out.println("Error creating management database");
			System.out.print(e);
			e.printStackTrace();
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

		dBManager.insertValuesInTable(org.gwaspi.constants.cDBGWASpi.SCH_MATRICES,
				org.gwaspi.constants.cDBReports.T_REPORTS,
				org.gwaspi.constants.cDBReports.F_INSERT_REPORT,
				rpMetaData);

	}

	public static void deleteReportByMatrixId(int matrixId) throws IOException {
		MatrixMetadata matrixMetadata = new MatrixMetadata(matrixId);
		org.gwaspi.model.ReportsList reportsList = new org.gwaspi.model.ReportsList(Integer.MIN_VALUE, matrixId);
		String reportsFolder = org.gwaspi.global.Config.getConfigValue("ReportsDir", "");

		for (Report rp : reportsList.reportsListAL) {
			File reportFile = new File(reportsFolder + "/STUDY_" + matrixMetadata.getStudyId() + "/" + rp.getReportFileName());

			if (reportFile.exists()) {
				if (!reportFile.canWrite()) {
					throw new IllegalArgumentException("Delete: write protected: " + reportFile.getPath());
				}
				boolean success = reportFile.delete();
			}
		}

		DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
		String statement = "DELETE FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBReports.T_REPORTS + " WHERE " + org.gwaspi.constants.cDBReports.f_PARENT_MATRIXID + "=" + matrixId;
		dBManager.executeStatement(statement);
	}

	public static void deleteReportByOperationId(int opId) throws IOException {
		OperationMetadata operationMetadata = new OperationMetadata(opId);
		org.gwaspi.model.ReportsList reportsList = new org.gwaspi.model.ReportsList(opId, operationMetadata.getParentMatrixId());
		String reportsFolder = org.gwaspi.global.Config.getConfigValue("ReportsDir", "");

		for (Report rp : reportsList.reportsListAL) {
			File reportFile = new File(reportsFolder + "/STUDY_" + operationMetadata.getStudyId() + "/" + rp.getReportFileName());

			if (reportFile.exists()) {
				if (!reportFile.canWrite()) {
					throw new IllegalArgumentException("Delete: write protected: " + reportFile.getPath());
				}
				boolean success = reportFile.delete();
			}
		}

		DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
		String statement = "DELETE FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBReports.T_REPORTS + " WHERE " + org.gwaspi.constants.cDBReports.f_PARENT_MATRIXID + "=" + opId;
		dBManager.executeStatement(statement);
	}
	//</editor-fold>
}

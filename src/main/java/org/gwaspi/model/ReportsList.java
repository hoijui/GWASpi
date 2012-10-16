package org.gwaspi.model;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBReports;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.dao.ReportService;
import org.gwaspi.dao.sql.ReportServiceImpl;
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
import org.gwaspi.netCDF.operations.OperationMetadata;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated use ReportService directly
 */
public class ReportsList {

	private static final ReportService reportService = new ReportServiceImpl();

	private ReportsList() {
	}

	public static Report getById(int reportId) throws IOException {
		return reportService.getById(reportId);
	}

	public static List<Report> getReportsList(int opId, int matrixId) throws IOException {
		return reportService.getReportsList(opId, matrixId);
	}

	public static Map<String, Object> getSortedMarkerSetByDoubleValue(Map<String, Object> map) {
		return reportService.getSortedMarkerSetByDoubleValue(map);
	}

	public static Map<String, Object> getSortedDescendingMarkerSetByDoubleValue(Map<String, Object> map) {
		return reportService.getSortedDescendingMarkerSetByDoubleValue(map);
	}

	public static String getReportNamePrefix(Operation op) {
		return reportService.getReportNamePrefix(op);
	}

	public static String createReportsMetadataTable(DbManager db) {
		return reportService.createReportsMetadataTable(db);
	}

	public static void insertRPMetadata(
			DbManager dBManager,
			String reportName,
			String fileName,
			String RPType,
			int parentMatrixId,
			int parentOPId,
			String description,
			int studyId)
	{
		reportService.insertRPMetadata(
				dBManager,
				reportName,
				fileName,
				RPType,
				parentMatrixId,
				parentOPId,
				description,
				studyId);
	}

	public static void deleteReportByMatrixId(int matrixId) throws IOException {
		reportService.deleteReportByMatrixId(matrixId);
	}

	public static void deleteReportByOperationId(int opId) throws IOException {
		reportService.deleteReportByOperationId(opId);
	}
}

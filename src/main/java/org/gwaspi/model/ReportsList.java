package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import org.gwaspi.dao.ReportService;
import org.gwaspi.dao.jpa.JPAReportService;
import org.gwaspi.dao.sql.ReportServiceImpl;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated use ReportService directly
 */
public class ReportsList {

	private static final ReportService reportService
			= MatricesList.USE_JPA
			? new JPAReportService(StudyList.getEntityManagerFactory())
			: new ReportServiceImpl();

	private ReportsList() {
	}

	public static Report getById(int reportId) throws IOException {
		return reportService.getById(reportId);
	}

	public static List<Report> getReportsList(int opId, int matrixId) throws IOException {
		return reportService.getReportsList(opId, matrixId);
	}

	public static String getReportNamePrefix(Operation op) {
		return reportService.getReportNamePrefix(op);
	}

	public static String createReportsMetadataTable() {
		return reportService.createReportsMetadataTable();
	}

	public static void insertRPMetadata(Report report) throws IOException {
		reportService.insertRPMetadata(report);
	}

	public static void deleteReportByMatrixId(int matrixId) throws IOException {
		reportService.deleteReportByMatrixId(matrixId);
	}

	public static void deleteReportByOperationId(int opId) throws IOException {
		reportService.deleteReportByOperationId(opId);
	}
}

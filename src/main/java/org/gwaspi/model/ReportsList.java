package org.gwaspi.model;

import org.gwaspi.dao.ReportService;
import org.gwaspi.dao.sql.ReportServiceImpl;
import java.io.IOException;
import java.util.List;

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
}

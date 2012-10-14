package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Report;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public interface ReportService {

	Report getById(int reportId) throws IOException;

	List<Report> getReportsList(int opId, int matrixId) throws IOException;

	List<Map<String, Object>> getReportListByOperationId(int opId) throws IOException;

	List<Map<String, Object>> getReportListByMatrixId(int matrixId) throws IOException;

	Object[][] getReportsTable(int opId) throws IOException;
}

package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Operation;
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

	Map<String, Object> getSortedMarkerSetByDoubleValue(Map<String, Object> map);

	Map<String, Object> getSortedDescendingMarkerSetByDoubleValue(Map<String, Object> map);

	String getReportNamePrefix(Operation op);

	String createReportsMetadataTable();

	void insertRPMetadata(Report report) throws IOException;

	void deleteReportByMatrixId(int matrixId) throws IOException;

	void deleteReportByOperationId(int opId) throws IOException;
}

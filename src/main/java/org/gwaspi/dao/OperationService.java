package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;

public interface OperationService {

	Operation getById(int operationId) throws IOException;

	List<Operation> getOperationsList(int matrixId) throws IOException;

	List<Operation> getOperationsList(int matrixId, int parentOpId) throws IOException;

	List<Operation> getOperationsList(int matrixId, int parentOpId, OPType opType) throws IOException;

	Object[][] getOperationsTable(int matrixId) throws IOException;

	Object[][] getOperationsTable(int matrixId, int opId) throws IOException;

	int getIdOfLastOperationTypeOccurance(List<Operation> operationsList, OPType opType);

	String createOperationsMetadataTable();

	void insertOPMetadata(
			int parentMatrixId,
			int parentOperationId,
			String friendlyName,
			String resultOPName,
			String type,
			String command,
			String description,
			Integer studyId)
			throws IOException;

	List<Object[]> getMatrixOperations(int matrixId) throws IOException;

	void deleteOperationBranch(int studyId, int opId, boolean deleteReports) throws IOException;

	OperationMetadata getOperationMetadata(int opId) throws IOException;

	OperationMetadata getOperationMetadata(String netCDFname) throws IOException;
}

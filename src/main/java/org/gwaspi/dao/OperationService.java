package org.gwaspi.dao;

import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import java.io.IOException;
import java.util.List;
import org.gwaspi.model.Operation;

public interface OperationService {

	Operation getById(int operationId) throws IOException;

	List<Operation> getOperationsList(int matrixId) throws IOException;

	List<Operation> getOperationsList(int matrixId, int parentOpId) throws IOException;

	List<Operation> getOperationsList(int matrixId, int parentOpId, OPType opType) throws IOException;

	Object[][] getOperationsTable(int matrixId) throws IOException;

	Object[][] getOperationsTable(int matrixId, int opId) throws IOException;

	int getIdOfLastOperationTypeOccurance(List<Operation> operationsList, OPType opType);
}

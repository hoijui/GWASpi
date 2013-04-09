package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;

public interface OperationService {

	public static class MatrixOperationSpec {

		private final Integer id;
		private final OPType type;

		public MatrixOperationSpec(Integer id, OPType type) {

			this.id = id;
			this.type = type;
		}

		public Integer getId() {
			return id;
		}

		public OPType geType() {
			return type;
		}
	}

	Operation getById(int operationId) throws IOException;

	List<Operation> getOperationsList(int matrixId) throws IOException;

	List<Operation> getOperationsList(int matrixId, int parentOpId) throws IOException;

	List<Operation> getOperationsList(int matrixId, int parentOpId, OPType opType) throws IOException;

	List<OperationMetadata> getOperationsTable(int matrixId) throws IOException;

	List<OperationMetadata> getOperationsTable(int matrixId, int opId) throws IOException;

	int getIdOfLastOperationTypeOccurance(List<Operation> operationsList, OPType opType);

	String createOperationsMetadataTable();

	void insertOPMetadata(OperationMetadata operationMetadata) throws IOException;

	List<MatrixOperationSpec> getMatrixOperations(int matrixId) throws IOException;

	void deleteOperationBranch(int studyId, int opId, boolean deleteReports) throws IOException;

	OperationMetadata getOperationMetadata(int opId) throws IOException;

	OperationMetadata getOperationMetadata(String netCDFname) throws IOException;
}

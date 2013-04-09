package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.OperationService;
import org.gwaspi.dao.OperationService.MatrixOperationSpec;
import org.gwaspi.dao.sql.OperationServiceImpl;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated use OperationService directly
 */
public class OperationsList {

	private static final OperationService operationService = new OperationServiceImpl();

	private OperationsList() {
	}

	public static Operation getById(int operationId) throws IOException {
		return operationService.getById(operationId);
	}

	public static List<Operation> getOperationsList(int matrixId) throws IOException {
		return operationService.getOperationsList(matrixId);
	}

	public static List<Operation> getOperationsList(int matrixId, int parentOpId) throws IOException {
		return operationService.getOperationsList(matrixId, parentOpId);
	}

	public static List<Operation> getOperationsList(int matrixId, int parentOpId, OPType opType) throws IOException {
		return operationService.getOperationsList(matrixId, parentOpId, opType);
	}

	public static List<OperationMetadata> getOperationsTable(int matrixId) throws IOException {
		return operationService.getOperationsTable(matrixId);
	}

	public static List<OperationMetadata> getOperationsTable(int matrixId, int opId) throws IOException {
		return operationService.getOperationsTable(matrixId, opId);
	}

	public static int getIdOfLastOperationTypeOccurance(List<Operation> operationsList, OPType opType) {
		return operationService.getIdOfLastOperationTypeOccurance(operationsList, opType);
	}

	public static String createOperationsMetadataTable() {
		return operationService.createOperationsMetadataTable();
	}

	public static void insertOPMetadata(OperationMetadata operationMetadata) throws IOException {
		operationService.insertOPMetadata(operationMetadata);
	}

	public static List<MatrixOperationSpec> getMatrixOperations(int matrixId) throws IOException {
		return operationService.getMatrixOperations(matrixId);
	}

	public static void deleteOperationBranch(int studyId, int opId, boolean deleteReports) throws IOException {
		operationService.deleteOperationBranch(studyId, opId, deleteReports);
	}

	public static OperationMetadata getOperationMetadata(int opId) throws IOException {
		return operationService.getOperationMetadata(opId);
	}
	public static OperationMetadata getOperationMetadata(String netCDFname) throws IOException {
		return operationService.getOperationMetadata(netCDFname);
	}
}

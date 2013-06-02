/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.OperationService;
import org.gwaspi.dao.jpa.JPAOperationService;

/**
 * @deprecated use OperationService directly
 */
public class OperationsList {

	private static final OperationService operationService
			= new JPAOperationService(StudyList.getEntityManagerFactory());

	private OperationsList() {
	}

	public static OperationMetadata getById(int operationId) throws IOException {
		return operationService.getById(operationId);
	}

	public static List<OperationMetadata> getOperationsList(int parentMatrixId) throws IOException {
		return operationService.getOperationsTable(parentMatrixId);
	}

	public static List<OperationMetadata> getOperationsList(int parentMatrixId, int parentOpId) throws IOException {
		return operationService.getOperationsList(parentMatrixId, parentOpId);
	}

	public static List<OperationMetadata> getOperationsList(int parentMatrixId, int parentOpId, OPType opType) throws IOException {
		return operationService.getOperationsList(parentMatrixId, parentOpId, opType);
	}

	public static List<OperationMetadata> getOperationsTable(int parentMatrixId) throws IOException {
		return operationService.getOperationsTable(parentMatrixId);
	}

	public static List<OperationMetadata> getOperationsTable(int parentMatrixId, int opId) throws IOException {
		return operationService.getOperationsTable(parentMatrixId, opId);
	}

	public static int getIdOfLastOperationTypeOccurance(List<OperationMetadata> operations, OPType opType) {
		int result = Integer.MIN_VALUE;
		for (int i = 0; i < operations.size(); i++) {
			if (operations.get(i).getOperationType().equals(OPType.MARKER_QA)) {
				result = operations.get(i).getId();
			}
		}
		return result;
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

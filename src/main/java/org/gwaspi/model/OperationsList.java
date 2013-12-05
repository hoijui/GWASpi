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
		return operationService.getOperation(operationId);
	}

	public static OperationMetadata getOperation(OperationKey operationKey) throws IOException {
		return operationService.getOperation(operationKey);
	}

	public static List<OperationMetadata> getOperationsList(MatrixKey parentMatrixKey) throws IOException {
		return operationService.getOperations(parentMatrixKey);
	}

	public static List<OperationMetadata> getOperationsList(int parentMatrixId, int parentOpId) throws IOException {
		return operationService.getOperations(parentMatrixId, parentOpId);
	}

	public static List<OperationMetadata> getOperationsList(int parentMatrixId, int parentOpId, OPType opType) throws IOException {
		return operationService.getOperations(parentMatrixId, parentOpId, opType);
	}

	public static List<OperationMetadata> getOperationsTable(MatrixKey parentMatrixKey) throws IOException {
		return operationService.getOperations(parentMatrixKey);
	}

	public static List<OperationMetadata> getOperationAndSubOperations(OperationKey operationKey) throws IOException {
		return operationService.getOperationAndSubOperations(operationKey);
	}

	public static OperationKey getIdOfLastOperationTypeOccurance(List<OperationMetadata> operations, OPType opType) {
		OperationKey result = null;
		for (int i = 0; i < operations.size(); i++) {
			if (operations.get(i).getOperationType().equals(OPType.MARKER_QA)) {
				result = OperationKey.valueOf(operations.get(i));
			}
		}
		return result;
	}

	public static OperationKey insertOPMetadata(OperationMetadata operationMetadata) throws IOException {
		return operationService.insertOperation(operationMetadata);
	}

	public static void deleteOperationBranch(OperationKey operationKey, boolean deleteReports) throws IOException {
		operationService.deleteOperation(operationKey, deleteReports);
	}

	public static OperationMetadata getOperationMetadata(int opId) throws IOException {
		return operationService.getOperation(opId);
	}

	public static List<OperationKey> getOperationKeysByName(String operationFriendlyName) throws IOException {
		return operationService.getOperationKeysByName(operationFriendlyName);
	}
}

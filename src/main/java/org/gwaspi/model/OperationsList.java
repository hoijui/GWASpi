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

	/**
	 * @see OperationService#getOperationMetadata(OperationKey)
	 */
	public static OperationMetadata getOperationMetadata(OperationKey operationKey) throws IOException {
		return operationService.getOperationMetadata(operationKey);
	}

	/**
	 * @see OperationService#getOperationKeysByName(StudyKey, String)
	 */
	public static List<OperationKey> getOperationKeysByName(StudyKey studyKey, String operationFriendlyName) throws IOException {
		return operationService.getOperationKeysByName(studyKey, operationFriendlyName);
	}

	/**
	 * @see OperationService#getOffspringOperationsMetadata(MatrixKey)
	 */
	public static List<OperationMetadata> getOffspringOperationsMetadata(MatrixKey origin) throws IOException {
		return operationService.getOffspringOperationsMetadata(origin);
	}

	/**
	 * @see OperationService#getOffspringOperationsMetadata(MatrixKey, OPType)
	 */
	public static List<OperationMetadata> getOffspringOperationsMetadata(MatrixKey origin, OPType opType) throws IOException {
		return operationService.getOffspringOperationsMetadata(origin, opType);
	}

	/**
	 * @see OperationService#getChildrenOperationsMetadata(OperationKey)
	 */
	public static List<OperationMetadata> getChildrenOperationsMetadata(OperationKey parent) throws IOException {
		return operationService.getChildrenOperationsMetadata(parent);
	}

	/**
	 * @see OperationService#getChildrenOperationsMetadata(OperationKey, OPType)
	 */
	public static List<OperationMetadata> getChildrenOperationsMetadata(OperationKey parent, OPType opType) throws IOException {
		return operationService.getChildrenOperationsMetadata(parent, opType);
	}

	/**
	 * @see OperationService#getChildrenOperationsMetadata(DataSetKey)
	 */
	public static List<OperationMetadata> getChildrenOperationsMetadata(DataSetKey parent) throws IOException {
		return operationService.getChildrenOperationsMetadata(parent);
	}

	/**
	 * @see OperationService#getChildrenOperationsMetadata(DataSetKey, OPType)
	 */
	public static List<OperationMetadata> getChildrenOperationsMetadata(DataSetKey parent, OPType opType) throws IOException {
		return operationService.getChildrenOperationsMetadata(parent, opType);
	}

	/**
	 * @see OperationService#getSelfAndOffspringOperationsMetadata(OperationKey)
	 */
	public static List<OperationMetadata> getSelfAndOffspringOperationsMetadata(OperationKey rootOperationKey) throws IOException {
		return operationService.getSelfAndOffspringOperationsMetadata(rootOperationKey);
	}

	public static OperationKey getIdOfLastOperationTypeOccurance(List<OperationMetadata> operations, OPType opType) {

		OperationKey result = null;

		for (int i = 0; i < operations.size(); i++) {
			if (operations.get(i).getOperationType().equals(opType)) {
				result = OperationKey.valueOf(operations.get(i));
			}
		}

		return result;
	}

	/**
	 * @see OperationService#getAncestorOperationTypes(OperationKey)
	 */
	public static List<OPType> getAncestorOperationTypes(OperationKey operationKey) throws IOException {
		return operationService.getAncestorOperationTypes(operationKey);
	}

	/**
	 * @see OperationService#insertOperation(OperationMetadata)
	 */
	public static OperationKey insertOperation(OperationMetadata operationMetadata) throws IOException {
		return operationService.insertOperation(operationMetadata);
	}

	/**
	 * @see OperationService#deleteOperationBranch(OperationKey, boolean)
	 */
	public static void deleteOperation(OperationKey operationKey, boolean deleteReports) throws IOException {
		operationService.deleteOperation(operationKey, deleteReports);
	}
}

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
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.OperationService;
import org.gwaspi.dao.jpa.JPAOperationService;

/**
 * @deprecated use OperationService directly
 */
public class OperationsList {

	private static OperationService operationService = null;

	private OperationsList() {
	}

	static void clearInternalService() {
		operationService = null;
	}

	private static OperationService getOperationService() {

		if (operationService == null) {
			operationService = new JPAOperationService(StudyList.getEntityManagerFactory());
		}

		return operationService;
	}

	/**
	 * @see OperationService#getOperationMetadata(OperationKey)
	 */
	public static OperationMetadata getOperationMetadata(OperationKey operationKey) throws IOException {
		return getOperationService().getOperationMetadata(operationKey);
	}

	/**
	 * @see OperationService#getOperationKeysByName(StudyKey, String)
	 */
	public static List<OperationKey> getOperationKeysByName(StudyKey studyKey, String operationFriendlyName) throws IOException {
		return getOperationService().getOperationKeysByName(studyKey, operationFriendlyName);
	}

	/**
	 * @see OperationService#getOffspringOperationsMetadata(DataSetKey)
	 */
	public static List<OperationMetadata> getOffspringOperationsMetadata(MatrixKey root) throws IOException {
		return getOperationService().getOffspringOperationsMetadata(new DataSetKey(root));
	}

	/**
	 * @see OperationService#getOffspringOperationsMetadata(DataSetKey, OPType)
	 */
	public static List<OperationMetadata> getOffspringOperationsMetadata(MatrixKey root, OPType opType) throws IOException {
		return getOperationService().getOffspringOperationsMetadata(new DataSetKey(root), opType);
	}

	/**
	 * @see OperationService#getOffspringOperationsMetadata(DataSetKey)
	 */
	public static List<OperationMetadata> getOffspringOperationsMetadata(DataSetKey root) throws IOException {
		return getOperationService().getOffspringOperationsMetadata(root);
	}

	/**
	 * @see OperationService#getOffspringOperationsMetadata(DataSetKey, OPType)
	 */
	public static List<OperationMetadata> getOffspringOperationsMetadata(DataSetKey root, OPType opType) throws IOException {
		return getOperationService().getOffspringOperationsMetadata(root, opType);
	}

	/**
	 * @see OperationService#getChildrenOperationsMetadata(DataSetKey)
	 */
	public static List<OperationMetadata> getChildrenOperationsMetadata(OperationKey parent) throws IOException {
		return getOperationService().getChildrenOperationsMetadata(new DataSetKey(parent));
	}

	/**
	 * @see OperationService#getChildrenOperationsMetadata(DataSetKey, OPType)
	 */
	public static List<OperationMetadata> getChildrenOperationsMetadata(OperationKey parent, OPType opType) throws IOException {
		return getOperationService().getChildrenOperationsMetadata(new DataSetKey(parent), opType);
	}

	/**
	 * @see OperationService#getChildrenOperationsMetadata(DataSetKey)
	 */
	public static List<OperationMetadata> getChildrenOperationsMetadata(DataSetKey parent) throws IOException {
		return getOperationService().getChildrenOperationsMetadata(parent);
	}

	/**
	 * @see OperationService#getChildrenOperationsMetadata(DataSetKey, OPType)
	 */
	public static List<OperationMetadata> getChildrenOperationsMetadata(DataSetKey parent, OPType opType) throws IOException {
		return getOperationService().getChildrenOperationsMetadata(parent, opType);
	}

	/**
	 * @see OperationService#getSelfAndOffspringOperationsMetadata(OperationKey)
	 */
	public static List<OperationMetadata> getSelfAndOffspringOperationsMetadata(OperationKey root) throws IOException {

		final List<OperationMetadata> offspring = getOffspringOperationsMetadata(new DataSetKey(root));
		final List<OperationMetadata> selfAndOffspring = new ArrayList<OperationMetadata>(1 + offspring.size());
		selfAndOffspring.add(getOperationMetadata(root));
		selfAndOffspring.addAll(offspring);

		return selfAndOffspring;
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
		return getOperationService().getAncestorOperationTypes(operationKey);
	}

	/**
	 * @see OperationService#insertOperation(OperationMetadata)
	 */
	public static OperationKey insertOperation(OperationMetadata operationMetadata) throws IOException {
		return getOperationService().insertOperation(operationMetadata);
	}

	/**
	 * @see OperationService#deleteOperationBranch(OperationKey, boolean)
	 */
	public static void deleteOperation(OperationKey operationKey, boolean deleteReports) throws IOException {
		getOperationService().deleteOperation(operationKey, deleteReports);
	}
}

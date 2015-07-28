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
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.dao.OperationService;
import org.gwaspi.dao.jpa.JPAOperationService;

public class OperationsList {

	private static OperationService operationService = null;

	private OperationsList() {
	}

	static void clearInternalService() {
		operationService = null;
	}

	public static OperationService getOperationService() {

		if (operationService == null) {
			operationService = new JPAOperationService(StudyList.getEntityManagerFactory());
		}

		return operationService;
	}

	/**
	 * @see OperationService#getSelfAndOffspringOperationsMetadata(OperationKey)
	 */
	public static List<OperationMetadata> getSelfAndOffspringOperationsMetadata(OperationKey root) throws IOException {

		final List<OperationMetadata> offspring = getOperationService().getOffspringOperationsMetadata(new DataSetKey(root));
		final List<OperationMetadata> selfAndOffspring = new ArrayList<OperationMetadata>(1 + offspring.size());
		selfAndOffspring.add(getOperationService().getOperationMetadata(root));
		selfAndOffspring.addAll(offspring);

		return selfAndOffspring;
	}

	public static OperationKey getIdOfLastOperationTypeOccurance(List<OperationMetadata> operations, OPType opType, int numMarkers) {

		OperationKey result = null;

		for (OperationMetadata operation : operations) {
			if (operation.getOperationType().equals(opType)
					&& (operation.getNumMarkers() == numMarkers))
			{
				result = OperationKey.valueOf(operation);
			}
		}

		return result;
	}

	public static List<OperationMetadata> getFilteredOffspring(
			final DataSetKey parentKey,
			final List<OPType> validTypes,
			final int numMarkers)
			throws IOException
	{
		final List<OperationMetadata> filteredOperations = new ArrayList<OperationMetadata>();

		for (final OPType validType : validTypes) {
			List<OperationMetadata> validTypeOperations = getOperationService().getOffspringOperationsMetadata(parentKey, validType);

			for (OperationMetadata validTypeOperation : validTypeOperations) {
				if (validTypeOperation.getNumMarkers() == numMarkers) {
					filteredOperations.add(validTypeOperation);
				}
			}
		}

		return filteredOperations;
	}
}

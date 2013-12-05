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

package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;

public interface OperationService {

	OperationMetadata getOperation(int operationId) throws IOException;

	OperationMetadata getOperation(OperationKey operationKey) throws IOException;

	List<OperationKey> getOperationKeysByName(String operationFriendlyName) throws IOException;

	List<OperationMetadata> getOperations(int parentMatrixId, int parentOpId) throws IOException;

	List<OperationMetadata> getOperations(int parentMatrixId, int parentOpId, OPType opType) throws IOException;

	List<OperationMetadata> getOperations(MatrixKey parentMatrixKey) throws IOException;

	List<OperationMetadata> getOperationAndSubOperations(OperationKey operationKey) throws IOException;

	OperationKey insertOperation(OperationMetadata operationMetadata) throws IOException;

	void deleteOperation(OperationKey operationKey, boolean deleteReports) throws IOException;
}

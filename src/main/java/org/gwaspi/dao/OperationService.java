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
import org.gwaspi.model.StudyKey;

public interface OperationService {

	OperationMetadata getOperation(OperationKey operationKey) throws IOException;

	List<OperationKey> getOperationKeysByName(StudyKey studyKey, String operationFriendlyName) throws IOException;

	/**
	 * Returns operations that have the given origin.
	 * @param origin
	 * @return
	 * @throws IOException
	 */
	List<OperationMetadata> getOperations(MatrixKey origin) throws IOException;

	/**
	 * Returns operations that have the given origin and are of the given type.
	 * @param origin
	 * @param opType
	 * @return
	 * @throws IOException
	 */
	List<OperationMetadata> getOperations(MatrixKey origin, OPType opType) throws IOException;

	/**
	 * Returns operations that have the given parent.
	 * @param parent
	 * @return
	 * @throws IOException
	 */
	List<OperationMetadata> getOperations(OperationKey parent) throws IOException;

	/**
	 * Returns operations that have the given parent and are of the given type.
	 * @param origin
	 * @param parent
	 * @param opType
	 * @return
	 * @throws IOException
	 */
	List<OperationMetadata> getOperations(OperationKey parent, OPType opType) throws IOException;

	List<OperationMetadata> getOperationAndSubOperations(OperationKey rootOperationKey) throws IOException;

	OperationKey insertOperation(OperationMetadata operationMetadata) throws IOException;

	void deleteOperation(OperationKey operationKey, boolean deleteReports) throws IOException;
}

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
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.StudyKey;

public interface OperationService {

	OperationMetadata getOperationMetadata(OperationKey operationKey) throws IOException;

	List<OperationKey> getOperationKeysByName(StudyKey studyKey, String operationFriendlyName) throws IOException;

	/**
	 * Returns operations that have the given root in their ancestry.
	 * @param root
	 * @return
	 * @throws IOException
	 */
	List<OperationMetadata> getOffspringOperationsMetadata(DataSetKey root) throws IOException;

	/**
	 * Returns operations that have the given root in their ancestry,
	 * are not hidden, and have no hidden ancestor.
	 * @param root
	 * @return
	 * @throws IOException
	 */
	List<OperationMetadata> getVisibleOffspringOperationsMetadata(DataSetKey root) throws IOException;

	/**
	 * Returns operations that have the given root in their ancestry
	 * and are of the given type.
	 * @param root
	 * @param opType
	 * @return
	 * @throws IOException
	 */
	List<OperationMetadata> getOffspringOperationsMetadata(DataSetKey root, OPType opType) throws IOException;

	/**
	 * Returns operations that have the given parent.
	 * @param parent
	 * @return
	 * @throws IOException
	 */
	List<OperationMetadata> getChildrenOperationsMetadata(DataSetKey parent) throws IOException;

	/**
	 * Returns operations that have the given parent and are of the given type.
	 * @param parent
	 * @param opType
	 * @return
	 * @throws IOException
	 */
	List<OperationMetadata> getChildrenOperationsMetadata(DataSetKey parent, OPType opType) throws IOException;

	/**
	 * Returns all the ancestor operations types,
	 * starting with the operation its self, then the direct parent,
	 * then the grand-parent, etc.
	 * @param operationKey
	 * @return
	 * @throws IOException
	 */
	List<OPType> getAncestorOperationTypes(OperationKey operationKey) throws IOException;

	OperationKey insertOperation(OperationMetadata operationMetadata) throws IOException;

	void deleteOperation(OperationKey operationKey, boolean deleteReports) throws IOException;
}

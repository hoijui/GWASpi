/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.operations;

import java.util.Set;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;

public interface OperationParams {

	OPType getType();

	DataSetKey getParent();

	/**
	 * @return result operation name
	 */
	String getName();

	/**
	 * Returns a set of all matrices (excluding the newly created one, if any)
	 * that are participating in the process of this operation.
	 * This is mainly used for locking.
	 * @return
	 */
	Set<MatrixKey> getParticipatingMatrices();

	/**
	 * Indicates whether this operation is hidden.
	 * A hidden operation is not visible as a separate entry in the
	 * "Processes Overview", nor in the data-tree.
	 * This is used for quasi-sub-operations, especially if they come in masses.
	 * @return <code>true</code> if the operation is hidden
	 */
	boolean isHidden();
}

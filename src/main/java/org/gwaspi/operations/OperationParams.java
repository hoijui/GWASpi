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
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;

public interface OperationParams {

	DataSetKey getParent();

	/**
	 * @return result operation name
	 */
	String getName();

	/**
	 * Returns a set of all matrices (excluding the newly created one, if any)
	 * that are participating the the process of this operation.
	 * This is mainly used for locking.
	 * @return
	 */
	Set<MatrixKey> getParticipatingMatrices();
}

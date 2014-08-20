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

import org.gwaspi.model.DataSetKey;

public abstract class AbstractMatrixCreatingOperationParams implements MatrixCreatingOperationParams {

	private final DataSetKey parent;
//	private final DataSetDestination dataSetDestination;
	private final String matrixDescription;
	private final String matrixFriendlyName;

	protected AbstractMatrixCreatingOperationParams(
			DataSetKey parent,
//			DataSetDestination dataSetDestination,
			String matrixDescription,
			String matrixFriendlyName)
	{
		this.parent = parent;
//		this.dataSetDestination = dataSetDestination;
		this.matrixDescription = matrixDescription;
		this.matrixFriendlyName = matrixFriendlyName;
	}

	@Override
	public DataSetKey getParent() {
		return parent;
	}

//	@Override
//	public DataSetDestination getDataSetDestination() {
//		return dataSetDestination;
//	}

	@Override
	public String getMatrixDescription() {
		return matrixDescription;
	}

	@Override
	public String getMatrixFriendlyName() {
		return matrixFriendlyName;
	}
}

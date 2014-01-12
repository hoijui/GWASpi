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

package org.gwaspi.netCDF.operations;

import java.io.IOException;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.OperationDataSet;

public abstract class AbstractOperation<DST extends OperationDataSet> implements MatrixOperation {

	private final MatrixKey matrixParent;
	private final OperationKey operationParent;

	protected AbstractOperation(MatrixKey parent) {

		this.matrixParent = parent;
		this.operationParent = null;
	}

	protected AbstractOperation(OperationKey parent) {

		this.matrixParent = null;
		this.operationParent = parent;
	}

	public abstract OPType getType();

	protected DataSetSource getParentDataSetSource() throws IOException {

		final DataSetSource parentDataSetSource;
		if (matrixParent != null) {
			parentDataSetSource = MatrixFactory.generateMatrixDataSetSource(matrixParent);
		} else {
			parentDataSetSource = OperationFactory.generateOperationDataSet(operationParent);
		}

		return parentDataSetSource;
	}

	protected MatrixKey getParentMatrixKey() throws IOException {

		final MatrixKey parentMatrixKey;
		if (matrixParent != null) {
			parentMatrixKey = matrixParent;
		} else {
			parentMatrixKey = operationParent.getParentMatrixKey();
		}

		return parentMatrixKey;
	}

	protected MatrixMetadata getParentMatrixMetadata() throws IOException {

		final MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(getParentMatrixKey());

		return parentMatrixMetadata;
	}

	protected DST generateFreshOperationDataSet() throws IOException {

		final DST operationDataSet;
		if (matrixParent != null) {
			operationDataSet = (DST) OperationFactory.generateOperationDataSet(getType(), matrixParent);
		} else {
			operationDataSet = (DST) OperationFactory.generateOperationDataSet(getType(), operationParent);
		}

		return operationDataSet;
	}
}

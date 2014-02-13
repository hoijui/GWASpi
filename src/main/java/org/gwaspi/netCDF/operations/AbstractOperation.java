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
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.OperationParams;

public abstract class AbstractOperation<DST extends OperationDataSet, PT extends OperationParams> implements MatrixOperation<PT> {

	/** @params deprecated */
	private final DataSetKey parent;
	private final PT params;

	protected AbstractOperation(DataSetKey parent) {

		this.parent = parent;
		this.params = null;
	}

	protected AbstractOperation(MatrixKey parent) {

		this.parent = new DataSetKey(parent);
		this.params = null;
	}

	protected AbstractOperation(OperationKey parent) {

		this.parent = new DataSetKey(parent);
		this.params = null;
	}

	protected AbstractOperation(PT params) {

		this.parent = params.getParent();
		this.params = params;
	}

	@Override
	public PT getParams() {
		return params;
	}

	@Override
	public boolean isCreatingResultMatrix() {
		return false;
	}

	public abstract OPType getType();

	protected DataSetKey getParentKey() {
		return parent;
	}

	protected DataSetSource getParentDataSetSource() throws IOException {

		final DataSetSource parentDataSetSource;
		if (parent.isMatrix()) {
			parentDataSetSource = MatrixFactory.generateMatrixDataSetSource(parent.getMatrixParent());
		} else {
			parentDataSetSource = OperationFactory.generateOperationDataSet(parent.getOperationParent());
		}

		return parentDataSetSource;
	}

	protected MatrixKey getParentMatrixKey() throws IOException {

		final MatrixKey parentMatrixKey;
		if (parent.isMatrix()) {
			parentMatrixKey = parent.getMatrixParent();
		} else {
			parentMatrixKey = parent.getOperationParent().getParentMatrixKey();
		}

		return parentMatrixKey;
	}

	protected MatrixMetadata getParentMatrixMetadata() throws IOException {

		final MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(getParentMatrixKey());

		return parentMatrixMetadata;
	}

	protected DST generateFreshOperationDataSet() throws IOException {

		final DST operationDataSet = (DST) OperationFactory.generateOperationDataSet(getType(), getParentMatrixKey(), parent);

		return operationDataSet;
	}
}

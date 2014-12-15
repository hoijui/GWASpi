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

package org.gwaspi.operations;

import java.io.IOException;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.ProgressHandler;

public abstract class AbstractOperationCreatingOperation<DST extends OperationDataSet, PT extends OperationParams> extends AbstractOperation<PT> {

	/** @params deprecated */
	private final DataSetKey parent;
	private final PT params;
	private ProgressHandler operationPH;

	protected AbstractOperationCreatingOperation(DataSetKey parent) {

		this.parent = parent;
		this.params = null;
		this.operationPH = null;
	}

	protected AbstractOperationCreatingOperation(MatrixKey parent) {

		this.parent = new DataSetKey(parent);
		this.params = null;
		this.operationPH = null;
	}

	protected AbstractOperationCreatingOperation(OperationKey parent) {

		this.parent = new DataSetKey(parent);
		this.params = null;
		this.operationPH = null;
	}

	protected AbstractOperationCreatingOperation(PT params) {

		this.parent = params.getParent();
		this.params = params;
		this.operationPH = null;
	}

//	public abstract OperationMetadata createOperationMetadata(DST operationDataSet) throws IOException;


	public int getNumItems() throws IOException {

		final int numItems;

		final DataSetSource parentDataSetSource = getParentDataSetSource();
		if (OperationManager.getOperationTypeInfo(this.getClass()).isMarkersOriented()) {
			numItems = parentDataSetSource.getNumMarkers();
		} else {
			numItems = parentDataSetSource.getNumSamples();
		}

		return numItems;
	}

	protected ProgressHandler getProgressHandler() throws IOException {

		if (operationPH == null) {
			final int numItems = getNumItems();

			operationPH =  new IntegerProgressHandler(
					getProcessInfo(),
					0, // start state, first marker/sample
					numItems - 1); // end state, last marker/sample
		}

		return operationPH;
	}

	@Override
	public PT getParams() {
		return params;
	}

	protected DataSetKey getParentKey() {
		return parent;
	}

	protected DataSetSource getParentDataSetSource() throws IOException {

		final DataSetSource parentDataSetSource;
		if (parent.isMatrix()) {
			parentDataSetSource = MatrixFactory.generateMatrixDataSetSource(parent.getMatrixParent());
		} else {
			parentDataSetSource = OperationManager.generateOperationDataSet(parent.getOperationParent());
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

		final DST operationDataSet = (DST) OperationManager.generateOperationDataSet(getClass(), getParentMatrixKey(), parent, getParams());

		return operationDataSet;
	}

//	protected abstract ProgressSource createProgressSource() throws IOException;
//
//	@Override
//	public ProgressSource getProgressSource() throws IOException {
//
//		if (operationPH == null) {
//			operationPH = createProgressSource();
//		}
//	}
}

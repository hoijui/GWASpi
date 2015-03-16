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

import java.io.IOException;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.loader.AbstractDataSetDestination;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.loader.DataSetDestinationProgressHandler;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;

public abstract class AbstractMatrixCreatingOperation<P extends OperationParams>
		extends AbstractOperation<P, MatrixKey>
{
//	private final PT params; // TODO So far, only Operation creating operations take params, matrix creating ones do not, but maybe should?
	private ProgressHandler operationPH;

	public AbstractMatrixCreatingOperation(DataSetDestination dataSetDestination) {
		super(dataSetDestination);

		this.operationPH = null;
	}

	public AbstractMatrixCreatingOperation() {
		this(null);
	}

	@Override
	public ProgressSource getProgressSource() throws IOException {

		if (operationPH == null) {
			final DataSetDestinationProgressHandler dataSetDestinationProgressHandler
					= new DataSetDestinationProgressHandler(getProcessInfo());
//			dataSetDestinationProgressHandler.setDataSetDestination(getDataSetDestination());
			((AbstractDataSetDestination)getDataSetDestination()).setProgressHandler(dataSetDestinationProgressHandler);
			operationPH = dataSetDestinationProgressHandler;
		}

		return operationPH;
	}
}

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

package org.gwaspi.threadbox;

import java.io.IOException;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.operations.MatrixOperation;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.gwaspi.netCDF.operations.OperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractThreaded_MergeMatrices extends CommonRunnable {

	protected final MatrixKey parentMatrixKey1;
	protected final MatrixKey parentMatrixKey2;
	protected final String newMatrixName;
	protected final String description;

	public AbstractThreaded_MergeMatrices(
			MatrixKey parentMatrixKey1,
			MatrixKey parentMatrixKey2,
			String newMatrixName,
			String description)
	{
		super(
				"Merge Matrices",
				"Merging Data",
				"Merge Matrices: " + newMatrixName,
				"Merging Matrices");

		this.parentMatrixKey1 = parentMatrixKey1;
		this.parentMatrixKey2 = parentMatrixKey2;
		this.newMatrixName = newMatrixName;
		this.description = description;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(AbstractThreaded_MergeMatrices.class);
	}

	protected abstract AbstractNetCDFDataSetDestination createMatrixDataSetDestination(
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2)
			throws IOException;

	protected abstract MatrixOperation createMatrixOperation(
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2,
			AbstractNetCDFDataSetDestination dataSetDestination)
			throws IOException;

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			final DataSetSource dataSetSource1 = MatrixFactory.generateMatrixDataSetSource(parentMatrixKey1);
			final DataSetSource dataSetSource2 = MatrixFactory.generateMatrixDataSetSource(parentMatrixKey2);

			final AbstractNetCDFDataSetDestination dataSetDestination = createMatrixDataSetDestination(dataSetSource1, dataSetSource2);
			final MatrixOperation joinMatrices = createMatrixOperation(dataSetSource1, dataSetSource2, dataSetDestination);

			joinMatrices.processMatrix();

			final MatrixKey resultMatrixKey = dataSetDestination.getResultMatrixKey();
			GWASpiExplorerNodes.insertMatrixNode(resultMatrixKey);

			if (!thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				return;
			}
			OperationManager.performQASamplesOperationAndCreateReports(new OP_QASamples(resultMatrixKey));

			if (!thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				return;
			}
			OperationManager.performQAMarkersOperationAndCreateReports(new OP_QAMarkers(resultMatrixKey));
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

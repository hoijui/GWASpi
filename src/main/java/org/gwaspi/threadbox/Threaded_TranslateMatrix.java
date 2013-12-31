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

import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.operations.MatrixTranslator;
import org.gwaspi.netCDF.operations.MatrixTranslatorNetCDFDataSetDestination;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.gwaspi.netCDF.operations.OperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_TranslateMatrix extends CommonRunnable {

	private final MatrixKey parentMatrixKey;
	private final String newMatrixName;
	private final String description;

	public Threaded_TranslateMatrix(
			MatrixKey parentMatrixKey,
			String newMatrixName,
			String description)
	{
		super(
				"Translate Matrix",
				"Translating Matrix",
				"Translate Matrix: " + newMatrixName,
				"Translating Matrix");

		this.parentMatrixKey = parentMatrixKey;
		this.newMatrixName = newMatrixName;
		this.description = description;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_TranslateMatrix.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			DataSetSource dataSetSource = MatrixFactory.generateMatrixDataSetSource(parentMatrixKey);
			AbstractNetCDFDataSetDestination dataSetDestination
					= new MatrixTranslatorNetCDFDataSetDestination(
					dataSetSource,
					newMatrixName,
					description);
			MatrixTranslator matrixTransformer = new MatrixTranslator(
					dataSetSource,
					dataSetDestination);

			matrixTransformer.processMatrix();
			MatrixKey resultMatrixKey = dataSetDestination.getResultMatrixKey();

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

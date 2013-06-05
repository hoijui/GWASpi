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

import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.operations.MatrixOperation;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
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

	protected abstract MatrixOperation createMatrixOperation() throws Exception;

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			final MatrixOperation joinMatrices = createMatrixOperation();

			final int resultMatrixId = joinMatrices.processMatrix();
			final MatrixKey resultMatrixKey = new MatrixKey(
					parentMatrixKey1.getStudyKey(),
					resultMatrixId);
			GWASpiExplorerNodes.insertMatrixNode(resultMatrixKey);

			if (!thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				return;
			}
			int sampleQAOpId = new OP_QASamples(resultMatrixKey).processMatrix();
			OperationKey sampleQAOpKey = new OperationKey(resultMatrixKey, sampleQAOpId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(sampleQAOpKey);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpKey, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpKey);

			if (!thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				return;
			}
			int markersQAOpId = new OP_QAMarkers(resultMatrixKey).processMatrix();
			OperationKey markersQAOpKey = new OperationKey(resultMatrixKey, markersQAOpId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(markersQAOpKey);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpKey);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpKey);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

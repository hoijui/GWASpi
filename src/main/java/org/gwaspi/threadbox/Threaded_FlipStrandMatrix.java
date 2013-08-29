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

import java.io.File;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.operations.MatrixGenotypesFlipper;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_FlipStrandMatrix extends CommonRunnable {

	private MatrixKey resultMatrixKey;
	private final MatrixKey parentMatrixKey;
	private final String newMatrixName;
	private final String description;
	private final String markerIdentifyer;
	private final File markerFlipFile;

	public Threaded_FlipStrandMatrix(
			MatrixKey parentMatrixKey,
			String newMatrixName,
			String description,
			String markerIdentifyer,
			File markerFlipFile)
	{
		super(
				"Flip Strand Matrix",
				"Flipping Genotypes",
				"Flip Strand Matrix ID: " + parentMatrixKey.getMatrixId(),
				"Extracting");

		this.parentMatrixKey = parentMatrixKey;
		this.newMatrixName = newMatrixName;
		this.description = description;
		this.markerIdentifyer = markerIdentifyer;
		this.markerFlipFile = markerFlipFile;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_FlipStrandMatrix.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			MatrixGenotypesFlipper flipMatrix = new MatrixGenotypesFlipper(
					parentMatrixKey,
					newMatrixName,
					description,
					markerIdentifyer,
					markerFlipFile);
			int resultMatrixId = flipMatrix.processMatrix();
			resultMatrixKey = new MatrixKey(parentMatrixKey.getStudyKey(), resultMatrixId);
			GWASpiExplorerNodes.insertMatrixNode(resultMatrixKey);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			int sampleQAOpId = new OP_QASamples(resultMatrixKey).processMatrix();
			OperationKey sampleQAOpKey = new OperationKey(resultMatrixKey, sampleQAOpId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(sampleQAOpKey);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpKey, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpKey);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			int markersQAOpId = new OP_QAMarkers(resultMatrixKey).processMatrix();
			OperationKey markersQAOpKey = new OperationKey(resultMatrixKey, markersQAOpId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(markersQAOpKey);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpKey);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpKey);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

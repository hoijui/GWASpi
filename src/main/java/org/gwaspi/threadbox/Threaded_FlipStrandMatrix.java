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
import org.gwaspi.netCDF.operations.MatrixGenotypesFlipper;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_FlipStrandMatrix extends CommonRunnable {

	private int resultMatrixId;
	private int studyId;
	private int parentMatrixId;
	private String newMatrixName;
	private String description;
	private String markerIdentifyer;
	private File markerFlipFile;

	public Threaded_FlipStrandMatrix(
			int studyId,
			int parentMatrixId,
			String newMatrixName,
			String description,
			String markerIdentifyer,
			File markerFlipFile)
	{
		super(
				"Flip Strand Matrix",
				"Flipping Genotypes",
				"Flip Strand Matrix ID: " + parentMatrixId,
				"Extracting");

		this.studyId = studyId;
		this.parentMatrixId = parentMatrixId;
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
			MatrixGenotypesFlipper flipMatrix = new MatrixGenotypesFlipper(studyId,
					parentMatrixId,
					newMatrixName,
					description,
					markerIdentifyer,
					markerFlipFile);
			resultMatrixId = flipMatrix.flipGenotypesToNewMatrix();
			GWASpiExplorerNodes.insertMatrixNode(studyId, resultMatrixId);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			int sampleQAOpId = new OP_QASamples(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, sampleQAOpId);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			int markersQAOpId = new OP_QAMarkers(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

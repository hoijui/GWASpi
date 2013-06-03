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

import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.operations.MatrixTranslator;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_TranslateMatrix extends CommonRunnable {

	private final StudyKey studyKey;
	private final int parentMatrixId;
	private final GenotypeEncoding gtEncoding;
	private final String newMatrixName;
	private final String description;

	public Threaded_TranslateMatrix(
			StudyKey studyKey,
			int parentMatrixId,
			GenotypeEncoding gtEncoding,
			String newMatrixName,
			String description)
	{
		super(
				"Translate Matrix",
				"Translating Matrix",
				"Translate Matrix: " + newMatrixName,
				"Translating Matrix");

		this.studyKey = studyKey;
		this.parentMatrixId = parentMatrixId;
		this.gtEncoding = gtEncoding;
		this.newMatrixName = newMatrixName;
		this.description = description;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_TranslateMatrix.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			MatrixTranslator matrixTransformer = new MatrixTranslator(
					studyKey,
					parentMatrixId,
					newMatrixName,
					description);

			int resultMatrixId;
			if (gtEncoding.equals(GenotypeEncoding.AB0)
					|| gtEncoding.equals(GenotypeEncoding.O12))
			{
				resultMatrixId = matrixTransformer.translateAB12AllelesToACGT();
			} else if (gtEncoding.equals(GenotypeEncoding.O1234))
			{
				resultMatrixId = matrixTransformer.translate1234AllelesToACGT();
			} else {
				throw new IllegalStateException("Invalid value for gtEncoding: " + gtEncoding);
			}

			GWASpiExplorerNodes.insertMatrixNode(studyKey, resultMatrixId);

			if (!thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				return;
			}
			int sampleQAOpId = new OP_QASamples(resultMatrixId).processMatrix();
			OperationKey sampleQAOpKey = OperationKey.valueOf(OperationsList.getById(sampleQAOpId));
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(sampleQAOpKey);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);

			if (!thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				return;
			}
			int markersQAOpId = new OP_QAMarkers(resultMatrixId).processMatrix();
			OperationKey markersQAOpKey = OperationKey.valueOf(OperationsList.getById(markersQAOpId));
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(markersQAOpKey);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

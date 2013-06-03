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

import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.gwaspi.netCDF.operations.OperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_MatrixQA extends CommonRunnable {

	private final int matrixId;

	public Threaded_MatrixQA(int matrixId)
	{
		super(
				"Matrix QA & Reports",
				"Matrix Quality Control",
				"Matrix QA & Reports on Matrix ID: " + matrixId,
				"Matrix Quality Control");

		this.matrixId = matrixId;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_MatrixQA.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<OPType> necessaryOPsAL = new ArrayList<OPType>();
		necessaryOPsAL.add(OPType.SAMPLE_QA);
		necessaryOPsAL.add(OPType.MARKER_QA);
		List<OPType> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

		if (missingOPsAL.size() > 0) {
			if (missingOPsAL.contains(OPType.SAMPLE_QA)) {
				int sampleQAOpId = new OP_QASamples(matrixId).processMatrix();
				OperationKey sampleQAOpKey = OperationKey.valueOf(OperationsList.getById(sampleQAOpId));
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(sampleQAOpKey);
				org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
			}
			if (missingOPsAL.contains(OPType.MARKER_QA)) {
				int markersQAOpId = new OP_QAMarkers(matrixId).processMatrix();
				OperationKey markersQAOpKey = OperationKey.valueOf(OperationsList.getById(markersQAOpId));
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(markersQAOpKey);
				org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			}
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

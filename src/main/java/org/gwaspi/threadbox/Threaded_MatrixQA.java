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
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.reports.OutputQAMarkers;
import org.gwaspi.reports.OutputQASamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_MatrixQA extends CommonRunnable {

	private final MatrixKey matrixKey;

	public Threaded_MatrixQA(MatrixKey matrixKey)
	{
		super(
				"Matrix QA & Reports",
				"Matrix Quality Control",
				"Matrix QA & Reports on Matrix ID: " + matrixKey.getMatrixId(),
				"Matrix Quality Control");

		this.matrixKey = matrixKey;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_MatrixQA.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<OPType> necessaryOPsAL = new ArrayList<OPType>();
		necessaryOPsAL.add(OPType.SAMPLE_QA);
		necessaryOPsAL.add(OPType.MARKER_QA);
		List<OPType> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixKey);

		if (missingOPsAL.size() > 0) {
			if (missingOPsAL.contains(OPType.SAMPLE_QA)) {
				OperationManager.performQASamplesOperationAndCreateReports(new OP_QASamples(matrixKey));
			}
			if (missingOPsAL.contains(OPType.MARKER_QA)) {
				OperationManager.performQAMarkersOperationAndCreateReports(new OP_QAMarkers(matrixKey));
			}
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

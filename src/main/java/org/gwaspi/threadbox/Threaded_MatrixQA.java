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
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.operations.qamarkers.MarkersQAOperationParams;
import org.gwaspi.operations.qasamples.SamplesQAOperationParams;
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

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_MatrixQA.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<OPType> necessaryOPs = new ArrayList<OPType>();
		necessaryOPs.add(OPType.SAMPLE_QA);
		necessaryOPs.add(OPType.MARKER_QA);
		List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, matrixKey);

		if (missingOPs.size() > 0) {
			if (missingOPs.contains(OPType.SAMPLE_QA)) {
				OperationManager.performQASamplesOperationAndCreateReports(new OP_QASamples(new SamplesQAOperationParams(new DataSetKey(matrixKey))));
			}
			if (missingOPs.contains(OPType.MARKER_QA)) {
				OperationManager.performQAMarkersOperationAndCreateReports(new OP_QAMarkers(new MarkersQAOperationParams(new DataSetKey(matrixKey))));
			}
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

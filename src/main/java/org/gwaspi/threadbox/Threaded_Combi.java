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

import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.reports.OutputTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_Combi extends CommonRunnable {

	private final CombiTestOperationParams params;

	public Threaded_Combi(CombiTestOperationParams params) {
		super(
				"Combi Association Test",
				"Combi Association Study",
				"Combi Association Test on: " + params.getParent().toString(),
				"Combi Association Test");

		this.params = params;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Combi.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

//		List<Operation> operations = OperationsList.getOperationsList(params.getMatrixKey().getId());
//		int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

		// XXX TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
//		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			OperationKey combiOpKey = OperationManager.performRawCombiTest(params);

			// XXX Make Reports (needs newMatrixId, QAopId, AssocOpId)
			OperationKey testOpKey = combiOpKey;
			if (testOpKey != null) {
				new OutputTest(testOpKey, OPType.COMBI_ASSOC_TEST).writeReportsForTestData();
				GWASpiExplorerNodes.insertReportsUnderOperationNode(testOpKey);
			}
		}
	}
}

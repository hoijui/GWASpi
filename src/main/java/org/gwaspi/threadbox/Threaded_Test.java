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

import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.reports.OutputTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_Test extends CommonRunnable {

	private final OperationKey censusOpKey;
	private final OperationKey hwOpKey;
	private final GWASinOneGOParams gwasParams;
	private final OPType testType;

	public Threaded_Test(
			OperationKey censusOpKey,
			OperationKey hwOpKey,
			GWASinOneGOParams gwasParams,
			OPType testType)
	{
		super(
				OutputTest.createTestName(testType) + " Test",
				OutputTest.createTestName(testType) + " Test",
				OutputTest.createTestName(testType) + " Test on Matrix ID: " + censusOpKey.getParentMatrixKey().getMatrixId(),
				OutputTest.createTestName(testType) + " Test");

		this.testType = testType;
		this.censusOpKey = censusOpKey;
		this.hwOpKey = hwOpKey;
		this.gwasParams = gwasParams;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Test.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<OperationMetadata> operations = OperationsList.getOffspringOperationsMetadata(censusOpKey.getParentMatrixKey());
		OperationKey markersQAOpKey = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

//		if (!gwasParams.isDiscardMarkerByMisRat()) {
//			gwasParams.setDiscardMarkerMisRatVal(1);
//		}
//		if (!gwasParams.isDiscardMarkerByHetzyRat()) {
//			gwasParams.setDiscardMarkerHetzyRatVal(1);
//		}
//		if (!gwasParams.isDiscardSampleByMisRat()) {
//			gwasParams.setDiscardSampleMisRatVal(1);
//		}
//		if (!gwasParams.isDiscardSampleByHetzyRat()) {
//			gwasParams.setDiscardSampleHetzyRatVal(1);
//		}

		// TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpKey);

		if (gwasParams.isDiscardMarkerHWCalc()) {
			gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getNumMarkers());
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			OperationKey testOpKey = OperationManager.performCleanTests(
					censusOpKey,
					hwOpKey,
					gwasParams.getDiscardMarkerHWTreshold(),
					testType);

			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
			if (testOpKey != null) {
				new OutputTest(testOpKey, testType, markersQAOpKey).writeReportsForTestData();
				GWASpiExplorerNodes.insertReportsUnderOperationNode(testOpKey);
			}
		}
	}
}

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
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.reports.OutputAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_Association extends CommonRunnable {

	private final int matrixId;
	private final int censusOpId;
	private final int hwOpId;
	private final GWASinOneGOParams gwasParams;
	private final boolean allelic;

	public Threaded_Association(
			int matrixId,
			int censusOpId,
			int hwOpId,
			GWASinOneGOParams gwasParams,
			boolean allelic)
	{
		super(
				(allelic ? "Allelic" : "Genotypic") + " Association Test",
				(allelic ? "Allelic" : "Genotypic") + " Association Study",
				(allelic ? "Allelic" : "Genotypic") + " Association Test on Matrix ID: " + matrixId,
				(allelic ? "Allelic" : "Genotypic") + " Association Test");

		this.matrixId = matrixId;
		this.censusOpId = censusOpId;
		this.hwOpId = hwOpId;
		this.gwasParams = gwasParams;
		this.allelic = allelic;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Association.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<OperationMetadata> operations = OperationsList.getOperationsList(matrixId);
		int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

		if (!gwasParams.isDiscardMarkerByMisRat()) {
			gwasParams.setDiscardMarkerMisRatVal(1);
		}
		if (!gwasParams.isDiscardMarkerByHetzyRat()) {
			gwasParams.setDiscardMarkerHetzyRatVal(1);
		}
		if (!gwasParams.isDiscardSampleByMisRat()) {
			gwasParams.setDiscardSampleMisRatVal(1);
		}
		if (!gwasParams.isDiscardSampleByHetzyRat()) {
			gwasParams.setDiscardSampleHetzyRatVal(1);
		}

		// TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);

		if (gwasParams.isDiscardMarkerHWCalc()) {
			gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getOpSetSize());
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			int assocOpId = OperationManager.performCleanAssociationTests(
					matrixId,
					censusOpId,
					hwOpId,
					gwasParams.getDiscardMarkerHWTreshold(),
					allelic);
			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, assocOpId);

			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
			if (assocOpId != Integer.MIN_VALUE) {
				new OutputAssociation(allelic).writeReportsForAssociationData(assocOpId);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
			}
		}
	}
}

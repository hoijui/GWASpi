package org.gwaspi.threadbox;

import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.MatrixOperation;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public abstract class AbstractThreaded_MergeMatrices extends CommonRunnable {

	protected final int studyId;
	protected final int parentMatrixId1;
	protected final int parentMatrixId2;
	protected final String newMatrixName;
	protected final String description;

	public AbstractThreaded_MergeMatrices(
			int studyId,
			int parentMatrixId1,
			int parentMatrixId2,
			String newMatrixName,
			String description)
	{
		super(
				"Merge Matrices",
				"Merging Data",
				"Merge Matrices: " + newMatrixName,
				"Merging Matrices");

		this.studyId = studyId;
		this.parentMatrixId1 = parentMatrixId1;
		this.parentMatrixId2 = parentMatrixId2;
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
			GWASpiExplorerNodes.insertMatrixNode(studyId, resultMatrixId);

			if (!thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				return;
			}
			int sampleQAOpId = new OP_QASamples(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, sampleQAOpId);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);

			if (!thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				return;
			}
			int markersQAOpId = new OP_QAMarkers(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

package org.gwaspi.threadbox;

import java.io.File;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.MatrixGenotypesFlipper;
import org.gwaspi.netCDF.operations.OP_QAMarkers_opt;
import org.gwaspi.netCDF.operations.OP_QASamples_opt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_FlipStrandMatrix extends CommonRunnable {

	private int resultMatrixId;
	private int studyId;
	private int parentMatrixId;
	private String newMatrixName;
	private String description;
	private String markerIdentifyer;
	private File markerFlipFile;

	public Threaded_FlipStrandMatrix(String threadName,
			String timeStamp,
			int studyId,
			int parentMatrixId,
			String newMatrixName,
			String description,
			String markerIdentifyer,
			File markerFlipFile)
	{
		super(threadName, timeStamp, "Flipping Genotypes");

		this.studyId = studyId;
		this.parentMatrixId = parentMatrixId;
		this.newMatrixName = newMatrixName;
		this.description = description;
		this.markerIdentifyer = markerIdentifyer;
		this.markerFlipFile = markerFlipFile;

		startInternal("Extracting");
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_FlipStrandMatrix.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
			MatrixGenotypesFlipper flipMatrix = new MatrixGenotypesFlipper(studyId,
					parentMatrixId,
					newMatrixName,
					description,
					markerIdentifyer,
					markerFlipFile);
			resultMatrixId = flipMatrix.flipGenotypesToNewMatrix();
			GWASpiExplorerNodes.insertMatrixNode(studyId, resultMatrixId);
		}

		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
			int sampleQAOpId = OP_QASamples_opt.processMatrix(resultMatrixId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, sampleQAOpId);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
		}

		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
			int markersQAOpId = OP_QAMarkers_opt.processMatrix(resultMatrixId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

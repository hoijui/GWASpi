package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.MatrixTranslator;
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
public class Threaded_TranslateMatrix extends CommonRunnable {

	private int studyId;
	private int parentMatrixId;
	private GenotypeEncoding gtEncoding;
	private String newMatrixName;
	private String description;

	public Threaded_TranslateMatrix(
			int studyId,
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

		this.studyId = studyId;
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
			MatrixTranslator matrixTransformer = new MatrixTranslator(studyId,
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

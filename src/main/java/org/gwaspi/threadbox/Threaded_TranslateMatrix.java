package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.MatrixTranslator_opt;
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
public class Threaded_TranslateMatrix extends CommonRunnable {

	private int resultMatrixId; // FIXME can be a local var
	private int studyId;
	private int parentMatrixId;
	private cNetCDF.Defaults.GenotypeEncoding gtEncoding;
	private String newMatrixName;
	private String description;

	public Threaded_TranslateMatrix(String threadName,
			String timeStamp,
			int studyId,
			int parentMatrixId,
			cNetCDF.Defaults.GenotypeEncoding gtEncoding,
			String newMatrixName,
			String description)
	{
		super(threadName, timeStamp, "Translating Matrix");

		this.studyId = studyId;
		this.parentMatrixId = parentMatrixId;
		this.gtEncoding = gtEncoding;
		this.newMatrixName = newMatrixName;
		this.description = description;

		startInternal(getTaskDescription());
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_TranslateMatrix.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
			MatrixTranslator_opt matrixTransformer = new MatrixTranslator_opt(studyId,
					parentMatrixId,
					newMatrixName,
					description);

			if (gtEncoding.equals(cNetCDF.Defaults.GenotypeEncoding.AB0)
					|| gtEncoding.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {

				resultMatrixId = matrixTransformer.translateAB12AllelesToACGT();
			} else if (gtEncoding.equals(cNetCDF.Defaults.GenotypeEncoding.O1234)) {

				resultMatrixId = matrixTransformer.translate1234AllelesToACGT();
			}
			GWASpiExplorerNodes.insertMatrixNode(studyId, resultMatrixId);
		}


		if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
			int sampleQAOpId = OP_QASamples_opt.processMatrix(resultMatrixId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, sampleQAOpId);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
		}

		if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
			int markersQAOpId = OP_QAMarkers_opt.processMatrix(resultMatrixId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
			org.gwaspi.reports.OutputQAMarkers_opt.writeReportsForQAMarkersData(markersQAOpId);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

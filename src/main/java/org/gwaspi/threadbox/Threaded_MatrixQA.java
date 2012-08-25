package org.gwaspi.threadbox;

import java.util.ArrayList;
import java.util.List;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.OP_QAMarkers_opt;
import org.gwaspi.netCDF.operations.OP_QASamples_opt;
import org.gwaspi.netCDF.operations.OperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_MatrixQA extends CommonRunnable {

	private int matrixId;

	public Threaded_MatrixQA(String threadName,
			String timeStamp,
			int matrixId)
	{
		super(threadName, timeStamp, "Matrix Quality Control");

		this.matrixId = matrixId;

		startInternal(getTaskDescription());
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_MatrixQA.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<String> necessaryOPsAL = new ArrayList<String>();
		necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
		necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
		List<String> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

		if (missingOPsAL.size() > 0) {
			if (missingOPsAL.contains(org.gwaspi.constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString())) {
				int sampleQAOpId = OP_QASamples_opt.processMatrix(matrixId);
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, sampleQAOpId);
				org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
			}
			if (missingOPsAL.contains(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA.toString())) {
				int markersQAOpId = OP_QAMarkers_opt.processMatrix(matrixId);
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, markersQAOpId);
				org.gwaspi.reports.OutputQAMarkers_opt.writeReportsForQAMarkersData(markersQAOpId);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			}
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF;
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

	public Threaded_MatrixQA(int matrixId)
	{
		super(
				"Matrix QA & Reports",
				"Matrix Quality Control",
				"Matrix QA & Reports on Matrix ID: " + matrixId,
				"Matrix Quality Control");

		this.matrixId = matrixId;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_MatrixQA.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<String> necessaryOPsAL = new ArrayList<String>();
		necessaryOPsAL.add(cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
		necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_QA.toString());
		List<String> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

		if (missingOPsAL.size() > 0) {
			if (missingOPsAL.contains(cNetCDF.Defaults.OPType.SAMPLE_QA.toString())) {
				int sampleQAOpId = new OP_QASamples_opt(matrixId).processMatrix();
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, sampleQAOpId);
				org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
			}
			if (missingOPsAL.contains(cNetCDF.Defaults.OPType.MARKER_QA.toString())) {
				int markersQAOpId = new OP_QAMarkers_opt(matrixId).processMatrix();
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, markersQAOpId);
				org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			}
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

package org.gwaspi.threadbox;

import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
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

		List<OPType> necessaryOPsAL = new ArrayList<OPType>();
		necessaryOPsAL.add(OPType.SAMPLE_QA);
		necessaryOPsAL.add(OPType.MARKER_QA);
		List<OPType> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

		if (missingOPsAL.size() > 0) {
			if (missingOPsAL.contains(OPType.SAMPLE_QA)) {
				int sampleQAOpId = new OP_QASamples(matrixId).processMatrix();
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, sampleQAOpId);
				org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
			}
			if (missingOPsAL.contains(OPType.MARKER_QA)) {
				int markersQAOpId = new OP_QAMarkers(matrixId).processMatrix();
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, markersQAOpId);
				org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
				GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			}
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

package org.gwaspi.threadbox;

import java.util.Map;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.loader.LoadManager;
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
public class Threaded_Loader_QA extends CommonRunnable {

	private int resultMatrixId;
	private int resultOpId;
	private GenotypesLoadDescription loadDescription;
	private Map<String, Object> sampleInfoMap;

	public Threaded_Loader_QA(
			String threadName,
			String timeStamp,
			GenotypesLoadDescription loadDescription,
			Map<String, Object> sampleInfoMap)
	{
		super(threadName, timeStamp, "Loading Genotypes");

		this.loadDescription = loadDescription;
		this.sampleInfoMap = sampleInfoMap;

		startInternal(getTaskDescription());
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Loader_QA.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			resultMatrixId = LoadManager.dispatchLoadByFormat(
					loadDescription,
					sampleInfoMap);

			MultiOperations.printCompleted("Loading Genotypes");
			GWASpiExplorerNodes.insertMatrixNode(loadDescription.getStudyId(), resultMatrixId);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			resultOpId = new OP_QASamples_opt(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, resultOpId);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(resultOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(resultOpId);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			resultOpId = new OP_QAMarkers_opt(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, resultOpId);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(resultOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
			GWASpiExplorerNodes.insertReportsUnderOperationNode(resultOpId);
		}
	}
}

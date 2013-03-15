package org.gwaspi.threadbox;

import java.util.Collection;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.loader.LoadManager;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @deprecated unused class!
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_Loader_QA extends CommonRunnable {

	private int resultMatrixId;
	private int resultOpId;
	private GenotypesLoadDescription loadDescription;
	private Collection<SampleInfo> sampleInfos;

	public Threaded_Loader_QA(
			GenotypesLoadDescription loadDescription,
			Collection<SampleInfo> sampleInfoMap)
	{
		super(
				"Loading Genotypes",
				"Loading Genotypes",
				"Loading Genotypes: " + loadDescription.getStudyId(),
				"Loading Genotypes");

		this.loadDescription = loadDescription;
		this.sampleInfos = sampleInfoMap;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Loader_QA.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			resultMatrixId = LoadManager.dispatchLoadByFormat(
					loadDescription,
					sampleInfos);

			MultiOperations.printCompleted("Loading Genotypes");
			GWASpiExplorerNodes.insertMatrixNode(loadDescription.getStudyId(), resultMatrixId);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			resultOpId = new OP_QASamples(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, resultOpId);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(resultOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(resultOpId);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			resultOpId = new OP_QAMarkers(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, resultOpId);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(resultOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
			GWASpiExplorerNodes.insertReportsUnderOperationNode(resultOpId);
		}
	}
}

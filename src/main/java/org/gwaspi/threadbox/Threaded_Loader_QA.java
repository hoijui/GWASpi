package org.gwaspi.threadbox;

import java.util.Map;
import org.gwaspi.model.GWASpiExplorerNodes;
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
	private String format;
	private Map<String, Object> sampleInfoLHM;
	private String newMatrixName;
	private String newMatrixDescription;
	private String file1;
	private String fileSampleInfo;
	private String file2;
	private String chromosome;
	private String strandType;
	private String gtCode;
	private int studyId;

	public Threaded_Loader_QA(
			String threadName,
			String timeStamp,
			String format,
			Map<String, Object> sampleInfoLHM,
			String newMatrixName,
			String newMatrixDescription,
			String file1,
			String fileSampleInfo,
			String file2,
			String chromosome,
			String strandType,
			String gtCode,
			int studyId)
	{
		super(threadName, timeStamp, "Loading Genotypes");

		this.format = format;
		this.sampleInfoLHM = sampleInfoLHM;
		this.newMatrixName = newMatrixName;
		this.newMatrixDescription = newMatrixDescription;
		this.file1 = file1;
		this.fileSampleInfo = fileSampleInfo;
		this.file2 = file2;
		this.chromosome = chromosome;
		this.strandType = strandType;
		this.gtCode = gtCode;
		this.studyId = studyId;

		startInternal(getTaskDescription());
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Loader_QA.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			resultMatrixId = LoadManager.dispatchLoadByFormat(format,
					sampleInfoLHM,
					newMatrixName,
					newMatrixDescription,
					file1,
					fileSampleInfo,
					file2,
					chromosome,
					strandType,
					gtCode,
					studyId);

			MultiOperations.printCompleted("Loading Genotypes");
			GWASpiExplorerNodes.insertMatrixNode(studyId, resultMatrixId);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			resultOpId = new OP_QASamples_opt().processMatrix(resultMatrixId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, resultOpId);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(resultOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(resultOpId);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			resultOpId = new OP_QAMarkers_opt().processMatrix(resultMatrixId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, resultOpId);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(resultOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
			GWASpiExplorerNodes.insertReportsUnderOperationNode(resultOpId);
		}
	}
}

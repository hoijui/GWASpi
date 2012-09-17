package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF;
import java.io.File;
import java.util.Set;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.MatrixDataExtractor_opt;
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
public class Threaded_ExtractMatrix extends CommonRunnable {

	private int resultMatrixId;
	private int studyId;
	private int parentMatrixId;
	private String newMatrixName;
	private String description;
	private cNetCDF.Defaults.SetMarkerPickCase markerPickCase;
	private cNetCDF.Defaults.SetSamplePickCase samplePickCase;
	private String markerPickVar;
	private String samplePickVar;
	private Set<Object> markerCriteria;
	private Set<Object> sampleCriteria;
	private File markerCriteriaFile;
	private File sampleCriteriaFile;

	public Threaded_ExtractMatrix(
			String threadName,
			String timeStamp,
			int studyId,
			int parentMatrixId,
			String newMatrixName,
			String description,
			cNetCDF.Defaults.SetMarkerPickCase markerPickCase,
			cNetCDF.Defaults.SetSamplePickCase samplePickCase,
			String markerPickVar,
			String samplePickVar,
			Set<Object> markerCriteria,
			Set<Object> sampleCriteria,
			File markerCriteriaFile,
			File sampleCriteriaFile)
	{
		super(threadName, timeStamp, "Extracting Data");

		this.studyId = studyId;
		this.parentMatrixId = parentMatrixId;
		this.newMatrixName = newMatrixName;
		this.description = description;
		this.markerPickCase = markerPickCase;
		this.samplePickCase = samplePickCase;
		this.markerPickVar = markerPickVar;
		this.samplePickVar = samplePickVar;
		this.markerCriteria = markerCriteria;
		this.sampleCriteria = sampleCriteria;
		this.markerCriteriaFile = markerCriteriaFile;
		this.sampleCriteriaFile = sampleCriteriaFile;

		startInternal("Extracting");
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_ExtractMatrix.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
			MatrixDataExtractor_opt exMatrix = new MatrixDataExtractor_opt(studyId,
					parentMatrixId,
					newMatrixName,
					description,
					markerPickCase,
					samplePickCase,
					markerPickVar,
					samplePickVar,
					markerCriteria,
					sampleCriteria,
					Integer.MIN_VALUE, //Filter pos, not used now
					markerCriteriaFile,
					sampleCriteriaFile);
			resultMatrixId = exMatrix.extractGenotypesToNewMatrix();
			GWASpiExplorerNodes.insertMatrixNode(studyId, resultMatrixId);
		}

		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
			int sampleQAOpId = new OP_QASamples_opt().processMatrix(resultMatrixId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, sampleQAOpId);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
		}

		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
			int markersQAOpId = new OP_QAMarkers_opt().processMatrix(resultMatrixId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

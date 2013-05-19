package org.gwaspi.threadbox;

import java.io.File;
import java.util.Set;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.MatrixDataExtractor;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_ExtractMatrix extends CommonRunnable {

	private int resultMatrixId;
	private int studyId;
	private int parentMatrixId;
	private String newMatrixName;
	private String description;
	private SetMarkerPickCase markerPickCase;
	private SetSamplePickCase samplePickCase;
	private String markerPickVar;
	private String samplePickVar;
	private Set<Object> markerCriteria;
	private Set<Object> sampleCriteria;
	private File markerCriteriaFile;
	private File sampleCriteriaFile;

	public Threaded_ExtractMatrix(
			int studyId,
			int parentMatrixId,
			String newMatrixName,
			String description,
			SetMarkerPickCase markerPickCase,
			SetSamplePickCase samplePickCase,
			String markerPickVar,
			String samplePickVar,
			Set<Object> markerCriteria,
			Set<Object> sampleCriteria,
			File markerCriteriaFile,
			File sampleCriteriaFile)
	{
		super(
				"Data Extract",
				"Extracting Data",
				"Data Extract: " + newMatrixName,
				"Extracting");

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
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_ExtractMatrix.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			MatrixDataExtractor exMatrix = new MatrixDataExtractor(studyId,
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

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			int sampleQAOpId = new OP_QASamples(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, sampleQAOpId);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
		}

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			int markersQAOpId = new OP_QAMarkers(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
	}
}

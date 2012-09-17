package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.loader.LoadManager;
import org.gwaspi.netCDF.loader.SampleInfoCollectorSwitch;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OP_QAMarkers_opt;
import org.gwaspi.netCDF.operations.OP_QASamples_opt;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.reports.OutputAllelicAssociation;
import org.gwaspi.reports.OutputGenotypicAssociation;
import org.gwaspi.reports.OutputTrendTest;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_Loader_GWASifOK extends CommonRunnable {

	private int resultMatrixId;
	private int samplesQAOpId;
	private int markersQAOpId;
	private String format;
	private boolean dummySamples;
	private int decision;
	private String newMatrixName;
	private String newMatrixDescription;
	private String file1;
	private String fileSampleInfo;
	private String file2;
	private String chromosome;
	private String strandType;
	private String gtCode;
	private int studyId;
	private GWASinOneGOParams gwasParams;

	public Threaded_Loader_GWASifOK(
			String threadName,
			String timeStamp,
			String format,
			boolean dummySamples,
			int decision,
			String newMatrixName,
			String newMatrixDescription,
			String file1,
			String fileSampleInfo,
			String file2,
			String chromosome,
			String strandType,
			String gtCode,
			int studyId,
			GWASinOneGOParams gwasParams)
	{
		super(threadName, timeStamp, "Loading Genotypes & Performing GWAS");

		this.format = format;
		this.dummySamples = dummySamples;
		this.decision = decision;
		this.newMatrixName = newMatrixName;
		this.newMatrixDescription = newMatrixDescription;
		this.file1 = file1;
		this.fileSampleInfo = fileSampleInfo;
		this.file2 = file2;
		this.chromosome = chromosome;
		this.strandType = strandType;
		this.gtCode = gtCode;
		this.studyId = studyId;
		this.gwasParams = gwasParams;

		startInternal(getTaskDescription());
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Loader_GWASifOK.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		Map<String, Object> sampleInfoLHM = SampleInfoCollectorSwitch.collectSampleInfo(format, dummySamples, fileSampleInfo, file1, file2);
		Set<String> affectionStates = SampleInfoCollectorSwitch.collectAffectionStates(sampleInfoLHM);

		//<editor-fold defaultstate="collapsed" desc="LOAD PROCESS">
		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
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
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="QA PROCESS">
		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
			samplesQAOpId = new OP_QASamples_opt().processMatrix(resultMatrixId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, samplesQAOpId);
			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(samplesQAOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(samplesQAOpId);
		}

		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
			markersQAOpId = new OP_QAMarkers_opt().processMatrix(resultMatrixId);
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
			org.gwaspi.reports.OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
		//</editor-fold>

		if (decision == JOptionPane.YES_OPTION
				&& affectionStates.contains("1")
				&& affectionStates.contains("2"))
		{
			// CHECK IF GWAS WAS REQUIRED AND IF AFFECTIONS AVAILABLE

			if (!gwasParams.isDiscardMarkerByMisRat()) {
				gwasParams.setDiscardMarkerMisRatVal(1);
			}
			if (!gwasParams.isDiscardMarkerByHetzyRat()) {
				gwasParams.setDiscardMarkerHetzyRatVal(1);
			}
			if (!gwasParams.isDiscardSampleByMisRat()) {
				gwasParams.setDiscardSampleMisRatVal(1);
			}
			if (!gwasParams.isDiscardSampleByHetzyRat()) {
				gwasParams.setDiscardSampleHetzyRatVal(1);
			}

			//<editor-fold defaultstate="collapsed" desc="PRE-GWAS PROCESS">
			// GENOTYPE FREQ.
			int censusOpId = Integer.MIN_VALUE;
			if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
				censusOpId = OperationManager.censusCleanMatrixMarkers(resultMatrixId,
						samplesQAOpId,
						markersQAOpId, gwasParams.getDiscardMarkerMisRatVal(), gwasParams.isDiscardGTMismatches(), gwasParams.getDiscardSampleMisRatVal(), gwasParams.getDiscardSampleHetzyRatVal(),
						cNetCDF.Defaults.DEFAULT_AFFECTION);
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, censusOpId);
			}

			// HW ON GENOTYPE FREQ.
			int hwOpId = Integer.MIN_VALUE;
			if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)
					&& censusOpId != Integer.MIN_VALUE) {
				hwOpId = org.gwaspi.netCDF.operations.OperationManager.performHardyWeinberg(censusOpId, cNetCDF.Defaults.DEFAULT_AFFECTION);
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, hwOpId);
			}
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="GWAS TESTS & REPORTS">
			// ALLELIC ASSOCIATION (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
			if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)
					&& censusOpId != Integer.MIN_VALUE
					&& hwOpId != Integer.MIN_VALUE) {
				OperationMetadata markerQAMetadata = new OperationMetadata(markersQAOpId);

				if (	gwasParams.isDiscardMarkerHWCalc()) {
					gwasParams.setDiscardMarkerHWTreshold((double) 0.05 / markerQAMetadata.getOpSetSize());
				}

				int assocOpId = org.gwaspi.netCDF.operations.OperationManager.performCleanAllelicTests(resultMatrixId,
						censusOpId,
						hwOpId, gwasParams.getDiscardMarkerHWTreshold());
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, assocOpId);

				// Make Reports (needs newMatrixId, QAopId, AssocOpId)
				if (assocOpId != Integer.MIN_VALUE) {
					OutputAllelicAssociation.writeReportsForAssociationData(assocOpId);
					GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
				}

			}

			// GENOTYPIC TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
			if (gwasParams.isPerformGenotypicTests()
					&& thisSwi.getQueueState().equals(QueueStates.PROCESSING)
					&& censusOpId != Integer.MIN_VALUE
					&& hwOpId != Integer.MIN_VALUE) {

				OperationMetadata markerQAMetadata = new OperationMetadata(markersQAOpId);

				if (	gwasParams.isDiscardMarkerHWCalc()) {
					gwasParams.setDiscardMarkerHWTreshold((double) 0.05 / markerQAMetadata.getOpSetSize());
				}

				int assocOpId = org.gwaspi.netCDF.operations.OperationManager.performCleanGenotypicTests(resultMatrixId,
						censusOpId,
						hwOpId, gwasParams.getDiscardMarkerHWTreshold());
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, assocOpId);

				//////Make Reports (needs newMatrixId, QAopId, AssocOpId)
				if (assocOpId != Integer.MIN_VALUE) {
					OutputGenotypicAssociation.writeReportsForAssociationData(assocOpId);
					GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
				}
			}

			// TREND TESTS (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
			if (gwasParams.isPerformTrendTests()
					&& thisSwi.getQueueState().equals(QueueStates.PROCESSING)
					&& censusOpId != Integer.MIN_VALUE
					&& hwOpId != Integer.MIN_VALUE) {

				OperationMetadata markerQAMetadata = new OperationMetadata(markersQAOpId);

				if (	gwasParams.isDiscardMarkerHWCalc()) {
					gwasParams.setDiscardMarkerHWTreshold((double) 0.05 / markerQAMetadata.getOpSetSize());
				}

				int trendOpId = org.gwaspi.netCDF.operations.OperationManager.performCleanTrendTests(resultMatrixId,
						censusOpId,
						hwOpId, gwasParams.getDiscardMarkerHWTreshold());
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, trendOpId);

				// Make Reports (needs newMatrixId, QAopId, AssocOpId)
				if (trendOpId != Integer.MIN_VALUE) {
					OutputTrendTest.writeReportsForTrendTestData(trendOpId);
					GWASpiExplorerNodes.insertReportsUnderOperationNode(trendOpId);
				}
			}
			//</editor-fold>
		}
	}
}

package org.gwaspi.threadbox;

import java.util.Collection;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.loader.LoadManager;
import org.gwaspi.netCDF.loader.SampleInfoCollectorSwitch;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OP_QAMarkers_opt;
import org.gwaspi.netCDF.operations.OP_QASamples_opt;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.reports.OutputAllelicAssociation;
import org.gwaspi.reports.OutputGenotypicAssociation;
import org.gwaspi.reports.OutputQAMarkers;
import org.gwaspi.reports.OutputQASamples;
import org.gwaspi.reports.OutputTrendTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_Loader_GWASifOK extends CommonRunnable {

	private boolean dummySamples;
	private boolean performGwas;
	private GenotypesLoadDescription loadDescription;
	private GWASinOneGOParams gwasParams;

	public Threaded_Loader_GWASifOK(
			GenotypesLoadDescription loadDescription,
			boolean dummySamples,
			boolean performGwas,
			GWASinOneGOParams gwasParams)
	{
		super(
				"Genotypes Loader & GWAS if OK",
				"Loading Genotypes & Performing GWAS",
				"Genotypes Loader & GWAS if OK: " + loadDescription.getFriendlyName(),
				"Loading Genotypes & Performing GWAS");

		this.loadDescription = loadDescription;
		this.dummySamples = dummySamples;
		this.performGwas = performGwas;
		this.gwasParams = gwasParams;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Loader_GWASifOK.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		Collection<SampleInfo> sampleInfos = SampleInfoCollectorSwitch.collectSampleInfo(
				loadDescription.getFormat(),
				dummySamples,
				loadDescription.getSampleFilePath(),
				loadDescription.getGtDirPath(),
				loadDescription.getAnnotationFilePath());
		Set<SampleInfo.Affection> affectionStates = SampleInfoCollectorSwitch.collectAffectionStates(sampleInfos);

		//<editor-fold defaultstate="expanded" desc="LOAD PROCESS">
		int resultMatrixId = Integer.MIN_VALUE;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			resultMatrixId = LoadManager.dispatchLoadByFormat(
					loadDescription,
					sampleInfos);
			MultiOperations.printCompleted("Loading Genotypes");
			GWASpiExplorerNodes.insertMatrixNode(loadDescription.getStudyId(), resultMatrixId);
		}
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="QA PROCESS">
		int samplesQAOpId = Integer.MIN_VALUE;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			samplesQAOpId = new OP_QASamples_opt(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, samplesQAOpId);
			OutputQASamples.writeReportsForQASamplesData(samplesQAOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(samplesQAOpId);
		}

		int markersQAOpId = Integer.MIN_VALUE;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			markersQAOpId = new OP_QAMarkers_opt(resultMatrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
			OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
		//</editor-fold>

		if (performGwas
				&& affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
				&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
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

			//<editor-fold defaultstate="expanded" desc="PRE-GWAS PROCESS">
			// GENOTYPE FREQ.
			int censusOpId = Integer.MIN_VALUE;
			if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				censusOpId = OperationManager.censusCleanMatrixMarkers(resultMatrixId,
						samplesQAOpId,
						markersQAOpId, gwasParams.getDiscardMarkerMisRatVal(), gwasParams.isDiscardGTMismatches(), gwasParams.getDiscardSampleMisRatVal(), gwasParams.getDiscardSampleHetzyRatVal(),
						cNetCDF.Defaults.DEFAULT_AFFECTION);
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, censusOpId);
			}

			// HW ON GENOTYPE FREQ.
			int hwOpId = Integer.MIN_VALUE;
			if (thisSwi.getQueueState().equals(QueueState.PROCESSING)
					&& censusOpId != Integer.MIN_VALUE) {
				hwOpId = OperationManager.performHardyWeinberg(censusOpId, cNetCDF.Defaults.DEFAULT_AFFECTION);
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, hwOpId);
			}
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="GWAS TESTS & REPORTS">
			// ALLELIC ASSOCIATION (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
			if (thisSwi.getQueueState().equals(QueueState.PROCESSING)
					&& censusOpId != Integer.MIN_VALUE
					&& hwOpId != Integer.MIN_VALUE) {
				OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);

				if (gwasParams.isDiscardMarkerHWCalc()) {
					gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getOpSetSize());
				}

				int assocOpId = OperationManager.performCleanAllelicTests(
						resultMatrixId,
						censusOpId,
						hwOpId,
						gwasParams.getDiscardMarkerHWTreshold());
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, assocOpId);

				// Make Reports (needs newMatrixId, QAopId, AssocOpId)
				if (assocOpId != Integer.MIN_VALUE) {
					OutputAllelicAssociation.writeReportsForAssociationData(assocOpId);
					GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
				}

			}

			// GENOTYPIC TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
			if (gwasParams.isPerformGenotypicTests()
					&& thisSwi.getQueueState().equals(QueueState.PROCESSING)
					&& censusOpId != Integer.MIN_VALUE
					&& hwOpId != Integer.MIN_VALUE) {

				OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);

				if (gwasParams.isDiscardMarkerHWCalc()) {
					gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getOpSetSize());
				}

				int assocOpId = OperationManager.performCleanGenotypicTests(
						resultMatrixId,
						censusOpId,
						hwOpId,
						gwasParams.getDiscardMarkerHWTreshold());
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, assocOpId);

				// Make Reports (needs newMatrixId, QAopId, AssocOpId)
				if (assocOpId != Integer.MIN_VALUE) {
					OutputGenotypicAssociation.writeReportsForAssociationData(assocOpId);
					GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
				}
			}

			// TREND TESTS (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
			if (gwasParams.isPerformTrendTests()
					&& thisSwi.getQueueState().equals(QueueState.PROCESSING)
					&& censusOpId != Integer.MIN_VALUE
					&& hwOpId != Integer.MIN_VALUE) {

				OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);

				if (gwasParams.isDiscardMarkerHWCalc()) {
					gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getOpSetSize());
				}

				int trendOpId = OperationManager.performCleanTrendTests(resultMatrixId,
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

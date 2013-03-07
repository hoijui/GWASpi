package org.gwaspi.threadbox;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.reports.OutputAllelicAssociation;
import org.gwaspi.reports.OutputGenotypicAssociation;
import org.gwaspi.reports.OutputTrendTest;
import org.gwaspi.samples.SamplesParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_GWAS extends CommonRunnable {

	private int matrixId;
	private File phenotypeFile;
	private GWASinOneGOParams gwasParams;

	public Threaded_GWAS(
			int matrixId,
			File phenotypeFile,
			GWASinOneGOParams gwasParams)
	{
		super("GWAS", "GWAS", "GWAS on Matrix ID: " + matrixId, "GWAS");

		this.matrixId = matrixId;
		this.phenotypeFile = phenotypeFile;
		this.gwasParams = gwasParams;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_GWAS.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<Operation> operations = OperationsList.getOperationsList(matrixId);
		int sampleQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.SAMPLE_QA);
		int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

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

			if (phenotypeFile != null && phenotypeFile.exists() && phenotypeFile.isFile()) { //BY EXTERNAL PHENOTYPE FILE

				Set<SampleInfo.Affection> affectionStates = SamplesParserManager.scanSampleInfoAffectionStates(phenotypeFile.getPath()); //use Sample Info file affection state

				if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
						&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
				{
					getLog().info("Updating Sample Info in DB");
					Collection<SampleInfo> sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(phenotypeFile.getPath());
					SampleInfoList.insertSampleInfos(matrixId, sampleInfos);

					censusOpId = OperationManager.censusCleanMatrixMarkersByPhenotypeFile(matrixId,
							sampleQAOpId,
							markersQAOpId, gwasParams.getDiscardMarkerMisRatVal(), gwasParams.isDiscardGTMismatches(), gwasParams.getDiscardSampleMisRatVal(), gwasParams.getDiscardSampleHetzyRatVal(),
							new StringBuilder().append(gwasParams.getFriendlyName()).append(" using ").append(phenotypeFile.getName()).toString(),
							phenotypeFile);

					org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
				} else {
					getLog().warn(Text.Operation.warnAffectionMissing);
				}
			} else { // BY DB AFFECTION
				Set<SampleInfo.Affection> affectionStates = SamplesParserManager.getDBAffectionStates(matrixId); // use Sample Info file affection state
				if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
						&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
				{
					censusOpId = OperationManager.censusCleanMatrixMarkers(matrixId,
							sampleQAOpId,
							markersQAOpId, gwasParams.getDiscardMarkerMisRatVal(), gwasParams.isDiscardGTMismatches(), gwasParams.getDiscardSampleMisRatVal(), gwasParams.getDiscardSampleHetzyRatVal(),
							new StringBuilder().append(gwasParams.getFriendlyName()).append(" using ").append(cNetCDF.Defaults.DEFAULT_AFFECTION).toString());

					org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
				} else {
					getLog().warn(Text.Operation.warnAffectionMissing);
				}
			}

			GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, censusOpId);
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
		// ALLELIC TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
		if (gwasParams.isPerformAllelicTests()
				&& thisSwi.getQueueState().equals(QueueState.PROCESSING)
				&& censusOpId != Integer.MIN_VALUE
				&& hwOpId != Integer.MIN_VALUE) {

			OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);
			int qaMarkerSetSize = markerQAMetadata.getOpSetSize();

			if (gwasParams.isDiscardMarkerHWCalc()) {
				gwasParams.setDiscardMarkerHWTreshold(0.05 / qaMarkerSetSize);
			}

			int assocOpId = OperationManager.performCleanAllelicTests(matrixId,
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
				&& thisSwi.getQueueState().equals(QueueState.PROCESSING)
				&& censusOpId != Integer.MIN_VALUE
				&& hwOpId != Integer.MIN_VALUE) {

			OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);
			int qaMarkerSetSize = markerQAMetadata.getOpSetSize();

			if (gwasParams.isDiscardMarkerHWCalc()) {
				gwasParams.setDiscardMarkerHWTreshold(0.05 / qaMarkerSetSize);
			}

			int assocOpId = OperationManager.performCleanGenotypicTests(
					matrixId,
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
			int qaMarkerSetSize = markerQAMetadata.getOpSetSize();

			if (gwasParams.isDiscardMarkerHWCalc()) {
				gwasParams.setDiscardMarkerHWTreshold(0.05 / qaMarkerSetSize);
			}

			int trendOpId = OperationManager.performCleanTrendTests(matrixId,
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

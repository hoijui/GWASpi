package org.gwaspi.threadbox;

import java.util.Collection;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.loader.LoadManager;
import org.gwaspi.netCDF.loader.SampleInfoCollectorSwitch;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.reports.OutputQAMarkers;
import org.gwaspi.reports.OutputQASamples;
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
		int matrixId = Integer.MIN_VALUE;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			matrixId = LoadManager.dispatchLoadByFormat(
					loadDescription,
					sampleInfos);
			MultiOperations.printCompleted("Loading Genotypes");
			GWASpiExplorerNodes.insertMatrixNode(loadDescription.getStudyId(), matrixId);
		}
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="QA PROCESS">
		int samplesQAOpId = Integer.MIN_VALUE;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			samplesQAOpId = new OP_QASamples(matrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, samplesQAOpId);
			OutputQASamples.writeReportsForQASamplesData(samplesQAOpId, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(samplesQAOpId);
		}

		int markersQAOpId = Integer.MIN_VALUE;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			markersQAOpId = new OP_QAMarkers(matrixId).processMatrix();
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, markersQAOpId);
			OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpId);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
		//</editor-fold>

		if (performGwas
				&& affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
				&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
		{
			Threaded_GWAS.checkRequired(gwasParams);

			//<editor-fold defaultstate="expanded" desc="PRE-GWAS PROCESS">
			// GENOTYPE FREQ.
			int censusOpId = Integer.MIN_VALUE;
			if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				censusOpId = OperationManager.censusCleanMatrixMarkers(
						matrixId,
						samplesQAOpId,
						markersQAOpId,
						gwasParams.getDiscardMarkerMisRatVal(),
						gwasParams.isDiscardGTMismatches(),
						gwasParams.getDiscardSampleMisRatVal(),
						gwasParams.getDiscardSampleHetzyRatVal(),
						cNetCDF.Defaults.DEFAULT_AFFECTION);
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, censusOpId);
			}

			int hwOpId = Threaded_GWAS.checkPerformHW(thisSwi, censusOpId);
			//</editor-fold>

			Threaded_GWAS.performGWAS(gwasParams, matrixId, thisSwi, markersQAOpId, censusOpId, hwOpId);
		}
	}
}

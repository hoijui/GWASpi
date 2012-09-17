package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import java.io.File;
import java.util.Map;
import java.util.Set;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.samples.SamplesParser;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_GTFreq_HW extends CommonRunnable {

	private final int matrixId;
	private final File phenotypeFile;
	private final GWASinOneGOParams gwasParams;

	public Threaded_GTFreq_HW(
			String threadName,
			String timeStamp,
			int matrixId,
			File phenotypeFile,
			GWASinOneGOParams gwasParams)
	{
		super(threadName, timeStamp, "Genotype Frequency count & Hardy-Weinberg test");

		this.matrixId = matrixId;
		this.phenotypeFile = phenotypeFile;
		this.gwasParams = gwasParams;

		startInternal(getTaskDescription());
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_GTFreq_HW.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		OperationsList opList = new OperationsList(matrixId);
		int sampleQAOpId = opList.getIdOfLastOperationTypeOccurance(cNetCDF.Defaults.OPType.SAMPLE_QA);
		int markersQAOpId = opList.getIdOfLastOperationTypeOccurance(cNetCDF.Defaults.OPType.MARKER_QA);

		//<editor-fold defaultstate="collapsed" desc="GT FREQ. & HW PROCESS">
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

		// GT FREQ. BY PHENOFILE OR DB AFFECTION
		int censusOpId = Integer.MIN_VALUE;
		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
			if (phenotypeFile != null && phenotypeFile.exists() && phenotypeFile.isFile()) { //BY EXTERNAL PHENOTYPE FILE

				Set<String> affectionStates = SamplesParser.scanSampleInfoAffectionStates(phenotypeFile.getPath()); //use Sample Info file affection state

				if (affectionStates.contains("1") && affectionStates.contains("2")) {
					getLog().info("Updating Sample Info in DB");
					Map<String, Object> sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(phenotypeFile.getPath());
					org.gwaspi.samples.InsertSampleInfo.processData(matrixId, sampleInfoLHM);

					censusOpId = OperationManager.censusCleanMatrixMarkersByPhenotypeFile(matrixId,
							sampleQAOpId,
							markersQAOpId, gwasParams.getDiscardMarkerMisRatVal(), gwasParams.isDiscardGTMismatches(), gwasParams.getDiscardSampleMisRatVal(), gwasParams.getDiscardSampleHetzyRatVal(),
							new StringBuilder().append(gwasParams.getFriendlyName()).append(" using ").append(phenotypeFile.getName()).toString(),
							phenotypeFile);

					org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
					//MultiOperations.updateTree();
				} else {
					getLog().info(Text.Operation.warnAffectionMissing);
				}
			} else { // BY DB AFFECTION
				Set<Object> affectionStates = SamplesParser.getDBAffectionStates(matrixId); //use Sample Info file affection state
				if (affectionStates.contains("1") && affectionStates.contains("2")) {
					censusOpId = OperationManager.censusCleanMatrixMarkers(matrixId,
							sampleQAOpId,
							markersQAOpId, gwasParams.getDiscardMarkerMisRatVal(), gwasParams.isDiscardGTMismatches(), gwasParams.getDiscardSampleMisRatVal(), gwasParams.getDiscardSampleHetzyRatVal(),
							new StringBuilder().append(gwasParams.getFriendlyName()).append(" using ").append(cNetCDF.Defaults.DEFAULT_AFFECTION).toString());


					org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
					//MultiOperations.updateTree();
				} else {
					getLog().info(Text.Operation.warnAffectionMissing);
				}
			}
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, censusOpId);
		}


		// HW ON GENOTYPE FREQ.
		if (thisSwi.getQueueState().equals(QueueStates.PROCESSING)) {
			if (censusOpId != Integer.MIN_VALUE) {
				int hwOpId = OperationManager.performHardyWeinberg(censusOpId, cNetCDF.Defaults.DEFAULT_AFFECTION);
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, hwOpId);
			}
		}
		//</editor-fold>
	}
}

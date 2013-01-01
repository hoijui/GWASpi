package org.gwaspi.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.threadbox.MultiOperations;

/**
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
class GenotypeFrequencyHardyWeinbergScriptCommand extends AbstractScriptCommand {

	GenotypeFrequencyHardyWeinbergScriptCommand() {
		super("genotype_frequency_hardy_weinberg");
	}

	@Override
	public boolean execute(List<String> args) throws IOException {

		//<editor-fold defaultstate="collapsed" desc="SCRIPT EXAMPLE">
		/*
		#This is a demo file
		#Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=genotype_frequency_hardy_weinberg
		1.study-id=1
		2.matrix-id=8
		3.gtfreq-name=alpha
		4.use-external-phenotype-file=true
		5.external-phenotype-file=/media/pheno_alpha
		6.discard-by-marker-missing-ratio=true
		7.discard-marker-missing-ratio-threshold=0.05
		8.discard-samples-by-missing-ratio=true
		9.discard-samples-missing-ratio-threshold=0.05
		[/script]
		*/
		//</editor-fold>

		GWASinOneGOParams gwasParams = new GWASinOneGOParams();

		// checking study
		int studyId = prepareStudy(args.get(1), false);
		boolean studyExists = checkStudy(studyId);

		if (studyExists) {
			int matrixId = Integer.parseInt(args.get(2)); // Parent Matrix Id
			String gtFrqName = args.get(3);
			boolean useExternalPhenoFile = Boolean.parseBoolean(args.get(4));
			File phenoFile = null;
			if (useExternalPhenoFile) {
				phenoFile = new File(args.get(5));
			}

			gwasParams.setDiscardGTMismatches(true);
			gwasParams.setDiscardMarkerByMisRat(Boolean.parseBoolean(args.get(6)));
			gwasParams.setDiscardMarkerMisRatVal(Double.parseDouble(args.get(7)));
			gwasParams.setDiscardSampleByMisRat(Boolean.parseBoolean(args.get(8)));
			gwasParams.setDiscardSampleMisRatVal(Double.parseDouble(args.get(9)));
			gwasParams.setFriendlyName(gtFrqName);
			gwasParams.setProceed(true);

			List<String> necessaryOPsAL = new ArrayList<String>();
			necessaryOPsAL.add(cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
			necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_QA.toString());
			List<String> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

			// QA block
			if (gwasParams.isProceed() && missingOPsAL.size() > 0) {
				gwasParams.setProceed(false);
				System.out.println(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
				MultiOperations.doMatrixQAs(studyId, matrixId);
			}

			// GT freq. & HW block
			if (gwasParams.isProceed()) {
				System.out.println(Text.All.processing);
				MultiOperations.doGTFreqDoHW(studyId,
						matrixId,
						phenoFile,
						gwasParams);
				return true;
			}
		}

		return false;
	}
}

package org.gwaspi.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.threadbox.MultiOperations;

class GwasInOneGoScriptCommand extends AbstractScriptCommand {

	GwasInOneGoScriptCommand() {
		super("gwas_in_one_go");
	}

	@Override
	public boolean execute(List<String> args) throws IOException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		#This is a demo file
		#Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=gwas_in_one_go
		1.study-id=1
		2.matrix-id=8
		3.gwas-name=alpha
		4.use-external-phenotype-file=true
		5.external-phenotype-file=/media/pheno_alpha
		6.discard-by-marker-missing-ratio=true
		7.discard-marker-missing-ratio-threshold=0.05
		8.calculate-discard-threshold-for-HW=false
		9.discard-marker-with-provided-HW-threshold=true
		10.discard-marker-HW-treshold=0.0000005
		11.discard-samples-by-missing-ratio=true
		12.discard-samples-missing-ratio-threshold=0.05
		13.discard-samples-by-heterozygosity-ratio=true
		14.discard-samples-heterozygosity-ratio-threshold=0.5
		15.perform-Allelic-Tests=true
		16.perform-Genotypic-Tests=true
		17.perform-Trend-Tests=true
		[/script]
		*/
		//</editor-fold>

		GWASinOneGOParams gwasParams = new GWASinOneGOParams();

		// checking study
		int studyId = prepareStudy(args.get(1), false);
		boolean studyExists = checkStudy(studyId);

		if (studyExists) {
			int matrixId = Integer.parseInt(args.get(2)); // Parent Matrix Id
			String gwasName = args.get(3);
			boolean useExternalPhenoFile = Boolean.parseBoolean(args.get(4));
			File phenoFile = null;
			if (useExternalPhenoFile) {
				phenoFile = new File(args.get(5));
			}

			gwasParams.setDiscardGTMismatches(true);
			gwasParams.setDiscardMarkerByMisRat(Boolean.parseBoolean(args.get(6)));
			gwasParams.setDiscardMarkerMisRatVal(Double.parseDouble(args.get(7)));
			gwasParams.setDiscardMarkerHWCalc(Boolean.parseBoolean(args.get(8)));
			gwasParams.setDiscardMarkerHWFree(Boolean.parseBoolean(args.get(9)));
			gwasParams.setDiscardMarkerHWTreshold(Double.parseDouble(args.get(10)));
			gwasParams.setDiscardSampleByMisRat(Boolean.parseBoolean(args.get(11)));
			gwasParams.setDiscardSampleMisRatVal(Double.parseDouble(args.get(12)));
			gwasParams.setDiscardSampleByHetzyRat(Boolean.parseBoolean(args.get(13)));
			gwasParams.setDiscardSampleHetzyRatVal(Double.parseDouble(args.get(14)));
			gwasParams.setPerformAllelicTests(Boolean.parseBoolean(args.get(15)));
			gwasParams.setPerformGenotypicTests(Boolean.parseBoolean(args.get(16)));
			gwasParams.setPerformTrendTests(Boolean.parseBoolean(args.get(17)));
			gwasParams.setFriendlyName(gwasName);
			gwasParams.setProceed(true);

			List<OPType> necessaryOPs = new ArrayList<OPType>();
			necessaryOPs.add(OPType.SAMPLE_QA);
			necessaryOPs.add(OPType.MARKER_QA);
			List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, matrixId);

			// QA block
			if (gwasParams.isProceed() && missingOPs.size() > 0) {
				gwasParams.setProceed(false);
				System.out.println(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
				MultiOperations.doMatrixQAs(studyId, matrixId);
			}

			// GWAS block
			if (gwasParams.isProceed()) {
				System.out.println(Text.All.processing);
				MultiOperations.doGWASwithAlterPhenotype(studyId,
						matrixId,
						phenoFile,
						gwasParams);
				return true;
			}
		}

		return false;
	}
}

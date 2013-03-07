package org.gwaspi.cli;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.threadbox.MultiOperations;

/**
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
class LoadGenotypesDoGwasInOneGoScriptCommand extends AbstractScriptCommand {

	LoadGenotypesDoGwasInOneGoScriptCommand() {
		super("load_genotypes_do_gwas_in_one_go");
	}

	@Override
	public boolean execute(List<String> args) throws IOException {

		// <editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		#This is a demo file
		#Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/GWASpi/data/
		[script]
		0.command=load_genotypes_do_gwas_in_one_go
		1.study-id=1
		2.format=PLINK
		3.use-dummy-samples=true
		4.new-matrix-name=Matrix 43
		5.description=Load genotypes of batch 42, perform GWAS in one go.
		6.file1-path=/GWASpi/input/Plink/mi_input.map
		7.file2-path=/GWASpi/input/Plink/mi_input.ped
		8.sample-info-path=no info file
		9.discard-marker-by-missing-ratio=false
		10.discard-marker-missing-ratio-threshold=0
		11.calculate-discard-threshold-for-HW=false
		12.discard-marker-with-provided-threshold=true
		13.discard-marker-HW-treshold=0.0000005
		14.discard-samples-by-missing-ratio=false
		15.discard-samples-missing-ratio-threshold=0
		16.discard-samples-by-heterozygosity-ratio=false
		17.discard-samples-heterozygosity-ratio-threshold=0.5
		18.perform-Allelic-Tests=true
		19.perform-Genotypic-Tests=true
		20.perform-Trend-Tests=true
		[/script]
		*/
		// </editor-fold>

		GWASinOneGOParams gwasParams = new GWASinOneGOParams();

		// checking study
		int studyId = prepareStudy(args.get(1), true);
		boolean studyExists = checkStudy(studyId);

		if (studyExists) {
			ImportFormat format = ImportFormat.compareTo(args.get(2));
			String newMatrixName = args.get(4);
			String description = args.get(5);

			gwasParams.setDiscardGTMismatches(true);
			gwasParams.setDiscardMarkerByMisRat(Boolean.parseBoolean(args.get(9)));
			gwasParams.setDiscardMarkerMisRatVal(Double.parseDouble(args.get(10)));
			gwasParams.setDiscardMarkerHWCalc(Boolean.parseBoolean(args.get(11)));
			gwasParams.setDiscardMarkerHWFree(Boolean.parseBoolean(args.get(12)));
			gwasParams.setDiscardMarkerHWTreshold(Double.parseDouble(args.get(13)));
			gwasParams.setDiscardSampleByMisRat(Boolean.parseBoolean(args.get(14)));
			gwasParams.setDiscardSampleMisRatVal(Double.parseDouble(args.get(15)));
			gwasParams.setDiscardSampleByHetzyRat(Boolean.parseBoolean(args.get(16)));
			gwasParams.setDiscardSampleHetzyRatVal(Double.parseDouble(args.get(17)));
			gwasParams.setPerformAllelicTests(Boolean.parseBoolean(args.get(18)));
			gwasParams.setPerformGenotypicTests(Boolean.parseBoolean(args.get(19)));
			gwasParams.setPerformTrendTests(Boolean.parseBoolean(args.get(20)));
			gwasParams.setFriendlyName(newMatrixName);
			gwasParams.setProceed(true);

			GenotypesLoadDescription loadDescription = new GenotypesLoadDescription(
					args.get(6), // File 1
					args.get(8), // Sample Info file
					args.get(7), // File 2
					studyId, // StudyId
					format, // Format
					newMatrixName, // New Matrix name
					description, // Description
					gwasParams.getChromosome(),
					gwasParams.getStrandType(),
					gwasParams.getGtCode() // Gt code (deprecated)
					);
			MultiOperations.loadMatrixDoGWASifOK(
					loadDescription, // Format
					Boolean.parseBoolean(args.get(3)), // Dummy samples
					true, // Do GWAS
					gwasParams); // gwasParams (dummy)

			return true;
		}

		return false;
	}
}

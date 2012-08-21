package org.gwaspi.cli;

import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.global.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.threadbox.MultiOperations;

/**
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class CliExecutor {

	private CliExecutor() {
	}

	public static boolean execute(File scriptFile) throws IOException {
		boolean success = false;

		//GET ALL SCRIPTS CONTAINED IN FILE
		ArrayList<Object> scriptsAL = org.gwaspi.cli.Utils.readArgsFromScript(scriptFile);

		System.out.println("\nScripts in queue: " + scriptsAL.size());

		//ITERATE THROUGH SCRIPTS AND LAUNCH THREAD FOR EACH
		for (int i = 0; i < scriptsAL.size(); i++) {

			//TRY TO GARBAGE COLLECT BEFORE ANY OTHER THING
			System.gc();

			//GET ARGS FOR CURRENT SCRIPT
			ArrayList<String> args = (ArrayList) scriptsAL.get(i);
			//GET COMMAND LINE OF CURRENT SCRIPT
			String command = args.get(0).toString();

			System.out.println("Script " + i + ": " + command);

			//<editor-fold defaultstate="collapsed" desc="LOADERS">
			if (command.equals("load_genotypes")) {

				GWASinOneGOParams gwasParams = new GWASinOneGOParams();

				//CHECKING STUDY
				int studyId = Integer.MIN_VALUE;
				try {
					studyId = Integer.parseInt(args.get(1)); //Study Id
				} catch (Exception e) {
					if (args.get(1).contains("New Study")) {
						studyId = addStudy(args.get(1), "Study created by command-line interface");
					}
				}
				boolean studyExists = checkStudy(studyId);


				String format = args.get(2);
				String newMatrixName = args.get(4);
				String description = args.get(5);


				MultiOperations.loadMatrixDoGWASifOK(format, //Format
						Boolean.parseBoolean(args.get(3)), //Dummy samples
						JOptionPane.NO_OPTION, //Do GWAS
						newMatrixName, //New Matrix name
						description, //Description
						args.get(6), //File 1
						args.get(8), //Sample Info file
						args.get(7), //File 2
						gwasParams.chromosome, //Chr (deprecated)
						gwasParams.strandType, //Strand type (not necessary)
						gwasParams.gtCode, //Gt code (deprecated)
						studyId, //StudyId
						gwasParams);                     //gwasParams (dummy)
				success = true;
			}

			if (command.equals("load_genotypes_do_gwas_in_one_go")) {

				// <editor-fold defaultstate="collapsed/expanded" desc="">
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


				// CHECKING STUDY
				int studyId = Integer.MIN_VALUE;
				try {
					studyId = Integer.parseInt(args.get(1)); //Study Id
				} catch (Exception e) {
					if (args.get(1).contains("New Study")) {
						studyId = addStudy(args.get(1), "Study created by command-line interface");
					}
				}
				boolean studyExists = checkStudy(studyId);


				String format = args.get(2);
				String newMatrixName = args.get(4);
				String description = args.get(5);

				gwasParams.discardGTMismatches = true;
				gwasParams.discardMarkerByMisRat = Boolean.parseBoolean(args.get(9));
				gwasParams.discardMarkerMisRatVal = Double.parseDouble(args.get(10));
				gwasParams.discardMarkerHWCalc = Boolean.parseBoolean(args.get(11));
				gwasParams.discardMarkerHWFree = Boolean.parseBoolean(args.get(12));
				gwasParams.discardMarkerHWTreshold = Double.parseDouble(args.get(13));
				gwasParams.discardSampleByMisRat = Boolean.parseBoolean(args.get(14));
				gwasParams.discardSampleMisRatVal = Double.parseDouble(args.get(15));
				gwasParams.discardSampleByHetzyRat = Boolean.parseBoolean(args.get(16));
				gwasParams.discardSampleHetzyRatVal = Double.parseDouble(args.get(17));
				gwasParams.performAllelicTests = Boolean.parseBoolean(args.get(18));
				gwasParams.performGenotypicTests = Boolean.parseBoolean(args.get(19));
				gwasParams.performTrendTests = Boolean.parseBoolean(args.get(20));
				gwasParams.friendlyName = newMatrixName;
				gwasParams.proceed = true;

				MultiOperations.loadMatrixDoGWASifOK(format, // Format
						Boolean.parseBoolean(args.get(3)), // Dummy samples
						JOptionPane.YES_OPTION, // Do GWAS
						newMatrixName, // New Matrix name
						description, // Description
						args.get(6), // File 1
						args.get(8), // Sample Info file
						args.get(7), // File 2
						gwasParams.chromosome, // Chr (deprecated)
						gwasParams.strandType, // Strand type (not necessary)
						gwasParams.gtCode, // Gt code (deprecated)
						studyId, // StudyId
						gwasParams); // gwasParams (dummy)
				success = true;
			}
			//</editor-fold>


			//<editor-fold defaultstate="collapsed" desc="GWAS IN ONE GO">

			//<editor-fold defaultstate="collapsed" desc="SCRIPT EXAMPLE">
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

			if (command.equals("gwas_in_one_go")) {
				GWASinOneGOParams gwasParams = new GWASinOneGOParams();


				// CHECKING STUDY
				int studyId = Integer.MIN_VALUE;
				try {
					studyId = Integer.parseInt(args.get(1)); // Study Id
				} catch (Exception e) {
					System.out.println("The Study-id must be an integer value of an existing Study!");
				}
				boolean studyExists = checkStudy(studyId);

				int matrixId = Integer.parseInt(args.get(2)); // Parent Matrix Id
				String gwasName = args.get(3);
				boolean useExternalPhenoFile = Boolean.parseBoolean(args.get(4)); //Use external phenotype file?
				File phenoFile = null;
				if (useExternalPhenoFile) {
					phenoFile = new File(args.get(5));
				}

				gwasParams.discardGTMismatches = true;
				gwasParams.discardMarkerByMisRat = Boolean.parseBoolean(args.get(6));
				gwasParams.discardMarkerMisRatVal = Double.parseDouble(args.get(7));
				gwasParams.discardMarkerHWCalc = Boolean.parseBoolean(args.get(8));
				gwasParams.discardMarkerHWFree = Boolean.parseBoolean(args.get(9));
				gwasParams.discardMarkerHWTreshold = Double.parseDouble(args.get(10));
				gwasParams.discardSampleByMisRat = Boolean.parseBoolean(args.get(11));
				gwasParams.discardSampleMisRatVal = Double.parseDouble(args.get(12));
				gwasParams.discardSampleByHetzyRat = Boolean.parseBoolean(args.get(13));
				gwasParams.discardSampleHetzyRatVal = Double.parseDouble(args.get(14));
				gwasParams.performAllelicTests = Boolean.parseBoolean(args.get(15));
				gwasParams.performGenotypicTests = Boolean.parseBoolean(args.get(16));
				gwasParams.performTrendTests = Boolean.parseBoolean(args.get(17));
				gwasParams.friendlyName = gwasName;
				gwasParams.proceed = true;

				ArrayList necessaryOPsAL = new ArrayList();
				necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
				necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
				ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

				// QA BLOCK
				if (gwasParams.proceed && missingOPsAL.size() > 0) {
					gwasParams.proceed = false;
					System.out.println(org.gwaspi.global.Text.Operation.warnQABeforeAnything + "\n" + org.gwaspi.global.Text.Operation.willPerformOperation);
					MultiOperations.doMatrixQAs(studyId, matrixId);
				}

				// GWAS BLOCK
				if (gwasParams.proceed) {
					System.out.println(org.gwaspi.global.Text.All.processing);
					MultiOperations.doGWASwithAlterPhenotype(studyId,
							matrixId,
							phenoFile,
							gwasParams);
					success = true;
				}

			}
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="GT FREQ. & HW">

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

			if (command.equals("genotype_frequency_hardy_weinberg")) {
				GWASinOneGOParams gwasParams = new GWASinOneGOParams();

				// CHECKING STUDY
				int studyId = Integer.MIN_VALUE;
				try {
					studyId = Integer.parseInt(args.get(1)); //Study Id
				} catch (Exception e) {
					System.out.println("The Study-id must be an integer value of an existing Study!");
				}
				boolean studyExists = checkStudy(studyId);

				int matrixId = Integer.parseInt(args.get(2)); //Parent Matrix Id
				String gtFrqName = args.get(3);
				boolean useExternalPhenoFile = Boolean.parseBoolean(args.get(4)); //Use external phenotype file?
				File phenoFile = null;
				if (useExternalPhenoFile) {
					phenoFile = new File(args.get(5));
				}

				// TODO This looks like to much thresholds for the task
				gwasParams.discardGTMismatches = true;
				gwasParams.discardMarkerByMisRat = Boolean.parseBoolean(args.get(6));
				gwasParams.discardMarkerMisRatVal = Double.parseDouble(args.get(7));
				gwasParams.discardSampleByMisRat = Boolean.parseBoolean(args.get(8));
				gwasParams.discardSampleMisRatVal = Double.parseDouble(args.get(9));
				gwasParams.friendlyName = gtFrqName;
				gwasParams.proceed = true;

				ArrayList necessaryOPsAL = new ArrayList();
				necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
				necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
				ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

				// QA BLOCK
				if (gwasParams.proceed && missingOPsAL.size() > 0) {
					gwasParams.proceed = false;
					System.out.println(org.gwaspi.global.Text.Operation.warnQABeforeAnything + "\n" + org.gwaspi.global.Text.Operation.willPerformOperation);
					MultiOperations.doMatrixQAs(studyId, matrixId);
				}

				// GT FREQ. & HW BLOCK
				if (gwasParams.proceed) {
					System.out.println(org.gwaspi.global.Text.All.processing);
					MultiOperations.doGTFreqDoHW(studyId,
							matrixId,
							phenoFile,
							gwasParams);
					success = true;
				}

			}
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="ALLELIC TEST">

			//<editor-fold defaultstate="collapsed" desc="SCRIPT EXAMPLE">
			/*
			#This is a demo file
			#Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
			data-dir=/media/data/GWASpi
			[script]
			0.command=allelic_association
			1.study-id=1
			2.matrix-id=8
			3.gtfreq-id=46
			4.hw-id=47
			5.calculate-discard-threshold-for-HW=false
			6.discard-marker-with-provided-HW-threshold=true
			7.discard-marker-HW-treshold=0.0000005
			[/script]
			*/
			//</editor-fold>


			if (command.equals("allelic_association")) {
				GWASinOneGOParams gwasParams = new GWASinOneGOParams();

				// CHECKING STUDY
				int studyId = Integer.MIN_VALUE;
				try {
					studyId = Integer.parseInt(args.get(1)); //Study Id
				} catch (Exception e) {
					System.out.println("The Study-id must be an integer value of an existing Study!");
				}
				boolean studyExists = checkStudy(studyId);

				int matrixId = Integer.parseInt(args.get(2)); // Parent Matrix Id
				int gtFreqId = Integer.parseInt(args.get(3)); // Parent GtFreq Id
				int hwId = Integer.parseInt(args.get(4)); // Parent GtFreq Id

				gwasParams.performAllelicTests = true;
				gwasParams.performGenotypicTests = false;
				gwasParams.performTrendTests = false;

				gwasParams.discardGTMismatches = true;
				gwasParams.discardMarkerHWCalc = Boolean.parseBoolean(args.get(5));
				gwasParams.discardMarkerHWFree = Boolean.parseBoolean(args.get(6));
				gwasParams.discardMarkerHWTreshold = Double.parseDouble(args.get(7));
				gwasParams.proceed = true;

				ArrayList necessaryOPsAL = new ArrayList();
				necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
				necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
				ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

				// QA BLOCK
				if (gwasParams.proceed && missingOPsAL.size() > 0) {
					gwasParams.proceed = false;
					System.out.println(org.gwaspi.global.Text.Operation.warnQABeforeAnything + "\n" + org.gwaspi.global.Text.Operation.willPerformOperation);
					MultiOperations.doMatrixQAs(studyId, matrixId);
				}

				// ALLELIC ALLELICTEST BLOCK
				if (gwasParams.proceed) {
					System.out.println(org.gwaspi.global.Text.All.processing);
					MultiOperations.doAllelicAssociationTest(studyId,
							matrixId,
							gtFreqId,
							hwId,
							gwasParams);
					success = true;
				}

			}
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="GENOTYPIC TEST">

			//<editor-fold defaultstate="collapsed" desc="SCRIPT EXAMPLE">
			/*
			#This is a demo file
			#Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
			data-dir=/media/data/GWASpi
			[script]
			0.command=genotypic_association
			1.study-id=1
			2.matrix-id=8
			3.gtfreq-id=46
			4.hw-id=47
			5.calculate-discard-threshold-for-HW=false
			6.discard-marker-with-provided-HW-threshold=true
			7.discard-marker-HW-treshold=0.0000005
			[/script]
			*/
			//</editor-fold>

			if (command.equals("genotypic_association")) {
				GWASinOneGOParams gwasParams = new GWASinOneGOParams();

				// CHECKING STUDY
				int studyId = Integer.MIN_VALUE;
				try {
					studyId = Integer.parseInt(args.get(1)); // Study Id
				} catch (Exception e) {
					System.out.println("The Study-id must be an integer value of an existing Study!");
				}
				boolean studyExists = checkStudy(studyId);

				int matrixId = Integer.parseInt(args.get(2)); // Parent Matrix Id
				int gtFreqId = Integer.parseInt(args.get(3)); // Parent GtFreq Id
				int hwId = Integer.parseInt(args.get(4)); // Parent GtFreq Id

				gwasParams.performAllelicTests = false;
				gwasParams.performGenotypicTests = true;
				gwasParams.performTrendTests = false;

				gwasParams.discardGTMismatches = true;
				gwasParams.discardMarkerHWCalc = Boolean.parseBoolean(args.get(5));
				gwasParams.discardMarkerHWFree = Boolean.parseBoolean(args.get(6));
				gwasParams.discardMarkerHWTreshold = Double.parseDouble(args.get(7));
				gwasParams.proceed = true;

				ArrayList necessaryOPsAL = new ArrayList();
				necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
				necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
				ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

				// QA BLOCK
				if (gwasParams.proceed && missingOPsAL.size() > 0) {
					gwasParams.proceed = false;
					System.out.println(org.gwaspi.global.Text.Operation.warnQABeforeAnything + "\n" + org.gwaspi.global.Text.Operation.willPerformOperation);
					MultiOperations.doMatrixQAs(studyId, matrixId);
				}

				// GENOTYPIC TEST BLOCK
				if (gwasParams.proceed) {
					System.out.println(org.gwaspi.global.Text.All.processing);
					MultiOperations.doGenotypicAssociationTest(studyId,
							matrixId,
							gtFreqId,
							hwId,
							gwasParams);
					success = true;
				}

			}
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="TREND TEST">

			//<editor-fold defaultstate="collapsed" desc="SCRIPT EXAMPLE">
			/*
			#This is a demo file
			#Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
			data-dir=/media/data/GWASpi
			[script]
			0.command=trend_test
			1.study-id=1
			2.matrix-id=8
			3.gtfreq-id=46
			4.hw-id=47
			5.calculate-discard-threshold-for-HW=false
			6.discard-marker-with-provided-HW-threshold=true
			7.discard-marker-HW-treshold=0.0000005
			[/script]
			*/
			//</editor-fold>

			if (command.equals("trend_test")) {
				GWASinOneGOParams gwasParams = new GWASinOneGOParams();

				// CHECKING STUDY
				int studyId = Integer.MIN_VALUE;
				try {
					studyId = Integer.parseInt(args.get(1)); // Study Id
				} catch (Exception e) {
					System.out.println("The Study-id must be an integer value of an existing Study!");
				}
				boolean studyExists = checkStudy(studyId);

				int matrixId = Integer.parseInt(args.get(2)); // Parent Matrix Id
				int gtFreqId = Integer.parseInt(args.get(3)); // Parent GtFreq Id
				int hwId = Integer.parseInt(args.get(4)); // Parent GtFreq Id

				gwasParams.performAllelicTests = false;
				gwasParams.performGenotypicTests = false;
				gwasParams.performTrendTests = true;

				gwasParams.discardGTMismatches = true;
				gwasParams.discardMarkerHWCalc = Boolean.parseBoolean(args.get(5));
				gwasParams.discardMarkerHWFree = Boolean.parseBoolean(args.get(6));
				gwasParams.discardMarkerHWTreshold = Double.parseDouble(args.get(7));
				gwasParams.proceed = true;

				ArrayList necessaryOPsAL = new ArrayList();
				necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
				necessaryOPsAL.add(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
				ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

				// QA BLOCK
				if (gwasParams.proceed && missingOPsAL.size() > 0) {
					gwasParams.proceed = false;
					System.out.println(org.gwaspi.global.Text.Operation.warnQABeforeAnything + "\n" + org.gwaspi.global.Text.Operation.willPerformOperation);
					MultiOperations.doMatrixQAs(studyId, matrixId);
				}

				// TRend TEST BLOCK
				if (gwasParams.proceed) {
					System.out.println(org.gwaspi.global.Text.All.processing);
					MultiOperations.doTrendTest(studyId,
							matrixId,
							gtFreqId,
							hwId,
							gwasParams);
					success = true;
				}

			}
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="EXPORT MATRIX">
			if (command.equals("export_matrix")) {

				//CHECKING STUDY
				int studyId = Integer.parseInt(args.get(1)); //Study Id
				int matrixId = Integer.parseInt(args.get(2)); //Study Id
				boolean studyExists = checkStudy(studyId);

				String format = args.get(3);

				if (studyExists) {
					MultiOperations.doExportMatrix(studyId, matrixId, ExportFormat.valueOf(format), org.gwaspi.constants.cDBSamples.f_AFFECTION);
					success = true;

				}

			}
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="SAMPLE INFO">
			if (command.equals("update_sample_info")) {

				// CHECKING STUDY
				int studyId = Integer.MIN_VALUE;
				try {
					studyId = Integer.parseInt(args.get(1)); // Study Id
				} catch (Exception e) {
					if (args.get(1).contains("New Study")) {
						studyId = addStudy(args.get(1).substring(10), "Study created by command-line interface");
					}
				}
				boolean studyExists = checkStudy(studyId);

				File sampleInfoFile = new File(args.get(2));
				if (sampleInfoFile != null && sampleInfoFile.exists()) {
					MultiOperations.updateSampleInfo(studyId,
							sampleInfoFile);
					success = true;

				}
			}
			//</editor-fold>
		}
		System.out.println("\n");
		return success;
	}

	//<editor-fold defaultstate="collapsed" desc="STUDY MANAGEMENT">
	public static boolean checkStudy(int studyId) throws IOException {
		boolean studyExists = false;
		Object[][] studyTable = org.gwaspi.model.StudyList.getStudyTable();
		for (int i = 0; i < studyTable.length; i++) {
			if ((Integer) studyTable[i][0] == studyId) {
				studyExists = true;
			}
		}

		if (!studyExists) {
			System.out.println("\n" + Text.Cli.studyNotExist);
			System.out.println(Text.Cli.availableStudies);
			for (int i = 0; i < studyTable.length; i++) {
				System.out.println("Study ID: " + studyTable[i][0]);
				System.out.println("Name: " + studyTable[i][1]);
				System.out.println("Description: " + studyTable[i][2]);
				System.out.println("\n");
			}

			org.gwaspi.gui.StartGWASpi.exit();
		}

		return studyExists;
	}

	public static int addStudy(String newStudyName, String description) throws IOException {
		int newStudyId;

		org.gwaspi.database.StudyGenerator.insertNewStudy(newStudyName, description);

		Object[][] studyTable = org.gwaspi.model.StudyList.getStudyTable();

		newStudyId = (Integer) studyTable[studyTable.length - 1][0];

		return newStudyId;
	}
	//</editor-fold>
}

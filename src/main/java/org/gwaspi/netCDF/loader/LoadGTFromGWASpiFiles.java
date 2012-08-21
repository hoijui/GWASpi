package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.samples.SampleSet;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

/* Hapmap genotypes loader
 * Can load a single file or multiple files, as long as they belong to a single population (CEU, YRI, JPT...)
 * Imports Hapmap genotype files as found on
 * http://hapmap.ncbi.nlm.nih.gov/downloads/genotypes/?N=D
 */
public final class LoadGTFromGWASpiFiles {

	private String gwaspiGTFilePath;
	private String sampleInfoPath;
	private int studyId;
	private String format = cImport.ImportFormat.GWASpi.toString();
	private String friendlyName;
	private String description;
	private String gtCode;
	private MatrixMetadata importMatrixMetadata;
	private org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding guessedGTCode = org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;

	//<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
	public LoadGTFromGWASpiFiles(String _gwaspiGTFilePath,
			String _sampleInfoPath,
			int _studyId,
			String _friendlyName,
			String _description,
			LinkedHashMap _sampleInfoLHM) throws IOException, InvalidRangeException, InterruptedException {

		gwaspiGTFilePath = _gwaspiGTFilePath;
		sampleInfoPath = _sampleInfoPath;
		studyId = _studyId;
		friendlyName = _friendlyName;
		description = _description;

		if (new File(gwaspiGTFilePath).exists()) {
			SampleSet matrixSampleSet = new SampleSet(studyId, "");
			LinkedHashMap matrixSampleSetLHM = matrixSampleSet.getSampleIdSetLHM(gwaspiGTFilePath);

			boolean testExcessSamplesInMatrix = false;
			boolean testExcessSamplesInFile = false;
			for (Iterator it = matrixSampleSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				if (!_sampleInfoLHM.containsKey(key)) {
					testExcessSamplesInMatrix = true;
				}
			}

			for (Iterator it = _sampleInfoLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				if (!matrixSampleSetLHM.containsKey(key)) {
					testExcessSamplesInFile = true;
				}
			}

			if (testExcessSamplesInFile) {
				System.out.println("There were Samples in the Sample Info file that are not present in the genotypes file.\n" + org.gwaspi.global.Text.App.appName + " will attempt to ignore them...");
			}
			if (testExcessSamplesInMatrix) {
				System.out.println("Warning!\nSome Samples in the imported genotypes are not described in the Sample Info file!\nData will not be imported!");
			}

			importMatrixMetadata = new MatrixMetadata(gwaspiGTFilePath, _studyId, _friendlyName);

			StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
			descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
			if (!description.isEmpty()) {
				descSB.append("\nDescription: ");
				descSB.append(description);
				descSB.append("\n");
			}
//                descSB.append("\nGenotype encoding: ");
//                descSB.append(importMatrixMetadata.getGenotypeEncoding());
			descSB.append("\n");
			descSB.append("Technology: ");
			descSB.append(importMatrixMetadata.getTechnology());
			descSB.append("\n");
			descSB.append("Markers: " + importMatrixMetadata.getMarkerSetSize() + ", Samples: " + importMatrixMetadata.getSampleSetSize());
			descSB.append("\n");
			descSB.append(Text.Matrix.descriptionHeader2);
			descSB.append(format);
			descSB.append("\n");
			descSB.append(Text.Matrix.descriptionHeader3);
			descSB.append("\n");
			descSB.append(gwaspiGTFilePath);
			descSB.append(" (Matrix file)\n");
			descSB.append(sampleInfoPath);
			descSB.append(" (Sample Info file)\n");

			description = descSB.toString();

			if (importMatrixMetadata.getGwaspiDBVersion().equals(org.gwaspi.global.Config.getConfigValue("CURRENT_GWASPIDB_VERSION", "2.0.2"))) { //COMPARE DATABASE VERSIONS
				if (!testExcessSamplesInMatrix) {
					DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
					org.gwaspi.netCDF.matrices.MatrixManager.insertMatrixMetadata(dBManager,
							studyId,
							friendlyName,
							importMatrixMetadata.getMatrixNetCDFName(),
							importMatrixMetadata.getGenotypeEncoding(),
							Integer.MIN_VALUE,
							Integer.MIN_VALUE,
							"",
							descSB.toString(), //description
							0);
				}
				copyMatrixToGenotypesFolder(studyId, gwaspiGTFilePath, importMatrixMetadata.getMatrixNetCDFName());
			} else {
				generateNewGWASpiDBversionMatrix();
			}

			importMatrixMetadata = new MatrixMetadata(importMatrixMetadata.getMatrixNetCDFName());

		}

	}

	public int processGWASpiGTFiles() {
		if (importMatrixMetadata != null) {
			return importMatrixMetadata.getMatrixId();
		} else {
			return Integer.MIN_VALUE;
		}
	}

	public int generateNewGWASpiDBversionMatrix() throws IOException, InvalidRangeException, InterruptedException {
		int result = Integer.MIN_VALUE;
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();


		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">

		MarkerSet_opt rdMarkerSet = new MarkerSet_opt(importMatrixMetadata.getStudyId(), gwaspiGTFilePath, importMatrixMetadata.getMatrixNetCDFName());
		rdMarkerSet.initFullMarkerIdSetLHM();
		rdMarkerSet.fillMarkerSetLHMWithChrAndPos();
		LinkedHashMap rdMarkerSetLHM = rdMarkerSet.getMarkerIdSetLHM();

		SampleSet rdSampleSet = new SampleSet(importMatrixMetadata.getStudyId(), gwaspiGTFilePath, importMatrixMetadata.getMatrixNetCDFName());
		LinkedHashMap rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();

		System.out.println("Done initializing sorted MarkerSetLHM at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//RETRIEVE CHROMOSOMES INFO
		LinkedHashMap chrSetLHM = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(rdMarkerSetLHM, 0, 1);


		MatrixFactory matrixFactory = new MatrixFactory(studyId,
				cImport.ImportFormat.GWASpi.toString(),
				friendlyName,
				importMatrixMetadata.getDescription(), //description
				importMatrixMetadata.getGenotypeEncoding(),
				importMatrixMetadata.getStrand(),
				0,
				rdSampleSetLHM.size(),
				rdMarkerSetLHM.size(),
				chrSetLHM.size(),
				gwaspiGTFilePath);

		NetcdfFileWriteable wrNcFile = matrixFactory.getNetCDFHandler();

		// create the file
		try {
			wrNcFile.create();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
		}
		//System.out.println("Done creating netCDF handle at "+global.Utils.getMediumDateTimeAsString());
		//</editor-fold>

		// <editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">

		//WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

		int[] sampleOrig = new int[]{0, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		samplesD2 = null;
		System.out.println("Done writing SampleSet to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		//WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		//Chromosome location for each marker
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(rdMarkerSetLHM, 0, cNetCDF.Strides.STRIDE_CHR);
		int[] markersOrig = new int[]{0, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing chromosomes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//Set of chromosomes found in matrix along with number of markersinfo
		org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, chrSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);

		//Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[]{0, 1, 2, 3};
		org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, chrSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);



		//WRITE POSITION METADATA FROM ANNOTATION FILE
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD1ArrayInt(rdMarkerSetLHM, 1);
		int[] posOrig = new int[1];
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing positions to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		//WRITE RSID & MARKERID METADATA FROM METADATALHM
		rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(rdMarkerSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueToD2ArrayChar(rdMarkerSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing MarkerId and RsId to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		//WRITE GT STRAND FROM ANNOTATION FILE
		rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueToD2ArrayChar(rdMarkerSetLHM, cNetCDF.Strides.STRIDE_STRAND);
		int[] gtOrig = new int[]{0, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = null;
		System.out.println("Done writing strand info to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		// </editor-fold>


		//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

		//Iterate through rdSampleSetLHM, use item position to read correct sample GTs into rdMarkerIdSetLHM.
		System.out.println(org.gwaspi.global.Text.All.processing);
		int sampleWrIndex = 0;
		for (Iterator it = rdSampleSetLHM.keySet().iterator(); it.hasNext();) {
			it.next();
			rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleWrIndex);

			//Write MarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
			org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetLHM(), sampleWrIndex);
			if (sampleWrIndex % 100 == 0) {
				System.out.println("Samples copied: " + sampleWrIndex);
			}
			sampleWrIndex++;
		}
		//</editor-fold>



		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
		try {
			//GUESS GENOTYPE ENCODING
			if (guessedGTCode.equals(org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
				guessedGTCode = Utils.detectGTEncoding(rdMarkerSet.getMarkerIdSetLHM());
			} else if (guessedGTCode.equals(org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.O12)) {
				guessedGTCode = Utils.detectGTEncoding(rdMarkerSet.getMarkerIdSetLHM());
			}


			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), guessedGTCode.toString().trim());
			int[] origin = new int[]{0, 0};
			wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			StringBuilder descSB = new StringBuilder(description);
			descSB.append("Genotype encoding: ");
			descSB.append(guessedGTCode);
			DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
			db.updateTable(org.gwaspi.constants.cDBGWASpi.SCH_MATRICES,
					org.gwaspi.constants.cDBMatrix.T_MATRICES,
					new String[]{constants.cDBMatrix.f_DESCRIPTION},
					new Object[]{descSB.toString()},
					new String[]{constants.cDBMatrix.f_ID},
					new Object[]{matrixFactory.getMatrixMetaData().getMatrixId()});

			//CLOSE FILE
			wrNcFile.close();
			result = matrixFactory.getMatrixMetaData().getMatrixId();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
			e.printStackTrace();
		}

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="HELPER METHODS">
	private LinkedHashMap getSampleIds(File hapmapGTFile) throws IOException {

		LinkedHashMap uniqueSamples = new LinkedHashMap();

		FileReader fr = new FileReader(hapmapGTFile.getPath());
		BufferedReader inputAnnotationBr = new BufferedReader(fr);
		String header = inputAnnotationBr.readLine();

		String l;
		String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = cImport.SampleInfo.sampleId; i < hapmapVals.length; i++) {
			uniqueSamples.put(hapmapVals[i], "");
		}

		return uniqueSamples;
	}

	private void copyMatrixToGenotypesFolder(int studyId, String importMatrixPath, String newMatrixCDFName) {
		try {
			String genotypesFolder = org.gwaspi.global.Config.getConfigValue("GTdir", "");
			File pathToStudy = new File(genotypesFolder + "/STUDY_" + studyId);
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(genotypesFolder, "/STUDY_" + studyId);
			}

			File origFile = new File(importMatrixPath);
			File newFile = new File(pathToStudy + "/" + newMatrixCDFName + ".nc");
			if (origFile.exists()) {
				org.gwaspi.global.Utils.copyFile(origFile, newFile);
			}
		} catch (IOException ex) {
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
			Logger.getLogger(LoadGTFromGWASpiFiles.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
			Logger.getLogger(LoadGTFromGWASpiFiles.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void logAsWhole(String startTime, int studyId, String dirPath, String format, String matrixName, String description) throws IOException {
		//LOG OPERATION IN STUDY HISTORY
		StringBuffer operation = new StringBuffer("\nLoaded raw " + format + " genotype data in path " + dirPath + ".\n");
		operation.append("Start Time: " + startTime + "\n");
		operation.append("End Time: " + org.gwaspi.global.Utils.getMediumDateTimeAsString() + ".\n");
		operation.append("Data stored in matrix " + matrixName + ".\n");
		operation.append("Description: " + description + ".\n");
		org.gwaspi.global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
		////////////////////////////////
	}
	//</editor-fold>
}

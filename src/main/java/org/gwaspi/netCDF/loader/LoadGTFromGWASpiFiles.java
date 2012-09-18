package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
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
 * Hapmap genotypes loader
 * Can load a single file or multiple files, as long as they belong to a single population (CEU, YRI, JPT...)
 * Imports Hapmap genotype files as found on
 * http://hapmap.ncbi.nlm.nih.gov/downloads/genotypes/?N=D
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public final class LoadGTFromGWASpiFiles implements GenotypesLoader {

	private String gwaspiGTFilePath;
	private String sampleInfoPath;
	private int studyId;
	private String format;
	private String friendlyName;
	private String description;
	private String gtCode;
	private int hasDictionary;
	private MatrixMetadata importMatrixMetadata;
	private cNetCDF.Defaults.GenotypeEncoding guessedGTCode = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;

	//<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
	public LoadGTFromGWASpiFiles(String _gwaspiGTFilePath,
			String _sampleInfoPath,
			int _studyId,
			String _friendlyName,
			String _description,
			Map<String, Object> _sampleInfoMap)
			throws IOException, InvalidRangeException, InterruptedException
	{
		gwaspiGTFilePath = _gwaspiGTFilePath;
		sampleInfoPath = _sampleInfoPath;
		studyId = _studyId;
		format = cImport.ImportFormat.GWASpi.toString();
		friendlyName = _friendlyName;
		hasDictionary = 0;
		description = _description;

		if (new File(gwaspiGTFilePath).exists()) {
		SampleSet matrixSampleSet = new SampleSet(studyId, "");
		Map<String, Object> matrixSampleSetMap = matrixSampleSet.getSampleIdSetMap(gwaspiGTFilePath);

		boolean testExcessSamplesInMatrix = false;
		boolean testExcessSamplesInFile = false;
		for (String key : matrixSampleSetMap.keySet()) {
			if (!_sampleInfoMap.containsKey(key)) {
				testExcessSamplesInMatrix = true;
				break;
			}
		}

		for (String key : _sampleInfoMap.keySet()) {
			if (!matrixSampleSetMap.containsKey(key)) {
				testExcessSamplesInFile = true;
				break;
			}
		}

		if (testExcessSamplesInFile) {
			System.out.println("There were Samples in the Sample Info file that are not present in the genotypes file.\n" + Text.App.appName + " will attempt to ignore them...");
		}
		if (testExcessSamplesInMatrix) {
			System.out.println("Warning!\nSome Samples in the imported genotypes are not described in the Sample Info file!\nData will not be imported!");
		}

		importMatrixMetadata = new MatrixMetadata(gwaspiGTFilePath, _studyId, _friendlyName);

		///////////// CREATE netCDF-3 FILE ////////////
		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!description.isEmpty()) {
			descSB.append("\nDescription: ");
			descSB.append(description);
			descSB.append("\n");
		}
//		descSB.append("\nStrand: ");
//		descSB.append(strand);
//		descSB.append("\nGenotype encoding: ");
//		descSB.append(importMatrixMetadata.getGenotypeEncoding());
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

		if (importMatrixMetadata.getGwaspiDBVersion().equals(Config.getConfigValue(Config.PROPERTY_CURRENT_GWASPIDB_VERSION, "2.0.2"))) { //COMPARE DATABASE VERSIONS
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

	public int processData() {
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
		rdMarkerSet.initFullMarkerIdSetMap();
		rdMarkerSet.fillMarkerSetMapWithChrAndPos();
		Map<String, Object> rdMarkerSetMap = rdMarkerSet.getMarkerIdSetMap();

		SampleSet rdSampleSet = new SampleSet(importMatrixMetadata.getStudyId(), gwaspiGTFilePath, importMatrixMetadata.getMatrixNetCDFName());
		Map<String, Object> rdSampleSetMap = rdSampleSet.getSampleIdSetMap();

		System.out.println("Done initializing sorted MarkerSetMap at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(rdMarkerSetMap, 0, 1);

		MatrixFactory matrixFactory = new MatrixFactory(studyId,
				format,
				friendlyName,
				importMatrixMetadata.getDescription(), //description
				importMatrixMetadata.getGenotypeEncoding(),
				importMatrixMetadata.getStrand(),
				hasDictionary,
				rdSampleSetMap.size(),
				rdMarkerSetMap.size(),
				chrSetMap.size(),
				gwaspiGTFilePath);

		NetcdfFileWriteable ncfile = matrixFactory.getNetCDFHandler();

		// create the file
		try {
			ncfile.create();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}
		//System.out.println("Done creating netCDF handle at "+global.Utils.getMediumDateTimeAsString());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">
		// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

		int[] sampleOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		samplesD2 = null;
		System.out.println("Done writing SampleSet to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		//Chromosome location for each marker
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(rdMarkerSetMap, 0, cNetCDF.Strides.STRIDE_CHR);
		int[] markersOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing chromosomes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// Set of chromosomes found in matrix along with number of markersinfo
		org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(ncfile, chrSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);

		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[]{0, 1, 2, 3};
		org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(ncfile, chrSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);


		// WRITE POSITION METADATA FROM ANNOTATION FILE
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD1ArrayInt(rdMarkerSetMap, 1);
		int[] posOrig = new int[1];
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing positions to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		//WRITE RSID & MARKERID METADATA FROM METADATAMap
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(rdMarkerSetMap, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueToD2ArrayChar(rdMarkerSetMap, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing MarkerId and RsId to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		//WRITE GT STRAND FROM ANNOTATION FILE
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueToD2ArrayChar(rdMarkerSetMap, cNetCDF.Strides.STRIDE_STRAND);
		int[] gtOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = null;
		System.out.println("Done writing strand info to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		// </editor-fold>

		//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

		//Iterate through rdSampleSetMap, use item position to read correct sample GTs into rdMarkerIdSetMap.
		System.out.println(Text.All.processing);
		int sampleWrIndex = 0;
		for (int i = 0; i < rdSampleSetMap.size(); i++) {
			rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleWrIndex);

			//Write MarkerIdSetMap to A3 ArrayChar and save to wrMatrix
			org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, rdMarkerSet.getMarkerIdSetMap(), sampleWrIndex);
			if (sampleWrIndex % 100 == 0) {
				System.out.println("Samples copied: " + sampleWrIndex);
			}
			sampleWrIndex++;
		}
		//</editor-fold>



		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
		try {
			//GUESS GENOTYPE ENCODING
			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
				guessedGTCode = Utils.detectGTEncoding(rdMarkerSet.getMarkerIdSetMap());
			} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
				guessedGTCode = Utils.detectGTEncoding(rdMarkerSet.getMarkerIdSetMap());
			}

			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), guessedGTCode.toString().trim());
			int[] origin = new int[]{0, 0};
			ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			StringBuilder descSB = new StringBuilder(description);
			descSB.append("Genotype encoding: ");
			descSB.append(guessedGTCode);
			DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			db.updateTable(cDBGWASpi.SCH_MATRICES,
					cDBMatrix.T_MATRICES,
					new String[]{cDBMatrix.f_DESCRIPTION},
					new Object[]{descSB.toString()},
					new String[]{cDBMatrix.f_ID},
					new Object[]{matrixFactory.getMatrixMetaData().getMatrixId()});

			//CLOSE FILE
			ncfile.close();
			result = matrixFactory.getMatrixMetaData().getMatrixId();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="HELPER METHODS">
	private Map<String, Object> getSampleIds(File hapmapGTFile) throws IOException {

		Map<String, Object> uniqueSamples = new LinkedHashMap<String, Object>();

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
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
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
			Dialogs.showWarningDialogue("A table saving error has occurred");
			ex.printStackTrace();
		} catch (Exception ex) {
			Dialogs.showWarningDialogue("A table saving error has occurred");
			ex.printStackTrace();
		}
	}
	//</editor-fold>
}

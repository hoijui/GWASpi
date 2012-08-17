package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import ucar.ma2.*;
import ucar.nc2.*;

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
public class LoadGTFromHapmapFiles_sampleSet {

	private String gtFilePath;
	private String sampleFilePath;
	private LinkedHashMap wrSampleSetLHM = new LinkedHashMap();
	private int studyId;
	private String format = cImport.ImportFormat.HAPMAP.toString();
	private String strand;
	private String friendlyName;
	private String description;
	private String gtCode;
	private org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding guessedGTCode = org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;
	private LinkedHashMap wrMarkerSetLHM = new LinkedHashMap();

	//<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
	public LoadGTFromHapmapFiles_sampleSet(String _gtFilePath,
			String _sampleFilePath,
			int _studyId,
			String _strand,
			String _friendlyName,
			String _gtCode,
			String _description,
			LinkedHashMap _sampleInfoLHM) throws FileNotFoundException, IOException {

		gtFilePath = _gtFilePath;
		sampleFilePath = _sampleFilePath;
		studyId = _studyId;
		strand = _strand;
		friendlyName = _friendlyName;
		gtCode = _gtCode;
		description = _description;

		//TODO: check if real samplefiles coincides with sampleInfoFile
		File hapmapGTFile = new File(gtFilePath);
		if (hapmapGTFile.isDirectory()) {
			File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(gtFilePath, false);
			for (int i = 0; i < gtFilesToImport.length; i++) {
				LinkedHashMap tempSamplesLHM = getHapmapSampleIds(gtFilesToImport[i]);
				wrSampleSetLHM.putAll(tempSamplesLHM);
			}
		} else {
			wrSampleSetLHM = getHapmapSampleIds(hapmapGTFile);
		}




	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="PROCESS GENOTYPES">
	public int processHapmapGTFile() throws FileNotFoundException, IOException, InvalidRangeException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">

		MetadataLoaderHapmap markerSetLoader = new MetadataLoaderHapmap(gtFilePath, format, studyId);
		wrMarkerSetLHM = markerSetLoader.getSortedMarkerSetWithMetaData();

		System.out.println("Done initializing sorted MarkerSetLHM at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		///////////// CREATE netCDF-3 FILE ////////////
		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!description.isEmpty()) {
			descSB.append("\nDescription: ");
			descSB.append(description);
			descSB.append("\n");
		}
//        descSB.append("\nStrand: ");
//        descSB.append(strand);
//        descSB.append("\n");
//        descSB.append("Genotype encoding: ");
//        descSB.append(gtCode);
		descSB.append("\n");
		descSB.append("Markers: " + wrMarkerSetLHM.size() + ", Samples: " + wrSampleSetLHM.size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(format);
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(gtFilePath);
		descSB.append(" (Genotype file)\n");
		if (new File(sampleFilePath).exists()) {
			descSB.append(sampleFilePath);
			descSB.append(" (Sample Info file)\n");
		}

		//RETRIEVE CHROMOSOMES INFO
		LinkedHashMap chrSetLHM = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerSetLHM, 2, 3);

		MatrixFactory matrixFactory = new MatrixFactory(studyId,
				format,
				friendlyName,
				descSB.toString(), //description
				gtCode,
				cNetCDF.Defaults.StrandType.FWD.toString(), //Affymetrix standard
				1,
				wrSampleSetLHM.size(),
				wrMarkerSetLHM.size(),
				chrSetLHM.size(),
				gtFilePath);

		NetcdfFileWriteable ncfile = matrixFactory.getNetCDFHandler();

		// create the file
		try {
			ncfile.create();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}
		//System.out.println("Done creating netCDF handle at "+global.Utils.getMediumDateTimeAsString());
		//</editor-fold>


		// <editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">
		//WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(wrSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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

		//WRITE RSID & MARKERID METADATA FROM METADATALHM
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrMarkerSetLHM, 1, cNetCDF.Strides.STRIDE_MARKER_NAME);

		int[] markersOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrMarkerSetLHM, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing MarkerId and RsId to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrMarkerSetLHM, 2, cNetCDF.Strides.STRIDE_CHR);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing chromosomes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//Set of chromosomes found in matrix along with number of markersinfo
		org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(ncfile, chrSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);

		//Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[]{0, 1, 2, 3};
		org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(ncfile, chrSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);



		//WRITE POSITION METADATA FROM ANNOTATION FILE
		//markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrMarkerSetLHM, 3, cNetCDF.Strides.STRIDE_POS);
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD1ArrayInt(wrMarkerSetLHM, 3);
		int[] posOrig = new int[1];
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing positions to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());



		//WRITE FWD STRAND DICTIONARY ALLELES METADATA FROM ANNOTATION FILE
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrMarkerSetLHM, 5, cNetCDF.Strides.STRIDE_GT);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing forward alleles to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		//WRITE GT STRAND FROM ANNOTATION FILE
		//TODO: Strand info is buggy in Hapmap bulk download!
		int[] gtOrig = new int[]{0, 0};
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrMarkerSetLHM, 4, cNetCDF.Strides.STRIDE_STRAND);

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


		// <editor-fold defaultstate="collapsed" desc="MATRIX GENOTYPES LOAD ">

		//Index MarkerIdLHM
		int count = 0;
		for (Iterator it2 = wrMarkerSetLHM.keySet().iterator(); it2.hasNext();) {
			Object key = it2.next();
			wrMarkerSetLHM.put(key, count);
			count++;
		}

		int dataStartRow = cImport.Genotypes.Hapmap_Standard.dataStartRow;

		//<editor-fold defaultstate="collapsed" desc="GET CURRENT MARKER SAMPLESET">

		FileReader inputFileReader = new FileReader(gtFilePath);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String header = null;
		for (int j = 0; j < dataStartRow; j++) {
			header = inputBufferReader.readLine();
		}
		String[] headerFields = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		LinkedHashMap sampleOrderLHM = new LinkedHashMap();
		for (int j = cImport.Genotypes.Hapmap_Standard.sampleId; j < headerFields.length; j++) {
			sampleOrderLHM.put(j, headerFields[j]);
		}

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		StringBuilder sb = new StringBuilder(gtStride);
		for (int j = 0; j < sb.capacity(); j++) {
			sb.append('0');
		}

		//GET ALLELES
		String l;
		int rowCount = 0;
		while ((l = inputBufferReader.readLine()) != null) {

			//MEMORY LEAN METHOD
			StringTokenizer st = new StringTokenizer(l, cImport.Separators.separators_SpaceTab_rgxp);
			String currentMarkerId = st.nextToken();
			int markerIdIndex = (Integer) wrMarkerSetLHM.get(currentMarkerId);


			//read genotypes from this point on
			int k = 1;
			byte[] tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
			while (k < (sampleOrderLHM.size() + cImport.Genotypes.Hapmap_Standard.sampleId)) {
				if (k < cImport.Genotypes.Hapmap_Standard.sampleId) {
					st.nextToken();
					k++;
				} else {
					String strAlleles = st.nextToken();
					if (strAlleles.equals((cImport.Genotypes.Hapmap_Standard.missing))) {
						tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
					} else {
						tmpAlleles = new byte[]{(byte) strAlleles.charAt(0), (byte) strAlleles.charAt(1)};
					}
					k++;
				}

			}

			st = null;


			if (guessedGTCode.equals(org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
				guessedGTCode = Utils.detectGTEncoding(wrSampleSetLHM);
			} else if (guessedGTCode.equals(org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.O12)) {
				guessedGTCode = Utils.detectGTEncoding(wrSampleSetLHM);
			}



			/////////// WRITING GENOTYPE DATA INTO netCDF FILE ////////////
			//TODO: BEST TO USE HYPERSLAB
			org.gwaspi.netCDF.operations.Utils.saveSingleMarkerGTsToMatrix(ncfile, wrMarkerSetLHM, markerIdIndex);

			rowCount++;
			if (rowCount == 0) {
				System.out.println(org.gwaspi.global.Text.All.processing);
			} else if (rowCount % 100000 == 0) {
				System.out.println("Done processing " + rowCount + " markers at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
			}

		}

		//</editor-fold>


		System.out.println("Done writing genotypes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
		// </editor-fold>


		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
		try {
			//GUESS GENOTYPE ENCODING
			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), guessedGTCode.toString().trim());
			int[] origin = new int[]{0, 0};
			ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

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
			ncfile.close();
			result = matrixFactory.getMatrixMetaData().getMatrixId();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}

		logAsWhole(startTime, studyId, gtFilePath, format, friendlyName, description);

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	public LinkedHashMap loadIndividualFiles(File file,
			String currSampleId,
			LinkedHashMap wrSampleSetLHM) throws FileNotFoundException, IOException, InvalidRangeException {

		int dataStartRow = cImport.Genotypes.Hapmap_Standard.dataStartRow;
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String header = null;
		for (int i = 0; i < dataStartRow; i++) {
			header = inputBufferReader.readLine();
		}
		String[] headerFields = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		LinkedHashMap sampleOrderLHM = new LinkedHashMap();
		for (int i = cImport.Genotypes.Hapmap_Standard.sampleId; i < headerFields.length; i++) {
			sampleOrderLHM.put(headerFields[i], i);
		}
		Object sampleColumnNb = sampleOrderLHM.get(currSampleId);

		//GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {


			//MEMORY LEAN METHOD
			if (!sampleColumnNb.equals(null)) {
				StringTokenizer st = new StringTokenizer(l, cImport.Separators.separators_SpaceTab_rgxp);
				String markerId = st.nextToken();

				//read genotypes from this point on
				int k = 1;
				byte[] tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
				while (k <= (Integer) sampleColumnNb) {
					if (k < (Integer) sampleColumnNb) {
						st.nextToken();
						k++;
					}
					if (k == (Integer) sampleColumnNb) {
						String strAlleles = st.nextToken();
						if (strAlleles.equals((cImport.Genotypes.Hapmap_Standard.missing))) {
							tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
						} else {
							tmpAlleles = new byte[]{(byte) strAlleles.charAt(0), (byte) strAlleles.charAt(1)};
						}
						k++;
					}
				}
				wrSampleSetLHM.put(markerId, tmpAlleles);
				st = null;
			}

		}

		if (guessedGTCode.equals(org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
			guessedGTCode = Utils.detectGTEncoding(wrSampleSetLHM);
		} else if (guessedGTCode.equals(org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(wrSampleSetLHM);
		}

		return wrSampleSetLHM;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="HELPER METHODS">
	private LinkedHashMap getHapmapSampleIds(File hapmapGTFile) throws FileNotFoundException, IOException {

		LinkedHashMap uniqueSamples = new LinkedHashMap();

		FileReader fr = new FileReader(hapmapGTFile.getPath());
		BufferedReader inputAnnotationBr = new BufferedReader(fr);
		String header = inputAnnotationBr.readLine();

		String l;
		String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = cImport.Genotypes.Hapmap_Standard.sampleId; i < hapmapVals.length; i++) {
			uniqueSamples.put(hapmapVals[i], "");
		}

		return uniqueSamples;
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

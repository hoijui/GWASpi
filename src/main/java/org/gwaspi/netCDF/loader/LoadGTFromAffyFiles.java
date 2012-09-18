package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.netCDF.matrices.MatrixFactory;
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
public class LoadGTFromAffyFiles implements GTFilesLoader {

	static interface Standard {
		// ProbesetID, Call, Confidence, Signal A, Signal B, Forced Call

		public static final int markerId = 0;
		public static final int alleles = 1; // Caution, using normal Call, not Forced Call!
		public static final String missing = "NoCall";
		public static final int score = 2;
		public static final int intensity_A = 3;
		public static final int intensity_B = 4;
	}

	private String gtDirPath;
	private String sampleFilePath;
	private String annotationFilePath;
	private List<String> samplesAL = new ArrayList<String>();
	private List<String> sampleInfoAL = new ArrayList<String>();
	private int studyId;
	private String format;
	private String friendlyName;
	private String description;
	private String gtCode;
	private String matrixStrand;
	private int hasDictionary;
	private cNetCDF.Defaults.GenotypeEncoding guessedGTCode = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;

	//<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
	public LoadGTFromAffyFiles(String _gtDirPath,
			String _sampleFilePath,
			String _annotationFilePath,
			int _studyId,
			String _format,
			String _friendlyName,
			String _gtCode,
			String _description,
			Map<String, Object> _sampleInfoMap)
	{
		gtDirPath = _gtDirPath;
		sampleFilePath = _sampleFilePath;
		annotationFilePath = _annotationFilePath;
		studyId = _studyId;
		format = _format;
		friendlyName = _friendlyName;
		gtCode = _gtCode;
		matrixStrand = cNetCDF.Defaults.StrandType.PLSMIN.toString();
		hasDictionary = 1;
		description = _description;

		sampleInfoAL.addAll(_sampleInfoMap.keySet());
	}
	//</editor-fold>

	public int processData() throws IOException, InvalidRangeException, InterruptedException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(gtDirPath, false);


		//<editor-fold defaultstate="collapsed" desc="SAMPLES GATHERING">
		//GET SAMPLES FROM FILES
		for (int i = 0; i < gtFilesToImport.length; i++) {
			String sampleId;
			switch (cImport.ImportFormat.compareTo(format)) {
				case Affymetrix_GenomeWide6:
					sampleId = getAffySampleId(gtFilesToImport[i]);
					break;
				default:
					sampleId = "";
					break;
			}
			samplesAL.add(sampleId);
		}

		//COMPARE SAMPLE INFO LIST TO AVAILABLE FILES
		samplesAL.containsAll(sampleInfoAL);
		//</editor-fold>

		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">
		MetadataLoaderAffy markerSetLoader = new MetadataLoaderAffy(annotationFilePath, format, studyId);
		Map<String, Object> markerSetMap = markerSetLoader.getSortedMarkerSetWithMetaData();

		System.out.println("Done initializing sorted MarkerSetMap at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		///////////// CREATE netCDF-3 FILE ////////////
		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!description.isEmpty()) {
			descSB.append("\nDescription: ");
			descSB.append(description);
			descSB.append("\n");
		}
//		descSB.append("\nGenotype encoding: ");
//		descSB.append(gtCode);
		descSB.append("\n");
		descSB.append("Markers: ").append(markerSetMap.size()).append(", Samples: ").append(samplesAL.size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(format);
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(gtDirPath);
		descSB.append(" (Genotype files)\n");
		descSB.append("\n");
		descSB.append(annotationFilePath);
		descSB.append(" (Annotation file)\n");
		if (new File(sampleFilePath).exists()) {
			descSB.append(sampleFilePath);
			descSB.append(" (Sample Info file)\n");
		}

		//RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetMap, 2, 5);

		MatrixFactory matrixFactory = new MatrixFactory(studyId,
				format,
				friendlyName,
				descSB.toString(), //description
				gtCode,
				matrixStrand, // Affymetrix standard
				hasDictionary,
				samplesAL.size(),
				markerSetMap.size(),
				chrSetMap.size(),
				gtDirPath);

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
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeALToD2ArrayChar(samplesAL, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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

		// WRITE RSID & MARKERID METADATA FROM METADATAMap
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 1, cNetCDF.Strides.STRIDE_MARKER_NAME);

		int[] markersOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing MarkerId and RsId to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		//Chromosome location for each marker
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 2, cNetCDF.Strides.STRIDE_CHR);

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
		//markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 5, cNetCDF.Strides.STRIDE_POS);
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD1ArrayInt(markerSetMap, 5);
		int[] posOrig = new int[1];
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing positions to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//WRITE FORWARD STRAND ALLELE DICTIONARY METADATA FROM ANNOTATION FILE
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 7, cNetCDF.Strides.STRIDE_GT);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing forward alleles to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//GENERATE PLUS STRAND ALLELES METADATA FROM ANNOTATION FILE
//        for (Iterator it=markerSetMap.keySet().iterator(); it.hasNext();) {
//            Object key = it.next();
//            Object[] values = (Object[]) markerSetMap.get(key);
//
//            values = Utils.fixPlusAlleles(values, 6, 7); //values, strand, alleles
//
//            markerSetMap.put(values[0], values);
//        }
//        markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 7, cNetCDF.Strides.STRIDE_GT);
//
//        try {
//            ncfile.write(cNetCDF.Variables.VAR_MARKERS_PLS_BASES, markersOrig, markersD2);
//        } catch (IOException e) {
//            System.err.println("ERROR writing file");
//        } catch (InvalidRangeException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Done writing plus alleles to matrix at "+global.Utils.getMediumDateTimeAsString());

		// WRITE GT STRAND FROM ANNOTATION FILE
		int[] gtOrig = new int[]{0, 0};
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 6, cNetCDF.Strides.STRIDE_STRAND);

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
		//PURGE markerSetMap
		for (Map.Entry<String, Object> entry : markerSetMap.entrySet()) {
			entry.setValue(cNetCDF.Defaults.DEFAULT_GT);
		}

		//START PROCESS OF LOADING GENOTYPES
		for (int i = 0; i < gtFilesToImport.length; i++) {
			//System.out.println("Input file: "+i);
			loadIndividualFiles(gtFilesToImport[i],
					ncfile,
					markerSetMap,
					samplesAL);

			if (i == 0) {
				System.out.println(Text.All.processing);
			} else if (i % 10 == 0) {
				System.out.println("Done processing sample Nº" + i + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
			}
		}

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

	public void loadIndividualFiles(File file,
			NetcdfFileWriteable ncfile,
			Map<String, Object> sortedMarkerSetMap,
			List<String> samplesAL)
			throws IOException, InvalidRangeException
	{
		//INIT Maps
		for (String markerId : sortedMarkerSetMap.keySet()) {
			sortedMarkerSetMap.put(markerId, cNetCDF.Defaults.DEFAULT_GT);
		}

		////////////// LOAD INPUT FILE ////////////////
		//GET SAMPLEID
		String sampleId = getAffySampleId(file);

		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		//Skip header rows
		String header = null;
		while (header == null) {
			header = inputBufferReader.readLine();
			if (header.startsWith("#")) {
				header = null;
			}
		}


		//GET ALLELES
		Map<String, byte[]> tempMarkerSet = new HashMap<String, byte[]>();
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			String[] cVals = l.split(cImport.Separators.separators_CommaTab_rgxp);

			byte[] alleles;
			switch (cImport.ImportFormat.compareTo(format)) {
				case Affymetrix_GenomeWide6:
					if (cVals[Standard.alleles].equals(Standard.missing)) {
						alleles = cNetCDF.Defaults.DEFAULT_GT;
					} else {
						alleles = new byte[]{(byte) (cVals[Standard.alleles].charAt(0)),
							(byte) (cVals[Standard.alleles].charAt(1))};
					}
					break;
				default:
					alleles = new byte[cNetCDF.Strides.STRIDE_GT];
					break;
			}

			tempMarkerSet.put(cVals[Standard.markerId], alleles);
		}

		for (String key : sortedMarkerSetMap.keySet()) {
			byte[] value = tempMarkerSet.containsKey(key) ? tempMarkerSet.get(key) : cNetCDF.Defaults.DEFAULT_GT;
			sortedMarkerSetMap.put(key, value);
		}
		if (tempMarkerSet != null) {
			tempMarkerSet.clear();
		}


		if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
			guessedGTCode = Utils.detectGTEncoding(sortedMarkerSetMap);
		} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(sortedMarkerSetMap);
		}

		/////////// WRITING GENOTYPE DATA INTO netCDF FILE ////////////
		int sampleIndex = samplesAL.indexOf(sampleId);
		if (sampleIndex != -1) {  //CHECK IF CURRENT FILE IS NOT PRESENT IN SAMPLEINFO FILE!!
			org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, sortedMarkerSetMap, sampleIndex);
		}

	}

	private static String getAffySampleId(File fileToScan) throws IOException {
//		FileReader inputFileReader = new FileReader(fileToScan);
//		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);
		String l = fileToScan.getName();
		String sampleId;
		int end = l.lastIndexOf(".birdseed-v2");
		if (end != -1) {
			sampleId = l.substring(0, end);
		} else {
			sampleId = l.substring(0, l.indexOf('.'));
		}

//		String[] cVals = l.split("_");
//		String sampleId = cVals[preprocessing.cFormats.sampleId];

		return sampleId;
	}
}

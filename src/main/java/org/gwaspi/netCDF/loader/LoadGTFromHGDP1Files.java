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
import java.util.LinkedHashMap;
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
public class LoadGTFromHGDP1Files implements GTFilesLoader {

	private static interface Standard {

		public static final int markerId = 0;
		public static final int genotypes = 1;
		public static final String missing = "--";
	}

	private String gtFilePath;
	private String sampleFilePath;
	private String annotationFilePath;
	private Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
	private int studyId;
	private String format = cImport.ImportFormat.HGDP1.toString();
	private String chromosome;
	private String strand;
	private String friendlyName;
	private String description;
	private String gtCode;
	private String matrixStrand;
	private int hasDictionary;
	private cNetCDF.Defaults.GenotypeEncoding guessedGTCode = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;

	//<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
	public LoadGTFromHGDP1Files(String _gtFilePath,
			String _sampleFilePath,
			String _annotationFilePath,
			int _studyId,
			String _chromosome,
			String _strand,
			String _friendlyName,
			String _gtCode,
			String _description,
			Map<String, Object> _sampleInfoMap)
			throws IOException
	{
		gtFilePath = _gtFilePath;
		sampleFilePath = _sampleFilePath;
		annotationFilePath = _annotationFilePath;
		studyId = _studyId;
		chromosome = _chromosome;
		strand = _strand;
		friendlyName = _friendlyName;
		gtCode = _gtCode;
		matrixStrand = cNetCDF.Defaults.StrandType.UNKNOWN.toString();
		hasDictionary = 0;
		description = _description;

		sampleInfoMap = _sampleInfoMap;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="PROCESS GENOTYPES">
	public int processData() throws IOException, InvalidRangeException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();


		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">
		MetadataLoaderHGDP1 markerSetLoader = new MetadataLoaderHGDP1(annotationFilePath, strand, studyId);
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
//		descSB.append("\nStrand: ");
//		descSB.append(strand);
//		descSB.append("\nGenotype encoding: ");
//		descSB.append(gtCode);
		descSB.append("\n");
		descSB.append("Markers: ").append(markerSetMap.size()).append(", Samples: ").append(sampleInfoMap.size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(format);
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(gtFilePath);
		descSB.append(" (Genotype file)\n");
		descSB.append(annotationFilePath);
		descSB.append(" (Marker file)\n");
		if (new File(sampleFilePath).exists()) {
			descSB.append(sampleFilePath);
			descSB.append(" (Sample Info file)\n");
		}

		//RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetMap, 2, 3);

		MatrixFactory matrixFactory = new MatrixFactory(studyId,
				format,
				friendlyName,
				descSB.toString(), //description
				gtCode,
				matrixStrand, // Affymetrix standard
				hasDictionary,
				sampleInfoMap.size(),
				markerSetMap.size(),
				chrSetMap.size(),
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

		//<editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">
		// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(sampleInfoMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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
		//markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(wrMarkerSetMap, 3, cNetCDF.Strides.STRIDE_POS);
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD1ArrayInt(markerSetMap, 3);
		int[] posOrig = new int[1];
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing positions to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		// WRITE GT STRAND FROM ANNOTATION FILE
		int[] gtOrig = new int[]{0, 0};
		String strandFlag;
		switch (cNetCDF.Defaults.StrandType.compareTo(strand)) {
			case PLUS:
				strandFlag = cImport.StrandFlags.strandPLS;
				break;
			case MINUS:
				strandFlag = cImport.StrandFlags.strandMIN;
				break;
			case FWD:
				strandFlag = cImport.StrandFlags.strandFWD;
				break;
			case REV:
				strandFlag = cImport.StrandFlags.strandREV;
				break;
			default:
				strandFlag = cImport.StrandFlags.strandUNK;
				break;
		}
		for (Map.Entry<String, Object> entry : markerSetMap.entrySet()) {
			entry.setValue(strandFlag);
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueToD2ArrayChar(markerSetMap, cNetCDF.Strides.STRIDE_STRAND);

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

		int sampleIndex = 0;
		for (String sampleId : sampleInfoMap.keySet()) {
			//PURGE MarkerIdMap
			for (Map.Entry<String, Object> entry : markerSetMap.entrySet()) {
				entry.setValue(cNetCDF.Defaults.DEFAULT_GT);
			}

			try {
				loadIndividualFiles(new File(gtFilePath),
						sampleId,
						markerSetMap);

				// WRITING GENOTYPE DATA INTO netCDF FILE
				org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, markerSetMap, sampleIndex);
			} catch (IOException ex) {
				//NOTHING
			} catch (InvalidRangeException ex) {
				//NOTHING
			}

			sampleIndex++;
			if (sampleIndex == 1) {
				System.out.println(Text.All.processing);
			} else if (sampleIndex % 100 == 0) {
				System.out.println("Done processing sample Nº" + sampleIndex + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
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

		AbstractLoadGTFromFiles.logAsWhole(startTime, studyId, gtFilePath, format, friendlyName, description);

		org.gwaspi.global.Utils.sysoutCompleted("writing data to Matrix");
		return result;
	}

	public void loadIndividualFiles(File file,
			String currSampleId,
			Map<String, Object> markerSetMap)
			throws IOException, InvalidRangeException
	{
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		StringBuilder sb = new StringBuilder(gtStride);
		for (int i = 0; i < sb.capacity(); i++) {
			sb.append('0');
		}

		Map<String, Object> tempMarkerIdMap = new LinkedHashMap<String, Object>();
		Map<String, Object> sampleOrderMap = new LinkedHashMap<String, Object>();

		String sampleHeader = inputBufferReader.readLine();
		String[] headerFields = null;
		headerFields = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
		for (int i = 0; i < headerFields.length; i++) {
			if (!headerFields[i].isEmpty()) {
				sampleOrderMap.put(headerFields[i], i);
			}
		}

		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			//GET ALLELES FROM MARKER ROWS
			String[] cVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
			String currMarkerId = cVals[Standard.markerId];

			Object columnNb = sampleOrderMap.get(currSampleId);
			if (!columnNb.equals(null)) { // FIXME use == instead of equals!?
				String strAlleles = cVals[(Integer) columnNb];
				if (strAlleles.equals(Standard.missing)) {
					tempMarkerIdMap.put(currMarkerId, cNetCDF.Defaults.DEFAULT_GT);
				} else {
					byte[] tmpAlleles = new byte[]{(byte) strAlleles.charAt(0),
						(byte) strAlleles.charAt(0)};
					tempMarkerIdMap.put(currMarkerId, tmpAlleles);
				}
			}
		}

		markerSetMap.putAll(tempMarkerIdMap);

		if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
			guessedGTCode = Utils.detectGTEncoding(markerSetMap);
		} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(markerSetMap);
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="HELPER METHODS">
	private Map<String, Object> getBeagleSampleIds(File hapmapGTFile) throws IOException {

		Map<String, Object> uniqueSamples = new LinkedHashMap<String, Object>();

		FileReader fr = new FileReader(hapmapGTFile.getPath());
		BufferedReader inputBeagleBr = new BufferedReader(fr);

		String sampleHeader = "";
		boolean gotSamples = false;
		while (!gotSamples) {
			String l = inputBeagleBr.readLine();
			if (l == null) {
				break;
			}
			if (l.startsWith("I")) {
				sampleHeader = l;
				gotSamples = true;
			}
		}

		String[] beagleSamples = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = 2; i < beagleSamples.length; i++) {
			uniqueSamples.put(beagleSamples[i], "");
		}

		return uniqueSamples;
	}
	//</editor-fold>
}

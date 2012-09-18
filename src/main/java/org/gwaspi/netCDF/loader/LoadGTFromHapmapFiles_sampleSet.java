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
import java.util.StringTokenizer;
import org.gwaspi.netCDF.matrices.MatrixFactory;
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
public class LoadGTFromHapmapFiles_sampleSet implements GenotypesLoader {

	private String gtFilePath;
	private String sampleFilePath;
	private Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
	private int studyId;
	private String format = cImport.ImportFormat.HAPMAP.toString();
	private String strand;
	private String friendlyName;
	private String description;
	private String gtCode;
	private String matrixStrand;
	private int hasDictionary;
	private cNetCDF.Defaults.GenotypeEncoding guessedGTCode = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;

	//<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
	public LoadGTFromHapmapFiles_sampleSet(String _gtFilePath,
			String _sampleFilePath,
			int _studyId,
			String _strand,
			String _friendlyName,
			String _gtCode,
			String _description,
			Map<String, Object> _sampleInfoMap)
			throws IOException
	{
		gtFilePath = _gtFilePath;
		sampleFilePath = _sampleFilePath;
		studyId = _studyId;
		strand = _strand;
		friendlyName = _friendlyName;
		gtCode = _gtCode;
		matrixStrand = cNetCDF.Defaults.StrandType.FWD.toString();
		hasDictionary = 1;
		description = _description;

		// TODO check if real samplefiles coincides with sampleInfoFile
		File hapmapGTFile = new File(gtFilePath);
		if (hapmapGTFile.isDirectory()) {
			File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(gtFilePath, false);
			for (int i = 0; i < gtFilesToImport.length; i++) {
				Map<String, Object> tempSamplesMap = getHapmapSampleIds(gtFilesToImport[i]);
				sampleInfoMap.putAll(tempSamplesMap);
			}
		} else {
			sampleInfoMap = getHapmapSampleIds(hapmapGTFile);
		}

	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="PROCESS GENOTYPES">
	public int processData() throws IOException, InvalidRangeException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">

		MetadataLoaderHapmap markerSetLoader = new MetadataLoaderHapmap(gtFilePath, format, studyId);
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
		if (new File(sampleFilePath).exists()) {
			descSB.append(sampleFilePath);
			descSB.append(" (Sample Info file)\n");
		}

		//RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetMap, 2, 3);

		MatrixFactory matrixFactory = new MatrixFactory(studyId,
				format,
				friendlyName,
				descSB.toString(), // description
				gtCode,
				matrixStrand,
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

		//WRITE FWD STRAND DICTIONARY ALLELES METADATA FROM ANNOTATION FILE
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 5, cNetCDF.Strides.STRIDE_GT);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing forward alleles to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// WRITE GT STRAND FROM ANNOTATION FILE
		// TODO Strand info is buggy in Hapmap bulk download!
		int[] gtOrig = new int[]{0, 0};
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 4, cNetCDF.Strides.STRIDE_STRAND);

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

		//Index MarkerIdMap
		int count = 0;
		for (Map.Entry<String, Object> entry : markerSetMap.entrySet()) {
			entry.setValue(count);
			count++;
		}

		int dataStartRow = LoadGTFromHapmapFiles.Standard.dataStartRow;

		//<editor-fold defaultstate="collapsed" desc="GET CURRENT MARKER SAMPLESET">

		FileReader inputFileReader = new FileReader(gtFilePath);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String header = null;
		for (int j = 0; j < dataStartRow; j++) {
			header = inputBufferReader.readLine();
		}
		String[] headerFields = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		Map<String, Object> sampleOrderMap = new LinkedHashMap<String, Object>();
		for (int j = LoadGTFromHapmapFiles.Standard.sampleId; j < headerFields.length; j++) {
			sampleOrderMap.put(j + "", headerFields[j]);
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
			int markerIdIndex = (Integer) markerSetMap.get(currentMarkerId);


			//read genotypes from this point on
			int k = 1;
			byte[] tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
			while (k < (sampleOrderMap.size() + LoadGTFromHapmapFiles.Standard.sampleId)) {
				if (k < LoadGTFromHapmapFiles.Standard.sampleId) {
					st.nextToken();
					k++;
				} else {
					String strAlleles = st.nextToken();
					if (strAlleles.equals((LoadGTFromHapmapFiles.Standard.missing))) {
						tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
					} else {
						tmpAlleles = new byte[]{(byte) strAlleles.charAt(0), (byte) strAlleles.charAt(1)};
					}
					k++;
				}
			}

			st = null;


			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
				guessedGTCode = Utils.detectGTEncoding(sampleInfoMap);
			} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
				guessedGTCode = Utils.detectGTEncoding(sampleInfoMap);
			}

			/////////// WRITING GENOTYPE DATA INTO netCDF FILE ////////////
			//TODO: BEST TO USE HYPERSLAB
			org.gwaspi.netCDF.operations.Utils.saveSingleMarkerGTsToMatrix(ncfile, markerSetMap, markerIdIndex);

			rowCount++;
			if (rowCount == 1) {
				System.out.println(Text.All.processing);
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

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="HELPER METHODS">
	private Map<String, Object> getHapmapSampleIds(File hapmapGTFile) throws IOException {

		Map<String, Object> uniqueSamples = new LinkedHashMap<String, Object>();

		FileReader fr = new FileReader(hapmapGTFile.getPath());
		BufferedReader inputAnnotationBr = new BufferedReader(fr);
		String header = inputAnnotationBr.readLine();

		String l;
		String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = LoadGTFromHapmapFiles.Standard.sampleId; i < hapmapVals.length; i++) {
			uniqueSamples.put(hapmapVals[i], "");
		}

		return uniqueSamples;
	}
	//</editor-fold>
}

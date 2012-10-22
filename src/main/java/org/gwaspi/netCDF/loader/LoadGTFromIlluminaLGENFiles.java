package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class LoadGTFromIlluminaLGENFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromIlluminaLGENFiles.class);

	private static interface Standard {

		public static final int familyId = 0;
		public static final int sampleId = 1;
		public static final int markerId = 2;
		public static final int allele1 = 3;
		public static final int allele2 = 4;
		public static final String missing = "-";
	}

	public LoadGTFromIlluminaLGENFiles() {
	}

	@Override
	public ImportFormat getFormat() {
		return ImportFormat.Illumina_LGEN;
	}

	@Override
	public StrandType getMatrixStrand() {
		return StrandType.PLSMIN;
	}

	@Override
	public boolean isHasDictionary() {
		return false;
	}

	@Override
	public int getMarkersD2ItemNb() {
		throw new UnsupportedOperationException("Not supported yet."); // FIXME
	}

	@Override
	public String getMarkersD2Variables() {
		throw new UnsupportedOperationException("Not supported yet."); // FIXME
	}

	@Override
	public int processData(GenotypesLoadDescription loadDescription, Map<String, Object> sampleInfo) throws IOException, InvalidRangeException, InterruptedException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		List<String> sampleInfoList = new ArrayList<String>();
		sampleInfoList.addAll(sampleInfo.keySet());

		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());

		//<editor-fold defaultstate="collapsed" desc="CREATE MARKERSET & NETCDF">
		MetadataLoaderIlluminaLGEN markerSetLoader = new MetadataLoaderIlluminaLGEN(loadDescription.getAnnotationFilePath(), loadDescription.getStudyId());
		Map<String, Object> markerSetMap = markerSetLoader.getSortedMarkerSetWithMetaData();

		log.info("Done initializing sorted MarkerSetMap");

		// CREATE netCDF-3 FILE
		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!loadDescription.getDescription().isEmpty()) {
			descSB.append("\nDescription: ");
			descSB.append(loadDescription.getDescription());
			descSB.append("\n");
		}
//		descSB.append("\nGenotype encoding: ");
//		descSB.append(gtCode);
		descSB.append("\n");
		descSB.append("Markers: ").append(markerSetMap.size()).append(", Samples: ").append(sampleInfoList.size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(loadDescription.getFormat());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(loadDescription.getGtDirPath());
		descSB.append(" (Genotype files)\n");
		descSB.append("\n");
		descSB.append(loadDescription.getAnnotationFilePath());
		descSB.append(" (Annotation file)\n");
		if (new File(loadDescription.getSampleFilePath()).exists()) {
			descSB.append(loadDescription.getSampleFilePath());
			descSB.append(" (Sample Info file)\n");
		}

		//RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetMap, 2, 3);

		MatrixFactory matrixFactory = new MatrixFactory(
				loadDescription.getStudyId(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				descSB.toString(), // description
				loadDescription.getGtCode(),
				(getMatrixStrand() != null) ? getMatrixStrand() : loadDescription.getStrand(),
				isHasDictionary(),
				sampleInfo.size(),
				markerSetMap.size(),
				chrSetMap.size(),
				loadDescription.getGtDirPath());

		NetcdfFileWriteable ncfile = matrixFactory.getNetCDFHandler();

		// create the file
		try {
			ncfile.create();
		} catch (IOException ex) {
			log.error("Failed creating file " + ncfile.getLocation(), ex);
		}
		//log.info("Done creating netCDF handle ");
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">
		// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeCollectionToD2ArrayChar(
				sampleInfoList,
				cNetCDF.Strides.STRIDE_SAMPLE_NAME);

		int[] sampleOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		samplesD2 = null;
		log.info("Done writing SampleSet to matrix");

		// WRITE RSID & MARKERID METADATA FROM METADATAMap
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 1, cNetCDF.Strides.STRIDE_MARKER_NAME);

		int[] markersOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing MarkerId and RsId to matrix");

		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		//Chromosome location for each marker
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 2, cNetCDF.Strides.STRIDE_CHR);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing chromosomes to matrix");

		// Set of chromosomes found in matrix along with number of markersinfo
		org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(ncfile, chrSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);

		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[]{0, 1, 2, 3};
		org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(ncfile, chrSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);


		// WRITE POSITION METADATA FROM ANNOTATION FILE
		//markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 5, cNetCDF.Strides.STRIDE_POS);
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD1ArrayInt(markerSetMap, 3);
		int[] posOrig = new int[1];
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing positions to matrix");

		// WRITE GT STRAND FROM ANNOTATION FILE
		int[] gtOrig = new int[]{0, 0};
		String strandFlag = cNetCDF.Defaults.StrandType.FWD.toString();
		for (Map.Entry<String, Object> entry : markerSetMap.entrySet()) {
			entry.setValue(strandFlag);
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueToD2ArrayChar(markerSetMap, cNetCDF.Strides.STRIDE_STRAND);

		try {
			ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		markersD2 = null;
		log.info("Done writing strand info to matrix");


		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="MATRIX GENOTYPES LOAD ">
		//START PROCESS OF LOADING GENOTYPES
		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		log.info(Text.All.processing);
		for (int i = 0; i < gtFilesToImport.length; i++) {
			//log.info("Input file: "+i);
			loadIndividualFiles(
					gtFilesToImport[i],
					ncfile,
					markerSetMap,
					sampleInfoList,
					guessedGTCode);

			if (i % 10 == 0) {
				log.info("Done processing file " + i);
			}
		}

		log.info("Done writing genotypes to matrix");
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
		} catch (IOException ex) {
			log.error("Failed creating file " + ncfile.getLocation(), ex);
		}

		AbstractLoadGTFromFiles.logAsWhole(startTime, loadDescription.getStudyId(), loadDescription.getGtDirPath(), loadDescription.getFormat(), loadDescription.getFriendlyName(), loadDescription.getDescription());

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	public void loadIndividualFiles(
			File file,
			NetcdfFileWriteable ncfile,
			Map<String, Object> sortedMarkerSetMap,
			List<String> samplesAL,
			GenotypeEncoding guessedGTCode)
			throws IOException, InvalidRangeException
	{
		// LOAD INPUT FILE
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		//Skip header rows
		String header = "";
		boolean gotHeader = false;
		while (!gotHeader && inputBufferReader.ready()) {
			header = inputBufferReader.readLine();
			if (header.startsWith("[Data]")) {
				header = inputBufferReader.readLine(); //Get next line which is real header
				gotHeader = true;
			}
		}

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		StringBuilder sb = new StringBuilder(gtStride);
		for (int i = 0; i < sb.capacity(); i++) {
			sb.append('0');
		}

		//GET ALLELES
		String l;
		Map<String, Object> tempMarkerSet = new LinkedHashMap<String, Object>();
		String currentSampleId = "";
		while ((l = inputBufferReader.readLine()) != null) {
			String[] cVals = l.split(cImport.Separators.separators_CommaTab_rgxp);

			if (cVals[1].equals(currentSampleId)) {
				byte[] tmpAlleles = new byte[]{(byte) cVals[Standard.allele1].charAt(0),
					(byte) cVals[Standard.allele2].charAt(0)};
				tempMarkerSet.put(cVals[Standard.markerId], tmpAlleles);
			} else {
				if (!currentSampleId.equals("")) { //EXCEPT FIRST TIME ROUND
					//INIT AND PURGE SORTEDMARKERSET Map
					for (Map.Entry<String, Object> entry : sortedMarkerSetMap.entrySet()) {
						entry.setValue(cNetCDF.Defaults.DEFAULT_GT);
					}

					//WRITE Map TO MATRIX
					for (Map.Entry<String, Object> entry : sortedMarkerSetMap.entrySet()) {
						String markerId = entry.getKey();
						byte[] value = (tempMarkerSet.get(markerId) != null) ? (byte[]) tempMarkerSet.get(markerId) : cNetCDF.Defaults.DEFAULT_GT;
						entry.setValue(value);
					}
					if (tempMarkerSet != null) {
						tempMarkerSet.clear();
					}

					// WRITING GENOTYPE DATA INTO netCDF FILE
					int sampleIndex = samplesAL.indexOf(currentSampleId);
					if (sampleIndex != -1) {  //CHECK IF CURRENT FILE IS NOT PRESENT IN SAMPLEINFO FILE!!
						org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, sortedMarkerSetMap, sampleIndex);
					}
				}

				currentSampleId = cVals[1];
				log.info("Loading Sample: " + currentSampleId);

				byte[] tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
				if (cVals[Standard.allele1].equals(Standard.missing)
						&& cVals[Standard.allele2].equals(Standard.missing)) {
					tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
				} else {
					tmpAlleles = new byte[]{(byte) (cVals[Standard.allele1].charAt(0)),
						(byte) (cVals[Standard.allele2].charAt(0))};
				}
				tempMarkerSet.put(cVals[Standard.markerId], tmpAlleles);
			}
		}

		//WRITE LAST SAMPLE Map TO MATRIX
		//INIT AND PURGE SORTEDMARKERSET Map
		for (Map.Entry<String, Object> entry : sortedMarkerSetMap.entrySet()) {
			entry.setValue(cNetCDF.Defaults.DEFAULT_GT);
		}
		for (Map.Entry<String, Object> entry : sortedMarkerSetMap.entrySet()) {
			String markerId = entry.getKey();
			byte[] value = (tempMarkerSet.get(markerId) != null) ? (byte[]) tempMarkerSet.get(markerId) : cNetCDF.Defaults.DEFAULT_GT;
			entry.setValue(value);
		}
		if (tempMarkerSet != null) {
			tempMarkerSet.clear();
		}

		if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
			guessedGTCode = Utils.detectGTEncoding(sortedMarkerSetMap);
		} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(sortedMarkerSetMap);
		}

		// WRITING GENOTYPE DATA INTO netCDF FILE
		int sampleIndex = samplesAL.indexOf(currentSampleId);
		if (sampleIndex != -1) {  //CHECK IF CURRENT FILE IS NOT PRESENT IN SAMPLEINFO FILE!!
			org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, sortedMarkerSetMap, sampleIndex);
		}

	}

	private static String getAffySampleId(File fileToScan) throws IOException {
//        FileReader inputFileReader = new FileReader(fileToScan);
//        BufferedReader inputBufferReader = new BufferedReader(inputFileReader);
		String l = fileToScan.getName();
		String sampleId = l;
		int end = l.lastIndexOf(".birdseed-v2");
		if (end != -1) {
			sampleId = l.substring(0, end);
		} else {
			sampleId = l.substring(0, l.indexOf("."));
		}


//        String[] cVals = l.split("_");
//        String sampleId = cVals[preprocessing.cFormats.sampleId];

		return sampleId;
	}
}

package org.gwaspi.netCDF.loader;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class LoadGTFromHapmapFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromHapmapFiles.class);

	public static interface Standard {

		public static final int dataStartRow = 1;
		public static final int sampleId = 11;
		public static final int markerId = 0;
		public static final int alleles = 1;
		public static final int chr = 2;
		public static final int pos = 3;
		public static final int strand = 4;
		public static final String missing = "NN";
		public static final int score = 10;
	}

	public LoadGTFromHapmapFiles() {
	}

	@Override
	public ImportFormat getFormat() {
		return ImportFormat.HAPMAP;
	}

	@Override
	public StrandType getMatrixStrand() {
		return StrandType.FWD;
	}

	@Override
	public boolean isHasDictionary() {
		return true;
	}

	@Override
	public int getMarkersD2ItemNb() {
		throw new UnsupportedOperationException("Not supported yet."); // FIXME
	}

	@Override
	public String getMarkersD2Variables() {
		throw new UnsupportedOperationException("Not supported yet."); // FIXME
	}

	//<editor-fold defaultstate="collapsed" desc="PROCESS GENOTYPES">
	@Override
	public int processData(GenotypesLoadDescription loadDescription, Map<String, Object> sampleInfo) throws IOException, InvalidRangeException, InterruptedException {

		// TODO check if real samplefiles coincides with sampleInfoFile
		File hapmapGTFile = new File(loadDescription.getGtDirPath());
		if (hapmapGTFile.isDirectory()) {
			File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());
			for (int i = 0; i < gtFilesToImport.length; i++) {
				Map<String, Object> tempSamplesMap = getHapmapSampleIds(gtFilesToImport[i]);
				sampleInfo.putAll(tempSamplesMap);
			}
		} else {
			sampleInfo.putAll(getHapmapSampleIds(hapmapGTFile));
		}

		return processHapmapGTFiles(hapmapGTFile.isDirectory(), loadDescription, sampleInfo);
	}

	private int processHapmapGTFiles(boolean hasManyFiles, GenotypesLoadDescription loadDescription, Map<String, Object> sampleInfo) throws IOException, InvalidRangeException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		File[] gtFilesToImport;
		if (hasManyFiles) {
			gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());
		} else {
			gtFilesToImport = new File[]{new File(loadDescription.getGtDirPath())};
		}

		Map<String, Object> markerSetMap = new LinkedHashMap<String, Object>();

		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">
		for (int i = 0; i < gtFilesToImport.length; i++) {
			MetadataLoaderHapmap markerSetLoader = new MetadataLoaderHapmap(
					gtFilesToImport[i].getPath(),
					loadDescription.getFormat(),
					loadDescription.getStudyId());
			Map<String, Object> tmpMarkerMap = markerSetLoader.getSortedMarkerSetWithMetaData();
			markerSetMap.putAll(tmpMarkerMap);
		}

		log.info("Done initializing sorted MarkerSetMap at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// CREATE netCDF-3 FILE
		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!loadDescription.getDescription().isEmpty()) {
			descSB.append("\nDescription: ");
			descSB.append(loadDescription.getDescription());
			descSB.append("\n");
		}
//		descSB.append("\nStrand: ");
//		descSB.append(strand);
//		descSB.append("\nGenotype encoding: ");
//		descSB.append(gtCode);
		descSB.append("\n");
		descSB.append("Markers: ").append(markerSetMap.size()).append(", Samples: ").append(sampleInfo.size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(loadDescription.getFormat());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(loadDescription.getGtDirPath());
		descSB.append(" (Genotype file)\n");
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
		//log.info("Done creating netCDF handle at "+global.Utils.getMediumDateTimeAsString());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">
		// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(sampleInfo, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

		int[] sampleOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		samplesD2 = null;
		log.info("Done writing SampleSet to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());

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
		log.info("Done writing MarkerId and RsId to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());

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
		log.info("Done writing chromosomes to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());

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
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing positions to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());

		//WRITE FWD STRAND DICTIONARY ALLELES METADATA FROM ANNOTATION FILE
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 5, cNetCDF.Strides.STRIDE_GT);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing forward alleles to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// WRITE GT STRAND FROM ANNOTATION FILE
		// TODO Strand info is buggy in Hapmap bulk download!
		int[] gtOrig = new int[]{0, 0};
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 4, cNetCDF.Strides.STRIDE_STRAND);

		try {
			ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		markersD2 = null;
		log.info("Done writing strand info to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());


		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="MATRIX GENOTYPES LOAD ">

		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		int sampleIndex = 0;
		for (String sampleId : sampleInfo.keySet()) {
			//PURGE MarkerIdMap
			for (Map.Entry<String, Object> entry : markerSetMap.entrySet()) {
				entry.setValue(cNetCDF.Defaults.DEFAULT_GT);
			}

			for (int i = 0; i < gtFilesToImport.length; i++) {
				loadIndividualFiles(
						gtFilesToImport[i],
						sampleId,
						markerSetMap,
						guessedGTCode);

				// WRITING GENOTYPE DATA INTO netCDF FILE
				org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, markerSetMap, sampleIndex);
			}

			sampleIndex++;
			if (sampleIndex == 1) {
				log.info(Text.All.processing);
			} else if (sampleIndex % 100 == 0) {
				log.info("Done processing sample Nº{} at {}", sampleIndex, org.gwaspi.global.Utils.getMediumDateTimeAsString());
			}
		}

		log.info("Done writing genotypes to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
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


		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	private void loadIndividualFiles(File file,
			String currSampleId,
			Map<String, Object> markerSetMap,
			GenotypeEncoding guessedGTCode)
			throws IOException, InvalidRangeException
	{
		int dataStartRow = Standard.dataStartRow;
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String header = null;
		for (int i = 0; i < dataStartRow; i++) {
			header = inputBufferReader.readLine();
		}
		String[] headerFields = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		Map<String, Object> sampleOrderMap = new LinkedHashMap<String, Object>();
		for (int i = Standard.sampleId; i < headerFields.length; i++) {
			sampleOrderMap.put(headerFields[i], i);
		}
		Object sampleColumnNb = sampleOrderMap.get(currSampleId);

		//GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {

			//MEMORY LEAN METHOD
			if (sampleColumnNb != null) {
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
						if (strAlleles.equals(Standard.missing)) {
							tmpAlleles = cNetCDF.Defaults.DEFAULT_GT;
						} else {
							tmpAlleles = new byte[]{(byte) strAlleles.charAt(0), (byte) strAlleles.charAt(1)};
						}
						k++;
					}
				}
				markerSetMap.put(markerId, tmpAlleles);
			}

		}

		if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
			guessedGTCode = Utils.detectGTEncoding(markerSetMap);
		} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(markerSetMap);
		}
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

		for (int i = Standard.sampleId; i < hapmapVals.length; i++) {
			uniqueSamples.put(hapmapVals[i], "");
		}

		return uniqueSamples;
	}
	//</editor-fold>
}

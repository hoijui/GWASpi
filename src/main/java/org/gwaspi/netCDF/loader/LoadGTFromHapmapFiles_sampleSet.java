package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

/**
 * HapMap genotypes loader.
 * Can load a single file or multiple files, as long as they belong to a single population (CEU, YRI, JPT...)
 * Imports Hapmap genotype files as found on
 * http://hapmap.ncbi.nlm.nih.gov/downloads/genotypes/?N=D
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated this class is unused and out of date!
 */
public class LoadGTFromHapmapFiles_sampleSet extends LoadGTFromHapmapFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromHapmapFiles_sampleSet.class);

	public LoadGTFromHapmapFiles_sampleSet() {
	}

	//<editor-fold defaultstate="expanded" desc="PROCESS GENOTYPES">
	@Override
	public int processData(GenotypesLoadDescription loadDescription, Collection<SampleInfo> sampleInfos) throws IOException, InvalidRangeException, InterruptedException {
		int result = Integer.MIN_VALUE;

		// TODO check if real sample files coincides with sampleInfoFile
		File hapmapGTFile = new File(loadDescription.getGtDirPath());
		if (hapmapGTFile.isDirectory()) {
			File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());
			for (int i = 0; i < gtFilesToImport.length; i++) {
				Collection<SampleInfo> tempSamples = getHapmapSampleIds(gtFilesToImport[i]);
				sampleInfos.addAll(tempSamples);
			}
		} else {
			sampleInfos.addAll(getHapmapSampleIds(hapmapGTFile));
		}

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		//<editor-fold defaultstate="expanded" desc="CREATE MARKERSET & NETCDF">

		MetadataLoaderHapmap markerSetLoader = new MetadataLoaderHapmap(
				loadDescription.getGtDirPath(),
				loadDescription.getFormat(),
				loadDescription.getStudyId());
		Map<MarkerKey, Object> markerSetMap = markerSetLoader.getSortedMarkerSetWithMetaData();

		log.info("Done initializing sorted MarkerSetMap");

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
		descSB.append("Markers: ").append(markerSetMap.size()).append(", Samples: ").append(sampleInfos.size());
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

		// RETRIEVE CHROMOSOMES INFO
		Map<MarkerKey, Object> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetMap, 2, 3);

		MatrixFactory matrixFactory = new MatrixFactory(
				loadDescription.getStudyId(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				descSB.toString(), // description
				loadDescription.getGtCode(),
				(getMatrixStrand() != null) ? getMatrixStrand() : loadDescription.getStrand(),
				isHasDictionary(),
				sampleInfos.size(),
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

		//<editor-fold defaultstate="expanded" desc="WRITE MATRIX METADATA">
		// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeCollectionToD2ArrayChar(AbstractLoadGTFromFiles.extractKeys(sampleInfos), cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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
		log.info("Done writing positions to matrix");

		//WRITE FWD STRAND DICTIONARY ALLELES METADATA FROM ANNOTATION FILE
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 5, cNetCDF.Strides.STRIDE_GT);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing forward alleles to matrix");

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
		log.info("Done writing strand info to matrix");


		// </editor-fold>

		// <editor-fold defaultstate="expanded" desc="MATRIX GENOTYPES LOAD ">
		// Index MarkerIdMap
		int count = 0;
		for (Map.Entry<?, Object> entry : markerSetMap.entrySet()) {
			entry.setValue(count);
			count++;
		}

		int dataStartRow = LoadGTFromHapmapFiles.Standard.dataStartRow;

		//<editor-fold defaultstate="expanded" desc="GET CURRENT MARKER SAMPLESET">
		FileReader inputFileReader = new FileReader(loadDescription.getGtDirPath());
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String header = null;
		for (int j = 0; j < dataStartRow; j++) {
			header = inputBufferReader.readLine();
		}
		String[] headerFields = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		Map<String, Object> sampleOrderMap = new LinkedHashMap<String, Object>();
		for (int j = LoadGTFromHapmapFiles.Standard.sampleId; j < headerFields.length; j++) {
			sampleOrderMap.put(String.valueOf(j), headerFields[j]);
		}

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		StringBuilder sb = new StringBuilder(gtStride);
		for (int j = 0; j < sb.capacity(); j++) {
			sb.append('0');
		}

		//GET ALLELES
		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		String l;
		int rowCount = 0;
		while ((l = inputBufferReader.readLine()) != null) {

			//MEMORY LEAN METHOD
			StringTokenizer st = new StringTokenizer(l, cImport.Separators.separators_SpaceTab_rgxp);
			MarkerKey currentMarkerKey = MarkerKey.valueOf(st.nextToken());
			int markerIdIndex = (Integer) markerSetMap.get(currentMarkerKey);


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
						tmpAlleles = new byte[] {
								(byte) strAlleles.charAt(0),
								(byte) strAlleles.charAt(1)};
					}
					k++;
				}
			}

			st = null;

//			if (guessedGTCode.equals(GenotypeEncoding.UNKNOWN)
//					|| guessedGTCode.equals(GenotypeEncoding.O12))
//			{
//				guessedGTCode = Utils.detectGTEncoding(sampleInfos); // FIXME This line .. seems not to make sense.. or is undecypherable due to this class beeing unused/untested since too long already
//			}

			// WRITING GENOTYPE DATA INTO netCDF FILE
			// TODO BEST TO USE HYPERSLAB
			org.gwaspi.netCDF.operations.Utils.saveSingleMarkerGTsToMatrix(ncfile, markerSetMap, markerIdIndex);

			rowCount++;
			if (rowCount == 1) {
				log.info(Text.All.processing);
			} else if (rowCount % 100000 == 0) {
				log.info("Done processing " + rowCount + " markers");
			}
		}
		//</editor-fold>

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
			MatricesList.saveMatrixDescription(
					matrixFactory.getMatrixMetaData().getMatrixId(),
					descSB.toString());

			//CLOSE FILE
			ncfile.close();
			result = matrixFactory.getMatrixMetaData().getMatrixId();
		} catch (IOException ex) {
			log.error("Failed creating file " + ncfile.getLocation(), ex);
		}

		AbstractLoadGTFromFiles.logAsWhole(
				startTime,
				loadDescription.getStudyId(),
				loadDescription.getGtDirPath(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				loadDescription.getDescription());

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPER METHODS">
	private Collection<SampleInfo> getHapmapSampleIds(File hapmapGTFile) throws IOException {

		Collection<SampleInfo> uniqueSamples = new LinkedList<SampleInfo>();

		FileReader fr = new FileReader(hapmapGTFile.getPath());
		BufferedReader inputAnnotationBr = new BufferedReader(fr);
		String header = inputAnnotationBr.readLine();
		inputAnnotationBr.close();

		String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = LoadGTFromHapmapFiles.Standard.sampleId; i < hapmapVals.length; i++) {
			SampleInfo sampleInfo = new SampleInfo(hapmapVals[i]);
			uniqueSamples.add(sampleInfo);
		}

		return uniqueSamples;
	}
	//</editor-fold>
}

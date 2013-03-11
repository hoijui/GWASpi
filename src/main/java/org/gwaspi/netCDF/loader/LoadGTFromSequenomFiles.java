package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
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
public class LoadGTFromSequenomFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromSequenomFiles.class);

	private static interface Standard {

		public static final int sampleId = 0;
		public static final int alleles = 1;
		public static final int markerId = 2;
		public static final int well = 3;
		public static final int qa_desc = 4;
	}

	public LoadGTFromSequenomFiles() {
	}

	@Override
	public ImportFormat getFormat() {
		return ImportFormat.Sequenom;
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
		throw new UnsupportedOperationException("Not supported yet."); // FIXME implement me!
	}

	@Override
	public String getMarkersD2Variables() {
		throw new UnsupportedOperationException("Not supported yet."); // FIXME implement me!
	}

	@Override
	public int processData(GenotypesLoadDescription loadDescription, Collection<SampleInfo> sampleInfos) throws IOException, InvalidRangeException, InterruptedException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());
//		File gtFileToImport = new File(gtDirPath);

		//<editor-fold defaultstate="expanded" desc="CREATE MARKERSET & NETCDF">
		MetadataLoader markerSetLoader = new MetadataLoaderSequenom(loadDescription.getAnnotationFilePath(), loadDescription.getStudyId());

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
		descSB.append(" (Genotype files)\n");
		descSB.append("\n");
		descSB.append(loadDescription.getAnnotationFilePath());
		descSB.append(" (Annotation file)\n");
		if (new File(loadDescription.getSampleFilePath()).exists()) {
			descSB.append(loadDescription.getSampleFilePath());
			descSB.append(" (Sample Info file)\n");
		}

		//RETRIEVE CHROMOSOMES INFO
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
		// WRITE SAMPLESET TO MATRIX FROM SAMPLES LIST
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
		for (Map.Entry<?, Object> entry : markerSetMap.entrySet()) {
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

		// <editor-fold defaultstate="expanded" desc="MATRIX GENOTYPES LOAD ">
		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		// INIT AND PURGE SORTEDMARKERSET Map
		int sampleIndex = 0;
		for (SampleInfo sampleInfo : sampleInfos) {
			// PURGE MarkerIdMap on current sample
			for (Map.Entry<?, Object> entry : markerSetMap.entrySet()) {
				entry.setValue(cNetCDF.Defaults.DEFAULT_GT);
			}

			// PARSE ALL FILES FOR ANY DATA ON CURRENT SAMPLE
			for (int i = 0; i < gtFilesToImport.length; i++) {
				//log.info("Input file: "+i);
				loadIndividualFiles(gtFilesToImport[i],
						sampleInfo.getKey(),
						markerSetMap);
			}

			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
				guessedGTCode = Utils.detectGTEncoding(markerSetMap);
			} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
				guessedGTCode = Utils.detectGTEncoding(markerSetMap);
			}

			// WRITING GENOTYPE DATA INTO netCDF FILE
			ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeMapToCurrentMarkerArrayByteD3(markerSetMap, cNetCDF.Strides.STRIDE_GT);
			int[] origin = new int[]{sampleIndex, 0, 0}; //0,0,0 for 1st Sample ; 1,0,0 for 2nd Sample....
			try {
				ncfile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			}

			sampleIndex++;
			if (sampleIndex == 1) {
				log.info(Text.All.processing);
			} else if (sampleIndex % 100 == 0) {
				log.info("Done processing sample Nº{}", sampleIndex);
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
			MatricesList.saveMatrixDescription(
					matrixFactory.getMatrixMetaData().getMatrixId(),
					descSB.toString());

			//CLOSE FILE
			ncfile.close();
			result = matrixFactory.getMatrixMetaData().getMatrixId();
		} catch (IOException ex) {
			log.error("Failed creating file " + ncfile.getLocation(), ex);
		}

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	/**
	 * @see AbstractLoadGTFromFiles#loadIndividualFiles
	 */
	public void loadIndividualFiles(File file,
			SampleKey sampleKey,
			Map<MarkerKey, Object> alleles)
			throws IOException, InvalidRangeException
	{
		// LOAD INPUT FILE
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		// GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			if (!l.contains("SAMPLE_ID")) { // SKIP ALL HEADER LINES
				String[] cVals = l.split(cImport.Separators.separators_Tab_rgxp);
				String currSampleId = cVals[Standard.sampleId];
				// NOTE The Sequenom format does not have a family-ID
				SampleKey currSampleKey = new SampleKey(currSampleId, SampleKey.FAMILY_ID_NONE);
				if (currSampleKey.equals(sampleKey)) {
					// ONLY PROCESS CURRENT SAMPLEID DATA
					String markerId = cVals[Standard.markerId].trim();
					try {
						Long.parseLong(markerId);
						markerId = "rs" + markerId;
					} catch (Exception ex) {
						log.warn(null, ex); // XXX maybe this is not a problem, but an OK thing?
					}

					String sAlleles = cVals[Standard.alleles];
					if (sAlleles.length() == 0) {
						sAlleles = "00";
					} else if (sAlleles.length() == 1) {
						sAlleles = sAlleles + sAlleles;
					}
					byte[] tmpAlleles = new byte[]{(byte) sAlleles.charAt(0), (byte) sAlleles.charAt(1)};
					alleles.put(MarkerKey.valueOf(markerId), tmpAlleles);
				}
			}
		}
		inputBufferReader.close();
	}
}

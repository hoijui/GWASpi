/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.netCDF.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

public class LoadGTFromAffyFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromAffyFiles.class);

	static interface Standard {
		// ProbesetID, Call, Confidence, Signal A, Signal B, Forced Call

		public static final int markerId = 0;
		public static final int alleles = 1; // Caution, using normal Call, not Forced Call!
		public static final String missing = "NoCall";
		public static final int score = 2;
		public static final int intensity_A = 3;
		public static final int intensity_B = 4;
	}

	public LoadGTFromAffyFiles() {
	}

	@Override
	public ImportFormat getFormat() {
		return ImportFormat.Affymetrix_GenomeWide6;
	}

	@Override
	public StrandType getMatrixStrand() {
		return StrandType.PLSMIN;
	}

	@Override
	public boolean isHasDictionary() {
		return true;
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

		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;

		//<editor-fold defaultstate="expanded" desc="SAMPLES GATHERING">
		// GET SAMPLES FROM FILES
		List<SampleKey> samples = new ArrayList<SampleKey>();
		for (int i = 0; i < gtFilesToImport.length; i++) {
			SampleKey sampleKey;
			switch (loadDescription.getFormat()) {
				case Affymetrix_GenomeWide6:
					sampleKey = getAffySampleId(loadDescription.getStudyId(), gtFilesToImport[i]);
					break;
				default:
					sampleKey = new SampleKey(loadDescription.getStudyId(), "", SampleKey.FAMILY_ID_NONE);
					break;
			}
			// NOTE The Beagle format does not have a family-ID
			samples.add(sampleKey);
		}

		// COMPARE SAMPLE INFO LIST TO AVAILABLE FILES
//		samples.containsAll(loadDescription.getSampleInfo().keySet());
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="CREATE MARKERSET & NETCDF">
		MetadataLoaderAffy markerSetLoader = new MetadataLoaderAffy(
				loadDescription.getAnnotationFilePath(),
				loadDescription.getFormat(),
				loadDescription.getStudyId());
		Map<MarkerKey, MarkerMetadata> markerSetMap = markerSetLoader.getSortedMarkerSetWithMetaData();

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
		descSB.append("Markers: ").append(markerSetMap.size()).append(", Samples: ").append(samples.size());
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
		Map<MarkerKey, int[]> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetMap, 2, 5);

		MatrixFactory matrixFactory = new MatrixFactory(
				loadDescription.getStudyId(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				descSB.toString(), // description
				loadDescription.getGtCode(),
				(getMatrixStrand() != null) ? getMatrixStrand() : loadDescription.getStrand(),
				isHasDictionary(),
				samples.size(),
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
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeCollectionToD2ArrayChar(samples, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, MarkerMetadata.TO_RS_ID, cNetCDF.Strides.STRIDE_MARKER_NAME);

		int[] markersOrig = new int[] {0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, MarkerMetadata.TO_MARKER_ID, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing MarkerId and RsId to matrix");

		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		// Chromosome location for each marker
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, MarkerMetadata.TO_CHR, cNetCDF.Strides.STRIDE_CHR);

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
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD1ArrayInt(markerSetMap, MarkerMetadata.TO_POS);
		int[] posOrig = new int[1];
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing positions to matrix");

		// WRITE FORWARD STRAND ALLELE DICTIONARY METADATA FROM ANNOTATION FILE
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, MarkerMetadata.TO_ALLELES, cNetCDF.Strides.STRIDE_GT);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing forward alleles to matrix");

		// GENERATE PLUS STRAND ALLELES METADATA FROM ANNOTATION FILE
//		for (Iterator it=markerSetMap.keySet().iterator(); it.hasNext();) {
//			Object key = it.next();
//			Object[] values = (Object[]) markerSetMap.get(key);
//
//			values = Utils.fixPlusAlleles(values, 6, 7); //values, strand, alleles
//
//			markerSetMap.put(values[0], values);
//		}
//		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 7, cNetCDF.Strides.STRIDE_GT);
//
//		try {
//			ncfile.write(cNetCDF.Variables.VAR_MARKERS_PLS_BASES, markersOrig, markersD2);
//		} catch (IOException ex) {
//			log.error("Failed writing file", ex);
//		} catch (InvalidRangeException ex) {
//			log.error(null, ex);
//		}
//		log.info("Done writing plus alleles to matrix ");

		// WRITE GT STRAND FROM ANNOTATION FILE
		int[] gtOrig = new int[] {0, 0};
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, MarkerMetadata.TO_STRAND, cNetCDF.Strides.STRIDE_STRAND);

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
		// PURGE alleles
		Map<MarkerKey, byte[]> alleles = AbstractLoadGTFromFiles.fillMap(markerSetMap.keySet(), cNetCDF.Defaults.DEFAULT_GT);

		// START PROCESS OF LOADING GENOTYPES
		for (int i = 0; i < gtFilesToImport.length; i++) {
			//log.info("Input file: "+i);
			loadIndividualFiles(
					loadDescription,
					guessedGTCode,
					gtFilesToImport[i],
					ncfile,
					alleles,
					samples);

			if (i == 0) {
				log.info(Text.All.processing);
			} else if (i % 10 == 0) {
				log.info("Done processing sample Nº" + i);
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

			MatrixMetadata matrixMetaData = matrixFactory.getMatrixMetaData();
			matrixMetaData.setDescription(descSB.toString());
			MatricesList.updateMatrix(matrixMetaData);

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
	private void loadIndividualFiles(
			GenotypesLoadDescription loadDescription,
			GenotypeEncoding guessedGTCode,
			File file,
			NetcdfFileWriteable ncfile,
			Map<MarkerKey, byte[]> sortedAlleles,
			List<SampleKey> samples)
			throws IOException, InvalidRangeException
	{
		// LOAD INPUT FILE
		// GET SAMPLEID
		SampleKey sampleKey = getAffySampleId(loadDescription.getStudyId(), file);

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


		// GET ALLELES
		Map<MarkerKey, byte[]> tempMarkerSet = new HashMap<MarkerKey, byte[]>();
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			String[] cVals = l.split(cImport.Separators.separators_CommaTab_rgxp);

			byte[] alleles;
			switch (loadDescription.getFormat()) {
				case Affymetrix_GenomeWide6:
					if (cVals[Standard.alleles].equals(Standard.missing)) {
						alleles = cNetCDF.Defaults.DEFAULT_GT;
					} else {
						alleles = new byte[] {
							(byte) (cVals[Standard.alleles].charAt(0)),
							(byte) (cVals[Standard.alleles].charAt(1))};
					}
					break;
				default:
					alleles = new byte[cNetCDF.Strides.STRIDE_GT];
					break;
			}

			tempMarkerSet.put(MarkerKey.valueOf(cVals[Standard.markerId]), alleles);
		}
		inputBufferReader.close();

		for (MarkerKey key : sortedAlleles.keySet()) {
			byte[] value = tempMarkerSet.containsKey(key) ? tempMarkerSet.get(key) : cNetCDF.Defaults.DEFAULT_GT;
			sortedAlleles.put(key, value);
		}
		if (tempMarkerSet != null) {
			tempMarkerSet.clear();
		}

		if (guessedGTCode.equals(GenotypeEncoding.UNKNOWN)) {
			guessedGTCode = Utils.detectGTEncoding(sortedAlleles);
		} else if (guessedGTCode.equals(GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(sortedAlleles);
		}

		// WRITING GENOTYPE DATA INTO netCDF FILE
		int sampleIndex = samples.indexOf(sampleKey);
		if (sampleIndex != -1) { // CHECK IF CURRENT FILE IS NOT PRESENT IN SAMPLEINFO FILE!!
			org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, sortedAlleles, sampleIndex);
		}
	}

	private static SampleKey getAffySampleId(int studyId, File fileToScan) throws IOException {
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

		return new SampleKey(studyId, sampleId, SampleKey.FAMILY_ID_NONE);
	}
}

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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
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

/**
 * HapMap genotypes loader.
 * Can load a single file or multiple files, as long as they belong to a single population (CEU, YRI, JPT...)
 * Imports Hapmap genotype files as found on
 * http://hapmap.ncbi.nlm.nih.gov/downloads/genotypes/?N=D
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
	public String getMarkersD2Variables() {
		throw new UnsupportedOperationException("Not supported yet."); // FIXME implement me!
	}

	//<editor-fold defaultstate="expanded" desc="PROCESS GENOTYPES">
	@Override
	public int processData(GenotypesLoadDescription loadDescription, Collection<SampleInfo> sampleInfos) throws IOException, InvalidRangeException, InterruptedException {

		// TODO check if real sample files coincides with sampleInfoFile
		File hapmapGTFile = new File(loadDescription.getGtDirPath());
		if (hapmapGTFile.isDirectory()) {
			File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());
			for (int i = 0; i < gtFilesToImport.length; i++) {
				Collection<SampleInfo> tempSamplesMap = getHapmapSampleIds(gtFilesToImport[i]);
				sampleInfos.addAll(tempSamplesMap);
			}
		} else {
			sampleInfos.addAll(getHapmapSampleIds(hapmapGTFile));
		}

		return processHapmapGTFiles(hapmapGTFile.isDirectory(), loadDescription, sampleInfos);
	}

	private int processHapmapGTFiles(boolean hasManyFiles, GenotypesLoadDescription loadDescription, Collection<SampleInfo> sampleInfos) throws IOException, InvalidRangeException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		File[] gtFilesToImport;
		if (hasManyFiles) {
			gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath());
		} else {
			gtFilesToImport = new File[]{new File(loadDescription.getGtDirPath())};
		}

		Map<MarkerKey, MarkerMetadata> markerSetMap = new LinkedHashMap<MarkerKey, MarkerMetadata>();

		//<editor-fold defaultstate="expanded" desc="CREATE MARKERSET & NETCDF">
		for (int i = 0; i < gtFilesToImport.length; i++) {
			MetadataLoaderHapmap markerSetLoader = new MetadataLoaderHapmap(
					gtFilesToImport[i].getPath(),
					loadDescription.getFormat(),
					loadDescription.getStudyId());
			Map<MarkerKey, MarkerMetadata> tmpMarkerMap = markerSetLoader.getSortedMarkerSetWithMetaData();
			markerSetMap.putAll(tmpMarkerMap);
		}

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

		//RETRIEVE CHROMOSOMES INFO
		Map<MarkerKey, int[]> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetMap, 2, 3);

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
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, MarkerMetadata.TO_RS_ID, cNetCDF.Strides.STRIDE_MARKER_NAME);

		int[] markersOrig = new int[]{0, 0};
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
		//markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(wrMarkerSetMap, 3, cNetCDF.Strides.STRIDE_POS);
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

		// WRITE FWD STRAND DICTIONARY ALLELES METADATA FROM ANNOTATION FILE
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, MarkerMetadata.TO_ALLELES, cNetCDF.Strides.STRIDE_GT);

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

		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		int sampleIndex = 0;
		for (SampleInfo sampleInfo : sampleInfos) {
			// PURGE MarkerIdMap
			Map<MarkerKey, byte[]> alleles = AbstractLoadGTFromFiles.fillMap(markerSetMap.keySet(), cNetCDF.Defaults.DEFAULT_GT);

			for (int i = 0; i < gtFilesToImport.length; i++) {
				loadIndividualFiles(
						gtFilesToImport[i],
						sampleInfo.getKey(),
						alleles,
						guessedGTCode);

				// WRITING GENOTYPE DATA INTO netCDF FILE
				org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, alleles, sampleIndex);
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
	private void loadIndividualFiles(File file,
			SampleKey sampleKey,
			Map<MarkerKey, byte[]> alleles,
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

		Map<SampleKey, Object> sampleOrderMap = new LinkedHashMap<SampleKey, Object>();
		for (int i = Standard.sampleId; i < headerFields.length; i++) {
			sampleOrderMap.put(SampleKey.valueOf(headerFields[i]), i); // FIXME this is only the sampleID, without familyID. does hapMap have a familyId?
		}
		Object sampleColumnNb = sampleOrderMap.get(sampleKey);

		// GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {

			// MEMORY LEAN METHOD
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
							tmpAlleles = new byte[] {
								(byte) strAlleles.charAt(0),
								(byte) strAlleles.charAt(1)};
						}
						k++;
					}
				}
				alleles.put(MarkerKey.valueOf(markerId), tmpAlleles);
			}
		}
		inputBufferReader.close();

		if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
			guessedGTCode = Utils.detectGTEncoding(alleles);
		} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(alleles);
		}
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

		for (int i = Standard.sampleId; i < hapmapVals.length; i++) {
			uniqueSamples.add(new SampleInfo(hapmapVals[i]));
		}

		return uniqueSamples;
	}
	//</editor-fold>
}

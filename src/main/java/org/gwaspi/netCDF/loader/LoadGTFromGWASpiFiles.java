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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

/**
 * Loads genotypes from GWASpi's own file format.
 * Can load a single file or multiple files, as long as they belong to
 * a single population (CEU, YRI, JPT...).
 */
public final class LoadGTFromGWASpiFiles implements GenotypesLoader {

	private final Logger log
			= LoggerFactory.getLogger(LoadGTFromGWASpiFiles.class);

	public LoadGTFromGWASpiFiles() {
	}

	@Override
	public ImportFormat getFormat() {
		return ImportFormat.GWASpi;
	}

	@Override
	public StrandType getMatrixStrand() {
		return null; // unused
	}

	@Override
	public boolean isHasDictionary() {
		return false;
	}

	@Override
	public String getMarkersD2Variables() {
		throw new UnsupportedOperationException("Not supported yet."); // FIXME implement me!
	}

	@Override
	public int processData(GenotypesLoadDescription loadDescription, Collection<SampleInfo> sampleInfos)
			throws IOException, InvalidRangeException, InterruptedException
	{
		int result = Integer.MIN_VALUE;

		if (new File(loadDescription.getGtDirPath()).exists()) {
		SampleSet matrixSampleSet = new SampleSet(loadDescription.getStudyId(), "");
		Map<SampleKey, byte[]> matrixSampleSetMap = matrixSampleSet.getSampleIdSetMapByteArray(loadDescription.getGtDirPath());

		boolean testExcessSamplesInMatrix = false;
		boolean testExcessSamplesInFile = false;
		Collection<SampleKey> sampleKeys = AbstractLoadGTFromFiles.extractKeys(sampleInfos);
		for (SampleKey key : matrixSampleSetMap.keySet()) {
			if (!sampleKeys.contains(key)) {
				testExcessSamplesInMatrix = true;
				break;
			}
		}

		for (SampleInfo sampleInfo : sampleInfos) {
			if (!matrixSampleSetMap.containsKey(sampleInfo.getKey())) {
				testExcessSamplesInFile = true;
				break;
			}
		}

		if (testExcessSamplesInFile) {
			log.info("There were Samples in the Sample Info file that are not present in the genotypes file.\n" + Text.App.appName + " will attempt to ignore them...");
		}
		if (testExcessSamplesInMatrix) {
			log.info("Warning!\nSome Samples in the imported genotypes are not described in the Sample Info file!\nData will not be imported!");
		}

		MatrixMetadata importMatrixMetadata = MatricesList.getMatrixMetadata(loadDescription.getGtDirPath(), loadDescription.getStudyId(), loadDescription.getFriendlyName());

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
//		descSB.append(importMatrixMetadata.getGenotypeEncoding());
		descSB.append("\n");
		descSB.append("Technology: ");
		descSB.append(importMatrixMetadata.getTechnology());
		descSB.append("\n");
		descSB.append("Markers: " + importMatrixMetadata.getMarkerSetSize() + ", Samples: " + importMatrixMetadata.getSampleSetSize());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(loadDescription.getFormat());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(loadDescription.getGtDirPath());
		descSB.append(" (Matrix file)\n");
		descSB.append(loadDescription.getSampleFilePath());
		descSB.append(" (Sample Info file)\n");

		if (importMatrixMetadata.getGwaspiDBVersion().equals(Config.getConfigValue(Config.PROPERTY_CURRENT_GWASPIDB_VERSION, null))) {
			// COMPARE DATABASE VERSIONS
			if (!testExcessSamplesInMatrix) {
				MatricesList.insertMatrixMetadata(new MatrixMetadata(
						loadDescription.getFriendlyName(),
						importMatrixMetadata.getMatrixNetCDFName(),
						descSB.toString(), // description
						importMatrixMetadata.getGenotypeEncoding(),
						loadDescription.getStudyId(),
						Integer.MIN_VALUE,
						Integer.MIN_VALUE,
						""
						));
			}
			copyMatrixToGenotypesFolder(loadDescription.getStudyId(), loadDescription.getGtDirPath(), importMatrixMetadata.getMatrixNetCDFName());
		} else {
			generateNewGWASpiDBversionMatrix(loadDescription, importMatrixMetadata);
		}

		importMatrixMetadata = MatricesList.getMatrixMetadataByNetCDFname(importMatrixMetadata.getMatrixNetCDFName());

		result = importMatrixMetadata.getMatrixId();
		}

		return result;
	}

	private int generateNewGWASpiDBversionMatrix(GenotypesLoadDescription loadDescription, MatrixMetadata importMatrixMetadata)
			throws IOException, InvalidRangeException, InterruptedException
	{
		int result = Integer.MIN_VALUE;
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		//<editor-fold defaultstate="expanded" desc="CREATE MARKERSET & NETCDF">
		MarkerSet rdMarkerSet = new MarkerSet(importMatrixMetadata);
		rdMarkerSet.initFullMarkerIdSetMap();
		rdMarkerSet.fillMarkerSetMapWithChrAndPos();
		Map<MarkerKey, MarkerMetadata> rdMarkerSetMap = rdMarkerSet.getMarkerMetadata();

		SampleSet rdSampleSet = new SampleSet(importMatrixMetadata.getStudyId(), loadDescription.getGtDirPath(), importMatrixMetadata.getMatrixNetCDFName());
		Map<SampleKey, byte[]> rdSampleSetMap = rdSampleSet.getSampleIdSetMapByteArray();

		log.info("Done initializing sorted MarkerSetMap");

		// RETRIEVE CHROMOSOMES INFO
		Map<MarkerKey, int[]> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(rdMarkerSetMap, 0, 1);

		MatrixFactory matrixFactory = new MatrixFactory(
				loadDescription.getStudyId(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				importMatrixMetadata.getDescription(), //description
				importMatrixMetadata.getGenotypeEncoding(),
				importMatrixMetadata.getStrand(),
				isHasDictionary(),
				rdSampleSetMap.size(),
				rdMarkerSetMap.size(),
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
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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


		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		// Chromosome location for each marker
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(rdMarkerSetMap, MarkerMetadata.TO_CHR, cNetCDF.Strides.STRIDE_CHR);
		int[] markersOrig = new int[] {0, 0};
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
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD1ArrayInt(rdMarkerSetMap, MarkerMetadata.TO_POS);
		int[] posOrig = new int[1];
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing positions to matrix");


		// WRITE RSID & MARKERID METADATA FROM METADATAMap
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueToD2ArrayChar(rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing MarkerId and RsId to matrix");


		// WRITE GT STRAND FROM ANNOTATION FILE
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueToD2ArrayChar(rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Strides.STRIDE_STRAND);
		int[] gtOrig = new int[] {0, 0};
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

		//<editor-fold defaultstate="expanded" desc="GENOTYPES WRITER">

		//Iterate through rdSampleSetMap, use item position to read correct sample GTs into rdMarkerIdSetMap.
		log.info(Text.All.processing);
		int sampleWrIndex = 0;
		for (int i = 0; i < rdSampleSetMap.size(); i++) {
			rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleWrIndex);

			//Write MarkerIdSetMap to A3 ArrayChar and save to wrMatrix
			org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, rdMarkerSet.getMarkerIdSetMapByteArray(), sampleWrIndex);
			if (sampleWrIndex % 100 == 0) {
				log.info("Samples copied: " + sampleWrIndex);
			}
			sampleWrIndex++;
		}
		//</editor-fold>



		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		try {
			//GUESS GENOTYPE ENCODING
			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
				guessedGTCode = Utils.detectGTEncoding(rdMarkerSet.getMarkerIdSetMapByteArray());
			} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
				guessedGTCode = Utils.detectGTEncoding(rdMarkerSet.getMarkerIdSetMapByteArray());
			}

			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), guessedGTCode.toString().trim());
			int[] origin = new int[]{0, 0};
			ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			StringBuilder descSB = new StringBuilder(loadDescription.getDescription());
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
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPER METHODS">
	private void copyMatrixToGenotypesFolder(int studyId, String importMatrixPath, String newMatrixCDFName) {
		try {
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
			File pathToStudy = new File(genotypesFolder, "STUDY_" + studyId);
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(pathToStudy);
			}

			File origFile = new File(importMatrixPath);
			File newFile = new File(pathToStudy + "/" + newMatrixCDFName + ".nc");
			if (origFile.exists()) {
				org.gwaspi.global.Utils.copyFile(origFile, newFile);
			}
		} catch (IOException ex) {
			Dialogs.showWarningDialogue("A table saving error has occurred");
			log.error("A table saving error has occurred", ex);
		} catch (Exception ex) {
			Dialogs.showWarningDialogue("A table saving error has occurred");
			log.error("A table saving error has occurred", ex);
		}
	}
	//</editor-fold>
}

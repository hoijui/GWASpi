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
import java.util.Collection;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.matrices.ChromosomeUtils;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
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
		return null;
	}

	@Override
	public boolean isHasDictionary() {
		return false;
	}

	@Override
	public void processData(GenotypesLoadDescription loadDescription, DataSetDestination samplesReceiver)
			throws Exception
	{
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		// HACK
		DataSet dataSet = ((InMemorySamplesReceiver) samplesReceiver).getDataSet();

		if (new File(loadDescription.getGtDirPath()).exists()) {
		SampleSet matrixSampleSet = new SampleSet(loadDescription.getStudyKey(), "");
		Map<SampleKey, byte[]> matrixSampleSetMap = matrixSampleSet.getSampleIdSetMapByteArray(loadDescription.getGtDirPath());

		boolean testExcessSamplesInMatrix = false;
		boolean testExcessSamplesInFile = false;
		Collection<SampleKey> sampleKeys = AbstractLoadGTFromFiles.extractKeys(dataSet.getSampleInfos());
		for (SampleKey key : matrixSampleSetMap.keySet()) {
			if (!sampleKeys.contains(key)) {
				testExcessSamplesInMatrix = true;
				break;
			}
		}

		for (SampleInfo sampleInfo : dataSet.getSampleInfos()) {
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

		MatrixMetadata importMatrixMetadata = MatricesList.getMatrixMetadata(
				loadDescription.getGtDirPath(),
				loadDescription.getStudyKey(),
				loadDescription.getFriendlyName());

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
		descSB.append("Markers: ").append(importMatrixMetadata.getMarkerSetSize());
		descSB.append(", Samples: ").append(importMatrixMetadata.getSampleSetSize());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(loadDescription.getFormat().toString());
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
						loadDescription.getStudyKey(),
						Integer.MIN_VALUE,
						Integer.MIN_VALUE,
						""
						));
			}
			copyMatrixToGenotypesFolder(
					loadDescription.getStudyKey(),
					loadDescription.getGtDirPath(),
					importMatrixMetadata.getMatrixNetCDFName());
		} else {
			generateNewGWASpiDBversionMatrix(loadDescription, samplesReceiver, importMatrixMetadata);
		}

//		importMatrixMetadata = MatricesList.getMatrixMetadataById(MatrixKey.valueOf(importMatrixMetadata));
		}
	}

	private int generateNewGWASpiDBversionMatrix(GenotypesLoadDescription loadDescription, DataSetDestination samplesReceiver, MatrixMetadata importMatrixMetadata)
			throws Exception
	{
		int result = Integer.MIN_VALUE;
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		//<editor-fold defaultstate="expanded" desc="CREATE MARKERSET & NETCDF">
		MarkerSet rdMarkerSet = new MarkerSet(importMatrixMetadata);
		rdMarkerSet.initFullMarkerIdSetMap();
		rdMarkerSet.fillMarkerSetMapWithChrAndPos();
		Map<MarkerKey, MarkerMetadata> rdMarkerSetMap = rdMarkerSet.getMarkerMetadata();

		SampleSet rdSampleSet = new SampleSet(
				importMatrixMetadata.getStudyKey(),
				loadDescription.getGtDirPath(),
				importMatrixMetadata.getMatrixNetCDFName());
		Map<SampleKey, byte[]> rdSampleSetMap = rdSampleSet.getSampleIdSetMapByteArray();

		log.info("Done initializing sorted MarkerSetMap");

		// RETRIEVE CHROMOSOMES INFO
		Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo = ChromosomeUtils.aggregateChromosomeInfo(rdMarkerSetMap, 0, 1);

		MatrixFactory matrixFactory = new MatrixFactory(
				loadDescription.getStudyKey(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				importMatrixMetadata.getDescription(), //description
				importMatrixMetadata.getGenotypeEncoding(),
				importMatrixMetadata.getStrand(),
				isHasDictionary(),
				rdSampleSetMap.size(),
				rdMarkerSetMap.size(),
				chromosomeInfo.size(),
				loadDescription.getGtDirPath());

		NetcdfFileWriteable ncfile = matrixFactory.getNetCDFHandler();

		// create the file
		ncfile.create();
		log.trace("Done creating netCDF handle: " + ncfile.toString());
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="WRITE MATRIX METADATA">
		// WRITE SAMPLESET TO MATRIX FROM SAMPLES LIST
		ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdSampleSetMap.keySet(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);

		int[] sampleOrig = new int[] {0, 0};
		ncfile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
		samplesD2 = null;
		log.info("Done writing SampleSet to matrix");

		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		// Chromosome location for each marker
		ArrayChar.D2 markersD2 = NetCdfUtils.writeValuesToD2ArrayChar(rdMarkerSetMap.values(), MarkerMetadata.TO_CHR, cNetCDF.Strides.STRIDE_CHR);
		int[] markersOrig = new int[] {0, 0};
		ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
		log.info("Done writing chromosomes to matrix");

		// Set of chromosomes found in matrix along with number of markersinfo
		NetCdfUtils.saveObjectsToStringToMatrix(ncfile, chromosomeInfo.keySet(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, cNetCDF.Strides.STRIDE_CHR);

		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[] {0, 1, 2, 3};
		NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(ncfile, chromosomeInfo.values(), columns, cNetCDF.Variables.VAR_CHR_INFO);


		// WRITE POSITION METADATA FROM ANNOTATION FILE
		ArrayInt.D1 markersPosD1 = NetCdfUtils.writeValuesToD1ArrayInt(rdMarkerSetMap.values(), MarkerMetadata.TO_POS);
		int[] posOrig = new int[1];
		ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		log.info("Done writing positions to matrix");

		// WRITE RSID & MARKERID METADATA FROM METADATAMap
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
		markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdMarkerSet.getMarkerIdSetMapCharArray().keySet(), cNetCDF.Strides.STRIDE_MARKER_NAME);
		ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);

		markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdMarkerSet.getMarkerIdSetMapCharArray().values(), cNetCDF.Strides.STRIDE_MARKER_NAME);
		ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		log.info("Done writing MarkerId and RsId to matrix");

		// WRITE GT STRAND FROM ANNOTATION FILE
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
		markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdMarkerSet.getMarkerIdSetMapCharArray().values(), cNetCDF.Strides.STRIDE_STRAND);
		int[] gtOrig = new int[] {0, 0};
		ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
		markersD2 = null;
		log.info("Done writing strand info to matrix");
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="GENOTYPES WRITER">
		//Iterate through rdSampleSetMap, use item position to read correct sample GTs into rdMarkerIdSetMap.
		int sampleWrIndex = 0;
		for (int i = 0; i < rdSampleSetMap.size(); i++) {
			rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleWrIndex);

			// Write MarkerIdSetMap to A3 ArrayChar and save to wrMatrix
//			NetCdfUtils.saveSingleSampleGTsToMatrix(ncfile, rdMarkerSet.getMarkerIdSetMapByteArray().values(), sampleWrIndex);
			samplesReceiver.addSampleGTAlleles(sampleWrIndex, rdMarkerSet.getMarkerIdSetMapByteArray().values());
			sampleWrIndex++;
			if ((sampleWrIndex == 1) || (sampleWrIndex % 100 == 0)) {
				log.info("Done processing sample {} / {}", sampleWrIndex,
						rdSampleSetMap.size());
			}
		}
		//</editor-fold>

		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		// GUESS GENOTYPE ENCODING
		if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)
				|| guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
			guessedGTCode = Utils.detectGTEncoding(rdMarkerSet.getMarkerIdSetMapByteArray().values());
		}

		ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
		Index index = guessedGTCodeAC.getIndex();
		guessedGTCodeAC.setString(index.set(0, 0), guessedGTCode.toString().trim());
		int[] origin = new int[]{0, 0};
		ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

		StringBuilder descSB = new StringBuilder(loadDescription.getDescription());
		descSB.append("Genotype encoding: ");
		descSB.append(guessedGTCode);

		MatrixMetadata matrixMetaData = matrixFactory.getResultMatrixMetadata();
		matrixMetaData.setDescription(descSB.toString());
		MatricesList.updateMatrix(matrixMetaData);

		// CLOSE FILE
		ncfile.close();
		result = matrixFactory.getResultMatrixMetadata().getMatrixId();

		AbstractLoadGTFromFiles.logAsWhole(
				startTime,
				loadDescription.getStudyKey().getId(),
				loadDescription.getGtDirPath(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				loadDescription.getDescription());

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");

		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPER METHODS">
	private void copyMatrixToGenotypesFolder(StudyKey studyKey, String importMatrixPath, String newMatrixCDFName) {
		try {
			File pathToStudy = new File(Study.constructGTPath(studyKey));
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(pathToStudy);
			}

			File origFile = new File(importMatrixPath);
			File newFile = new File(pathToStudy, newMatrixCDFName + ".nc");
			if (origFile.exists()) {
				org.gwaspi.global.Utils.copyFile(origFile, newFile);
			}
		} catch (Exception ex) {
			Dialogs.showWarningDialogue("A table saving error has occurred");
			log.error("A table saving error has occurred", ex);
		}
	}
	//</editor-fold>
}

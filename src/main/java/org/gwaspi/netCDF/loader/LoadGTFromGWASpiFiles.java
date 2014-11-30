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
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.datasource.netcdf.NetCDFDataSetSource;
import org.gwaspi.datasource.netcdf.NetCdfSamplesKeysSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

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

//	@Override
//	protected void addAdditionalBigDescriptionProperties(StringBuilder description, GenotypesLoadDescription loadDescription) {
//		super.addAdditionalBigDescriptionProperties(description, loadDescription); // XXX uncomment!
//
//		description.append(loadDescription.getGtDirPath());
//		description.append(" (Matrix file)\n");
//		description.append(loadDescription.getSampleFilePath());
//		description.append(" (Sample Info file)\n");
//	}

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
	public void processData(
			GenotypesLoadDescription loadDescription,
			Map<SampleKey, SampleInfo> sampleInfos,
			DataSetDestination samplesReceiver)
			throws IOException
	{
		final Collection<SampleInfo> sampleInfos2 = sampleInfos.values();

		if (new File(loadDescription.getGtDirPath()).exists()) {
		NetcdfFile gwaspiStorageFile = NetcdfFile.open(loadDescription.getGtDirPath());
		SamplesKeysSource samplesKeysSource = NetCdfSamplesKeysSource.createForMatrix(null, loadDescription.getStudyKey(), gwaspiStorageFile);

		boolean testExcessSamplesInMatrix = false;
		boolean testExcessSamplesInFile = false;
		Collection<SampleKey> sampleKeys = sampleInfos.keySet();
		for (SampleKey key : samplesKeysSource) {
			if (!sampleKeys.contains(key)) {
				testExcessSamplesInMatrix = true;
				break;
			}
		}

		for (SampleInfo sampleInfo : sampleInfos2) {
			if (!samplesKeysSource.contains(sampleInfo.getKey())) {
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

		MatrixMetadata importMatrixMetadata = NetCDFDataSetSource.loadMatrixMetadata(
				new File(loadDescription.getGtDirPath()),
				loadDescription.getFriendlyName(),
				loadDescription.getStudyKey(),
				null);

		final String currentGwaspiDbVersion = Config.getConfigValue(
				Config.PROPERTY_CURRENT_GWASPIDB_VERSION, null);
		if (importMatrixMetadata.getGwaspiDBVersion().equals(currentGwaspiDbVersion)) {
			// COMPARE DATABASE VERSIONS
			if (!testExcessSamplesInMatrix) {
				StringBuilder description = new StringBuilder(Text.Matrix.descriptionHeader1);
				description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
				if (!loadDescription.getDescription().isEmpty()) {
					description.append("\nDescription: ");
					description.append(loadDescription.getDescription());
					description.append("\n");
				}
	//			description.append("\nStrand: ");
	//			description.append(strand);
	//			description.append("\nGenotype encoding: ");
	//			description.append(importMatrixMetadata.getGenotypeEncoding());
				description.append("\n");
				description.append("Technology: ");
				description.append(importMatrixMetadata.getTechnology());
				description.append("\n");
				description.append("Markers: ").append(importMatrixMetadata.getNumMarkers());
				description.append(", Samples: ").append(importMatrixMetadata.getNumSamples());
				description.append("\n");
				description.append(Text.Matrix.descriptionHeader2);
				description.append(loadDescription.getFormat().toString());
				description.append("\n");
				description.append(Text.Matrix.descriptionHeader3);
				description.append("\n");
				description.append(loadDescription.getGtDirPath());
				description.append(" (Matrix file)\n");
				description.append(loadDescription.getSampleFilePath());
				description.append(" (Sample Info file)\n");
				MatricesList.insertMatrixMetadata(new MatrixMetadata(
						loadDescription.getFriendlyName(),
//						importMatrixMetadata.getSimpleName(), // XXX here is the problem!
						description.toString(),
						importMatrixMetadata.getGenotypeEncoding(),
						loadDescription.getStudyKey()
						));
			}
			copyMatrixToGenotypesFolder(
					loadDescription.getStudyKey(),
					loadDescription.getGtDirPath(),
					MatrixMetadata.generatePathToNetCdfFile(importMatrixMetadata));
		} else {
			// if the source has a different GWASpi-DB version
			// then what we currently run/use,
			// make a total read & write run
			generateNewGWASpiDBversionMatrix(loadDescription, samplesReceiver, importMatrixMetadata);
		}

//		importMatrixMetadata = MatricesList.getMatrixMetadataById(MatrixKey.valueOf(importMatrixMetadata));
		}
	}

	private int generateNewGWASpiDBversionMatrix(GenotypesLoadDescription loadDescription, DataSetDestination samplesReceiver, MatrixMetadata importMatrixMetadata)
			throws IOException
	{
		int result = MatrixKey.NULL_ID;
		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		//<editor-fold defaultstate="expanded" desc="CREATE MARKERSET & NETCDF">
//		MatrixKey importMatrixKey = MatrixKey.valueOf(importMatrixMetadata);
		DataSetSource dataSetSource = new NetCDFDataSetSource(new File(loadDescription.getGtDirPath()), loadDescription.getStudyKey());
//		MarkerSet rdMarkerSet = new MarkerSet(importMatrixMetadata);
//		rdMarkerSet.initFullMarkerIdSetMap();
//		rdMarkerSet.fillMarkerSetMapWithChrAndPos();
//		Map<MarkerKey, MarkerMetadata> rdMarkerSetMap = rdMarkerSet.getMarkerMetadata();
//		MarkersMetadataSource markersMetadatasSource = dataSetSource.getMarkersMetadatasSource();
//
//		SampleSet rdSampleSet = new SampleSet(
//				importMatrixMetadata.getStudyKey(),
//				loadDescription.getGtDirPath(),
//				importMatrixMetadata.getSimpleName());
//		Map<SampleKey, byte[]> rdSampleSetMap = rdSampleSet.getSampleIdSetMapByteArray();
//
//		log.info("Done initializing sorted MarkerSetMap");
//
//		// RETRIEVE CHROMOSOMES INFO
//		Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo = ChromosomeUtils.aggregateChromosomeInfo(rdMarkerSetMap, 0, 1);

//		MatrixMetadata matrixMetadata = new MatrixMetadata(
//				loadDescription.getStudyKey(),
//				loadDescription.getFriendlyName(),
//				loadDescription.getFormat(),
//				importMatrixMetadata.getDescription(), // description
//				importMatrixMetadata.getGenotypeEncoding(),
//				importMatrixMetadata.getStrand(),
//				isHasDictionary(),
//				rdSampleSetMap.size(),
//				dataSetSource.getNumMarkers(),
//				chromosomeInfo.size(),
//				loadDescription.getGtDirPath());

//		NetcdfFileWriteable ncfile = AbstractNetCDFDataSetDestination.generateNetcdfHandler(matrixMetadata);
//
//		// create the file
//		ncfile.create();
//		log.trace("Done creating netCDF handle: " + ncfile.toString());
//		//</editor-fold>
//
//		//<editor-fold defaultstate="expanded" desc="WRITE MATRIX METADATA">
//		// WRITE SAMPLESET TO MATRIX FROM SAMPLES LIST
//		ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdSampleSetMap.keySet(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);
//		int[] sampleOrig = new int[] {0, 0};
//		ncfile.write(cNetCDF.Variables.VAR_SAMPLE_KEY, sampleOrig, samplesD2);
//		samplesD2 = null;
//		log.info("Done writing SampleSet to matrix");
//
//		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
//		// Chromosome location for each marker
//		ArrayChar.D2 markersD2 = NetCdfUtils.writeValuesToD2ArrayChar(markersMetadatasSource, MarkerMetadata.TO_CHR, cNetCDF.Strides.STRIDE_CHR);
//		int[] markersOrig = new int[] {0, 0};
//		ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
//		log.info("Done writing chromosomes to matrix");
//		// Set of chromosomes found in matrix along with number of markersinfo
//		NetCdfUtils.saveObjectsToStringToMatrix(ncfile, chromosomeInfo.keySet(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, cNetCDF.Strides.STRIDE_CHR);
//		// Number of marker per chromosome & max pos for each chromosome
//		int[] columns = new int[] {0, 1, 2, 3};
//		NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(ncfile, chromosomeInfo.values(), columns, cNetCDF.Variables.VAR_CHR_INFO);
//
//		// WRITE POSITION METADATA FROM ANNOTATION FILE
//		ArrayInt.D1 markersPosD1 = NetCdfUtils.writeValuesToD1ArrayInt(markersMetadatasSource, MarkerMetadata.TO_POS);
//		int[] posOrig = new int[1];
//		ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
//		log.info("Done writing positions to matrix");
//
//		// WRITE RSID & MARKERID METADATA FROM METADATAMap
//		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
//		markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdMarkerSet.getMarkerIdSetMapCharArray().keySet(), cNetCDF.Strides.STRIDE_MARKER_NAME);
//		ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
//
//		markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdMarkerSet.getMarkerIdSetMapCharArray().values(), cNetCDF.Strides.STRIDE_MARKER_NAME);
//		ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
//		log.info("Done writing MarkerId and RsId to matrix");
//
//		// WRITE GT STRAND FROM ANNOTATION FILE
//		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
//		markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdMarkerSet.getMarkerIdSetMapCharArray().values(), cNetCDF.Strides.STRIDE_STRAND);
//		int[] gtOrig = new int[] {0, 0};
//		ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
//		markersD2 = null;
//		log.info("Done writing strand info to matrix");
//		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="GENOTYPES WRITER">
		//Iterate through rdSampleSetMap, use item position to read correct sample GTs into rdMarkerIdSetMap.
		SamplesGenotypesSource rdSamplesGenotypesSource = dataSetSource.getSamplesGenotypesSource();
//		Iterator<GenotypesList> rdSamplesGenotypesSourceIt = rdSamplesGenotypesSource.iterator();
//		for (int sampleWrIndex = 0; sampleWrIndex < rdSampleSetMap.size(); sampleWrIndex++) {
		int sampleWrIndex = 0;
		for (GenotypesList genotypesList : rdSamplesGenotypesSource) {
//			rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleWrIndex);

			// Write MarkerIdSetMap to A3 ArrayChar and save to wrMatrix
//			NetCdfUtils.saveSingleSampleGTsToMatrix(ncfile, rdMarkerSet.getMarkerIdSetMapByteArray().values(), sampleWrIndex);
//			samplesReceiver.addSampleGTAlleles(sampleWrIndex, rdMarkerSet.getMarkerIdSetMapByteArray().values());
			samplesReceiver.addSampleGTAlleles(sampleWrIndex, genotypesList);
			sampleWrIndex++;
		}
		//</editor-fold>

//		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
//		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
//		// GUESS GENOTYPE ENCODING
//		if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)
//				|| guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
//			guessedGTCode = Utils.detectGTEncoding(rdMarkerSet.getMarkerIdSetMapByteArray().values());
//		}
//
//		ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
//		Index index = guessedGTCodeAC.getIndex();
//		guessedGTCodeAC.setString(index.set(0, 0), guessedGTCode.toString().trim());
//		int[] origin = new int[]{0, 0};
//		ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);
//
//		StringBuilder description = new StringBuilder(loadDescription.getDescription());
//		description.append("Genotype encoding: ");
//		description.append(guessedGTCode);
//
//		MatrixMetadata matrixMetaData = matrixFactory.getResultMatrixMetadata();
//		matrixMetaData.setDescription(description.toString());
//		MatricesList.updateMatrix(matrixMetaData);
//
//		// CLOSE FILE
//		ncfile.close();
//		result = matrixFactory.getResultMatrixMetadata().getMatrixId();

		AbstractLoadGTFromFiles.logAsWhole(
				startTime,
				loadDescription.getStudyKey().getId(),
				loadDescription.getGtDirPath(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				loadDescription.getDescription());

		org.gwaspi.global.Utils.sysoutCompleted("Loading Genotypes");

		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPER METHODS">
	private void copyMatrixToGenotypesFolder(StudyKey studyKey, String importMatrixPath, File newMatrixCDFFile) {
		try {
			File pathToStudy = new File(Study.constructGTPath(studyKey));
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(pathToStudy);
			}

			File origFile = new File(importMatrixPath);
			File newFile = newMatrixCDFFile;
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

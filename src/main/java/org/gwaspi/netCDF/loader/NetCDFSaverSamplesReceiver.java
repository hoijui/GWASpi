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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cImport.StrandFlags;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

public class NetCDFSaverSamplesReceiver extends InMemorySamplesReceiver {

	private final Logger log
			= LoggerFactory.getLogger(NetCDFSaverSamplesReceiver.class);

	private final GenotypesLoadDescription loadDescription;
	private AbstractLoadGTFromFiles gtLoader; // HACK
	private String startTime;
	private MatrixKey resultMatrixKey;
//	private int curAlleleSampleIndex;
	private int curAllelesMarkerIndex;
	private StringBuilder descSB;
	private MatrixFactory matrixFactory;
	private NetcdfFileWriteable ncfile;
	private Boolean alleleLoadPerSample;
	/** This is only used when alleleLoadPerSample == FALSE */
	private List<byte[]> genotypesHyperslabs;
	private int hyperSlabRows;

	public NetCDFSaverSamplesReceiver(
			GenotypesLoadDescription loadDescription)
	{
		this.loadDescription = loadDescription;
		this.gtLoader = null;
//		this.curAlleleSampleIndex = -1;
		this.curAllelesMarkerIndex = -1;
		this.alleleLoadPerSample = null;
		this.genotypesHyperslabs = null;
		this.hyperSlabRows = -1;
	}

	public MatrixKey getResultMatrixKey() {
		return resultMatrixKey;
	}

	public void setGTLoader(AbstractLoadGTFromFiles gtLoader) {
		this.gtLoader = gtLoader;
	}

//	private final ImportFormat format;
//	private final StrandType matrixStrand;
//	private final boolean hasDictionary;
//	private final String markersD2Variables;
//
//	public NetCDFSaverSamplesReceiver(
//			ImportFormat format,
//			StrandType matrixStrand,
//			boolean hasDictionary,
//			String markersD2Variables
//			)
//	{
//		this.format = format;
//		this.matrixStrand = matrixStrand;
//		this.hasDictionary = hasDictionary;
//		this.markersD2Variables = markersD2Variables;
//	}
//
//	@Override
//	public ImportFormat getFormat() {
//		return format;
//	}
//
//	@Override
//	public StrandType getMatrixStrand() {
//		return matrixStrand;
//	}
//
//	@Override
//	public boolean isHasDictionary() {
//		return hasDictionary;
//	}
//
//	@Override
//	public String getMarkersD2Variables() {
//		return markersD2Variables;
//	}
//
//	protected void addAdditionalBigDescriptionProperties(StringBuilder descSB, GenotypesLoadDescription loadDescription) {
//	}
//
//	protected abstract MetadataLoader createMetaDataLoader(GenotypesLoadDescription loadDescription);

	@Override
	public void init() throws Exception {

		startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
		resultMatrixKey = null;
	}

	@Override
	public void finishedLoadingSampleInfos() throws Exception {
		SampleInfoList.insertSampleInfos(getDataSet().getSampleInfos());
	}

	//<editor-fold defaultstate="expanded" desc="PROCESS GENOTYPES">
	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException, InvalidRangeException, InterruptedException {
//	public void init() throws Exception {
//		int result = Integer.MIN_VALUE;
//
//		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		List<SampleKey> sampleKeys = NetCDFSaverSamplesReceiver.extractKeys(getDataSet().getSampleInfos());

//		Map<MarkerKey, MarkerMetadata> markerSetMap = new LinkedHashMap<MarkerKey, MarkerMetadata>();
//
//		//<editor-fold defaultstate="expanded" desc="CREATE MARKERSET & NETCDF">
//		MetadataLoader markerSetLoader = createMetaDataLoader(loadDescription);
//		for (MarkerMetadata markerMetadata : markerSetLoader) {
//			markerSetMap.put(MarkerKey.valueOf(markerMetadata), markerMetadata);
//		}
//		Map<MarkerKey, MarkerMetadata> tmpMarkerMap = markerSetLoader.getSortedMarkerSetWithMetaData();
//		markerSetMap.putAll(tmpMarkerMap);
//
//		log.info("Done initializing sorted MarkerSetMap");

		// CREATE netCDF-3 FILE
		descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
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
		descSB.append("Markers: ").append(getDataSet().getMarkerMetadatas().size()).append(", Samples: ").append(getDataSet().getSampleInfos().size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(loadDescription.getFormat().toString());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		gtLoader.addAdditionalBigDescriptionProperties(descSB, loadDescription);
		if (new File(loadDescription.getSampleFilePath()).exists()) {
			descSB.append(loadDescription.getSampleFilePath()); // the FAM file, in case of PLink Binary
			descSB.append(" (Sample Info file)\n");
		}

		// RETRIEVE CHROMOSOMES INFO
		Map<MarkerKey, int[]> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(getDataSet().getMarkerMetadatas(), 2, 3);

		matrixFactory = new MatrixFactory(
				loadDescription.getStudyKey(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				descSB.toString(), // description
				loadDescription.getGtCode(),
				(gtLoader.getMatrixStrand() != null) ? gtLoader.getMatrixStrand() : loadDescription.getStrand(),
				gtLoader.isHasDictionary(),
				getDataSet().getSampleInfos().size(),
				getDataSet().getMarkerMetadatas().size(),
				chrSetMap.size(),
				loadDescription.getGtDirPath());

		ncfile = matrixFactory.getNetCDFHandler();

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
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeCollectionToD2ArrayChar(sampleKeys, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(getDataSet().getMarkerMetadatas(), MarkerMetadata.TO_RS_ID, cNetCDF.Strides.STRIDE_MARKER_NAME);

		int[] markersOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(getDataSet().getMarkerMetadatas(), MarkerMetadata.TO_MARKER_ID, cNetCDF.Strides.STRIDE_MARKER_NAME);
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
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(getDataSet().getMarkerMetadatas(), MarkerMetadata.TO_CHR, cNetCDF.Strides.STRIDE_CHR);

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
		int[] columns = new int[] {0, 1, 2, 3};
		org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(ncfile, chrSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);


		// WRITE POSITION METADATA FROM ANNOTATION FILE
		//markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(sortedMarkerSetMap, 3, cNetCDF.Strides.STRIDE_POS);
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD1ArrayInt(getDataSet().getMarkerMetadatas(), MarkerMetadata.TO_POS);
		int[] posOrig = new int[1];
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing positions to matrix");

		// WRITE CUSTOM ALLELES METADATA FROM ANNOTATION FILE
		if (gtLoader.getMarkersD2Variables() != null) {
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(getDataSet().getMarkerMetadatas(), gtLoader.getBaseDictPropertyExtractor(), cNetCDF.Strides.STRIDE_GT);
		try {
			ncfile.write(gtLoader.getMarkersD2Variables(), markersOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		log.info("Done writing forward alleles to matrix");
		}

		// WRITE GT STRAND FROM ANNOTATION FILE
		int[] gtOrig = new int[] {0, 0};
		if (gtLoader.isHasStrandInfo()) {
			// TODO Strand info is buggy in Hapmap bulk download!
			markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(getDataSet().getMarkerMetadatas(), MarkerMetadata.TO_STRAND, cNetCDF.Strides.STRIDE_STRAND);
		} else {
			String strandFlag = gtLoader.getStrandFlag(loadDescription);
			markersD2 = org.gwaspi.netCDF.operations.Utils.writeSingleValueToD2ArrayChar(strandFlag, cNetCDF.Strides.STRIDE_STRAND, getDataSet().getMarkerMetadatas().size());
		}

		try {
			ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
		markersD2 = null;
		log.info("Done writing strand info to matrix");
		//</editor-fold>
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws Exception {

		alleleLoadPerSample = perSample;

		if (alleleLoadPerSample) {
//			curAlleleSampleIndex = 0;
		} else {
//			curAllelesMarkerIndex = 0;

			// PLAYING IT SAFE WITH HALF THE maxProcessMarkers
			// This number specifies, how many markers (lines in hte file)
			// are read into memory, before writing them to the NetCDF-file
			hyperSlabRows = (int) Math.round(
					(double) StartGWASpi.maxProcessMarkers / (getDataSet().getSampleInfos().size() * 2));
			if (getDataSet().getMarkerMetadatas().size() < hyperSlabRows) {
				hyperSlabRows = getDataSet().getMarkerMetadatas().size();
			}

			genotypesHyperslabs = new ArrayList<byte[]>(hyperSlabRows);
		}

		log.info(Text.All.processing);
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws Exception {

		if (!alleleLoadPerSample) {
			throw new IllegalStateException("You can not mix loading per sample and loading per marker");
		}

//		org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, sampleAlleles, curAlleleSampleIndex);
		org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, sampleAlleles, sampleIndex);
//		curAlleleSampleIndex++;
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws Exception {

		if (alleleLoadPerSample) {
			throw new IllegalStateException("You can not mix loading per sample and loading per marker");
		}

//		// WRITING HYPERSLABS AT A TIME
//		for (SampleInfo sampleInfo : getDataSet().getSampleInfos()) {
//			String sampleId = sampleInfo.getSampleId();
//			byte[] value = mappedGenotypes.get(sampleId);
//			genotypesHyperslabs.add(value);
//		}
		genotypesHyperslabs.addAll(markerAlleles);
//		curAllelesMarkerIndex++;
		curAllelesMarkerIndex = markerIndex;

		if (curAllelesMarkerIndex != 1 && curAllelesMarkerIndex % (hyperSlabRows) == 0) {
			ArrayByte.D3 genotypesArray = org.gwaspi.netCDF.operations.Utils.writeListValuesToSamplesHyperSlabArrayByteD3(genotypesHyperslabs, getDataSet().getSampleInfos().size(), cNetCDF.Strides.STRIDE_GT);
			int[] origin = new int[]{0, (curAllelesMarkerIndex - hyperSlabRows), 0}; //0,0,0 for 1st marker ; 0,1,0 for 2nd marker....
//						log.info("Origin at rowCount "+rowCounter+": "+origin[0]+"|"+origin[1]+"|"+origin[2]);
			try {
				ncfile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypesArray);
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Bad origin at rowCount " + curAllelesMarkerIndex + ": " + origin[0] + "|" + origin[1] + "|" + origin[2], ex);
			}

			genotypesHyperslabs.clear();
		}
	}

	@Override
	public void finishedLoadingAlleles() throws Exception {

		if (!alleleLoadPerSample) {
//			int markerNb = bimSamples.size();
			int markerNb = curAllelesMarkerIndex;
			// WRITING LAST HYPERSLAB
			int lastHyperSlabRows = markerNb - (genotypesHyperslabs.size() / getDataSet().getSampleInfos().size());
			ArrayByte.D3 genotypesArray = org.gwaspi.netCDF.operations.Utils.writeListValuesToSamplesHyperSlabArrayByteD3(genotypesHyperslabs, getDataSet().getSampleInfos().size(), cNetCDF.Strides.STRIDE_GT);
			int[] origin = new int[]{0, lastHyperSlabRows, 0}; //0,0,0 for 1st marker ; 0,1,0 for 2nd marker....
//			log.info("Last origin at rowCount "+rowCounter+": "+origin[0]+"|"+origin[1]+"|"+origin[2]);
			try {
				ncfile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypesArray);
				log.info("Processed markers: " + curAllelesMarkerIndex);
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Bad origin at rowCount " + curAllelesMarkerIndex + ": " + origin[0] + "|" + origin[1] + "|" + origin[2], ex);
			}
		}

//		//<editor-fold defaultstate="expanded" desc="MATRIX GENOTYPES LOAD ">
//		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
//		log.info(Text.All.processing);
//		loadGenotypes(loadDescription, getDataSet().getSampleInfos(), getDataSet().getMarkerMetadatas(), ncfile, sampleKeys, guessedGTCode);
//		log.info("Done writing genotypes to matrix");
//		//</editor-fold>

		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
		try {
			// GUESS GENOTYPE ENCODING
			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), gtLoader.getGuessedGTCode().toString().trim());
			int[] origin = new int[]{0, 0};
			ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			descSB.append("Genotype encoding: ");
			descSB.append(gtLoader.getGuessedGTCode().toString());

			MatrixMetadata matrixMetaData = matrixFactory.getMatrixMetaData();
			matrixMetaData.setDescription(descSB.toString());
			MatricesList.updateMatrix(matrixMetaData);

			// CLOSE FILE
			ncfile.close();
			int resultMatrixId = matrixFactory.getMatrixMetaData().getMatrixId();
			resultMatrixKey = new MatrixKey(loadDescription.getStudyKey(), resultMatrixId);
		} catch (IOException ex) {
			log.error("Failed creating file " + ncfile.getLocation(), ex);
		}

		logAsWhole(
				startTime,
				loadDescription.getStudyKey().getId(),
				loadDescription.getGtDirPath(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				loadDescription.getDescription());

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
	}

	@Override
	public void done() throws Exception {
	}

	static void logAsWhole(String startTime, int studyId, String dirPath, ImportFormat format, String matrixName, String description) throws IOException {
		// LOG OPERATION IN STUDY HISTORY
		StringBuilder operation = new StringBuilder("\nLoaded raw " + format + " genotype data in path " + dirPath + ".\n");
		operation.append("Start Time: ").append(startTime).append("\n");
		operation.append("End Time: ").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append(".\n");
		operation.append("Data stored in matrix ").append(matrixName).append(".\n");
		operation.append("Description: ").append(description).append(".\n");
		org.gwaspi.global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
	}

	static List<SampleKey> extractKeys(Collection<SampleInfo> sampleInfos) {

		List<SampleKey> sampleKeys = new ArrayList<SampleKey>(sampleInfos.size());

		for (SampleInfo sampleInfo : sampleInfos) {
			sampleKeys.add(sampleInfo.getKey());
		}

		return sampleKeys;
	}
}

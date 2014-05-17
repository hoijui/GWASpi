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
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Config;
import org.gwaspi.global.Extractor;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.operations.NetCdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public abstract class AbstractNetCDFDataSetDestination extends AbstractDataSetDestination {

	private static final Logger log
			= LoggerFactory.getLogger(AbstractNetCDFDataSetDestination.class);

	private static final boolean saveSamplesMetadata = true;

	private MatrixKey resultMatrixKey;
//	private int curAlleleSampleIndex;
	private int curAllelesMarkerIndex;
	private MatrixMetadata matrixMetadata;
	private NetcdfFileWriteable ncfile;
	private Boolean alleleLoadPerSample;
	/** This is only used when alleleLoadPerSample == FALSE */
	private List<byte[]> genotypesHyperslabs;
	private int hyperSlabRows;

	public AbstractNetCDFDataSetDestination() {

//		this.curAlleleSampleIndex = -1;
		this.curAllelesMarkerIndex = -1;
		this.alleleLoadPerSample = null;
		this.genotypesHyperslabs = null;
		this.hyperSlabRows = -1;
	}

	public MatrixKey getResultMatrixKey() {
		return resultMatrixKey;
	}

	@Override
	public void init() throws IOException {
		super.init();

		resultMatrixKey = null;
	}

	@Override
	public void finishedLoadingSampleInfos() throws IOException {
		super.finishedLoadingSampleInfos();

		SampleInfoList.insertSampleInfos(getDataSet().getSampleInfos());
	}

	protected abstract MatrixMetadata createMatrixMetadata() throws IOException;

	/**
	 * @return the strand-flag applicable to all markers,
	 *   or <code>null</code>, if each marker has a separate strand-flag
	 */
	protected abstract String getStrandFlag();

	private static Dimension generatePossiblyVarDimension(NetcdfFileWriteable ncFile, String varName, int size) {

		final Dimension dimension;

		if (size <= 0) {
			dimension = ncFile.addDimension(varName, 0, true, true, false);
		} else {
			dimension = ncFile.addDimension(varName, size);
		}

		return dimension;
	}

	public static NetcdfFileWriteable generateNetcdfHandler(MatrixMetadata matrixMetadata)
			throws InvalidRangeException, IOException
	{
		int gtStride = cNetCDF.Strides.STRIDE_GT;
		int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
		int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;
//		int strandStride = cNetCDF.Strides.STRIDE_STRAND;

		File writeFile = MatrixMetadata.generatePathToNetCdfFile(matrixMetadata);
		File writeFileParentFolder = writeFile.getParentFile();
		if (!writeFileParentFolder.exists()) {
			org.gwaspi.global.Utils.createFolder(writeFileParentFolder);
		}
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFile.getAbsolutePath(), false);

		// global attributes
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, matrixMetadata.getStudyKey().getId());
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_MATRIX_ID, matrixMetadata.getMatrixId());
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_FRIENDLY_NAME, matrixMetadata.getFriendlyName().toString());
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY, matrixMetadata.getTechnology().toString());
		String versionNb = Config.getConfigValue(Config.PROPERTY_CURRENT_GWASPIDB_VERSION, null);
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_GWASPIDB_VERSION, versionNb);
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, matrixMetadata.getDescription());
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STRAND, matrixMetadata.getStrand().toString());
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY, matrixMetadata.getHasDictionary() ? 1 : 0);
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_MATRIX_TYPE, matrixMetadata.getMatrixType());
		// NOTE We save the date as string,
		//   because NetCDF fails with long
		//   (though it is suposed to suppoert it),
		//   and we use the number representation to be Locale independent
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_CREATION_DATE, String.valueOf(matrixMetadata.getCreationDate().getTime()));

		// dimensions
		Dimension samplesDim = generatePossiblyVarDimension(ncfile, cNetCDF.Dimensions.DIM_SAMPLESET, matrixMetadata.getNumSamples());
		Dimension markersDim = generatePossiblyVarDimension(ncfile, cNetCDF.Dimensions.DIM_MARKERSET, matrixMetadata.getNumMarkers());
		Dimension chrSetDim = generatePossiblyVarDimension(ncfile, cNetCDF.Dimensions.DIM_CHRSET, matrixMetadata.getNumChromosomes());
		Dimension gtStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_GTSTRIDE, gtStride);
		Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
		Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
//		Dimension dim32 = ncfile.addDimension(cNetCDF.Dimensions.DIM_32, 32);
//		Dimension dim16 = ncfile.addDimension(cNetCDF.Dimensions.DIM_16, 16);
		Dimension dim8 = ncfile.addDimension(cNetCDF.Dimensions.DIM_8, 8);
		Dimension dim4 = ncfile.addDimension(cNetCDF.Dimensions.DIM_4, 4);
		Dimension dim2 = ncfile.addDimension(cNetCDF.Dimensions.DIM_2, 2);
		Dimension dim1 = ncfile.addDimension(cNetCDF.Dimensions.DIM_1, 1);

		// GENOTYPE SPACES
		List<Dimension> genotypeSpace = new ArrayList<Dimension>(3);
		genotypeSpace.add(samplesDim);
		genotypeSpace.add(markersDim);
		genotypeSpace.add(gtStrideDim);

		// MARKER SPACES
		List<Dimension> markersSpace = new ArrayList<Dimension>(1);
		markersSpace.add(markersDim);

		List<Dimension> markersNameSpace = new ArrayList<Dimension>(2);
		markersNameSpace.add(markersDim);
		markersNameSpace.add(markerStrideDim);

		List<Dimension> markerPropertySpace8 = new ArrayList<Dimension>(2);
		markerPropertySpace8.add(markersDim);
		markerPropertySpace8.add(dim8);

		List<Dimension> markerPropertySpace4 = new ArrayList<Dimension>(2);
		markerPropertySpace4.add(markersDim);
		markerPropertySpace4.add(dim4);

		List<Dimension> markerPropertySpace2 = new ArrayList<Dimension>(2);
		markerPropertySpace2.add(markersDim);
		markerPropertySpace2.add(dim2);

		// CHROMOSOME SPACES
		List<Dimension> chromosomesNameSpace = new ArrayList<Dimension>(2);
		chromosomesNameSpace.add(chrSetDim);
		chromosomesNameSpace.add(dim8);

		List<Dimension> chromosomesInfoSpace = new ArrayList<Dimension>(2);
		chromosomesInfoSpace.add(chrSetDim);
		chromosomesInfoSpace.add(dim4);

		// SAMPLE SPACES
		List<Dimension> sampleSpace = new ArrayList<Dimension>(2);
		sampleSpace.add(samplesDim);

		List<Dimension> sampleNameSpace = new ArrayList<Dimension>(2);
		sampleNameSpace.add(samplesDim);
		sampleNameSpace.add(sampleStrideDim);

		// OTHER SPACES
		List<Dimension> gtEncodingSpace = new ArrayList<Dimension>(2);
		gtEncodingSpace.add(dim1);
		gtEncodingSpace.add(dim8);

		// Define Marker Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERSET, DataType.CHAR, markersNameSpace);
//		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_MARKERSET, cNetCDF.Attributes.LENGTH, matrixMetadata.getNumMarkers()); // NOTE not required, as it can be read from th edimensions directly, which is also more reliable

		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markersNameSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_CHR, DataType.CHAR, markerPropertySpace8);
		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_POS, DataType.INT, markersSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, DataType.CHAR, markerPropertySpace2);

		// Define Chromosome Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX, DataType.CHAR, chromosomesNameSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_CHR_INFO, DataType.INT, chromosomesInfoSpace);

		// Define Sample Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_KEY, DataType.CHAR, sampleNameSpace);
//		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_SAMPLE_KEY, cNetCDF.Attributes.LENGTH, matrixMetadata.getNumSamples()); // NOTE not required, as it can be read from the dimensions directly, which is also more reliable
		if (saveSamplesMetadata) {
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_ORDER_ID, DataType.INT, sampleSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_FATHER, DataType.CHAR, sampleNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_MOTHER, DataType.CHAR, sampleNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLES_SEX, DataType.INT, sampleSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLES_AFFECTION, DataType.INT, sampleSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_CATEGORY, DataType.CHAR, sampleNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_DISEASE, DataType.CHAR, sampleNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_POPULATION, DataType.CHAR, sampleNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_AGE, DataType.INT, sampleSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_FILTER, DataType.CHAR, sampleNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_APPROVED, DataType.INT, sampleSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLE_STATUS, DataType.INT, sampleSpace);
		}

		// Define Genotype Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_GENOTYPES, DataType.BYTE, genotypeSpace);
		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_GENOTYPES, cNetCDF.Attributes.GLOB_STRAND, StrandType.UNKNOWN.toString());
		ncfile.addVariable(cNetCDF.Variables.VAR_GT_STRAND, DataType.CHAR, markerPropertySpace4);

		// ENCODING VARIABLE
		ncfile.addVariable(cNetCDF.Variables.GLOB_GTENCODING, DataType.CHAR, gtEncodingSpace);

		// CHECK IF JVM IS 32/64 bits to use LFS or not
		int JVMbits = Integer.parseInt(System.getProperty("sun.arch.data.model", "32"));
		if (JVMbits == 64) {
			ncfile.setLargeFile(true);
		}
		ncfile.setFill(true);

		return ncfile;
	}

	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException {
		super.finishedLoadingMarkerMetadatas();
	}

	private void storeSamplesAndMarkersMetadata() throws IOException {

		try {
			matrixMetadata = createMatrixMetadata();

			if (matrixMetadata.getNumSamples() <= 0) {
				throw new RuntimeException("No samples loaded");
			}
			if (matrixMetadata.getNumMarkers() <= 0) {
				throw new RuntimeException("No markers loaded");
			}

			resultMatrixKey = MatricesList.insertMatrixMetadata(matrixMetadata);
			try {
				ncfile = generateNetcdfHandler(matrixMetadata);
			} catch (InvalidRangeException ex) {
				throw new IOException(ex);
			}
			getDataSet().setMatrixMetadata(matrixMetadata);

			// create the NetCDF file
			ncfile.create();
			log.trace("Done creating netCDF handle: " + ncfile.toString());

			Collection<SampleInfo> sampleInfos = getDataSet().getSampleInfos();
			saveSamplesMetadata(sampleInfos, ncfile);

			boolean hasDictionary = matrixMetadata.getHasDictionary();
			saveMarkersMetadata(getDataSet().getMarkerMetadatas().values(), hasDictionary, getStrandFlag(), ncfile);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void finishedLoadingChromosomeMetadatas() throws IOException {
		super.finishedLoadingChromosomeMetadatas();

		storeSamplesAndMarkersMetadata();
		saveChromosomeMetadata(getDataSet().getChromosomeInfos(), ncfile);
	}

	private static void saveSamplesMetadata(Collection<SampleInfo> sampleInfos, NetcdfFileWriteable ncfile) throws IOException, InvalidRangeException {

		List<SampleKey> sampleKeys = extractKeys(sampleInfos);

		// WRITE SAMPLESET TO MATRIX FROM SAMPLES LIST
		ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(sampleKeys, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
		int[] sampleOrig2D = new int[] {0, 0};
		ncfile.write(cNetCDF.Variables.VAR_SAMPLE_KEY, sampleOrig2D, samplesD2);

		if (saveSamplesMetadata) {
			int[] sampleOrig1D = new int[] {0};
			ArrayInt.D1 samplesD1;
			final int commonStringMaxLength = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			samplesD1 = NetCdfUtils.writeValuesToD1ArrayInt(sampleInfos, SampleInfo.TO_ORDER_ID);
			ncfile.write(cNetCDF.Variables.VAR_SAMPLE_ORDER_ID, sampleOrig1D, samplesD1);

			samplesD2 = NetCdfUtils.writeValuesToD2ArrayChar(sampleInfos, SampleInfo.TO_FATHER_ID, commonStringMaxLength);
			ncfile.write(cNetCDF.Variables.VAR_SAMPLE_FATHER, sampleOrig2D, samplesD2);

			samplesD2 = NetCdfUtils.writeValuesToD2ArrayChar(sampleInfos, SampleInfo.TO_MOTHER_ID, commonStringMaxLength);
			ncfile.write(cNetCDF.Variables.VAR_SAMPLE_MOTHER, sampleOrig2D, samplesD2);

			samplesD1 = NetCdfUtils.writeValuesToD1ArrayInt(sampleInfos, new Extractor.EnumToIntMetaExtractor<SampleInfo, Sex>(SampleInfo.TO_SEX));
			ncfile.write(cNetCDF.Variables.VAR_SAMPLES_SEX, sampleOrig1D, samplesD1);

			samplesD1 = NetCdfUtils.writeValuesToD1ArrayInt(sampleInfos, new Extractor.EnumToIntMetaExtractor<SampleInfo, Affection>(SampleInfo.TO_AFFECTION));
			ncfile.write(cNetCDF.Variables.VAR_SAMPLES_AFFECTION, sampleOrig1D, samplesD1);

			samplesD2 = NetCdfUtils.writeValuesToD2ArrayChar(sampleInfos, SampleInfo.TO_CATEGORY, commonStringMaxLength);
			ncfile.write(cNetCDF.Variables.VAR_SAMPLE_CATEGORY, sampleOrig2D, samplesD2);

			samplesD2 = NetCdfUtils.writeValuesToD2ArrayChar(sampleInfos, SampleInfo.TO_DISEASE, commonStringMaxLength);
			ncfile.write(cNetCDF.Variables.VAR_SAMPLE_DISEASE, sampleOrig2D, samplesD2);

			samplesD2 = NetCdfUtils.writeValuesToD2ArrayChar(sampleInfos, SampleInfo.TO_POPULATION, commonStringMaxLength);
			ncfile.write(cNetCDF.Variables.VAR_SAMPLE_POPULATION, sampleOrig2D, samplesD2);

			samplesD1 = NetCdfUtils.writeValuesToD1ArrayInt(sampleInfos, SampleInfo.TO_AGE);
			ncfile.write(cNetCDF.Variables.VAR_SAMPLE_AGE, sampleOrig1D, samplesD1);

			samplesD2 = NetCdfUtils.writeValuesToD2ArrayChar(sampleInfos, SampleInfo.TO_FILTER, commonStringMaxLength);
			ncfile.write(cNetCDF.Variables.VAR_SAMPLE_FILTER, sampleOrig2D, samplesD2);

			samplesD1 = NetCdfUtils.writeValuesToD1ArrayInt(sampleInfos, SampleInfo.TO_APPROVED);
			ncfile.write(cNetCDF.Variables.VAR_SAMPLE_APPROVED, sampleOrig1D, samplesD1);

			samplesD1 = NetCdfUtils.writeValuesToD1ArrayInt(sampleInfos, SampleInfo.TO_STATUS);
			ncfile.write(cNetCDF.Variables.VAR_SAMPLE_STATUS, sampleOrig1D, samplesD1);
		}

		log.info("Done writing SampleSet to matrix");
	}

	private static void saveMarkersMetadata(Collection<MarkerMetadata> markerMetadatas, boolean hasDictionary, String strandFlag, NetcdfFileWriteable wrNcFile) throws IOException, InvalidRangeException {

		ArrayChar.D2 markersD2;
		final int[] markersOrig = new int[] {0, 0};

		// WRITE RSID
		markersD2 = NetCdfUtils.writeValuesToD2ArrayChar(markerMetadatas, MarkerMetadata.TO_RS_ID, cNetCDF.Strides.STRIDE_MARKER_NAME);
		wrNcFile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		log.info("Done writing RsId to matrix");

		// WRITE MARKERID
		markersD2 = NetCdfUtils.writeValuesToD2ArrayChar(markerMetadatas, MarkerMetadata.TO_MARKER_ID, cNetCDF.Strides.STRIDE_MARKER_NAME);
		wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		log.info("Done writing MarkerId to matrix");

		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		// Chromosome location for each marker
		markersD2 = NetCdfUtils.writeValuesToD2ArrayChar(markerMetadatas, MarkerMetadata.TO_CHR, cNetCDF.Strides.STRIDE_CHR);
		wrNcFile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
		log.info("Done writing chromosomes to matrix");

		// WRITE POSITION METADATA FROM ANNOTATION FILE
		ArrayInt.D1 markersPosD1 = NetCdfUtils.writeValuesToD1ArrayInt(markerMetadatas, MarkerMetadata.TO_POS);
		int[] posOrig = new int[] {0};
		wrNcFile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		log.info("Done writing positions to matrix");

		// WRITE CUSTOM ALLELES METADATA FROM ANNOTATION FILE
		if (hasDictionary) {
			markersD2 = NetCdfUtils.writeValuesToD2ArrayChar(markerMetadatas, MarkerMetadata.TO_ALLELES, cNetCDF.Strides.STRIDE_GT);
			wrNcFile.write(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, markersOrig, markersD2);
			log.info("Done writing forward alleles to matrix");
		}

		// WRITE GENOTYPE STRAND FROM ANNOTATION FILE
		int[] gtOrig = new int[] {0, 0};
		if (strandFlag == null) {
			// TODO Strand info is buggy in Hapmap bulk download!
			markersD2 = NetCdfUtils.writeValuesToD2ArrayChar(markerMetadatas, MarkerMetadata.TO_STRAND, cNetCDF.Strides.STRIDE_STRAND);
		} else {
			markersD2 = NetCdfUtils.writeSingleValueToD2ArrayChar(strandFlag, cNetCDF.Strides.STRIDE_STRAND, markerMetadatas.size());
		}
		wrNcFile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
		log.info("Done writing strand info to matrix");
	}

	private static void saveChromosomeMetadata(Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo, NetcdfFileWriteable wrNcFile) throws IOException {

		// Set of chromosomes found in matrix along with number of markersinfo
		NetCdfUtils.saveObjectsToStringToMatrix(wrNcFile, chromosomeInfo.keySet(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, cNetCDF.Strides.STRIDE_CHR);
		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[] {0, 1, 2, 3};
		NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(wrNcFile, chromosomeInfo.values(), columns, cNetCDF.Variables.VAR_CHR_INFO);
		log.info("Done writing chromosome infos");
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws IOException {
		super.startLoadingAlleles(perSample);

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

		String savingBy = (perSample ? "sample" : "marker");
		log.info("initialized NetCDF storage, saving {} by {}", savingBy, savingBy);
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws IOException {
		super.addSampleGTAlleles(sampleIndex, sampleAlleles);

		if (!alleleLoadPerSample) {
			throw new IllegalStateException("You can not mix loading per sample and loading per marker");
		}

		NetCdfUtils.saveSingleSampleGTsToMatrix(ncfile, sampleAlleles, sampleIndex);

		final int numSamples = matrixMetadata.getNumSamples();
		if ((sampleIndex == 0) || ((sampleIndex + 1 % 100) == 0) || ((sampleIndex  + 1) == numSamples)) {
			log.info("Stored sample genotype-lists: {} / {}", sampleIndex + 1, numSamples);
		}
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws IOException {
		super.addMarkerGTAlleles(markerIndex, markerAlleles);

		if (alleleLoadPerSample) {
			throw new IllegalStateException("You can not mix loading per sample and loading per marker");
		}

		genotypesHyperslabs.addAll(markerAlleles);
		curAllelesMarkerIndex = markerIndex;

		if (curAllelesMarkerIndex != 1 && curAllelesMarkerIndex % (hyperSlabRows) == 0) {
			// WRITING HYPERSLABS AT A TIME
			ArrayByte.D3 genotypesArray = NetCdfUtils.writeListValuesToSamplesHyperSlabArrayByteD3(
					genotypesHyperslabs, getDataSet().getSampleInfos().size(), cNetCDF.Strides.STRIDE_GT);
			int[] origin = new int[] {0, (curAllelesMarkerIndex - hyperSlabRows), 0}; // 0, 0, 0 for 1st marker; 0, 1, 0 for 2nd marker ...
			try {
				ncfile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypesArray);
				log.info("Stored marker genotype-lists: {} / {}", curAllelesMarkerIndex + 1, matrixMetadata.getNumMarkers());
			} catch (InvalidRangeException ex) {
				throw new IOException("Bad origin at rowCount " + curAllelesMarkerIndex + ": " + origin[0] + "|" + origin[1] + "|" + origin[2], ex);
			}

			genotypesHyperslabs.clear();
		}
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {
		super.finishedLoadingAlleles();

		if (!alleleLoadPerSample) {
//			int markerNb = bimSamples.size();
			int markerNb = curAllelesMarkerIndex;
			// WRITING LAST HYPERSLAB
			int lastHyperSlabRows = markerNb - (genotypesHyperslabs.size() / getDataSet().getSampleInfos().size());
			ArrayByte.D3 genotypesArray = NetCdfUtils.writeListValuesToSamplesHyperSlabArrayByteD3(genotypesHyperslabs, getDataSet().getSampleInfos().size(), cNetCDF.Strides.STRIDE_GT);
			int[] origin = new int[] {0, lastHyperSlabRows, 0}; //0, 0, 0 for 1st marker; 0, 1, 0 for 2nd marker....
//			log.info("Last origin at rowCount "+rowCounter+": "+origin[0]+"|"+origin[1]+"|"+origin[2]);
			try {
				ncfile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypesArray);
				log.info("Stored marker genotype-lists: {} / {}", curAllelesMarkerIndex + 1, matrixMetadata.getNumMarkers());
			} catch (InvalidRangeException ex) {
				throw new IOException("Bad origin at rowCount " + curAllelesMarkerIndex + ": " + origin[0] + "|" + origin[1] + "|" + origin[2], ex);
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
			guessedGTCodeAC.setString(index.set(0, 0), getGuessedGTCode().toString());
			int[] origin = new int[] {0, 0};
			ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			StringBuilder description = new StringBuilder(matrixMetadata.getDescription());
			description.append("Genotype encoding: ");
			description.append(getGuessedGTCode().toString());
			matrixMetadata.setDescription(description.toString());
			MatricesList.updateMatrix(matrixMetadata);

			// CLOSE FILE
			ncfile.close();
//			resultMatrixKey = MatrixKey.valueOf(matrixMetadata);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}

		org.gwaspi.global.Utils.sysoutCompleted("Loading Genotypes");
	}

	protected abstract GenotypeEncoding getGuessedGTCode();

	@Override
	public void done() throws IOException {
		super.done();
	}

	static List<SampleKey> extractKeys(Collection<SampleInfo> sampleInfos) {

		List<SampleKey> sampleKeys = new ArrayList<SampleKey>(sampleInfos.size());

		for (SampleInfo sampleInfo : sampleInfos) {
			sampleKeys.add(sampleInfo.getKey());
		}

		return sampleKeys;
	}
}

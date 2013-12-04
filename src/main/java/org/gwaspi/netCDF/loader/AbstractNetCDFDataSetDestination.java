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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

public abstract class AbstractNetCDFDataSetDestination extends AbstractDataSetDestination {

	private static final Logger log
			= LoggerFactory.getLogger(AbstractNetCDFDataSetDestination.class);

	private MatrixKey resultMatrixKey;
//	private int curAlleleSampleIndex;
	private int curAllelesMarkerIndex;
	private MatrixFactory matrixFactory;
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

		resultMatrixKey = null;
	}

	@Override
	public void finishedLoadingSampleInfos() throws IOException {
		super.finishedLoadingSampleInfos();

		SampleInfoList.insertSampleInfos(getDataSet().getSampleInfos());
	}

	protected abstract MatrixFactory createMatrixFactory() throws IOException;

	/**
	 * @return the strand-flag applicable to all markers,
	 *   or <code>null</code>, if each marker has a separate strand-flag
	 */
	protected abstract String getStrandFlag();

	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException {
		super.finishedLoadingMarkerMetadatas();

		try {
			matrixFactory = createMatrixFactory();
			MatrixMetadata resultMatrixMetadata = matrixFactory.getResultMatrixMetadata();

			getDataSet().setMatrixMetadata(resultMatrixMetadata);

			// create the NetCDF file
			ncfile = matrixFactory.getNetCDFHandler();
			ncfile.create();
			log.trace("Done creating netCDF handle: " + ncfile.toString());

			Collection<SampleInfo> sampleInfos = getDataSet().getSampleInfos();
			List<SampleKey> sampleKeys = extractKeys(sampleInfos);
			saveSamplesMetadata(sampleKeys, ncfile);

			boolean hasDictionary = resultMatrixMetadata.getHasDictionray();
			saveMarkersMetadata(getDataSet().getMarkerMetadatas().values(), hasDictionary, getStrandFlag(), ncfile);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void finishedLoadingChromosomeMetadatas() throws IOException {
		super.finishedLoadingChromosomeMetadatas();

		try {
			saveChromosomeMetadata(getDataSet().getChromosomeInfos(), ncfile);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	private static void saveSamplesMetadata(Collection<SampleKey> sampleKeys, NetcdfFileWriteable ncfile) throws IOException, InvalidRangeException {

		// WRITE SAMPLESET TO MATRIX FROM SAMPLES LIST
		ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(sampleKeys, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
		int[] sampleOrig = new int[] {0, 0};
		ncfile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
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

	private static void saveChromosomeMetadata(Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo, NetcdfFileWriteable wrNcFile) throws IOException, InvalidRangeException {

		// Set of chromosomes found in matrix along with number of markersinfo
		NetCdfUtils.saveObjectsToStringToMatrix(wrNcFile, chromosomeInfo.keySet(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, cNetCDF.Strides.STRIDE_CHR);
		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[] {0, 1, 2, 3};
		NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(wrNcFile, chromosomeInfo.values(), columns, cNetCDF.Variables.VAR_CHR_INFO);
		log.info("Done writing chromosome infos");
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws IOException {

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

		if (!alleleLoadPerSample) {
			throw new IllegalStateException("You can not mix loading per sample and loading per marker");
		}

		NetCdfUtils.saveSingleSampleGTsToMatrix(ncfile, sampleAlleles, sampleIndex);
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws IOException {

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
			} catch (InvalidRangeException ex) {
				throw new IOException("Bad origin at rowCount " + curAllelesMarkerIndex + ": " + origin[0] + "|" + origin[1] + "|" + origin[2], ex);
			}

			genotypesHyperslabs.clear();
		}
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {

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
				log.info("Processed markers: " + curAllelesMarkerIndex);
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

			MatrixMetadata matrixMetaData = matrixFactory.getResultMatrixMetadata();
			StringBuilder descSB = new StringBuilder(matrixMetaData.getDescription());
			descSB.append("Genotype encoding: ");
			descSB.append(getGuessedGTCode().toString());
			matrixMetaData.setDescription(descSB.toString());
			MatricesList.updateMatrix(matrixMetaData);

			// CLOSE FILE
			ncfile.close();
			resultMatrixKey = MatrixKey.valueOf(matrixMetaData);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
	}

	protected abstract GenotypeEncoding getGuessedGTCode();

	@Override
	public void done() throws IOException {
	}

	static List<SampleKey> extractKeys(Collection<SampleInfo> sampleInfos) {

		List<SampleKey> sampleKeys = new ArrayList<SampleKey>(sampleInfos.size());

		for (SampleInfo sampleInfo : sampleInfos) {
			sampleKeys.add(sampleInfo.getKey());
		}

		return sampleKeys;
	}
}

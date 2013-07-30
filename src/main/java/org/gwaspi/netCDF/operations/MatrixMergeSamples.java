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

package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

public class MatrixMergeSamples extends AbstractMergeMatrixOperation {

	private final Logger log = LoggerFactory.getLogger(MatrixMergeSamples.class);

	/**
	 * This constructor to join 2 Matrices.
	 * The MarkerSet from the 1st Matrix will be used in the result Matrix.
	 * No new Markers from the 2nd Matrix will be added.
	 * Samples from the 2nd Matrix will be appended to the end of the SampleSet from the 1st Matrix.
	 * Duplicate Samples from the 2nd Matrix will overwrite Samples in the 1st Matrix
	 */
	public MatrixMergeSamples(
			MatrixKey rdMatrixKey1,
			MatrixKey rdMatrixKey2,
			String wrMatrixFriendlyName,
			String wrMatrixDescription)
			throws IOException, InvalidRangeException
	{
		super(
				rdMatrixKey1,
				rdMatrixKey2,
				wrMatrixFriendlyName,
				wrMatrixDescription);
	}

	/**
	 * Appends samples and keeps markers constant.
	 */
	@Override
	public int processMatrix() throws IOException, InvalidRangeException {
		int resultMatrixId = Integer.MIN_VALUE;

		NetcdfFile rdNcFile1 = NetcdfFile.open(rdMatrix1Metadata.getPathToMatrix());
		NetcdfFile rdNcFile2 = NetcdfFile.open(rdMatrix2Metadata.getPathToMatrix());

		// Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
		Map<SampleKey, byte[]> rdSampleSetMap1 = rdSampleSet1.getSampleIdSetMapByteArray();
		Map<SampleKey, byte[]> rdSampleSetMap2 = rdSampleSet2.getSampleIdSetMapByteArray();
		Map<SampleKey, int[]> wrComboSampleSetMap = getComboSampleSetWithIndicesArray(rdSampleSetMap1, rdSampleSetMap2);
		Map<SampleKey, ?> theSamples = wrComboSampleSetMap;

		// Use comboed wrComboSampleSetMap as SampleSet
		final int numSamples = theSamples.size();
		final String humanReadableMethodName = Text.Trafo.mergeSamplesOnly;
		final String methodDescription = Text.Trafo.mergeMethodSampleJoin;

		rdMarkerSet1.initFullMarkerIdSetMap();
		rdMarkerSet2.initFullMarkerIdSetMap();

		// RETRIEVE CHROMOSOMES INFO
		Map<MarkerKey, int[]> chrInfo = rdMarkerSet1.getChrInfoSetMap();

		//<editor-fold defaultstate="expanded" desc="CREATE MATRIX">
		try {
			// CREATE netCDF-3 FILE
			boolean hasDictionary = false;
			if (rdMatrix1Metadata.getHasDictionray() == rdMatrix2Metadata.getHasDictionray()) {
				hasDictionary = rdMatrix1Metadata.getHasDictionray();
			}
			GenotypeEncoding gtEncoding = GenotypeEncoding.UNKNOWN;
			if (rdMatrix1Metadata.getGenotypeEncoding().equals(rdMatrix2Metadata.getGenotypeEncoding())) {
				gtEncoding = rdMatrix1Metadata.getGenotypeEncoding();
			}
			ImportFormat technology = ImportFormat.UNKNOWN;
			if (rdMatrix1Metadata.getTechnology().equals(rdMatrix2Metadata.getTechnology())) {
				technology = rdMatrix1Metadata.getTechnology();
			}

			StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
			descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
			descSB.append("\n");
			descSB.append("Markers: ").append(rdSampleSetMap1.size()).append(", Samples: ").append(numSamples);
			descSB.append("\n");
			descSB.append(Text.Trafo.mergedFrom);
			descSB.append("\nMX-");
			descSB.append(rdMatrix1Metadata.getMatrixId());
			descSB.append(" - ");
			descSB.append(rdMatrix1Metadata.getMatrixFriendlyName());
			descSB.append("\nMX-");
			descSB.append(rdMatrix2Metadata.getMatrixId());
			descSB.append(" - ");
			descSB.append(rdMatrix2Metadata.getMatrixFriendlyName());
			descSB.append("\n\n");
			descSB.append("Merge Method - ");
			descSB.append(humanReadableMethodName);
			descSB.append(":\n");
			descSB.append(methodDescription);

			MatrixFactory wrMatrixHandler = new MatrixFactory(
					technology, // technology
					wrMatrixFriendlyName,
					wrMatrixDescription + "\n\n" + descSB.toString(), // description
					gtEncoding, // GT encoding
					rdMatrix1Metadata.getStrand(),
					hasDictionary, // has dictionary?
					numSamples,
					rdMarkerSet1.getMarkerSetSize(), // Keep rdwrMarkerIdSetMap1 from Matrix1. MarkerSet is constant
					chrInfo.size(),
					rdMatrix1Key, // Parent matrixId 1
					rdMatrix2Key); // Parent matrixId 2

			NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
			try {
				wrNcFile.create();
			} catch (IOException ex) {
				log.error("Failed creating file " + wrNcFile.getLocation(), ex);
			}
			//log.trace("Done creating netCDF handle in MatrixSampleJoin: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

			//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
			// SAMPLESET
			ArrayChar.D2 samplesD2 = Utils.writeCollectionToD2ArrayChar(theSamples.keySet(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);

			int[] sampleOrig = new int[]{0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			}
			log.info("Done writing SampleSet to matrix");

			// Keep rdwrMarkerIdSetMap1 from Matrix1 constant
			// MARKERSET MARKERID
			ArrayChar.D2 markersD2 = Utils.writeCollectionToD2ArrayChar(rdMarkerSet1.getMarkerKeys(), cNetCDF.Strides.STRIDE_MARKER_NAME);
			int[] markersOrig = new int[] {0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			}

			// MARKERSET RSID
			rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet1.getMarkerIdSetMapCharArray().values(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

			// MARKERSET CHROMOSOME
			rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet1.getMarkerIdSetMapCharArray().values(), cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

			// Set of chromosomes found in matrix along with number of markersinfo
			org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(wrNcFile, chrInfo, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
			// Number of marker per chromosome & max pos for each chromosome
			int[] columns = new int[] {0, 1, 2, 3};
			org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(wrNcFile, chrInfo.values(), columns, cNetCDF.Variables.VAR_CHR_INFO);

			// MARKERSET POSITION
			rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			//Utils.saveCharMapValueToWrMatrix(wrNcFile, rdwrMarkerIdSetMap1, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
			Utils.saveIntMapD1ToWrMatrix(wrNcFile, rdMarkerSet1.getMarkerIdSetMapInteger().values(), cNetCDF.Variables.VAR_MARKERS_POS);

			// MARKERSET DICTIONARY ALLELES
			Attribute hasDictionary1 = rdNcFile1.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY);
			if ((Integer) hasDictionary1.getNumericValue() == 1) {
				rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet1.getMarkerIdSetMapCharArray().values(), cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);
			}

			//<editor-fold defaultstate="expanded" desc="GENOTYPE STRAND">
			rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet1.getMarkerIdSetMapCharArray().values(), cNetCDF.Variables.VAR_GT_STRAND, 3);
			//</editor-fold>
			//</editor-fold>

			writeGenotypes(wrNcFile, wrComboSampleSetMap.values());

			// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
			try {
				// GENOTYPE ENCODING
				ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
				Index index = guessedGTCodeAC.getIndex();
				guessedGTCodeAC.setString(index.set(0, 0), rdMatrix1Metadata.getGenotypeEncoding().toString());
				int[] origin = new int[]{0, 0};
				wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

				descSB.append("\nGenotype encoding: ");
				descSB.append(rdMatrix1Metadata.getGenotypeEncoding());

				MatrixMetadata resultMatrixMetadata = wrMatrixHandler.getResultMatrixMetadata();
				resultMatrixMetadata.setDescription(descSB.toString());
				MatricesList.updateMatrix(resultMatrixMetadata);

				resultMatrixId = wrMatrixHandler.getResultMatrixId();

				wrNcFile.close();
				rdNcFile1.close();
				rdNcFile2.close();

				// CHECK FOR MISMATCHES
				if (rdMatrix1Metadata.getGenotypeEncoding().equals(GenotypeEncoding.ACGT0)
						|| rdMatrix1Metadata.getGenotypeEncoding().equals(GenotypeEncoding.O1234))
				{
					double[] mismatchState = checkForMismatches(wrMatrixHandler.getResultMatrixKey()); // mismatchCount, mismatchRatio
					if (mismatchState[1] > 0.01) {
						log.warn("");
						log.warn("Mismatch ratio is bigger than 1% ({}%)!", (mismatchState[1] * 100));
						log.warn("There might be an issue with strand positioning of your genotypes!");
						log.warn("");
						//resultMatrixId = new int[] {wrMatrixHandler.getResultMatrixId(),-4};  // The threshold of acceptable mismatching genotypes has been crossed
					}
				}
			} catch (IOException ex) {
				log.error("Failed creating file " + wrNcFile.getLocation(), ex);
			}

			org.gwaspi.global.Utils.sysoutCompleted("extraction to new Matrix");
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		} catch (IOException ex) {
			log.error(null, ex);
		}
		//</editor-fold>

		return resultMatrixId;
	}

	protected void writeGenotypes(
			NetcdfFileWriteable wrNcFile,
			Collection<int[]> wrComboSampleSetMap)
			throws InvalidRangeException, IOException
	{
		rdMarkerSet2.initFullMarkerIdSetMap();

		// Iterate through wrSampleSetMap, use item position to read correct sample GTs into rdMarkerIdSetMap.
		for (int[] sampleIndices : wrComboSampleSetMap) { // Next SampleId
			// sampleIndices: Next position[rdMatrixNb, rdPos, wrPos] to read/write

			// Iterate through wrMarkerIdSetMap, get the correct GT from rdMarkerIdSetMap
			if (sampleIndices[0] == 1) { // Read from Matrix1
				rdMarkerSet1.fillWith(cNetCDF.Defaults.DEFAULT_GT);
				rdMarkerSet1.fillGTsForCurrentSampleIntoInitMap(sampleIndices[1]);
			}
			if (sampleIndices[0] == 2) { // Read from Matrix2
				rdMarkerSet1.fillWith(cNetCDF.Defaults.DEFAULT_GT);
				rdMarkerSet2.fillGTsForCurrentSampleIntoInitMap(sampleIndices[1]);
				for (Map.Entry<MarkerKey, byte[]> entry : rdMarkerSet1.getMarkerIdSetMapByteArray().entrySet()) {
					MarkerKey key = entry.getKey();
					if (rdMarkerSet2.getMarkerIdSetMapByteArray().containsKey(key)) {
						byte[] markerValue = rdMarkerSet2.getMarkerIdSetMapByteArray().get(key);
						entry.setValue(markerValue);
					}
				}
				//rdwrMarkerIdSetMap1 = rdMarkerSet2.replaceWithValuesFrom(rdwrMarkerIdSetMap1, rdMarkerIdSetMap2);
			}

			// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
			Utils.saveSingleSampleGTsToMatrix(wrNcFile, rdMarkerSet1.getMarkerIdSetMapByteArray().values(), sampleIndices[2]);
		}
	}
}

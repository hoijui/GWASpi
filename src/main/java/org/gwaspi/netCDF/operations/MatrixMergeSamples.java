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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersChromosomeInfosSource;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
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
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2,
			DataSetDestination dataSetDestination)
			throws IOException
	{
		super(
			dataSetSource1,
			dataSetSource2,
			dataSetDestination);
	}

	private MatrixFactory createMatrixFactory(MatrixKey matrix1Key, MatrixKey matrix2Key, int numSamples, int numMarkers, int numChromosomes, String matrixFriendlyName, String matrixDescription) throws IOException {

		// Use comboed wrComboSampleSetMap as SampleSet
		final String humanReadableMethodName = Text.Trafo.mergeSamplesOnly;
		final String methodDescription = Text.Trafo.mergeMethodSampleJoin;

		MatrixMetadata rdMatrix1Metadata = dataSetSource1.getMatrixMetadata();
		MatrixMetadata rdMatrix2Metadata = dataSetSource2.getMatrixMetadata();

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

		StringBuilder description = new StringBuilder();
		description.append(matrixDescription);
		description.append("\n\n");
		description.append(Text.Matrix.descriptionHeader1);
		description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		description.append("\n");
		description.append("Markers: ").append(dataSetSource1.getMarkersKeysSource().size());
		description.append(", Samples: ").append(numSamples);
		description.append("\n");
		description.append(Text.Trafo.mergedFrom);
		description.append("\nMX-");
		description.append(rdMatrix1Metadata.getMatrixId());
		description.append(" - ");
		description.append(rdMatrix1Metadata.getMatrixFriendlyName());
		description.append("\nMX-");
		description.append(rdMatrix2Metadata.getMatrixId());
		description.append(" - ");
		description.append(rdMatrix2Metadata.getMatrixFriendlyName());
		description.append("\n\n");
		description.append("Merge Method - ");
		description.append(humanReadableMethodName);
		description.append(":\n");
		description.append(methodDescription);
		description.append("\nGenotype encoding: ");
		description.append(gtEncoding.toString());

		try {
			return new MatrixFactory(
					technology, // technology
					matrixFriendlyName,
					description.toString(), // description
					gtEncoding, // GT encoding
					rdMatrix1Metadata.getStrand(),
					hasDictionary, // has dictionary?
					numSamples,
					numMarkers,
					numChromosomes,
					matrix1Key, // Parent matrix 1 key
					matrix2Key); // Parent matrix 2 key
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	/**
	 * Appends samples and keeps markers constant.
	 */
	@Override
	public int processMatrix() throws IOException {
		int resultMatrixId = Integer.MIN_VALUE;

		// Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
		Map<SampleKey, int[]> wrComboSampleSetMap
				= getComboSampleSetWithIndicesArray(
				dataSetSource1.getSamplesKeysSource(),
				dataSetSource2.getSamplesKeysSource());
		Map<SampleKey, ?> theSamples = wrComboSampleSetMap;

		// Use comboed wrComboSampleSetMap as SampleSet
		final int numSamples = theSamples.size();
		// Keep rdwrMarkerIdSetMap1 from Matrix1. MarkerSet is constant
		final int numMarkers = dataSetSource1.getMarkersKeysSource().size();

		// RETRIEVE CHROMOSOMES INFO
		MarkersChromosomeInfosSource chromosomeInfo = dataSetSource1.getMarkersChromosomeInfosSource();

		MatrixFactory wrMatrixHandler = createMatrixFactory(rdMatrix1Key, rdMatrix2Key, numSamples, numMarkers, chromosomeInfo.size(), wrMatrixFriendlyName, wrMatrixDescription);

		try {
			NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
			wrNcFile.create();
			log.trace("Done creating netCDF handle: " + wrNcFile.toString());

			// NOTE We do not need to safe the sample-info again, cause it is already stored in the study from the two matrices we are merging
//			for (SampleKey sampleKey : theSamples.keySet()) {
//				dataSetDestination.addSampleInfo(sampleKey);
//			}

			// copy & paste the marker-metadata from matrix 1
			for (MarkerMetadata markerMetadata : dataSetSource1.getMarkersMetadatasSource()) {
				dataSetDestination.addMarkerMetadata(markerMetadata);
			}

			writeGenotypes(wrComboSampleSetMap.values());

			GenotypeEncoding genotypeEncoding = rdMatrix1Metadata.getGenotypeEncoding();
			// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
			// GENOTYPE ENCODING
			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), genotypeEncoding.toString());
			int[] origin = new int[] {0, 0};
			wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			resultMatrixId = wrMatrixHandler.getResultMatrixId();

			// CHECK FOR MISMATCHES
			if (genotypeEncoding.equals(GenotypeEncoding.ACGT0)
					|| genotypeEncoding.equals(GenotypeEncoding.O1234))
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

			org.gwaspi.global.Utils.sysoutCompleted("extraction to new Matrix");
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}

		return resultMatrixId;
	}

	private void writeGenotypes(
			Collection<int[]> wrComboSampleSetMap)
			throws IOException
	{
		// Iterate through wrSampleSetMap, use item position to read correct sample GTs into rdMarkerIdSetMap.
		for (int[] sampleIndices : wrComboSampleSetMap) { // Next SampleId
			// sampleIndices: Next position[rdMatrixNb, rdPos, wrPos] to read/write
			final int dataSetIndices = sampleIndices[0];
			final int readSampleIndices = sampleIndices[1];
			final int writeSampleIndices = sampleIndices[2];

			DataSetSource readDataSetSource;
			if (dataSetIndices == 1) {
				readDataSetSource = dataSetSource1;
			} else if (dataSetIndices == 2) {
				readDataSetSource = dataSetSource2;
			} else {
				throw new RuntimeException("Invalid dataSetIndices " + dataSetIndices);
			}

			List<byte[]> readSampleGenotypes = readDataSetSource.getSamplesGenotypesSource().get(readSampleIndices);

			if (dataSetIndices == 2) {
				// make sure we use the order of marker keys of the first data set
				List<byte[]> origReadSampleGenotypes = readSampleGenotypes;
				readSampleGenotypes = new ArrayList<byte[]>(Collections.nCopies(dataSetSource1.getMarkersKeysSource().size(), cNetCDF.Defaults.DEFAULT_GT));
				MarkersKeysSource markersKeysSource2 = dataSetSource2.getMarkersKeysSource();
				int index1 = 0;
				for (MarkerKey markerKey : dataSetSource1.getMarkersKeysSource()) {
					final int index2 = markersKeysSource2.indexOf(markerKey);
					if (index2 >= 0) {
						readSampleGenotypes.set(index1, origReadSampleGenotypes.get(index2));
					}
					index1++;
				}
			}

			dataSetDestination.addSampleGTAlleles(writeSampleIndices, readSampleGenotypes);
		}
	}
}

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
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersChromosomeInfosSource;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
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

	/**
	 * Appends samples and keeps markers constant.
	 */
	@Override
	public int processMatrix() throws IOException {
		int resultMatrixId = Integer.MIN_VALUE;

		// Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
		Map<SampleKey, int[]> wrComboSampleSetMap = getComboSampleSetWithIndicesArray(dataSetSource1.getSamplesKeysSource(), dataSetSource2.getSamplesKeysSource());
		Map<SampleKey, ?> theSamples = wrComboSampleSetMap;

		// Use comboed wrComboSampleSetMap as SampleSet
		final int numSamples = theSamples.size();
		final String humanReadableMethodName = Text.Trafo.mergeSamplesOnly;
		final String methodDescription = Text.Trafo.mergeMethodSampleJoin;

		// RETRIEVE CHROMOSOMES INFO
		MarkersChromosomeInfosSource chrInfo = dataSetSource1.getMarkersChromosomeInfosSource();

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
			descSB.append("Markers: ").append(dataSetSource1.getMarkersKeysSource().size()).append(", Samples: ").append(numSamples);
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

			// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
			// GENOTYPE ENCODING
			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), rdMatrix1Metadata.getGenotypeEncoding().toString());
			int[] origin = new int[] {0, 0};
			wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			descSB.append("\nGenotype encoding: ");
			descSB.append(rdMatrix1Metadata.getGenotypeEncoding());

			MatrixMetadata resultMatrixMetadata = wrMatrixHandler.getResultMatrixMetadata();
			resultMatrixMetadata.setDescription(descSB.toString());
			MatricesList.updateMatrix(resultMatrixMetadata);

			resultMatrixId = wrMatrixHandler.getResultMatrixId();

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

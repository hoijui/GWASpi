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

package org.gwaspi.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.DataSetDestination;

public class MatrixMergeSamples extends AbstractMergeMatrixOperation {

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					true,
					Text.Trafo.mergeSamplesOnly,
					Text.Trafo.mergeMethodSampleJoin,
					null);
	static {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new MatrixOperationFactory(
				MatrixMergeSamples.class, OPERATION_TYPE_INFO));
	}

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

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	/**
	 * Appends samples and keeps markers constant.
	 */
	@Override
	public int processMatrix() throws IOException {

		int resultMatrixId = MatrixKey.NULL_ID;

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

		final DataSetDestination dataSetDestination = getDataSetDestination();

		dataSetDestination.init();

		// NOTE We do not need to safe the sample-info again,
		//   cause it is already stored in the study
		//   from the two matrices we are merging
		// NOTE the above only applies to NetCDF?
		dataSetDestination.startLoadingSampleInfos(true);
		for (SampleKey sampleKey : theSamples.keySet()) {
			dataSetDestination.addSampleKey(sampleKey);
		}
		dataSetDestination.finishedLoadingSampleInfos();

		// copy & paste the marker-metadata from matrix 1
		dataSetDestination.startLoadingMarkerMetadatas(false); // FIXME could be true!
		for (MarkerMetadata markerMetadata : dataSetSource1.getMarkersMetadatasSource()) {
			dataSetDestination.addMarkerMetadata(markerMetadata);
		}
		dataSetDestination.finishedLoadingMarkerMetadatas();

		// RETRIEVE CHROMOSOMES INFO
		dataSetDestination.startLoadingChromosomeMetadatas();
		Iterator<ChromosomeInfo> chromosomesInfosIt = dataSetSource1.getChromosomesInfosSource().iterator();
		for (ChromosomeKey chromosomeKey : dataSetSource1.getChromosomesKeysSource()) {
			ChromosomeInfo chromosomeInfo = chromosomesInfosIt.next();
			dataSetDestination.addChromosomeMetadata(chromosomeKey, chromosomeInfo);
		}
		dataSetDestination.finishedLoadingChromosomeMetadatas();

		writeGenotypesMeta(wrComboSampleSetMap.values());

		dataSetDestination.done();

		org.gwaspi.global.Utils.sysoutCompleted("Merging into to new Matrix");

		return resultMatrixId;
	}

	protected void writeGenotypesMeta(Collection<int[]> wrComboSampleSetMap)
			throws IOException
	{
		initiateGenotypesMismatchChecking(dataSetSource1.getMarkersKeysSource().size());
		writeGenotypes(wrComboSampleSetMap);
		finalizeGenotypesMismatchChecking();
		validateMissingRatio();
	}

	private void writeGenotypes(Collection<int[]> wrComboSampleSetMap)
			throws IOException
	{
		// Iterate through wrSampleSetMap, use item position to read
		// the correct sample GTs into rdMarkerIdSetMap.
		getDataSetDestination().startLoadingAlleles(true);
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

			addSampleGTAlleles(writeSampleIndices, readSampleGenotypes);
		}
		getDataSetDestination().finishedLoadingAlleles();
	}
}

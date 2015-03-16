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

package org.gwaspi.operations.merge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.global.Text;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.MatrixOperationFactory;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.ProcessInfo;

public class MergeAllMatrixOperation extends AbstractMergeMarkersMatrixOperation {

	private static final ProcessInfo PROCESS_INFO = new DefaultProcessInfo(
			Text.Trafo.mergeAll,
			Text.Trafo.mergeMethodMergeAll);

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					true,
					Text.Trafo.mergeAll,
					Text.Trafo.mergeMethodMergeAll,
					null);
	static {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new MatrixOperationFactory(
				MergeAllMatrixOperation.class, OPERATION_TYPE_INFO));
	}

	public MergeAllMatrixOperation(
			MergeMatrixOperationParams params,
			DataSetDestination dataSetDestination)
			throws IOException
	{
		super(
				params,
				dataSetDestination);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return OPERATION_TYPE_INFO;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return PROCESS_INFO;
	}

	/**
	 * Mingles markers and keeps samples constant.
	 * @throws IOException
	 */
	@Override
	public MatrixKey call() throws IOException {

		// Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
		Map<SampleKey, int[]> wrSampleSetMap = getComboSampleSetWithIndicesArray(dataSetSource1.getSamplesKeysSource(), dataSetSource2.getSamplesKeysSource());
		Map<SampleKey, ?> theSamples = wrSampleSetMap;

		return mergeMatrices(
				wrSampleSetMap,
				theSamples.keySet(),
				OPERATION_TYPE_INFO.getName(),
				OPERATION_TYPE_INFO.getDescription());
	}

	@Override
	protected void writeGenotypes(
			Map<SampleKey, int[]> wrSampleSetMap,
			Collection<MarkerKey> wrComboSortedMarkers)
			throws IOException
	{
		// create indices maps for the two sample sets
		MarkersKeysSource markersKeysSource1 = dataSetSource1.getMarkersKeysSource();
		MarkersKeysSource markersKeysSource2 = dataSetSource2.getMarkersKeysSource();
		Map<MarkerKey, Integer> sampleSet1Indices = new HashMap<MarkerKey, Integer>(markersKeysSource1.size());
		Map<MarkerKey, Integer> sampleSet2Indices = new HashMap<MarkerKey, Integer>(markersKeysSource2.size());
		for (MarkerKey markerKey : wrComboSortedMarkers) {
			sampleSet1Indices.put(markerKey, markersKeysSource1.indexOf(markerKey));
			sampleSet2Indices.put(markerKey, markersKeysSource2.indexOf(markerKey));
		}

		// Get SampleId index from each Matrix
		// Iterate through wrSampleSetMap
		getDataSetDestination().startLoadingAlleles(true);
		int wrSampleIndex = 0;
		for (Map.Entry<SampleKey, int[]> entry : wrSampleSetMap.entrySet()) {
			int[] rdSampleIndices = entry.getValue(); // position[rdPos matrix 1, rdPos matrix 2]

			// Read from Matrix1
			final int index1 = rdSampleIndices[0];
			GenotypesList sampleGTs1 = null;
			if (index1 >= 0) {
				sampleGTs1 = dataSetSource1.getSamplesGenotypesSource().get(index1);
			}

			// Read from Matrix2
			final int index2 = rdSampleIndices[1];
			GenotypesList sampleGTs2 = null;
			if (index2 >= 0) {
				sampleGTs2 = dataSetSource2.getSamplesGenotypesSource().get(index2);
			}

			// Fill wrComboSortedMarkerGTs with matrix 1+2 Genotypes
			Collection<byte[]> wrComboSortedMarkerGTs = new ArrayList<byte[]>(wrComboSortedMarkers.size());
			for (MarkerKey markerKey : wrComboSortedMarkers) {
				byte[] genotype;
				if ((sampleGTs2 != null) && sampleSet2Indices.containsKey(markerKey)) {
					genotype = sampleGTs2.get(sampleSet2Indices.get(markerKey));
				} else if ((sampleGTs1 != null) && sampleSet1Indices.containsKey(markerKey)) {
					genotype = sampleGTs1.get(sampleSet1Indices.get(markerKey));
				} else {
					genotype = NetCDFConstants.Defaults.DEFAULT_GT;
				}

				wrComboSortedMarkerGTs.add(genotype);
			}

			addSampleGTAlleles(wrSampleIndex, new ArrayList<byte[]>(wrComboSortedMarkerGTs));
			wrSampleIndex++;
		}
		getDataSetDestination().finishedLoadingAlleles();
	}
}

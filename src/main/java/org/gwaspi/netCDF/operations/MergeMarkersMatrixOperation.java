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
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.netCDF.loader.DataSetDestination;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

public class MergeMarkersMatrixOperation extends AbstractMergeMarkersMatrixOperation {

	public MergeMarkersMatrixOperation(
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2,
			DataSetDestination dataSetDestination)
			throws IOException, InvalidRangeException
	{
		super(
				dataSetSource1,
				dataSetSource2,
				dataSetDestination);
	}

	@Override
	public int processMatrix() throws IOException, InvalidRangeException {

		// Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
		Map<SampleKey, int[]> wrSampleSetMap = getSampleSetWithIndicesMap(dataSetSource1.getSamplesKeysSource(), dataSetSource2.getSamplesKeysSource());
		Map<SampleKey, byte[]> theSamples = rdSampleSetMap1;

		final int numSamples = rdMatrix1Metadata.getSampleSetSize(); // Keep rdMatrix1Metadata from Matrix1. SampleSet is constant
		final String humanReadableMethodName = Text.Trafo.mergeMarkersOnly;
		final String methodDescription = Text.Trafo.mergeMethodMarkerJoin;

		return mergeMatrices(
				wrSampleSetMap,
				theSamples,
				numSamples,
				humanReadableMethodName,
				methodDescription).getMatrixId();
	}

	@Override
	protected void writeGenotypes(
			NetcdfFileWriteable wrNcFile,
			Map<SampleKey, int[]> wrSampleSetMap,
			Map<MarkerKey, ?> wrComboSortedMarkerSetMap,
			Map<SampleKey, byte[]> rdSampleSetMap1,
			Map<SampleKey, byte[]> rdSampleSetMap2)
			throws InvalidRangeException, IOException
	{
		// Get SampleId index from each Matrix
		// Iterate through wrSampleSetMap
		for (int[] sampleIndices : wrSampleSetMap.values()) { // position[rdPos matrix 1, rdPos matrix 2]
			// Read from Matrix1
			rdMarkerSet1.fillWith(cNetCDF.Defaults.DEFAULT_GT);
			rdMarkerSet1.fillGTsForCurrentSampleIntoInitMap(sampleIndices[0]);

			// Read from Matrix2
			rdMarkerSet2.fillWith(cNetCDF.Defaults.DEFAULT_GT);
			rdMarkerSet2.fillGTsForCurrentSampleIntoInitMap(sampleIndices[1]);

			// Fill wrSortedMingledMarkerMap with matrix 1+2 Genotypes
			Map<MarkerKey, byte[]> wrComboSortedMarkerGTs = new LinkedHashMap<MarkerKey, byte[]>(wrComboSortedMarkerSetMap.size());
			for (Map.Entry<MarkerKey, ?> markerEntry : wrComboSortedMarkerSetMap.entrySet()) {
				MarkerKey markerKey = markerEntry.getKey();
				byte[] genotype = cNetCDF.Defaults.DEFAULT_GT;
				if (rdMarkerSet1.getMarkerIdSetMapByteArray().containsKey(markerKey)) {
					genotype = rdMarkerSet1.getMarkerIdSetMapByteArray().get(markerKey);
				}
				if (rdMarkerSet2.getMarkerIdSetMapByteArray().containsKey(markerKey)) {
					genotype = rdMarkerSet2.getMarkerIdSetMapByteArray().get(markerKey);
				}

				wrComboSortedMarkerGTs.put(markerKey, genotype);
			}

			// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
			Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrComboSortedMarkerGTs.values(), sampleIndices[0]);
		}
	}

	private static Map<SampleKey, int[]> getSampleSetWithIndicesMap(SamplesKeysSource sampleKeys1, SamplesKeysSource sampleKeys2) {
		Map<SampleKey, int[]> resultMap = new LinkedHashMap<SampleKey, int[]>();

		int rdPos = 0;
		for (SampleKey key : sampleKeys1) {
			int[] position = new int[] {rdPos, 0}; // rdPos matrix 1
			resultMap.put(key, position);
			rdPos++;
		}

		rdPos = 0;
		for (SampleKey key : sampleKeys2) {
			// IF SAMPLE ALLREADY EXISTS IN MATRIX1 SUBSTITUTE VALUES WITH MATRIX2
			if (resultMap.containsKey(key)) {
				int[] position = resultMap.get(key);
				position[1] = rdPos; // rdPos matrix 2
				resultMap.put(key, position);
			}

			rdPos++;
		}

		return resultMap;
	}
}

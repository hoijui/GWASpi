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
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

public class MergeAllMatrixOperation extends AbstractMergeMarkersMatrixOperation {

	public MergeAllMatrixOperation(
			int studyId,
			int rdMatrix1Id,
			int rdMatrix2Id,
			String wrMatrixFriendlyName,
			String wrMatrixDescription)
			throws IOException, InvalidRangeException
	{
		super(
				studyId,
				rdMatrix1Id,
				rdMatrix2Id,
				wrMatrixFriendlyName,
				wrMatrixDescription);
	}

	/**
	 * Mingles markers and keeps samples constant.
	 */
	@Override
	public int processMatrix() throws IOException, InvalidRangeException {

		// Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
		Map<SampleKey, byte[]> rdSampleSetMap1 = rdSampleSet1.getSampleIdSetMapByteArray();
		Map<SampleKey, byte[]> rdSampleSetMap2 = rdSampleSet2.getSampleIdSetMapByteArray();
		Map<SampleKey, int[]> wrSampleSetMap = getComboSampleSetWithIndicesArray(rdSampleSetMap1, rdSampleSetMap2);
		Map<SampleKey, ?> theSamples = wrSampleSetMap;

		final int numSamples = wrSampleSetMap.size(); // Comboed SampleSet
		final String humanReadableMethodName = Text.Trafo.mergeAll;
		final String methodDescription = Text.Trafo.mergeMethodMergeAll;

		return mergeMatrices(
				rdSampleSetMap1,
				rdSampleSetMap2,
				wrSampleSetMap,
				theSamples,
				numSamples,
				humanReadableMethodName,
				methodDescription);
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
		int wrSampleIndex = 0;
		for (Map.Entry<SampleKey, int[]> entry : wrSampleSetMap.entrySet()) {
			SampleKey sampleKey = entry.getKey();
			int[] rdSampleIndices = entry.getValue(); // position[rdPos matrix 1, rdPos matrix 2]

			// Read from Matrix1
			rdMarkerSet1.fillWith(cNetCDF.Defaults.DEFAULT_GT);
			if (rdSampleSet1.getSampleKeys().contains(sampleKey)) {
				rdMarkerSet1.fillGTsForCurrentSampleIntoInitMap(rdSampleIndices[0]);
			}

			// Read from Matrix2
			rdMarkerSet2.fillWith(cNetCDF.Defaults.DEFAULT_GT);

			if (rdSampleSet2.getSampleKeys().contains(sampleKey)) {
				rdMarkerSet2.fillGTsForCurrentSampleIntoInitMap(rdSampleIndices[1]);
			}

			// Fill wrSortedMingledMarkerMap with matrix 1+2 Genotypes
			Map<MarkerKey, byte[]> wrComboSortedMarkerGTs = new LinkedHashMap<MarkerKey, byte[]>(wrComboSortedMarkerSetMap.size());
			for (Map.Entry<MarkerKey, ?> markerEntry : wrComboSortedMarkerSetMap.entrySet()) {
				MarkerKey markerKey = markerEntry.getKey();
				byte[] genotype = cNetCDF.Defaults.DEFAULT_GT;
				if (rdSampleSetMap1.containsKey(sampleKey) && rdMarkerSet1.getMarkerIdSetMapByteArray().containsKey(markerKey)) {
					genotype = rdMarkerSet1.getMarkerIdSetMapByteArray().get(markerKey);
				}
				if (rdSampleSetMap2.containsKey(sampleKey) && rdMarkerSet2.getMarkerIdSetMapByteArray().containsKey(markerKey)) {
					genotype = rdMarkerSet2.getMarkerIdSetMapByteArray().get(markerKey);
				}

				wrComboSortedMarkerGTs.put(markerKey, genotype);
			}

			// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
			Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrComboSortedMarkerGTs, wrSampleIndex);
			wrSampleIndex++;
		}
	}
}

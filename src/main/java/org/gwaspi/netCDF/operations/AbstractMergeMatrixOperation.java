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
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.ExtendedMarkerKey;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMergeMatrixOperation implements MatrixOperation {

	private static final Logger log = LoggerFactory.getLogger(AbstractMergeMatrixOperation.class);

	protected final DataSetSource dataSetSource1;
	protected final DataSetSource dataSetSource2;
	protected final DataSetDestination dataSetDestination;

	protected AbstractMergeMatrixOperation(
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2,
			DataSetDestination dataSetDestination)
			throws IOException
	{
		this.dataSetSource1 = dataSetSource1;
		this.dataSetSource2 = dataSetSource2;
		this.dataSetDestination = dataSetDestination;
	}

	private static Map<MarkerKey, ExtendedMarkerKey> getMatrixMapWithChrAndPosAndMarkerId(MarkersMetadataSource markersMetadataSource) {

		Map<MarkerKey, ExtendedMarkerKey> workMap = new LinkedHashMap<MarkerKey, ExtendedMarkerKey>(markersMetadataSource.size());
		for (MarkerMetadata markerMetadata : markersMetadataSource) {
			workMap.put(MarkerKey.valueOf(markerMetadata), ExtendedMarkerKey.valueOf(markerMetadata));
		}

		return workMap;
	}

	protected SortedMap<ExtendedMarkerKey, MarkerKey> mingleAndSortMarkerSetRaw() {

		Map<MarkerKey, ExtendedMarkerKey> workMap = getMatrixMapWithChrAndPosAndMarkerId(dataSetSource1.getMarkersMetadatasSource());
		workMap.putAll(getMatrixMapWithChrAndPosAndMarkerId(dataSetSource2.getMarkersMetadatasSource()));

		// sort by extended marker key
		SortedMap<ExtendedMarkerKey, MarkerKey> sorted = new TreeMap<ExtendedMarkerKey, MarkerKey>();
		for (Map.Entry<MarkerKey, ExtendedMarkerKey> entry : workMap.entrySet()) {
			sorted.put(entry.getValue(), entry.getKey());
		}

		return sorted;
	}

	protected Map<MarkerKey, MarkerMetadata> mingleAndSortMarkerSet() {

		SortedMap<ExtendedMarkerKey, MarkerKey> sorted = mingleAndSortMarkerSetRaw();

		// repackage
		Map<MarkerKey, MarkerMetadata> result = new LinkedHashMap<MarkerKey, MarkerMetadata>();
		for (Map.Entry<ExtendedMarkerKey, MarkerKey> entry : sorted.entrySet()) {
			ExtendedMarkerKey key = entry.getKey();
			MarkerMetadata markerInfo = new MarkerMetadata(
					key.getChr(),
					key.getPos());

			MarkerKey markerKey = entry.getValue();
			result.put(markerKey, markerInfo);
		}

		return result;
	}

	protected static Map<SampleKey, int[]> getComboSampleSetWithIndicesArray(SamplesKeysSource sampleKeys1, SamplesKeysSource sampleKeys2) {
		Map<SampleKey, int[]> resultMap = new LinkedHashMap<SampleKey, int[]>();

		int wrPos = 0;
		int rdPos = 0;
		for (SampleKey key : sampleKeys1) {
			int[] position = new int[] {1, rdPos, wrPos}; // rdMatrixNb, rdPos, wrPos
			resultMap.put(key, position);
			wrPos++;
			rdPos++;
		}

		rdPos = 0;
		for (SampleKey key : sampleKeys2) {
			int[] position;
			// IF SAMPLE ALLREADY EXISTS IN MATRIX1 SUBSTITUTE VALUES WITH MATRIX2
			if (resultMap.containsKey(key)) {
				position = resultMap.get(key);
				position[0] = 2; // rdMatrixNb
				position[1] = rdPos; // rdPos
			} else {
				position = new int[]{2, rdPos, wrPos}; // rdMatrixNb, rdPos, wrPos
			}

			resultMap.put(key, position);
			wrPos++;
			rdPos++;
		}

		return resultMap;
	}

	protected static double[] checkForMismatches(DataSetSource dataSetSource) throws IOException {

		double[] result = new double[2];

		int markerNb = 0;
		int mismatchCount = 0;
		for (GenotypesList markerGenotypes : dataSetSource.getMarkersGenotypesSource()) {
			Map<Byte, Integer> knownAlleles = new LinkedHashMap<Byte, Integer>();

			// Iterate through sampleSet
			for (byte[] tempGT : markerGenotypes) {
				// Gather alleles different from 0 into a list of known alleles and count the number of appearences
				if (tempGT[0] != ((byte) '0')) {
					int tempCount = 0;
					if (knownAlleles.containsKey(tempGT[0])) {
						tempCount = knownAlleles.get(tempGT[0]);
					}
					knownAlleles.put(tempGT[0], tempCount + 1);
				}
				if (tempGT[1] != ((byte) '0')) {
					int tempCount = 0;
					if (knownAlleles.containsKey(tempGT[1])) {
						tempCount = knownAlleles.get(tempGT[1]);
					}
					knownAlleles.put(tempGT[1], tempCount + 1);
				}
			}

			if (knownAlleles.size() > 2) {
				mismatchCount++;
			}

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Checking markers for mismatches: {}", markerNb);
			}
		}
		final int numSamples = dataSetSource.getSamplesKeysSource().size();
		final double mismatchRatio = (double) mismatchCount / numSamples;

		result[0] = mismatchCount;
		result[1] = mismatchRatio;

		return result;
	}
}

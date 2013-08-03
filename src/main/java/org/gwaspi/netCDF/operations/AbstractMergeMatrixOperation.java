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
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.ComparatorChrAutPosMarkerIdAsc;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

public abstract class AbstractMergeMatrixOperation implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(AbstractMergeMatrixOperation.class);

	protected MatrixKey rdMatrix1Key;
	protected MatrixKey rdMatrix2Key;
	protected final String wrMatrixFriendlyName;
	protected final String wrMatrixDescription;
	protected MatrixMetadata rdMatrix1Metadata;
	protected MatrixMetadata rdMatrix2Metadata;
	protected MatrixMetadata wrMatrixMetadata;
	protected MarkerSet rdMarkerSet1;
	protected MarkerSet rdMarkerSet2;
	protected MarkerSet wrMarkerSet;
	protected SampleSet rdSampleSet1;
	protected SampleSet rdSampleSet2;
	protected SampleSet wrSampleSet;

	protected AbstractMergeMatrixOperation(
			MatrixKey rdMatrix1Key,
			MatrixKey rdMatrix2Key,
			String wrMatrixFriendlyName,
			String wrMatrixDescription)
			throws IOException, InvalidRangeException
	{
		this.wrMatrixMetadata = null;
		this.wrMarkerSet = null;
		this.wrSampleSet = null;

		this.rdMatrix1Key = rdMatrix1Key;
		this.rdMatrix2Key = rdMatrix2Key;

		this.rdMatrix1Metadata = MatricesList.getMatrixMetadataById(this.rdMatrix1Key);
		this.rdMatrix2Metadata = MatricesList.getMatrixMetadataById(this.rdMatrix2Key);

		this.wrMatrixFriendlyName = wrMatrixFriendlyName;
		this.wrMatrixDescription = wrMatrixDescription;

		this.rdMarkerSet1 = new MarkerSet(this.rdMatrix1Key);
		this.rdMarkerSet2 = new MarkerSet(this.rdMatrix2Key);

		this.rdSampleSet1 = new SampleSet(this.rdMatrix1Key);
		this.rdSampleSet2 = new SampleSet(this.rdMatrix2Key);
	}

	private static Map<MarkerKey, char[]> getMatrixMapWithChrAndPos(MarkerSet rdMarkerSet) {

		rdMarkerSet.initFullMarkerIdSetMap();

		rdMarkerSet.fillWith(new char[0]);
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
		Map<MarkerKey, char[]> chrMap = new LinkedHashMap<MarkerKey, char[]>(rdMarkerSet.getMarkerIdSetMapCharArray());
		Map<MarkerKey, char[]> workMap = new LinkedHashMap<MarkerKey, char[]>(chrMap.size());
		rdMarkerSet.fillWith(new char[0]);
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
		for (Map.Entry<MarkerKey, char[]> entry : chrMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			char[] chr = entry.getValue();
			Integer pos = rdMarkerSet.getMarkerIdSetMapInteger().get(markerKey);
			StringBuilder sbKey = new StringBuilder(new String(chr));
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(pos.toString());
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerKey.getMarkerId());
			workMap.put(markerKey, sbKey.toString().toCharArray());
		}
		if (rdMarkerSet.getMarkerIdSetMapInteger() != null) {
			rdMarkerSet.getMarkerIdSetMapInteger().clear();
		}

		return workMap;
	}

	protected Map<MarkerKey, MarkerMetadata> mingleAndSortMarkerSet() {

		Map<MarkerKey, char[]> workMap = getMatrixMapWithChrAndPos(rdMarkerSet1);
		workMap.putAll(getMatrixMapWithChrAndPos(rdMarkerSet2));

		// SORT MERGED Map
		SortedMap<String, MarkerKey> sortedMetadataTM = new TreeMap<String, MarkerKey>(new ComparatorChrAutPosMarkerIdAsc());
		for (Map.Entry<MarkerKey, char[]> entry : workMap.entrySet()) {
			MarkerKey key = entry.getKey();
			String value = new String(entry.getValue());
			sortedMetadataTM.put(value, key);
		}

		// PACKAGE IN A Map
		Map<MarkerKey, MarkerMetadata> result = new LinkedHashMap<MarkerKey, MarkerMetadata>();
		for (Map.Entry<String, MarkerKey> entry : sortedMetadataTM.entrySet()) {
			String key = entry.getKey();
			String[] keyValues = key.split(cNetCDF.Defaults.TMP_SEPARATOR);
			MarkerMetadata markerInfo = new MarkerMetadata(
					keyValues[0], // chr
					Integer.parseInt(keyValues[1])); // pos

			MarkerKey markerKey = entry.getValue();
			result.put(markerKey, markerInfo);
		}

		return result;
	}

	protected static Map<SampleKey, int[]> getComboSampleSetWithIndicesArray(Map<SampleKey, ?> sampleSetMap1, Map<SampleKey, ?> sampleSetMap2) {
		Map<SampleKey, int[]> resultMap = new LinkedHashMap<SampleKey, int[]>();

		int wrPos = 0;
		int rdPos = 0;
		for (SampleKey key : sampleSetMap1.keySet()) {
			int[] position = new int[] {1, rdPos, wrPos}; // rdMatrixNb, rdPos, wrPos
			resultMap.put(key, position);
			wrPos++;
			rdPos++;
		}

		rdPos = 0;
		for (SampleKey key : sampleSetMap2.keySet()) {
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

	protected double[] checkForMismatches(MatrixKey wrMatrixKey) throws IOException, InvalidRangeException {
		double[] result = new double[2];

		wrMatrixMetadata = MatricesList.getMatrixMetadataById(wrMatrixKey);
		wrSampleSet = new SampleSet(wrMatrixKey);
		wrMarkerSet = new MarkerSet(wrMatrixKey);
		wrMarkerSet.initFullMarkerIdSetMap();
		Map<SampleKey, byte[]> wrSampleSetMap = wrSampleSet.getSampleIdSetMapByteArray();

		NetcdfFile rdNcFile = NetcdfFile.open(wrMatrixMetadata.getPathToMatrix());

		// Iterate through markerset, take it marker by marker
		int markerNb = 0;
		double mismatchCount = 0;

		// Iterate through markerSet
		for (MarkerKey markerKey : wrMarkerSet.getMarkerKeys()) {
			Map<Byte, Integer> knownAlleles = new LinkedHashMap<Byte, Integer>();

			// Get a sampleset-full of GTs
			wrSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, wrSampleSetMap, markerNb);

			// Iterate through sampleSet
			for (byte[] tempGT : wrSampleSetMap.values()) {
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

		double mismatchRatio = mismatchCount / wrSampleSet.getSampleSetSize();
		result[0] = mismatchCount;
		result[1] = mismatchRatio;

		return result;
	}
}

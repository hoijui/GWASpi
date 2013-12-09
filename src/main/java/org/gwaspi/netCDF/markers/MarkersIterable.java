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
package org.gwaspi.netCDF.markers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import org.gwaspi.samples.SampleSet;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

/**
 * TODO
 */
public class MarkersIterable implements
		Iterable<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>>
{
	/**
	 * Basically, this is a delayed loader of a list of elements
	 * to be excluded.
	 * @param VT the type of value that is to be excluded or not
	 */
	public interface Excluder<VT> {

		void init() throws IOException;

		int getTotalExcluded();

		boolean isExcluded(VT object);
	}

	public static class HWExcluder implements Excluder<MarkerKey> {

		private final OperationKey hwOperationKey;
		private final double hwThreshold;
		private Collection<MarkerKey> excludeMarkers;

		public HWExcluder(OperationKey hwOperationKey, double hwThreshold) {

			this.hwOperationKey = hwOperationKey;
			this.hwThreshold = hwThreshold;
			this.excludeMarkers = null;
		}

		@Override
		public void init() throws IOException {

			Collection<MarkerKey> toBeExcluded = new HashSet<MarkerKey>();

			OperationMetadata hwOP = OperationsList.getOperation(hwOperationKey);

			if (hwOP == null) {
				throw new IllegalArgumentException(
						"Hardy-Weinberg operation does not exist: "
						+ hwOperationKey.toString());
			}

			NetcdfFile rdHWNcFile = NetcdfFile.open(OperationMetadata.generatePathToNetCdfFile(hwOP).getAbsolutePath());
			MarkerOperationSet rdHWOperationSet = new MarkerOperationSet(OperationKey.valueOf(hwOP));
			Map<MarkerKey, Double> rdHWMarkers = rdHWOperationSet.getOpSetMap();

			// EXCLUDE MARKER BY HARDY WEINBERG THRESHOLD
			rdHWMarkers = rdHWOperationSet.fillOpSetMapWithVariable(rdHWNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
			for (Map.Entry<MarkerKey, Double> entry : rdHWMarkers.entrySet()) {
				double value = entry.getValue();
				if (value < hwThreshold) {
					toBeExcluded.add(entry.getKey());
				}
			}
			rdHWNcFile.close();

			excludeMarkers = toBeExcluded;
		}

		@Override
		public int getTotalExcluded() {
			return excludeMarkers.size();
		}

		@Override
		public boolean isExcluded(MarkerKey object) {
			return excludeMarkers.contains(object);
		}

	}

	private final MatrixKey matrixKey;
	private final Excluder<MarkerKey> excluder;
	private final SampleSet sampleSet;
	private final List<MarkerKey> markerKeys;
	private final Set<SampleKey> sampleKeys;
	private final Map<SampleKey, SampleInfo> sampleInfos;

	/**
	 * Allows to iterate over the unfiltered MarkerKeys of a matrix.
	 * @param matrixKey
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public MarkersIterable(MatrixKey matrixKey, Excluder<MarkerKey> excluder) throws IOException {

		this.matrixKey = matrixKey;
		this.excluder = excluder;

		if (excluder != null) {
			excluder.init();
		}

		MarkerSet rdMarkerSet = new MarkerSet(matrixKey);
		rdMarkerSet.initFullMarkerIdSetMap();
//		rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(readStudyId);
//		rdMarkerSet.fillWith(cNetCDF.Defaults.DEFAULT_GT);
//
//		Map<MarkerKey, byte[]> wrMarkerSetMap = new LinkedHashMap<MarkerKey, byte[]>();
//		wrMarkerSetMap.putAll(rdMarkerSet.getMarkerIdSetMapByteArray());

		this.sampleSet = new SampleSet(matrixKey);
//			samples = sampleSet.getSampleIdSetMapByteArray();
		this.sampleKeys = sampleSet.getSampleKeys();
		// This one has to be ordered! (and it is, due to the map being a LinkedHashMap)
		this.markerKeys = new ArrayList<MarkerKey>(rdMarkerSet.getMarkerIdSetMapInteger().keySet());

		this.sampleInfos = retrieveSampleInfos();
	}

	public MarkersIterable(MatrixKey matrixKey) throws IOException, InvalidRangeException {
		this(matrixKey, null);
	}

	private Map<SampleKey, SampleInfo> retrieveSampleInfos() throws IOException {

		// This one has to be ordered! (and it is, due to the map being a LinkedHashMap)
		Set<SampleKey> sampleKeysOrdered = sampleSet.getSampleIdSetMapByteArray().keySet();

		List<SampleInfo> allSampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(matrixKey.getStudyKey());
		Map<SampleKey, SampleInfo> sampleInfosUnordered = new LinkedHashMap<SampleKey, SampleInfo>(allSampleInfos.size());
		for (SampleInfo sampleInfo : allSampleInfos) {
			sampleInfosUnordered.put(sampleInfo.getKey(), sampleInfo);
		}

		// we use LinkedHashMap for retainig the order of input
		Map<SampleKey, SampleInfo> localSampleInfos = new LinkedHashMap<SampleKey, SampleInfo>(sampleInfosUnordered.size());
		for (SampleKey sampleKey : sampleKeysOrdered) {
			localSampleInfos.put(sampleKey, sampleInfosUnordered.get(sampleKey));
		}

		return localSampleInfos;
	}

	MatrixKey getMatrixKey() {
		return matrixKey;
	}

	Excluder<MarkerKey> getExcluder() {
		return excluder;
	}

	SampleSet getSampleSet() {
		return sampleSet;
	}

	Set<SampleKey> getSampleKeys() {
		return sampleKeys;
	}

	public Map<SampleKey, SampleInfo> getSampleInfos() {
		return sampleInfos;
	}

	public List<MarkerKey> getMarkerKeys() {
		return markerKeys;
	}

	@Override
	public Iterator<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>> iterator() {
		try {
			return new MarkersIterator(this);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (InvalidRangeException ex) {
			throw new RuntimeException(ex);
		}
	}
}

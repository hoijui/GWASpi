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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.samples.SampleSet;
import ucar.ma2.InvalidRangeException;

/**
 * TODO
 */
public class MarkersIterable implements
		Iterable<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>>
{
	private static class Excluder {
	}

	private final MatrixKey matrixKey;
	private final Collection<SampleKey> excluder;
	private final SampleSet sampleSet;
	private final List<MarkerKey> markerKeys;
	private Set<SampleKey> sampleKeys;
	private int nextMarker;
	private Map<SampleKey, SampleInfo> sampleInfos;

	/**
	 * Allows to iterate over the unfiltered MarkerKeys of a matrix.
	 * @param matrixKey
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public MarkersIterable(MatrixKey matrixKey) throws IOException, InvalidRangeException {

		this.matrixKey = matrixKey;
		this.excluder = null;

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

		this.nextMarker = 0;
		this.sampleInfos = retrieveSampleInfos();
	}

	public MarkersIterable(OperationKey hardyWeinbergOk) throws IOException, InvalidRangeException {

		this.matrixKey = hardyWeinbergOk.getParentMatrixKey();

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

		this.nextMarker = 0;
		this.sampleInfos = retrieveSampleInfos();
	}

	private Map<SampleKey, SampleInfo> retrieveSampleInfos() throws IOException, InvalidRangeException {

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
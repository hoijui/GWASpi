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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.global.Text;
import org.gwaspi.model.Census;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.filter.SimpleOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;
import org.gwaspi.operations.markercensus.MarkerCensusOperationDataSet;
import org.gwaspi.operations.trendtest.AbstractNetCdfTestOperationDataSet;
import org.gwaspi.operations.trendtest.CommonTestOperationDataSet;
import org.gwaspi.operations.trendtest.TrendTestOperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTestMatrixOperation<DST extends CommonTestOperationDataSet, PT extends TrendTestOperationParams> extends AbstractOperation<DST, PT> {

	private final Logger log
			= LoggerFactory.getLogger(AbstractTestMatrixOperation.class);

	public AbstractTestMatrixOperation(PT params) {
		super(params);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	@Override
	public int processMatrix() throws IOException {

		int resultOpId = OperationKey.NULL_ID;

		SimpleOperationDataSet filteredOperationDataSet
				= (SimpleOperationDataSet) OperationManager.generateOperationDataSet(getParams().getParent().getOperationParent());

		// CHECK IF THERE IS ANY DATA LEFT TO PROCESS AFTER PICKING
		if (!filteredOperationDataSet.isDataLeft()) {
			log.warn(Text.Operation.warnNoDataLeftAfterPicking);
			return resultOpId;
		}

		MarkerCensusOperationDataSet markerCensusOperationDataSet
				= (MarkerCensusOperationDataSet) OperationManager.generateOperationDataSet(getParams().getMarkerCensus());

		AbstractNetCdfTestOperationDataSet dataSet = (AbstractNetCdfTestOperationDataSet) generateFreshOperationDataSet();

		dataSet.setNumMarkers(filteredOperationDataSet.getNumMarkers());
		dataSet.setNumSamples(filteredOperationDataSet.getNumSamples());
		dataSet.setNumChromosomes(filteredOperationDataSet.getNumChromosomes());

		dataSet.setMarkerCensusOPKey(getParams().getMarkerCensus()); // HACK
		dataSet.setTestType(getParams().getType()); // HACK
		dataSet.setTestName(getParams().getName()); // HACK

		Map<Integer, MarkerKey> censusOpMarkers = markerCensusOperationDataSet.getMarkersKeysSource().getIndicesMap();
		Map<Integer, MarkerKey> wrMarkerKeysFiltered = filteredOperationDataSet.getMarkersKeysSource().getIndicesMap();

		Iterator<Census> wrCaseMarkerCensusesIt = markerCensusOperationDataSet.getCensus(Category.CASE).iterator();
		Iterator<Census> wrCtrlMarkerCensusesIt = markerCensusOperationDataSet.getCensus(Category.CONTROL).iterator();
		List<Census> wrCaseMarkerCensusesFiltered = new ArrayList<Census>(wrMarkerKeysFiltered.size());
		List<Census> wrCtrlMarkerCensusesFiltered = new ArrayList<Census>(wrMarkerKeysFiltered.size());
		for (Integer allOrigIndex : censusOpMarkers.keySet()) {
			final Census markerCensusCase = wrCaseMarkerCensusesIt.next();
			final Census markerCensusCtrl = wrCtrlMarkerCensusesIt.next();
			if (wrMarkerKeysFiltered.containsKey(allOrigIndex)) {
				wrCaseMarkerCensusesFiltered.add(markerCensusCase);
				wrCtrlMarkerCensusesFiltered.add(markerCensusCtrl);
			}
		}

		org.gwaspi.global.Utils.sysoutStart(getParams().getName());
		performTest(
				dataSet,
				wrMarkerKeysFiltered,
				wrCaseMarkerCensusesFiltered,
				wrCtrlMarkerCensusesFiltered);
		org.gwaspi.global.Utils.sysoutCompleted(getParams().getName());

		dataSet.finnishWriting();
		resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK

		return resultOpId;
	}

	private static <K, V> Map<K, V> filter(Map<K, V> toBeFiltered, Collection<K> toBeExcluded) {

		Map<K, V> filtered = new LinkedHashMap<K, V>();
		if (toBeFiltered != null) {
			for (Map.Entry<K, V> entry : toBeFiltered.entrySet()) {
				K key = entry.getKey();

				if (!toBeExcluded.contains(key)) {
					filtered.put(key, entry.getValue());
				}
			}
		}

		return filtered;
	}

	private static <K, V> Map<K, V> filterByValues(Map<K, V> toBeFiltered, Collection<V> toBeExcluded) {

		Map<K, V> filtered = new LinkedHashMap<K, V>();
		if (toBeFiltered != null) {
			for (Map.Entry<K, V> entry : toBeFiltered.entrySet()) {
				V value = entry.getValue();

				if (!toBeExcluded.contains(value)) {
					filtered.put(entry.getKey(), value);
				}
			}
		}

		return filtered;
	}

	/**
	 * EXCLUDE MARKER BY HARDY WEINBERG THRESHOLD.
	 * @param hwOPKey
	 * @param hwPValueThreshold
	 * @param excludeMarkerSetMap
	 * @return
	 * @throws IOException
	 */
	public static boolean excludeMarkersByHW(OperationKey hwOPKey, double hwPValueThreshold, Collection<MarkerKey> excludeMarkerSetMap) throws IOException {

		excludeMarkerSetMap.clear();
		int totalMarkerNb = 0;

		if (hwOPKey != null) {
			HardyWeinbergOperationDataSet hardyWeinbergOperationDataSet = (HardyWeinbergOperationDataSet) OperationManager.generateOperationDataSet(hwOPKey);

			Collection<HardyWeinbergOperationEntry> hwEntriesControl = hardyWeinbergOperationDataSet.getEntriesControl();
			totalMarkerNb = hwEntriesControl.size();
			for (HardyWeinbergOperationEntry hardyWeinbergOperationEntry : hwEntriesControl) {
				double pValue = hardyWeinbergOperationEntry.getP();
				if (pValue < hwPValueThreshold) {
					excludeMarkerSetMap.add(hardyWeinbergOperationEntry.getKey());
				}
			}
		}

		return (excludeMarkerSetMap.size() < totalMarkerNb);
	}

	/**
	 * Performs the actual Test.
	 */
	protected abstract void performTest(
			OperationDataSet dataSet,
			Map<Integer, MarkerKey> markerOrigIndicesKeys,
			List<Census> caseMarkersCensus,
			List<Census> ctrlMarkersCensus)
			throws IOException;
}

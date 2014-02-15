/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.operations.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry;

public class ByHardyWeinbergThresholdFilterOperation extends AbstractFilterOperation<ByHardyWeinbergThresholdFilterOperationParams> {

	public ByHardyWeinbergThresholdFilterOperation(ByHardyWeinbergThresholdFilterOperationParams params) {
		super(params);
	}

	@Override
	public OPType getType() {
		return OPType.FILTER_BY_HW_THREASHOLD;
	}

	@Override
	protected void filter(
			Map<Integer, MarkerKey> filteredMarkerOrigIndicesAndKeys,
//			Map<Integer, ChromosomeKey> filteredChromosomeOrigIndicesAndKeys,
			Map<Integer, SampleKey> filteredSampleOrigIndicesAndKeys)
			throws IOException
	{
		DataSetSource parentDataSetSource = getParentDataSetSource();

		final OperationKey hwOpKey = getParams().getHardyWeinbergOperationKey();
		final double hwPValueThreshold = getParams().getHardyWeinbergPValueThreshold();

		HardyWeinbergOperationDataSet hardyWeinbergOperationDataSet
				= (HardyWeinbergOperationDataSet) OperationFactory.generateOperationDataSet(hwOpKey);

		Map<Integer, MarkerKey> parentMarkersOrigIndicesAndKeys = parentDataSetSource.getMarkersKeysSource().getIndicesMap();
		List<HardyWeinbergOperationEntry> hwEntriesControl = hardyWeinbergOperationDataSet.getEntriesControl();
		Iterator<HardyWeinbergOperationEntry> hwEntriesControlIt = hwEntriesControl.iterator();
		HardyWeinbergOperationEntry curHardyWeinbergOperationEntry
				= hwEntriesControlIt.next();
		for (Map.Entry<Integer, MarkerKey> parentMarkersEntry : parentMarkersOrigIndicesAndKeys.entrySet()) {
			if (curHardyWeinbergOperationEntry.getIndex() == parentMarkersEntry.getKey()) {
				final double pValue = curHardyWeinbergOperationEntry.getP();
				if (pValue >= hwPValueThreshold) {
					filteredMarkerOrigIndicesAndKeys.put(
							parentMarkersEntry.getKey(),
							parentMarkersEntry.getValue());
				}
				curHardyWeinbergOperationEntry = hwEntriesControlIt.next();
			} else {
				// This marker is not a control entry, thus include it no matter what.
				filteredMarkerOrigIndicesAndKeys.put(
						parentMarkersEntry.getKey(),
						parentMarkersEntry.getValue());
			}
		}

		// we use all samples from the parent
		filteredSampleOrigIndicesAndKeys.putAll(parentDataSetSource.getSamplesKeysSource().getIndicesMap());
	}

	@Override
	protected String getFilterDescription() {
		return "Removes all markers that have a Hardy & Weinberg P-value smaller then a given threshold.";
	}
}

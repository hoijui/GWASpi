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

package org.gwaspi.operations.combi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.filter.AbstractFilterOperation;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry;

public class ByCombiWeightsFilterOperation extends AbstractFilterOperation<ByCombiWeightsFilterOperationParams> {

	public ByCombiWeightsFilterOperation(ByCombiWeightsFilterOperationParams params) {
		super(params);
	}

	@Override
	public OPType getType() {
		return OPType.FILTER_BY_HW_THREASHOLD;
	}

	@Override
	protected void filter(
			Map<Integer, MarkerKey> filteredMarkerOrigIndicesAndKeys,
			Map<Integer, SampleKey> filteredSampleOrigIndicesAndKeys)
			throws IOException
	{
		final int markersToKeep = getParams().getMarkersToKeep();

		CombiTestOperationDataSet combiTestOperationDataSet
				= (CombiTestOperationDataSet) getParentDataSetSource();

		Map<Integer, MarkerKey> parentMarkersOrigIndicesAndKeys = combiTestOperationDataSet.getMarkersKeysSource().getIndicesMap();
		final List<Double> combiWeights = combiTestOperationDataSet.getWeights();
		final List<Double> combiWeightsSorted = new ArrayList<Double>(combiWeights);
		Collections.sort(combiWeightsSorted);
		final double thresholdWeight = combiWeightsSorted.get(markersToKeep);

		Iterator<Double> combiWeightsIt = combiWeights.iterator();
		for (Map.Entry<Integer, MarkerKey> parentMarkersEntry : parentMarkersOrigIndicesAndKeys.entrySet()) {
			final double curCombiWeight = combiWeightsIt.next();
			if (curCombiWeight > thresholdWeight) {
					filteredMarkerOrigIndicesAndKeys.put(
							parentMarkersEntry.getKey(),
							parentMarkersEntry.getValue());
			}
		}

		// we use all samples from the parent
		filteredSampleOrigIndicesAndKeys.putAll(combiTestOperationDataSet.getSamplesKeysSource().getIndicesMap()); // XXX could be done more efficiently, without loading all the keys first, but by just signaling to the method caller, that we use all samples somehow
	}

	@Override
	protected String getFilterDescription() {
		return "Removes all markers that have a Hardy & Weinberg P-value smaller then a given threshold.";
	}
}

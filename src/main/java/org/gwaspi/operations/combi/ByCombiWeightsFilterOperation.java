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
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.operations.DefaultOperationTypeInfo;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.netCDF.operations.OperationTypeInfo;
import org.gwaspi.operations.filter.AbstractFilterOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByCombiWeightsFilterOperation extends AbstractFilterOperation<ByCombiWeightsFilterOperationParams> {

	private static final Logger LOG
			= LoggerFactory.getLogger(ByCombiWeightsFilterOperation.class);

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Filter markers by COMBI weights threshold",
					"Filter markers by COMBI weights threshold"); // TODO We need a more elaborate description of this operation!
	static {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationTypeInfo(
				ByCombiWeightsFilterOperation.class,
				OPERATION_TYPE_INFO);
	}

	private static final int WEIGHTS_MOVING_AVERAGE_FILTER_NORM = 2;

	public ByCombiWeightsFilterOperation(ByCombiWeightsFilterOperationParams params) {
		super(params);
	}

	@Override
	public OPType getType() {
		return OPType.FILTER_BY_WEIGHTS;
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
		final List<Double> rawWeights = combiTestOperationDataSet.getWeights();

		LOG.info("apply moving average filter (p-norm filter) on the weights");
		List<Double> weightsFiltered = applyMovingAverageFilter(rawWeights, getParams().getWeightsFilterWidth());

		if (CombiTestMatrixOperation.spy != null) {
			CombiTestMatrixOperation.spy.smoothedWeightsCalculated(weightsFiltered);
		}
		LOG.debug("filtered weights: " + weightsFiltered);

		List<Double> weightsAbsolute = new ArrayList<Double>(weightsFiltered.size());
		for (Double wFiltered : weightsFiltered) {
			weightsAbsolute.add(Math.abs(wFiltered));
		}

		final List<Double> combiWeightsSorted = new ArrayList<Double>(weightsAbsolute);
		Collections.sort(combiWeightsSorted);
		final double thresholdWeight = combiWeightsSorted.get(markersToKeep);

		Iterator<Double> combiWeightsIt = rawWeights.iterator();
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

	/**
	 * Apply a moving average filter (p-norm filter).
	 * Basically "smoothes out the landscape".
	 * @param weights
	 * @param filterWidth
	 * @return filtered weights
	 */
	private static List<Double> applyMovingAverageFilter(final List<Double> weights, final int filterWidth) {

		List<Double> weightsFiltered = new ArrayList(weights);
		Util.pNormFilter(weightsFiltered, filterWidth, WEIGHTS_MOVING_AVERAGE_FILTER_NORM);

		return weightsFiltered;
	}

	@Override
	protected String getFilterDescription() {
		return "Removes all markers that have a COMBI weight smaller then a given threshold.";
	}
}

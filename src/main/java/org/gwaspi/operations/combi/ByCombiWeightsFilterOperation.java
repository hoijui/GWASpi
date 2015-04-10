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
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.filter.AbstractFilterOperation;
import org.gwaspi.operations.filter.SimpleFilterOperationMetadataFactory;
import org.gwaspi.operations.filter.SimpleOperationFactory;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByCombiWeightsFilterOperation extends AbstractFilterOperation<ByCombiWeightsFilterOperationParams> {

	private static final Logger LOG
			= LoggerFactory.getLogger(ByCombiWeightsFilterOperation.class);

	private static final ProcessInfo byCombiWeightsFilterPI = new DefaultProcessInfo(
			"Filter markers by COMBI weights threshold",
			""); // TODO

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Filter markers by COMBI weights threshold",
					"Filter markers by COMBI weights threshold", // TODO We need a more elaborate description of this operation!
					OPType.FILTER_BY_WEIGHTS,
					true,
					false);
	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new SimpleOperationFactory(
				ByCombiWeightsFilterOperation.class,
				new SimpleFilterOperationMetadataFactory<ByCombiWeightsFilterOperationParams>(
						OPERATION_TYPE_INFO,
						"Removes all markers that have a COMBI weight smaller then a given threshold.")));
	}

	private static final int WEIGHTS_MOVING_AVERAGE_FILTER_NORM = 2;

	private ProgressHandler filterPH;

	public ByCombiWeightsFilterOperation(ByCombiWeightsFilterOperationParams params) {
		super(params);

		this.filterPH = null;
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return OPERATION_TYPE_INFO;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return byCombiWeightsFilterPI;
	}

	@Override
	protected ProgressSource getFilteringProgressSource() throws IOException {

		if (filterPH == null) {
			final ProcessInfo filterPI = new SubProcessInfo(
					getProcessInfo(),
					getParams().getName() + " filtering",
					null);
//			final int numItems = getNumItems();
//			filterPH = new IntegerProgressHandler(filterPI, 0, numItems - 1);
			// NOTE We use this instead of the above, because filtering is so fast,
			//   that possible per part UI updates would slow it down by huge factors.
			filterPH = new IndeterminateProgressHandler(filterPI);
		}

		return filterPH;
	}

	@Override
	protected void filter(
			Map<Integer, MarkerKey> filteredMarkerOrigIndicesAndKeys,
			Map<Integer, SampleKey> filteredSampleOrigIndicesAndKeys)
			throws IOException
	{
		filterPH.setNewStatus(ProcessStatus.INITIALIZING);
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
//		LOG.debug("filtered weights: " + weightsFiltered); // this is way too verbose

		List<Double> weightsAbsolute = new ArrayList<Double>(weightsFiltered.size());
		for (Double wFiltered : weightsFiltered) {
			weightsAbsolute.add(Math.abs(wFiltered));
		}

		final List<Double> combiWeightsSorted = new ArrayList<Double>(weightsAbsolute);
		// sorts in ascending order -> biggest values are at the end
		Collections.sort(combiWeightsSorted);
		// use the n'th biggest value as threshold
		final double thresholdWeight = combiWeightsSorted.get(Math.max(0, combiWeightsSorted.size() - markersToKeep - 1));

		Iterator<Double> combiWeightsIt = weightsAbsolute.iterator();
		filterPH.setNewStatus(ProcessStatus.RUNNING);
		for (Map.Entry<Integer, MarkerKey> parentMarkersEntry : parentMarkersOrigIndicesAndKeys.entrySet()) {
			final double curCombiWeight = combiWeightsIt.next();
			if (curCombiWeight > thresholdWeight) { // XXX; do not filter out, just set P value to 1! -> requries to create a new list of P values... where do we get the old ones from?
					filteredMarkerOrigIndicesAndKeys.put(
							parentMarkersEntry.getKey(),
							parentMarkersEntry.getValue());
			}
			// NOTE We omit progress reporting, because it would be too huge
			//   a percentual performance penalty for this leight-weight operation.
//			filterPH.setProgress(mi);
		}
		// NOTE Here we would report 100% compleetion (see also the note above),
		//   but this would create insufficient completion-fraction values
		//   in the parent SuperProgressSource.
//		filterPH.setProgress(0);
		filterPH.setNewStatus(ProcessStatus.FINALIZING);

		// we use all samples from the parent
		filteredSampleOrigIndicesAndKeys.putAll(combiTestOperationDataSet.getSamplesKeysSource().getIndicesMap()); // XXX could be done more efficiently, without loading all the keys first, but by just signaling to the method caller, that we use all samples somehow
		filterPH.setNewStatus(ProcessStatus.COMPLEETED);
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
}

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.datasource.filter.IndicesFilteredList;
import org.gwaspi.global.Text;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.matrices.ChromosomeUtils;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.AbstractOperationCreatingOperation;
import org.gwaspi.operations.OperationParams;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFilterOperation<P extends OperationParams>
		extends AbstractOperationCreatingOperation<SimpleOperationDataSet, P>
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractFilterOperation.class);

	private ProgressHandler storePH;
	private SuperProgressSource progressSource;

	protected AbstractFilterOperation(P params) {
		super(params);
	}

	private SuperProgressSource getSuperProgressHandler() throws IOException {

		if (progressSource == null) {
//			final int numItems = getNumItems();
			final ProgressSource filterPS = getFilteringProgressSource();
//			storePH = new IntegerProgressHandler(
//					new SubProcessInfo(PROCESS_INFO, getParams().getName() + " storing", null),
//					0, numItems - 1);
			storePH = new IndeterminateProgressHandler(
					new SubProcessInfo(getProcessInfo(), getParams().getName() + " storing", null));

			final Map<ProgressSource, Double> subProgressSourcesAndWeights;
			final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
					= new LinkedHashMap<ProgressSource, Double>(2);
			tmpSubProgressSourcesAndWeights.put(filterPS, 0.5);
			tmpSubProgressSourcesAndWeights.put(storePH, 0.5);
			subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);

			progressSource = new SuperProgressSource(getProcessInfo(), subProgressSourcesAndWeights);
		}

		return progressSource;
	}

	@Override
	public ProgressSource getProgressSource() throws IOException {
		return getSuperProgressHandler();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	protected abstract void filter(
			Map<Integer, MarkerKey> filteredMarkerOrigIndicesAndKeys,
//			Map<Integer, ChromosomeKey> filteredChromosomeOrigIndicesAndKeys,
			Map<Integer, SampleKey> filteredSampleOrigIndicesAndKeys)
			throws IOException;

	protected abstract ProgressSource getFilteringProgressSource() throws IOException;

	@Override
	public int processMatrix() throws IOException {

		final SuperProgressSource progressHandler = getSuperProgressHandler();
		progressHandler.setNewStatus(ProcessStatus.INITIALIZING);

		final DataSetSource parentDataSetSource = getParentDataSetSource();
		Map<Integer, MarkerKey> filteredMarkerOrigIndicesAndKeys
				= new LinkedHashMap<Integer, MarkerKey>(parentDataSetSource.getNumMarkers());
//		Map<Integer, ChromosomeKey> filteredChromosomeOrigIndicesAndKeys
//				= new LinkedHashMap<Integer, ChromosomeKey>(parentDataSetSource.getNumChromosomes());
		Map<Integer, SampleKey> filteredSampleOrigIndicesAndKeys
				= new LinkedHashMap<Integer, SampleKey>(parentDataSetSource.getNumSamples());

		progressHandler.setNewStatus(ProcessStatus.RUNNING);
		filter(
				filteredMarkerOrigIndicesAndKeys,
//				filteredChromosomeOrigIndicesAndKeys,
				filteredSampleOrigIndicesAndKeys);

		if (filteredMarkerOrigIndicesAndKeys.isEmpty()) {
			LOG.warn(Text.Operation.warnNoDataLeftAfterPicking + " (markers)");
			return Integer.MIN_VALUE;
		}
		if (filteredSampleOrigIndicesAndKeys.isEmpty()) {
			LOG.warn(Text.Operation.warnNoDataLeftAfterPicking + " (samples)");
			return Integer.MIN_VALUE;
		}

		final List<Integer> filtereMarkersOriginalIndices = new ArrayList<Integer>(filteredMarkerOrigIndicesAndKeys.keySet());
		final List<MarkerKey> filtereMarkersKeys = new ArrayList<MarkerKey>(filteredMarkerOrigIndicesAndKeys.values());

		final List<Integer> filtereSamplesOriginalIndices = new ArrayList<Integer>(filteredSampleOrigIndicesAndKeys.keySet());
		final List<SampleKey> filtereSamplesKeys = new ArrayList<SampleKey>(filteredSampleOrigIndicesAndKeys.values());

		final Map<Integer, ChromosomeKey> filteredChromosomeOrigIndicesAndKeys
					= ChromosomeUtils.aggregateChromosomeIndicesAndKeys(
							parentDataSetSource.getChromosomesKeysSource().getIndicesMap(),
							ChromosomeUtils.aggregateChromosomeKeys(new IndicesFilteredList<String>(MatrixFactory.generateMatrixDataSetSource(getParentKey().getOrigin()).getMarkersMetadatasSource().getChromosomes(), filtereMarkersOriginalIndices)));
		final List<Integer> filtereChromosomesOriginalIndices = new ArrayList<Integer>(filteredChromosomeOrigIndicesAndKeys.keySet());
		final List<ChromosomeKey> filtereChromosomesKeys = new ArrayList<ChromosomeKey>(filteredChromosomeOrigIndicesAndKeys.values());

		storePH.setNewStatus(ProcessStatus.INITIALIZING);
		SimpleOperationDataSet dataSet = generateFreshOperationDataSet();

		storePH.setNewStatus(ProcessStatus.RUNNING);

		dataSet.setNumMarkers(filteredMarkerOrigIndicesAndKeys.size());
		dataSet.setNumChromosomes(filteredChromosomeOrigIndicesAndKeys.size());
		dataSet.setNumSamples(filteredSampleOrigIndicesAndKeys.size());

		dataSet.setMarkers(filtereMarkersOriginalIndices, filtereMarkersKeys);
		dataSet.setChromosomes(filtereChromosomesOriginalIndices, filtereChromosomesKeys);
		dataSet.setSamples(filtereSamplesOriginalIndices, filtereSamplesKeys);
		storePH.setNewStatus(ProcessStatus.FINALIZING);
		progressHandler.setNewStatus(ProcessStatus.FINALIZING);

		dataSet.finnishWriting();
		storePH.setNewStatus(ProcessStatus.COMPLEETED);
		progressHandler.setNewStatus(ProcessStatus.COMPLEETED);

		return dataSet.getOperationKey().getId();
	}
}

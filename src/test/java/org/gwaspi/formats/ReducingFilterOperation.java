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

package org.gwaspi.formats;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.filter.AbstractFilterOperation;
import org.gwaspi.operations.filter.SimpleFilterOperationMetadataFactory;
import org.gwaspi.operations.filter.SimpleOperationFactory;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;

public class ReducingFilterOperation extends AbstractFilterOperation<ReducingFilterOperationParams> {

	private static final ProcessInfo processInfo = new DefaultProcessInfo(
			"Reduce #samples filter",
			"Reduce #samples filter"); // TODO We need a more elaborate description of this operation!); // TODO We need a more elaborate description of this operation!

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Reduce #samples filter",
					"Reduce #samples filter", // TODO We need a more elaborate description of this operation!
					OPType.FILTER_FRACTION,
					false,
					true);
	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new SimpleOperationFactory<ReducingFilterOperationParams>(
						ReducingFilterOperation.class,
						new SimpleFilterOperationMetadataFactory<ReducingFilterOperationParams>(
								OPERATION_TYPE_INFO,
								"Removes a given percentage of the samples, preserving the affected/unaffected ratio")));
	}

	private ProgressHandler filterPH;

	public ReducingFilterOperation(final ReducingFilterOperationParams params) {
		super(params);

		this.filterPH = null;
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return OPERATION_TYPE_INFO;
	}

	@Override
	protected ProgressSource getFilteringProgressSource() throws IOException {

		if (filterPH == null) {
			final int numItems = getNumItems();
			filterPH = new IntegerProgressHandler(
					new SubProcessInfo(getProcessInfo(), getParams().getName() + " filtering", null),
					0, numItems - 1);
		}

		return filterPH;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return processInfo;
	}

	@Override
	protected void filter(
			final Map<Integer, MarkerKey> filteredMarkerOrigIndicesAndKeys,
			final Map<Integer, SampleKey> filteredSampleOrigIndicesAndKeys)
			throws IOException
	{
		filterPH.setNewStatus(ProcessStatus.INITIALIZING);
		final DataSetSource parentDataSetSource = getParentDataSetSource();

		// we use all markers from the parent
		filteredMarkerOrigIndicesAndKeys.putAll(parentDataSetSource.getMarkersKeysSource().getIndicesMap());

		// ... but only a given percentage of samples
		final SamplesInfosSource samplesInfosSource = parentDataSetSource.getSamplesInfosSource();
		final int numSamples = samplesInfosSource.size();
		final int numSamplesAffected
				= Collections.frequency(samplesInfosSource.getAffections(), Affection.AFFECTED);
		final int numSamplesUnaffected
				= Collections.frequency(samplesInfosSource.getAffections(), Affection.UNAFFECTED);
		final int numSamplesFilteredAffected = (int) (numSamplesAffected * getParams().getRemainingFraction());
		final int numSamplesFilteredUnaffected = (int) (numSamplesUnaffected * getParams().getRemainingFraction());
		final Map<Integer, SampleKey> filteredSamplesAffected = new HashMap<Integer, SampleKey>();
		final Map<Integer, SampleKey> filteredSamplesUnaffected = new HashMap<Integer, SampleKey>();

		final Iterator<Map.Entry<Integer, SampleKey>> sampleKeysIt
				= parentDataSetSource.getSamplesKeysSource().getIndicesMap().entrySet().iterator();
		// XXX We simply take the first samples. maybe we should randomize it?
		filterPH.setNewStatus(ProcessStatus.RUNNING);
		int localSampleIndex = 0;
		for (final SampleInfo sampleInfo : samplesInfosSource) {
			final Map.Entry<Integer, SampleKey> sampleKey = sampleKeysIt.next();
			final Affection affection = sampleInfo.getAffection();
			if (affection == Affection.AFFECTED) {
				if (filteredSamplesAffected.size() < numSamplesFilteredAffected) {
					filteredSamplesAffected.put(sampleKey.getKey(), sampleKey.getValue());
					filteredSampleOrigIndicesAndKeys.put(sampleKey.getKey(), sampleKey.getValue());
				}
			} else if (affection == Affection.UNAFFECTED) {
				if (filteredSamplesUnaffected.size() < numSamplesFilteredUnaffected) {
					filteredSamplesUnaffected.put(sampleKey.getKey(), sampleKey.getValue());
					filteredSampleOrigIndicesAndKeys.put(sampleKey.getKey(), sampleKey.getValue());
				}
			}
			filterPH.setProgress(localSampleIndex);
			localSampleIndex++;
		}
		filterPH.setNewStatus(ProcessStatus.COMPLEETED);
	}
}

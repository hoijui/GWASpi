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
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;

public class ByValidAffectionFilterOperation extends AbstractFilterOperation<ByValidAffectionFilterOperationParams> {

	private static final ProcessInfo processInfo = new DefaultProcessInfo(
			"Filter samples by valid affection",
			"Filter samples by valid affection"); // TODO We need a more elaborate description of this operation!); // TODO We need a more elaborate description of this operation!

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Filter samples by valid affection",
					"Filter samples by valid affection", // TODO We need a more elaborate description of this operation!
					OPType.FILTER_BY_VALID_AFFECTION,
					false,
					true);
	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(
				new SimpleOperationFactory<ByValidAffectionFilterOperationParams>(
						ByValidAffectionFilterOperation.class,
						new SimpleFilterOperationMetadataFactory(
								OPERATION_TYPE_INFO,
								"Removes all samples that are invalid, which means, they are neither marked as affeted nor as unaffected")));
	}

	private ProgressHandler filterPH;

	public ByValidAffectionFilterOperation(ByValidAffectionFilterOperationParams params) {
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
			Map<Integer, MarkerKey> filteredMarkerOrigIndicesAndKeys,
			Map<Integer, SampleKey> filteredSampleOrigIndicesAndKeys)
			throws IOException
	{
		filterPH.setNewStatus(ProcessStatus.INITIALIZING);
		DataSetSource parentDataSetSource = getParentDataSetSource();

		// we use all markers from the parent
		filteredMarkerOrigIndicesAndKeys.putAll(parentDataSetSource.getMarkersKeysSource().getIndicesMap());

		// ... but only the samples with a valid affection
		SamplesKeysSource samplesKeysSource = parentDataSetSource.getSamplesKeysSource();
		List<Affection> sampleAffections = parentDataSetSource.getSamplesInfosSource().getAffections();
		Iterator<Map.Entry<Integer, SampleKey>> samplesIt
				= samplesKeysSource.getIndicesMap().entrySet().iterator();
		filterPH.setNewStatus(ProcessStatus.RUNNING);
		int localSampleIndex = 0;
		for (Affection sampleAffection : sampleAffections) {
			Map.Entry<Integer, SampleKey> sample = samplesIt.next();
			if (Affection.isValid(sampleAffection)) {
				filteredSampleOrigIndicesAndKeys.put(sample.getKey(), sample.getValue());
			}
			filterPH.setProgress(localSampleIndex);
			localSampleIndex++;
		}
		filterPH.setNewStatus(ProcessStatus.COMPLEETED);
	}
}

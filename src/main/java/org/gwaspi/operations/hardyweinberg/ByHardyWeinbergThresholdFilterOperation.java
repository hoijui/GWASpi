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

package org.gwaspi.operations.hardyweinberg;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.filter.AbstractFilterOperation;
import org.gwaspi.operations.filter.SimpleOperationFactory;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;

public class ByHardyWeinbergThresholdFilterOperation extends AbstractFilterOperation<ByHardyWeinbergThresholdFilterOperationParams> {

	public static final ProgressSource PLACEHOLDER_PS_HW_TF = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_HW_TF", null));

	private static final ProcessInfo processInfo = new DefaultProcessInfo(
			"Filter by Hardy&Weinberg threshold",
			"Filter by Hardy&Weinberg threshold"); // TODO We need a more elaborate description of this operation!

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Filter by Hardy&Weinberg threshold",
					"Filter by Hardy&Weinberg threshold", // TODO We need a more elaborate description of this operation!
					OPType.FILTER_BY_HW_THREASHOLD,
					true,
					false);
	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new SimpleOperationFactory(
				ByHardyWeinbergThresholdFilterOperation.class, OPERATION_TYPE_INFO));
	}

	private ProgressHandler filterPH;

	public ByHardyWeinbergThresholdFilterOperation(ByHardyWeinbergThresholdFilterOperationParams params) {
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
//			Map<Integer, ChromosomeKey> filteredChromosomeOrigIndicesAndKeys,
			Map<Integer, SampleKey> filteredSampleOrigIndicesAndKeys)
			throws IOException
	{
		filterPH.setNewStatus(ProcessStatus.INITIALIZING);
		DataSetSource parentDataSetSource = getParentDataSetSource();

		final OperationKey hwOpKey = getParams().getHardyWeinbergOperationKey();
		final double hwPValueThreshold = getParams().getHardyWeinbergPValueThreshold();

		HardyWeinbergOperationDataSet hardyWeinbergOperationDataSet
				= (HardyWeinbergOperationDataSet) OperationManager.generateOperationDataSet(hwOpKey);

		Map<Integer, MarkerKey> parentMarkersOrigIndicesAndKeys = parentDataSetSource.getMarkersKeysSource().getIndicesMap();
		List<HardyWeinbergOperationEntry> hwEntriesControl = hardyWeinbergOperationDataSet.getEntriesControl();
		Iterator<HardyWeinbergOperationEntry> hwEntriesControlIt = hwEntriesControl.iterator();
		HardyWeinbergOperationEntry curHardyWeinbergOperationEntry = null;
		int localMarkerIndex = 0;
		filterPH.setNewStatus(ProcessStatus.RUNNING);
		for (Map.Entry<Integer, MarkerKey> parentMarkersEntry : parentMarkersOrigIndicesAndKeys.entrySet()) {
			if (curHardyWeinbergOperationEntry == null) {
				curHardyWeinbergOperationEntry = hwEntriesControlIt.next();
			}

			if (curHardyWeinbergOperationEntry.getIndex() == parentMarkersEntry.getKey()) {
				final double pValue = curHardyWeinbergOperationEntry.getP();
				if (pValue >= hwPValueThreshold) {
					filteredMarkerOrigIndicesAndKeys.put(
							parentMarkersEntry.getKey(),
							parentMarkersEntry.getValue());
				}
				curHardyWeinbergOperationEntry = null;
			} else {
				// This marker is not a control entry, thus include it no matter what.
				filteredMarkerOrigIndicesAndKeys.put(
						parentMarkersEntry.getKey(),
						parentMarkersEntry.getValue());
			}
			filterPH.setProgress(localMarkerIndex);
			localMarkerIndex++;
		}
		filterPH.setNewStatus(ProcessStatus.FINALIZING);

		// we use all samples from the parent
		filteredSampleOrigIndicesAndKeys.putAll(parentDataSetSource.getSamplesKeysSource().getIndicesMap());
		filterPH.setNewStatus(ProcessStatus.COMPLEETED);
	}

	@Override
	protected String getFilterDescription() {
		return "Removes all markers that have a Hardy & Weinberg P-value smaller then a given threshold.";
	}
}

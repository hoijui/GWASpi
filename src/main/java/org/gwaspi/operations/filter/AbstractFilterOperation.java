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
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.operations.AbstractOperation;
import org.gwaspi.operations.AbstractOperationDataSet;
import org.gwaspi.operations.OperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFilterOperation<PT extends OperationParams> extends AbstractOperation<SimpleOperationDataSet, PT> {

	private static final Logger LOG
			= LoggerFactory.getLogger(AbstractFilterOperation.class);

	protected AbstractFilterOperation(PT params) {
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

	protected abstract String getFilterDescription();

	protected abstract void filter(
			Map<Integer, MarkerKey> filteredMarkerOrigIndicesAndKeys,
//			Map<Integer, ChromosomeKey> filteredChromosomeOrigIndicesAndKeys,
			Map<Integer, SampleKey> filteredSampleOrigIndicesAndKeys)
			throws IOException;

	@Override
	public int processMatrix() throws IOException {

		DataSetSource parentDataSetSource = getParentDataSetSource();
		Map<Integer, MarkerKey> filteredMarkerOrigIndicesAndKeys
				= new LinkedHashMap<Integer, MarkerKey>(parentDataSetSource.getNumMarkers());
//		Map<Integer, ChromosomeKey> filteredChromosomeOrigIndicesAndKeys
//				= new LinkedHashMap<Integer, ChromosomeKey>(parentDataSetSource.getNumChromosomes());
		Map<Integer, SampleKey> filteredSampleOrigIndicesAndKeys
				= new LinkedHashMap<Integer, SampleKey>(parentDataSetSource.getNumSamples());

		filter(
				filteredMarkerOrigIndicesAndKeys,
//				filteredChromosomeOrigIndicesAndKeys,
				filteredSampleOrigIndicesAndKeys);

		if (filteredMarkerOrigIndicesAndKeys.isEmpty()) {
			LOG.warn(Text.Operation.warnNoDataLeftAfterPicking);
			return Integer.MIN_VALUE;
		}
		if (filteredSampleOrigIndicesAndKeys.isEmpty()) {
			LOG.warn(Text.Operation.warnNoDataLeftAfterPicking);
			return Integer.MIN_VALUE;
		}

		SimpleOperationDataSet dataSet = generateFreshOperationDataSet();

		dataSet.setType(getType());
		dataSet.setFilterDescription(getFilterDescription());

		dataSet.setNumMarkers(filteredMarkerOrigIndicesAndKeys.size());
//		dataSet.setNumChromosomes(filteredChromosomeOrigIndicesAndKeys.size());
		dataSet.setNumChromosomes(filteredSampleOrigIndicesAndKeys.size());

		dataSet.setMarkers(filteredMarkerOrigIndicesAndKeys);
//		dataSet.setChromosomes(filteredChromosomeOrigIndicesAndKeys);
		dataSet.setSamples(filteredSampleOrigIndicesAndKeys);

		return ((AbstractOperationDataSet) dataSet).getOperationKey().getId(); // HACK
	}

}

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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class NetCdfSimpleOperationDataSet extends AbstractNetCdfOperationDataSet<SimpleOperationEntry> implements SimpleOperationDataSet {

	private OPType operationType;
	private String filterDescription;

	public NetCdfSimpleOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(true, origin, parent, operationKey);
	}

	public NetCdfSimpleOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
	}

	@Override
	public boolean isDataLeft() throws IOException {
		return ((getNumMarkers() > 0) && (getNumSamples() > 0));
	}

	@Override
	public void setType(OPType operationType) throws IOException {
		this.operationType = operationType;
	}

	@Override
	public void setFilterDescription(String filterDescription) throws IOException {
		this.filterDescription = filterDescription;
	}

	@Override
	protected void supplementNetCdfHandler(
			NetcdfFileWriteable ncFile,
			OperationMetadata operationMetadata,
			List<Dimension> markersSpace,
			List<Dimension> chromosomesSpace,
			List<Dimension> samplesSpace)
			throws IOException
	{
	}

	@Override
	protected OperationMetadata createOperationMetadata() throws IOException {

		return new OperationMetadata(
				getParent(), // parent data set
				"Filtering_by_" + filterDescription, // friendly name
				"Filters the markers and/or samples by " + filterDescription, // description
				operationType, // operationType
				getNumMarkers(),
				getNumSamples(),
				getNumChromosomes(),
				isMarkersOperationSet());
	}

//	@Override
//	public void setHardyWeinbergName(String hardyWeinbergName) {
//		this.hardyWeinbergName = hardyWeinbergName;
//	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<SimpleOperationEntry> writeBuffer) throws IOException {
	}

	@Override
	public List<SimpleOperationEntry> getEntries(int from, int to) throws IOException {

		Map<Integer, MarkerKey> markersKeys = getMarkersKeysSource().getIndicesMap(from, to);

		List<SimpleOperationEntry> entries
				= new ArrayList<SimpleOperationEntry>(markersKeys.size());
		for (Map.Entry<Integer, MarkerKey> origIndicesAndKey : markersKeys.entrySet()) {
			entries.add(new DefaultSimpleOperationEntry(
					origIndicesAndKey.getValue(),
					origIndicesAndKey.getKey()));
		}

		return entries;
	}
}

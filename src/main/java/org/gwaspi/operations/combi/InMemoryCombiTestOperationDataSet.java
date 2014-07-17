/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.AbstractInMemoryOperationDataSet;
import org.gwaspi.operations.AbstractOperationDataSet;

public class InMemoryCombiTestOperationDataSet extends AbstractInMemoryOperationDataSet<CombiTestOperationEntry> implements CombiTestOperationDataSet {

	private List<Double> weights;

	public InMemoryCombiTestOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(true, origin, parent, operationKey);
	}

	public InMemoryCombiTestOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
	}

	@Override
	protected OperationMetadata createOperationMetadata() throws IOException { // XXX this is a direct copy of the NetCdf* version of this class!

		DataSetKey parentDataSetKey = getParent();
		DataSetMetadata parentDataSetMetadata = MatricesList.getDataSetMetadata(parentDataSetKey);
		return new OperationMetadata(
				parentDataSetKey, // parent data set
				"COMBI_Test"/* + myFriendlyName*/, // friendly name
				"COMBI test on " + parentDataSetMetadata.getFriendlyName(), // description
				OPType.COMBI_ASSOC_TEST, // operationType
				getNumMarkers(),
				getNumSamples(),
				getNumChromosomes(),
				isMarkersOperationSet());
	}

	@Override
	public void setWeights(List<Double> weights) throws IOException {

		this.weights = weights;
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<CombiTestOperationEntry> writeBuffer) throws IOException {
		// NOTE Do nothing, as we have no persistent storage
	}

	@Override
	public List<Double> getWeights() throws IOException {
		return getWeights(-1, -1);
	}

	@Override
	public List<Double> getWeights(int from, int to) throws IOException {
		return weights.subList(from, to);
	}

	@Override
	public List<CombiTestOperationEntry> getEntries(int from, int to) throws IOException { // XXX this is a direct copy of the NetCdf* version of this class!

		Map<Integer, MarkerKey> markersKeys = getMarkersKeysSource().getIndicesMap(from, to);
		List<Double> weightsPart = getWeights(from, to);

		List<CombiTestOperationEntry> entries
				= new ArrayList<CombiTestOperationEntry>(markersKeys.size());
		Iterator<Double> weightsIt = weightsPart.iterator();
		for (Map.Entry<Integer, MarkerKey> origIndicesAndKey : markersKeys.entrySet()) {
			entries.add(new DefaultCombiTestOperationEntry(
					origIndicesAndKey.getValue(),
					origIndicesAndKey.getKey(),
					weightsIt.next()));
		}

		return entries;
	}
}

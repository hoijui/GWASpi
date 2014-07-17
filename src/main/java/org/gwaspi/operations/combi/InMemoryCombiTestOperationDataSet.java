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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.datasource.inmemory.AbstractInMemoryListSource;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractInMemoryOperationDataSet;
import org.gwaspi.operations.OperationTypeInfo;

public class InMemoryCombiTestOperationDataSet
		extends AbstractInMemoryOperationDataSet<CombiTestOperationEntry>
		implements CombiTestOperationDataSet
{
	public InMemoryCombiTestOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(origin, parent, operationKey);
	}

	public InMemoryCombiTestOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return CombiTestOperationFactory.OPERATION_TYPE_INFO;
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

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				CombiTestOperationEntry.TO_WEIGHTS);
	}

	@Override
	public void setWeights(List<Double> weights) throws IOException {

		Iterator<Map.Entry<Integer, MarkerKey>> markerIndicesAndKeysIt
				= getParentDataSetSource().getMarkersKeysSource().getIndicesMap().entrySet().iterator();
		for (Double weight : weights) {
			Map.Entry<Integer, MarkerKey> markerIndexAndKey = markerIndicesAndKeysIt.next();
			addEntry(new DefaultCombiTestOperationEntry(markerIndexAndKey.getValue(), markerIndexAndKey.getKey(), weight));
		}
	}
}

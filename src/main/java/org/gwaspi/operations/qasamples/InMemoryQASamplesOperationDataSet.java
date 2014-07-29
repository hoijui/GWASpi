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

package org.gwaspi.operations.qasamples;

import java.io.IOException;
import java.util.List;
import org.gwaspi.datasource.inmemory.AbstractInMemoryListSource;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.operations.AbstractInMemoryOperationDataSet;
import org.gwaspi.operations.OperationTypeInfo;

public class InMemoryQASamplesOperationDataSet
		extends AbstractInMemoryOperationDataSet<QASamplesOperationEntry>
		implements QASamplesOperationDataSet
{

	public InMemoryQASamplesOperationDataSet(MatrixKey origin, DataSetKey parent) {
		super(origin, parent);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return QASamplesOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public List<Double> getMissingRatios(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QASamplesOperationEntry.TO_MISSING_RATIO);
	}

	@Override
	public List<Integer> getMissingCounts(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QASamplesOperationEntry.TO_MISSING_COUNT);
	}

	@Override
	public List<Double> getHetzyRatios(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QASamplesOperationEntry.TO_HETZY_RATIO);
	}
}

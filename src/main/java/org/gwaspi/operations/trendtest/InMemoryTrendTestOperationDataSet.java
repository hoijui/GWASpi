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

package org.gwaspi.operations.trendtest;

import java.io.IOException;
import java.util.List;
import org.gwaspi.datasource.inmemory.AbstractInMemoryListSource;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.operations.AbstractInMemoryOperationDataSet;
import org.gwaspi.operations.OperationTypeInfo;

public class InMemoryTrendTestOperationDataSet
		extends AbstractInMemoryOperationDataSet<TrendTestOperationEntry>
		implements TrendTestOperationDataSet
{

	public InMemoryTrendTestOperationDataSet(MatrixKey origin, DataSetKey parent) {
		super(origin, parent);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return TrendTestOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public List<Double> getTs(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				TrendTestOperationEntry.TO_T);
	}

	@Override
	public List<Double> getPs(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				TrendTestOperationEntry.TO_P);
	}
}

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

package org.gwaspi.operations.trendtest;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.OperationMetadataFactory;

public class TestOperationFactory<DST extends CommonTestOperationDataSet>
		extends AbstractDefaultTypesOperationFactory<DST>
{
	private final OperationMetadataFactory<DST, QASamplesOperationParams> operationMetadataFactory;

	public TestOperationFactory(final Class<? extends MatrixOperation> type, final OperationTypeInfo typeInfo) {
		super(type, typeInfo);

		this.operationMetadataFactory = new TestOperationMetadataFactory(typeInfo);
	}

	@Override
	protected DST generateReadOperationDataSetNetCdf(
			OperationKey operationKey, DataSetKey parent, Map<String, Object> properties)
			throws IOException
	{

		return new NetCdfQASamplesOperationDataSet(
				parent.getOrigin(), parent, operationKey);
	}

	@Override
	protected DST generateSpecificWriteOperationDataSetMemory(
			DataSetKey parent, Map<String, Object> properties)
			throws IOException
	{
		return new InMemoryQASamplesOperationDataSet(
				parent.getOrigin(), parent);
	}

	@Override
	public OperationMetadataFactory<DST, QASamplesOperationParams> getOperationMetadataFactory() {
		return operationMetadataFactory;
	}
}

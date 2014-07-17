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
import java.util.Map;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.OperationMetadataFactory;
import org.gwaspi.operations.OperationParams;

public class SimpleOperationFactory<PT extends OperationParams> extends AbstractDefaultTypesOperationFactory<SimpleOperationDataSet, PT> {

	private final OperationMetadataFactory<SimpleOperationDataSet, PT> operationMetadataFactory;

	public SimpleOperationFactory(Class<? extends MatrixOperation> type, OperationMetadataFactory<SimpleOperationDataSet, PT> operationMetadataFactory) {
		super(type, operationMetadataFactory.getTypeInfo());

		this.operationMetadataFactory = operationMetadataFactory;
	}

	@Override
	protected SimpleOperationDataSet generateReadOperationDataSetNetCdf(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {
		return new NetCdfSimpleOperationDataSet(parent.getOrigin(), parent, operationKey, operationMetadataFactory.getTypeInfo());
	}

	@Override
	protected SimpleOperationDataSet generateSpecificWriteOperationDataSetMemory(DataSetKey parent, Map<String, Object> properties) throws IOException {
		return new InMemorySimpleOperationDataSet(parent.getOrigin(), parent, operationMetadataFactory.getTypeInfo());
	}

	@Override
	public OperationMetadataFactory<SimpleOperationDataSet, PT> getOperationMetadataFactory() {
		return operationMetadataFactory;
	}
}

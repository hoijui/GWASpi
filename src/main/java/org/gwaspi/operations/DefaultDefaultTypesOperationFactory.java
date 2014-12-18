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

package org.gwaspi.operations;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;

/**
 * The default implementation of an operation factory supporting the default types.
 */
public abstract class DefaultDefaultTypesOperationFactory<D extends OperationDataSet, P extends OperationParams>
		extends AbstractDefaultTypesOperationFactory<D, P>
{

	protected DefaultDefaultTypesOperationFactory(Class<? extends MatrixOperation> type, OperationTypeInfo typeInfo) {
		super(type, typeInfo);
	}

	@Override
	protected D generateReadOperationDataSetNetCdf(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {
		throw new UnsupportedOperationException("Factory type \"" + PROPERTY_VALUE_TYPE_NETCDF + "\" is not supported (yet).");
	}

	@Override
	protected D generateWriteOperationDataSetNetCdf(DataSetKey parent, Map<String, Object> properties) throws IOException {
		return generateReadOperationDataSetNetCdf(null, parent, properties);
	}

	@Override
	protected D generateSpecificWriteOperationDataSetMemory(DataSetKey parent, Map<String, Object> properties) throws IOException {
		throw new UnsupportedOperationException("Factory type \"" + PROPERTY_VALUE_TYPE_MEMORY + "\" is not supported (yet).");
	}
}

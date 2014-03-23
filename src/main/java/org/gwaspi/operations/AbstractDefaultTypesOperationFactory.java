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
import org.gwaspi.netCDF.operations.MatrixOperation;
import org.gwaspi.netCDF.operations.OperationTypeInfo;

public abstract class AbstractDefaultTypesOperationFactory extends AbstractOperationFactory {

	public static final String PROPERTY_VALUE_TYPE_NETCDF = "netcdf";
	public static final String PROPERTY_VALUE_TYPE_MEMORY = "memory";

	protected AbstractDefaultTypesOperationFactory(Class<? extends MatrixOperation> type, OperationTypeInfo typeInfo) {
		super(type, typeInfo);
	}

	protected OperationDataSet generateReadOperationDataSetNetCdf(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {
		throw new UnsupportedOperationException("Factory type \"" + PROPERTY_VALUE_TYPE_NETCDF + "\" is not supported (yet).");
	}

	protected OperationDataSet generateReadOperationDataSetMemory(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {
		throw new UnsupportedOperationException("Factory type \"" + PROPERTY_VALUE_TYPE_MEMORY + "\" is not supported (yet).");
	}

	@Override
	public final OperationDataSet generateReadOperationDataSet(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {

		final Object type = properties.get(PROPERTY_NAME_TYPE);
		if ((type == null) || type.equals(PROPERTY_VALUE_TYPE_NETCDF)) {
			return generateReadOperationDataSetNetCdf(operationKey, parent, properties);
		} else if (type.equals(PROPERTY_VALUE_TYPE_MEMORY)) {
			return generateReadOperationDataSetMemory(operationKey, parent, properties);
		} else {
			throw new UnsupportedOperationException("Factory type \"" + type + "\" is not supported (yet).");
		}
	}

	protected OperationDataSet generateWriteOperationDataSetNetCdf(DataSetKey parent, Map<String, Object> properties) throws IOException {
		return generateReadOperationDataSetNetCdf(null, parent, properties);
	}

	protected OperationDataSet generateWriteOperationDataSetMemory(DataSetKey parent, Map<String, Object> properties) throws IOException {
		return generateReadOperationDataSetMemory(null, parent, properties);
	}

	@Override
	public final OperationDataSet generateWriteOperationDataSet(DataSetKey parent, Map<String, Object> properties) throws IOException {

		final Object type = properties.get(PROPERTY_NAME_TYPE);
		if ((type == null) || type.equals(PROPERTY_VALUE_TYPE_NETCDF)) {
			return generateWriteOperationDataSetNetCdf(parent, properties);
		} else if (type.equals(PROPERTY_VALUE_TYPE_MEMORY)) {
			return generateWriteOperationDataSetMemory(parent, properties);
		} else {
			throw new UnsupportedOperationException("Factory type \"" + type + "\" is not supported (yet).");
		}
	}
}

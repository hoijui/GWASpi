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
import java.util.HashMap;
import java.util.Map;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;

public abstract class AbstractDefaultTypesOperationFactory<DST extends OperationDataSet>
		extends AbstractOperationFactory<DST>
{

	public static final String PROPERTY_VALUE_TYPE_NETCDF = "netcdf";
	public static final String PROPERTY_VALUE_TYPE_MEMORY = "memory";

	private final OperationKeyListener operationKeyRegisterer;
	private final Map<OperationKey, DST> operationKeyToDataSet;

	protected AbstractDefaultTypesOperationFactory(Class<? extends MatrixOperation> type, OperationTypeInfo typeInfo) {
		super(type, typeInfo);

		this.operationKeyRegisterer = new InMemoryOperationKeyListener();
		this.operationKeyToDataSet = new HashMap<OperationKey, DST>();
	}

	protected abstract DST generateReadOperationDataSetNetCdf(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException;

	protected DST generateReadOperationDataSetMemory(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {

		final DST fetchedDataSet = operationKeyToDataSet.get(operationKey);
		if (fetchedDataSet == null) {
			throw new IOException("There is no data in memory for operation " + operationKey.toString());
		}
		return fetchedDataSet;
	}

	@Override
	public final DST generateReadOperationDataSet(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {

		final Object type = properties.get(PROPERTY_NAME_TYPE);
		if ((type == null) || type.equals(PROPERTY_VALUE_TYPE_NETCDF)) {
			return generateReadOperationDataSetNetCdf(operationKey, parent, properties);
		} else if (type.equals(PROPERTY_VALUE_TYPE_MEMORY)) {
			return generateReadOperationDataSetMemory(operationKey, parent, properties);
		} else {
			throw new UnsupportedOperationException("Factory type \"" + type + "\" is not supported (yet).");
		}
	}

	protected DST generateWriteOperationDataSetNetCdf(DataSetKey parent, Map<String, Object> properties) throws IOException {
		return generateReadOperationDataSetNetCdf(null, parent, properties);
	}

	private class InMemoryOperationKeyListener implements OperationKeyListener {

		InMemoryOperationKeyListener() {}

		@Override
		public void operationKeySet(OperationKeySetEvent evt) {
			operationKeyToDataSet.put(evt.getSource().getOperationKey(), (DST) evt.getSource());
		}
	}

	protected abstract DST generateSpecificWriteOperationDataSetMemory(DataSetKey parent, Map<String, Object> properties) throws IOException;

	protected DST generateWriteOperationDataSetMemory(DataSetKey parent, Map<String, Object> properties) throws IOException {

		final DST specificWriteDS = generateSpecificWriteOperationDataSetMemory(parent, properties);
		specificWriteDS.addOperationKeyListener(operationKeyRegisterer);
		return specificWriteDS;
	}

	@Override
	public final DST generateWriteOperationDataSet(DataSetKey parent, Map<String, Object> properties) throws IOException {

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

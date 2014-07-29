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

public interface OperationFactory<DST extends OperationDataSet> {

	/**
	 * Property name for the main type of OperationDataSet to generate.
	 * This will be used in the properties parameter in {@link #generateReadOperationDataSet}.
	 */
	public static final String PROPERTY_NAME_TYPE = "type";

	Class<? extends MatrixOperation> getType();

	OperationTypeInfo getTypeInfo();

	OperationMetadataFactory<DST> getOperationMetadataFactory();

	DST generateReadOperationDataSet(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException;

	DST generateWriteOperationDataSet(DataSetKey parent, Map<String, Object> properties) throws IOException;
}

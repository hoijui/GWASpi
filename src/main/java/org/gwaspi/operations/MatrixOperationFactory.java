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

public class MatrixOperationFactory extends AbstractOperationFactory {

	public MatrixOperationFactory(Class<? extends MatrixOperation> type, OperationTypeInfo typeInfo) {
		super(type, typeInfo);
	}

	@Override
	public OperationDataSet generateReadOperationDataSet(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {
		throw new UnsupportedOperationException("(coding error) This is a matrix-operation factory; you can not generate an OperationDataSet from it.");
	}

	@Override
	public OperationDataSet generateWriteOperationDataSet(DataSetKey parent, Map<String, Object> properties) throws IOException {
		throw new UnsupportedOperationException("(coding error) This is a matrix-operation factory; you can not generate an OperationDataSet from it.");
	}
}

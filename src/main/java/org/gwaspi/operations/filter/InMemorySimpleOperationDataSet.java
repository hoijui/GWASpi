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

import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.operations.AbstractInMemoryOperationDataSet;
import org.gwaspi.operations.OperationTypeInfo;

public class InMemorySimpleOperationDataSet extends AbstractInMemoryOperationDataSet<SimpleOperationEntry> implements SimpleOperationDataSet {

	private final OperationTypeInfo typeInfo;

	public InMemorySimpleOperationDataSet(MatrixKey origin, DataSetKey parent, OperationTypeInfo typeInfo) {
		super(origin, parent);

		this.typeInfo = typeInfo;
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return typeInfo;
	}
}

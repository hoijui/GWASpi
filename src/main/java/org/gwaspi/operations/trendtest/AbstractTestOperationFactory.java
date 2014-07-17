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

import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.OperationMetadataFactory;

public abstract class AbstractTestOperationFactory<DST extends CommonTestOperationDataSet, PT extends TrendTestOperationParams>
		extends AbstractDefaultTypesOperationFactory<DST, PT>
{
	private final OperationMetadataFactory<DST, PT> operationMetadataFactory;

	public AbstractTestOperationFactory(final Class<? extends MatrixOperation> type, final OperationTypeInfo typeInfo) {
		super(type, typeInfo);

		this.operationMetadataFactory = new TestOperationMetadataFactory<DST, PT>(typeInfo);
	}

	@Override
	public OperationMetadataFactory<DST, PT> getOperationMetadataFactory() {
		return operationMetadataFactory;
	}
}

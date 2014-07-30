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

package org.gwaspi.operations.combi;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationMetadataFactory;

public class CombiTestOperationFactory
		extends AbstractDefaultTypesOperationFactory<CombiTestOperationDataSet>
{
	static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"COMBI Test",
					"Assigns a weight to each marker, rating its ability to predict the affection", // FIXME TODO We need a more elaborate description of this operation!
					cNetCDF.Defaults.OPType.COMBI_ASSOC_TEST,
					true,
					false);

	private final OperationMetadataFactory<CombiTestOperationDataSet, CombiTestOperationParams> operationMetadataFactory;

	public CombiTestOperationFactory() {
		super(CombiTestMatrixOperation.class, OPERATION_TYPE_INFO);

		this.operationMetadataFactory = new CombiTestOperationMetadataFactory();
	}

	@Override
	protected CombiTestOperationDataSet generateReadOperationDataSetNetCdf(
			OperationKey operationKey, DataSetKey parent, Map<String, Object> properties)
			throws IOException
	{

		return new NetCdfCombiTestOperationDataSet(
				parent.getOrigin(), parent, operationKey);
	}

	@Override
	protected CombiTestOperationDataSet generateSpecificWriteOperationDataSetMemory(
			DataSetKey parent, Map<String, Object> properties)
			throws IOException
	{
		return new InMemoryCombiTestOperationDataSet(
				parent.getOrigin(), parent);
	}

	@Override
	public OperationMetadataFactory<CombiTestOperationDataSet, CombiTestOperationParams> getOperationMetadataFactory() {
		return operationMetadataFactory;
	}
}

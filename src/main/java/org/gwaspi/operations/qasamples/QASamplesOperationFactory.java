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

package org.gwaspi.operations.qasamples;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.OperationMetadataFactory;

public class QASamplesOperationFactory
		extends AbstractDefaultTypesOperationFactory<QASamplesOperationDataSet, QASamplesOperationParams>
{
	static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Samples Quality Assurance",
					"Samples Quality Assurance", // TODO We need a more elaborate description of this operation!
					OPType.SAMPLE_QA,
					false,
					true);

	private final OperationMetadataFactory<QASamplesOperationDataSet, QASamplesOperationParams> operationMetadataFactory;

	public QASamplesOperationFactory() {
		super(QASamplesOperation.class, OPERATION_TYPE_INFO);

		this.operationMetadataFactory = new QASamplesOperationMetadataFactory();
	}

	@Override
	protected QASamplesOperationDataSet generateReadOperationDataSetNetCdf(
			OperationKey operationKey, DataSetKey parent, Map<String, Object> properties)
			throws IOException
	{

		return new NetCdfQASamplesOperationDataSet(
				parent.getOrigin(), parent, operationKey);
	}

	@Override
	protected QASamplesOperationDataSet generateSpecificWriteOperationDataSetMemory(
			DataSetKey parent, Map<String, Object> properties)
			throws IOException
	{
		return new InMemoryQASamplesOperationDataSet(
				parent.getOrigin(), parent);
	}

	@Override
	public OperationMetadataFactory<QASamplesOperationDataSet, QASamplesOperationParams> getOperationMetadataFactory() {
		return operationMetadataFactory;
	}
}

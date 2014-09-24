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

package org.gwaspi.operations.combi;

import java.io.IOException;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.OperationMetadataFactory;

public class CombiTestOperationMetadataFactory
		implements OperationMetadataFactory<CombiTestOperationDataSet, CombiTestOperationParams>
{

	@Override
	public OperationTypeInfo getTypeInfo() {
		return CombiTestOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public OperationMetadata generateMetadata(
			final CombiTestOperationDataSet operationDataSet,
			final CombiTestOperationParams params)
			throws IOException
	{
		DataSetKey parentDataSetKey = operationDataSet.getParent();
		DataSetMetadata parentDataSetMetadata = MatricesList.getDataSetMetadata(parentDataSetKey);
		return new OperationMetadata(
				parentDataSetKey, // parent data set
				"COMBI_Test"/* + myFriendlyName*/, // friendly name
				"COMBI test on " + parentDataSetMetadata.getFriendlyName(), // description
				getTypeInfo().getType(), // operationType
				operationDataSet.getNumMarkers(),
				operationDataSet.getNumSamples(),
				operationDataSet.getNumChromosomes(),
				getTypeInfo().isMarkersOriented());
	}
}

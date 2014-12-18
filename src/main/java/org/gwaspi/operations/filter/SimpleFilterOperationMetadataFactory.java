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

import java.io.IOException;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.OperationMetadataFactory;
import org.gwaspi.operations.OperationParams;
import org.gwaspi.operations.OperationTypeInfo;

public class SimpleFilterOperationMetadataFactory<P extends OperationParams>
		implements OperationMetadataFactory<SimpleOperationDataSet, P>
{
	private final OperationTypeInfo typeInfo;
	private final String filterDescription;

	public SimpleFilterOperationMetadataFactory(OperationTypeInfo typeInfo, final String filterDescription) {

		this.typeInfo = typeInfo;
		this.filterDescription = filterDescription;
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return typeInfo;
	}

	@Override
	public OperationMetadata generateMetadata(SimpleOperationDataSet operationDataSet, P params) throws IOException {

		return new OperationMetadata(
				operationDataSet.getParent(), // parent data set
				"Filtering_by_" + filterDescription, // friendly name
				"Filters the markers and/or samples by " + filterDescription, // description
				getTypeInfo().getType(), // operationType
				operationDataSet.getNumMarkers(),
				operationDataSet.getNumSamples(),
				operationDataSet.getNumChromosomes(),
				getTypeInfo().isMarkersOriented());
	}
}

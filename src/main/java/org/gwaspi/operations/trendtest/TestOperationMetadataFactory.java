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

import java.io.IOException;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.OperationMetadataFactory;

public class TestOperationMetadataFactory implements OperationMetadataFactory<CommonTestOperationDataSet, TrendTestOperationParams> {

	private final OperationTypeInfo typeInfo;

	public TestOperationMetadataFactory(final OperationTypeInfo typeInfo) {
		this.typeInfo = typeInfo;
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return typeInfo;
	}

	@Override
	public OperationMetadata generateMetadata(CommonTestOperationDataSet operationDataSet, TrendTestOperationParams params) throws IOException {

		OperationMetadata markerCensusOP = OperationsList.getOperationMetadata(params.getMarkerCensus());

		return new OperationMetadata(
				operationDataSet.getParent(), // parent data set
				params.getName(), // friendly name
				params.getName() + " on " + markerCensusOP.getFriendlyName()
						+ "\n" + markerCensusOP.getDescription(), // description
				getTypeInfo().getType(),
				operationDataSet.getNumMarkers(),
				operationDataSet.getNumSamples(),
				operationDataSet.getNumChromosomes(),
				getTypeInfo().isMarkersOriented());
	}
}

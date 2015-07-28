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
import org.gwaspi.dao.OperationService;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.OperationMetadataFactory;

public class TestOperationMetadataFactory<D extends CommonTestOperationDataSet, P extends TrendTestOperationParams>
		implements OperationMetadataFactory<D, P>
{

	private final OperationTypeInfo typeInfo;

	public TestOperationMetadataFactory(final OperationTypeInfo typeInfo) {
		this.typeInfo = typeInfo;
	}

	private static OperationService getOperationService() {
		return OperationsList.getOperationService();
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return typeInfo;
	}

	@Override
	public OperationMetadata generateMetadata(D operationDataSet, P params) throws IOException {

		OperationMetadata markerCensusOP = getOperationService().getOperationMetadata(params.getMarkerCensus());

		final boolean markersOriented = getTypeInfo().isMarkersOriented();
		final int numMarkers = operationDataSet.getNumMarkers();
		final int numSamples = operationDataSet.getNumSamples();
		return new OperationMetadata(
				operationDataSet.getParent(), // parent data set
				params.getName(), // friendly name
				params.getName() + " on " + markerCensusOP.getFriendlyName()
						+ "\n" + markerCensusOP.getDescription(), // description
				getTypeInfo().getType(),
				markersOriented ? numMarkers : numSamples,
				markersOriented ? numSamples : numMarkers,
				operationDataSet.getNumChromosomes(),
				markersOriented);
	}
}

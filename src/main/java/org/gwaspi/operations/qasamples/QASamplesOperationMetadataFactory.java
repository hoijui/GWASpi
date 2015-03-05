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

package org.gwaspi.operations.qasamples;

import java.io.IOException;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.OperationMetadataFactory;

public class QASamplesOperationMetadataFactory implements OperationMetadataFactory<QASamplesOperationDataSet, QASamplesOperationParams> {

	@Override
	public OperationTypeInfo getTypeInfo() {
		return QASamplesOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public OperationMetadata generateMetadata(QASamplesOperationDataSet operationDataSet, QASamplesOperationParams params) throws IOException {

		DataSetMetadata rdDataSetMetadata = MatricesList.getDataSetMetadata(operationDataSet.getParent());

		final boolean markersOriented = getTypeInfo().isMarkersOriented();
		final int numMarkers = operationDataSet.getNumMarkers();
		final int numSamples = operationDataSet.getNumSamples();
		return new OperationMetadata(
				operationDataSet.getParent(), // parent data set
				"Sample QA", // friendly name
				"Sample census on " + rdDataSetMetadata.getFriendlyName()
						+ "\nSamples: " + operationDataSet.getNumSamples(), // description
				getTypeInfo().getType(),
				markersOriented ? numMarkers : numSamples,
				markersOriented ? numSamples : numMarkers,
				operationDataSet.getNumChromosomes(),
				markersOriented);
	}
}

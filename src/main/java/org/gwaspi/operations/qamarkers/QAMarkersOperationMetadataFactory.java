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

package org.gwaspi.operations.qamarkers;

import java.io.IOException;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.OperationMetadataFactory;

public class QAMarkersOperationMetadataFactory implements OperationMetadataFactory<QAMarkersOperationDataSet> {

	@Override
	public OperationTypeInfo getTypeInfo() {
		return QAMarkersOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public OperationMetadata generateMetadata(QAMarkersOperationDataSet operationDataSet) throws IOException {

		DataSetMetadata rdDataSetMetadata = MatricesList.getDataSetMetadata(operationDataSet.getParent());

		String description = "Marker Quality Assurance on "
				+ rdDataSetMetadata.getFriendlyName()
				+ "\nMarkers: " + operationDataSet.getNumMarkers()
				+ "\nStarted at: " + org.gwaspi.global.Utils.getShortDateTimeAsString();

		return new OperationMetadata(
				operationDataSet.getParent(), // parent data set
				"Marker QA", // friendly name
				description, // description
				getTypeInfo().getType(),
				operationDataSet.getNumMarkers(),
				operationDataSet.getNumSamples(),
				operationDataSet.getNumChromosomes(),
				getTypeInfo().isMarkersOriented());
	}
}

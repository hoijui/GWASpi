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

package org.gwaspi.operations.hardyweinberg;

import java.io.IOException;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.OperationMetadataFactory;

public class HardyWeinbergOperationMetadataFactory
		implements OperationMetadataFactory<HardyWeinbergOperationDataSet, HardyWeinbergOperationParams>
{

	@Override
	public OperationTypeInfo getTypeInfo() {
		return HardyWeinbergOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public OperationMetadata generateMetadata(
			final HardyWeinbergOperationDataSet operationDataSet,
			final HardyWeinbergOperationParams params)
			throws IOException
	{
		final boolean markersOriented = getTypeInfo().isMarkersOriented();
		final int numMarkers = operationDataSet.getNumMarkers();
		final int numSamples = operationDataSet.getNumSamples();
		return new OperationMetadata(
				operationDataSet.getParent(), // parent data set
				"Hardy-Weinberg_" + params.getName(), // friendly name
				"Hardy-Weinberg test on Samples marked as controls (only females for the X chromosome)"
					+ "\nMarkers: " + operationDataSet.getNumMarkers() + ""
					+ "\nSamples: " + operationDataSet.getNumSamples(), // description
				getTypeInfo().getType(), // operationType
				markersOriented ? numMarkers : numSamples,
				markersOriented ? numSamples : numMarkers,
				operationDataSet.getNumChromosomes(),
				markersOriented);
	}
}

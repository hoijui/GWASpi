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

package org.gwaspi.operations.markercensus;

import java.io.IOException;
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.OperationMetadataFactory;

public class MarkerCensusOperationMetadataFactory
		implements OperationMetadataFactory<MarkerCensusOperationDataSet, MarkerCensusOperationParams>
{

	@Override
	public OperationTypeInfo getTypeInfo() {
		return MarkerCensusOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public OperationMetadata generateMetadata(
			MarkerCensusOperationDataSet operationDataSet,
			final MarkerCensusOperationParams params)
			throws IOException
	{
		DataSetMetadata rdDataSetMetadata = MatricesList.getDataSetMetadata(operationDataSet.getParent());

		NetCDFConstants.Defaults.OPType opType = getTypeInfo().getType();

		String description = "Genotype frequency count -" + params.getName() + "- on " + rdDataSetMetadata.getFriendlyName();
		if (params.getPhenotypeFile() != null) {
			description += "\nCase/Control status read from file: " + params.getPhenotypeFile().getPath();
			opType = NetCDFConstants.Defaults.OPType.MARKER_CENSUS_BY_PHENOTYPE;
		}

		final boolean markersOriented = getTypeInfo().isMarkersOriented();
		final int numMarkers = operationDataSet.getNumMarkers();
		final int numSamples = operationDataSet.getNumSamples();
		return new OperationMetadata(
				operationDataSet.getParent(), // parent data set
				params.getName(), // friendly name
				description
					+ "\nSample missing ratio threshold: " + params.getSampleMissingRatio()
					+ "\nSample heterozygosity ratio threshold: " + params.getSampleHetzygRatio()
					+ "\nMarker missing ratio threshold: " + params.getMarkerMissingRatio()
					+ "\nDiscard mismatching Markers: " + params.isDiscardMismatches()
					+ "\nMarkers: " + operationDataSet.getNumMarkers()
					+ "\nSamples: " + operationDataSet.getNumSamples(), // description
				opType,
				markersOriented ? numMarkers : numSamples,
				markersOriented ? numSamples : numMarkers,
				operationDataSet.getNumChromosomes(),
				markersOriented);
	}
}

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
import org.gwaspi.gui.reports.Report_Analysis;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MatricesList;
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

		DataSetMetadata rdDataSetMetadata = MatricesList.getDataSetMetadata(operationDataSet.getParent());
		OperationMetadata markerCensusOP = OperationsList.getOperationMetadata(markerCensusOPKey);

		return new OperationMetadata(
				operationDataSet.getParent(), // parent data set
				testName, // friendly name
				testName + " on " + markerCensusOP.getFriendlyName()
						+ "\n" + markerCensusOP.getDescription()
						+ "\nHardy-Weinberg threshold: "
						+ Report_Analysis.FORMAT_SCIENTIFIC.format(hardyWeinbergThreshold), // description
				getTypeInfo().getType(),
				operationDataSet.getNumMarkers(),
				operationDataSet.getNumSamples(),
				operationDataSet.getNumChromosomes(),
				getTypeInfo().isMarkersOriented());
	}
}

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

package org.gwaspi.operations.trendtest;

import org.gwaspi.operations.qasamples.*;
import java.io.IOException;
import java.util.Collection;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.gui.reports.Report_Analysis;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

public abstract class AbstractNetCdfTestOperationDataSet extends AbstractNetCdfOperationDataSet {

	private final Logger log = LoggerFactory.getLogger(AbstractNetCdfTestOperationDataSet.class);

	private String testName;

	public AbstractNetCdfTestOperationDataSet() {
		super(true);
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	@Override
	protected OperationFactory createOperationFactory() throws IOException {

		try {
			MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(getReadMatrixKey());

			// CREATE netCDF-3 FILE
			return new OperationFactory(
					rdCensusOPMetadata.getStudyKey(),
					testName, // friendly name
					testName + " on " + markerCensusOP.getFriendlyName()
						+ "\n" + rdCensusOPMetadata.getDescription()
						+ "\nHardy-Weinberg threshold: " + Report_Analysis.FORMAT_SCIENTIFIC.format(hwThreshold), // description
					wrMarkerMetadata.size(),
					rdCensusOPMetadata.getImplicitSetSize(),
					chromosomeInfo.size(),
					testType,
					rdCensusOPMetadata.getParentMatrixKey(), // Parent matrixId
					markerCensusOP.getId()); // Parent operationId
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}
}

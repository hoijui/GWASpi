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

import java.io.IOException;
import org.gwaspi.gui.reports.Report_Analysis;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

public abstract class AbstractNetCdfTestOperationDataSet extends AbstractNetCdfOperationDataSet {

	// - Variables.VAR_OPSET: wrMarkerMetadata.keySet() [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: markers RS ID from the rd marker census opertion, sorted by wrMarkerMetadata.keySet() [Collection<String>]
	// - Variables.VAR_IMPLICITSET: "implicit set", rdSampleSetMap.keySet(), original sample keys [Collection<SampleKey>]
	// - Variables.VAR_CHR_IN_MATRIX: chromosomeInfo.keySet() [Collection<ChromosomeKey>]
	// - Variables.VAR_CHR_INFO: chromosomeInfo.values() [Collection<ChromosomeInfo>]
	// switch (test-type) {
	//   case "allelic association test": Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR: {T, P-Value, OR} [Double[3]]
	//   case "genotypic association test": Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR: {T, P-Value, OR-1, OR-2} [Double[4]]
	//   case "trend test": Association.VAR_OP_MARKERS_ASTrendTestTP: {T, P-Value} [Double[2]]
	// }

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
					getNumMarkers(),
					getNumSamples(),
					getNumChromosomes(),
					testType,
					rdCensusOPMetadata.getParentMatrixKey(), // Parent matrixId
					markerCensusOP.getId()); // Parent operationId
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}
}

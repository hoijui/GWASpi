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
import java.util.List;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.gui.reports.Report_Analysis;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public abstract class AbstractNetCdfTestOperationDataSet<ET> extends AbstractNetCdfOperationDataSet<ET> {

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

	private OperationKey markerCensusOPKey;
	private double hardyWeinbergThreshold;
	private String testName;
	private OPType testType;

	public AbstractNetCdfTestOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(true, origin, parent, operationKey);

		this.markerCensusOPKey = null;
		this.hardyWeinbergThreshold = Double.MIN_VALUE;
		this.testName = null;
		this.testType = null;
	}

	public AbstractNetCdfTestOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
	}

	public void setMarkerCensusOPKey(OperationKey markerCensusOPKey) {
		this.markerCensusOPKey = markerCensusOPKey;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public void setTestType(OPType testType) {
		this.testType = testType;
	}

	public void setHardyWeinbergThreshold(double hardyWeinbergThreshold) {
		this.hardyWeinbergThreshold = hardyWeinbergThreshold;
	}

	@Override
	protected void supplementNetCdfHandler(
			NetcdfFileWriteable ncFile,
			OperationMetadata operationMetadata,
			List<Dimension> markersSpace,
			List<Dimension> chromosomesSpace,
			List<Dimension> samplesSpace)
			throws IOException
	{
		final OPType opType = operationMetadata.getOperationType();

		// Define Variables
		ncFile.addVariable(cNetCDF.Association.VAR_OP_MARKERS_T, DataType.DOUBLE, markersSpace);
		ncFile.addVariable(cNetCDF.Association.VAR_OP_MARKERS_P, DataType.DOUBLE, markersSpace);
		if ((opType == OPType.ALLELICTEST) || (opType == OPType.GENOTYPICTEST)) {
			ncFile.addVariable(cNetCDF.Association.VAR_OP_MARKERS_OR, DataType.DOUBLE, markersSpace);
			if (opType == OPType.GENOTYPICTEST) {
				ncFile.addVariable(cNetCDF.Association.VAR_OP_MARKERS_OR2, DataType.DOUBLE, markersSpace);
			}
		}
		if (opType == OPType.COMBI_ASSOC_TEST) {
			// TODO FIXME What is stored for the combi test?
		}
	}

	@Override
	protected OperationMetadata createOperationMetadata() throws IOException {

		OperationMetadata markerCensusOP = OperationsList.getOperationMetadata(markerCensusOPKey);

		return new OperationMetadata(
				new DataSetKey(markerCensusOPKey), // parent data set
				testName, // friendly name
				testName + " on " + markerCensusOP.getFriendlyName()
						+ "\n" + markerCensusOP.getDescription()
						+ "\nHardy-Weinberg threshold: "
						+ Report_Analysis.FORMAT_SCIENTIFIC.format(hardyWeinbergThreshold), // description
				testType,
				getNumMarkers(),
				getNumSamples(),
				getNumChromosomes(),
				isMarkersOperationSet());
	}
}

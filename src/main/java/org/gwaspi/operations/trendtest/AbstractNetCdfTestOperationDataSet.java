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
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.gui.reports.Report_Analysis;
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

	public AbstractNetCdfTestOperationDataSet(OperationKey operationKey) {
		super(true, operationKey);

		this.markerCensusOPKey = null;
		this.hardyWeinbergThreshold = Double.MIN_VALUE;
		this.testName = null;
		this.testType = null;
	}

	public AbstractNetCdfTestOperationDataSet() {
		this(null);
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
	public NetcdfFileWriteable generateNetCdfHandler(
			OperationMetadata operationMetadata)
			throws IOException
	{
		final int boxDimensions;
		final String boxDimensionsName;
		final String ncVariableName;
		if (operationMetadata.getOperationType() == OPType.TRENDTEST) {
			boxDimensions = 2;
			boxDimensionsName = cNetCDF.Dimensions.DIM_2BOXES;
			ncVariableName = cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP;
		} else if (operationMetadata.getOperationType() == OPType.ALLELICTEST) {
			boxDimensions = 3;
			boxDimensionsName = cNetCDF.Dimensions.DIM_3BOXES;
			ncVariableName = cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR;
		} else if (operationMetadata.getOperationType() == OPType.GENOTYPICTEST) {
			boxDimensions = 4;
			boxDimensionsName = cNetCDF.Dimensions.DIM_4BOXES;
			ncVariableName = cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR;
		} else if (operationMetadata.getOperationType() == OPType.COMBI_ASSOC_TEST) {
			boxDimensions = 4; // FIXME
			boxDimensionsName = cNetCDF.Dimensions.DIM_4BOXES; // FIXME
			ncVariableName = cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR; // FIXME
		} else {
			throw new IOException("Unsupported operation type " + operationMetadata.getOperationType().name());
		}

		final int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
		final int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

		NetcdfFileWriteable ncfile = createNetCdfFile(operationMetadata);

		// global attributes
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, operationMetadata.getStudyId());
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, operationMetadata.getDescription());

		// dimensions
		Dimension setDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, operationMetadata.getOpSetSize());
		Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, operationMetadata.getImplicitSetSize());
		Dimension chrSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_CHRSET, operationMetadata.getNumChromosomes());
		Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
		Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
		Dimension boxDim = ncfile.addDimension(boxDimensionsName, boxDimensions);
		Dimension dim8 = ncfile.addDimension(cNetCDF.Dimensions.DIM_8, 8);
		Dimension dim4 = ncfile.addDimension(cNetCDF.Dimensions.DIM_4, 4);

		// OP SPACES
		List<Dimension> opSpace = new ArrayList<Dimension>();
		opSpace.add(setDim);
		opSpace.add(boxDim);

		// MARKER SPACES
		List<Dimension> markerNameSpace = new ArrayList<Dimension>();
		markerNameSpace.add(setDim);
		markerNameSpace.add(markerStrideDim);

		// CHROMOSOME SPACES
		List<Dimension> chrSetSpace = new ArrayList<Dimension>();
		chrSetSpace.add(chrSetDim);
		chrSetSpace.add(dim8);

		List<Dimension> chrInfoSpace = new ArrayList<Dimension>();
		chrInfoSpace.add(chrSetDim);
		chrInfoSpace.add(dim4);

		// SAMPLE SPACES
		List<Dimension> sampleSetSpace = new ArrayList<Dimension>();
		sampleSetSpace.add(implicitSetDim);
		sampleSetSpace.add(sampleStrideDim);

		// Define OP Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_OPSET, DataType.CHAR, markerNameSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markerNameSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET, DataType.CHAR, sampleSetSpace);

		// Define Chromosome Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX, DataType.CHAR, chrSetSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_CHR_INFO, DataType.INT, chrInfoSpace);

		ncfile.addVariable(ncVariableName, DataType.DOUBLE, opSpace);

		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, operationMetadata.getOpSetSize());

		return ncfile;
	}

	@Override
	protected OperationMetadata createOperationMetadata() throws IOException {

		OperationMetadata markerCensusOP = OperationsList.getOperation(markerCensusOPKey);

		return new OperationMetadata(
				markerCensusOP.getParentMatrixKey(), // parent matrix
				markerCensusOP.getId(), // parent operation ID
				testName, // friendly name
				testName + " on " + markerCensusOP.getFriendlyName()
						+ "\n" + markerCensusOP.getDescription()
						+ "\nHardy-Weinberg threshold: "
						+ Report_Analysis.FORMAT_SCIENTIFIC.format(hardyWeinbergThreshold), // description
				testType,
				getNumMarkers(),
				getNumSamples(),
				getNumChromosomes());
	}
}

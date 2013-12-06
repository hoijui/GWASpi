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

package org.gwaspi.netCDF.operations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.allelicassociationtest.NetCdfAllelicAssociationTestsOperationDataSet;
import org.gwaspi.operations.genotypicassociationtest.NetCdfGenotypicAssociationTestsOperationDataSet;
import org.gwaspi.operations.hardyweinberg.NetCdfHardyWeinbergOperationDataSet;
import org.gwaspi.operations.markercensus.NetCdfMarkerCensusOperationDataSet;
import org.gwaspi.operations.qamarkers.NetCdfQAMarkersOperationDataSet;
import org.gwaspi.operations.qasamples.NetCdfQASamplesOperationDataSet;
import org.gwaspi.operations.trendtest.NetCdfTrendTestOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import ucar.ma2.DataType;
//import ucar.ma2.InvalidRangeException;
//import ucar.nc2.Dimension;
//import ucar.nc2.NetcdfFileWriteable;

public class OperationFactory {

	private static final Logger log = LoggerFactory.getLogger(OperationFactory.class);

	private NetcdfFileWriteable netCDFHandler = null;
	private String resultOPnetCDFName = "";
	private String resultOPType = "";
	private OperationKey resultOperationKey = null;
	private OperationMetadata opMetaData = null;

	/**
	 * To use with matrix input.
	 */
	public OperationFactory(
			StudyKey studyKey,
			String friendlyName,
			String description,
			int opSetSize,
			int implicitSetSize,
			int chrSetSize,
			OPType opType,
			MatrixKey parentMatrixKey,
			int parentOperationId)
			throws InvalidRangeException, IOException
	{
		// OPERATION CASE SELECTOR
		resultOPnetCDFName = opType.name() + "_" + MatrixFactory.generateMatrixNetCDFNameByDate();
		switch (opType) {
			case MARKER_QA:
				//resultOPnetCDFName = OPType + "_" + rdMatrixMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfMarkerQAHandler(
						studyKey,
						resultOPnetCDFName,
						description,
						opType,
						opSetSize,
						implicitSetSize);
				break;
			case SAMPLE_QA:
				//resultOPnetCDFName = OPType + "_" + rdMatrixMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfSampleQAHandler(
						studyKey,
						resultOPnetCDFName,
						description,
						opType,
						opSetSize,
						implicitSetSize);
				break;
			case MARKER_CENSUS_BY_AFFECTION:
				//resultOPnetCDFName = OPType + "_" + MatrixManager.generateMatrixNetCDFNameByDate();
				netCDFHandler = generateNetcdfCensusHandler(
						studyKey,
						resultOPnetCDFName,
						description,
						opType,
						opSetSize,
						implicitSetSize);
				break;
			case MARKER_CENSUS_BY_PHENOTYPE:
				//resultOPnetCDFName = OPType + "_" + MatrixManager.generateMatrixNetCDFNameByDate();
				netCDFHandler = generateNetcdfCensusHandler(
						studyKey,
						resultOPnetCDFName,
						description,
						opType,
						opSetSize,
						implicitSetSize);
				break;
			case HARDY_WEINBERG:
				OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(parentOperationId);
				//resultOPnetCDFName = OPType + "_" + rdOPMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfHardyWeinbergHandler(
						studyKey,
						resultOPnetCDFName,
						description,
						opType,
						opSetSize,
						implicitSetSize);
				break;
			case ALLELICTEST:
			case GENOTYPICTEST:
			case COMBI_ASSOC_TEST:
				rdOPMetadata = OperationsList.getOperationMetadata(parentOperationId);
				//resultOPnetCDFName = OPType + "_" + rdOPMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfAssociationHandler(
						studyKey,
						resultOPnetCDFName,
						description,
						opType,
						opSetSize,
						implicitSetSize,
						chrSetSize);
				break;
			case TRENDTEST:
				rdOPMetadata = OperationsList.getOperationMetadata(parentOperationId);
				//resultOPnetCDFName = OPType + "_" + rdOPMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfTrendTestHandler(
						studyKey,
						resultOPnetCDFName,
						description,
						opType,
						opSetSize,
						implicitSetSize,
						chrSetSize);
				break;
			default:
				throw new IllegalArgumentException("invalid OPType: " + opType);
		}

		resultOperationKey = OperationsList.insertOPMetadata(new OperationMetadata(
				Integer.MIN_VALUE,
				parentMatrixKey,
				parentOperationId,
				friendlyName,
				resultOPnetCDFName,
				description,
				"",
				opType,
				Integer.MIN_VALUE,
				Integer.MIN_VALUE,
				null
				));

		opMetaData = OperationsList.getOperation(resultOperationKey);
	}

	// ACCESSORS
	public NetcdfFileWriteable getNetCDFHandler() {
		return netCDFHandler;
	}

	public String getResultOPName() {
		return resultOPnetCDFName;
	}

	public String getResultOPType() {
		return resultOPType;
	}

	public int getResultOPId() {
		return resultOperationKey.getId();
	}

	public OperationKey getResultOperationKey() {
		return resultOperationKey;
	}

	public OperationMetadata getResultOPMetadata() {
		return opMetaData;
	}

	/**
	 * Creates a new OperationDataSet for the specified type.
	 * @param operationType
	 * @return
	 * @throws IOException
	 */
	public static OperationDataSet generateOperationDataSet(OPType operationType) throws IOException {
		return generateOperationDataSet(operationType, null);
	}

	public static OperationDataSet generateOperationDataSet(OperationKey operationKey) throws IOException {

		OperationMetadata operationMetadata = OperationsList.getOperation(operationKey);
		OPType operationType = operationMetadata.getOperationType();

		return generateOperationDataSet(operationType, operationKey);
	}

	private static OperationDataSet generateOperationDataSet(OPType operationType, OperationKey operationKey) throws IOException {

		OperationDataSet operationDataSet;

		boolean useNetCdf = true;
		if (useNetCdf) {
			switch (operationType) {
				case SAMPLE_QA:
					operationDataSet = new NetCdfQASamplesOperationDataSet(operationKey);
					break;
				case MARKER_QA:
					operationDataSet = new NetCdfQAMarkersOperationDataSet(operationKey);
					break;
				case MARKER_CENSUS_BY_AFFECTION:
				case MARKER_CENSUS_BY_PHENOTYPE:
					operationDataSet = new NetCdfMarkerCensusOperationDataSet(operationKey);
					break;
				case HARDY_WEINBERG:
					operationDataSet = new NetCdfHardyWeinbergOperationDataSet(operationKey);
					break;
				case ALLELICTEST:
					operationDataSet = new NetCdfAllelicAssociationTestsOperationDataSet(operationKey);
					break;
				case GENOTYPICTEST:
					operationDataSet = new NetCdfGenotypicAssociationTestsOperationDataSet(operationKey);
					break;
				case COMBI_ASSOC_TEST:
					// FIXME
					throw new UnsupportedOperationException("create and implement the class mentioned in hte comment below");
//					operationDataSet = new NetCdfComb(operationKey);
//					break;
				case TRENDTEST:
					operationDataSet = new NetCdfTrendTestOperationDataSet(operationKey);
					break;
				default:
				case SAMPLE_HTZYPLOT:
				case MANHATTANPLOT:
				case QQPLOT:
					throw new IllegalArgumentException("This operation type is invalid, or has no data-attached");
			}
		} else {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		return operationDataSet;
	}

	private static NetcdfFileWriteable generateNetcdfMarkerQAHandler(
			StudyKey studyKey,
			String resultOPName,
			String description,
			OPType opType,
			int markerSetSize,
			int sampleSetSize)
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			File pathToStudy = new File(Study.constructGTPath(studyKey));
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(pathToStudy);
			}

			int gtStride = cNetCDF.Strides.STRIDE_GT;
			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + resultOPName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyKey.getId());
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);

			// dimensions
			Dimension markerSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, markerSetSize);
			Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, sampleSetSize);
			Dimension boxes4Dim = ncfile.addDimension(cNetCDF.Dimensions.DIM_4BOXES, 4);
			Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
			Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
			Dimension alleleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_GTSTRIDE, gtStride / 2);
			Dimension dim4 = ncfile.addDimension(cNetCDF.Dimensions.DIM_4, 4);

			// OP SPACES
			List<Dimension> OP1Space = new ArrayList<Dimension>();
			OP1Space.add(markerSetDim);

			List<Dimension> OP2x4Space = new ArrayList<Dimension>();
			OP2x4Space.add(markerSetDim);
			OP2x4Space.add(boxes4Dim);

			// MARKER SPACES
			List<Dimension> markerNameSpace = new ArrayList<Dimension>();
			markerNameSpace.add(markerSetDim);
			markerNameSpace.add(markerStrideDim);

			List<Dimension> markerPropertySpace4 = new ArrayList<Dimension>();
			markerPropertySpace4.add(markerSetDim);
			markerPropertySpace4.add(dim4);

			// SAMPLE SPACES
			List<Dimension> sampleSetSpace = new ArrayList<Dimension>();
			sampleSetSpace.add(implicitSetDim);
			sampleSetSpace.add(sampleStrideDim);

			// ALLELES SPACES
			List<Dimension> allelesSpace = new ArrayList<Dimension>();
			allelesSpace.add(markerSetDim);
			allelesSpace.add(alleleStrideDim);

			// Define OP Variables
			ncfile.addVariable(cNetCDF.Variables.VAR_OPSET, DataType.CHAR, markerNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markerNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET, DataType.CHAR, sampleSetSpace);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, DataType.INT, OP2x4Space);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT, DataType.DOUBLE, OP1Space);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE, DataType.INT, OP1Space);
			ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, markerSetSize);

			// Define Genotype Variables
			//ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_KNOWNALLELES, DataType.CHAR, allelesSpace);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, DataType.BYTE, allelesSpace);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ, DataType.DOUBLE, OP1Space);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, DataType.BYTE, allelesSpace);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ, DataType.DOUBLE, OP1Space);
			ncfile.addVariable(cNetCDF.Variables.VAR_GT_STRAND, DataType.CHAR, markerPropertySpace4);
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return ncfile;
	}

	private static NetcdfFileWriteable generateNetcdfSampleQAHandler(
			StudyKey studyKey,
			String matrixName,
			String description,
			OPType opType,
			int sampleSetSize,
			int markerSetSize)
			throws InvalidRangeException, IOException
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			File pathToStudy = new File(Study.constructGTPath(studyKey));
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(pathToStudy);
			}

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;
			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;

			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyKey.getId());
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);

			// dimensions
			Dimension sampleSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, sampleSetSize);
			Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, markerSetSize);
			Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
			Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);

			// OP SPACES
			List<Dimension> OP1Space = new ArrayList<Dimension>();
			OP1Space.add(sampleSetDim);

			// SAMPLE SPACES
			List<Dimension> sampleSetSpace = new ArrayList<Dimension>();
			sampleSetSpace.add(sampleSetDim);
			sampleSetSpace.add(sampleStrideDim);

			// MARKER SPACES
			List<Dimension> markerSetSpace = new ArrayList<Dimension>();
			markerSetSpace.add(implicitSetDim);
			markerSetSpace.add(markerStrideDim);

			// Define OP Variables
			ncfile.addVariable(cNetCDF.Variables.VAR_OPSET, DataType.CHAR, sampleSetSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET, DataType.CHAR, markerSetSpace);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT, DataType.DOUBLE, OP1Space);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT, DataType.INT, OP1Space);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT, DataType.DOUBLE, OP1Space);
			ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, sampleSetSize);
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return ncfile;

	}

	private static NetcdfFileWriteable generateNetcdfCensusHandler(
			StudyKey studyKey,
			String matrixName,
			String description,
			OPType opType,
			int markerSetSize,
			int sampleSetSize)
			throws InvalidRangeException, IOException
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			File pathToStudy = new File(Study.constructGTPath(studyKey));
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(pathToStudy);
			}

			int gtStride = cNetCDF.Strides.STRIDE_GT;
			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyKey.getId());
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);

			// dimensions
			Dimension markerSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, markerSetSize);
			Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, sampleSetSize);
			Dimension boxes3Dim = ncfile.addDimension(cNetCDF.Dimensions.DIM_3BOXES, 3);
			Dimension boxes4Dim = ncfile.addDimension(cNetCDF.Dimensions.DIM_4BOXES, 4);
			Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
			Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
			Dimension gtStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_GTSTRIDE, gtStride);
			Dimension dim4 = ncfile.addDimension(cNetCDF.Dimensions.DIM_4, 4);

			// OP SPACES
//			List<Dimension> OP1Space = new ArrayList<Dimension>();
//			OP1Space.add(markerSetDim);

			List<Dimension> OP2x3Space = new ArrayList<Dimension>();
			OP2x3Space.add(markerSetDim);
			OP2x3Space.add(boxes3Dim);

			List<Dimension> OP2x4Space = new ArrayList<Dimension>();
			OP2x4Space.add(markerSetDim);
			OP2x4Space.add(boxes4Dim);

			// MARKER SPACES
			List<Dimension> markerNameSpace = new ArrayList<Dimension>();
			markerNameSpace.add(markerSetDim);
			markerNameSpace.add(markerStrideDim);

			List<Dimension> markerPropertySpace4 = new ArrayList<Dimension>();
			markerPropertySpace4.add(markerSetDim);
			markerPropertySpace4.add(dim4);

			// SAMPLE SPACES
			List<Dimension> sampleSetSpace = new ArrayList<Dimension>();
			sampleSetSpace.add(implicitSetDim);
			sampleSetSpace.add(sampleStrideDim);

			// ALLELES SPACES
			List<Dimension> allelesSpace = new ArrayList<Dimension>();
			allelesSpace.add(markerSetDim);
			allelesSpace.add(gtStrideDim);

			// Define OP Variables
			ncfile.addVariable(cNetCDF.Variables.VAR_OPSET, DataType.CHAR, markerNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markerNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET, DataType.CHAR, sampleSetSpace);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, DataType.INT, OP2x4Space);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE, DataType.INT, OP2x3Space);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL, DataType.INT, OP2x3Space);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW, DataType.INT, OP2x3Space);
			ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, markerSetSize);

			// Define Genotype Variables
			ncfile.addVariable(cNetCDF.Variables.VAR_ALLELES, DataType.CHAR, allelesSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_GT_STRAND, DataType.CHAR, markerPropertySpace4);
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return ncfile;
	}

	private static NetcdfFileWriteable generateNetcdfHardyWeinbergHandler(
			StudyKey studyKey,
			String matrixName,
			String description,
			OPType opType,
			int markerSetSize,
			int sampleSetSize)
			throws InvalidRangeException, IOException
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			File pathToStudy = new File(Study.constructGTPath(studyKey));
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(pathToStudy);
			}

			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyKey.getId());
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);

			// dimensions
			Dimension setDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, markerSetSize);
			Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, sampleSetSize);
			Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
			Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
			Dimension boxesDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_4BOXES, 2);

			// OP SPACES
			List<Dimension> OP1Space = new ArrayList<Dimension>();
			OP1Space.add(setDim);

			List<Dimension> OP2Space = new ArrayList<Dimension>();
			OP2Space.add(setDim);
			OP2Space.add(boxesDim);

			// MARKER SPACES
			List<Dimension> markerNameSpace = new ArrayList<Dimension>();
			markerNameSpace.add(setDim);
			markerNameSpace.add(markerStrideDim);

			// SAMPLE SPACES
			List<Dimension> sampleSetSpace = new ArrayList<Dimension>();
			sampleSetSpace.add(implicitSetDim);
			sampleSetSpace.add(sampleStrideDim);

			// Define OP Variables
			ncfile.addVariable(cNetCDF.Variables.VAR_OPSET, DataType.CHAR, markerNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markerNameSpace);
			ncfile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET, DataType.CHAR, sampleSetSpace);

//			ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL, DataType.DOUBLE, OP1Space);
//			ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE, DataType.DOUBLE, OP1Space);
			ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL, DataType.DOUBLE, OP1Space);
			ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT, DataType.DOUBLE, OP1Space);

//			ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALL, DataType.DOUBLE, OP2Space);
//			ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CASE, DataType.DOUBLE, OP2Space);
			ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL, DataType.DOUBLE, OP2Space);
			ncfile.addVariable(cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT, DataType.DOUBLE, OP2Space);

			ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, markerSetSize);
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return ncfile;
	}

	private static NetcdfFileWriteable generateNetcdfAssociationHandler(
			StudyKey studyKey,
			String matrixName,
			String description,
			OPType opType,
			int markerSetSize,
			int sampleSetSize,
			int chrSetSize)
			throws InvalidRangeException, IOException
	{

		final int boxDimensions;
		final String boxDimensionsName;
		final String ncVariableName;
		if (opType == OPType.ALLELICTEST) {
			boxDimensions = 3;
			boxDimensionsName = cNetCDF.Dimensions.DIM_3BOXES;
			ncVariableName = cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR;
		} else if (opType == OPType.GENOTYPICTEST) {
			boxDimensions = 4;
			boxDimensionsName = cNetCDF.Dimensions.DIM_4BOXES;
			ncVariableName = cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR;
		} else if (opType == OPType.COMBI_ASSOC_TEST) {
			boxDimensions = 4; // FIXME
			boxDimensionsName = cNetCDF.Dimensions.DIM_4BOXES; // FIXME
			ncVariableName = cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR; // FIXME
		} else {
			throw new IOException("Unsupported operation type " + opType.name());
		}

		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			File pathToStudy = new File(Study.constructGTPath(studyKey));
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(pathToStudy);
			}

			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyKey.getId());
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);

			// dimensions
			Dimension setDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, markerSetSize);
			Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, sampleSetSize);
			Dimension chrSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_CHRSET, chrSetSize);
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

			ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, markerSetSize);
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return ncfile;
	}

	private static NetcdfFileWriteable generateNetcdfTrendTestHandler(
			StudyKey studyKey,
			String matrixName,
			String description,
			OPType opType,
			int markerSetSize,
			int sampleSetSize,
			int chrSetSize)
			throws InvalidRangeException, IOException
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			File pathToStudy = new File(Study.constructGTPath(studyKey));
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(pathToStudy);
			}

			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyKey.getId());
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);

			// dimensions
			Dimension setDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, markerSetSize);
			Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, sampleSetSize);
			Dimension chrSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_CHRSET, chrSetSize);
			Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
			Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
			Dimension box2Dim = ncfile.addDimension(cNetCDF.Dimensions.DIM_2BOXES, 2);
			Dimension dim8 = ncfile.addDimension(cNetCDF.Dimensions.DIM_8, 8);
			Dimension dim4 = ncfile.addDimension(cNetCDF.Dimensions.DIM_4, 4);

			// OP SPACES
			List<Dimension> OP2Space = new ArrayList<Dimension>();
			OP2Space.add(setDim);
			OP2Space.add(box2Dim);

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

			ncfile.addVariable(cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP, DataType.DOUBLE, OP2Space);

			ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, markerSetSize);
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return ncfile;
	}
}

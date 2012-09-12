package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OperationFactory {

	private final static Logger log = LoggerFactory.getLogger(OperationFactory.class);

	private NetcdfFileWriteable netCDFHandler = null;
	private String resultOPnetCDFName = "";
	private String resultOPType = "";
	private int resultOPId = Integer.MIN_VALUE;
	private OperationMetadata opMetaData = null;

	/**
	 * To use with matrix input.
	 */
	public OperationFactory(Integer studyId,
			String friendlyName,
			String description,
			int opSetSize,
			int implicitSetSize,
			int chrSetSize,
			String OPType,
			int parentMatrixId,
			int parentOperationId)
			throws InvalidRangeException, IOException
	{
		// OPERATION CASE SELECTOR
		resultOPnetCDFName = OPType + "_" + org.gwaspi.netCDF.matrices.MatrixManager.generateMatrixNetCDFNameByDate();
		switch (cNetCDF.Defaults.OPType.compareTo(OPType)) {
			case MARKER_QA:
				//resultOPnetCDFName = OPType + "_" + rdMatrixMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfMarkerQAHandler(studyId,
						resultOPnetCDFName,
						description,
						OPType,
						opSetSize,
						implicitSetSize);
				break;
			case SAMPLE_QA:
				//resultOPnetCDFName = OPType + "_" + rdMatrixMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfSampleQAHandler(studyId,
						resultOPnetCDFName,
						description,
						OPType,
						opSetSize,
						implicitSetSize);
				break;
			case MARKER_CENSUS_BY_AFFECTION:
				//resultOPnetCDFName = OPType + "_" + org.gwaspi.netCDF.matrices.MatrixManager.generateMatrixNetCDFNameByDate();
				netCDFHandler = generateNetcdfCensusHandler(studyId,
						resultOPnetCDFName,
						description,
						OPType,
						opSetSize,
						implicitSetSize);
				break;
			case MARKER_CENSUS_BY_PHENOTYPE:
				//resultOPnetCDFName = OPType + "_" + org.gwaspi.netCDF.matrices.MatrixManager.generateMatrixNetCDFNameByDate();
				netCDFHandler = generateNetcdfCensusHandler(studyId,
						resultOPnetCDFName,
						description,
						OPType,
						opSetSize,
						implicitSetSize);
				break;
			case HARDY_WEINBERG:
				OperationMetadata rdOPMetadata = new OperationMetadata(parentOperationId);
				//resultOPnetCDFName = OPType + "_" + rdOPMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfHardyWeinbergHandler(studyId,
						resultOPnetCDFName,
						description,
						OPType,
						opSetSize,
						implicitSetSize);
				break;
			case ALLELICTEST:
				rdOPMetadata = new OperationMetadata(parentOperationId);
				//resultOPnetCDFName = OPType + "_" + rdOPMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfAllelicAssociationHandler(studyId,
						resultOPnetCDFName,
						description,
						OPType,
						opSetSize,
						implicitSetSize,
						chrSetSize);
				break;
			case GENOTYPICTEST:
				rdOPMetadata = new OperationMetadata(parentOperationId);
				//resultOPnetCDFName = OPType + "_" + rdOPMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfGenotypicAssociationHandler(studyId,
						resultOPnetCDFName,
						description,
						OPType,
						opSetSize,
						implicitSetSize,
						chrSetSize);
				break;
			case TRENDTEST:
				rdOPMetadata = new OperationMetadata(parentOperationId);
				//resultOPnetCDFName = OPType + "_" + rdOPMetadata.getMatrixCDFName();
				netCDFHandler = generateNetcdfTrendTestHandler(studyId,
						resultOPnetCDFName,
						description,
						OPType,
						opSetSize,
						implicitSetSize,
						chrSetSize);
				break;
			default:
				throw new IllegalArgumentException("invalid OPType: " + OPType);
		}

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		org.gwaspi.netCDF.operations.OperationManager.insertOPMetadata(dBManager,
				parentMatrixId,
				parentOperationId,
				friendlyName,
				resultOPnetCDFName,
				OPType,
				"",
				description,
				studyId);

		opMetaData = new OperationMetadata(resultOPnetCDFName);

		resultOPId = opMetaData.getOPId();
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
		return resultOPId;
	}

	public OperationMetadata getResultOPMetadata() {
		return opMetaData;
	}

	public static NetcdfFileWriteable generateNetcdfMarkerQAHandler(
			Integer studyId,
			String resultOPName,
			String description,
			String OPType,
			int markerSetSize,
			int sampleSetSize)
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
			File pathToStudy = new File(genotypesFolder + "/STUDY_" + studyId);
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(genotypesFolder, "/STUDY_" + studyId);
			}

			int gtStride = cNetCDF.Strides.STRIDE_GT;
			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + resultOPName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyId);
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
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, DataType.CHAR, allelesSpace);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ, DataType.DOUBLE, OP1Space);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, DataType.CHAR, allelesSpace);
			ncfile.addVariable(cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ, DataType.DOUBLE, OP1Space);
			ncfile.addVariable(cNetCDF.Variables.VAR_GT_STRAND, DataType.CHAR, markerPropertySpace4);
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return ncfile;
	}

	public static NetcdfFileWriteable generateNetcdfSampleQAHandler(Integer studyId,
			String matrixName,
			String description,
			String OPType,
			int sampleSetSize,
			int markerSetSize)
			throws InvalidRangeException, IOException
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
			File pathToStudy = new File(genotypesFolder + "/STUDY_" + studyId);
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(genotypesFolder, "/STUDY_" + studyId);
			}

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;
			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;

			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyId);
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

	public static NetcdfFileWriteable generateNetcdfCensusHandler(Integer studyId,
			String matrixName,
			String description,
			String OPType,
			int markerSetSize,
			int sampleSetSize)
			throws InvalidRangeException, IOException
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
			File pathToStudy = new File(genotypesFolder + "/STUDY_" + studyId);
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(genotypesFolder, "/STUDY_" + studyId);
			}

			int gtStride = cNetCDF.Strides.STRIDE_GT;
			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyId);
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

	public static NetcdfFileWriteable generateNetcdfHardyWeinbergHandler(Integer studyId,
			String matrixName,
			String description,
			String OPType,
			int markerSetSize,
			int sampleSetSize)
			throws InvalidRangeException, IOException
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
			File pathToStudy = new File(genotypesFolder + "/STUDY_" + studyId);
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(genotypesFolder, "/STUDY_" + studyId);
			}

			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyId);
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

	public static NetcdfFileWriteable generateNetcdfAllelicAssociationHandler(Integer studyId,
			String matrixName,
			String description,
			String OPType,
			int markerSetSize,
			int sampleSetSize,
			int chrSetSize)
			throws InvalidRangeException, IOException
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
			File pathToStudy = new File(genotypesFolder + "/STUDY_" + studyId);
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(genotypesFolder, "/STUDY_" + studyId);
			}

			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyId);
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);

			// dimensions
			Dimension setDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, markerSetSize);
			Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, sampleSetSize);
			Dimension chrSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_CHRSET, chrSetSize);
			Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
			Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
			Dimension box2Dim = ncfile.addDimension(cNetCDF.Dimensions.DIM_2BOXES, 2);
			Dimension box3Dim = ncfile.addDimension(cNetCDF.Dimensions.DIM_3BOXES, 3);
			Dimension dim8 = ncfile.addDimension(cNetCDF.Dimensions.DIM_8, 8);
			Dimension dim4 = ncfile.addDimension(cNetCDF.Dimensions.DIM_4, 4);

			// OP SPACES
//			List<Dimension> OP2Space = new ArrayList<Dimension>();
//			OP2Space.add(setDim);
//			OP2Space.add(box2Dim);

			List<Dimension> OP3Space = new ArrayList<Dimension>();
			OP3Space.add(setDim);
			OP3Space.add(box3Dim);

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

			//ncfile.addVariable(cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR, DataType.DOUBLE, OP2Space);
			ncfile.addVariable(cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR, DataType.DOUBLE, OP3Space);

			ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, markerSetSize);
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return ncfile;
	}

	public static NetcdfFileWriteable generateNetcdfGenotypicAssociationHandler(Integer studyId,
			String matrixName,
			String description,
			String OPType,
			int markerSetSize,
			int sampleSetSize,
			int chrSetSize)
			throws InvalidRangeException, IOException
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
			File pathToStudy = new File(genotypesFolder + "/STUDY_" + studyId);
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(genotypesFolder, "/STUDY_" + studyId);
			}

			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyId);
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);

			// dimensions
			Dimension setDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, markerSetSize);
			Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, sampleSetSize);
			Dimension chrSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_CHRSET, chrSetSize);
			Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
			Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
			Dimension box4Dim = ncfile.addDimension(cNetCDF.Dimensions.DIM_4BOXES, 4);
			Dimension dim8 = ncfile.addDimension(cNetCDF.Dimensions.DIM_8, 8);
			Dimension dim4 = ncfile.addDimension(cNetCDF.Dimensions.DIM_4, 4);

			// OP SPACES
			List<Dimension> OP4Space = new ArrayList<Dimension>();
			OP4Space.add(setDim);
			OP4Space.add(box4Dim);

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

			ncfile.addVariable(cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR, DataType.DOUBLE, OP4Space);

			ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, markerSetSize);
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return ncfile;
	}

	public static NetcdfFileWriteable generateNetcdfTrendTestHandler(Integer studyId,
			String matrixName,
			String description,
			String OPType,
			int markerSetSize,
			int sampleSetSize,
			int chrSetSize)
			throws InvalidRangeException, IOException
	{
		NetcdfFileWriteable ncfile = null;
		try {
			// CREATE netCDF-3 FILE
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
			File pathToStudy = new File(genotypesFolder + "/STUDY_" + studyId);
			if (!pathToStudy.exists()) {
				org.gwaspi.global.Utils.createFolder(genotypesFolder, "/STUDY_" + studyId);
			}

			int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
			int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

			String writeFileName = pathToStudy + "/" + matrixName + ".nc";
			ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

			// global attributes
			ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyId);
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

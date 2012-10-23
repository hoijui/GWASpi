package org.gwaspi.netCDF.matrices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Config;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
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
public class MatrixFactory {

	private NetcdfFileWriteable netCDFHandler = null;
	private String resultMatrixName = "";
	private int resultMatrixId = Integer.MIN_VALUE;
	private MatrixMetadata matrixMetaData = null;

	private MatrixFactory(
			int studyId,
			ImportFormat technology,
			String friendlyName,
			String description,
			GenotypeEncoding matrixType,
			StrandType strand,
			boolean hasDictionary,
			int samplesDimSize,
			int markerDimSize,
			int chrDimSize,
			int origMatrix1Id,
			int origMatrix2Id,
			String inputLocation)
			throws InvalidRangeException, IOException
	{
		if (samplesDimSize > 0 && markerDimSize > 0) {
			resultMatrixName = MatricesList.generateMatrixNetCDFNameByDate();
			netCDFHandler = generateNetcdfHandler(
					studyId,
					resultMatrixName,
					technology,
					description,
					matrixType,
					strand,
					hasDictionary,
					samplesDimSize,
					markerDimSize,
					chrDimSize);

			// CHECK IF JVM IS 32/64 bits to use LFS or not
			int JVMbits = Integer.parseInt(System.getProperty("sun.arch.data.model", "32"));
			if (JVMbits == 64) {
				netCDFHandler.setLargeFile(true);
			}
			netCDFHandler.setFill(true);

			MatricesList.insertMatrixMetadata(new MatrixMetadata(
					friendlyName,
					resultMatrixName,
					description,
					matrixType,
					studyId,
					origMatrix1Id,
					origMatrix2Id,
					inputLocation));

			matrixMetaData = MatricesList.getMatrixMetadataByNetCDFname(resultMatrixName);

			resultMatrixId = matrixMetaData.getMatrixId();
		}
	}

	/**
	 * Constructor to use with matrix input
	 */
	public MatrixFactory(
			int studyId,
			ImportFormat technology,
			String friendlyName,
			String description,
			GenotypeEncoding matrixType,
			StrandType strand,
			boolean hasDictionary,
			int samplesDimSize,
			int markerDimSize,
			int chrDimSize,
			int origMatrix1Id,
			int origMatrix2Id)
			throws InvalidRangeException, IOException
	{
		this(
				studyId,
				technology,
				friendlyName,
				description,
				matrixType,
				strand,
				hasDictionary,
				samplesDimSize,
				markerDimSize,
				chrDimSize,
				origMatrix1Id,
				origMatrix2Id,
				"Matrix is result of " + origMatrix1Id);
	}

	/**
	 * Constructor to use with file input
	 */
	public MatrixFactory(
			int studyId,
			ImportFormat technology,
			String friendlyName,
			String description,
			GenotypeEncoding matrixType,
			StrandType strand,
			boolean hasDictionary,
			int samplesDimSize,
			int markerDimSize,
			int chrDimSize,
			String dataLocation)
			throws InvalidRangeException, IOException
	{
		this(
				studyId,
				technology,
				friendlyName,
				description,
				matrixType,
				strand,
				hasDictionary,
				samplesDimSize,
				markerDimSize,
				chrDimSize,
				-1,
				-1,
				dataLocation);
	}

	// ACCESSORS
	public NetcdfFileWriteable getNetCDFHandler() {
		return netCDFHandler;
	}

	public String getResultMatrixName() {
		return resultMatrixName;
	}

	public int getResultMatrixId() {
		return resultMatrixId;
	}

	public MatrixMetadata getResultMatrixMetadata() {
		return matrixMetaData;
	}

	public static NetcdfFileWriteable generateNetcdfHandler(
			Integer studyId,
			String matrixName,
			ImportFormat technology,
			String description,
			GenotypeEncoding matrixType,
			StrandType strand,
			boolean hasDictionary,
			int sampleSetSize,
			int markerSetSize,
			int chrSetSize)
			throws InvalidRangeException, IOException
	{
		// CREATE netCDF-3 FILE
		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
		File pathToStudy = new File(genotypesFolder + "/STUDY_" + studyId);
		if (!pathToStudy.exists()) {
			org.gwaspi.global.Utils.createFolder(genotypesFolder, "/STUDY_" + studyId);
		}

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
		int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;
//		int strandStride = cNetCDF.Strides.STRIDE_STRAND;

		String writeFileName = pathToStudy + "/" + matrixName + ".nc";
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

		// global attributes
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyId);
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY, technology.toString());
		String versionNb = Config.getConfigValue(Config.PROPERTY_CURRENT_GWASPIDB_VERSION, "2.0.2");
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_GWASPIDB_VERSION, versionNb);
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STRAND, strand.toString());
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY, hasDictionary ? 1 : 0);

		// dimensions
		Dimension samplesDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESET, 0, true, true, false);
		Dimension markersDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSET, markerSetSize);
		Dimension chrSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_CHRSET, chrSetSize);
		Dimension gtStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_GTSTRIDE, gtStride);
		Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
		Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
//		Dimension dim32 = ncfile.addDimension(cNetCDF.Dimensions.DIM_32, 32);
//		Dimension dim16 = ncfile.addDimension(cNetCDF.Dimensions.DIM_16, 16);
		Dimension dim8 = ncfile.addDimension(cNetCDF.Dimensions.DIM_8, 8);
		Dimension dim4 = ncfile.addDimension(cNetCDF.Dimensions.DIM_4, 4);
		Dimension dim2 = ncfile.addDimension(cNetCDF.Dimensions.DIM_2, 2);
		Dimension dim1 = ncfile.addDimension(cNetCDF.Dimensions.DIM_1, 1);

		// GENOTYPE SPACES
		List<Dimension> genotypeSpace = new ArrayList<Dimension>();
		genotypeSpace.add(samplesDim);
		genotypeSpace.add(markersDim);
		genotypeSpace.add(gtStrideDim);

		// MARKER SPACES
		List<Dimension> markerNameSpace = new ArrayList<Dimension>();
		markerNameSpace.add(markersDim);
		markerNameSpace.add(markerStrideDim);

		List<Dimension> markerPositionSpace = new ArrayList<Dimension>();
		markerPositionSpace.add(markersDim);

		List<Dimension> markerPropertySpace8 = new ArrayList<Dimension>();
		markerPropertySpace8.add(markersDim);
		markerPropertySpace8.add(dim8);

		List<Dimension> markerPropertySpace4 = new ArrayList<Dimension>();
		markerPropertySpace4.add(markersDim);
		markerPropertySpace4.add(dim4);

		List<Dimension> markerPropertySpace2 = new ArrayList<Dimension>();
		markerPropertySpace2.add(markersDim);
		markerPropertySpace2.add(dim2);

		// CHROMOSOME SPACES
		List<Dimension> chrSetSpace = new ArrayList<Dimension>();
		chrSetSpace.add(chrSetDim);
		chrSetSpace.add(dim8);

		List<Dimension> chrInfoSpace = new ArrayList<Dimension>();
		chrInfoSpace.add(chrSetDim);
		chrInfoSpace.add(dim4);

		// SAMPLE SPACES
		List<Dimension> sampleSetSpace = new ArrayList<Dimension>();
		sampleSetSpace.add(samplesDim);
		sampleSetSpace.add(sampleStrideDim);

		// OTHER SPACES
		List<Dimension> gtEncodingSpace = new ArrayList<Dimension>();
		gtEncodingSpace.add(dim1);
		gtEncodingSpace.add(dim8);

		// Define Marker Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERSET, DataType.CHAR, markerNameSpace);
		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_MARKERSET, cNetCDF.Attributes.LENGTH, markerSetSize);

		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markerNameSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_CHR, DataType.CHAR, markerPropertySpace8);
		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_POS, DataType.INT, markerPositionSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, DataType.CHAR, markerPropertySpace2);

		// Define Chromosome Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX, DataType.CHAR, chrSetSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_CHR_INFO, DataType.INT, chrInfoSpace);

		// Define Sample Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLESET, DataType.CHAR, sampleSetSpace);
		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_SAMPLESET, cNetCDF.Attributes.LENGTH, sampleSetSize);

		// Define Genotype Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_GENOTYPES, DataType.BYTE, genotypeSpace);
		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_GENOTYPES, cNetCDF.Attributes.GLOB_STRAND, "");
		ncfile.addVariable(cNetCDF.Variables.VAR_GT_STRAND, DataType.CHAR, markerPropertySpace4);

		// ENCODING VARIABLE
		ncfile.addVariable(cNetCDF.Variables.GLOB_GTENCODING, DataType.CHAR, gtEncodingSpace);

		return ncfile;
	}

	public MatrixMetadata getMatrixMetaData() {
		return matrixMetaData;
	}
}

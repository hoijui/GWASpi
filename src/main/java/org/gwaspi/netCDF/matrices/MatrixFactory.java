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

package org.gwaspi.netCDF.matrices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Config;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.markers.NetCDFDataSetSource;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class MatrixFactory {

	private final NetcdfFileWriteable netCDFHandler;
	private final String resultMatrixName;
	private final MatrixKey resultMatrixKey;
	private final MatrixMetadata matrixMetaData;

	private MatrixFactory(
			ImportFormat technology,
			String friendlyName,
			String description,
			GenotypeEncoding matrixType,
			StrandType strand,
			boolean hasDictionary,
			int samplesDimSize,
			int markerDimSize,
			int chrDimSize,
			MatrixKey origMatrix1Key,
			MatrixKey origMatrix2Key,
			String inputLocation)
			throws IOException
	{
		if (samplesDimSize > 0 && markerDimSize > 0) {
			try {
			resultMatrixName = generateMatrixNetCDFNameByDate();
			netCDFHandler = generateNetcdfHandler(
					origMatrix1Key.getStudyKey(),
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

			MatrixMetadata tmpMatrixMetaData = new MatrixMetadata(
					friendlyName,
					resultMatrixName,
					description,
					matrixType,
					origMatrix1Key.getStudyKey(),
					origMatrix1Key.getMatrixId(),
					origMatrix2Key.getMatrixId(),
					inputLocation);
			resultMatrixKey = MatricesList.insertMatrixMetadata(tmpMatrixMetaData);
			matrixMetaData = tmpMatrixMetaData;
			} catch (InvalidRangeException ex) {
				throw new IOException(ex);
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Constructor to use with matrix input
	 */
	public MatrixFactory(
			ImportFormat technology,
			String friendlyName,
			String description,
			GenotypeEncoding matrixType,
			StrandType strand,
			boolean hasDictionary,
			int samplesDimSize,
			int markerDimSize,
			int chrDimSize,
			MatrixKey origMatrixKey1,
			MatrixKey origMatrixKey2)
			throws IOException
	{
		this(
				technology,
				friendlyName,
				description,
				matrixType,
				strand,
				hasDictionary,
				samplesDimSize,
				markerDimSize,
				chrDimSize,
				origMatrixKey1,
				origMatrixKey2,
				"Matrix is result of " + origMatrixKey1);
	}

	/**
	 * Constructor to use with file input
	 */
	public MatrixFactory(
			StudyKey studyKey,
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
			throws IOException
	{
		this(
				technology,
				friendlyName,
				description,
				matrixType,
				strand,
				hasDictionary,
				samplesDimSize,
				markerDimSize,
				chrDimSize,
				new MatrixKey(studyKey, Integer.MIN_VALUE),
				new MatrixKey(studyKey, Integer.MIN_VALUE),
				dataLocation);
	}

	// ACCESSORS
	public NetcdfFileWriteable getNetCDFHandler() {
		return netCDFHandler;
	}

	public String getResultMatrixName() {
		return resultMatrixName;
	}

	/**
	 * @deprecated
	 */
	public int getResultMatrixId() {
		return resultMatrixKey.getMatrixId();
	}

	public MatrixKey getResultMatrixKey() {
		return resultMatrixKey;
	}

	public MatrixMetadata getResultMatrixMetadata() {
		return matrixMetaData;
	}

	public static NetcdfFileWriteable generateNetcdfHandler(
			StudyKey studyKey,
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
		File pathToStudy = new File(Study.constructGTPath(studyKey));
		if (!pathToStudy.exists()) {
			org.gwaspi.global.Utils.createFolder(pathToStudy);
		}

		int gtStride = cNetCDF.Strides.STRIDE_GT;
		int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
		int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;
//		int strandStride = cNetCDF.Strides.STRIDE_STRAND;

		File writeFile = new File(pathToStudy, matrixName + ".nc");
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFile.getAbsolutePath(), false);

		// global attributes
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyKey.getId());
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY, technology.toString());
		String versionNb = Config.getConfigValue(Config.PROPERTY_CURRENT_GWASPIDB_VERSION, null);
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

	public static String generateMatrixNetCDFNameByDate(Date date) {

		String matrixName = "GT_";
		matrixName += org.gwaspi.global.Utils.getShortDateTimeForFileName(date);
		matrixName = matrixName.replace(":", "");
		matrixName = matrixName.replace(" ", "");
		matrixName = matrixName.replace("/", "");
//		matrixName = matrixName.replaceAll("[a-zA-Z]", "");
//		matrixName = matrixName.substring(0, matrixName.length() - 3); // Remove "CET" from name

		return matrixName;
	}

	public static DataSetSource generateMatrixDataSetSource(MatrixKey matrixKey) {

		try {
			return new NetCDFDataSetSource(matrixKey);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}

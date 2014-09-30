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

package org.gwaspi.datasource.netcdf;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.ChromosomesInfosSource;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * TODO add description
 * TODO rename to MatrixNetCdfDataSetSource
 */
public class NetCDFDataSetSource implements DataSetSource {

	private static final Logger LOG
			= LoggerFactory.getLogger(NetCDFDataSetSource.class);

	private final File netCDFpath;
	private NetcdfFile rdNetCdfFile;
	private StudyKey studyKey;
	private MatrixKey matrixKey;
	private MatrixMetadata matrixMetadata;

	public NetCDFDataSetSource(MatrixKey matrixKey) throws IOException {

		this.matrixKey = matrixKey;
		this.matrixMetadata = MatricesList.getMatrixMetadataById(matrixKey);
		this.netCDFpath = MatrixMetadata.generatePathToNetCdfFile(matrixMetadata);
		this.rdNetCdfFile = null;
	}

	public NetCDFDataSetSource(File netCDFpath, MatrixKey matrixKey) throws IOException {

		this.netCDFpath = netCDFpath;
		this.rdNetCdfFile = null;
		this.studyKey = null;
		this.matrixKey = matrixKey;
		this.matrixMetadata = null;
	}

	public NetCDFDataSetSource(File netCDFpath, StudyKey studyKey) throws IOException {

		this.netCDFpath = netCDFpath;
		this.rdNetCdfFile = null;
		this.studyKey = studyKey;
		this.matrixKey = null;
		this.matrixMetadata = null;
	}

//	public NetCDFDataSetSource(File netCDFpath) throws IOException {
//		this(netCDFpath, null);
//	}

	private NetcdfFile getReadNetCdfFile() throws IOException {

		ensureReadNetCdfFile();
		return rdNetCdfFile;
	}

	private void ensureReadNetCdfFile() throws IOException {

		if (rdNetCdfFile == null) {
			rdNetCdfFile = NetcdfFile.open(netCDFpath.getAbsolutePath());
		}
	}

	private void ensureMatrixMetadata() throws IOException {

		if (matrixMetadata == null) {
			ensureReadNetCdfFile();
			matrixMetadata = loadMatrixMetadata(netCDFpath, null, studyKey, null);
			matrixKey = MatrixKey.valueOf(matrixMetadata);
		}
	}

	@Override
	public DataSetSource getOriginDataSetSource() throws IOException {
		return this;
	}

	@Override
	public MatrixMetadata getMatrixMetadata() throws IOException {

		ensureMatrixMetadata();
		return matrixMetadata;
	}

//	private static MatrixMetadata completeMatricesTable(MatrixMetadata toCompleteMatrixMetadata) throws IOException {
//		String pathToStudy = Study.constructGTPath(toCompleteMatrixMetadata.getKey().getStudyKey());
//		String pathToMatrix = pathToStudy + toCompleteMatrixMetadata.getMatrixNetCDFName() + ".nc";
//		return loadMatrixMetadataFromFile(
//				toCompleteMatrixMetadata.getMatrixId(),
//				toCompleteMatrixMetadata.getFriendlyName(),
//				toCompleteMatrixMetadata.getMatrixNetCDFName(),
//				new StudyKey(toCompleteMatrixMetadata.getStudyId()),
//				pathToMatrix,
//				toCompleteMatrixMetadata.getDescription(),
//				toCompleteMatrixMetadata.getMatrixType(),
//				toCompleteMatrixMetadata.getCreationDate());
//	}

	public static MatrixMetadata loadMatrixMetadata(
			File netCDFFile)
			throws IOException
	{
		return loadMatrixMetadata(netCDFFile, null, null, null);
	}

    /**
	 * This method loads all the matrix meta-data
	 * from a GWASpi NetCdf file ("*.nc") that contains matrix data.
	 * @param newMatrixFriendlyName may be null
	 */
	public static MatrixMetadata loadMatrixMetadata(
			File netCDFFile,
			String newMatrixFriendlyName,
			StudyKey studyKey,
			MatrixKey matrixKey)
			throws IOException
	{
		NetcdfFile ncFile = NetcdfFile.open(netCDFFile.getAbsolutePath());
		return loadMatrixMetadata(ncFile, newMatrixFriendlyName, studyKey, matrixKey);
	}

	public static MatrixMetadata loadMatrixMetadata(
			NetcdfFile netCDFFile,
			String newMatrixFriendlyName,
			StudyKey studyKey,
			MatrixKey matrixKey)
			throws IOException
	{
		int studyId = StudyKey.NULL_ID;
		int matrixId = MatrixKey.NULL_ID;
		String friendlyName = "";
		String description = "";
		String gwaspiDBVersion = "";
		ImportFormat technology = ImportFormat.UNKNOWN;
		GenotypeEncoding gtEncoding = GenotypeEncoding.UNKNOWN;
		StrandType strand = StrandType.UNKNOWN;
		boolean hasDictionray = false;
		int numMarkers = Integer.MIN_VALUE;
		int numSamples = Integer.MIN_VALUE;
		int numChromosomes = Integer.MIN_VALUE;
		String matrixType = null;
		Date creationDate = null;

		try {
			try {
				studyId = netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY).getNumericValue().intValue();
			} catch (Exception ex) {
				LOG.error("No matrix-ID stored in matrix NetCDF file: " + netCDFFile.getLocation(), ex);
			}

			try {
				matrixId = netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_MATRIX_ID).getNumericValue().intValue();
			} catch (Exception ex) {
				LOG.error("No matrix-ID stored in matrix NetCDF file: " + netCDFFile.getLocation(), ex);
			}

			try {
				friendlyName = netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_FRIENDLY_NAME).getStringValue();
			} catch (Exception ex) {
				LOG.error("No friendly name stored in matrix NetCDF file: " + netCDFFile.getLocation(), ex);
			}

			try {
				description = netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION).getStringValue();
			} catch (Exception ex) {
				LOG.error("No description stored in matrix NetCDF file: " + netCDFFile.getLocation(), ex);
			}

			technology = ImportFormat.compareTo(netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY).getStringValue());

			try {
				gwaspiDBVersion = netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GWASPIDB_VERSION).getStringValue();
			} catch (Exception ex) {
				LOG.error(null, ex);
			}

			Variable var = netCDFFile.findVariable(cNetCDF.Variables.GLOB_GTENCODING);
			if (var != null) {
				try {
					ArrayChar.D2 gtCodeAC = (ArrayChar.D2) var.read("(0:0:1, 0:7:1)");
//					gtEncoding = GenotypeEncoding.valueOf(gtCodeAC.getString(0));
					gtEncoding = GenotypeEncoding.compareTo(gtCodeAC.getString(0)); // HACK, the above was used before
				} catch (InvalidRangeException ex) {
					LOG.error(null, ex);
				}
			}

			String strandStr = netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_STRAND).getStringValue();
			strand = StrandType.fromString(strandStr);
			hasDictionray = ((Integer) netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY).getNumericValue() != 0);

			Dimension markersDim = netCDFFile.findDimension(cNetCDF.Dimensions.DIM_MARKERSET);
			numMarkers = markersDim.getLength();

			Dimension samplesDim = netCDFFile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
			numSamples = samplesDim.getLength();

			Dimension chromosomesDim = netCDFFile.findDimension(cNetCDF.Dimensions.DIM_CHRSET);
			numChromosomes = chromosomesDim.getLength();

			try {
				matrixType = netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_MATRIX_TYPE).getStringValue();
			} catch (Exception ex) {
				LOG.error("No description stored in matrix NetCDF file: " + netCDFFile.getLocation(), ex);
			}

			try {
				creationDate = new Date(Long.parseLong(netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_CREATION_DATE).getStringValue()));
			} catch (Exception ex) {
				LOG.error("No friendly name stored in matrix NetCDF file: " + netCDFFile.getLocation(), ex);
			}
		} catch (IOException ex) {
			LOG.error("Cannot open file: " + netCDFFile, ex);
		} finally {
			if (null != netCDFFile) {
				try {
					netCDFFile.close();
				} catch (IOException ex) {
					LOG.warn("Cannot close file: " + netCDFFile, ex);
				}
			}
		}

		String simpleName = new File(netCDFFile.getLocation()).getName();
		simpleName = simpleName.substring(0, simpleName.lastIndexOf('.')); // cut off the '.nc' file extension

		// Use the values from the NetCDF file only if none were provided!
		if (matrixKey == null) {
			if (studyKey == null) {
				studyKey = new StudyKey(studyId);
				matrixKey = new MatrixKey(studyKey, matrixId);
			} else {
				matrixKey = new MatrixKey(studyKey, MatrixKey.NULL_ID);
			}
		}

		MatrixMetadata matrixMetadata = new MatrixMetadata(
			matrixKey,
			friendlyName,
			simpleName,
			technology,
			gwaspiDBVersion,
			description,
			gtEncoding,
			strand,
			hasDictionray,
			numMarkers,
			numSamples,
			numChromosomes,
			matrixType,
			creationDate);

		return matrixMetadata;
	}

	@Override
	public MarkersGenotypesSource getMarkersGenotypesSource() throws IOException {
		return NetCdfMarkersGenotypesSource.createForMatrix(getReadNetCdfFile());
	}

	@Override
	public MarkersMetadataSource getMarkersMetadatasSource() throws IOException {
		return NetCdfMarkersMetadataSource.createForMatrix(getReadNetCdfFile());
	}

	@Override
	public int getNumMarkers() throws IOException {

		ensureMatrixMetadata();
		return matrixMetadata.getNumMarkers();
	}

	@Override
	public int getNumChromosomes() throws IOException {

		ensureMatrixMetadata();
		return matrixMetadata.getNumChromosomes();
	}

	@Override
	public int getNumSamples() throws IOException {

		ensureMatrixMetadata();
		return matrixMetadata.getNumSamples();
	}

	@Override
	public ChromosomesKeysSource getChromosomesKeysSource() throws IOException {
		return NetCdfChromosomesKeysSource.createForMatrix(getReadNetCdfFile());
	}

	@Override
	public ChromosomesInfosSource getChromosomesInfosSource() throws IOException {
		return NetCdfChromosomesInfosSource.createForMatrix(getReadNetCdfFile());
	}

	@Override
	public MarkersKeysSource getMarkersKeysSource() throws IOException {
		return NetCdfMarkersKeysSource.createForMatrix(getReadNetCdfFile());
	}

	@Override
	public SamplesGenotypesSource getSamplesGenotypesSource() throws IOException {
		return NetCdfSamplesGenotypesSource.createForMatrix(getReadNetCdfFile());
	}

	@Override
	public SamplesInfosSource getSamplesInfosSource() throws IOException {

		ensureMatrixMetadata();
		return NetCdfSamplesInfosSource.createForMatrix(matrixKey.getStudyKey(), getReadNetCdfFile());
	}

	@Override
	public SamplesKeysSource getSamplesKeysSource() throws IOException {

		ensureMatrixMetadata();
		return NetCdfSamplesKeysSource.createForMatrix(matrixKey.getStudyKey(), getReadNetCdfFile());
	}
}

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

package org.gwaspi.netCDF.markers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.ChromosomesInfosSource;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.model.StudyKey;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * TODO
 */
public class NetCDFDataSetSource implements DataSetSource {

	private static final Logger LOG
			= LoggerFactory.getLogger(NetCDFDataSetSource.class);

	private final StudyKey studyKey;
	private final File netCDFpath;
	private MatrixKey matrixKey;
	private MatrixMetadata matrixMetadata;
//	private MatrixKey matrixKey;
//	private MarkerSet markerSet;
//	private SampleSet sampleSet;

//	public NetCDFDataSetSource(MatrixKey matrixKey) throws IOException {
//
//		this.netCDFpath = null;
//		this.matrixKey = matrixKey;
//		this.markerSet = new MarkerSet(matrixKey);
//		this.sampleSet = new SampleSet(matrixKey);
//	}

	public NetCDFDataSetSource(StudyKey studyKey, File netCDFpath) throws IOException {

		this.studyKey = studyKey;
		this.netCDFpath = netCDFpath;
		this.matrixKey = null;
		this.matrixMetadata = null;
	}

	private void ensureMatrixMetadata() throws IOException {

		if (matrixMetadata == null) {
			matrixMetadata = loadMatrixMetadata(studyKey, netCDFpath, null);
			matrixKey = MatrixKey.valueOf(matrixMetadata);
		}
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

    /**
	 * This method loads all the matrix meta-data
	 * from a GWASpi NetCdf file ("*.nc") that contains matrix data.
	 * @param newMatrixFriendlyName may be null
	 */
	public static MatrixMetadata loadMatrixMetadata(
			StudyKey studyKey,
			File netCDFFile,
			String newMatrixFriendlyName)
			throws IOException
	{
		String friendlyName = "";
		String description = "";
		String gwaspiDBVersion = "";
		cImport.ImportFormat technology = cImport.ImportFormat.UNKNOWN;
		cNetCDF.Defaults.GenotypeEncoding gtEncoding = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;
		cNetCDF.Defaults.StrandType strand = cNetCDF.Defaults.StrandType.UNKNOWN;
		boolean hasDictionray = false;
		int numMarkers = Integer.MIN_VALUE;
		int numSamples = Integer.MIN_VALUE;
		int numChromosomes = Integer.MIN_VALUE;
		String matrixType = null;
		Date creationDate = null;

		NetcdfFile ncfile = null;
		if (netCDFFile.exists()) {
			try {
				ncfile = NetcdfFile.open(netCDFFile.getAbsolutePath());

				try {
					friendlyName = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_FRIENDLY_NAME).getStringValue();
				} catch (Exception ex) {
					LOG.error("No friendly name stored in matrix NetCDF file: " + netCDFFile.getAbsolutePath(), ex);
				}

				try {
					description = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION).getStringValue();
				} catch (Exception ex) {
					LOG.error("No description stored in matrix NetCDF file: " + netCDFFile.getAbsolutePath(), ex);
				}

				technology = cImport.ImportFormat.compareTo(ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY).getStringValue());

				try {
					gwaspiDBVersion = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GWASPIDB_VERSION).getStringValue();
				} catch (Exception ex) {
					LOG.error(null, ex);
				}

				Variable var = ncfile.findVariable(cNetCDF.Variables.GLOB_GTENCODING);
				if (var != null) {
					try {
						ArrayChar.D2 gtCodeAC = (ArrayChar.D2) var.read("(0:0:1, 0:7:1)");
//						gtEncoding = GenotypeEncoding.valueOf(gtCodeAC.getString(0));
						gtEncoding = cNetCDF.Defaults.GenotypeEncoding.compareTo(gtCodeAC.getString(0)); // HACK, the above was used before
					} catch (InvalidRangeException ex) {
						LOG.error(null, ex);
					}
				}

				strand = cNetCDF.Defaults.StrandType.valueOf(ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_STRAND).getStringValue());
				hasDictionray = ((Integer) ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY).getNumericValue() != 0);

				Dimension markersDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_MARKERSET);
				numMarkers = markersDim.getLength();

				Dimension samplesDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
				numSamples = samplesDim.getLength();

				Dimension chromosomesDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
				numChromosomes = chromosomesDim.getLength();

				try {
					matrixType = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_MATRIX_TYPE).getStringValue();
				} catch (Exception ex) {
					LOG.error("No description stored in matrix NetCDF file: " + netCDFFile.getAbsolutePath(), ex);
				}

				try {
					creationDate = new Date(ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_CREATION_DATE).getNumericValue().longValue());
				} catch (Exception ex) {
					LOG.error("No friendly name stored in matrix NetCDF file: " + netCDFFile.getAbsolutePath(), ex);
				}
			} catch (IOException ex) {
				LOG.error("Cannot open file: " + ncfile, ex);
			} finally {
				if (null != ncfile) {
					try {
						ncfile.close();
					} catch (IOException ex) {
						LOG.warn("Cannot close file: " + ncfile, ex);
					}
				}
			}
		}

		String simpleName = netCDFFile.getName();
		simpleName = simpleName.substring(0, simpleName.lastIndexOf('.') - 1); // cut off the '.nc' file extension

		MatrixMetadata matrixMetadata = new MatrixMetadata(
			new MatrixKey(studyKey, Integer.MIN_VALUE),
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

//		return sampleSet;
		ensureMatrixMetadata();
		return new SampleSet(matrixKey);
	}

	@Override
	public MarkersMetadataSource getMarkersMetadatasSource() throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); XXX;
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

//	private int getDimension(String dimensionVar) throws IOException {
//
//		int dimension = Integer.MIN_VALUE;
//
//		NetcdfFile ncfile = null;
//		try {
//			ncfile = NetcdfFile.open(pathToMatrix);
//
//			Dimension setDim = ncfile.findDimension(dimensionVar);
//			dimension = setDim.getLength();
//		} finally {
//			if (null != ncfile) {
//				try {
//					ncfile.close();
//				} catch (IOException ex) {
//					LOG.warn("Cannot close file: " + ncfile, ex);
//				}
//			}
//		}
//
//		return dimension;
//	}


	private static final class NetCdfChromosomesKeysSource extends ArrayList<ChromosomeKey> implements ChromosomesKeysSource {
	}

	private static final class NetCdfChromosomesInfosSource extends ArrayList<ChromosomeInfo> implements ChromosomesInfosSource {
	}

	@Override
	public ChromosomesKeysSource getChromosomesKeysSource() throws IOException {

		ensureMatrixMetadata();
		MarkerSet markerSet = new MarkerSet(matrixKey);

		NetCdfChromosomesKeysSource chrInfSrc = new NetCdfChromosomesKeysSource();
		markerSet.initFullMarkerIdSetMap(); // XXX may not always be required
		chrInfSrc.addAll(markerSet.getChrInfoSetMap().keySet());

		return chrInfSrc;
	}

	@Override
	public ChromosomesInfosSource getChromosomesInfosSource() throws IOException {

		ensureMatrixMetadata();
		MarkerSet markerSet = new MarkerSet(matrixKey);

		NetCdfChromosomesInfosSource chrInfSrc = new NetCdfChromosomesInfosSource();
		markerSet.initFullMarkerIdSetMap(); // XXX may not always be required
		chrInfSrc.addAll(markerSet.getChrInfoSetMap().values());

		return chrInfSrc;
	}

	@Override
	public MarkersKeysSource getMarkersKeysSource() throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); XXX;
	}

	@Override
	public SamplesGenotypesSource getSamplesGenotypesSource() throws IOException {

		ensureMatrixMetadata();
		return new MarkerSet(matrixKey);
	}

	@Override
	public SamplesInfosSource getSamplesInfosSource() throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); XXX;
	}

	@Override
	public SamplesKeysSource getSamplesKeysSource() throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); XXX;
	}
}

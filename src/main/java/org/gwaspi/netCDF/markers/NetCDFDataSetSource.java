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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.ChromosomesInfosSource;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerKeyFactory;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SampleKeyFactory;
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
	private NetcdfFile rdNetCdfFile;
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
		this.rdNetCdfFile = null;
		this.matrixKey = null;
		this.matrixMetadata = null;
	}

	private void ensureReadNetCdfFile() throws IOException {

		if (rdNetCdfFile == null) {
			rdNetCdfFile = NetcdfFile.open(netCDFpath.getAbsolutePath());
		}
	}

	private void ensureMatrixMetadata() throws IOException {

		if (matrixMetadata == null) {
			ensureReadNetCdfFile();
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
		NetcdfFile ncFile = NetcdfFile.open(netCDFFile.getAbsolutePath());
		return loadMatrixMetadata(studyKey, ncFile, newMatrixFriendlyName);
	}

	public static MatrixMetadata loadMatrixMetadata(
			StudyKey studyKey,
			NetcdfFile netCDFFile,
			String newMatrixFriendlyName)
			throws IOException
	{
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

			Dimension chromosomesDim = netCDFFile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
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
		return new SampleSet(matrixMetadata);
	}

	@Override
	public MarkersMetadataSource getMarkersMetadatasSource() throws IOException {

		ensureReadNetCdfFile();
		return new NetCdfMarkersMetadataSource(rdNetCdfFile);
	}

	private static class NetCdfMarkersMetadataSource extends AbstractListSource<MarkerMetadata> implements MarkersMetadataSource {

		private static final int DEFAULT_CHUNK_SIZE = 50;

		NetCdfMarkersMetadataSource(NetcdfFile rdNetCdfFile) {
			super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_MARKERSET);
		}

		@Override
		public List<MarkerMetadata> getRange(int from, int to) throws IOException {

			List<MarkerMetadata> values = new ArrayList<MarkerMetadata>(to - from);

			List<String> markerIdsIt = getMarkerIds(from, to);
			Iterator<String> rsIdsIt = getRsIds(from, to).iterator();
			Iterator<String> chromosomesIt = getChromosomes(from, to).iterator();
			Iterator<Integer> positionsIt = getPositions(from, to).iterator();
			Iterator<String> allelesIt = getAlleles(from, to).iterator();
			Iterator<String> strandsIt = getStrands(from, to).iterator();
			for (String markerId : markerIdsIt) {
				values.add(new MarkerMetadata(
						markerId,
						rsIdsIt.next(),
						chromosomesIt.next(),
						positionsIt.next(),
						allelesIt.next(),
						strandsIt.next()
				));
			}

			return values;
		}

		@Override
		public List<String> getMarkerIds() throws IOException {
			return getMarkerIds(-1, -1);
		}

		@Override
		public List<String> getRsIds() throws IOException {
			return getRsIds(-1, -1);
		}

		@Override
		public List<String> getChromosomes() throws IOException {
			return getChromosomes(-1, -1);
		}

		@Override
		public List<Integer> getPositions() throws IOException {
			return getPositions(-1, -1);
		}

		@Override
		public List<String> getAlleles() throws IOException {
			return getAlleles(-1, -1);
		}

		@Override
		public List<String> getStrands() throws IOException {
			return getStrands(-1, -1);
		}

		@Override
		public List<String> getMarkerIds(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_MARKERSET, from, to);
		}

		@Override
		public List<String> getRsIds(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_MARKERS_RSID, from, to);
		}

		@Override
		public List<String> getChromosomes(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_MARKERS_CHR, from, to);
		}

		@Override
		public List<Integer> getPositions(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_MARKERS_POS, from, to);
		}

		@Override
		public List<String> getAlleles(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, from, to);
		}

		@Override
		public List<String> getStrands(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_GT_STRAND, from, to);
		}
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


	private static final class NetCdfChromosomesKeysSource extends AbstractListSource<ChromosomeKey> implements ChromosomesKeysSource {

		/**
		 * As we have max 23 chromosomes, in general,
		 * this constant does not really matter;
		 * though it should be at least 23.
		 */
		private static final int DEFAULT_CHUNK_SIZE = 100;

		NetCdfChromosomesKeysSource(NetcdfFile rdNetCdfFile) {
			super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_CHRSET);
		}

		@Override
		public List<ChromosomeKey> getRange(int from, int to) throws IOException {

			List<ChromosomeKey> chromosomes;

			List<String> keys = readVar(cNetCDF.Variables.VAR_CHR_IN_MATRIX, from, to);

			chromosomes = new ArrayList<ChromosomeKey>(keys.size());
			for (String encodedKey : keys) {
				chromosomes.add(ChromosomeKey.valueOf(encodedKey));
			}

			return chromosomes;
		}
	}

	@Override
	public ChromosomesKeysSource getChromosomesKeysSource() throws IOException {

		ensureReadNetCdfFile();
		return new NetCdfChromosomesKeysSource(rdNetCdfFile);
	}

	private static final class NetCdfChromosomesInfosSource extends AbstractListSource<ChromosomeInfo> implements ChromosomesInfosSource {

		/**
		 * As we have max 23 chromosomes, in general,
		 * this constant does not really matter;
		 * though it should be at least 23.
		 */
		private static final int DEFAULT_CHUNK_SIZE = 100;

		NetCdfChromosomesInfosSource(NetcdfFile rdNetCdfFile) {
			super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_CHRSET);
		}

		@Override
		public List<ChromosomeInfo> getRange(int from, int to) throws IOException {

			List<ChromosomeInfo> chromosomes;

			List<int[]> chromosomeInfosRaw = readVar(cNetCDF.Variables.VAR_CHR_INFO, from, to);

			chromosomes = new ArrayList<ChromosomeInfo>(chromosomeInfosRaw.size());
			for (int[] infoRaw : chromosomeInfosRaw) {
				chromosomes.add(new ChromosomeInfo(
						infoRaw[0],
						infoRaw[1],
						infoRaw[2],
						infoRaw[3]));
			}

			return chromosomes;
		}
	}

	@Override
	public ChromosomesInfosSource getChromosomesInfosSource() throws IOException {

		ensureReadNetCdfFile();
		return new NetCdfChromosomesInfosSource(rdNetCdfFile);
	}

	private static class NetCdfMarkersKeysSource extends AbstractListSource<MarkerKey> implements MarkersKeysSource {

		private static final int DEFAULT_CHUNK_SIZE = 200;

		NetCdfMarkersKeysSource(NetcdfFile rdNetCdfFile) {
			super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_MARKERSET);
		}

		@Override
		public Map<Integer, MarkerKey> getIndicesMap() throws IOException {
			return getIndicesMap(-1, -1);
		}

		@Override
		public List<MarkerKey> getRange(int from, int to) throws IOException {

			List<MarkerKey> markers;

			List<String> keys = readVar(cNetCDF.Variables.VAR_MARKERSET, from, to);

			markers = new ArrayList<MarkerKey>(keys.size());
			MarkerKeyFactory markerKeyFactory = new MarkerKeyFactory();
			for (String encodedKey : keys) {
				markers.add(markerKeyFactory.decode(encodedKey));
			}

			return markers;
		}

		@Override
		public Map<Integer, MarkerKey> getIndicesMap(int from, int to) throws IOException {

			Map<Integer, MarkerKey> markers;

			List<String> keys = readVar(cNetCDF.Variables.VAR_MARKERSET, from, to);

			markers = new LinkedHashMap<Integer, MarkerKey>(keys.size());
			int index = (from >= 0) ? from : 0;
			MarkerKeyFactory markerKeyFactory = new MarkerKeyFactory();
			for (String encodedKey : keys) {
				markers.put(index++, markerKeyFactory.decode(encodedKey));
			}

			return markers;
		}
	}

	@Override
	public MarkersKeysSource getMarkersKeysSource() throws IOException {

		ensureReadNetCdfFile();
		return new NetCdfMarkersKeysSource(rdNetCdfFile);
	}

	@Override
	public SamplesGenotypesSource getSamplesGenotypesSource() throws IOException {

		ensureMatrixMetadata();
		return new MarkerSet(matrixMetadata);
	}

	private static class NetCdfSamplesInfosSource extends AbstractListSource<SampleInfo> implements SamplesInfosSource {

		private static final int DEFAULT_CHUNK_SIZE = 50;

		private final StudyKey studyKey;

		NetCdfSamplesInfosSource(StudyKey studyKey, NetcdfFile rdNetCdfFile) {
			super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_SAMPLESET);

			this.studyKey = studyKey;
		}

		@Override
		public List<SampleInfo> getRange(int from, int to) throws IOException {

			List<SampleInfo> values = new ArrayList<SampleInfo>(to - from);

			List<SampleKey> sampleKeys = getSampleKeys(from, to);
			Iterator<Integer> orderIdsIt = getOrderIds(from, to).iterator();
			Iterator<String> fathersIt = getFathers(from, to).iterator();
			Iterator<String> mothersIt = getMothers(from, to).iterator();
			Iterator<Sex> sexesIt = getSexes(from, to).iterator();
			Iterator<Affection> affectionsIt = getAffections(from, to).iterator();
			Iterator<String> categoriesIt = getCategoriess(from, to).iterator();
			Iterator<String> diseasesIt = getDiseases(from, to).iterator();
			Iterator<String> populationsIt = getPopulations(from, to).iterator();
			Iterator<Integer> agesIt = getAges(from, to).iterator();
			Iterator<String> filtersIt = getFilters(from, to).iterator();
			Iterator<Integer> approvedsIt = getApproveds(from, to).iterator();
			Iterator<Integer> statusesIt = getStatuses(from, to).iterator();
			for (SampleKey sampleKey : sampleKeys) {
				values.add(new SampleInfo(
						studyKey,
						sampleKey.getSampleId(),
						sampleKey.getFamilyId(),
						orderIdsIt.next(),
						fathersIt.next(),
						mothersIt.next(),
						sexesIt.next(),
						affectionsIt.next(),
						categoriesIt.next(),
						diseasesIt.next(),
						populationsIt.next(),
						agesIt.next(),
						filtersIt.next(),
						approvedsIt.next(),
						statusesIt.next()
				));
			}

			return values;
		}

		@Override
		public List<SampleKey> getSampleKeys() throws IOException {
			return getSampleKeys(-1, -1);
		}

		@Override
		public List<Integer> getOrderIds() throws IOException {
			return getOrderIds(-1, -1);
		}

		@Override
		public List<String> getFathers() throws IOException {
			return getFathers(-1, -1);
		}

		@Override
		public List<String> getMothers() throws IOException {
			return getMothers(-1, -1);
		}

		@Override
		public List<SampleInfo.Sex> getSexes() throws IOException {
			return getSexes(-1, -1);
		}

		@Override
		public List<SampleInfo.Affection> getAffections() throws IOException {
			return getAffections(-1, -1);
		}

		@Override
		public List<String> getCategoriess() throws IOException {
			return getCategoriess(-1, -1);
		}

		@Override
		public List<String> getDiseases() throws IOException {
			return getDiseases(-1, -1);
		}

		@Override
		public List<String> getPopulations() throws IOException {
			return getPopulations(-1, -1);
		}

		@Override
		public List<Integer> getAges() throws IOException {
			return getAges(-1, -1);
		}

		@Override
		public List<String> getFilters() throws IOException {
			return getFilters(-1, -1);
		}

		@Override
		public List<Integer> getApproveds() throws IOException {
			return getApproveds(-1, -1);
		}

		@Override
		public List<Integer> getStatuses() throws IOException {
			return getStatuses(-1, -1);
		}

		@Override
		public List<SampleKey> getSampleKeys(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_KEY, from, to);
		}

		@Override
		public List<Integer> getOrderIds(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_ORDER_ID, from, to);
		}

		@Override
		public List<String> getFathers(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_FATHER, from, to);
		}

		@Override
		public List<String> getMothers(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_MOTHER, from, to);
		}

		@Override
		public List<SampleInfo.Sex> getSexes(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLES_SEX, from, to);
		}

		@Override
		public List<SampleInfo.Affection> getAffections(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLES_AFFECTION, from, to);
		}

		@Override
		public List<String> getCategoriess(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_CATEGORY, from, to);
		}

		@Override
		public List<String> getDiseases(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_DISEASE, from, to);
		}

		@Override
		public List<String> getPopulations(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_POPULATION, from, to);
		}

		@Override
		public List<Integer> getAges(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_AGE, from, to);
		}

		@Override
		public List<String> getFilters(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_FILTER, from, to);
		}

		@Override
		public List<Integer> getApproveds(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_APPROVED, from, to);
		}

		@Override
		public List<Integer> getStatuses(int from, int to) throws IOException {
			return readVar(cNetCDF.Variables.VAR_SAMPLE_STATUS, from, to);
		}
	}


	@Override
	public SamplesInfosSource getSamplesInfosSource() throws IOException {
		return new NetCdfSamplesInfosSource(studyKey, rdNetCdfFile);
	}

	private static class NetCdfSamplesKeysSource extends AbstractListSource<SampleKey> implements SamplesKeysSource {

		private static final int DEFAULT_CHUNK_SIZE = 200;

		private final StudyKey studyKey;

		NetCdfSamplesKeysSource(StudyKey studyKey, NetcdfFile rdNetCdfFile) {
			super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_SAMPLESET);

			this.studyKey = studyKey;
		}

		@Override
		public List<SampleKey> getRange(int from, int to) throws IOException {

			List<SampleKey> samples;

			List<String> keys = readVar(cNetCDF.Variables.VAR_SAMPLE_KEY, from, to);

			samples = new ArrayList<SampleKey>(keys.size());
			SampleKeyFactory sampleKeyFactory = new SampleKeyFactory(studyKey);
			for (String encodedKey : keys) {
				samples.add(sampleKeyFactory.decode(encodedKey));
			}

			return samples;
		}

		public Map<Integer, SampleKey> getIndicesMap(int from, int to) throws IOException {

			Map<Integer, SampleKey> samples;

			List<String> keys = readVar(cNetCDF.Variables.VAR_MARKERSET, from, to);

			samples = new LinkedHashMap<Integer, SampleKey>(keys.size());
			int index = (from >= 0) ? from : 0;
			SampleKeyFactory sampleKeyFactory = new SampleKeyFactory(studyKey);
			for (String encodedKey : keys) {
				samples.put(index++, sampleKeyFactory.decode(encodedKey));
			}

			return samples;
		}
	}

	@Override
	public SamplesKeysSource getSamplesKeysSource() throws IOException {

		ensureReadNetCdfFile();
		return new NetCdfSamplesKeysSource(studyKey, rdNetCdfFile);
	}
}

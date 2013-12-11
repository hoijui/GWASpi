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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
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
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SampleKeyFactory;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.samples.GwaspiSamplesParser;
import org.gwaspi.samples.SampleSet;
import org.gwaspi.samples.SamplesParserManager;
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
		cImport.ImportFormat technology = cImport.ImportFormat.UNKNOWN;
		cNetCDF.Defaults.GenotypeEncoding gtEncoding = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;
		cNetCDF.Defaults.StrandType strand = cNetCDF.Defaults.StrandType.UNKNOWN;
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

			technology = cImport.ImportFormat.compareTo(netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY).getStringValue());

			try {
				gwaspiDBVersion = netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GWASPIDB_VERSION).getStringValue();
			} catch (Exception ex) {
				LOG.error(null, ex);
			}

			Variable var = netCDFFile.findVariable(cNetCDF.Variables.GLOB_GTENCODING);
			if (var != null) {
				try {
					ArrayChar.D2 gtCodeAC = (ArrayChar.D2) var.read("(0:0:1, 0:7:1)");
//						gtEncoding = GenotypeEncoding.valueOf(gtCodeAC.getString(0));
					gtEncoding = cNetCDF.Defaults.GenotypeEncoding.compareTo(gtCodeAC.getString(0)); // HACK, the above was used before
				} catch (InvalidRangeException ex) {
					LOG.error(null, ex);
				}
			}

			strand = cNetCDF.Defaults.StrandType.valueOf(netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_STRAND).getStringValue());
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
				creationDate = new Date(netCDFFile.findGlobalAttribute(cNetCDF.Attributes.GLOB_CREATION_DATE).getNumericValue().longValue());
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

		ensureReadNetCdfFile();
		return new NetCdfMarkersMetadataSource(rdNetCdfFile);
	}

	private static class NetCdfMarkersMetadataSource extends AbstractList<MarkerMetadata> implements MarkersMetadataSource {

		private static final int DEFAULT_CHUNK_SIZE = 50;
		private final NetcdfFile rdNetCdfFile;
		private int loadedChunkNumber;
		private List<MarkerMetadata> loadedChunk;

		NetCdfMarkersMetadataSource(NetcdfFile rdNetCdfFile) {

			this.rdNetCdfFile = rdNetCdfFile;
			this.loadedChunkNumber = -1;
			this.loadedChunk = null;
		}

		private <VT> List<VT> readVar(String varName, int from, int to) throws IOException {

			List<VT> values = new ArrayList<VT>(0);
			NetCdfUtils.readVariable(rdNetCdfFile, varName, from, to, values, null);
			return values;
		}

		@Override
		public MarkerMetadata get(int index) {

			final int chunkNumber = index / DEFAULT_CHUNK_SIZE;
			final int inChunkPosition = index % DEFAULT_CHUNK_SIZE;

			if (chunkNumber != loadedChunkNumber) {
				try {
					loadedChunk = getRange(chunkNumber, chunkNumber + DEFAULT_CHUNK_SIZE);
					loadedChunkNumber = chunkNumber;
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}

			return loadedChunk.get(inChunkPosition);
		}

		@Override
		public int size() {

			Dimension dim = rdNetCdfFile.findDimension(cNetCDF.Dimensions.DIM_MARKERSET);
			return dim.getLength();
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

	private static class NetCdfMarkersKeysSource extends AbstractList<MarkerKey> implements MarkersKeysSource {

		private static final int DEFAULT_CHUNK_SIZE = 200;
		private final NetcdfFile rdNetCdfFile;
		private int loadedChunkNumber;
		private List<MarkerKey> loadedChunk;

		NetCdfMarkersKeysSource(NetcdfFile rdNetCdfFile) {

			this.rdNetCdfFile = rdNetCdfFile;
			this.loadedChunkNumber = -1;
			this.loadedChunk = null;
		}

		private <VT> List<VT> readVar(String varName, int from, int to) throws IOException {

			List<VT> values = new ArrayList<VT>(0);
			NetCdfUtils.readVariable(rdNetCdfFile, varName, from, to, values, null);
			return values;
		}

		@Override
		public MarkerKey get(int index) {

			final int chunkNumber = index / DEFAULT_CHUNK_SIZE;
			final int inChunkPosition = index % DEFAULT_CHUNK_SIZE;

			if (chunkNumber != loadedChunkNumber) {
				try {
					loadedChunk = getRange(chunkNumber, chunkNumber + DEFAULT_CHUNK_SIZE);
					loadedChunkNumber = chunkNumber;
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}

			return loadedChunk.get(inChunkPosition);
		}

		@Override
		public int size() {

			Dimension dim = rdNetCdfFile.findDimension(cNetCDF.Dimensions.DIM_MARKERSET);
			return dim.getLength();
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
		return new MarkerSet(matrixKey);
	}

	private static class NetCdfSamplesInfosSource extends LinkedList<SampleInfo> implements SamplesInfosSource {

	}

	@Override
	public SamplesInfosSource getSamplesInfosSource() throws IOException {

		// HACK all stuff down here is hacky!
		final NetCdfSamplesInfosSource samplesInfosSource = new NetCdfSamplesInfosSource();
		DataSetDestination tmpDataSetDestination = new DataSetDestination() {

			public void init() throws IOException {}
			public void startLoadingDummySampleInfos() throws IOException {}
			public void finishedLoadingDummySampleInfos() throws IOException {}
			public void startLoadingSampleInfos(boolean storeOnlyKeys) throws IOException {}

			public void addSampleInfo(SampleInfo sampleInfo) throws IOException {
				samplesInfosSource.add(sampleInfo);
			}

			public void addSampleKey(SampleKey sampleKey) throws IOException {}
			public void finishedLoadingSampleInfos() throws IOException {}
			public void startLoadingMarkerMetadatas(boolean storeOnlyKeys) throws IOException {}
			public void addMarkerMetadata(MarkerMetadata markerMetadata) throws IOException {}
			public void addMarkerKey(MarkerKey markerKey) throws IOException {}
			public void finishedLoadingMarkerMetadatas() throws IOException {}
			public void startLoadingChromosomeMetadatas() throws IOException {}
			public void addChromosomeMetadata(ChromosomeKey chromosomeKey, ChromosomeInfo chromosomeInfo) throws IOException {}
			public void finishedLoadingChromosomeMetadatas() throws IOException {}
			public void startLoadingAlleles(boolean perSample) throws IOException {}
			public void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws IOException {}
			public void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws IOException {}
			public void finishedLoadingAlleles() throws IOException {}
			public void done() throws IOException {}
		};
		SamplesParserManager.scanSampleInfo(studyKey, cImport.ImportFormat.GWASpi, gtPath, tmpDataSetDestination);

		return samplesInfosSource;
	}

	private static class NetCdfSamplesKeysSource extends AbstractList<SampleKey> implements SamplesKeysSource {

		private static final int DEFAULT_CHUNK_SIZE = 200;

		private final StudyKey studyKey;
		private final NetcdfFile rdNetCdfFile;
		private int loadedChunkNumber;
		private List<SampleKey> loadedChunk;

		NetCdfSamplesKeysSource(StudyKey studyKey, NetcdfFile rdNetCdfFile) {

			this.studyKey = studyKey;
			this.rdNetCdfFile = rdNetCdfFile;
			this.loadedChunkNumber = -1;
			this.loadedChunk = null;
		}

		private <VT> List<VT> readVar(String varName, int from, int to) throws IOException {

			List<VT> values = new ArrayList<VT>(0);
			NetCdfUtils.readVariable(rdNetCdfFile, varName, from, to, values, null);
			return values;
		}

		@Override
		public SampleKey get(int index) {

			final int chunkNumber = index / DEFAULT_CHUNK_SIZE;
			final int inChunkPosition = index % DEFAULT_CHUNK_SIZE;

			if (chunkNumber != loadedChunkNumber) {
				try {
					loadedChunk = getRange(chunkNumber, chunkNumber + DEFAULT_CHUNK_SIZE);
					loadedChunkNumber = chunkNumber;
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}

			return loadedChunk.get(inChunkPosition);
		}

		@Override
		public int size() {

			Dimension dim = rdNetCdfFile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
			return dim.getLength();
		}

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

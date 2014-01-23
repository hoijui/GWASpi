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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.CompactGenotypesList;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.GenotypesListFactory;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCdfMarkersGenotypesSource extends AbstractNetCdfListSource<GenotypesList> implements MarkersGenotypesSource {

	private static final Logger log
			= LoggerFactory.getLogger(NetCdfMarkersGenotypesSource.class);

//	private final MatrixMetadata matrixMetadata;
//	private Map<SampleKey, ?> sampleIdSetMap;
//	private int numMarkers;
//	private NetcdfFile rdNcFile;
//	private GenotypesListFactory genotyesListFactory;
//
//	public NetCdfMarkersGenotypesSource(MatrixMetadata matrixMetadata) throws IOException {
//
//		this.matrixMetadata = matrixMetadata;
//		this.numMarkers = matrixMetadata.getNumMarkers();
//		this.sampleIdSetMap = new LinkedHashMap<SampleKey, Object>();
//		this.rdNcFile = NetcdfFile.open(MatrixMetadata.generatePathToNetCdfFile(matrixMetadata).getAbsolutePath()); // HACK most likely not optimal.. this file
//		this.genotyesListFactory = CompactGenotypesList.FACTORY;
//	}
//
//	public NetCdfMarkersGenotypesSource(MatrixKey matrixKey) throws IOException {
//		this(MatricesList.getMatrixMetadataById(matrixKey));
//	}
//
//	/**
//	 * @deprecated we should get rid of this!
//	 */
//	public NetCdfMarkersGenotypesSource(String netCDFName) throws IOException {
////		this(MatricesList.getMatrixMetadataByNetCDFname(netCDFName));
//
//		this.matrixMetadata = null;
//		this.numMarkers = 0;
//		this.sampleIdSetMap = new LinkedHashMap<SampleKey, Object>();
//		this.rdNcFile = null;
//		this.genotyesListFactory = CompactGenotypesList.FACTORY;
//	}
//
//	public NetCdfMarkersGenotypesSource(StudyKey studyKey, String netCDFPath, String netCDFName) throws IOException {
//		this(NetCDFDataSetSource.loadMatrixMetadata(studyKey, new File(netCDFPath), netCDFName));
//	}
	private final GenotypesListFactory genotyesListFactory;

	private static final int DEFAULT_CHUNK_SIZE = 200;
	private static final int DEFAULT_CHUNK_SIZE_SHATTERED = 1;

	private NetCdfMarkersGenotypesSource(NetcdfFile rdNetCdfFile) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_MARKERSET);

		this.genotyesListFactory = CompactGenotypesList.FACTORY;
	}

	private NetCdfMarkersGenotypesSource(
			NetcdfFile rdNetCdfFile,
			List<Integer> originalSamplesIndices,
			List<Integer> originalMarkersIndices)
			throws IOException
	{
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE_SHATTERED, originalMarkersIndices);

		this.genotyesListFactory = new CompactGenotypesList.SelectiveIndicesGenotypesListFactory(originalSamplesIndices);
	}

	public static MarkersGenotypesSource createForMatrix(NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfMarkersGenotypesSource(rdNetCdfFile);
	}

	public static MarkersGenotypesSource createForOperation(
			NetcdfFile rdNetCdfFile,
			List<Integer> originalSamplesIndices,
			List<Integer> originalMarkersIndices)
			throws IOException
	{
		return new NetCdfMarkersGenotypesSource(rdNetCdfFile, originalSamplesIndices, originalMarkersIndices);
	}

	@Override
	public List<GenotypesList> getRange(int from, int to) throws IOException {
		return readMarkerGTs(getReadNetCdfFile(), cNetCDF.Variables.VAR_GENOTYPES, from, to);
	}

	private List<GenotypesList> readMarkerGTs(NetcdfFile rdNetCdf, String netCdfVarName, int fromMarkerIndex, int toMarkerIndex) throws IOException {

		Variable var = rdNetCdf.findVariable(netCdfVarName);

		if (var != null) {
			List<GenotypesList> values = new ArrayList<GenotypesList>(toMarkerIndex - fromMarkerIndex + 1);

//			for (int mi = 0; mi < var.getShape(1); mi++) {
			for (int mi = fromMarkerIndex; mi <= toMarkerIndex; mi++) {
				List<byte[]> sampleGTs = NetCdfSamplesGenotypesSource.readMarkerGTs(var, -1, -1, mi);
				GenotypesList genotypesList = genotyesListFactory.extract(sampleGTs);
				values.add(genotypesList);
			}

			return values;
		} else {
			throw new IOException("Variable " + netCdfVarName + " not found in NetCdf file " + rdNetCdf.getLocation());
		}
	}

	// ACCESSORS
//	private int getSampleSetSize() {
//		return numMarkers;
//	}
//
//	private MatrixMetadata getMatrixMetadata(List<String> _sampleSetAL) {
//		return matrixMetadata;
//	}

	//<editor-fold defaultstate="expanded" desc="SAMPLESET FETCHERS">
//	private Map<SampleKey, ?> getSampleIdSetMap() throws IOException {
//		return getSampleIdSetMap(MatrixMetadata.generatePathToNetCdfFile(matrixMetadata).getAbsolutePath());
//	}

//	private Map<SampleKey, char[]> getSampleIdSetMapCharArray() throws IOException {
//		return (Map<SampleKey, char[]>) getSampleIdSetMap();
//	}
//
//	private Map<SampleKey, byte[]> getSampleIdSetMapByteArray() throws IOException {
//		return (Map<SampleKey, byte[]>) getSampleIdSetMap();
//	}
//
//	private Set<SampleKey> getSampleKeys() throws IOException {
//		return getSampleIdSetMap().keySet();
//	}

//	private Map<SampleKey, ?> getSampleIdSetMap(String matrixImportPath) throws IOException {
//		NetcdfFile ncfile = null;
//
//		try {
//			ncfile = NetcdfFile.open(matrixImportPath);
//			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_SAMPLE_KEY);
//
//			if (null == var) {
//				return null;
//			}
//
//			int[] varShape = var.getShape();
//			Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
//
//			try {
//				numMarkers = markerSetDim.getLength();
//
//				StringBuilder netCdfReadStrBldr = new StringBuilder(64);
//				netCdfReadStrBldr
//						.append("(0:")
//						.append(numMarkers - 1)
//						.append(":1, 0:")
//						.append(varShape[1] - 1)
//						.append(":1)");
//				String netCdfReadStr = netCdfReadStrBldr.toString();
//
//				ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
//
//				sampleIdSetMap = NetCdfUtils.writeD2ArrayCharToMapSampleKeys(
//						matrixMetadata.getStudyKey(),
//						sampleSetAC,
//						null);
//			} catch (InvalidRangeException ex) {
//				throw new IOException("Cannot read data", ex);
//			}
//		} finally {
//			if (null != ncfile) {
//				try {
//					ncfile.close();
//				} catch (IOException ex) {
//					log.warn("Cannot close file", ex);
//				}
//			}
//		}
//
//		return sampleIdSetMap;
//	}

//	private Map<SampleKey, byte[]> getSampleIdSetMapByteArray(String matrixImportPath) throws IOException {
//		return (Map<SampleKey, byte[]>) getSampleIdSetMap(matrixImportPath);
//	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="SAMPLESET FILLERS">
	private static String constructNetCDFReadStr(Dimension sampleSetDim, int markerNb, int varDim2) {

		// NOTE exemplary code!
		// how to best create the required string?
		// see http://stackoverflow.com/questions/513600/should-i-use-javas-string-format-if-performance-is-important
		// suposedly the slowest; String.format
//				String netCdfReadStr = String.format(
//						"(0:%d:1, %d:%d:1, 0:%d:1)",
//						(sampleSetDim.getLength() - 1),
//						markerNb,
//						markerNb,
//						(varDim2 - 1));
		// faster; MessageFormat
//				MessageFormat gtReadMF = new MessageFormat("(0:%d:1, %d:%d:1, 0:%d:1)");
//				String netCdfReadStr = gtReadMF.format(new Object[] {
//						(sampleSetDim.getLength() - 1),
//						markerNb,
//						markerNb,
//						(varShape[2] - 1)});
		// ... about the same as MessageFormat(?); String + String
//				String netCdfReadStr
//						= "(0:" + (sampleSetDim.getLength() - 1) + ":1, "
//						+ markerNb + ":" + markerNb + ":1, "
//						+ "0:" + (varDim2 - 1) + ":1)";
		// fastest and most memory friendly way: StringBuilder (not thread-safe)
		StringBuilder netCdfReadStrBldr = new StringBuilder(64);
		netCdfReadStrBldr
				.append("(0:")
				.append(sampleSetDim.getLength() - 1)
				.append(":1, ")
				.append(markerNb)
				.append(":")
				.append(markerNb)
				.append(":1, " + "0:")
				.append(varDim2 - 1)
				.append(":1)");
		String netCdfReadStr = netCdfReadStrBldr.toString();

		return netCdfReadStr;
	}

	private static ArrayByte.D3 readGTs(Variable genotypes, String netCdfReadStr) throws InvalidRangeException, IOException {
		return (ArrayByte.D3) genotypes.read(netCdfReadStr);
	}

//	private static void readAllSamplesGTsFromCurrentMarkerToMap(NetcdfFile rdNcFile, Map<SampleKey, byte[]> rdBytes, int markerNb) throws IOException {
//		readAllSamplesGTsFromCurrentMarker(rdNcFile, rdBytes, markerNb);
//	}

//	private static List<byte[]> readAllSamplesGTsFromCurrentMarkerToList(NetcdfFile rdNcFile, int markerNb) throws IOException {
//		return readAllSamplesGTsFromCurrentMarker(rdNcFile, null, markerNb);
//	}

	private static List<byte[]> readAllSamplesGTsFromCurrentMarker(NetcdfFile rdNcFile, Map<SampleKey, byte[]> rdBytes, int markerNb) throws IOException {

		Variable genotypes = rdNcFile.findVariable(cNetCDF.Variables.VAR_GENOTYPES);

		if (null == genotypes) {
			return null;
		}
		try {
			int[] varShape = genotypes.getShape();

			Dimension sampleSetDim = rdNcFile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);

			String netCdfReadStr = constructNetCDFReadStr(sampleSetDim, markerNb, varShape[2]);

//				ArrayByte.D3 gt_ACD3 = (ArrayByte.D3) genotypes.read(netCdfReadStr);
			ArrayByte.D3 gt_ACD3 = readGTs(genotypes, netCdfReadStr);

			int[] shp = gt_ACD3.getShape();
			int reducer = 0;
			if (shp[0] == 1) {
				reducer++;
			}
			if (shp[1] == 1) {
				reducer++;
			}
			if (shp[2] == 1) {
				reducer++;
			}

			if (reducer == 1) {
				ArrayByte.D2 gt_ACD2 = (ArrayByte.D2) gt_ACD3.reduce();
				if (rdBytes == null) {
					return NetCdfUtils.writeD2ArrayByteToList(gt_ACD2);
				} else {
					NetCdfUtils.writeD2ArrayByteToMapValues(gt_ACD2, rdBytes);
				}
			} else {
				throw new IllegalStateException();
			}
		} catch (InvalidRangeException ex) {
			throw new IOException("Cannot read data", ex);
		}

		return null;
	}

//	private void fillSampleIdSetMapWithVariable(Map<SampleKey, ?> map, String variable) throws IOException {
//
//		NetcdfFile ncfile = null;
//		try {
//			ncfile = NetcdfFile.open(MatrixMetadata.generatePathToNetCdfFile(matrixMetadata).getAbsolutePath());
//			Variable var = ncfile.findVariable(variable);
//
//			if (null == var) {
//				return;
//			}
//
//			DataType dataType = var.getDataType();
//			int[] varShape = var.getShape();
//			Dimension sampleSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
//
//			try {
//				numMarkers = sampleSetDim.getLength();
//				if (dataType == DataType.CHAR) {
//					StringBuilder netCdfReadStrBldr = new StringBuilder(64);
//					netCdfReadStrBldr
//							.append("(0:")
//							.append(numMarkers - 1)
//							.append(":1, 0:")
//							.append(varShape[1] - 1)
//							.append(":1)");
//					String netCdfReadStr = netCdfReadStrBldr.toString();
//
//					ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
//					NetCdfUtils.writeD2ArrayCharToMapValues(sampleSetAC, (Map<SampleKey, char[]>) map);
//					sampleIdSetMap = map;
//				}
//				if (dataType == DataType.DOUBLE) {
//					ArrayDouble.D1 sampleSetAF = (ArrayDouble.D1) var.read("(0:" + (numMarkers - 1) + ":1");
//					NetCdfUtils.writeD1ArrayDoubleToMapValues(sampleSetAF, (Map<SampleKey, Double>) map);
//					sampleIdSetMap = map;
//				}
//			} catch (InvalidRangeException ex) {
//				throw new IOException("Cannot read data", ex);
//			}
//		} finally {
//			if (null != ncfile) {
//				try {
//					ncfile.close();
//				} catch (IOException ex) {
//					log.warn("Cannot close file", ex);
//				}
//			}
//		}
//	}

//	/** @deprecate unused. was used in MatrixDataExtractor */
//	private void fillSampleIdSetMapWithFilterVariable(Map<SampleKey, char[]> map, String variable, int filterPos) throws IOException {
//
//		NetcdfFile ncfile = null;
//		try {
//			ncfile = NetcdfFile.open(MatrixMetadata.generatePathToNetCdfFile(matrixMetadata).getAbsolutePath());
//			Variable var = ncfile.findVariable(variable);
//
//			if (null == var) {
//				return;
//			}
//
//			DataType dataType = var.getDataType();
////			int[] varShape = var.getShape();
//			Dimension sampleSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
//
//			try {
//				numMarkers = sampleSetDim.getLength();
//				if (dataType == DataType.CHAR) {
//					StringBuilder netCdfReadStrBldr = new StringBuilder(64);
//					netCdfReadStrBldr
//							.append("(0:")
//							.append(numMarkers - 1)
//							.append(":1, ")
//							.append(filterPos)
//							.append(":")
//							.append(filterPos)
//							.append(":1)");
//					String netCdfReadStr = netCdfReadStrBldr.toString();
//
//					ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
//					NetCdfUtils.writeD2ArrayCharToMapValues(sampleSetAC, map);
//					sampleIdSetMap = map;
//				}
//			} catch (InvalidRangeException ex) {
//				throw new IOException("Cannot read data", ex);
//			}
//		} finally {
//			if (null != ncfile) {
//				try {
//					ncfile.close();
//				} catch (IOException ex) {
//					log.warn("Cannot close file", ex);
//				}
//			}
//		}
//	}
//	//</editor-fold>

//	@Override
//	public int size() {
//		return sampleIdSetMap.size();
//	}

//	@Override
//	public GenotypesList get(int markerIndex) {
//
////		rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerNb);
////		for (byte[] tempGT : rdSampleSetMap.values()) {
////			if (sampleIdSetMap == null) {
////				initFullMarkerIdSetMap();
////			}
//		List<byte[]> markerGenotypes = null;
//		try {
//			markerGenotypes = readAllSamplesGTsFromCurrentMarkerToList(rdNcFile, markerIndex);
//		} catch (IOException ex) {
//			throw new RuntimeException(ex);
//		}
//
//		return genotyesListFactory.createGenotypesList(markerGenotypes);
//	}
}

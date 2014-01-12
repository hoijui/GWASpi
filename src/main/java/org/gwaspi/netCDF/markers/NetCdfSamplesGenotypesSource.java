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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.CompactGenotypesList;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.GenotypesListFactory;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * This modification of the MarkerSet can create instances of the MarkerSetMap.
 * The markerSet object has several methods to initialize it's Map,
 * which can later be filled with it's corresponding values.
 * The Map is not copied or passed as a parameter to the fillers.
 *
 * The matrix netCDF file is opened at creation of the MarkerSet and closed
 * at finalization of the class. No need to pass a netCDF handler anymore.
 */
public class NetCdfSamplesGenotypesSource extends AbstractListSource<GenotypesList> implements SamplesGenotypesSource {

	private static final Logger log
			= LoggerFactory.getLogger(NetCdfSamplesGenotypesSource.class);

//	private final StudyKey studyKey;
//	private final int numMarkers;
//	private final NetcdfFile ncfile;
//	private int startMkIdx;
//	private int endMkIdx;
//	private Map<MarkerKey, ?> markerIdSetMap;
	private final GenotypesListFactory genotyesListFactory;

	private static final int DEFAULT_CHUNK_SIZE = 50;
	private static final int DEFAULT_CHUNK_SIZE_SHATTERED = 1;

	private NetCdfSamplesGenotypesSource(NetcdfFile rdNetCdfFile) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_SAMPLESET);

		this.genotyesListFactory = CompactGenotypesList.FACTORY;
	}

	private NetCdfSamplesGenotypesSource(NetcdfFile rdNetCdfFile, List<Integer> originalIndices) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE_SHATTERED, originalIndices);

		this.genotyesListFactory = CompactGenotypesList.FACTORY;
	}

	public static SamplesGenotypesSource createForMatrix(NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfSamplesGenotypesSource(rdNetCdfFile);
	}

	public static SamplesGenotypesSource createForOperation(NetcdfFile rdNetCdfFile, List<Integer> originalIndices) throws IOException {
		return new NetCdfSamplesGenotypesSource(rdNetCdfFile, originalIndices);
	}

	@Override
	public List<GenotypesList> getRange(int from, int to) throws IOException {
		return readSampleGTs(getReadNetCdfFile(), cNetCDF.Variables.VAR_GENOTYPES, from, to);
	}

	private List<GenotypesList> readSampleGTs(NetcdfFile rdNetCdf, String netCdfVarName, int fromSampleIndex, int toSampleIndex) throws IOException {

		Variable var = rdNetCdf.findVariable(netCdfVarName);

		if (var != null) {
			List<GenotypesList> values = new ArrayList<GenotypesList>(toSampleIndex - fromSampleIndex);

			for (int si = 0; si < var.getShape(0); si++) {
				List<byte[]> markerGTs = readSampleGTs(var, si, -1, -1);
				GenotypesList genotypesList = genotyesListFactory.createGenotypesList(markerGTs);
				values.add(genotypesList);
			}

			return values;
		} else {
			throw new IOException("Variable " + netCdfVarName + " not found in NetCdf file " + rdNetCdf.getLocation());
		}
	}

	private static String buildNetCdfReadString(int fromSampleIndex, int toSampleIndex, int fromMarkerIndex, int toMarkerIndex, int numGTs) {

		StringBuilder netCdfReadStr = new StringBuilder(64);

		netCdfReadStr
				.append("(")
				.append(fromSampleIndex)
				.append(":")
				.append(toSampleIndex)
				.append(":1, ")
				.append(fromMarkerIndex)
				.append(":")
				.append(toMarkerIndex)
				.append(":1, " + "0:")
				.append(numGTs - 1)
				.append(":1)");

		return netCdfReadStr.toString();
	}

	static List<byte[]> readSampleGTs(Variable netCdfGTsVar, int sampleIndex, int fromMarkerIndex, int toMarkerIndex) throws IOException {

		final int[] varShape = netCdfGTsVar.getShape();

		if (fromMarkerIndex == -1) {
			fromMarkerIndex = 0;
		}
		if (toMarkerIndex == -1) {
			toMarkerIndex = varShape[1] - 1;
		}

		try {
			String netCdfReadStr = buildNetCdfReadString(sampleIndex, sampleIndex, fromMarkerIndex, toMarkerIndex, varShape[2]);
			ArrayByte.D3 sampleMarkerGTs = (ArrayByte.D3) netCdfGTsVar.read(netCdfReadStr);
			ArrayByte.D2 markerGTs = (ArrayByte.D2) sampleMarkerGTs.reduce(0);
			List<byte[]> rawList = NetCdfUtils.writeD2ArrayByteToList(markerGTs);

			return rawList;
		} catch (InvalidRangeException ex) {
			throw new IOException("Cannot read data", ex);
		}
	}

	static List<byte[]> readMarkerGTs(Variable netCdfGTsVar, int fromSampleIndex, int toSampleIndex, int markerIndex) throws IOException {

		final int[] varShape = netCdfGTsVar.getShape();

		if (fromSampleIndex == -1) {
			fromSampleIndex = 0;
		}
		if (toSampleIndex == -1) {
			toSampleIndex = varShape[0] - 1;
		}

		try {
			String netCdfReadStr = buildNetCdfReadString(fromSampleIndex, toSampleIndex, markerIndex, markerIndex, varShape[2]);
			ArrayByte.D3 sampleMarkerGTs = (ArrayByte.D3) netCdfGTsVar.read(netCdfReadStr);
			ArrayByte.D2 sampleGTs = (ArrayByte.D2) sampleMarkerGTs.reduce(1);
			List<byte[]> rawList = NetCdfUtils.writeD2ArrayByteToList(sampleGTs);

			return rawList;
		} catch (InvalidRangeException ex) {
			throw new IOException("Cannot read data", ex);
		}
	}

//	static ArrayByte.D3 readGTs(NetcdfFile rdNetCdf, String netCdfVarName, int fromSampleIndex, int toSampleIndex, int fromMarkerIndex, int toMarkerIndex) throws IOException {
//
//		Variable var = rdNetCdf.findVariable(netCdfVarName);
//
//		if (var != null) {
//			int[] varShape = var.getShape();
//
//			if (fromSampleIndex == -1) {
//				fromSampleIndex = 0;
//			}
//			if (toSampleIndex == -1) {
//				toSampleIndex = varShape[0] - 1;
//			}
//			if (fromMarkerIndex == -1) {
//				fromMarkerIndex = 0;
//			}
//			if (toMarkerIndex == -1) {
//				toMarkerIndex = varShape[1] - 1;
//			}
//
//			try {
//				StringBuilder netCdfReadStrBldr = new StringBuilder(64);
//				netCdfReadStrBldr
//						.append("(")
//						.append(fromSampleIndex)
//						.append(":")
//						.append(toSampleIndex)
//						.append(":1, ")
//						.append(fromMarkerIndex)
//						.append(":")
//						.append(toMarkerIndex)
//						.append(":1, " + "0:")
//						.append(varShape[2] - 1)
//						.append(":1)");
//				String netCdfReadStr = netCdfReadStrBldr.toString();
//
//				ArrayByte.D3 gt_ACD3 = (ArrayByte.D3) var.read(netCdfReadStr);
//
//				return gt_ACD3;
//			} catch (InvalidRangeException ex) {
//				throw new IOException("Cannot read data", ex);
//			}
//		} else {
//			throw new IOException("Variable " + netCdfVarName + " not found in NetCdf file " + rdNetCdf.getLocation());
//		}
//	}

	static void readSampleGTs(ArrayByte.D3 from, Collection<GenotypesList> to, GenotypesListFactory genotyesListFactory) throws IOException {

		try {
			int[] shp = from.getShape();
			List<Range> ranges = new ArrayList<Range>(3);
			ranges.add(new Range(0, 0));
			ranges.add(new Range(0, shp[1] - 1));
			ranges.add(new Range(0, shp[2] - 1));
			for (int r0 = 0; r0 < shp[0]; r0++) {
				ranges.set(0, new Range(r0, r0));
				ArrayByte.D2 gt_ACD2 = (ArrayByte.D2) from.section(ranges);
				List<byte[]> rawList = NetCdfUtils.writeD2ArrayByteToList(gt_ACD2);
				GenotypesList innerList = genotyesListFactory.createGenotypesList(rawList);
				to.add(innerList);
			}
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}
	}

//	public NetCdfSamplesGenotypesSource(MatrixMetadata matrixMetadata) throws IOException {
//
//		this.numMarkers = matrixMetadata.getNumMarkers();
//		this.ncfile = NetcdfFile.open(MatrixMetadata.generatePathToNetCdfFile(matrixMetadata).getAbsolutePath());
//		this.startMkIdx = 0;
//		this.endMkIdx = Integer.MIN_VALUE;
//		this.markerIdSetMap = null;
//		this.genotyesListFactory = CompactGenotypesList.FACTORY;
//	}
//
//	public NetCdfSamplesGenotypesSource(MatrixKey matrixKey) throws IOException {
//		this(MatricesList.getMatrixMetadataById(matrixKey));
//	}
//
//	@Override
//	protected void finalize() throws Throwable {
//		try {
//			if (null != ncfile) {
//				try {
//					ncfile.close();
//				} catch (IOException ex) {
//					log.warn("Cannot close netCDF file: " + ncfile.getLocation(), ex);
//				}
//			}
//		} finally {
//			super.finalize();
//		}
//	}

	//<editor-fold defaultstate="expanded" desc="MARKERSET INITILAIZERS">
	// USE MARKERID AS KEYS
//	private void initFullMarkerIdSetMap() {
//		initMarkerIdSetMap(0, Integer.MIN_VALUE);
//	}

	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(Map<String, IV> markerIdAlleles) {
		Map<MarkerKey, IV> reparsedData = new LinkedHashMap<MarkerKey, IV>();
		for (Map.Entry<String, IV> entry : markerIdAlleles.entrySet()) {
			reparsedData.put(MarkerKey.valueOf(entry.getKey()), entry.getValue());
		}
		return reparsedData;
	}
	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(ArrayByte.D2 markersAC) {
		Map<String, IV> markerIdAlleles = NetCdfUtils.writeD2ArrayByteToMapKeys(markersAC);
		return wrapToMarkerKeyMap(markerIdAlleles);
	}
	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(ArrayChar.D2 markersAC, IV commonValue) {
		Map<String, IV> markerIdAlleles = NetCdfUtils.writeD2ArrayCharToMapKeys(markersAC, commonValue);
		return wrapToMarkerKeyMap(markerIdAlleles);
	}
	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(ArrayChar.D2 markersAC) {
		return wrapToMarkerKeyMap(markersAC, null);
	}

	private static <IV> Map<ChromosomeKey, IV> wrapToChromosomeKeyMap(Map<String, IV> chromosomeInfos) {
		Map<ChromosomeKey, IV> reparsedData = new LinkedHashMap<ChromosomeKey, IV>();
		for (Map.Entry<String, IV> entry : chromosomeInfos.entrySet()) {
			reparsedData.put(ChromosomeKey.valueOf(entry.getKey()), entry.getValue());
		}
		return reparsedData;
	}
	private static <IV> Map<ChromosomeKey, IV> wrapToChromosomeKeyMap(ArrayChar.D2 markersAC, IV commonValue) {
		Map<String, IV> markerIdAlleles = NetCdfUtils.writeD2ArrayCharToMapKeys(markersAC, commonValue);
		return wrapToChromosomeKeyMap(markerIdAlleles);
	}
	private static <IV> Map<ChromosomeKey, IV> wrapToChromosomeKeyMap(ArrayChar.D2 markersAC) {
		return wrapToChromosomeKeyMap(markersAC, null);
	}

//	@Override
//	public List<GenotypesList> getRange(int from, int to) throws IOException {
//
//		this.startMkIdx = from;
//		this.endMkIdx = to;
//
//		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERSET);
//
//		if (var != null) {
//			DataType dataType = var.getDataType();
//			int[] varShape = var.getShape();
//
//			try {
//				// KEEP INDEXES REAL
//				if (startMkIdx < 0) {
//					startMkIdx = 0;
//				}
//				if (endMkIdx < 0 || endMkIdx >= numMarkers) {
//					endMkIdx = numMarkers - 1;
//				}
//
//				StringBuilder netCdfReadStrBldr = new StringBuilder(64);
//				netCdfReadStrBldr
//						.append("(")
//						.append(startMkIdx)
//						.append(":")
//						.append(endMkIdx)
//						.append(":1, 0:")
//						.append(varShape[1] - 1)
//						.append(":1)");
//				String netCdfReadStr = netCdfReadStrBldr.toString();
//
//				if (dataType == DataType.CHAR) {
//					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
//					markerIdSetMap = wrapToMarkerKeyMap(markerSetAC);
//				}
//				if (dataType == DataType.BYTE) {
//					ArrayByte.D2 markerSetAC = (ArrayByte.D2) var.read(netCdfReadStr);
//					markerIdSetMap = wrapToMarkerKeyMap(markerSetAC);
//				}
//			} catch (IOException ex) {
//				log.error("Cannot read data", ex);
//			} catch (InvalidRangeException ex) {
//				log.error("Cannot read data", ex);
//			}
//		}
//	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="CHROMOSOME INFO">
//	/**
//     * This Method is safe to return an independent Map.
//	 * The size of this Map is very small.
//	 * @deprecate unused
//	 */
//	private Map<ChromosomeKey, ChromosomeInfo> getChrInfoSetMap() {
//
//		Map<ChromosomeKey, ChromosomeInfo> chrInfoMap = new LinkedHashMap<ChromosomeKey, ChromosomeInfo>();
//
//		// GET NAMES OF CHROMOSOMES
//		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX);
//		if (var != null) {
//			DataType dataType = var.getDataType();
//			int[] varShape = var.getShape();
//			try {
//				if (dataType == DataType.CHAR) {
//					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (varShape[0] - 1) + ":1, 0:7:1)");
//					chrInfoMap = wrapToChromosomeKeyMap(markerSetAC);
//				}
//			} catch (IOException ex) {
//				log.error("Cannot read data", ex);
//			} catch (InvalidRangeException ex) {
//				log.error("Cannot read data", ex);
//			}
//
//			// GET INFO FOR EACH CHROMOSOME
//			var = ncfile.findVariable(cNetCDF.Variables.VAR_CHR_INFO); // Nb of markers, first physical position, last physical position, end index number in MarkerSet
//			if (null == var) {
//				return null;
//			}
//			dataType = var.getDataType();
//			varShape = var.getShape();
//
//			try {
//				if (dataType == DataType.INT) {
//					ArrayInt.D2 chrSetAI = (ArrayInt.D2) var.read("(0:" + (varShape[0] - 1) + ":1, 0:3:1)");
//					NetCdfUtils.writeD2ArrayIntToChromosomeInfoMapValues(chrSetAI, chrInfoMap);
//				}
//			} catch (IOException ex) {
//				log.error("Cannot read data", ex);
//			} catch (InvalidRangeException ex) {
//				log.error("Cannot read data", ex);
//			}
//		} else {
//			return null;
//		}
//
//		return chrInfoMap;
//	}

//	/**
//	 * @deprecate unused
//	 */
//	private static MarkerKey getChrByMarkerIndex(Map<MarkerKey, ChromosomeInfo> chrInfoMap, int markerIndex) {
//		MarkerKey result = null;
//		for (Map.Entry<MarkerKey, ChromosomeInfo> entry : chrInfoMap.entrySet()) {
//			MarkerKey markerKey = entry.getKey();
//			ChromosomeInfo value = entry.getValue();
//			if ((markerIndex <= value.getIndex()) && (result == null)) {
//				result = markerKey;
//			}
//		}
//		return result;
//	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="MARKERSET FILLERS">
////	/**
////	 * @deprecate unused
////	 */
////	private void fillGTsForCurrentSampleIntoInitMap(int sampleNb) throws IOException {
////
////		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_GENOTYPES);
////
////		if (var != null) {
////
////			int[] varShape = var.getShape();
////
////			try {
////				// KEEP INDEXES REAL
////				if (startMkIdx < 0) {
////					startMkIdx = 0;
////				}
////				if (endMkIdx < 0 || endMkIdx >= numMarkers) {
////					endMkIdx = numMarkers - 1;
////				}
////
////				StringBuilder netCdfReadStrBldr = new StringBuilder(64);
////				netCdfReadStrBldr
////						.append("(")
////						.append(sampleNb)
////						.append(":")
////						.append(sampleNb)
////						.append(":1, ")
////						.append(startMkIdx)
////						.append(":")
////						.append(endMkIdx)
////						.append(":1, " + "0:")
////						.append(varShape[2] - 1)
////						.append(":1)");
////				String netCdfReadStr = netCdfReadStrBldr.toString();
////
////				ArrayByte.D3 gt_ACD3 = (ArrayByte.D3) var.read(netCdfReadStr);
////
////				int[] shp = gt_ACD3.getShape();
////				int reducer = 0;
////				if (shp[0] == 1) {
////					reducer++;
////				}
////				if (shp[1] == 1) {
////					reducer++;
////				}
////				if (shp[2] == 1) {
////					reducer++;
////				}
////
////				if (reducer == 1) {
////					ArrayByte.D2 gt_ACD2 = (ArrayByte.D2) gt_ACD3.reduce();
////					NetCdfUtils.writeD2ArrayByteToMapValues(gt_ACD2, (Map<MarkerKey, byte[]>) markerIdSetMap);
////				} else {
////					throw new IllegalStateException();
////				}
////			} catch (IOException ex) {
////				log.error("Cannot read data", ex);
////			} catch (InvalidRangeException ex) {
////				log.error("Cannot read data", ex);
////			}
////		}
////	}

//	/**
//	 * @deprecate unused
//	 */
//	private void fillInitMapWithVariable(String variable) {
//
//		Variable var = ncfile.findVariable(variable);
//		if (var != null) {
//			DataType dataType = var.getDataType();
//			int[] varShape = var.getShape();
//
//			StringBuilder netCdfReadStrBldr = new StringBuilder(64);
//			netCdfReadStrBldr
//					.append("(")
//					.append(startMkIdx)
//					.append(":")
//					.append(endMkIdx)
//					.append(":1");
//			if (varShape.length == 2) {
//				netCdfReadStrBldr
//						.append(", 0:")
//						.append(varShape[1] - 1)
//						.append(":1");
//			}
//			netCdfReadStrBldr.append(")");
//			String netCdfReadStr = netCdfReadStrBldr.toString();
//
//			try {
//				if ((dataType == DataType.CHAR) && (varShape.length == 2)) {
//					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
//					NetCdfUtils.writeD2ArrayCharToMapValues(markerSetAC, (Map<MarkerKey, char[]>) markerIdSetMap);
//				}
//				if (dataType == DataType.DOUBLE) {
//					if (varShape.length == 1) {
//						ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read(netCdfReadStr);
//						NetCdfUtils.writeD1ArrayDoubleToMapValues(markerSetAF, (Map<MarkerKey, Double>) markerIdSetMap);
//					}
//					if (varShape.length == 2) {
//						ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read(netCdfReadStr);
//						NetCdfUtils.writeD2ArrayDoubleToMapValues(markerSetAF, (Map<MarkerKey, double[]>) markerIdSetMap);
//					}
//				}
//				if (dataType == DataType.INT) {
//					if (varShape.length == 1) {
//						ArrayInt.D1 markerSetAD = (ArrayInt.D1) var.read(netCdfReadStr);
//						NetCdfUtils.writeD1ArrayIntToMapValues(markerSetAD, (Map<MarkerKey, Integer>) markerIdSetMap);
//					}
//					if (varShape.length == 2) {
//						ArrayInt.D2 markerSetAD = (ArrayInt.D2) var.read(netCdfReadStr);
//						NetCdfUtils.writeD2ArrayIntToMapValues(markerSetAD, (Map<MarkerKey, int[]>) markerIdSetMap);
//					}
//				}
//			} catch (IOException ex) {
//				log.error("Cannot read data", ex);
//			} catch (InvalidRangeException ex) {
//				log.error("Cannot read data", ex);
//			}
//		}
//	}

//	/**
//	 * Fills markerIdSetMap with values of type MarkerMetadata,
//	 * with only chr and pos set.
//	 * @deprecate unused
//	 */
//	private void fillMarkerSetMapWithChrAndPos() {
//
//		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
//		if (var != null) {
//			DataType dataType = var.getDataType();
//			int[] varShape = var.getShape();
//
//			try {
//				if ((dataType == DataType.CHAR) && (varShape.length == 2)) {
//					StringBuilder netCdfReadStrBldr = new StringBuilder(64);
//					netCdfReadStrBldr
//							.append("(")
//							.append(startMkIdx)
//							.append(":")
//							.append(endMkIdx)
//							.append(":1, 0:")
//							.append(varShape[1] - 1)
//							.append(":1)");
//					String netCdfReadStr = netCdfReadStrBldr.toString();
//
//					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
//					NetCdfUtils.writeD2ArrayCharToMapValues(markerSetAC, (Map<MarkerKey, char[]>) markerIdSetMap);
//				}
//			} catch (IOException ex) {
//				log.error("Cannot read data", ex);
//			} catch (InvalidRangeException ex) {
//				log.error("Cannot read data", ex);
//			}
//		}
//
//		var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_POS);
//		if (var != null) {
//			DataType dataType = var.getDataType();
////			int[] varShape = var.getShape();
//
//			try {
//				if (dataType == DataType.INT) {
//					ArrayInt.D1 markerSetAI = (ArrayInt.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");
//
//					int[] shape = markerSetAI.getShape();
//					Index index = markerSetAI.getIndex();
//					Iterator<Entry<MarkerKey, Object>> it = ((Map<MarkerKey, Object>) markerIdSetMap).entrySet().iterator();
//					for (int i = 0; i < shape[0]; i++) {
//						Entry<MarkerKey, Object> entry = it.next();
//						MarkerMetadata chrInfo = new MarkerMetadata(
//								new String((char[]) entry.getValue()), // chr
//								markerSetAI.getInt(index.set(i))); // pos
//						entry.setValue(chrInfo);
//					}
//				}
//			} catch (IOException ex) {
//				log.error("Cannot read data", ex);
//			} catch (InvalidRangeException ex) {
//				log.error("Cannot read data", ex);
//			}
//		}
//	}

//	private void fillWith(Object value) {
//		fillWith((Map<MarkerKey, Object>)markerIdSetMap, value);
//	}
//	private void fillWith(char[] value) {
//		fillWith((Object)value);
//	}
//	private void fillWith(byte[] value) {
//		fillWith((Object)value);
//	}
//	private void fillWith(int[] value) {
//		fillWith((Object)value);
//	}
//	private void fillWith(double[] value) {
//		fillWith((Object)value);
//	}
//	private void fillWith(Integer value) {
//		fillWith((Object)value);
//	}
//	private void fillWith(Double value) {
//		fillWith((Object)value);
//	}
//
//	private static <K, V> void fillWith(Map<K, V> map, V defaultVal) {
//
//		for (Map.Entry<K, V> entry : map.entrySet()) {
//			entry.setValue(defaultVal);
//		}
//	}
//
//	// HELPERS TO TRANSFER VALUES FROM ONE Map TO ANOTHER
//	private static <K, V> void replaceWithValuesFrom(Map<K, V> toBeModified, Map<K, V> newValuesSource) {
//
//		for (Map.Entry<K, V> entry : toBeModified.entrySet()) {
//			entry.setValue(newValuesSource.get(entry.getKey()));
//		}
//	}
//
//	// HELPER TO APPEND VALUE TO AN EXISTING Map CHAR VALUE
//	/** @deprecated unused */
//	private void appendVariableToMarkerSetMapValue(String variable, String separator) {
//
//		Variable var = ncfile.findVariable(variable);
//		if (var != null) {
//			DataType dataType = var.getDataType();
//			int[] varShape = var.getShape();
//			try {
//				if ((dataType == DataType.CHAR) && (varShape.length == 2)) {
//					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
//
//					int[] shape = markerSetAC.getShape();
//					Index index = markerSetAC.getIndex();
//					Iterator<Entry<MarkerKey, char[]>> it = ((Map<MarkerKey, char[]>) markerIdSetMap).entrySet().iterator();
//					for (int i = 0; i < shape[0]; i++) {
//						Entry<MarkerKey, char[]> entry = it.next();
//						String value = new String(entry.getValue());
//						if (!value.isEmpty()) {
//							value += separator;
//						}
//						StringBuilder newValue = new StringBuilder();
//						for (int j = 0; j < shape[1]; j++) {
//							newValue.append(markerSetAC.getChar(index.set(i, j)));
//						}
//						entry.setValue((value + newValue.toString().trim()).toCharArray());
//					}
//				} else if ((dataType == DataType.FLOAT) && (varShape.length == 1)) {
//					ArrayFloat.D1 markerSetAF = (ArrayFloat.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");
//
//					int[] shape = markerSetAF.getShape();
//					Index index = markerSetAF.getIndex();
//					Iterator<Entry<MarkerKey, char[]>> it = ((Map<MarkerKey, char[]>) markerIdSetMap).entrySet().iterator();
//					for (int i = 0; i < shape[0]; i++) {
//						Entry<MarkerKey, char[]> entry = it.next();
//						String value = new String(entry.getValue());
//						if (!value.isEmpty()) {
//							value += separator;
//						}
//						Float floatValue = markerSetAF.getFloat(index.set(i));
//						entry.setValue((value + floatValue.toString()).toCharArray());
//					}
//				} else if ((dataType == DataType.INT) && (varShape.length == 1)) {
//					ArrayInt.D1 markerSetAF = (ArrayInt.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");
//
//					int[] shape = markerSetAF.getShape();
//					Index index = markerSetAF.getIndex();
//					Iterator<Entry<MarkerKey, char[]>> it = ((Map<MarkerKey, char[]>) markerIdSetMap).entrySet().iterator();
//					for (int i = 0; i < shape[0]; i++) {
//						Entry<MarkerKey, char[]> entry = it.next();
//						String value = new String(entry.getValue());
//						if (!value.isEmpty()) {
//							value += separator;
//						}
//						Integer intValue = markerSetAF.getInt(index.set(i));
//						entry.setValue((value + intValue.toString()).toCharArray());
//					}
//				}
//			} catch (IOException ex) {
//				log.error("Cannot read data", ex);
//			} catch (InvalidRangeException ex) {
//				log.error("Cannot read data", ex);
//			}
//		}
//	}
//
//	/**
//	 * HELPER GETS DICTIONARY OF CURRENT MATRIX. IS CONCURRENT TO INSTANTIATED Map.
//	 * @deprecated unused
//	 */
//	private Map<MarkerKey, char[]> getDictionaryBases() throws IOException {
//
//		Map<MarkerKey, char[]> dictionary = new LinkedHashMap<MarkerKey, char[]>();
//
//		try {
//			Variable varBasesDict = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
//			if (null != varBasesDict) {
//				int[] dictShape = varBasesDict.getShape();
//
//				ArrayChar.D2 dictAlleles_ACD2 = (ArrayChar.D2) varBasesDict.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (dictShape[1] - 1) + ":1)");
//
//				Index index = dictAlleles_ACD2.getIndex();
//				Iterator<MarkerKey> it = markerIdSetMap.keySet().iterator();
//				for (int i = 0; i < dictShape[0]; i++) {
//					MarkerKey key = it.next();
//					StringBuilder alleles = new StringBuilder("");
//					// Get Alleles
//					for (int j = 0; j < dictShape[1]; j++) {
//						alleles.append(dictAlleles_ACD2.getChar(index.set(i, j)));
//					}
//					dictionary.put(key, alleles.toString().trim().toCharArray());
//				}
//			}
//		} catch (InvalidRangeException ex) {
//			throw new IOException("Cannot read data", ex);
//		}
//
//		return dictionary;
//	}
	//</editor-fold>
//
//	private <V> Map<MarkerKey, V> getMarkerIdSetMap() {
//		return ((Map<MarkerKey, V>) markerIdSetMap);
//	}

//	private Map<MarkerKey, byte[]> getMarkerIdSetMapByteArray() {
//		return getMarkerIdSetMap();
//	}
//
//	private Map<MarkerKey, char[]> getMarkerIdSetMapCharArray() {
//		return getMarkerIdSetMap();
//	}
//
//	private Map<MarkerKey, MarkerMetadata> getMarkerMetadata() {
//		return getMarkerIdSetMap();
//	}
//
//	private Map<MarkerKey, Integer> getMarkerIdSetMapInteger() {
//		return getMarkerIdSetMap();
//	}
//
//	private Set<MarkerKey> getMarkerKeys() {
//		return getMarkerIdSetMap().keySet();
//	}

//	@Override
//	public Iterator<Entry<MarkerKey, GenotypesList>> iterator() { XXX;
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//	}

//	@Override
//	public int size() {
//		return markerIdSetMap.size();
//	}

//	@Override
//	public GenotypesList get(int sampleIndex) {XXX; // should be implemented by parent already
//
//		if (markerIdSetMap == null) {
//			initFullMarkerIdSetMap();
//		}
//		try {
//			fillGTsForCurrentSampleIntoInitMap(sampleIndex);
//		} catch (IOException ex) {
//			throw new RuntimeException(ex);
//		}
//		Collection<byte[]> sampleGenotypes = getMarkerIdSetMapByteArray().values();
//
//		return genotyesListFactory.createGenotypesList(sampleGenotypes);
//	}
}

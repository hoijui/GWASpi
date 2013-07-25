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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
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
public class MarkerSet {

	private static final Logger log
			= LoggerFactory.getLogger(MarkerSet.class);

	// MARKERSET_MEATADATA
	private final ImportFormat technology; // platform
	private final int markerSetSize; // probe_nb
	private final NetcdfFile ncfile;
	private int startMkIdx;
	private int endMkIdx;
	private Map<MarkerKey, ?> markerIdSetMap;
	private Map<MarkerKey, ?> markerRsIdSetMap;

	public MarkerSet(MatrixMetadata matrixMetadata) throws IOException {

		this.technology = matrixMetadata.getTechnology();
		this.markerSetSize = matrixMetadata.getMarkerSetSize();
		this.ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
		this.startMkIdx = 0;
		this.endMkIdx = Integer.MIN_VALUE;
		this.markerIdSetMap = new LinkedHashMap<MarkerKey, Object>();
		this.markerRsIdSetMap = new LinkedHashMap<MarkerKey, Object>();
	}

	public MarkerSet(MatrixKey matrixKey) throws IOException {
		this(MatricesList.getMatrixMetadataById(matrixKey));
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (null != ncfile) {
				try {
					ncfile.close();
				} catch (IOException ex) {
					log.warn("Cannot close netCDF file: " + ncfile.getLocation(), ex);
				}
			}
		} finally {
			super.finalize();
		}
	}

	// ACCESSORS
	public ImportFormat getTechnology() {
		return technology;
	}

	public int getMarkerSetSize() {
		return markerSetSize;
	}

	//<editor-fold defaultstate="expanded" desc="MARKERSET INITILAIZERS">
	// USE MARKERID AS KEYS
	public void initFullMarkerIdSetMap() {
		startMkIdx = 0;
		endMkIdx = Integer.MIN_VALUE;
		initMarkerIdSetMap(startMkIdx, endMkIdx);
	}

	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(Map<String, IV> markerIdAlleles) {
		Map<MarkerKey, IV> reparsedData = new LinkedHashMap<MarkerKey, IV>();
		for (Map.Entry<String, IV> entry : markerIdAlleles.entrySet()) {
			reparsedData.put(MarkerKey.valueOf(entry.getKey()), entry.getValue());
		}
		return reparsedData;
	}

	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(ArrayByte.D2 markersAC) {
		Map<String, IV> markerIdAlleles = org.gwaspi.netCDF.operations.Utils.writeD2ArrayByteToMapKeys(markersAC);
		return wrapToMarkerKeyMap(markerIdAlleles);
	}

	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(ArrayChar.D2 markersAC, IV commonValue) {
		Map<String, IV> markerIdAlleles = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(markersAC, commonValue);
		return wrapToMarkerKeyMap(markerIdAlleles);
	}
	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(ArrayChar.D2 markersAC) {
		return wrapToMarkerKeyMap(markersAC, null);
	}

	public void initMarkerIdSetMap(int _startMkInd, int _endMkIdx) {
		startMkIdx = _startMkInd;
		endMkIdx = _endMkIdx;

		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERSET);

		if (var != null) {
			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				// KEEP INDEXES REAL
				if (startMkIdx < 0) {
					startMkIdx = 0;
				}
				if (endMkIdx < 0 || endMkIdx >= markerSetSize) {
					endMkIdx = markerSetSize - 1;
				}

				StringBuilder netCdfReadStrBldr = new StringBuilder(64);
				netCdfReadStrBldr
						.append("(")
						.append(startMkIdx)
						.append(":")
						.append(endMkIdx)
						.append(":1, 0:")
						.append(varShape[1] - 1)
						.append(":1)");
				String netCdfReadStr = netCdfReadStrBldr.toString();

				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
					markerIdSetMap = wrapToMarkerKeyMap(markerSetAC);
				}
				if (dataType == DataType.BYTE) {
					ArrayByte.D2 markerSetAC = (ArrayByte.D2) var.read(netCdfReadStr);
					markerIdSetMap = wrapToMarkerKeyMap(markerSetAC);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}
	}

	// USE RSID AS KEYS
	/**
	 * @deprecated is unused
	 */
	public void initFullRsIdSetMap() {
		startMkIdx = 0;
		endMkIdx = Integer.MIN_VALUE;
		initRsIdSetMap(startMkIdx, endMkIdx);
	}

	/**
	 * @deprecated is unused
	 */
	public void initRsIdSetMap(int _startMkInd, int _endMkIdx) {
		startMkIdx = _startMkInd;
		endMkIdx = _endMkIdx;

		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_RSID);

		if (var != null) {

			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				// KEEP INDEXES REAL
				if (startMkIdx == Integer.MIN_VALUE) {
					startMkIdx = 0;
				}
				if (endMkIdx == Integer.MIN_VALUE) {
					endMkIdx = markerSetSize - 1;
				}

				StringBuilder netCdfReadStrBldr = new StringBuilder(64);
				netCdfReadStrBldr
						.append("(")
						.append(startMkIdx)
						.append(":")
						.append(endMkIdx)
						.append(":1, 0:")
						.append(varShape[1] - 1)
						.append(":1)");
				String netCdfReadStr = netCdfReadStrBldr.toString();

				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
					markerRsIdSetMap = wrapToMarkerKeyMap(markerSetAC);
				}
				if (dataType == DataType.BYTE) {
					ArrayByte.D2 markerSetAC = (ArrayByte.D2) var.read(netCdfReadStr);
					markerRsIdSetMap = wrapToMarkerKeyMap(markerSetAC);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="CHROMOSOME INFO">
	/**
     * This Method is safe to return an independent Map.
	 * The size of this Map is very small.
	 */
	public Map<MarkerKey, int[]> getChrInfoSetMap() {

		Map<MarkerKey, int[]> chrInfoMap = new LinkedHashMap<MarkerKey, int[]>();

		// GET NAMES OF CHROMOSOMES
		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX);
		if (var != null) {
			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();
			try {
				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (varShape[0] - 1) + ":1, 0:7:1)");
					chrInfoMap = wrapToMarkerKeyMap(markerSetAC);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}

			// GET INFO FOR EACH CHROMOSOME
			var = ncfile.findVariable(cNetCDF.Variables.VAR_CHR_INFO); // Nb of markers, first physical position, last physical position, end index number in MarkerSet
			if (null == var) {
				return null;
			}
			dataType = var.getDataType();
			varShape = var.getShape();

			try {
				if (dataType == DataType.INT) {
					ArrayInt.D2 chrSetAI = (ArrayInt.D2) var.read("(0:" + (varShape[0] - 1) + ":1, 0:3:1)");
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayIntToMapValues(chrSetAI, chrInfoMap);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		} else {
			return null;
		}

		return chrInfoMap;
	}

	public static String getChrByMarkerIndex(Map<MarkerKey, int[]> chrInfoMap, int markerIndex) {
		String result = null;
		for (Map.Entry<MarkerKey, int[]> entry : chrInfoMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			int[] value = entry.getValue();
			if ((markerIndex <= value[3]) && (result == null)) {
				result = markerKey.getMarkerId();
			}
		}
		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="MARKERSET FILLERS">
	public void fillGTsForCurrentSampleIntoInitMap(int sampleNb) throws IOException {

		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_GENOTYPES);

		if (var != null) {

			int[] varShape = var.getShape();

			try {
				// KEEP INDEXES REAL
				if (startMkIdx < 0) {
					startMkIdx = 0;
				}
				if (endMkIdx < 0 || endMkIdx >= markerSetSize) {
					endMkIdx = markerSetSize - 1;
				}

				StringBuilder netCdfReadStrBldr = new StringBuilder(64);
				netCdfReadStrBldr
						.append("(")
						.append(sampleNb)
						.append(":")
						.append(sampleNb)
						.append(":1, ")
						.append(startMkIdx)
						.append(":")
						.append(endMkIdx)
						.append(":1, " + "0:")
						.append(varShape[2] - 1)
						.append(":1)");
				String netCdfReadStr = netCdfReadStrBldr.toString();

				ArrayByte.D3 gt_ACD3 = (ArrayByte.D3) var.read(netCdfReadStr);

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
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayByteToMapValues(gt_ACD2, (Map<MarkerKey, byte[]>) markerIdSetMap);
				} else if (reducer == 2) {
					ArrayByte.D1 gt_ACD1 = (ArrayByte.D1) gt_ACD3.reduce();
					org.gwaspi.netCDF.operations.Utils.writeD1ArrayByteToMapValues(gt_ACD1, (Map<MarkerKey, char[]>) markerIdSetMap);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}
	}

	public void fillInitMapWithVariable(String variable) {

		Variable var = ncfile.findVariable(variable);
		if (var != null) {
			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			StringBuilder netCdfReadStrBldr = new StringBuilder(64);
			netCdfReadStrBldr
					.append("(")
					.append(startMkIdx)
					.append(":")
					.append(endMkIdx)
					.append(":1");
			if (varShape.length == 2) {
				netCdfReadStrBldr
						.append(", 0:")
						.append(varShape[1] - 1)
						.append(":1");
			}
			netCdfReadStrBldr.append(")");
			String netCdfReadStr = netCdfReadStrBldr.toString();

			try {
				if ((dataType == DataType.CHAR) && (varShape.length == 2)) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(markerSetAC, (Map<MarkerKey, char[]>) markerIdSetMap);
				}
				if (dataType == DataType.DOUBLE) {
					if (varShape.length == 1) {
						ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read(netCdfReadStr);
						org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToMapValues(markerSetAF, (Map<MarkerKey, Double>) markerIdSetMap);
					}
					if (varShape.length == 2) {
						ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read(netCdfReadStr);
						org.gwaspi.netCDF.operations.Utils.writeD2ArrayDoubleToMapValues(markerSetAF, (Map<MarkerKey, double[]>) markerIdSetMap);
					}
				}
				if (dataType == DataType.INT) {
					if (varShape.length == 1) {
						ArrayInt.D1 markerSetAD = (ArrayInt.D1) var.read(netCdfReadStr);
						org.gwaspi.netCDF.operations.Utils.writeD1ArrayIntToMapValues(markerSetAD, (Map<MarkerKey, Integer>) markerIdSetMap);
					}
					if (varShape.length == 2) {
						ArrayInt.D2 markerSetAD = (ArrayInt.D2) var.read(netCdfReadStr);
						org.gwaspi.netCDF.operations.Utils.writeD2ArrayIntToMapValues(markerSetAD, (Map<MarkerKey, int[]>) markerIdSetMap);
					}
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}
	}

	/**
	 * Fills markerIdSetMap with values of type MarkerMetadata,
	 * with only chr and pos set.
	 */
	public void fillMarkerSetMapWithChrAndPos() {

		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
		if (var != null) {
			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				if ((dataType == DataType.CHAR) && (varShape.length == 2)) {
					StringBuilder netCdfReadStrBldr = new StringBuilder(64);
					netCdfReadStrBldr
							.append("(")
							.append(startMkIdx)
							.append(":")
							.append(endMkIdx)
							.append(":1, 0:")
							.append(varShape[1] - 1)
							.append(":1)");
					String netCdfReadStr = netCdfReadStrBldr.toString();

					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(markerSetAC, (Map<MarkerKey, char[]>) markerIdSetMap);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}

		var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_POS);
		if (var != null) {
			DataType dataType = var.getDataType();
//			int[] varShape = var.getShape();

			try {
				if (dataType == DataType.INT) {
					ArrayInt.D1 markerSetAI = (ArrayInt.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");

					int[] shape = markerSetAI.getShape();
					Index index = markerSetAI.getIndex();
					Iterator<Entry<MarkerKey, Object>> it = ((Map<MarkerKey, Object>) markerIdSetMap).entrySet().iterator();
					for (int i = 0; i < shape[0]; i++) {
						Entry<MarkerKey, Object> entry = it.next();
						MarkerMetadata chrInfo = new MarkerMetadata(
								new String((char[]) entry.getValue()), // chr
								markerSetAI.getInt(index.set(i))); // pos
						entry.setValue(chrInfo);
					}
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}
	}

	private void fillWith(Object value) {
		fillWith((Map<MarkerKey, Object>)markerIdSetMap, value);
	}
	public void fillWith(char[] value) {
		fillWith((Object)value);
	}
	public void fillWith(byte[] value) {
		fillWith((Object)value);
	}
	public void fillWith(int[] value) {
		fillWith((Object)value);
	}
	public void fillWith(double[] value) {
		fillWith((Object)value);
	}
	public void fillWith(Integer value) {
		fillWith((Object)value);
	}
	public void fillWith(Double value) {
		fillWith((Object)value);
	}

	private static <K, V> void fillWith(Map<K, V> map, V defaultVal) {

		for (Map.Entry<K, V> entry : map.entrySet()) {
			entry.setValue(defaultVal);
		}
	}

	// HELPERS TO TRANSFER VALUES FROM ONE Map TO ANOTHER
	public static <K, V> void replaceWithValuesFrom(Map<K, V> toBeModified, Map<K, V> newValuesSource) {

		for (Map.Entry<K, V> entry : toBeModified.entrySet()) {
			entry.setValue(newValuesSource.get(entry.getKey()));
		}
	}

	// HELPER TO APPEND VALUE TO AN EXISTING Map CHAR VALUE
	public void appendVariableToMarkerSetMapValue(String variable, String separator) {

		Variable var = ncfile.findVariable(variable);
		if (var != null) {
			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();
			try {
				if ((dataType == DataType.CHAR) && (varShape.length == 2)) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");

					int[] shape = markerSetAC.getShape();
					Index index = markerSetAC.getIndex();
					Iterator<Entry<MarkerKey, char[]>> it = ((Map<MarkerKey, char[]>) markerIdSetMap).entrySet().iterator();
					for (int i = 0; i < shape[0]; i++) {
						Entry<MarkerKey, char[]> entry = it.next();
						String value = new String(entry.getValue());
						if (!value.isEmpty()) {
							value += separator;
						}
						StringBuilder newValue = new StringBuilder();
						for (int j = 0; j < shape[1]; j++) {
							newValue.append(markerSetAC.getChar(index.set(i, j)));
						}
						entry.setValue((value + newValue.toString().trim()).toCharArray());
					}
				} else if ((dataType == DataType.FLOAT) && (varShape.length == 1)) {
					ArrayFloat.D1 markerSetAF = (ArrayFloat.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");

					int[] shape = markerSetAF.getShape();
					Index index = markerSetAF.getIndex();
					Iterator<Entry<MarkerKey, char[]>> it = ((Map<MarkerKey, char[]>) markerIdSetMap).entrySet().iterator();
					for (int i = 0; i < shape[0]; i++) {
						Entry<MarkerKey, char[]> entry = it.next();
						String value = new String(entry.getValue());
						if (!value.isEmpty()) {
							value += separator;
						}
						Float floatValue = markerSetAF.getFloat(index.set(i));
						entry.setValue((value + floatValue.toString()).toCharArray());
					}
				} else if ((dataType == DataType.INT) && (varShape.length == 1)) {
					ArrayInt.D1 markerSetAF = (ArrayInt.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");

					int[] shape = markerSetAF.getShape();
					Index index = markerSetAF.getIndex();
					Iterator<Entry<MarkerKey, char[]>> it = ((Map<MarkerKey, char[]>) markerIdSetMap).entrySet().iterator();
					for (int i = 0; i < shape[0]; i++) {
						Entry<MarkerKey, char[]> entry = it.next();
						String value = new String(entry.getValue());
						if (!value.isEmpty()) {
							value += separator;
						}
						Integer intValue = markerSetAF.getInt(index.set(i));
						entry.setValue((value + intValue.toString()).toCharArray());
					}
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}
	}

	/**
	 * HELPER GETS DICTIONARY OF CURRENT MATRIX. IS CONCURRENT TO INSTANTIATED Map
	 */
	public Map<MarkerKey, char[]> getDictionaryBases() throws IOException {
		Map<MarkerKey, char[]> dictionnary = new LinkedHashMap<MarkerKey, char[]>();
		try {
			Variable varBasesDict = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
			if (null != varBasesDict) {
				int[] dictShape = varBasesDict.getShape();

				ArrayChar.D2 dictAlleles_ACD2 = (ArrayChar.D2) varBasesDict.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (dictShape[1] - 1) + ":1)");

				Index index = dictAlleles_ACD2.getIndex();
				Iterator<MarkerKey> it = markerIdSetMap.keySet().iterator();
				for (int i = 0; i < dictShape[0]; i++) {
					MarkerKey key = it.next();
					StringBuilder alleles = new StringBuilder("");
					// Get Alleles
					for (int j = 0; j < dictShape[1]; j++) {
						alleles.append(dictAlleles_ACD2.getChar(index.set(i, j)));
					}
					dictionnary.put(key, alleles.toString().trim().toCharArray());
				}
			}
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}
		return dictionnary;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="MARKERSET PICKERS">
	/**
	 * THESE Maps DO NOT CONTAIN SAME ITEMS AS INIT Map.
	 * RETURN Map OK
	 */
	public <V> Map<MarkerKey, V> pickValidMarkerSetItemsByValue(String variable, Set<V> criteria, boolean includes) {
		Map<MarkerKey, V> returnMap = new LinkedHashMap<MarkerKey, V>();
		this.fillInitMapWithVariable(variable);

		if (includes) {
			for (Map.Entry<MarkerKey, V> entry : ((Map<MarkerKey, V>) markerIdSetMap).entrySet()) {
				MarkerKey key = entry.getKey();
				V value = entry.getValue();
				if (criteria.contains(value)) {
					returnMap.put(key, value);
				}
			}
		} else {
			for (Map.Entry<MarkerKey, V> entry : ((Map<MarkerKey, V>) markerIdSetMap).entrySet()) {
				MarkerKey key = entry.getKey();
				V value = entry.getValue();
				if (!criteria.contains(value)) {
					returnMap.put(key, value);
				}
			}
		}

		return returnMap;
	}

	public <V> Map<MarkerKey, V> pickValidMarkerSetItemsByKey(Set<MarkerKey> criteria, boolean includes) {
		Map<MarkerKey, V> returnMap = new LinkedHashMap<MarkerKey, V>();

		if (includes) {
			for (Map.Entry<MarkerKey, V> entry : ((Map<MarkerKey, V>) markerIdSetMap).entrySet()) {
				MarkerKey key = entry.getKey();
				V value = entry.getValue();
				if (criteria.contains(key)) {
					returnMap.put(key, value);
				}
			}
		} else {
			for (Map.Entry<MarkerKey, V> entry : ((Map<MarkerKey, V>) markerIdSetMap).entrySet()) {
				MarkerKey key = entry.getKey();
				V value = entry.getValue();
				if (!criteria.contains(key)) {
					returnMap.put(key, value);
				}
			}
		}

		return returnMap;
	}
	//</editor-fold>

	private <V> Map<MarkerKey, V> getMarkerIdSetMap() {
		return ((Map<MarkerKey, V>) markerIdSetMap);
	}

	public Map<MarkerKey, byte[]> getMarkerIdSetMapByteArray() {
		return getMarkerIdSetMap();
	}

	public Map<MarkerKey, char[]> getMarkerIdSetMapCharArray() {
		return getMarkerIdSetMap();
	}

	public Map<MarkerKey, MarkerMetadata> getMarkerMetadata() {
		return getMarkerIdSetMap();
	}

	public Map<MarkerKey, Integer> getMarkerIdSetMapInteger() {
		return getMarkerIdSetMap();
	}

	public Set<MarkerKey> getMarkerKeys() {
		return getMarkerIdSetMap().keySet();
	}

	/**
	 * @deprecated is unused
	 */
	public <V> Map<MarkerKey, V> getMarkerRsIdSetMap() {
		return ((Map<MarkerKey, V>) markerRsIdSetMap);
	}
}

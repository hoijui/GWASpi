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

package org.gwaspi.netCDF.operations;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public abstract class AbstractOperationSet<K, V> {

	private static final Logger log = LoggerFactory.getLogger(AbstractOperationSet.class);

	private final KeyFactory<K> keyFactory;
	private final OperationKey operationKey;
	private final int opIndexFrom;
	private final int opIndexTo;
	private Map<K, V> opSetMap;

	public AbstractOperationSet(OperationKey operationKey, KeyFactory<K> keyFactory) throws IOException {
		this(operationKey, keyFactory, -1, -1);
	}

	public AbstractOperationSet(OperationKey operationKey, KeyFactory<K> keyFactory, int opIndexFrom, int opIndexTo) throws IOException {

		this.operationKey = operationKey;
		this.opSetMap = null;
		this.keyFactory = keyFactory;
		this.opIndexFrom = opIndexFrom;
		this.opIndexTo = opIndexTo;
	}

	protected OperationKey getOperationKey() {
		return operationKey;
	}

	//<editor-fold defaultstate="expanded" desc="OPERATION-SET FETCHERS">
	private static <T, V> Map<T, V> wrapToKeyMap(ArrayChar.D2 markersOrSamplesAC, KeyFactory<T> keyFactory) {

		Map<String, V> keyStrs = NetCdfUtils.writeD2ArrayCharToMapKeys(markersOrSamplesAC, null);

		Map<T, V> reparsedData = new LinkedHashMap<T, V>();
		for (Map.Entry<String, V> entry : keyStrs.entrySet()) {
			reparsedData.put(keyFactory.decode(entry.getKey()), entry.getValue());
		}

		return reparsedData;
	}

	public Map<K, V> getOpSetMap() {

		NetcdfFile ncfile = null;
		try {
			OperationMetadata opMetadata = OperationsList.getOperation(operationKey);
			ncfile = NetcdfFile.open(OperationMetadata.generatePathToNetCdfFile(opMetadata).getAbsolutePath());
			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_OPSET);

			if (null == var) {
				return null;
			}

			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				final int opSetSize = varShape[0];
				Dimension fromTo = new Dimension(opIndexFrom, opIndexTo);
				NetCdfUtils.checkDimensions(opSetSize, fromTo);
				final int from = fromTo.width;
				final int to = fromTo.height;

				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + from + ":" + to + ":1, 0:" + (varShape[1] - 1) + ":1)");
					opSetMap = wrapToKeyMap(markerSetAC, keyFactory);
				}
			} catch (InvalidRangeException ex) {
				throw new IOException(ex);
			}
		} catch (IOException ex) {
			log.error("Cannot open file", ex);
		} finally {
			if (null != ncfile) {
				try {
					ncfile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file", ex);
				}
			}
		}

		return opSetMap;
	}

//	public Map<K, V> getMarkerRsIdSetMap() {
//
//		NetcdfFile ncfile = null;
//		Map<K, V> opRsIdSetMap = new LinkedHashMap<K, V>();
//		try {
//			ncfile = NetcdfFile.open(opMetadata.getPathToMatrix());
//			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
//
//			if (null == var) {
//				return null;
//			}
//
//			DataType dataType = var.getDataType();
//			int[] varShape = var.getShape();
//
//			try {
//				final int opSetSize = varShape[0];
//				Dimension fromTo = new Dimension(opIndexFrom, opIndexTo);
//				NetCdfUtils.checkDimensions(opSetSize, fromTo);
//				final int from = fromTo.width;
//				final int to = fromTo.height;
//
//				if (dataType == DataType.CHAR) {
//					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + from + ":" + to + ":1, 0:" + (varShape[1] - 1) + ":1)");
//					opRsIdSetMap = wrapToKeyMap(markerSetAC, keyFactory);
//				}
//			} catch (InvalidRangeException ex) {
//				throw new IOException(ex);
//			}
//		} catch (IOException ex) {
//			log.error("Cannot open file", ex);
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
//		return opRsIdSetMap;
//	}

//	public Map<SampleKey, V> getImplicitSetMap() {
//
//		NetcdfFile ncfile = null;
//		Map<SampleKey, V> implicitSetMap = new LinkedHashMap<SampleKey, V>();
//		try {
//			ncfile = NetcdfFile.open(opMetadata.getPathToMatrix());
//			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_IMPLICITSET);
//
//			if (null == var) {
//				return null;
//			}
//
//			final int[] varShape = var.getShape();
//			try {
//				final int implicitSetSize = varShape[0];
//				ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:" + (implicitSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
//
//				implicitSetMap = wrapToKeyMap(sampleSetAC, new SampleKeyFactory(operationKey.getParentMatrixKey().getStudyKey()));
//			} catch (InvalidRangeException ex) {
//				throw new IOException(ex);
//			}
//		} catch (IOException ex) {
//			log.error("Cannot open file", ex);
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
//		return implicitSetMap;
//	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="OPERATION-SET FILLERS">
	public Map<K, V> fillOpSetMapWithVariable(NetcdfFile ncfile, String variable) {

		try {
			NetCdfUtils.readVariable(ncfile, variable, -1, -1, null, opSetMap);
		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		}

		return opSetMap;
	}

	public void fillOpSetMapWithDefaultValue(V defaultVal) {
		fillMapWithDefaultValue(opSetMap, defaultVal);
	}

	public static <V> void fillMapWithDefaultValue(Map<?, V> map, V defaultVal) {
		for (Map.Entry<?, V> entry : map.entrySet()) {
			entry.setValue(defaultVal);
		}
	}

	public static <K, V> Map<K, V> fillMapWithKeyAndDefaultValue(Collection<K> keys, V defaultVal) {

		Map<K, V> result = new LinkedHashMap<K, V>(keys.size());

		for (K key : keys) {
			result.put(key, defaultVal);
		}

		return result;
	}

	public List getListWithVariable(NetcdfFile ncfile, String variable) {

		List list = new ArrayList();

		Variable var = ncfile.findVariable(variable);
		if (null == var) {
			return null;
		}

		DataType dataType = var.getDataType();
		final int[] varShape = var.getShape();
		final int opSetSize = varShape[0];
		Dimension fromTo = new Dimension(opIndexFrom, opIndexTo);
		NetCdfUtils.checkDimensions(opSetSize, fromTo);
		final int from = fromTo.width;
		final int to = fromTo.height;
		String fetchVarStr;
		if (varShape.length == 1) {
			fetchVarStr = "(" + from + ":" + to + ":1)";
		} else if (varShape.length == 2) {
			fetchVarStr = "(" + from + ":" + to + ":1, 0:" + (varShape[1] - 1) + ":1)";
		} else {
			throw new IllegalArgumentException();
		}

		((ArrayList) list).ensureCapacity(from - to);

		try {
			if ((dataType == DataType.CHAR) && (varShape.length == 2)) {
				ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read(fetchVarStr);
				list = NetCdfUtils.writeD2ArrayCharToList(markerSetAC);
			}
			if (dataType == DataType.DOUBLE) {
				if (varShape.length == 1) {
					ArrayDouble.D1 markerSetAD = (ArrayDouble.D1) var.read(fetchVarStr);
					list = NetCdfUtils.writeD1ArrayDoubleToList(markerSetAD);
				} else if (varShape.length == 2) {
					ArrayDouble.D2 markerSetAD = (ArrayDouble.D2) var.read(fetchVarStr);
					list = NetCdfUtils.writeD2ArrayDoubleToList(markerSetAD);
				}
			}
		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}

		return list;
	}

//	public Map<K, Object> appendVariableToMarkerSetMapValue(NetcdfFile ncfile, String variable, String separator) {
//
//		Variable var = ncfile.findVariable(variable);
//
//		if (null == var) {
//			return null;
//		}
//		DataType dataType = var.getDataType();
//		int[] varShape = var.getShape();
//
//		try {
//			opSetSize = varShape[0];
//			if ((dataType == DataType.CHAR) && (varShape.length == 2)) {
//				ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)"); // FIXME needs to use from and to!
//
//				int[] shape = markerSetAC.getShape();
//				Index index = markerSetAC.getIndex();
//				Iterator<? extends Map.Entry<?, V>> it = opSetMap.entrySet().iterator();
//				for (int i = 0; i < shape[0]; i++) {
//					Map.Entry<?, V> entry = it.next();
//					String value = entry.getValue().toString();
//					if (!value.isEmpty()) {
//						value += separator;
//					}
//					StringBuilder newValue = new StringBuilder();
//					for (int j = 0; j < shape[1]; j++) {
//						newValue.append(markerSetAC.getChar(index.set(i, j)));
//					}
//					entry.setValue(value + newValue.toString().trim());
//				}
//			}
//			if ((dataType == DataType.FLOAT) && (varShape.length == 1)) {
//				ArrayFloat.D1 markerSetAF = (ArrayFloat.D1) var.read("(0:" + (opSetSize - 1) + ":1)");
//
//				int[] shape = markerSetAF.getShape();
//				Index index = markerSetAF.getIndex();
//				Iterator<? extends Map.Entry<?, V>> it = opSetMap.entrySet().iterator();
//				for (int i = 0; i < shape[0]; i++) {
//					Map.Entry<?, V> entry = it.next();
//					String value = entry.getValue().toString();
//					if (!value.isEmpty()) {
//						value += separator;
//					}
//					Float floatValue = markerSetAF.getFloat(index.set(i));
//					entry.setValue(value + floatValue.toString());
//				}
//			}
//			if ((dataType == DataType.INT) && (varShape.length == 1)) {
//				ArrayInt.D1 markerSetAF = (ArrayInt.D1) var.read("(0:" + (opSetSize - 1) + ":1)");
//
//				int[] shape = markerSetAF.getShape();
//				Index index = markerSetAF.getIndex();
//				Iterator<? extends Map.Entry<?, V>> it = opSetMap.entrySet().iterator();
//				for (int i = 0; i < shape[0]; i++) {
//					Map.Entry<?, V> entry = it.next();
//					String value = entry.getValue().toString();
//					if (!value.isEmpty()) {
//						value += separator;
//					}
//					Integer intValue = markerSetAF.getInt(index.set(i));
//					entry.setValue(value + intValue.toString());
//				}
//			}
//		} catch (IOException ex) {
//			log.error("Cannot read data", ex);
//		} catch (InvalidRangeException ex) {
//			log.error("Cannot read data", ex);
//		}
//
//		return opSetMap;
//	}
	//</editor-fold>
}

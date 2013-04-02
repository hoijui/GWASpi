package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class AbstractOperationSet<K, V> {

	private static final Logger log = LoggerFactory.getLogger(AbstractOperationSet.class);

	private int opSetSize = 0;
	private int implicitSetSize = 0;
	private OperationMetadata opMetadata;
	private Map<K, V> opSetMap = null;
	private final KeyFactory<K> keyFactory;

	public AbstractOperationSet(int studyId, int opId, KeyFactory<K> keyFactory) throws IOException {
		this.opMetadata = OperationsList.getOperationMetadata(opId);
		this.opSetSize = opMetadata.getOpSetSize();
		this.keyFactory = keyFactory;
	}

	public int getOpSetSize() {
		return opSetSize;
	}

	public int getImplicitSetSize() {
		return implicitSetSize;
	}

	protected OperationMetadata getOperationMetadata() {
		return opMetadata;
	}

	//<editor-fold defaultstate="expanded" desc="OPERATION-SET FETCHERS">
	private static <T, V> Map<T, V> wrapToKeyMap(ArrayChar.D2 markersOrSamplesAC, KeyFactory<T> keyFactory) {
		Map<String, V> keyStrs = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(markersOrSamplesAC, null);
		Map<T, V> reparsedData = new LinkedHashMap<T, V>();
		for (Map.Entry<String, V> entry : keyStrs.entrySet()) {
			reparsedData.put(keyFactory.decode(entry.getKey()), entry.getValue());
		}
		return reparsedData;
	}

	public Map<K, V> getOpSetMap() {
		NetcdfFile ncfile = null;

		try {
			ncfile = NetcdfFile.open(opMetadata.getPathToMatrix());
			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_OPSET);

			if (null == var) {
				return null;
			}

			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				opSetSize = varShape[0];

				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					opSetMap = wrapToKeyMap(markerSetAC, keyFactory);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
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

	public Map<K, V> getMarkerRsIdSetMap() {
		NetcdfFile ncfile = null;
		Map<K, V> opRsIdSetMap = new LinkedHashMap<K, V>();

		try {
			ncfile = NetcdfFile.open(opMetadata.getPathToMatrix());
			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_RSID);

			if (null == var) {
				return null;
			}

			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				opSetSize = varShape[0];
				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					opRsIdSetMap = wrapToKeyMap(markerSetAC, keyFactory);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
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

		return opRsIdSetMap;
	}

	public Map<SampleKey, V> getImplicitSetMap() {
		NetcdfFile ncfile = null;
		Map<SampleKey, V> implicitSetMap = new LinkedHashMap<SampleKey, V>();

		try {
			ncfile = NetcdfFile.open(opMetadata.getPathToMatrix());
			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_IMPLICITSET);

			if (null == var) {
				return null;
			}

			int[] varShape = var.getShape();
			try {
				implicitSetSize = varShape[0];
				ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:" + (implicitSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");

				implicitSetMap = wrapToKeyMap(sampleSetAC, SampleKey.KEY_FACTORY);
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
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

		return implicitSetMap;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="OPERATION-SET FILLERS">
	public Map<K, V> fillOpSetMapWithVariable(NetcdfFile ncfile, String variable) {

		Variable var = ncfile.findVariable(variable);

		if (null == var) {
			return null;
		}

		DataType dataType = var.getDataType();
		int[] varShape = var.getShape();
		try {
			opSetSize = varShape[0];
			if ((dataType == DataType.CHAR) && (varShape.length == 2)) {
				ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
				org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(markerSetAC, (Map<K, char[]>)opSetMap);
			}
			if (dataType == DataType.DOUBLE) {
				if (varShape.length == 1) {
					ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read("(0:" + (opSetSize - 1) + ":1)");
					org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToMapValues(markerSetAF, (Map<K, Double>)opSetMap);
				} else if (varShape.length == 2) {
					ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1))");
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayDoubleToMapValues(markerSetAF, (Map<K, double[]>)opSetMap);
				}
			}
			if (dataType == DataType.INT) {
				if (varShape.length == 1) {
					ArrayInt.D1 markerSetAD = (ArrayInt.D1) var.read("(0:" + (opSetSize - 1) + ":1)");
					org.gwaspi.netCDF.operations.Utils.writeD1ArrayIntToMapValues(markerSetAD, (Map<K, Integer>)opSetMap);
				} else if (varShape.length == 2) {
					ArrayInt.D2 markerSetAD = (ArrayInt.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1))");
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayIntToMapValues(markerSetAD, (Map<K, int[]>)opSetMap);
				}
			}
		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		} catch (InvalidRangeException ex) {
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

	public Map<String, Object> fillWrMapWithRdMapValue(Map<String, Object> wrMap, Map<String, Object> rdMap) {
		for (Map.Entry<String, Object> entry : wrMap.entrySet()) {
			String key = entry.getKey();
			Object value = rdMap.get(key);
			entry.setValue(value);
		}
		return wrMap;
	}

	public List getListWithVariable(NetcdfFile ncfile, String variable) {

		List list = new ArrayList();

		Variable var = ncfile.findVariable(variable);
		if (null == var) {
			return null;
		}

		DataType dataType = var.getDataType();
		int[] varShape = var.getShape();
		opSetSize = varShape[0];
		((ArrayList) list).ensureCapacity(opSetSize);

		try {
			if ((dataType == DataType.CHAR) && (varShape.length == 2)) {
				ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
				list = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToList(markerSetAC);
			}
			if (dataType == DataType.DOUBLE) {
				if (varShape.length == 1) {
					ArrayDouble.D1 markerSetAD = (ArrayDouble.D1) var.read("(0:" + (opSetSize - 1) + ":1)");
					list = org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToList(markerSetAD);
				} else if (varShape.length == 2) {
					ArrayDouble.D2 markerSetAD = (ArrayDouble.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					list = org.gwaspi.netCDF.operations.Utils.writeD2ArrayDoubleToList(markerSetAD);
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
//				ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
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

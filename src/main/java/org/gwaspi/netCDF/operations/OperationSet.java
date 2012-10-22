package org.gwaspi.netCDF.operations;

import org.gwaspi.model.OperationMetadata;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OperationSet {

	private static final Logger log = LoggerFactory.getLogger(OperationSet.class);

	// MARKERSET_MEATADATA
	private int opSetSize = 0;
	private int implicitSetSize = 0;
	private OperationMetadata opMetadata;
	private Map<String, Object> opSetMap = new LinkedHashMap<String, Object>();
	private Map<String, Object> opRsIdSetMap = new LinkedHashMap<String, Object>();

	public OperationSet(int studyId, int opId) throws IOException {
		opMetadata = new OperationMetadata(opId);
		opSetSize = opMetadata.getOpSetSize();
	}

	// ACCESSORS
	public int getOpSetSize() {
		return opSetSize;
	}

	public int getImplicitSetSize() {
		return implicitSetSize;
	}

	//<editor-fold defaultstate="collapsed" desc="OPERATION-SET FETCHERS">
	public Map<String, Object> getOpSetMap() {
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
					opSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(markerSetAC);
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

	public Map<String, Object> getMarkerRsIdSetMap() {
		NetcdfFile ncfile = null;

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
					opSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(markerSetAC);
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

	Map<String, Object> getImplicitSetMap() {
		NetcdfFile ncfile = null;
		Map<String, Object> implicitSetMap = new LinkedHashMap<String, Object>();

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

				implicitSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(sampleSetAC);

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

	//<editor-fold defaultstate="collapsed" desc="CHROMOSOME INFO">
	public Map<String, Object> getChrInfoSetMap() {
		NetcdfFile ncfile = null;
		Map<String, Object> chrInfoMap = new LinkedHashMap<String, Object>();

		try {
			ncfile = NetcdfFile.open(opMetadata.getPathToMatrix());

			// GET NAMES OF CHROMOSOMES
			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX);

			if (null == var) {
				return null;
			}

			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (varShape[0] - 1) + ":1, 0:7:1)");
					chrInfoMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(markerSetAC);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}

			//GET INFO FOR EACH CHROMOSOME
			var = ncfile.findVariable(cNetCDF.Variables.VAR_CHR_INFO); //Nb of markers, first physical position, last physical position, start index number in MarkerSet
			dataType = var.getDataType();
			varShape = var.getShape();

			if (null == var) {
				return null;
			}
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

		return chrInfoMap;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="OPERATION-SET FILLERS">
	public Map<String, Object> fillOpSetMapWithVariable(NetcdfFile ncfile, String variable) {

		Variable var = ncfile.findVariable(variable);

		if (null == var) {
			return null;
		}

		DataType dataType = var.getDataType();
		int[] varShape = var.getShape();
		try {
			opSetSize = varShape[0];
			if (dataType == DataType.CHAR) {
				if (varShape.length == 2) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(markerSetAC, opSetMap);
				}
			}
			if (dataType == DataType.DOUBLE) {
				if (varShape.length == 1) {
					ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read("(0:" + (opSetSize - 1) + ":1)");
					org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToMapValues(markerSetAF, opSetMap);
				}
				if (varShape.length == 2) {
					ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1))");
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayDoubleToMapValues(markerSetAF, opSetMap);
				}
			}
			if (dataType == DataType.INT) {
				if (varShape.length == 1) {
					ArrayInt.D1 markerSetAD = (ArrayInt.D1) var.read("(0:" + (opSetSize - 1) + ":1)");
					org.gwaspi.netCDF.operations.Utils.writeD1ArrayIntToMapValues(markerSetAD, opSetMap);
				}
				if (varShape.length == 2) {
					ArrayInt.D2 markerSetAD = (ArrayInt.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1))");
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayIntToMapValues(markerSetAD, opSetMap);
				}
			}
		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}

		return opSetMap;
	}

	public void fillMapWithDefaultValue(Map<String, Object> map, Object defaultVal) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			entry.setValue(defaultVal);
		}
	}

	public Map<String, Object> fillWrMapWithRdMapValue(Map<String, Object> wrMap, Map<String, Object> rdMap) {
		for (Map.Entry<String, Object> entry : wrMap.entrySet()) {
			String key = entry.getKey();
			Object value = rdMap.get(key);
			entry.setValue(value);
		}
		return wrMap;
	}

	public List getALWithVariable(NetcdfFile ncfile, String variable) {

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
			if (dataType == DataType.CHAR) {
				if (varShape.length == 2) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					list = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToList(markerSetAC);
				}
			}
			if (dataType == DataType.DOUBLE) {
				if (varShape.length == 1) {
					ArrayDouble.D1 markerSetAD = (ArrayDouble.D1) var.read("(0:" + (opSetSize - 1) + ":1)");
					list = org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToList(markerSetAD);
				}
				if (varShape.length == 2) {
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

	public Map<String, Object> appendVariableToMarkerSetMapValue(NetcdfFile ncfile, String variable, String separator) {

		Variable var = ncfile.findVariable(variable);

		if (null == var) {
			return null;
		}
		DataType dataType = var.getDataType();
		int[] varShape = var.getShape();

		try {
			opSetSize = varShape[0];
			if (dataType == DataType.CHAR) {
				if (varShape.length == 2) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (opSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");

					int[] shape = markerSetAC.getShape();
					Index index = markerSetAC.getIndex();
					Iterator<String> it = opSetMap.keySet().iterator();
					for (int i = 0; i < shape[0]; i++) {
						String key = it.next();
						String value = opSetMap.get(key).toString();
						if (!value.isEmpty()) {
							value += separator;
						}
						StringBuilder newValue = new StringBuilder();
						for (int j = 0; j < shape[1]; j++) {
							newValue.append(markerSetAC.getChar(index.set(i, j)));
						}
						opSetMap.put(key, value + newValue.toString().trim());
					}
				}
			}
			if (dataType == DataType.FLOAT) {
				if (varShape.length == 1) {
					ArrayFloat.D1 markerSetAF = (ArrayFloat.D1) var.read("(0:" + (opSetSize - 1) + ":1)");

					int[] shape = markerSetAF.getShape();
					Index index = markerSetAF.getIndex();
					Iterator<String> it = opSetMap.keySet().iterator();
					for (int i = 0; i < shape[0]; i++) {
						String key = it.next();
						String value = opSetMap.get(key).toString();
						if (!value.isEmpty()) {
							value += separator;
						}
						Float floatValue = markerSetAF.getFloat(index.set(i));
						opSetMap.put(key, value + floatValue.toString());
					}
				}
			}
			if (dataType == DataType.INT) {
				if (varShape.length == 1) {
					ArrayInt.D1 markerSetAF = (ArrayInt.D1) var.read("(0:" + (opSetSize - 1) + ":1)");

					int[] shape = markerSetAF.getShape();
					Index index = markerSetAF.getIndex();
					Iterator<String> it = opSetMap.keySet().iterator();
					for (int i = 0; i < shape[0]; i++) {
						String key = it.next();
						String value = opSetMap.get(key).toString();
						if (!value.isEmpty()) {
							value += separator;
						}
						Integer intValue = markerSetAF.getInt(index.set(i));
						opSetMap.put(key, value + intValue.toString());
					}
				}
			}

		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}

		return opSetMap;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="OPERATION-SET PICKERS">
	public Map<String, Object> pickValidMarkerSetItemsByValue(NetcdfFile ncfile, String variable, Set<Object> criteria, boolean includes) {
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		Map<String, Object> readMap = fillOpSetMapWithVariable(ncfile, variable);

		if (includes) {
			for (Map.Entry<String, Object> entry : readMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (criteria.contains(value)) {
					returnMap.put(key, value);
				}
			}
		} else {
			for (Map.Entry<String, Object> entry : readMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (!criteria.contains(value)) {
					returnMap.put(key, value);
				}
			}
		}

		return returnMap;
	}

	public Map<String, Object> pickValidMarkerSetItemsByKey(Map<String, Object> map, Set<Object> criteria, boolean includes) {
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();

		if (includes) {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (criteria.contains(key)) {
					returnMap.put(key, value);
				}
			}
		} else {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (!criteria.contains(key)) {
					returnMap.put(key, value);
				}
			}
		}

		return returnMap;
	}
	//</editor-fold>
}

package org.gwaspi.netCDF.markers;

import org.gwaspi.constants.cNetCDF;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
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
 * This modification of the MarkerSet can create instances of the MarkerSetLHM
 * The markerSet Object has several methods to initialize it's LHM, which can later
 * be filled with it's corresponding values.
 * The LHM is not copied or passed as a parameter to the fillers.
 * The LHMs are made public so that they can be referenced from other classes,
 * avoiding duplication of large LHM datasets in memory.
 *
 * The matrix netCDF file is opened at creation of the MarkerSet and closed
 * at finalization of the class. No need to pass a netCDF handler anymore.
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MarkerSet_opt {

	private final static Logger log
			= LoggerFactory.getLogger(MarkerSet_opt.class);

	// MARKERSET_MEATADATA
	private String technology = ""; // platform
	private int markerSetSize = 0; // probe_nb
	private MatrixMetadata matrixMetadata;
	private NetcdfFile ncfile = null;
	private int startMkIdx = 0;
	private int endMkIdx = Integer.MIN_VALUE;
	private Map<String, Object> markerIdSetLHM = new LinkedHashMap<String, Object>();
	private Map<String, Object> markerRsIdSetLHM = new LinkedHashMap<String, Object>();

	public MarkerSet_opt(int studyId, int matrixId) throws IOException {
		matrixMetadata = new MatrixMetadata(matrixId);
		technology = matrixMetadata.getTechnology();
		markerSetSize = matrixMetadata.getMarkerSetSize();


		ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
	}

	public MarkerSet_opt(int studyId, String netCDFPath, String netCDFName) throws IOException {
		matrixMetadata = new MatrixMetadata(netCDFPath, studyId, netCDFName);
		technology = matrixMetadata.getTechnology();
		markerSetSize = matrixMetadata.getMarkerSetSize();

		ncfile = NetcdfFile.open(netCDFPath);
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (null != ncfile) {
				try {
					ncfile.close();
				} catch (IOException ex) {
					log.error("Cannot close netCDF file: " + ncfile.getLocation(), ex);
				}
			}
		} finally {
			super.finalize();
		}
	}

	// ACCESSORS
	public String getTechnology() {
		return technology;
	}

	public int getMarkerSetSize() {
		return markerSetSize;
	}

	//<editor-fold defaultstate="collapsed" desc="MARKERSET INITILAIZERS">
	//USE MARKERID AS KEYS
	public void initFullMarkerIdSetLHM() {
		startMkIdx = 0;
		endMkIdx = Integer.MIN_VALUE;
		initMarkerIdSetLHM(startMkIdx, endMkIdx);
	}

	public void initMarkerIdSetLHM(int _startMkInd, int _endMkIdx) {
		startMkIdx = _startMkInd;
		endMkIdx = _endMkIdx;

		Variable var = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERSET);

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

				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
				}
				if (dataType == DataType.BYTE) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}
	}

	// USE RSID AS KEYS
	public void initFullRsIdSetLHM() {
		startMkIdx = 0;
		endMkIdx = Integer.MIN_VALUE;
		initRsIdSetLHM(startMkIdx, endMkIdx);
	}

	public void initRsIdSetLHM(int _startMkInd, int _endMkIdx) {
		startMkIdx = _startMkInd;
		endMkIdx = _endMkIdx;

		Variable var = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID);

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

				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
				}
				if (dataType == DataType.BYTE) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}

	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="CHROMOSOME INFO">
	/**
     * This Method is safe to return an independent LHM.
	 * The size of this LHM is very small.
	 */
	public Map<String, Object> getChrInfoSetLHM() {

		Map<String, Object> chrInfoLHM = new LinkedHashMap<String, Object>();

		// GET NAMES OF CHROMOSOMES
		Variable var = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_CHR_IN_MATRIX);
		if (var != null) {
			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();
			try {
				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (varShape[0] - 1) + ":1, 0:7:1)");
					chrInfoLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}

			// GET INFO FOR EACH CHROMOSOME
			var = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_CHR_INFO); //Nb of markers, first physical position, last physical position, end index number in MarkerSet
			dataType = var.getDataType();
			varShape = var.getShape();
			if (null == var) {
				return null;
			}

			try {
				if (dataType == DataType.INT) {
					ArrayInt.D2 chrSetAI = (ArrayInt.D2) var.read("(0:" + (varShape[0] - 1) + ":1, 0:3:1)");
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayIntToLHMValues(chrSetAI, chrInfoLHM);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		} else {
			return null;
		}

		return chrInfoLHM;
	}

	public static String getChrByMarkerIndex(Map<String, Object> chrInfoLHM, int markerIndex) {
		String result = null;
		for (Map.Entry<String, Object> entry : chrInfoLHM.entrySet()) {
			String chr = entry.getKey();
			int[] value = (int[]) entry.getValue();
			if (markerIndex <= value[3] && result == null) {
				result = chr.toString();
			}
		}
		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="MARKERSET FILLERS">
	public void fillGTsForCurrentSampleIntoInitLHM(int sampleNb) throws IOException {

		Variable var = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_GENOTYPES);

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

				ArrayByte.D3 gt_ACD3 = (ArrayByte.D3) var.read("(" + sampleNb + ":" + sampleNb + ":1, "
						+ startMkIdx + ":" + endMkIdx + ":1, "
						+ "0:" + (varShape[2] - 1) + ":1)");

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
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayByteToLHMValues(gt_ACD2, markerIdSetLHM);
				} else if (reducer == 2) {
					ArrayByte.D1 gt_ACD1 = (ArrayByte.D1) gt_ACD3.reduce();
					org.gwaspi.netCDF.operations.Utils.writeD1ArrayByteToLHMValues(gt_ACD1, markerIdSetLHM);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}
	}

	public void fillInitLHMWithVariable(String variable) {

		Variable var = ncfile.findVariable(variable);
		if (var != null) {
			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				if (dataType == DataType.CHAR) {
					if (varShape.length == 2) {
						ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
						org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMValues(markerSetAC, markerIdSetLHM);
					}
				}
				if (dataType == DataType.DOUBLE) {
					if (varShape.length == 1) {
						ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");
						org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToLHMValues(markerSetAF, markerIdSetLHM);
					}
					if (varShape.length == 2) {
						ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1))");
						org.gwaspi.netCDF.operations.Utils.writeD2ArrayDoubleToLHMValues(markerSetAF, markerIdSetLHM);
					}
				}
				if (dataType == DataType.INT) {
					if (varShape.length == 1) {
						ArrayInt.D1 markerSetAD = (ArrayInt.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");
						org.gwaspi.netCDF.operations.Utils.writeD1ArrayIntToLHMValues(markerSetAD, markerIdSetLHM);
					}
					if (varShape.length == 2) {
						ArrayInt.D2 markerSetAD = (ArrayInt.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1))");
						org.gwaspi.netCDF.operations.Utils.writeD2ArrayIntToLHMValues(markerSetAD, markerIdSetLHM);
					}
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}
	}

	public void fillMarkerSetLHMWithChrAndPos() {

		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
		if (var != null) {
			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				if (dataType == DataType.CHAR) {
					if (varShape.length == 2) {
						ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");
						org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMValues(markerSetAC, markerIdSetLHM);
					}
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
					Iterator<String> it = markerIdSetLHM.keySet().iterator();
					for (int i = 0; i < shape[0]; i++) {
						String key = it.next();
						Object[] chrInfo = new Object[2];
						chrInfo[0] = markerIdSetLHM.get(key);   //CHR
						chrInfo[1] = markerSetAI.getInt(index.set(i)); //POS
						markerIdSetLHM.put(key, chrInfo);
					}
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		}
	}

	public void fillWith(Object value) {
		fillWith(markerIdSetLHM, value);
	}

	public static <K, V> Map<K, V> fillWith(Map<K, V> map, V defaultVal) {

		for (Map.Entry<K, V> entry : map.entrySet()) {
			entry.setValue(defaultVal);
		}

		return map;
	}

	// HELPERS TO TRANSFER VALUES FROM ONE LHM TO ANOTHER
	public static<K, V> Map<K, V> replaceWithValuesFrom(Map<K, V> toBeModified, Map<K, V> newValuesSource) {

		for (Map.Entry<K, V> entry : toBeModified.entrySet()) {
			entry.setValue(newValuesSource.get(entry.getKey()));
		}

		return toBeModified;
	}

	// HELPER TO APPEND VALUE TO AN EXISTING LHM CHAR VALUE
	public void appendVariableToMarkerSetLHMValue(String variable, String separator) {

		Variable var = ncfile.findVariable(variable);
		if (var != null) {
			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();
			try {
				if (dataType == DataType.CHAR) {
					if (varShape.length == 2) {
						ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (varShape[1] - 1) + ":1)");

						int[] shape = markerSetAC.getShape();
						Index index = markerSetAC.getIndex();
						Iterator<String> it = markerIdSetLHM.keySet().iterator();
						for (int i = 0; i < shape[0]; i++) {
							String key = it.next();
							String value = markerIdSetLHM.get(key).toString();
							if (!value.isEmpty()) {
								value += separator;
							}
							StringBuilder newValue = new StringBuilder();
							for (int j = 0; j < shape[1]; j++) {
								newValue.append(markerSetAC.getChar(index.set(i, j)));
							}
							markerIdSetLHM.put(key, value + newValue.toString().trim());
						}
					}
				}
				if (dataType == DataType.FLOAT) {
					if (varShape.length == 1) {
						ArrayFloat.D1 markerSetAF = (ArrayFloat.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");

						int[] shape = markerSetAF.getShape();
						Index index = markerSetAF.getIndex();
						Iterator<String> it = markerIdSetLHM.keySet().iterator();
						for (int i = 0; i < shape[0]; i++) {
							String key = it.next();
							String value = markerIdSetLHM.get(key).toString();
							if (!value.isEmpty()) {
								value += separator;
							}
							Float floatValue = markerSetAF.getFloat(index.set(i));
							markerIdSetLHM.put(key, value + floatValue.toString());
						}
					}
				}
				if (dataType == DataType.INT) {
					if (varShape.length == 1) {
						ArrayInt.D1 markerSetAF = (ArrayInt.D1) var.read("(" + startMkIdx + ":" + endMkIdx + ":1)");

						int[] shape = markerSetAF.getShape();
						Index index = markerSetAF.getIndex();
						Iterator<String> it = markerIdSetLHM.keySet().iterator();
						for (int i = 0; i < shape[0]; i++) {
							String key = it.next();
							String value = markerIdSetLHM.get(key).toString();
							if (!value.isEmpty()) {
								value += separator;
							}
							Integer intValue = markerSetAF.getInt(index.set(i));
							markerIdSetLHM.put(key, value + intValue.toString());
						}
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
	 * HELPER GETS DICTIONARY OF CURRENT MATRIX. IS CONCURRENT TO INSTANTIATED LHM
	 */
	public Map<String, Object> getDictionaryBases() throws IOException {
		Map<String, Object> dictionnary = new LinkedHashMap<String, Object>();
		try {
			Variable varBasesDict = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
			if (null != varBasesDict) {
				int[] dictShape = varBasesDict.getShape();

				ArrayChar.D2 dictAlleles_ACD2 = (ArrayChar.D2) varBasesDict.read("(" + startMkIdx + ":" + endMkIdx + ":1, 0:" + (dictShape[1] - 1) + ":1)");

				Index index = dictAlleles_ACD2.getIndex();
				Iterator<String> it = markerIdSetLHM.keySet().iterator();
				for (int i = 0; i < dictShape[0]; i++) {
					String key = it.next();
					StringBuilder alleles = new StringBuilder("");
					// Get Alleles
					for (int j = 0; j < dictShape[1]; j++) {
						alleles.append(dictAlleles_ACD2.getChar(index.set(i, j)));
					}
					dictionnary.put(key, alleles.toString().trim());
				}
			}
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}
		return dictionnary;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="MARKERSET PICKERS">
	/**
	 * THESE LHMs DO NOT CONTAIN SAME ITEMS AS INIT LHM.
	 * RETURN LHM OK
	 */
	public Map<String, Object> pickValidMarkerSetItemsByValue(String variable, Set<Object> criteria, boolean includes) {
		Map<String, Object> returnLHM = new LinkedHashMap<String, Object>();
		this.fillInitLHMWithVariable(variable);

		if (includes) {
			for (Map.Entry<String, Object> entry : markerIdSetLHM.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (criteria.contains(value)) {
					returnLHM.put(key, value);
				}
			}
		} else {
			for (Map.Entry<String, Object> entry : markerIdSetLHM.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (!criteria.contains(value)) {
					returnLHM.put(key, value);
				}
			}
		}

		return returnLHM;
	}

	public Map<String, Object> pickValidMarkerSetItemsByKey(Set<Object> criteria, boolean includes) {
		Map<String, Object> returnLHM = new LinkedHashMap<String, Object>();

		if (includes) {
			for (Map.Entry<String, Object> entry : markerIdSetLHM.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (criteria.contains(key)) {
					returnLHM.put(key, value);
				}
			}
		} else {
			for (Map.Entry<String, Object> entry : markerIdSetLHM.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (!criteria.contains(key)) {
					returnLHM.put(key, value);
				}
			}
		}

		return returnLHM;
	}
	//</editor-fold>

	public Map<String, Object> getMarkerIdSetLHM() {
		return markerIdSetLHM;
	}

	public Map<String, Object> getMarkerRsIdSetLHM() {
		return markerRsIdSetLHM;
	}
}

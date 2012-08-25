package org.gwaspi.netCDF.markers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
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
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MarkerSet {

	// MARKERSET_MEATADATA
	private String technology = ""; // platform
	private int markerSetSize = 0; // probe_nb
	private MatrixMetadata matrixMetadata;
	private Map<String, Object> markerIdSetLHM = new LinkedHashMap<String, Object>();
	private Map<String, Object> markerRsIdSetLHM = new LinkedHashMap<String, Object>();

	public MarkerSet(int studyId, int matrixId) throws IOException {
		matrixMetadata = new MatrixMetadata(matrixId);
		technology = matrixMetadata.getTechnology();
		markerSetSize = matrixMetadata.getMarkerSetSize();
	}

	public MarkerSet(int studyId, String netCDFName) throws IOException {
	}

	////////// ACCESSORS /////////
	public String getTechnology() {
		return technology;
	}

	public int getMarkerSetSize() {
		return markerSetSize;
	}

	//<editor-fold defaultstate="collapsed" desc="MARKERSET FETCHERS">
	public Map<String, Object> getMarkerIdSetLHM() {
		NetcdfFile ncfile = null;

		try {
			ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
			Variable var = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERSET);

			if (null == var) {
				return null;
			}

			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				markerSetSize = varShape[0];

				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
				}
				if (dataType == DataType.BYTE) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
				}

			} catch (IOException ioe) {
				System.out.println("Cannot read data: " + ioe);
			} catch (InvalidRangeException e) {
				System.out.println("Cannot read data: " + e);
			}

		} catch (IOException ioe) {
			System.out.println("Cannot open file: " + ioe);
		} finally {
			if (null != ncfile) {
				try {
					ncfile.close();
				} catch (IOException ioe) {
					System.out.println("Cannot close file: " + ioe);
				}
			}
		}

		return markerIdSetLHM;
	}

	public Map<String, Object> getMarkerRsIdSetLHM() {
		NetcdfFile ncfile = null;

		try {
			ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
			Variable var = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID);

			if (null == var) {
				return null;
			}

			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();

			try {
				markerSetSize = varShape[0];
				if (dataType == DataType.CHAR) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
				}

			} catch (IOException ioe) {
				System.out.println("Cannot read data: " + ioe);
			} catch (InvalidRangeException e) {
				System.out.println("Cannot read data: " + e);
			}

		} catch (IOException ioe) {
			System.out.println("Cannot open file: " + ioe);
		} finally {
			if (null != ncfile) {
				try {
					ncfile.close();
				} catch (IOException ioe) {
					System.out.println("Cannot close file: " + ioe);
				}
			}
		}

		return markerRsIdSetLHM;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="MARKERSET FILLERS">
	public Map<String, Object> readAllMarkersGTsForCurrentSampleToLHM(NetcdfFile ncfile, Map<String, Object> rdLhm, int sampleNb) throws IOException {
		Variable genotypes = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_GENOTYPES);

		if (null == genotypes) {
			return rdLhm;
		}
		try {
			int[] varShape = genotypes.getShape();

//			ArrayChar.D3 gt_ACD3 = (ArrayChar.D3) genotypes.read(
//					"(" + sampleNb + ":" + sampleNb + ":1, "
//					+ "0:" + (varShape[1] - 1) + ":1, "
//					+ "0:" + (varShape[2] - 1) + ":1)");
			ArrayByte.D3 gt_ACD3 = (ArrayByte.D3) genotypes.read("(" + sampleNb + ":" + sampleNb + ":1, "
					+ "0:" + (varShape[1] - 1) + ":1, "
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
				rdLhm = org.gwaspi.netCDF.operations.Utils.writeD2ArrayByteToLHMValues(gt_ACD2, rdLhm);
			} else if (reducer == 2) {
				ArrayByte.D1 gt_ACD1 = (ArrayByte.D1) gt_ACD3.reduce();
				rdLhm = org.gwaspi.netCDF.operations.Utils.writeD1ArrayByteToLHMValues(gt_ACD1, rdLhm);
			}

		} catch (IOException ioe) {
			System.out.println("Cannot read data: " + ioe);
		} catch (InvalidRangeException e) {
			System.out.println("Cannot read data: " + e);
		}

		return rdLhm;
	}

	public Map<String, Object> fillStrandedBasesToLHMArray(Map<String, Object> rdLhm, String strandedBases) throws IOException {
		Map<String, Object> dictionaryLhm = new LinkedHashMap<String, Object>();
		NetcdfFile ncfile = null;
		try {
			ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
			Variable varStrands = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_GT_STRAND);
			Variable varStrandedBases = ncfile.findVariable(strandedBases);

			if (null == varStrandedBases) {
				return dictionaryLhm;
			}
			if (null == varStrands) {
				return dictionaryLhm;
			}
			try {

				int[] pmAllelesShape = varStrandedBases.getShape();
				int[] strandsShape = varStrands.getShape();

				ArrayChar.D2 pmAlleles_ACD2 = (ArrayChar.D2) varStrandedBases.read("(0:" + (strandsShape[0] - 1) + ":1, 0:" + (pmAllelesShape[1] - 1) + ":1)");

				Index index = pmAlleles_ACD2.getIndex();
				Iterator<String> it = rdLhm.keySet().iterator();
				for (int i = 0; i < pmAllelesShape[0]; i++) {
					String key = it.next();
					StringBuilder alleles = new StringBuilder("");
					//Get Alleles
					for (int j = 0; j < pmAllelesShape[1]; j++) {
						alleles.append(pmAlleles_ACD2.getChar(index.set(i, j)));
					}
					dictionaryLhm.put(key, alleles.toString().trim());
				}

			} catch (InvalidRangeException e) {
				System.out.println("Cannot read data: " + e);
			}
		} catch (IOException ioe) {
			System.out.println("Cannot open file: " + ioe);
		} finally {
			if (null != ncfile) {
				try {
					ncfile.close();
				} catch (IOException ioe) {
					System.out.println("Cannot close file: " + ioe);
				}
			}
		}

		return dictionaryLhm;
	}

	public Map<String, Object> fillMarkerSetLHMWithVariable(NetcdfFile ncfile, String variable) {

		Variable var = ncfile.findVariable(variable);

		if (null == var) {
			return null;
		}

		DataType dataType = var.getDataType();
		int[] varShape = var.getShape();

		try {
			markerSetSize = varShape[0];
			if (dataType == DataType.CHAR) {
				if (varShape.length == 2) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMValues(markerSetAC, markerIdSetLHM);
				}
			}
			if (dataType == DataType.DOUBLE) {
				if (varShape.length == 1) {
					ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read("(0:" + (markerSetSize - 1) + ":1)");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToLHMValues(markerSetAF, markerIdSetLHM);
				}
				if (varShape.length == 2) {
					ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1))");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayDoubleToLHMValues(markerSetAF, markerIdSetLHM);
				}
			}
			if (dataType == DataType.INT) {
				if (varShape.length == 1) {
					ArrayInt.D1 markerSetAD = (ArrayInt.D1) var.read("(0:" + (markerSetSize - 1) + ":1)");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD1ArrayIntToLHMValues(markerSetAD, markerIdSetLHM);
				}
				if (varShape.length == 2) {
					ArrayInt.D2 markerSetAD = (ArrayInt.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1))");
					markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayIntToLHMValues(markerSetAD, markerIdSetLHM);
				}
			}

		} catch (IOException ioe) {
			System.out.println("Cannot read data: " + ioe);
		} catch (InvalidRangeException e) {
			System.out.println("Cannot read data: " + e);
			e.printStackTrace();
		}

		return markerIdSetLHM;
	}

	public Map<String, Object> fillLHMWithDefaultValue(Map<String, Object> lhm, Object defaultVal) {
		for (Iterator<String> it = lhm.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			lhm.put(key, defaultVal);
		}
		return lhm;
	}

	public Map<String, Object> fillWrLHMWithRdLHMValue(Map<String, Object> wrLHM, Map<String, Object> rdLHM) {
		for (Iterator<String> it = wrLHM.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			Object value = rdLHM.get(key);
			wrLHM.put(key, value);
		}
		return wrLHM;
	}

	public List fillALWithVariable(NetcdfFile ncfile, String variable) {

		List al = new ArrayList();

		Variable var = ncfile.findVariable(variable);
		if (null == var) {
			return null;
		}

		DataType dataType = var.getDataType();
		int[] varShape = var.getShape();
		markerSetSize = varShape[0];
		((ArrayList) al).ensureCapacity(markerSetSize);

		try {
			if (dataType == DataType.CHAR) {
				if (varShape.length == 2) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					al = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToAL(markerSetAC);
				}
			}
			if (dataType == DataType.DOUBLE) {
				if (varShape.length == 1) {
					ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read("(0:" + (markerSetSize - 1) + ":1)");
					al = org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToAL(markerSetAF);
				}
				if (varShape.length == 2) {
					ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
					al = org.gwaspi.netCDF.operations.Utils.writeD2ArrayDoubleToAL(markerSetAF);
				}
			}
		} catch (IOException ioe) {
			System.out.println("Cannot read data: " + ioe);
		} catch (InvalidRangeException e) {
			System.out.println("Cannot read data: " + e);
		}

		return al;
	}

	public Map<String, Object> appendVariableToMarkerSetLHMValue(NetcdfFile ncfile, String variable, String separator) {

		Variable var = ncfile.findVariable(variable);

		if (null == var) {
			return null;
		}

		DataType dataType = var.getDataType();
		int[] varShape = var.getShape();

		try {
			markerSetSize = varShape[0];
			if (dataType == DataType.CHAR) {
				if (varShape.length == 2) {
					ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");

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
					ArrayFloat.D1 markerSetAF = (ArrayFloat.D1) var.read("(0:" + (markerSetSize - 1) + ":1)");

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
					ArrayInt.D1 markerSetAF = (ArrayInt.D1) var.read("(0:" + (markerSetSize - 1) + ":1)");

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

		} catch (IOException ioe) {
			System.out.println("Cannot read data: " + ioe);
		} catch (InvalidRangeException e) {
			System.out.println("Cannot read data: " + e);
		}

		return markerIdSetLHM;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="MARKERSET PICKERS">
	public Map<String, Object> pickValidMarkerSetItemsByValue(NetcdfFile ncfile, String variable, Set<Object> criteria, boolean includes) {
		Map<String, Object> returnLHM = new LinkedHashMap<String, Object>();
		Map<String, Object> readLhm = this.fillMarkerSetLHMWithVariable(ncfile, variable);

		if (includes) {
			for (Iterator<String> it = readLhm.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = readLhm.get(key);
				if (criteria.contains(value)) {
					returnLHM.put(key, value);
				}
			}
		} else {
			for (Iterator<String> it = readLhm.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = readLhm.get(key);
				if (!criteria.contains(value)) {
					returnLHM.put(key, value);
				}
			}
		}

		return returnLHM;
	}

	public Map<String, Object> pickValidMarkerSetItemsByKey(Map<String, Object> lhm, Set<Object> criteria, boolean includes) {
		Map<String, Object> returnLHM = new LinkedHashMap<String, Object>();

		if (includes) {
			for (Iterator<String> it = lhm.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = lhm.get(key);
				if (criteria.contains(key)) {
					returnLHM.put(key, value);
				}
			}
		} else {
			for (Iterator<String> it = lhm.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = lhm.get(key);
				if (!criteria.contains(key)) {
					returnLHM.put(key, value);
				}
			}
		}

		return returnLHM;
	}
	//</editor-fold>
	//////////// HELPER METHODS ///////////
}

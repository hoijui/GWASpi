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

package org.gwaspi.samples;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * TODO move to package org.gwaspi.model and create a SampleService
 */
public class SampleSet {

	private final Logger log
			= LoggerFactory.getLogger(SampleSet.class);

	// SAMPLESET_MEATADATA
	private final MatrixMetadata matrixMetadata;
	private Map<SampleKey, ?> sampleIdSetMap;
	private int sampleSetSize;

	private SampleSet(MatrixMetadata matrixMetadata) throws IOException {
		this.matrixMetadata = matrixMetadata;
		this.sampleSetSize = matrixMetadata.getMarkerSetSize();
		this.sampleIdSetMap = new LinkedHashMap<SampleKey, Object>();
	}

	public SampleSet(MatrixKey matrixKey) throws IOException {
		this(MatricesList.getMatrixMetadataById(matrixKey));
	}

	public SampleSet(StudyKey studyKey, String netCDFName) throws IOException {
		this(MatricesList.getMatrixMetadataByNetCDFname(netCDFName));
	}

	public SampleSet(StudyKey studyKey, String netCDFPath, String netCDFName) throws IOException {
		this(MatricesList.getMatrixMetadata(netCDFPath, studyKey, netCDFName));
	}

	// ACCESSORS
	public int getSampleSetSize() {
		return sampleSetSize;
	}

	public MatrixMetadata getMatrixMetadata(List<String> _sampleSetAL) {
		return matrixMetadata;
	}

	//<editor-fold defaultstate="expanded" desc="SAMPLESET FETCHERS">
	private Map<SampleKey, ?> getSampleIdSetMap() throws InvalidRangeException {
		NetcdfFile ncfile = null;

		try {
			ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_SAMPLESET);

			if (null == var) {
				return null;
			}

			int[] varShape = var.getShape();
			Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);

			try {
				sampleSetSize = markerSetDim.getLength();

				StringBuilder netCdfReadStrBldr = new StringBuilder(64);
				netCdfReadStrBldr
						.append("(0:")
						.append(sampleSetSize - 1)
						.append(":1, 0:")
						.append(varShape[1] - 1)
						.append(":1)");
				String netCdfReadStr = netCdfReadStrBldr.toString();

				ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read(netCdfReadStr);

				sampleIdSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapSampleKeys(
						matrixMetadata.getStudyKey(),
						sampleSetAC,
						null);
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

		return sampleIdSetMap;
	}

	public Map<SampleKey, char[]> getSampleIdSetMapCharArray() throws InvalidRangeException {
		return (Map<SampleKey, char[]>) getSampleIdSetMap();
	}

	public Map<SampleKey, byte[]> getSampleIdSetMapByteArray() throws InvalidRangeException {
		return (Map<SampleKey, byte[]>) getSampleIdSetMap();
	}

	public Set<SampleKey> getSampleKeys() throws InvalidRangeException {
		return getSampleIdSetMap().keySet();
	}

	private Map<SampleKey, ?> getSampleIdSetMap(String matrixImportPath) throws InvalidRangeException {
		NetcdfFile ncfile = null;

		try {
			ncfile = NetcdfFile.open(matrixImportPath);
			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_SAMPLESET);

			if (null == var) {
				return null;
			}

			int[] varShape = var.getShape();
			Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);

			try {
				sampleSetSize = markerSetDim.getLength();

				StringBuilder netCdfReadStrBldr = new StringBuilder(64);
				netCdfReadStrBldr
						.append("(0:")
						.append(sampleSetSize - 1)
						.append(":1, 0:")
						.append(varShape[1] - 1)
						.append(":1)");
				String netCdfReadStr = netCdfReadStrBldr.toString();

				ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read(netCdfReadStr);

				sampleIdSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapSampleKeys(
						matrixMetadata.getStudyKey(),
						sampleSetAC,
						null);
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

		return sampleIdSetMap;
	}

	public Map<SampleKey, byte[]> getSampleIdSetMapByteArray(String matrixImportPath) throws InvalidRangeException {
		return (Map<SampleKey, byte[]>) getSampleIdSetMap(matrixImportPath);
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="SAMPLESET FILLERS">
	public void readAllSamplesGTsFromCurrentMarkerToMap(NetcdfFile rdNcFile, Map<SampleKey, byte[]> rdBytes, Map<SampleKey, char[]> rdChar, int markerNb) throws IOException {

		try {
			Variable genotypes = rdNcFile.findVariable(cNetCDF.Variables.VAR_GENOTYPES);

			if (null == genotypes) {
				return;
			}
			try {
				int[] varShape = genotypes.getShape();

				Dimension sampleSetDim = rdNcFile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);

				// NOTE exemplary code!
				// how to best create the required string?
				// see http://stackoverflow.com/questions/513600/should-i-use-javas-string-format-if-performance-is-important
				// suposedly the slowest; String.format
//				String netCdfReadStr = String.format(
//						"(0:%d:1, %d:%d:1, 0:%d:1)",
//						(sampleSetDim.getLength() - 1),
//						markerNb,
//						markerNb,
//						(varShape[2] - 1));
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
//						+ "0:" + (varShape[2] - 1) + ":1)";
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
						.append(varShape[2] - 1)
						.append(":1)");
				String netCdfReadStr = netCdfReadStrBldr.toString();

				ArrayByte.D3 gt_ACD3 = (ArrayByte.D3) genotypes.read(netCdfReadStr);

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
					if ((rdBytes == null) || rdChar != null) {
						throw new IOException("Tried to read byte[] values, but found String ones");
					}
					ArrayByte.D2 gt_ACD2 = (ArrayByte.D2) gt_ACD3.reduce();
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayByteToMapValues(gt_ACD2, rdBytes);
				} else if (reducer == 2) {
					if ((rdChar == null) || rdBytes != null) {
						throw new IOException("Tried to read String values, but found byte[] ones");
					}
					ArrayByte.D1 gt_ACD1 = (ArrayByte.D1) gt_ACD3.reduce();
					org.gwaspi.netCDF.operations.Utils.writeD1ArrayByteToMapValues(gt_ACD1, rdChar);
				}
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		} catch (IOException ex) {
			log.error("Cannot open file", ex);
		}
	}

	public void readAllSamplesGTsFromCurrentMarkerToMap(NetcdfFile rdNcFile, Map<SampleKey, byte[]> rdMap, int markerNb) throws IOException {
		readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdMap, null, markerNb);
	}

	public void readAllSamplesGTsFromCurrentMarkerToStringMap(NetcdfFile rdNcFile, Map<SampleKey, char[]> rdMap, int markerNb) throws IOException {
		readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, null, rdMap, markerNb);
	}

	private void fillSampleIdSetMapWithVariable(Map<SampleKey, ?> map, String variable) {
		NetcdfFile ncfile = null;

		try {
			ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
			Variable var = ncfile.findVariable(variable);

			if (null == var) {
				return;
			}

			DataType dataType = var.getDataType();
			int[] varShape = var.getShape();
			Dimension sampleSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);

			try {
				sampleSetSize = sampleSetDim.getLength();
				if (dataType == DataType.CHAR) {
					StringBuilder netCdfReadStrBldr = new StringBuilder(64);
					netCdfReadStrBldr
							.append("(0:")
							.append(sampleSetSize - 1)
							.append(":1, 0:")
							.append(varShape[1] - 1)
							.append(":1)");
					String netCdfReadStr = netCdfReadStrBldr.toString();

					ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(sampleSetAC, (Map<SampleKey, char[]>) map);
					sampleIdSetMap = map;
				}
				if (dataType == DataType.DOUBLE) {
					ArrayDouble.D1 sampleSetAF = (ArrayDouble.D1) var.read("(0:" + (sampleSetSize - 1) + ":1");
					org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToMapValues(sampleSetAF, (Map<SampleKey, Double>) map);
					sampleIdSetMap = map;
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
	}

	private void fillSampleIdSetMapWithFilterVariable(Map<SampleKey, char[]> map, String variable, int filterPos) {
		NetcdfFile ncfile = null;

		try {
			ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
			Variable var = ncfile.findVariable(variable);

			if (null == var) {
				return;
			}

			DataType dataType = var.getDataType();
//			int[] varShape = var.getShape();
			Dimension sampleSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);

			try {
				sampleSetSize = sampleSetDim.getLength();
				if (dataType == DataType.CHAR) {
					StringBuilder netCdfReadStrBldr = new StringBuilder(64);
					netCdfReadStrBldr
							.append("(0:")
							.append(sampleSetSize - 1)
							.append(":1, ")
							.append(filterPos)
							.append(":")
							.append(filterPos)
							.append(":1)");
					String netCdfReadStr = netCdfReadStrBldr.toString();

					ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read(netCdfReadStr);
					org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(sampleSetAC, map);
					sampleIdSetMap = map;
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
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="SAMPLESET PICKERS">
	public Map<SampleKey, Integer> pickValidSampleSetItemsByDBField(StudyKey studyKey, Set<SampleKey> sampleKeys, String dbField, Set<?> criteria, boolean include) throws IOException {
		Map<SampleKey, Integer> returnMap = new LinkedHashMap<SampleKey, Integer>();
		List<SampleInfo> sampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(studyKey);

		int pickCounter = 0;
		if (include) {
			for (SampleKey key : sampleKeys) {
				// loop through rows of result set
				for (SampleInfo sampleInfo : sampleInfos) {
					if (sampleInfo.getKey().equals(key)
							&& criteria.contains(sampleInfo.getField(dbField).toString()))
					{
						returnMap.put(key, pickCounter);
					}
				}
				pickCounter++;
			}
		} else {
			for (SampleKey key : sampleKeys) {
				// loop through rows of result set
				for (SampleInfo sampleInfo : sampleInfos) {
					if (sampleInfo.getKey().equals(key)
							&& !criteria.contains(sampleInfo.getField(dbField).toString()))
					{
						returnMap.put(key, pickCounter);
					}
				}
				pickCounter++;
			}
		}

		return returnMap;
	}

	public <V> Map<SampleKey, Integer> pickValidSampleSetItemsByNetCDFValue(Map<SampleKey, V> map, String variable, Set<V> criteria, boolean include) {
		Map<SampleKey, Integer> returnMap = new LinkedHashMap<SampleKey, Integer>();
		fillSampleIdSetMapWithVariable(map, variable);

		int pickCounter = 0;
		if (include) {
			for (Map.Entry<SampleKey, V> entry : map.entrySet()) {
				if (criteria.contains(entry.getValue())) {
					returnMap.put(entry.getKey(), pickCounter);
				}
				pickCounter++;
			}
		} else {
			for (Map.Entry<SampleKey, V> entry : map.entrySet()) {
				if (!criteria.contains(entry.getValue())) {
					returnMap.put(entry.getKey(), pickCounter);
				}
				pickCounter++;
			}
		}

		return returnMap;
	}

	public Map<SampleKey, Integer> pickValidSampleSetItemsByNetCDFFilter(Map<SampleKey, char[]> map, String variable, int fiterPos, Set<char[]> criteria, boolean include) {
		Map<SampleKey, Integer> returnMap = new LinkedHashMap<SampleKey, Integer>();
		fillSampleIdSetMapWithFilterVariable(map, variable, fiterPos);

		int pickCounter = 0;
		if (include) {
			for (Map.Entry<SampleKey, char[]> entry : map.entrySet()) {
				if (criteria.contains(entry.getValue())) {
					returnMap.put(entry.getKey(), pickCounter);
				}
				pickCounter++;
			}
		} else {
			for (Map.Entry<SampleKey, char[]> entry : map.entrySet()) {
				if (!criteria.contains(entry.getValue())) {
					returnMap.put(entry.getKey(), pickCounter);
				}
				pickCounter++;
			}
		}

		return returnMap;
	}

	public Map<SampleKey, Integer> pickValidSampleSetItemsByNetCDFKey(Set<SampleKey> sampleKeys, Set<SampleKey> criteria, boolean include) throws IOException {
		Map<SampleKey, Integer> returnMap = new LinkedHashMap<SampleKey, Integer>();

		int pickCounter = 0;
		if (include) {
			for (SampleKey key : sampleKeys) {
				if (criteria.contains(key)) {
					returnMap.put(key, pickCounter);
				}
				pickCounter++;
			}
		} else {
			for (SampleKey key : sampleKeys) {
				if (!criteria.contains(key)) {
					returnMap.put(key, pickCounter);
				}
				pickCounter++;
			}
		}

		return returnMap;
	}
	//</editor-fold>
}

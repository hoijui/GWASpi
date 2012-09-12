package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cNetCDF;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

	private final static Logger log
			= LoggerFactory.getLogger(Utils.class);

	private Utils() {
	}

	//////// HELPER METHODS ////////
	//<editor-fold defaultstate="collapsed" desc="SAVERS">
	public static boolean saveCharLHMKeyToWrMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLHM, String variable, int varStride) {
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(wrLHM, varStride);

			int[] markersOrig = new int[]{0, 0};
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing file", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveCharLHMValueToWrMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLHM, String variable, int varStride) {
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueToD2ArrayChar(wrLHM, varStride);

			int[] markersOrig = new int[]{0, 0};
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing file", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveCharLHMItemToWrMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLHM, String variable, int itemIndex, int varStride) {
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = Utils.writeLHMValueItemToD2ArrayChar(wrLHM, itemIndex, varStride);
			int[] markersOrig = new int[]{0, 0};
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing file", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	//<editor-fold defaultstate="collapsed" desc="GENOTYPE SAVERS">
	public static boolean saveSingleSampleGTsToMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLhm, int sampleIndex) {
		boolean result = false;
		ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToSingleSampleArrayByteD3(wrLhm, cNetCDF.Strides.STRIDE_GT);
//		ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToCurrentSampleArrayByteD3(wrLhm, cNetCDF.Strides.STRIDE_GT);

		int[] origin = new int[]{sampleIndex, 0, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
//			log.info("Done writing Sample {} genotypes at {}", samplePos, org.gwaspi.global.Utils.getMediumDateTimeAsString());
			result = true;
		} catch (IOException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		} catch (InvalidRangeException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		}
		return result;
	}

	public static boolean saveSingleMarkerGTsToMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLhm, int markerIndex) {
		boolean result = false;
		ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToSingleMarkerArrayByteD3(wrLhm, cNetCDF.Strides.STRIDE_GT);

		int[] origin = new int[]{0, markerIndex, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
//			log.info("Done writing genotypes at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
			result = true;
		} catch (IOException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		} catch (InvalidRangeException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		}
		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="D1 SAVERS">
	public static boolean saveDoubleLHMD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLHM, String variable) {
		boolean result = false;

		try {
			ArrayDouble.D1 arrayDouble = Utils.writeLHMValueToD1ArrayDouble(wrLHM);
			int[] origin1 = new int[1];
			try {
				wrNcFile.write(variable, origin1, arrayDouble);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveDoubleLHMItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLHM, int itemNb, String variable) {
		boolean result = false;

		try {
			ArrayDouble.D1 arrayDouble = Utils.writeLHMValueItemToD1ArrayDouble(wrLHM, itemNb);
			int[] origin1 = new int[1];
			try {
				wrNcFile.write(variable, origin1, arrayDouble);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveIntLHMD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLHM, String variable) {
		boolean result = false;

		try {
			ArrayInt.D1 arrayInt = Utils.writeLHMValueToD1ArrayInt(wrLHM);
			int[] origin1 = new int[1];
			try {
				wrNcFile.write(variable, origin1, arrayInt);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveIntLHMItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLHM, int itemNb, String variable) {
		boolean result = false;

		try {
			ArrayInt.D1 arrayInt = Utils.writeLHMValueItemToD1ArrayInt(wrLHM, itemNb);
			int[] origin1 = new int[1];
			try {
				wrNcFile.write(variable, origin1, arrayInt);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="D2 SAVERS">
	public static boolean saveIntLHMD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLHM, int[] columns, String variable) {
		boolean result = false;

		try {
			ArrayInt.D2 arrayIntD2 = Utils.writeLHMValueItemToD2ArrayInt(wrLHM, columns);
			int[] origin1 = new int[2];
			try {
				wrNcFile.write(variable, origin1, arrayIntD2);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveDoubleLHMD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object> wrLHM, int[] columns, String variable) {
		boolean result = false;

		try {
			ArrayDouble.D2 arrayDoubleD2 = Utils.writeLHMValueItemToD2ArrayDouble(wrLHM, columns);
			int[] origin1 = new int[2];
			try {
				wrNcFile.write(variable, origin1, arrayDoubleD2);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	//</editor-fold>
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="CHUNKED SAVERS">
	public static boolean saveCharChunkedLHMToWrMatrix(NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrLHM,
			String variable,
			int varStride,
			int offset) {
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueToD2ArrayChar(wrLHM, varStride);

			int[] markersOrig = new int[]{offset, 0}; //first origin is the initial markerset position, second is the original allele position
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing file", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveCharChunkedLHMItemToWrMatrix(NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrLHM,
			String variable,
			int itemNb,
			int varStride,
			int offset) {
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = Utils.writeLHMValueItemToD2ArrayChar(wrLHM, itemNb, varStride);
			int[] markersOrig = new int[]{offset, 0};
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing file", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	//<editor-fold defaultstate="collapsed" desc="GENOTYPE SAVERS">
	public static boolean saveChunkedCurrentSampleGTsToMatrix(NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrLhm,
			int samplePos,
			int offset) throws InvalidRangeException {
		boolean result = false;
		ArrayChar.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToCurrentSampleArrayCharD3(wrLhm, cNetCDF.Strides.STRIDE_GT);

		int[] origin = new int[]{samplePos, offset, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
			log.info("Done writing Sample {} genotypes at {}", samplePos, org.gwaspi.global.Utils.getMediumDateTimeAsString());
			result = true;
		} catch (IOException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		} catch (InvalidRangeException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		}
		return result;
	}

	public static boolean saveChunkedCurrentMarkerGTsToMatrix(NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrLhm,
			int markerPos,
			int offset) {
		boolean result = false;
		ArrayChar.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToCurrentMarkerArrayCharD3(wrLhm, cNetCDF.Strides.STRIDE_GT);

		int[] origin = new int[]{offset, markerPos, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
			log.info("Done writing genotypes at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
			result = true;
		} catch (IOException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		} catch (InvalidRangeException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		}
		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="D1 SAVERS">
	public static boolean saveDoubleChunkedLHMD1ToWrMatrix(NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrLHM,
			String variable,
			int offset) {
		boolean result = false;

		try {
			ArrayDouble.D1 arrayDouble = Utils.writeLHMValueToD1ArrayDouble(wrLHM);
			int[] origin1 = new int[]{offset};
			try {
				wrNcFile.write(variable, origin1, arrayDouble);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveDoubleChunkedLHMItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrLHM,
			int itemNb,
			String variable,
			int offset) {
		boolean result = false;

		try {
			ArrayDouble.D1 arrayDouble = Utils.writeLHMValueItemToD1ArrayDouble(wrLHM, itemNb);
			int[] origin1 = new int[]{offset};
			try {
				wrNcFile.write(variable, origin1, arrayDouble);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveIntChunkedLHMD1ToWrMatrix(NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrLHM,
			String variable,
			int offset) {
		boolean result = false;

		try {
			ArrayInt.D1 arrayInt = Utils.writeLHMValueToD1ArrayInt(wrLHM);
			int[] origin1 = new int[]{offset};
			try {
				wrNcFile.write(variable, origin1, arrayInt);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveIntChunkedLHMItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrLHM,
			int itemNb,
			String variable,
			int offset) {
		boolean result = false;

		try {
			ArrayInt.D1 arrayInt = Utils.writeLHMValueItemToD1ArrayInt(wrLHM, itemNb);
			int[] origin1 = new int[]{offset};
			try {
				wrNcFile.write(variable, origin1, arrayInt);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="D2 SAVERS">
	public static boolean saveIntChunkedLHMD2ToWrMatrix(NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrLHM,
			int[] columns,
			String variable,
			int offset) {
		boolean result = false;

		try {
			ArrayInt.D2 arrayIntD2 = Utils.writeLHMValueItemToD2ArrayInt(wrLHM, columns);
			int[] origin1 = new int[]{offset, 0};
			try {
				wrNcFile.write(variable, origin1, arrayIntD2);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	public static boolean saveDoubleChunkedD2ToWrMatrix(NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrLHM,
			int[] columns,
			String variable,
			int offset) {
		boolean result = false;

		try {
			ArrayDouble.D2 arrayDoubleD2 = Utils.writeLHMValueItemToD2ArrayDouble(wrLHM, columns);
			int[] origin1 = new int[]{offset, 0};
			try {
				wrNcFile.write(variable, origin1, arrayDoubleD2);
				log.info("Done writing {} at {}", variable, org.gwaspi.global.Utils.getMediumDateTimeAsString());
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString(), ex);
		}

		return result;
	}

	//</editor-fold>
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="POJOs TO netCDFJOs">
	//<editor-fold defaultstate="collapsed" desc="ArrayChar.D3">
	public static ArrayChar.D3 writeLHMToCurrentSampleArrayCharD3(Map<String, Object> lhm, int stride) {
		ArrayChar.D3 charArray = new ArrayChar.D3(1, lhm.size(), stride);
		Index ima = charArray.getIndex();

		int markerCounter = 0;
		for (Object value : lhm.values()) {
			charArray.setString(ima.set(0, markerCounter, 0), value.toString().trim()); //1 Sample at a time, iterating through markers, starting at gtSpan 0
			markerCounter++;
		}

		return charArray;
	}

	public static ArrayChar.D3 writeLHMToCurrentMarkerArrayCharD3(Map<String, Object> lhm, int stride) {
		ArrayChar.D3 charArray = new ArrayChar.D3(lhm.size(), 1, stride);
		Index ima = charArray.getIndex();

		int sampleCounter = 0;
		for (Object value : lhm.values()) {
			charArray.setString(ima.set(sampleCounter, 0, 0), value.toString().trim()); //1 Marker at a time, iterating through samples, starting at gtSpan 0
			sampleCounter++;
		}

		return charArray;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ArrayChar.D2">
	public static ArrayChar.D2 writeALToD2ArrayChar(List al, int stride) {
		ArrayChar.D2 charArray = new ArrayChar.D2(al.size(), stride);
		Index ima = charArray.getIndex();

		for (int i = 0; i < al.size(); i++) {
			String value = al.get(i).toString();
			charArray.setString(ima.set(i, 0), value.trim());
		}

		return charArray;
	}

	public static ArrayChar.D2 writeLHMValueToD2ArrayChar(Map<String, Object> lhm, int stride) {
		ArrayChar.D2 charArray = new ArrayChar.D2(lhm.size(), stride);
		Index ima = charArray.getIndex();

		int count = 0;
		for (Object value : lhm.values()) {
			charArray.setString(ima.set(count, 0), value.toString().trim());
			count++;
		}

		return charArray;
	}

	public static ArrayChar.D2 writeLHMKeysToD2ArrayChar(Map<String, Object> lhm, int stride) {
		ArrayChar.D2 charArray = new ArrayChar.D2(lhm.size(), stride);
		Index ima = charArray.getIndex();

		int count = 0;
		for (String key : lhm.keySet()) {
			charArray.setString(ima.set(count, 0), key.trim());
			count++;
		}

		return charArray;
	}

	public static ArrayChar.D2 writeLHMValueItemToD2ArrayChar(Map<String, Object> lhm, int itemNb, int stride) {
		ArrayChar.D2 charArray = new ArrayChar.D2(lhm.size(), stride);
		Index index = charArray.getIndex();

		int count = 0;
		for (Object value : lhm.values()) {
			Object[] values = (Object[]) value;
			String newValue = values[itemNb].toString();
			charArray.setString(index.set(count, 0), newValue.trim());
			count++;
		}

		return charArray;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ArrayDouble.D1 & D2">
	public static ArrayDouble.D1 writeLHMValueToD1ArrayDouble(Map<String, Object> lhm) {
		ArrayDouble.D1 doubleArray = new ArrayDouble.D1(lhm.size());
		Index index = doubleArray.getIndex();

		int count = 0;
		for (Object value : lhm.values()) {
			doubleArray.setDouble(index.set(count), (Double) value);
			count++;
		}

		return doubleArray;
	}

	private static ArrayDouble.D1 writeLHMValueItemToD1ArrayDouble(Map<String, Object> lhm, int itemNb) {
		ArrayDouble.D1 doubleArray = new ArrayDouble.D1(lhm.size());
		Index index = doubleArray.getIndex();

		int count = 0;
		for (Object value : lhm.values()) {
			Object[] values = (Object[]) value;
			doubleArray.setDouble(index.set(count), (Double) values[itemNb]);
			count++;
		}

		return doubleArray;
	}

	private static ArrayDouble.D2 writeLHMValueItemToD2ArrayDouble(Map<String, Object> lhm, int[] columns) {
		ArrayDouble.D2 doubleArray = new ArrayDouble.D2(lhm.size(), columns.length);
		Index ima = doubleArray.getIndex();

		int i = 0;
		for (Object value : lhm.values()) {
			Object[] values = (Object[]) value;
			for (int j = 0; j < columns.length; j++) {
				doubleArray.setDouble(ima.set(i, j), (Double) values[columns[j]]);
			}
			i++;
		}

		return doubleArray;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ArrayInt.D1 & D2">
	public static ArrayInt.D1 writeLHMValueToD1ArrayInt(Map<String, Object> lhm) {
		ArrayInt.D1 intArray = new ArrayInt.D1(lhm.size());
		Index index = intArray.getIndex();

		int count = 0;
		for (Object value : lhm.values()) {
			intArray.setInt(index.set(count), (Integer) value);
			count++;
		}

		return intArray;
	}

	public static ArrayInt.D1 writeLHMValueItemToD1ArrayInt(Map<String, Object> lhm, int itemNb) {
		ArrayInt.D1 intArray = new ArrayInt.D1(lhm.size());
		Index index = intArray.getIndex();

		int count = 0;
		for (Object value : lhm.values()) {
			Object[] values = (Object[]) value;
			intArray.setInt(index.set(count), (Integer) values[itemNb]);
			count++;
		}

		return intArray;
	}

	// TODO can be optimized with arraycopy?
	public static ArrayInt.D2 writeLHMValueItemToD2ArrayInt(Map<String, Object> lhm, int[] columns) {
		ArrayInt.D2 intArray = new ArrayInt.D2(lhm.size(), columns.length);
		Index ima = intArray.getIndex();

		int i = 0;
		for (Object value : lhm.values()) {
			int[] values = (int[]) value;
			for (int j = 0; j < columns.length; j++) {
				intArray.setInt(ima.set(i, j), values[columns[j]]);
			}
			i++;
		}

		return intArray;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="ArrayByte.D3">
	public static ArrayByte.D3 writeALValuesToSamplesHyperSlabArrayByteD3(List<byte[]> genotypesAL, int sampleNb, int stride) {
		int markerNb = genotypesAL.size() / sampleNb;
		int alCounter = 0;

		//samplesDim, markersDim, gtStrideDim
		ArrayByte.D3 byteArray = new ArrayByte.D3(sampleNb, markerNb, stride);
		Index ima = byteArray.getIndex();

		for (int markerCounter = 0; markerCounter < markerNb; markerCounter++) {
			for (int sampleCounter = 0; sampleCounter < sampleNb; sampleCounter++) {

				byte[] value = genotypesAL.get(alCounter);
				byteArray.setByte(ima.set(sampleCounter, markerCounter, 0), value[0]); //1 Sample at a time, iterating through markers, first byte
				byteArray.setByte(ima.set(sampleCounter, markerCounter, 1), value[1]); //1 Sample at a time, iterating through markers, second byte
				alCounter++;
			}
		}

		return byteArray;
	}

	public static ArrayByte.D3 writeLHMToSingleSampleArrayByteD3(Map<String, Object> lhm, int stride) {
		//samplesDim, markersDim, gtStrideDim
		ArrayByte.D3 byteArray = new ArrayByte.D3(1, lhm.size(), stride);
		Index ima = byteArray.getIndex();

		int markerCount = 0;
		for (Object value : lhm.values()) {
			byte[] values = (byte[]) value;
			byteArray.setByte(ima.set(0, markerCount, 0), values[0]); //1 Sample at a time, iterating through markers, first byte
			byteArray.setByte(ima.set(0, markerCount, 1), values[1]); //1 Sample at a time, iterating through markers, second byte
			markerCount++;
		}

		return byteArray;
	}

	public static ArrayByte.D3 writeLHMToSingleMarkerArrayByteD3(Map<String, Object> lhm, int stride) {
		ArrayByte.D3 byteArray = new ArrayByte.D3(lhm.size(), 1, stride);
		Index ima = byteArray.getIndex();

		int markerCounter = 0;
		for (Object value : lhm.values()) {
			byte[] values = (byte[]) value;
			byteArray.setByte(ima.set(markerCounter, 0, 0), values[0]); //1 Marker at a time, iterating through samples, first byte
			byteArray.setByte(ima.set(markerCounter, 0, 1), values[1]); //1 Marker at a time, iterating through samples, second byte
			markerCounter++;
		}

		return byteArray;
	}

	/**
	 * This writeLHMToCurrentMarkerArrayByteD3 has now been deprecated in favor
	 * of writeLHMToSingleMarkerArrayByteD3 Method is probably INCORRECT!
	 *
	 * @deprecated Use {@link #writeLHMToSingleMarkerArrayByteD3} instead
	 */
	public static ArrayByte.D3 writeLHMToCurrentMarkerArrayByteD3(Map<String, Object> lhm, int stride) {
		ArrayByte.D3 byteArray = new ArrayByte.D3(1, lhm.size(), stride);
		Index ima = byteArray.getIndex();

		int markerCounter = 0;
		for (Object value : lhm.values()) {
			byte[] values = (byte[]) value;
			byteArray.setByte(ima.set(0, markerCounter, 0), values[0]); //1 Marker at a time, iterating through samples, first byte
			byteArray.setByte(ima.set(0, markerCounter, 1), values[1]); //1 Marker at a time, iterating through samples, second byte
			markerCounter++;
		}

		return byteArray;
	}
	//</editor-fold>

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="netCDFJOs TO POJOs">
	//<editor-fold defaultstate="collapsed" desc="ArrayChar.D2">
	public static Map<String, Object> writeD2ArrayCharToLHMKeys(ArrayChar inputArray) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
//		StringBuilder key = new StringBuilder("");

		int[] shape = inputArray.getShape();
//		Index index = inputArray.getIndex();
		for (int i = 0; i < shape[0]; i++) {
			ArrayChar wrCharArray = new ArrayChar(new int[]{1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			result.put(String.valueOf(values).trim(), "");

//			key = new StringBuilder("");
//			for (int j=0; j<shape[1]; j++) {
//				key.append(inputArray.getChar(index.set(i,j)));
//			}
//			result.put(key.toString().trim(), "");
		}

		return result;
	}

	public static void writeD2ArrayCharToLHMValues(ArrayChar inputArray, Map<String, Object> map) {

		int[] shape = inputArray.getShape();
		Iterator<Entry<String, Object>> it = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			ArrayChar wrCharArray = new ArrayChar(new int[]{1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			it.next().setValue(String.valueOf(values).trim());
		}
	}

	public static List<String> writeD2ArrayCharToAL(ArrayChar inputArray) {
		Long expectedSize = inputArray.getSize();
		List<String> als = new ArrayList(expectedSize.intValue());

		int[] shape = inputArray.getShape();
		for (int i = 0; i < shape[0]; i++) {
			ArrayChar wrCharArray = new ArrayChar(new int[]{1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			als.add(String.valueOf(values).trim());
		}


		return als;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ArrayChar.D1">
	public static Map<String, Object> writeD1ArrayCharToLHMKeys(ArrayChar inputArray) {
		Map<String, Object> resultLHM = new LinkedHashMap();
		StringBuilder key = new StringBuilder("");
		Index index = inputArray.getIndex();

		int[] shape = inputArray.getShape();
		for (int j = 0; j < shape[0]; j++) {
			key.append(inputArray.getChar(index.set(j)));
		}
		resultLHM.put(key.toString().trim(), "");

		return resultLHM;

	}

	public static Map<String, Object> writeD1ArrayCharToLHMValues(ArrayChar inputArray, Map<String, Object> lhm) {
		StringBuilder value = new StringBuilder("");
		Index index = inputArray.getIndex();

		int[] shape = inputArray.getShape();
		Iterator<String> it = lhm.keySet().iterator();
		String key = it.next();

		for (int j = 0; j < shape[0]; j++) {
			value.append(inputArray.getChar(index.set(j)));
		}
		lhm.put(key, value.toString().trim());

		return lhm;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="ArrayDouble.D1">
	public static void writeD1ArrayDoubleToLHMValues(ArrayDouble inputArray, Map<String, Object> map) {

		int[] shape = inputArray.getShape();
		Index index = inputArray.getIndex();
		Iterator<Entry<String, Object>> entries = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			Double value = inputArray.getDouble(index.set(i));
			entries.next().setValue(value);
		}
	}

	public static List<Double> writeD1ArrayDoubleToAL(ArrayDouble.D1 inputArray) {
		Long expectedSize = inputArray.getSize();
		List<Double> alf = new ArrayList<Double>(expectedSize.intValue());

		int[] shape = inputArray.getShape();
		Index index = inputArray.getIndex();
		for (int i = 0; i < shape[0]; i++) {
			Double value = inputArray.getDouble(index.set(i));
			alf.add(value);
		}

		return alf;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="ArrayDouble.D2">
	public static void writeD2ArrayDoubleToLHMValues(ArrayDouble.D2 inputArray, Map<String, Object> map) {
		int[] shape = inputArray.getShape();
		Iterator<Entry<String, Object>> entries = map.entrySet().iterator();

		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			ArrayDouble wrDoubleArray = new ArrayDouble(new int[]{1, shape[1]});
			ArrayDouble.D2.arraycopy(inputArray, i, wrDoubleArray, 0, shape[1]);
			double[] values = (double[]) wrDoubleArray.copyTo1DJavaArray();

			entries.next().setValue(values);
		}
	}

	public static List<double[]> writeD2ArrayDoubleToAL(ArrayDouble.D2 inputArray) {
		Long expectedSize = inputArray.getSize();
		List<double[]> alf = new ArrayList<double[]>(expectedSize.intValue());

		int[] shape = inputArray.getShape();
		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			ArrayDouble wrDoubleArray = new ArrayDouble(new int[]{1, shape[1]});
			ArrayDouble.D2.arraycopy(inputArray, i, wrDoubleArray, 0, shape[1]);
			double[] values = (double[]) wrDoubleArray.copyTo1DJavaArray();
			alf.add(values);
		}

		return alf;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ArrayInt.D1">
	public static void writeD1ArrayIntToLHMValues(ArrayInt inputArray, Map<String, Object> map) {

		int[] shape = inputArray.getShape();
		Index index = inputArray.getIndex();
		Iterator<Entry<String, Object>> entries = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			Integer value = inputArray.getInt(index.set(i));
			entries.next().setValue(value);
		}
	}

	public static List<Integer> writeD1ArrayIntToAL(ArrayInt.D1 inputArray) {
		Long expectedSize = inputArray.getSize();
		List<Integer> ali = new ArrayList<Integer>(expectedSize.intValue());

		int[] shape = inputArray.getShape();
		Index index = inputArray.getIndex();
		for (int i = 0; i < shape[0]; i++) {
			int value = inputArray.getInt(index.set(i));
			ali.add(value);
		}

		return ali;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="ArrayInt.D2">
	public static void writeD2ArrayIntToLHMValues(ArrayInt.D2 inputArray, Map<String, Object> map) {
		int[] shape = inputArray.getShape();
		Iterator<Entry<String, Object>> entries = map.entrySet().iterator();

		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			ArrayInt wrIntArray = new ArrayInt(new int[]{1, shape[1]});
			ArrayInt.D2.arraycopy(inputArray, i, wrIntArray, 0, shape[1]);
			int[] values = (int[]) wrIntArray.copyTo1DJavaArray();

			entries.next().setValue(values);
		}
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="ArrayByte.D2">
	public static void writeD2ArrayByteToLHMValues(ArrayByte inputArray, Map<String, Object> map) {

		int[] shape = inputArray.getShape();
		Iterator<Entry<String, Object>> entries = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			Entry<String, Object> entry = entries.next();

			ArrayByte wrArray = new ArrayByte(new int[]{1, shape[1]});
			ArrayByte.D2.arraycopy(inputArray, i * shape[1], wrArray, 0, shape[1]);
			byte[] values = (byte[]) wrArray.copyTo1DJavaArray();
			entry.setValue(values);
		}
	}

	public static List<byte[]> writeD2ArrayByteToAL(ArrayByte inputArray) {
		Long expectedSize = inputArray.getSize();
		List<byte[]> als = new ArrayList<byte[]>(expectedSize.intValue());

		int[] shape = inputArray.getShape();
		for (int i = 0; i < shape[0]; i++) {
			ArrayByte wrArray = new ArrayByte(new int[]{1, shape[1]});
			ArrayByte.D2.arraycopy(inputArray, i * shape[1], wrArray, 0, shape[1]);
			byte[] values = (byte[]) wrArray.copyTo1DJavaArray();
			als.add(values);
		}

		return als;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ArrayByte.D1">
	public static void writeD1ArrayByteToLHMValues(ArrayByte inputArray, Map<String, Object> map) {
		StringBuilder value = new StringBuilder("");
		Index index = inputArray.getIndex();

		int[] shape = inputArray.getShape();
		Iterator<String> it = map.keySet().iterator();
		String key = it.next();
		for (int j = 0; j < shape[0]; j++) {
			value.append(inputArray.getChar(index.set(j)));
		}
		map.put(key, value.toString().trim());
	}
	//</editor-fold>
	//</editor-fold>
}

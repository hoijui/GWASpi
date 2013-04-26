package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.TypeConverter;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleKey;
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

	private static final Logger log
			= LoggerFactory.getLogger(Utils.class);

	private Utils() {
	}

	//<editor-fold defaultstate="expanded" desc="SAVERS">
	public static <K> boolean saveCharMapKeyToWrMatrix(NetcdfFileWriteable wrNcFile, Map<K, ?> wrMap, String variable, int varStride) {
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = writeMapKeysToD2ArrayChar(wrMap, varStride);

			int[] markersOrig = new int[] {0, 0};
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing file", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	public static boolean saveCharMapValueToWrMatrix(NetcdfFileWriteable wrNcFile, Map<?, char[]> wrMap, String variable, int varStride) {
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = writeMapValueToD2ArrayChar(wrMap, varStride);

			int[] markersOrig = new int[] {0, 0};
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing file", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	public static <V> boolean saveCharMapItemToWrMatrix(NetcdfFileWriteable wrNcFile, Map<?, V> wrMap, String variable, TypeConverter<V, String> typeConverter, int varStride) {
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = writeMapValueItemToD2ArrayChar(wrMap, typeConverter, varStride);
			int[] markersOrig = new int[] {0, 0};
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing file", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	//<editor-fold defaultstate="expanded" desc="GENOTYPE SAVERS">
	public static boolean saveSingleSampleGTsToMatrix(NetcdfFileWriteable wrNcFile, Map<?, byte[]> wrMap, int sampleIndex) {
		boolean result = false;
		ArrayByte.D3 genotypes = writeMapToSingleSampleArrayByteD3(wrMap, cNetCDF.Strides.STRIDE_GT);
//		ArrayByte.D3 genotypes = writeMapToCurrentSampleArrayByteD3(wrMap, cNetCDF.Strides.STRIDE_GT);

		int[] origin = new int[] {sampleIndex, 0, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
//			log.info("Done writing Sample {} genotypes", samplePos);
			result = true;
		} catch (IOException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		} catch (InvalidRangeException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		}
		return result;
	}

	public static boolean saveSingleMarkerGTsToMatrix(NetcdfFileWriteable wrNcFile, Map<?, byte[]> wrMap, int markerIndex) {
		boolean result = false;
		ArrayByte.D3 genotypes = writeMapToSingleMarkerArrayByteD3(wrMap, cNetCDF.Strides.STRIDE_GT);

		int[] origin = new int[] {0, markerIndex, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
//			log.info("Done writing genotypes");
			result = true;
		} catch (IOException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		} catch (InvalidRangeException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		}
		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="D1 SAVERS">
	public static boolean saveDoubleMapD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<?, Double> wrMap, String variable) {
		boolean result = false;

		try {
			ArrayDouble.D1 arrayDouble = writeMapValueToD1ArrayDouble(wrMap);
			int[] origin1 = new int[1];
			try {
				wrNcFile.write(variable, origin1, arrayDouble);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	public static boolean saveDoubleMapItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<?, Double[]> wrMap, final int itemNb, String variable) {

		TypeConverter<Double[], Double> typeConverter = new TypeConverter<Double[], Double>() {
			@Override
			public Double convert(Double[] from) {
				return from[itemNb];
			}
		};

		return saveDoubleMapItemD1ToWrMatrix(wrNcFile, wrMap, typeConverter, variable);
	}

	public static <V> boolean saveDoubleMapItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<?, V> wrMap, TypeConverter<V, Double> typeConverter, String variable) {
		boolean result = false;

		try {
			ArrayDouble.D1 arrayDouble = Utils.writeMapValueItemToD1ArrayDouble(wrMap, typeConverter);
			int[] origin1 = new int[1];
			try {
				wrNcFile.write(variable, origin1, arrayDouble);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	public static boolean saveIntMapD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<?, Integer> wrMap, String variable) {
		boolean result = false;

		try {
			ArrayInt.D1 arrayInt = Utils.writeMapValueToD1ArrayInt(wrMap);
			int[] origin1 = new int[1];
			try {
				wrNcFile.write(variable, origin1, arrayInt);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	/**
	 * @deprecated unused
	 */
	public static boolean saveIntMapItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<String, Object[]> wrMap, int itemNb, String variable) {
		boolean result = false;

		try {
			ArrayInt.D1 arrayInt = Utils.writeMapValueItemToD1ArrayInt(wrMap, itemNb);
			int[] origin1 = new int[1];
			try {
				wrNcFile.write(variable, origin1, arrayInt);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="D2 SAVERS">
	public static boolean saveIntMapD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<?, int[]> wrMap, int[] columns, String variable) {
		boolean result = false;

		try {
			ArrayInt.D2 arrayIntD2 = writeMapValueItemToD2ArrayInt(wrMap, columns);
			int[] origin1 = new int[2];
			try {
				wrNcFile.write(variable, origin1, arrayIntD2);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	public static boolean saveDoubleMapD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Map<?, Double[]> wrMap, int[] columns, String variable) {
		boolean result = false;

		try {
			ArrayDouble.D2 arrayDoubleD2 = writeMapValueItemToD2ArrayDouble(wrMap, columns);
			int[] origin1 = new int[2];
			try {
				wrNcFile.write(variable, origin1, arrayDoubleD2);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}
	//</editor-fold>
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="CHUNKED SAVERS">
	public static boolean saveCharChunkedMapToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Map<?, char[]> wrMap,
			String variable,
			int varStride,
			int offset)
	{
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = writeMapValueToD2ArrayChar(wrMap, varStride);

			// first origin is the initial markerset position, second is the original allele position
			int[] markersOrig = new int[] {offset, 0};
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing file", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	/**
	 * @deprecated unused
	 */
	public static boolean saveCharChunkedMapItemToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Map<String, MarkerMetadata> wrMap,
			String variable,
			int itemNb,
			int varStride,
			int offset)
	{
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = null; //writeMapValueItemToD2ArrayChar(wrMap, itemNb, varStride);
			int[] markersOrig = new int[] {offset, 0};
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing file", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	//<editor-fold defaultstate="expanded" desc="GENOTYPE SAVERS">
	public static boolean saveChunkedCurrentSampleGTsToMatrix(
			NetcdfFileWriteable wrNcFile,
			Map<?, Object> wrMap,
			int samplePos,
			int offset)
			throws InvalidRangeException
	{
		boolean result = false;
		ArrayChar.D3 genotypes = writeMapToCurrentSampleArrayCharD3(wrMap, cNetCDF.Strides.STRIDE_GT);

		int[] origin = new int[]{samplePos, offset, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
			log.info("Done writing Sample {} genotypes", samplePos);
			result = true;
		} catch (IOException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		} catch (InvalidRangeException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		}
		return result;
	}

	public static boolean saveChunkedCurrentMarkerGTsToMatrix(
			NetcdfFileWriteable wrNcFile,
			Map<String, Object> wrMap,
			int markerPos,
			int offset)
	{
		boolean result = false;
		ArrayChar.D3 genotypes = writeMapToCurrentMarkerArrayCharD3(wrMap, cNetCDF.Strides.STRIDE_GT);

		int[] origin = new int[]{offset, markerPos, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
			log.info("Done writing genotypes");
			result = true;
		} catch (IOException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		} catch (InvalidRangeException ex) {
			log.error("Failed writing genotypes to netCDF in MatrixDataExtractor", ex);
		}
		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="D1 SAVERS">
	public static boolean saveDoubleChunkedMapD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Map<?, Double> wrMap,
			String variable,
			int offset)
	{
		boolean result = false;

		try {
			ArrayDouble.D1 arrayDouble = Utils.writeMapValueToD1ArrayDouble(wrMap);
			int[] origin1 = new int[]{offset};
			try {
				wrNcFile.write(variable, origin1, arrayDouble);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	/**
	 * @deprecated unused
	 */
	public static boolean saveDoubleChunkedMapItemD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Map<?, Object[]> wrMap,
			int itemNb,
			String variable,
			int offset)
	{
		boolean result = false;

		try {
			ArrayDouble.D1 arrayDouble = null; //Utils.writeMapValueItemToD1ArrayDouble(wrMap, itemNb);
			int[] origin1 = new int[] {offset};
			try {
				wrNcFile.write(variable, origin1, arrayDouble);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	public static boolean saveIntChunkedMapD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Map<?, Integer> wrMap,
			String variable,
			int offset)
	{
		boolean result = false;

		try {
			ArrayInt.D1 arrayInt = Utils.writeMapValueToD1ArrayInt(wrMap);
			int[] origin1 = new int[]{offset};
			try {
				wrNcFile.write(variable, origin1, arrayInt);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	/**
	 * @deprecated unused
	 */
	public static boolean saveIntChunkedMapItemD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Map<String, Object[]> wrMap,
			int itemNb,
			String variable,
			int offset)
	{
		boolean result = false;

		try {
			ArrayInt.D1 arrayInt = Utils.writeMapValueItemToD1ArrayInt(wrMap, itemNb);
			int[] origin1 = new int[]{offset};
			try {
				wrNcFile.write(variable, origin1, arrayInt);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="D2 SAVERS">
	public static boolean saveIntChunkedMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Map<?, int[]> wrMap,
			int[] columns,
			String variable,
			int offset)
	{
		boolean result = false;

		try {
			ArrayInt.D2 arrayIntD2 = Utils.writeMapValueItemToD2ArrayInt(wrMap, columns);
			int[] origin1 = new int[]{offset, 0};
			try {
				wrNcFile.write(variable, origin1, arrayIntD2);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	public static boolean saveDoubleChunkedD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Map<String, Double[]> wrMap,
			int[] columns,
			String variable,
			int offset) {
		boolean result = false;

		try {
			ArrayDouble.D2 arrayDoubleD2 = Utils.writeMapValueItemToD2ArrayDouble(wrMap, columns);
			int[] origin1 = new int[] {offset, 0};
			try {
				wrNcFile.write(variable, origin1, arrayDoubleD2);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			} catch (InvalidRangeException ex) {
				log.error("Failed writing " + variable + " to netCDF", ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}
	//</editor-fold>
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="POJOs TO netCDFJOs">
	//<editor-fold defaultstate="expanded" desc="ArrayChar.D3">
	public static ArrayChar.D3 writeMapToCurrentSampleArrayCharD3(Map<?, Object> map, int stride) {
		ArrayChar.D3 charArray = new ArrayChar.D3(1, map.size(), stride);
		Index ima = charArray.getIndex();

		int markerCounter = 0;
		for (Object value : map.values()) {
			// 1 Sample at a time, iterating through markers, starting at gtSpan 0
			charArray.setString(ima.set(0, markerCounter, 0), value.toString().trim()); // XXX This is currently unused, but one might have to use org.gwaspi.global.Utils.toMeaningfullRep() instead of toString() here
			markerCounter++;
		}

		return charArray;
	}

	public static ArrayChar.D3 writeMapToCurrentMarkerArrayCharD3(Map<?, Object> map, int stride) {
		ArrayChar.D3 charArray = new ArrayChar.D3(map.size(), 1, stride);
		Index ima = charArray.getIndex();

		int sampleCounter = 0;
		for (Object value : map.values()) {
			// 1 Marker at a time, iterating through samples, starting at gtSpan 0
			charArray.setString(ima.set(sampleCounter, 0, 0), value.toString().trim()); // XXX This is currently unused, but one might have to use org.gwaspi.global.Utils.toMeaningfullRep() instead of toString() here
			sampleCounter++;
		}

		return charArray;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ArrayChar.D2">
	public static ArrayChar.D2 writeSingleValueToD2ArrayChar(String value, int stride, int num) {
		ArrayChar.D2 charArray = new ArrayChar.D2(num, stride);
		Index ima = charArray.getIndex();

		final String valueStr = value.trim();
		for (int i = 0; i < num; i++) {
			charArray.setString(ima.set(i, 0), valueStr);
		}

		return charArray;
	}

	public static ArrayChar.D2 writeCollectionToD2ArrayChar(Collection<?> values, int stride) {
		ArrayChar.D2 charArray = new ArrayChar.D2(values.size(), stride);
		Index ima = charArray.getIndex();

		int i = 0;
		for (Object value : values) {
			charArray.setString(ima.set(i, 0), value.toString().trim());
			i++;
		}

		return charArray;
	}

	public static ArrayChar.D2 writeMapValueToD2ArrayChar(Map<?, char[]> map, int stride) {
		return writeCollectionToD2ArrayChar(map.values(), stride);
	}

	public static <K> ArrayChar.D2 writeMapKeysToD2ArrayChar(Map<K, ?> map, int stride) {
		return writeCollectionToD2ArrayChar(map.keySet(), stride);
	}

	public static <V> ArrayChar.D2 writeMapValueItemToD2ArrayChar(Map<?, V> map, TypeConverter<V, String> valueToStringConverter, int stride) {
		ArrayChar.D2 charArray = new ArrayChar.D2(map.size(), stride);
		Index index = charArray.getIndex();

		int count = 0;
		for (V value : map.values()) {
			String strValue = valueToStringConverter.convert(value);
			charArray.setString(index.set(count, 0), strValue.trim());
			count++;
		}

		return charArray;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ArrayDouble.D1 & D2">
	public static ArrayDouble.D1 writeMapValueToD1ArrayDouble(Map<?, Double> map) {
		ArrayDouble.D1 doubleArray = new ArrayDouble.D1(map.size());
		Index index = doubleArray.getIndex();

		int count = 0;
		for (Double value : map.values()) {
			doubleArray.setDouble(index.set(count), value);
			count++;
		}

		return doubleArray;
	}

	private static <V> ArrayDouble.D1 writeMapValueItemToD1ArrayDouble(Map<?, V> map, TypeConverter<V, Double> typeConverter) {
		ArrayDouble.D1 doubleArray = new ArrayDouble.D1(map.size());
		Index index = doubleArray.getIndex();

		int count = 0;
		for (V value : map.values()) {
			doubleArray.setDouble(index.set(count), typeConverter.convert(value));
			count++;
		}

		return doubleArray;
	}

	private static ArrayDouble.D2 writeMapValueItemToD2ArrayDouble(Map<?, Double[]> map, int[] columns) {
		ArrayDouble.D2 doubleArray = new ArrayDouble.D2(map.size(), columns.length);
		Index ima = doubleArray.getIndex();

		int i = 0;
		for (Double[] values : map.values()) {
			for (int j = 0; j < columns.length; j++) {
				doubleArray.setDouble(ima.set(i, j), values[columns[j]]);
			}
			i++;
		}

		return doubleArray;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ArrayInt.D1 & D2">
	public static ArrayInt.D1 writeMapValueToD1ArrayInt(Map<?, Integer> map) {
		ArrayInt.D1 intArray = new ArrayInt.D1(map.size());
		Index index = intArray.getIndex();

		int count = 0;
		for (Integer value : map.values()) {
			intArray.setInt(index.set(count), value);
			count++;
		}

		return intArray;
	}

	/**
	 * @deprecated unused
	 */
	public static ArrayInt.D1 writeMapValueItemToD1ArrayInt(Map<?, Object[]> map, int itemNb) {
		ArrayInt.D1 intArray = new ArrayInt.D1(map.size());
		Index index = intArray.getIndex();

		int count = 0;
		for (Object[] values : map.values()) {
			intArray.setInt(index.set(count), (Integer) values[itemNb]);
			count++;
		}

		return intArray;
	}
	public static <V> ArrayInt.D1 writeMapValueItemToD1ArrayInt(Map<?, V> map, TypeConverter<V, Integer> valueToIntegerConverter) {
		ArrayInt.D1 intArray = new ArrayInt.D1(map.size());
		Index index = intArray.getIndex();

		int count = 0;
		for (V value : map.values()) {
			intArray.setInt(index.set(count), valueToIntegerConverter.convert(value));
			count++;
		}

		return intArray;
	}

	public static ArrayInt.D2 writeMapValueItemToD2ArrayInt(Map<?, int[]> map, int[] columns) {
		ArrayInt.D2 intArray = new ArrayInt.D2(map.size(), columns.length);
		Index ima = intArray.getIndex();

		int i = 0;
		for (int[] values : map.values()) {
			for (int j = 0; j < columns.length; j++) {
				intArray.setInt(ima.set(i, j), values[columns[j]]);
			}
			i++;
		}

		return intArray;
	}

	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ArrayByte.D3">
	public static ArrayByte.D3 writeALValuesToSamplesHyperSlabArrayByteD3(List<byte[]> genotypesAL, int sampleNb, int stride) {
		int markerNb = genotypesAL.size() / sampleNb;
		int alCounter = 0;

		// samplesDim, markersDim, gtStrideDim
		ArrayByte.D3 byteArray = new ArrayByte.D3(sampleNb, markerNb, stride);
		Index ima = byteArray.getIndex();

		for (int markerCounter = 0; markerCounter < markerNb; markerCounter++) {
			for (int sampleCounter = 0; sampleCounter < sampleNb; sampleCounter++) {

				byte[] value = genotypesAL.get(alCounter);
				// 1 Sample at a time, iterating through markers
				byteArray.setByte(ima.set(sampleCounter, markerCounter, 0), value[0]); // first byte
				byteArray.setByte(ima.set(sampleCounter, markerCounter, 1), value[1]); // second byte
				alCounter++;
			}
		}

		return byteArray;
	}

	public static ArrayByte.D3 writeMapToSingleSampleArrayByteD3(Map<?, byte[]> map, int stride) {
		// samplesDim, markersDim, gtStrideDim
		ArrayByte.D3 byteArray = new ArrayByte.D3(1, map.size(), stride);
		Index ima = byteArray.getIndex();

		int markerCount = 0;
		for (byte[] values : map.values()) {
			// 1 Sample at a time, iterating through markers
			byteArray.setByte(ima.set(0, markerCount, 0), values[0]); // first byte
			byteArray.setByte(ima.set(0, markerCount, 1), values[1]); // second byte
			markerCount++;
		}

		return byteArray;
	}

	public static ArrayByte.D3 writeMapToSingleMarkerArrayByteD3(Map<?, byte[]> map, int stride) {
		ArrayByte.D3 byteArray = new ArrayByte.D3(map.size(), 1, stride);
		Index ima = byteArray.getIndex();

		int markerCounter = 0;
		for (byte[] gts : map.values()) {
			// 1 Marker at a time, iterating through samples
			byteArray.setByte(ima.set(markerCounter, 0, 0), gts[0]); // first byte
			byteArray.setByte(ima.set(markerCounter, 0, 1), gts[1]); // second byte
			markerCounter++;
		}

		return byteArray;
	}

	/**
	 * This writeMapToCurrentMarkerArrayByteD3 has now been deprecated in favor
	 * of writeMapToSingleMarkerArrayByteD3 Method is probably INCORRECT!
	 *
	 * @deprecated Use {@link #writeMapToSingleMarkerArrayByteD3} instead
	 */
	public static ArrayByte.D3 writeMapToCurrentMarkerArrayByteD3(Map<?, byte[]> map, int stride) {
		ArrayByte.D3 byteArray = new ArrayByte.D3(1, map.size(), stride);
		Index ima = byteArray.getIndex();

		int markerCounter = 0;
		for (byte[] values : map.values()) {
			// 1 Marker at a time, iterating through samples
			byteArray.setByte(ima.set(0, markerCounter, 0), values[0]); // first byte
			byteArray.setByte(ima.set(0, markerCounter, 1), values[1]); // second byte
			markerCounter++;
		}

		return byteArray;
	}
	//</editor-fold>
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="netCDFJOs TO POJOs">
	//<editor-fold defaultstate="expanded" desc="ArrayChar.D2">
	public static <V> Map<String, V> writeD2ArrayCharToMapKeys(ArrayChar inputArray, V commonValue) {
		Map<String, V> result = new LinkedHashMap<String, V>();
//		StringBuilder key = new StringBuilder("");

		int[] shape = inputArray.getShape();
//		Index index = inputArray.getIndex();
		for (int i = 0; i < shape[0]; i++) {
			ArrayChar wrCharArray = new ArrayChar(new int[] {1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			result.put(new String(values).trim(), commonValue);

//			key = new StringBuilder("");
//			for (int j = 0; j < shape[1]; j++) {
//				key.append(inputArray.getChar(index.set(i,j)));
//			}
//			result.put(key.toString().trim(), commonValue);
		}

		return result;
	}

	/**
	 * @deprecated just remove, as it was wrong anyway (we need MarkerKey, not SampleKey)
	 */
	public static <V> Map<SampleKey, V> writeD2ArrayCharToMapSampleKeys(ArrayChar inputArray, V commonValue) {
		Map<SampleKey, V> result = new LinkedHashMap<SampleKey, V>();

		int[] shape = inputArray.getShape();
		for (int i = 0; i < shape[0]; i++) {
			ArrayChar wrCharArray = new ArrayChar(new int[] {1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			result.put(SampleKey.valueOf(String.valueOf(values).trim()), commonValue);
		}

		return result;
	}

	public static <K> void writeD2ArrayCharToMapValues(ArrayChar inputArray, Map<K, char[]> map) {

		int[] shape = inputArray.getShape();
		Iterator<Entry<K, char[]>> it = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			ArrayChar wrCharArray = new ArrayChar(new int[] {1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			it.next().setValue(String.valueOf(values).trim().toCharArray());
		}
	}

	public static List<String> writeD2ArrayCharToList(ArrayChar inputArray) {
		Long expectedSize = inputArray.getSize();
		List<String> als = new ArrayList(expectedSize.intValue());

		int[] shape = inputArray.getShape();
		for (int i = 0; i < shape[0]; i++) {
			ArrayChar wrCharArray = new ArrayChar(new int[] {1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			als.add(String.valueOf(values).trim());
		}


		return als;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ArrayChar.D1">
	public static Map<String, Object> writeD1ArrayCharToMapKeys(ArrayChar inputArray) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		StringBuilder key = new StringBuilder("");
		Index index = inputArray.getIndex();

		int[] shape = inputArray.getShape();
		for (int j = 0; j < shape[0]; j++) {
			key.append(inputArray.getChar(index.set(j)));
		}
		result.put(key.toString().trim(), "");

		return result;

	}

	public static void writeD1ArrayCharToMapValues(ArrayChar inputArray, Map<String, Object> map) {
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

	//<editor-fold defaultstate="expanded" desc="ArrayDouble.D1">
	public static <K> void writeD1ArrayDoubleToMapValues(ArrayDouble inputArray, Map<K, Double> map) {

		int[] shape = inputArray.getShape();
		Index index = inputArray.getIndex();
		Iterator<Entry<K, Double>> entries = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			Double value = inputArray.getDouble(index.set(i));
			entries.next().setValue(value);
		}
	}

	public static List<Double> writeD1ArrayDoubleToList(ArrayDouble.D1 inputArray) {
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

	//<editor-fold defaultstate="expanded" desc="ArrayDouble.D2">
	public static <K> void writeD2ArrayDoubleToMapValues(ArrayDouble.D2 inputArray, Map<K, double[]> map) {
		int[] shape = inputArray.getShape();
		Iterator<Entry<K, double[]>> entries = map.entrySet().iterator();

		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			ArrayDouble wrDoubleArray = new ArrayDouble(new int[]{1, shape[1]});
			ArrayDouble.D2.arraycopy(inputArray, i, wrDoubleArray, 0, shape[1]);
			double[] values = (double[]) wrDoubleArray.copyTo1DJavaArray();

			entries.next().setValue(values);
		}
	}

	public static List<double[]> writeD2ArrayDoubleToList(ArrayDouble.D2 inputArray) {
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

	//<editor-fold defaultstate="expanded" desc="ArrayInt.D1">
	public static <K> void writeD1ArrayIntToMapValues(ArrayInt inputArray, Map<K, Integer> map) {

		int[] shape = inputArray.getShape();
		Index index = inputArray.getIndex();
		Iterator<Entry<K, Integer>> entries = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			Integer value = inputArray.getInt(index.set(i));
			entries.next().setValue(value);
		}
	}

	public static List<Integer> writeD1ArrayIntToList(ArrayInt.D1 inputArray) {
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

	//<editor-fold defaultstate="expanded" desc="ArrayInt.D2">
	public static <K> void writeD2ArrayIntToMapValues(ArrayInt.D2 inputArray, Map<K, int[]> map) {
		int[] shape = inputArray.getShape();
		Iterator<Entry<K, int[]>> entries = map.entrySet().iterator();

		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			ArrayInt wrIntArray = new ArrayInt(new int[] {1, shape[1]});
			ArrayInt.D2.arraycopy(inputArray, i, wrIntArray, 0, shape[1]);
			int[] values = (int[]) wrIntArray.copyTo1DJavaArray();

			entries.next().setValue(values);
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ArrayByte.D2">
	public static <V> Map<String, V> writeD2ArrayByteToMapKeys(ArrayByte inputArray) {
		Map<String, V> result = new LinkedHashMap<String, V>();

		int[] shape = inputArray.getShape();
		for (int i = 0; i < shape[0]; i++) {
			ArrayByte wrByteArray = new ArrayByte(new int[] {1, shape[1]});
			ArrayByte.D2.arraycopy(inputArray, i * shape[1], wrByteArray, 0, shape[1]);
			byte[] values = (byte[]) wrByteArray.copyTo1DJavaArray();
			result.put(new String(values).trim(), null);
		}

		return result;
	}

	public static <K> void writeD2ArrayByteToMapValues(ArrayByte inputArray, Map<K, byte[]> map) {

		int[] shape = inputArray.getShape();
		Iterator<Entry<K, byte[]>> entries = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			Entry<K, byte[]> entry = entries.next();

			ArrayByte wrArray = new ArrayByte(new int[] {1, shape[1]});
			ArrayByte.D2.arraycopy(inputArray, i * shape[1], wrArray, 0, shape[1]);
			byte[] values = (byte[]) wrArray.copyTo1DJavaArray();
			entry.setValue(values);
		}
	}

	public static List<byte[]> writeD2ArrayByteToList(ArrayByte inputArray) {
		Long expectedSize = inputArray.getSize();
		List<byte[]> als = new ArrayList<byte[]>(expectedSize.intValue());

		int[] shape = inputArray.getShape();
		for (int i = 0; i < shape[0]; i++) {
			ArrayByte wrArray = new ArrayByte(new int[] {1, shape[1]});
			ArrayByte.D2.arraycopy(inputArray, i * shape[1], wrArray, 0, shape[1]);
			byte[] values = (byte[]) wrArray.copyTo1DJavaArray();
			als.add(values);
		}

		return als;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ArrayByte.D1">
	public static <K> void writeD1ArrayByteToMapValues(ArrayByte inputArray, Map<K, char[]> map) {
		StringBuilder value = new StringBuilder("");
		Index index = inputArray.getIndex();

		int[] shape = inputArray.getShape();
		Iterator<K> it = map.keySet().iterator();
		K key = it.next();
		for (int j = 0; j < shape[0]; j++) {
			value.append(inputArray.getChar(index.set(j)));
		}
		map.put(key, value.toString().trim().toCharArray());
	}
	//</editor-fold>
	//</editor-fold>
}

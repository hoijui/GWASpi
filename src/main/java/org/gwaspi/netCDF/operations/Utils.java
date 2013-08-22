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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.EnumeratedValueExtractor;
import org.gwaspi.global.TypeConverter;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.Genotype;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

public class Utils {

	private static final Logger log
			= LoggerFactory.getLogger(Utils.class);

	private Utils() {
	}

	//<editor-fold defaultstate="expanded" desc="SAVERS">
	public static <K> boolean saveCharMapKeyToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<K> keys, String variable, int varStride) {
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = writeCollectionToD2ArrayChar(keys, varStride);

			int[] markersOrig = new int[] {0, 0};
			try {
				wrNcFile.write(variable, markersOrig, markersD2);
				log.info("Done writing {}", variable);
				result = true;
			} catch (IOException ex) {
				log.error("Failed writing file", ex);
			} catch (InvalidRangeException ex) {
				throw new IOException(ex);
			}
		} catch (Exception ex) {
			log.error("Failed writing " + variable, ex);
		}

		return result;
	}

	public static boolean saveCharMapValueToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<char[]> wrMap, String variable, int varStride) {
		return saveCharMapToWrMatrix(wrNcFile, wrMap, variable, varStride, 0);
	}

	public static <V> boolean saveCharMapItemToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<V> wrMap, String variable, TypeConverter<V, String> typeConverter, int varStride) {
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = writeValuesToD2ArrayChar(wrMap, typeConverter, varStride);
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
	public static boolean saveSingleSampleGTsToMatrix(NetcdfFileWriteable wrNcFile, Collection<byte[]> values, int sampleIndex) {
		boolean result = false;
		ArrayByte.D3 genotypes = writeToSingleSampleArrayByteD3(values, cNetCDF.Strides.STRIDE_GT);

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

	public static boolean saveSingleMarkerGTsToMatrix(NetcdfFileWriteable wrNcFile, Collection<byte[]> rawGenotypes, int markerIndex) {
		boolean result = false;
		ArrayByte.D3 genotypes = writeMapToSingleMarkerArrayByteD3(rawGenotypes, cNetCDF.Strides.STRIDE_GT);

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
	public static boolean saveDoubleMapD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Double> wrMap, String variable) {
		return saveDoubleMapD1ToWrMatrix(wrNcFile, wrMap, variable, 0);
	}

	public static boolean saveDoubleMapItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Double[]> wrMap, final int itemNb, String variable) {

		TypeConverter<Double[], Double> typeConverter = new TypeConverter<Double[], Double>() {
			@Override
			public Double convert(Double[] from) {
				return from[itemNb];
			}
		};

		return saveDoubleMapItemD1ToWrMatrix(wrNcFile, wrMap, typeConverter, variable);
	}

	public static <V> boolean saveDoubleMapItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<V> wrMap, TypeConverter<V, Double> typeConverter, String variable) {
		boolean result = false;

		try {
			ArrayDouble.D1 arrayDouble = Utils.writeValuesToD1ArrayDouble(wrMap, typeConverter);
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

	public static boolean saveIntMapD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Integer> wrMap, String variable) {
		return saveIntMapD1ToWrMatrix(wrNcFile, wrMap, variable, 0);
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="D2 SAVERS">
	public static boolean saveIntMapD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<int[]> values, int[] columns, String variable) {
		return saveIntMapD2ToWrMatrix(wrNcFile, values, columns, variable, 0);
	}

	public static boolean saveChromosomeInfosD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<ChromosomeInfo> values, int[] columns, String variable) {
		return saveIntMapD2ToWrMatrix(wrNcFile, values, ChromosomeInfo.EXTRACTOR, variable, 0);
	}

	public static <V> boolean saveIntMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<V> values,
			EnumeratedValueExtractor<V, Iterator<Integer>> valuesExtractor,
			String variable)
	{
		return saveIntMapD2ToWrMatrix(wrNcFile, values, valuesExtractor, variable, 0);
	}

	public static boolean saveDoubleMapD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Double[]> values, int[] columns, String variable) {
		return saveDoubleMapD2ToWrMatrix(wrNcFile, values, columns, variable, 0);
	}
	//</editor-fold>
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="CHUNKED SAVERS">
	public static boolean saveCharMapToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<char[]> values,
			String variable,
			int varStride,
			int offset)
	{
		boolean result = false;

		try {
			ArrayChar.D2 markersD2 = writeCollectionToD2ArrayChar(values, varStride);

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

	//<editor-fold defaultstate="expanded" desc="D1 SAVERS">
	public static boolean saveDoubleMapD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Double> values,
			String variable,
			int offset)
	{
		boolean result = false;

		try {
			ArrayDouble.D1 arrayDouble = Utils.writeValuesToD1ArrayDouble(values);
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

	public static boolean saveIntMapD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Integer> values,
			String variable,
			int offset)
	{
		boolean result = false;

		try {
			ArrayInt.D1 arrayInt = Utils.writeValuesToD1ArrayInt(values);
			int[] origin1 = new int[] {offset};
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
	public static boolean saveIntMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<int[]> values,
			int[] columns,
			String variable,
			int offset)
	{
		boolean result = false;

		try {
			ArrayInt.D2 arrayIntD2 = Utils.writeValuesToD2ArrayInt(values, columns);
			int[] origin1 = new int[] {offset, 0};
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

	public static <V> boolean saveIntMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<V> values,
			EnumeratedValueExtractor<V, Iterator<Integer>> valuesExtractor,
			String variable,
			int offset)
	{
		boolean result = false;

		try {
			ArrayInt.D2 arrayIntD2 = Utils.writeValuesToD2ArrayInt(values, valuesExtractor);
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

	public static boolean saveDoubleMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Double[]> values,
			int[] columns,
			String variable,
			int offset)
	{
		boolean result = false;

		try {
			ArrayDouble.D2 arrayDoubleD2 = Utils.writeValuesToD2ArrayDouble(values, columns);
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
			if (value.toString().length() >= 2 && value.toString().charAt(1) == '[') {
				throw new RuntimeException(".. this means, we should use org.gwaspi.global.Utils.toMeaningfullRep() in the line below, instead of toString()");
			}
			charArray.setString(ima.set(i, 0), value.toString().trim());
			i++;
		}

		return charArray;
	}

	public static <V> ArrayChar.D2 writeValuesToD2ArrayChar(Collection<V> values, TypeConverter<V, String> valueToStringConverter, int stride) {
		ArrayChar.D2 charArray = new ArrayChar.D2(values.size(), stride);
		Index index = charArray.getIndex();

		int count = 0;
		for (V value : values) {
			String strValue = valueToStringConverter.convert(value);
			charArray.setString(index.set(count, 0), strValue.trim());
			count++;
		}

		return charArray;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ArrayDouble.D1 & D2">
	public static ArrayDouble.D1 writeValuesToD1ArrayDouble(Collection<Double> values) {
		ArrayDouble.D1 doubleArray = new ArrayDouble.D1(values.size());
		Index index = doubleArray.getIndex();

		int count = 0;
		for (Double value : values) {
			doubleArray.setDouble(index.set(count), value);
			count++;
		}

		return doubleArray;
	}

	private static <V> ArrayDouble.D1 writeValuesToD1ArrayDouble(Collection<V> values, TypeConverter<V, Double> typeConverter) {
		ArrayDouble.D1 doubleArray = new ArrayDouble.D1(values.size());
		Index index = doubleArray.getIndex();

		int count = 0;
		for (V value : values) {
			doubleArray.setDouble(index.set(count), typeConverter.convert(value));
			count++;
		}

		return doubleArray;
	}

	private static ArrayDouble.D2 writeValuesToD2ArrayDouble(Collection<Double[]> values, int[] columns) {
		ArrayDouble.D2 doubleArray = new ArrayDouble.D2(values.size(), columns.length);
		Index ima = doubleArray.getIndex();

		int i = 0;
		for (Double[] valuesArr : values) {
			for (int j = 0; j < columns.length; j++) {
				doubleArray.setDouble(ima.set(i, j), valuesArr[columns[j]]);
			}
			i++;
		}

		return doubleArray;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ArrayInt.D1 & D2">
	public static ArrayInt.D1 writeValuesToD1ArrayInt(Collection<Integer> values) {
		ArrayInt.D1 intArray = new ArrayInt.D1(values.size());
		Index index = intArray.getIndex();

		int count = 0;
		for (Integer value : values) {
			intArray.setInt(index.set(count), value);
			count++;
		}

		return intArray;
	}

	public static <V> ArrayInt.D1 writeValuesToD1ArrayInt(Collection<V> values, TypeConverter<V, Integer> valueToIntegerConverter) {
		ArrayInt.D1 intArray = new ArrayInt.D1(values.size());
		Index index = intArray.getIndex();

		int count = 0;
		for (V value : values) {
			intArray.setInt(index.set(count), valueToIntegerConverter.convert(value));
			count++;
		}

		return intArray;
	}

	public static ArrayInt.D2 writeValuesToD2ArrayInt(Collection<int[]> values, int[] columns) {
		ArrayInt.D2 intArray = new ArrayInt.D2(values.size(), columns.length);
		Index ima = intArray.getIndex();

		int i = 0;
		for (int[] valuesArr : values) {
			for (int j = 0; j < columns.length; j++) {
				intArray.setInt(ima.set(i, j), valuesArr[columns[j]]);
			}
			i++;
		}

		return intArray;
	}

	public static <V> ArrayInt.D2 writeValuesToD2ArrayInt(Collection<V> values, EnumeratedValueExtractor<V, Iterator<Integer>> valuesExtractor) {
		ArrayInt.D2 intArray = new ArrayInt.D2(values.size(), valuesExtractor.getNumberOfValues());
		Index ima = intArray.getIndex();

		int i = 0;
		for (V value : values) {
			Iterator<Integer> valuesIt = valuesExtractor.extract(value);
			int j = 0;
			while (valuesIt.hasNext()) {
				intArray.setInt(ima.set(i, j), valuesIt.next());
				j++;
			}
			i++;
		}

		return intArray;
	}

	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ArrayByte.D3">
	public static ArrayByte.D3 writeListValuesToSamplesHyperSlabArrayByteD3(Collection<byte[]> genotypes, int sampleNb, int stride) {
		int markerNb = genotypes.size() / sampleNb;

		// samplesDim, markersDim, gtStrideDim
		ArrayByte.D3 byteArray = new ArrayByte.D3(sampleNb, markerNb, stride);
		Index ima = byteArray.getIndex();

		Iterator<byte[]> genotype = genotypes.iterator();
		for (int markerCounter = 0; markerCounter < markerNb; markerCounter++) {
			for (int sampleCounter = 0; sampleCounter < sampleNb; sampleCounter++) {

				byte[] value = genotype.next();
				// 1 Sample at a time, iterating through markers
				byteArray.setByte(ima.set(sampleCounter, markerCounter, 0), value[0]); // first byte
				byteArray.setByte(ima.set(sampleCounter, markerCounter, 1), value[1]); // second byte
			}
		}

		return byteArray;
	}

	public static ArrayByte.D3 writeToSingleSampleArrayByteD3(Collection<byte[]> values, int stride) {
		// samplesDim, markersDim, gtStrideDim
		ArrayByte.D3 byteArray = new ArrayByte.D3(1, values.size(), stride);
		Index ima = byteArray.getIndex();

		int markerCount = 0;
		for (byte[] value : values) {
			// 1 Sample at a time, iterating through markers
			byteArray.setByte(ima.set(0, markerCount, 0), value[0]); // first byte
			byteArray.setByte(ima.set(0, markerCount, 1), value[1]); // second byte
			markerCount++;
		}

		return byteArray;
	}

	public static ArrayByte.D3 writeMapToSingleMarkerArrayByteD3(Collection<byte[]> values, int stride) {
		ArrayByte.D3 byteArray = new ArrayByte.D3(values.size(), 1, stride);
		Index ima = byteArray.getIndex();

		int markerCounter = 0;
		for (byte[] gts : values) {
			// 1 Marker at a time, iterating through samples
			byteArray.setByte(ima.set(markerCounter, 0, 0), gts[0]); // first byte
			byteArray.setByte(ima.set(markerCounter, 0, 1), gts[1]); // second byte
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
	public static <V> Map<SampleKey, V> writeD2ArrayCharToMapSampleKeys(StudyKey studyKey, ArrayChar inputArray, V commonValue) {
		Map<SampleKey, V> result = new LinkedHashMap<SampleKey, V>();

		int[] shape = inputArray.getShape();
		for (int i = 0; i < shape[0]; i++) {
			ArrayChar wrCharArray = new ArrayChar(new int[] {1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			result.put(SampleKey.valueOf(studyKey, String.valueOf(values).trim()), commonValue);
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

	public static <K> void writeD2ArrayIntToChromosomeInfoMapValues(ArrayInt.D2 inputArray, Map<K, ChromosomeInfo> map) {
		int[] shape = inputArray.getShape();
		Iterator<Entry<K, ChromosomeInfo>> entries = map.entrySet().iterator();

		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			ArrayInt wrIntArray = new ArrayInt(new int[] {1, shape[1]});
			ArrayInt.D2.arraycopy(inputArray, i, wrIntArray, 0, shape[1]);
			int[] values = (int[]) wrIntArray.copyTo1DJavaArray();
			ChromosomeInfo chromosomeInfo = new ChromosomeInfo(
					values[0],
					values[1],
					values[2],
					values[3]);

			entries.next().setValue(chromosomeInfo);
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
		ArrayByte wrArray = new ArrayByte(new int[] {1, shape[1]});
		for (int i = 0; i < shape[0]; i++) {
			Entry<K, byte[]> entry = entries.next();

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


	public static Set<byte[]> extractUniqueGenotypesOrdered(
			final Collection<byte[]> rawGenotypes,
			final List<Boolean> indicesToKeep)
	{
		Map<Integer, byte[]> unique = new TreeMap<Integer, byte[]>();
		Iterator<Boolean> keep = indicesToKeep.iterator();
		for (byte[] genotype : rawGenotypes) {
			if (keep.next().booleanValue()) {
				unique.put(Genotype.hashCode(genotype), genotype);
			}
		}
		Set<byte[]> uniqueGenotypes = new LinkedHashSet<byte[]>(unique.values());
//		Collections.sort(uniqueGenotypes); // NOTE not required, because we use TreeMap

		return uniqueGenotypes;
	}

	public static Set<byte[]> extractUniqueGenotypesOrdered(
			final Collection<byte[]> rawGenotypes)
	{
		Map<Integer, byte[]> unique = new TreeMap<Integer, byte[]>();
		for (byte[] genotype : rawGenotypes) {
			unique.put(Genotype.hashCode(genotype), genotype);
		}
		Set<byte[]> uniqueGenotypes = new LinkedHashSet<byte[]>(unique.values());
//		Collections.sort(uniqueGenotypes); // NOTE not required, because we use TreeMap

		return uniqueGenotypes;
	}
}

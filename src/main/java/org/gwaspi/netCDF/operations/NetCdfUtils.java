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
import ucar.ma2.Array;
import ucar.ma2.ArrayBoolean;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

public class NetCdfUtils {

	private static final Logger log
			= LoggerFactory.getLogger(NetCdfUtils.class);

	private static final int[] ORIGIN_D1 = new int[] {0};
	private static final int[] ORIGIN_D2 = new int[] {0, 0};

	private NetCdfUtils() {
	}

	//<editor-fold defaultstate="expanded" desc="SAVERS">
	public static <K> void saveObjectsToStringToMatrix(NetcdfFileWriteable wrNcFile, Collection<K> keys, String variable, int varStride) throws IOException {

		ArrayChar.D2 markersD2 = writeCollectionToD2ArrayChar(keys, varStride);
		try {
			wrNcFile.write(variable, ORIGIN_D2, markersD2);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	public static void saveCharMapValueToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<char[]> wrMap, String variable, int varStride) throws IOException {
		saveCharMapToWrMatrix(wrNcFile, wrMap, variable, varStride, 0);
	}

	public static <V> void saveCharMapItemToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<V> wrMap, String variable, TypeConverter<V, String> typeConverter, int varStride) throws IOException {

		ArrayChar.D2 markersD2 = writeValuesToD2ArrayChar(wrMap, typeConverter, varStride);
		try {
			wrNcFile.write(variable, ORIGIN_D2, markersD2);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	public static <V> void saveByteMapItemToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<V> wrMap, String variable, TypeConverter<V, Byte> typeConverter, int varStride) throws IOException {

		ArrayByte.D2 markersD2 = writeValuesToD2ArrayByte(wrMap, typeConverter, varStride);
		try {
			wrNcFile.write(variable, ORIGIN_D2, markersD2);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	//<editor-fold defaultstate="expanded" desc="GENOTYPE SAVERS">
	public static void saveSingleSampleGTsToMatrix(NetcdfFileWriteable wrNcFile, Collection<byte[]> values, int sampleIndex) throws IOException {

		ArrayByte.D3 genotypes = writeToSingleSampleArrayByteD3(values, cNetCDF.Strides.STRIDE_GT);
		int[] sampleGTOrigin = new int[] {sampleIndex, 0, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GENOTYPES, sampleGTOrigin, genotypes);
//			log.info("Done writing Sample {} genotypes", samplePos);
		} catch (InvalidRangeException ex) {
			throw new IOException("Failed writing genotypes to netCDF in MatrixDataExtractor, netCDF file " + wrNcFile.toString(), ex);
		}
	}

	public static void saveSingleMarkerGTsToMatrix(NetcdfFileWriteable wrNcFile, Collection<byte[]> rawGenotypes, int markerIndex) throws IOException {

		ArrayByte.D3 genotypes = writeMapToSingleMarkerArrayByteD3(rawGenotypes, cNetCDF.Strides.STRIDE_GT);
		int[] markerGTOrigin = new int[] {0, markerIndex, 0};
		try {
			wrNcFile.write(cNetCDF.Variables.VAR_GENOTYPES, markerGTOrigin, genotypes);
//			log.info("Done writing genotypes");
		} catch (InvalidRangeException ex) {
			throw new IOException("Failed writing genotypes to netCDF in MatrixDataExtractor, netCDF file " + wrNcFile.toString(), ex);
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="D1 SAVERS">
	public static void saveDoubleMapD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Double> wrMap, String variable) throws IOException {
		saveDoubleMapD1ToWrMatrix(wrNcFile, wrMap, variable, 0);
	}

	public static void saveDoubleMapItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Double[]> wrMap, final int itemNb, String variable) throws IOException {

		TypeConverter<Double[], Double> typeConverter = new TypeConverter<Double[], Double>() {
			@Override
			public Double convert(Double[] from) {
				return from[itemNb];
			}
		};

		saveDoubleMapItemD1ToWrMatrix(wrNcFile, wrMap, typeConverter, variable);
	}

	public static <V> void saveDoubleMapItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<V> wrMap, TypeConverter<V, Double> typeConverter, String variable) throws IOException {

		ArrayDouble.D1 arrayDouble = NetCdfUtils.writeValuesToD1ArrayDouble(wrMap, typeConverter);
		try {
			wrNcFile.write(variable, ORIGIN_D1, arrayDouble);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	public static void saveIntMapD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Integer> wrMap, String variable) throws IOException {
		saveIntMapD1ToWrMatrix(wrNcFile, wrMap, variable, 0);
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="D2 SAVERS">
	public static void saveIntMapD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<int[]> values, int[] columns, String variable) throws IOException {
		saveIntMapD2ToWrMatrix(wrNcFile, values, columns, variable, 0);
	}

	public static void saveChromosomeInfosD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<ChromosomeInfo> values, int[] columns, String variable) throws IOException {
		saveIntMapD2ToWrMatrix(wrNcFile, values, ChromosomeInfo.EXTRACTOR, variable, 0);
	}

	public static <V> void saveIntMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<V> values,
			EnumeratedValueExtractor<V, Iterator<Integer>> valuesExtractor,
			String variable)
			throws IOException
	{
		saveIntMapD2ToWrMatrix(wrNcFile, values, valuesExtractor, variable, 0);
	}

	public static void saveDoubleMapD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Double[]> values, int[] columns, String variable) throws IOException {
		saveDoubleMapD2ToWrMatrix(wrNcFile, values, columns, variable, 0);
	}
	//</editor-fold>
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="CHUNKED SAVERS">
	public static void saveCharMapToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<char[]> values,
			String variable,
			int varStride,
			int offset)
			throws IOException
	{
		ArrayChar.D2 markersD2 = writeCollectionToD2ArrayChar(values, varStride);
		// first origin is the initial markerset position,
		// second is the original allele position
		int[] markersOrigin = new int[] {offset, 0};
		try {
			wrNcFile.write(variable, markersOrigin, markersD2);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	//<editor-fold defaultstate="expanded" desc="D1 SAVERS">
	public static void saveDoubleMapD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Double> values,
			String variable,
			int offset)
			throws IOException
	{
		ArrayDouble.D1 arrayDouble = NetCdfUtils.writeValuesToD1ArrayDouble(values);
		int[] originD1 = new int[] {offset};
		try {
			wrNcFile.write(variable, originD1, arrayDouble);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	public static void saveIntMapD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Integer> values,
			String variable,
			int offset)
			throws IOException
	{
		ArrayInt.D1 arrayInt = NetCdfUtils.writeValuesToD1ArrayInt(values);
		int[] originD1 = new int[] {offset};
		try {
			wrNcFile.write(variable, originD1, arrayInt);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	public static void saveBooleansD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Boolean> values,
			String variable,
			int offset)
			throws IOException
	{
		ArrayBoolean.D1 arrayBoolean = NetCdfUtils.writeValuesToD1ArrayBoolean(values);
		int[] originD1 = new int[] {offset};
		try {
			wrNcFile.write(variable, originD1, arrayBoolean);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	public static void saveBooleansD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Boolean> values,
			String variable)
			throws IOException
	{
		saveBooleansD1ToWrMatrix(wrNcFile, values, variable, 0);
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="D2 SAVERS">
	public static void saveIntMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<int[]> values,
			int[] columns,
			String variable,
			int offset)
			throws IOException
	{
		ArrayInt.D2 arrayIntD2 = NetCdfUtils.writeValuesToD2ArrayInt(values, columns);
		int[] originD2 = new int[] {offset, 0};
		try {
			wrNcFile.write(variable, originD2, arrayIntD2);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	public static <V> void saveIntMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<V> values,
			EnumeratedValueExtractor<V, Iterator<Integer>> valuesExtractor,
			String variable,
			int offset)
			throws IOException
	{
		ArrayInt.D2 arrayIntD2 = NetCdfUtils.writeValuesToD2ArrayInt(values, valuesExtractor);
		int[] originD2 = new int[] {offset, 0};
		try {
			wrNcFile.write(variable, originD2, arrayIntD2);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	public static void saveDoubleMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Double[]> values,
			int[] columns,
			String variable,
			int offset)
			throws IOException
	{
		ArrayDouble.D2 arrayDoubleD2 = NetCdfUtils.writeValuesToD2ArrayDouble(values, columns);
		int[] originD2 = new int[] {offset, 0};
		try {
			wrNcFile.write(variable, originD2, arrayDoubleD2);
			log.info("Done writing {}", variable);
		} catch (Exception ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
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
			String strValue = value.toString();
			if (strValue.length() >= 2 && strValue.charAt(1) == '[') {
				throw new RuntimeException(".. this means, we should use org.gwaspi.global.Utils.toMeaningfullRep() in the line below, instead of toString()");
			}
			charArray.setString(ima.set(i, 0), strValue.trim());
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
//	public static <V> ArrayChar.D1 writeValuesToD1ArrayChar(Collection<V> values, TypeConverter<V, String> valueToStringConverter, int stride) {
//
//		ArrayChar.D1 charArray = new ArrayChar.D1(values.size(), stride);
//		Index index = charArray.getIndex();
//		int count = 0;
//		for (V value : values) {
//			String strValue = valueToStringConverter.convert(value);
//			charArray.setString(index.set(count), strValue.trim());
//			count++;
//		}
//
//		return charArray;
//	}

	public static <V> ArrayByte.D2 writeValuesToD2ArrayByte(Collection<V> values, TypeConverter<V, Byte> valueToStringConverter, int stride) {

		ArrayByte.D2 byteArray = new ArrayByte.D2(values.size(), stride);
		Index index = byteArray.getIndex();
		int count = 0;
		for (V value : values) {
			Byte byteValue = valueToStringConverter.convert(value);
			byteArray.setByte(index.set(count, 0), byteValue);
			count++;
		}

		return byteArray;
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

	public static ArrayBoolean.D1 writeValuesToD1ArrayBoolean(Collection<Boolean> values) {

		ArrayBoolean.D1 booleanArray = new ArrayBoolean.D1(values.size());
		Index index = booleanArray.getIndex();
		int count = 0;
		for (Boolean value : values) {
			booleanArray.setBoolean(index.set(count), value);
			count++;
		}

		return booleanArray;
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

		int[] shape = inputArray.getShape();
		for (int i = 0; i < shape[0]; i++) {
			ArrayChar wrCharArray = new ArrayChar(new int[] {1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			result.put(new String(values).trim(), commonValue);
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

//	public static <K> void writeD1ArrayByteToMapValues(ArrayByte inputArray, Map<K, Byte> map) {
//
//		int i = 0;
//		for (Map.Entry<K, Byte> entry : map.entrySet()) {
//			entry.setValue(inputArray.getByte(i++));
//			i++;
//		}
//	}
//
//	public static <K> void writeD1ArrayByteToMapValues(ArrayChar inputArray, Map<K, Character> map) {
//
//		int i = 0;
//		for (Map.Entry<K, Character> entry : map.entrySet()) {
//			entry.setValue(inputArray.getChar(i++));
//		}
//	}

	public static <K, V> void writeD1ArrayToMapValues(Array from, Map<K, V> to) {

//		int[] shape = from.getShape();
		Index index = from.getIndex();

		int i = 0;
		if (from instanceof ArrayByte) {
			for (Map.Entry<K, Byte> entry : ((Map<K, Byte>) to).entrySet()) {
				entry.setValue(((ArrayByte) from).getByte(index.set(i++)));
			}
		} else if (from instanceof ArrayChar) {
			for (Map.Entry<K, Character> entry : ((Map<K, Character>) to).entrySet()) {
				entry.setValue(((ArrayChar) from).getChar(index.set(i++)));
			}
		} else if (from instanceof ArrayDouble) {
			for (Map.Entry<K, Double> entry : ((Map<K, Double>) to).entrySet()) {
				entry.setValue(((ArrayDouble) from).getDouble(index.set(i++)));
			}
		} else if (from instanceof ArrayInt) {
			for (Map.Entry<K, Integer> entry : ((Map<K, Integer>) to).entrySet()) {
				entry.setValue(((ArrayInt) from).getInt(index.set(i++)));
			}
		} else if (from instanceof ArrayBoolean) {
			for (Map.Entry<K, Boolean> entry : ((Map<K, Boolean>) to).entrySet()) {
				entry.setValue(((ArrayBoolean) from).getBoolean(index.set(i++)));
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public static <K, V> void writeD2ArrayToMapValues(Array from, Map<K, V> to) {

		int[] shape = from.getShape();
		Iterator<Entry<K, V>> entries = to.entrySet().iterator();
		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			ArrayInt wrValuesArray = new ArrayInt(new int[] {1, shape[1]});
			ArrayInt.D2.arraycopy(from, i, wrValuesArray, 0, shape[1]);
			entries.next().setValue((V) wrValuesArray.copyTo1DJavaArray());
		}
	}


	public static <V> void writeD1ArrayToCollection(Array from, Collection<V> to) {

//		Long expectedSize = input.getSize();
		int[] shape = from.getShape();
		final int size = shape[0]; //|| expectedSize;
		if (to instanceof ArrayList) {
			((ArrayList<V>) to).ensureCapacity(size);
		}

		Index index = from.getIndex();
		if (from instanceof ArrayByte) {
			for (int i = 0; i < size; i++) {
				((Collection<Byte>) to).add(from.getByte(index.set(i)));
			}
		} else if (from instanceof ArrayChar) {
			for (int i = 0; i < size; i++) {
				((Collection<Character>) to).add(from.getChar(index.set(i)));
			}
		} else if (from instanceof ArrayDouble) {
			for (int i = 0; i < size; i++) {
				((Collection<Double>) to).add(from.getDouble(index.set(i)));
			}
		} else if (from instanceof ArrayInt) {
			for (int i = 0; i < size; i++) {
				((Collection<Integer>) to).add(from.getInt(index.set(i)));
			}
		} else if (from instanceof ArrayBoolean) {
			for (int i = 0; i < size; i++) {
				((Collection<Boolean>) to).add(from.getBoolean(index.set(i)));
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public static <V> void writeD2ArrayToCollection(Array from, Collection<V> to) {

		int[] shape = from.getShape();
		final int size = shape[0];
		if (to instanceof ArrayList) {
			((ArrayList<V>) to).ensureCapacity(size);
		}
		final int[] dimensions = new int[] {1, shape[1]};

		Array tmpArray;
		if (from instanceof ArrayByte) {
			tmpArray = new ArrayByte(dimensions);
		} else if (from instanceof ArrayChar) {
			tmpArray = new ArrayChar(dimensions);
		} else if (from instanceof ArrayDouble) {
			tmpArray = new ArrayDouble(dimensions);
		} else if (from instanceof ArrayInt) {
			tmpArray = new ArrayInt(dimensions);
		} else if (from instanceof ArrayBoolean) {
			tmpArray = new ArrayBoolean(dimensions);
		} else {
			throw new UnsupportedOperationException();
		}
		for (int i = 0; i < size; i++) {
			ArrayByte.D2.arraycopy(from, i * shape[1], tmpArray, 0, shape[1]);
			to.add((V) tmpArray.copyTo1DJavaArray());
		}
	}
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

	private static void checkDimensions(int maxSize, Dimension dimension) {

		if (dimension.width == -1) {
			dimension.width = 0;
		}
		if (dimension.height == -1) {
			dimension.height = maxSize - 1;
		}
		if ((dimension.width < 0) || (dimension.width > maxSize)) {
			throw new IllegalArgumentException();
		}
		if ((dimension.height < 0) || (dimension.height > maxSize)) {
			throw new IllegalArgumentException();
		}
		if (dimension.height < dimension.width) {
			throw new IllegalArgumentException();
		}
	}

	public static void readVariable(NetcdfFile ncFile, String variableName, int indexFrom, int indexTo, Collection<?> targetCollection, Map<?, ?> targetMap) throws IOException {

		Variable var = ncFile.findVariable(variableName);

		if ((var == null)
				|| ((targetCollection == null) == (targetMap == null)))
		{
			throw new IllegalArgumentException();
		}

		DataType dataType = var.getDataType();
		final int[] varShape = var.getShape();
		try {
			final int opSetSize = varShape[0];
			Dimension fromTo = new Dimension(indexFrom, indexTo);
			checkDimensions(opSetSize, fromTo);
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

			if (targetCollection != null) {
				if ((dataType == DataType.BYTE)) {
					if (varShape.length == 1) {
						ArrayByte.D1 markerSetAC = (ArrayByte.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToCollection(markerSetAC, (Collection<Byte>)targetCollection);
					} else if (varShape.length == 2) {
						ArrayByte.D2 markerSetAC = (ArrayByte.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToCollection(markerSetAC, (Collection<byte[]>)targetCollection);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if ((dataType == DataType.CHAR)) {
					if (varShape.length == 1) {
						ArrayChar.D1 markerSetAC = (ArrayChar.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToCollection(markerSetAC, (Collection<Character>)targetCollection);
					} else if (varShape.length == 2) {
						ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToCollection(markerSetAC, (Collection<char[]>)targetCollection);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.DOUBLE) {
					if (varShape.length == 1) {
						ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToCollection(markerSetAF, (Collection<Double>)targetCollection);
					} else if (varShape.length == 2) {
						ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToCollection(markerSetAF, (Collection<double[]>)targetCollection);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.INT) {
					if (varShape.length == 1) {
						ArrayInt.D1 markerSetAD = (ArrayInt.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToCollection(markerSetAD, (Collection<Integer>)targetCollection);
					} else if (varShape.length == 2) {
						ArrayInt.D2 markerSetAD = (ArrayInt.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToCollection(markerSetAD, (Collection<int[]>)targetCollection);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.BOOLEAN) {
					if (varShape.length == 1) {
						ArrayBoolean.D1 markerSetAD = (ArrayBoolean.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToCollection(markerSetAD, (Collection<Boolean>)targetCollection);
					} else if (varShape.length == 2) {
						ArrayBoolean.D2 markerSetAD = (ArrayBoolean.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToCollection(markerSetAD, (Collection<boolean[]>)targetCollection);
					} else {
						throw new UnsupportedOperationException();
					}
				} else {
					throw new UnsupportedOperationException();
				}
			} else {
				if ((dataType == DataType.BYTE)) {
					if (varShape.length == 1) {
						ArrayByte.D1 markerSetAC = (ArrayByte.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToMapValues(markerSetAC, (Map<?, Byte>)targetMap);
					} else if (varShape.length == 2) {
						ArrayByte.D2 markerSetAC = (ArrayByte.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayByteToMapValues(markerSetAC, (Map<?, byte[]>)targetMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if ((dataType == DataType.CHAR)) {
					if (varShape.length == 1) {
						ArrayChar.D1 markerSetAC = (ArrayChar.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToMapValues(markerSetAC, (Map<?, Character>)targetMap);
					} else if (varShape.length == 2) {
						ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayCharToMapValues(markerSetAC, (Map<?, char[]>)targetMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.DOUBLE) {
					if (varShape.length == 1) {
						ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayDoubleToMapValues(markerSetAF, (Map<?, Double>)targetMap);
					} else if (varShape.length == 2) {
						ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayDoubleToMapValues(markerSetAF, (Map<?, double[]>)targetMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.INT) {
					if (varShape.length == 1) {
						ArrayInt.D1 markerSetAD = (ArrayInt.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayIntToMapValues(markerSetAD, (Map<?, Integer>)targetMap);
					} else if (varShape.length == 2) {
						ArrayInt.D2 markerSetAD = (ArrayInt.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayIntToMapValues(markerSetAD, (Map<?, int[]>)targetMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.BOOLEAN) {
					if (varShape.length == 1) {
						ArrayBoolean.D1 markerSetAD = (ArrayBoolean.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToMapValues(markerSetAD, (Map<?, Boolean>)targetMap);
					} else if (varShape.length == 2) {
						ArrayBoolean.D2 markerSetAD = (ArrayBoolean.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToMapValues(markerSetAD, (Map<?, boolean[]>)targetMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else {
					throw new UnsupportedOperationException();
				}
			}
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}
}

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

package org.gwaspi.operations;

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
import org.gwaspi.global.Extractor;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.Genotype;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.GenotypesListFactory;
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

	private static final int[] ORIGIN_D1 = new int[] {0};
	private static final int[] ORIGIN_D2 = new int[] {0, 0};

	/**
	 * Whether to use {@link Array#arraycopy(Array, int, Array, int, int)}
	 * or {@link Array#section(int[], int[])}.
	 */
	public static boolean ARRAY_COPY = false;

	private NetCdfUtils() {
	}

	private static void write(final NetcdfFileWriteable wrNcFile, final String variable, final int[] origin, Array data) throws IOException {

		try {
			wrNcFile.write(variable, origin, data);
		} catch (InvalidRangeException ex) {
			throw new IOException("Failed writing " + variable + " to netCDF file " + wrNcFile.toString(), ex);
		}
	}

	public static <K> void saveObjectsToStringToMatrix(NetcdfFileWriteable wrNcFile, Collection<K> keys, String variable, int varStride) throws IOException {

		final ArrayChar.D2 data = writeCollectionToD2ArrayChar(keys, varStride);
		write(wrNcFile, variable, ORIGIN_D2, data);
	}

	public static <V> void saveCharMapItemToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<V> wrMap, String variable, Extractor<V, String> typeConverter, int varStride) throws IOException {

		final ArrayChar.D2 data = writeValuesToD2ArrayChar(wrMap, typeConverter, varStride);
		write(wrNcFile, variable, ORIGIN_D2, data);
	}

	public static <V> void saveByteMapItemToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<V> wrMap, String variable, Extractor<V, Byte> typeConverter, int varStride) throws IOException {

		final ArrayByte.D1 data = writeValuesToD1ArrayByte(wrMap, typeConverter, varStride);
		write(wrNcFile, variable, ORIGIN_D1, data);
	}

	public static void saveSingleSampleGTsToMatrix(NetcdfFileWriteable wrNcFile, Collection<byte[]> values, int sampleIndex) throws IOException {

		final ArrayByte.D3 genotypes = writeToSingleSampleArrayByteD3(values, cNetCDF.Strides.STRIDE_GT);
		final int[] sampleGTOrigin = new int[] {sampleIndex, 0, 0};
		write(wrNcFile, cNetCDF.Variables.VAR_GENOTYPES, sampleGTOrigin, genotypes);
	}

	public static void saveSingleMarkerGTsToMatrix(NetcdfFileWriteable wrNcFile, Collection<byte[]> rawGenotypes, int markerIndex) throws IOException {

		final ArrayByte.D3 genotypes = writeMapToSingleMarkerArrayByteD3(rawGenotypes, cNetCDF.Strides.STRIDE_GT);
		final int[] origin = new int[] {0, markerIndex, 0};
		write(wrNcFile, cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
	}

	public static void saveDoubleMapD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Double> wrMap, String variable) throws IOException {
		saveDoubleMapD1ToWrMatrix(wrNcFile, wrMap, variable, 0);
	}

	public static void saveDoubleMapItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Double[]> wrMap, final int itemNb, String variable) throws IOException {

		Extractor<Double[], Double> typeConverter = new Extractor<Double[], Double>() {
			@Override
			public Double extract(Double[] from) {
				return from[itemNb];
			}
		};

		saveDoubleMapItemD1ToWrMatrix(wrNcFile, wrMap, typeConverter, variable);
	}

	public static <V> void saveDoubleMapItemD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<V> wrMap, Extractor<V, Double> typeConverter, String variable) throws IOException {

		final ArrayDouble.D1 data = NetCdfUtils.writeValuesToD1ArrayDouble(wrMap, typeConverter);
		write(wrNcFile, variable, ORIGIN_D1, data);
	}

	public static void saveCharMapValueToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<char[]> wrMap, String variable, int varStride) throws IOException {
		saveCharMapToWrMatrix(wrNcFile, wrMap, variable, varStride, 0);
	}
	public static void saveCharMapToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<char[]> values,
			String variable,
			int varStride,
			int offset)
			throws IOException
	{
		final ArrayChar.D2 data = writeCollectionToD2ArrayChar(values, varStride);
		// first origin is the initial marker-set position,
		// second is the original allele position
		final int[] origin = new int[] {offset, 0};
		write(wrNcFile, variable, origin, data);
	}

	public static void saveDoubleMapD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Double> values,
			String variable,
			int offset)
			throws IOException
	{
		final ArrayDouble.D1 data = NetCdfUtils.writeValuesToD1ArrayDouble(values);
		final int[] origin = new int[] {offset};
		write(wrNcFile, variable, origin, data);
	}

	public static void saveIntMapD1ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Integer> wrMap, String variable) throws IOException {
		saveIntMapD1ToWrMatrix(wrNcFile, wrMap, variable, 0);
	}
	public static void saveIntMapD1ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Integer> values,
			String variable,
			int offset)
			throws IOException
	{
		final ArrayInt.D1 data = NetCdfUtils.writeValuesToD1ArrayInt(values);
		final int[] origin = new int[] {offset};
		write(wrNcFile, variable, origin, data);
	}

	public static void saveIntMapD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<int[]> values, int[] columns, String variable) throws IOException {
		saveIntMapD2ToWrMatrix(wrNcFile, values, columns, variable, 0);
	}
	public static void saveIntMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<int[]> values,
			int[] columns,
			String variable,
			int offset)
			throws IOException
	{
		final ArrayInt.D2 data = NetCdfUtils.writeValuesToD2ArrayInt(values, columns);
		final int[] origin = new int[] {offset, 0};
		write(wrNcFile, variable, origin, data);
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
	public static <V> void saveIntMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<V> values,
			EnumeratedValueExtractor<V, Iterator<Integer>> valuesExtractor,
			String variable,
			int offset)
			throws IOException
	{
		final ArrayInt.D2 data = NetCdfUtils.writeValuesToD2ArrayInt(values, valuesExtractor);
		final int[] origin = new int[] {offset, 0};
		write(wrNcFile, variable, origin, data);
	}

	public static void saveDoubleMapD2ToWrMatrix(NetcdfFileWriteable wrNcFile, Collection<Double[]> values, int[] columns, String variable) throws IOException {
		saveDoubleMapD2ToWrMatrix(wrNcFile, values, columns, variable, 0);
	}
	public static void saveDoubleMapD2ToWrMatrix(
			NetcdfFileWriteable wrNcFile,
			Collection<Double[]> values,
			int[] columns,
			String variable,
			int offset)
			throws IOException
	{
		final ArrayDouble.D2 data = NetCdfUtils.writeValuesToD2ArrayDouble(values, columns);
		final int[] origin = new int[] {offset, 0};
		write(wrNcFile, variable, origin, data);
	}

	public static ArrayChar.D2 writeSingleValueToD2ArrayChar(String value, int stride, int num) {

		final ArrayChar.D2 charArray = new ArrayChar.D2(num, stride);
		final Index ima = charArray.getIndex();
		final String valueStr = value.trim();
		for (int i = 0; i < num; i++) {
			charArray.setString(ima.set(i, 0), valueStr);
		}

		return charArray;
	}

	public static ArrayChar.D2 writeCollectionToD2ArrayChar(Collection<?> values, int stride) {

		final ArrayChar.D2 charArray = new ArrayChar.D2(values.size(), stride);
		final Index ima = charArray.getIndex();
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

	public static <V> ArrayChar.D2 writeValuesToD2ArrayChar(Collection<V> values, Extractor<V, String> valueToStringConverter, int stride) {

		final ArrayChar.D2 charArray = new ArrayChar.D2(values.size(), stride);
		final Index index = charArray.getIndex();
		int count = 0;
		for (V value : values) {
			String strValue = valueToStringConverter.extract(value);
			charArray.setString(index.set(count, 0), strValue.trim());
			count++;
		}

		return charArray;
	}

	public static <V> ArrayByte.D1 writeValuesToD1ArrayByte(Collection<V> values, Extractor<V, Byte> valueToStringConverter, int stride) {

		final ArrayByte.D1 byteArray = new ArrayByte.D1(values.size());
		final Index index = byteArray.getIndex();
		int count = 0;
		for (V value : values) {
			Byte byteValue = valueToStringConverter.extract(value);
			byteArray.setByte(index.set(count), byteValue);
			count++;
		}

		return byteArray;
	}

	public static <V> ArrayByte.D2 writeValuesToD2ArrayByte(Collection<V> values, Extractor<V, Byte> valueToStringConverter, int stride) {

		final ArrayByte.D2 byteArray = new ArrayByte.D2(values.size(), stride);
		final Index index = byteArray.getIndex();
		int count = 0;
		for (V value : values) {
			Byte byteValue = valueToStringConverter.extract(value);
			byteArray.setByte(index.set(count, 0), byteValue);
			count++;
		}

		return byteArray;
	}

	public static ArrayDouble.D1 writeValuesToD1ArrayDouble(Collection<Double> values) {

		final ArrayDouble.D1 doubleArray = new ArrayDouble.D1(values.size());
		final Index index = doubleArray.getIndex();
		int count = 0;
		for (Double value : values) {
			doubleArray.setDouble(index.set(count), value);
			count++;
		}

		return doubleArray;
	}

	private static <V> ArrayDouble.D1 writeValuesToD1ArrayDouble(Collection<V> values, Extractor<V, Double> typeConverter) {

		final ArrayDouble.D1 doubleArray = new ArrayDouble.D1(values.size());
		final Index index = doubleArray.getIndex();
		int count = 0;
		for (V value : values) {
			doubleArray.setDouble(index.set(count), typeConverter.extract(value));
			count++;
		}

		return doubleArray;
	}

	private static ArrayDouble.D2 writeValuesToD2ArrayDouble(Collection<Double[]> values, int[] columns) {

		final ArrayDouble.D2 doubleArray = new ArrayDouble.D2(values.size(), columns.length);
		final Index ima = doubleArray.getIndex();
		int i = 0;
		for (Double[] valuesArr : values) {
			for (int j = 0; j < columns.length; j++) {
				doubleArray.setDouble(ima.set(i, j), valuesArr[columns[j]]);
			}
			i++;
		}

		return doubleArray;
	}

	public static ArrayInt.D1 writeValuesToD1ArrayInt(Collection<Integer> values) {

		final ArrayInt.D1 intArray = new ArrayInt.D1(values.size());
		final Index index = intArray.getIndex();
		int count = 0;
		for (Integer value : values) {
			intArray.setInt(index.set(count), value);
			count++;
		}

		return intArray;
	}

	public static <V> ArrayInt.D1 writeValuesToD1ArrayInt(Collection<V> values, Extractor<V, Integer> valueToIntegerConverter) {

		final ArrayInt.D1 intArray = new ArrayInt.D1(values.size());
		final Index index = intArray.getIndex();
		int count = 0;
		for (V value : values) {
			intArray.setInt(index.set(count), valueToIntegerConverter.extract(value));
			count++;
		}

		return intArray;
	}

	public static ArrayInt.D2 writeValuesToD2ArrayInt(Collection<int[]> values, int[] columns) {

		final ArrayInt.D2 intArray = new ArrayInt.D2(values.size(), columns.length);
		final Index ima = intArray.getIndex();
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

		final ArrayInt.D2 intArray = new ArrayInt.D2(values.size(), valuesExtractor.getNumberOfValues());
		final Index ima = intArray.getIndex();
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

	public static ArrayByte.D3 writeListValuesToSamplesHyperSlabArrayByteD3(Collection<byte[]> genotypes, int sampleNb, int stride) {

		int markerNb = genotypes.size() / sampleNb;
		// samplesDim, markersDim, gtStrideDim
		final ArrayByte.D3 byteArray = new ArrayByte.D3(sampleNb, markerNb, stride);
		final Index ima = byteArray.getIndex();
		final Iterator<byte[]> genotype = genotypes.iterator();
		for (int markerCounter = 0; markerCounter < markerNb; markerCounter++) {
			for (int sampleCounter = 0; sampleCounter < sampleNb; sampleCounter++) {

				final byte[] value = genotype.next();
				// 1 Sample at a time, iterating through markers
				byteArray.setByte(ima.set(sampleCounter, markerCounter, 0), value[0]); // first byte
				byteArray.setByte(ima.set(sampleCounter, markerCounter, 1), value[1]); // second byte
			}
		}

		return byteArray;
	}

	public static ArrayByte.D3 writeToSingleSampleArrayByteD3(Collection<byte[]> values, int stride) {

		// samplesDim, markersDim, gtStrideDim
		final ArrayByte.D3 byteArray = new ArrayByte.D3(1, values.size(), stride);
		final Index ima = byteArray.getIndex();
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

		final ArrayByte.D3 byteArray = new ArrayByte.D3(values.size(), 1, stride);
		final Index ima = byteArray.getIndex();
		int markerCounter = 0;
		for (byte[] gts : values) {
			// 1 Marker at a time, iterating through samples
			byteArray.setByte(ima.set(markerCounter, 0, 0), gts[0]); // first byte
			byteArray.setByte(ima.set(markerCounter, 0, 1), gts[1]); // second byte
			markerCounter++;
		}

		return byteArray;
	}

	public static <V> Map<String, V> writeD2ArrayCharToMapKeys(ArrayChar inputArray, V commonValue) {

		final Map<String, V> result = new LinkedHashMap<String, V>();

		final int[] shape = inputArray.getShape();
		for (int i = 0; i < shape[0]; i++) {
			final ArrayChar wrCharArray = new ArrayChar(new int[] {1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			final char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			result.put(new String(values).trim(), commonValue);
		}

		return result;
	}

	public static <K> void writeD2ArrayCharToMapValues(ArrayChar inputArray, Map<K, char[]> map) {

		final int[] shape = inputArray.getShape();
		final Iterator<Entry<K, char[]>> it = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			final ArrayChar wrCharArray = new ArrayChar(new int[] {1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			final char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			it.next().setValue(String.valueOf(values).trim().toCharArray());
		}
	}

	public static List<String> writeD2ArrayCharToList(ArrayChar inputArray) {

		final int[] shape = inputArray.getShape();
		final List<String> result = new ArrayList(shape[0]);
		for (int i = 0; i < shape[0]; i++) {
			final ArrayChar wrCharArray = new ArrayChar(new int[] {1, shape[1]});
			ArrayChar.D2.arraycopy(inputArray, i * shape[1], wrCharArray, 0, shape[1]);
			final char[] values = (char[]) wrCharArray.copyTo1DJavaArray();
			result.add(String.valueOf(values).trim());
		}

		return result;
	}

	public static <K> void writeD1ArrayDoubleToMapValues(ArrayDouble inputArray, Map<K, Double> map) {

		final int[] shape = inputArray.getShape();
		final Index index = inputArray.getIndex();
		final Iterator<Entry<K, Double>> entries = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			final Double value = inputArray.getDouble(index.set(i));
			entries.next().setValue(value);
		}
	}

	public static List<Double> writeD1ArrayDoubleToList(ArrayDouble.D1 inputArray) {

		final int[] shape = inputArray.getShape();
		final List<Double> result = new ArrayList<Double>(shape[0]);
		final Index index = inputArray.getIndex();
		for (int i = 0; i < shape[0]; i++) {
			final Double value = inputArray.getDouble(index.set(i));
			result.add(value);
		}

		return result;
	}

	public static <K> void writeD2ArrayDoubleToMapValues(ArrayDouble.D2 inputArray, Map<K, double[]> map) {

		final int[] shape = inputArray.getShape();
		final Iterator<Entry<K, double[]>> entries = map.entrySet().iterator();
		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			final ArrayDouble wrDoubleArray = new ArrayDouble(new int[]{1, shape[1]});
			ArrayDouble.D2.arraycopy(inputArray, i, wrDoubleArray, 0, shape[1]);
			final double[] values = (double[]) wrDoubleArray.copyTo1DJavaArray();

			entries.next().setValue(values);
		}
	}

	public static List<double[]> writeD2ArrayDoubleToList(ArrayDouble.D2 inputArray) {

		final int[] shape = inputArray.getShape();
		final List<double[]> result = new ArrayList<double[]>(shape[0]);
		for (int i = 0; i < (shape[0] * shape[1]); i +=  shape[1]) {
			final ArrayDouble wrDoubleArray = new ArrayDouble(new int[]{1, shape[1]});
			ArrayDouble.D2.arraycopy(inputArray, i, wrDoubleArray, 0, shape[1]);
			final double[] values = (double[]) wrDoubleArray.copyTo1DJavaArray();
			result.add(values);
		}

		return result;
	}

	public static <K> void writeD1ArrayIntToMapValues(ArrayInt inputArray, Map<K, Integer> map) {

		final int[] shape = inputArray.getShape();
		final Index index = inputArray.getIndex();
		final Iterator<Entry<K, Integer>> entries = map.entrySet().iterator();
		for (int i = 0; i < shape[0]; i++) {
			final Integer value = inputArray.getInt(index.set(i));
			entries.next().setValue(value);
		}
	}

	public static List<Integer> writeD1ArrayIntToList(ArrayInt.D1 inputArray) {

		final int[] shape = inputArray.getShape();
		final List<Integer> result = new ArrayList<Integer>(shape[0]);
		final Index index = inputArray.getIndex();
		for (int i = 0; i < shape[0]; i++) {
			int value = inputArray.getInt(index.set(i));
			result.add(value);
		}

		return result;
	}

	public static <K> void writeD2ArrayIntToMapValues(ArrayInt.D2 inputArray, Map<K, int[]> map) {

		final int[] shape = inputArray.getShape();
		final Iterator<Entry<K, int[]>> entries = map.entrySet().iterator();
		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			final ArrayInt wrIntArray = new ArrayInt(new int[] {1, shape[1]});
			ArrayInt.D2.arraycopy(inputArray, i, wrIntArray, 0, shape[1]);
			final int[] values = (int[]) wrIntArray.copyTo1DJavaArray();
			entries.next().setValue(values);
		}
	}

	public static <K> void writeD2ArrayIntToChromosomeInfoMapValues(ArrayInt.D2 inputArray, Map<K, ChromosomeInfo> map) {

		final int[] shape = inputArray.getShape();
		final Iterator<Entry<K, ChromosomeInfo>> entries = map.entrySet().iterator();
		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			final ArrayInt wrIntArray = new ArrayInt(new int[] {1, shape[1]});
			ArrayInt.D2.arraycopy(inputArray, i, wrIntArray, 0, shape[1]);
			final int[] values = (int[]) wrIntArray.copyTo1DJavaArray();
			ChromosomeInfo chromosomeInfo = new ChromosomeInfo(
					values[0],
					values[1],
					values[2],
					values[3]);

			entries.next().setValue(chromosomeInfo);
		}
	}

	public static <K> void writeD2ArrayByteToMapValues(ArrayByte inputArray, Map<K, byte[]> map) {

		final int[] shape = inputArray.getShape();
		final Iterator<Entry<K, byte[]>> entries = map.entrySet().iterator();
		final ArrayByte wrArray = new ArrayByte(new int[] {1, shape[1]});
		for (int i = 0; i < shape[0]; i++) {
			Entry<K, byte[]> entry = entries.next();
			ArrayByte.D2.arraycopy(inputArray, i * shape[1], wrArray, 0, shape[1]);
			byte[] values = (byte[]) wrArray.copyTo1DJavaArray();
			entry.setValue(values);
		}
	}

	public static List<byte[]> writeD2ArrayByteToList(ArrayByte inputArray) {

		final int[] shape = inputArray.getShape();
		final int numLists = shape[0];
		final int numValues = shape[1]; // 2, in case of GTs
		final List<byte[]> resultLists = new ArrayList<byte[]>(numLists);

		if (ARRAY_COPY) {
			// Using ArrayByte.D2.arraycopy (slow!)
			final ArrayByte wrArray = new ArrayByte(new int[] {1, numValues});
			for (int li = 0; li < numLists; li++) {
				ArrayByte.D2.arraycopy(inputArray, li * numValues, wrArray, 0, numValues);
				final byte[] values = (byte[]) wrArray.copyTo1DJavaArray();
				resultLists.add(values);
			}
		} else {
			// Using ArrayByte.section(origin, shape)
			int[] origin = {0, 0};
			final int[] newShape = {1, numValues};
			try {
				for (int li = 0; li < numLists; li++) {
					origin[0] = li;
					final ArrayByte.D1 reducedView = (ArrayByte.D1) inputArray.section(origin, newShape);
					final byte[] values = (byte[]) reducedView.copyTo1DJavaArray();
					resultLists.add(values);
				}
			} catch (InvalidRangeException ex) {
				throw new RuntimeException(ex);
			}
		}

		return resultLists;
	}

	public static List<GenotypesList> writeD3ArrayByteToGenotypeLists(
			final ArrayByte.D3 inputArray,
			final GenotypesListFactory genotypesListFactory,
			boolean markers)
			throws IOException
	{
		// NOTE We only support up until Integer Integer.MAX_VALUE lists,
		//   which is 2^31 - 1
		final int[] shape = inputArray.getShape(); // [0]: #samples, [1]: #markers, [2]: #allelesPerGenotype(== 2)
		final List<GenotypesList> genotypesLists = new ArrayList<GenotypesList>(shape[0]);

		final int numLists;
		final int listsIndicesIndex;
		final int[] reducedOrigin = new int[] {0, 0, 0};
		final int[] reducedShape = new int[] {shape[0], shape[1], shape[2]};
		if (markers) {
			listsIndicesIndex = 1;
		} else {
			listsIndicesIndex = 0;
		}
		numLists = shape[listsIndicesIndex];
		reducedOrigin[listsIndicesIndex] = -1;
		reducedShape[listsIndicesIndex] = 1;
		try {
			for (int li = 0; li < numLists; li++) {
				reducedOrigin[listsIndicesIndex] = li;
				final ArrayByte.D2 netCdfGts = (ArrayByte.D2) inputArray.section(reducedOrigin, reducedShape);
				final List<byte[]> rawGts = writeD2ArrayByteToList(netCdfGts);
				genotypesLists.add(genotypesListFactory.extract(rawGts));
			}
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}

		return genotypesLists;
	}

	public static <K, V> void writeD1ArrayToMapValues(Array from, Map<K, V> to) {

		final Index index = from.getIndex();

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

		final int[] shape = from.getShape();
		final Iterator<Entry<K, V>> entries = to.entrySet().iterator();
		for (int i = 0; i < (shape[0] * shape[1]); i = i + shape[1]) {
			ArrayInt wrValuesArray = new ArrayInt(new int[] {1, shape[1]});
			ArrayInt.D2.arraycopy(from, i, wrValuesArray, 0, shape[1]);
			entries.next().setValue((V) wrValuesArray.copyTo1DJavaArray());
		}
	}

	public static <V> void writeD1ArrayToCollection(Array from, Collection<V> to) {

		final int[] shape = from.getShape();
		final int size = shape[0];
		if (to instanceof ArrayList) {
			((ArrayList<V>) to).ensureCapacity(size);
		}

		final Index index = from.getIndex();
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

		final int[] shape = from.getShape();
		final int size = shape[0];
		if (to instanceof ArrayList) {
			((ArrayList<V>) to).ensureCapacity(size);
		}
		final int[] dimensions = new int[] {1, shape[1]};

		final Array tmpArray;
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

		if (from instanceof ArrayChar) {
			for (int i = 0; i < size; i++) {
				ArrayByte.D2.arraycopy(from, i * shape[1], tmpArray, 0, shape[1]);
				final char[] charArray = (char[]) tmpArray.copyTo1DJavaArray();
				String strValue = String.valueOf(charArray); // XXX; // uses all 64 chars, not just up to \0
				strValue = strValue.trim();
				to.add((V) strValue);
//				new ch.qos.logback.classic.encoder.PatternLayoutEncoder()
			}
		} else {
			for (int i = 0; i < size; i++) {
				ArrayByte.D2.arraycopy(from, i * shape[1], tmpArray, 0, shape[1]);
				to.add((V) tmpArray.copyTo1DJavaArray());
			}
		}
	}

	public static Set<byte[]> extractUniqueGenotypesOrdered(
			final Collection<byte[]> rawGenotypes,
			final List<Boolean> indicesToKeep)
	{
		final Map<Integer, byte[]> unique = new TreeMap<Integer, byte[]>();
		final Iterator<Boolean> keep = indicesToKeep.iterator();
		for (byte[] genotype : rawGenotypes) {
			if (keep.next()) {
				unique.put(Genotype.hashCode(genotype), genotype);
			}
		}
		final Set<byte[]> uniqueGenotypes = new LinkedHashSet<byte[]>(unique.values());
//		Collections.sort(uniqueGenotypes); // NOTE not required, because we use TreeMap

		return uniqueGenotypes;
	}

	public static <VT> List<VT> includeOnlyIndices(
			final List<VT> values,
			final List<Integer> indicesToInclude)
	{
		if (values.size() == indicesToInclude.size()) {
			return values;
		} else {
			final List<VT> selected = new ArrayList<VT>(indicesToInclude.size());
			for (Integer indexToSelect : indicesToInclude) {
				selected.add(values.get(indexToSelect));
			}
			return selected;
		}
	}

	public static Set<byte[]> extractUniqueGenotypesOrdered(
			final Collection<byte[]> rawGenotypes)
	{
		final Map<Integer, byte[]> unique = new TreeMap<Integer, byte[]>();
		for (byte[] genotype : rawGenotypes) {
			unique.put(Genotype.hashCode(genotype), genotype);
		}
		final Set<byte[]> uniqueGenotypes = new LinkedHashSet<byte[]>(unique.values());
//		Collections.sort(uniqueGenotypes); // NOTE not required, because we use TreeMap

		return uniqueGenotypes;
	}

	public static void checkDimensions(int maxSize, Dimension dimension) {

		if (dimension.width == -1) {
			dimension.width = 0;
		}
		if (dimension.height == -1) {
			dimension.height = maxSize - 1;
		}
		if ((dimension.width < 0) || (dimension.width > maxSize)) {
			throw new IllegalArgumentException("Dimension width not in range [" + 0 + ", " + maxSize + "]: " + dimension.width);
		}
		if ((dimension.height < 0) || (dimension.height > maxSize)) {
			throw new IllegalArgumentException("Dimension height not in range [" + 0 + ", " + maxSize + "]: " + dimension.height);
		}
		if (dimension.height < dimension.width) {
			throw new IllegalArgumentException("Dimension height (is " + dimension.height + ") should be smaller then width (" + dimension.width + ")");
		}
	}

//	public static <R> void readVariable(NetcdfFile ncFile, String variableName, int indexFrom, int indexTo, Collection<R> targetCollection, Value) throws IOException {
//		XXX;
//	}

	public static void readVariable(NetcdfFile ncFile, String variableName, int indexFrom, int indexTo, Collection<?> targetCollection, Map<?, ?> targetValuesMap) throws IOException {
		readVariable(ncFile, variableName, indexFrom, indexTo, targetCollection, targetValuesMap, null);
	}

	public static void readVariable(NetcdfFile ncFile, String variableName, int indexFrom, int indexTo, Collection<?> targetCollection, Map<?, ?> targetValuesMap, Map<?, ?> targetKeysMap) throws IOException {

		Variable var = ncFile.findVariable(variableName);

		int numSetTargetContainers = 0;
		if (targetCollection != null) {
			numSetTargetContainers++;
		}
		if (targetValuesMap != null) {
			numSetTargetContainers++;
		}
		if (targetKeysMap != null) {
			numSetTargetContainers++;
			throw new UnsupportedOperationException("is also filling values, not keys!");
		}
		if (var == null) {
			throw new IllegalArgumentException("Variable \"" + variableName + "\" not found in Net-CDF file: " + ncFile.getLocation());
		}
		if (numSetTargetContainers != 1) {
			throw new IllegalArgumentException("Exactly one target container has to be specified, but " + numSetTargetContainers + " were");
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
						ArrayByte.D1 data = (ArrayByte.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToCollection(data, (Collection<Byte>)targetCollection);
					} else if (varShape.length == 2) {
						ArrayByte.D2 data = (ArrayByte.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToCollection(data, (Collection<byte[]>)targetCollection);
//					} else if (varShape.length == 3) {
//						ArrayByte.D3 data = (ArrayByte.D3) var.read(fetchVarStr);
//						NetCdfUtils.writeD3ArrayToCollection(data, (Collection<Collection<byte[]>>)targetCollection);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if ((dataType == DataType.CHAR)) {
					if (varShape.length == 1) {
						ArrayChar.D1 data = (ArrayChar.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToCollection(data, (Collection<Character>)targetCollection);
					} else if (varShape.length == 2) {
						ArrayChar.D2 data = (ArrayChar.D2) var.read(fetchVarStr);
//						NetCdfUtils.writeD2ArrayToCollection(data, (Collection<char[]>)targetCollection);
						NetCdfUtils.writeD2ArrayToCollection(data, (Collection<String>)targetCollection);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.DOUBLE) {
					if (varShape.length == 1) {
						ArrayDouble.D1 data = (ArrayDouble.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToCollection(data, (Collection<Double>)targetCollection);
					} else if (varShape.length == 2) {
						ArrayDouble.D2 data = (ArrayDouble.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToCollection(data, (Collection<double[]>)targetCollection);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.INT) {
					if (varShape.length == 1) {
						ArrayInt.D1 data = (ArrayInt.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToCollection(data, (Collection<Integer>)targetCollection);
					} else if (varShape.length == 2) {
						ArrayInt.D2 data = (ArrayInt.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToCollection(data, (Collection<int[]>)targetCollection);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.BOOLEAN) {
					if (varShape.length == 1) {
						ArrayBoolean.D1 data = (ArrayBoolean.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToCollection(data, (Collection<Boolean>)targetCollection);
					} else if (varShape.length == 2) {
						ArrayBoolean.D2 data = (ArrayBoolean.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToCollection(data, (Collection<boolean[]>)targetCollection);
					} else {
						throw new UnsupportedOperationException();
					}
				} else {
					throw new UnsupportedOperationException();
				}
			} else if (targetValuesMap != null) {
				if ((dataType == DataType.BYTE)) {
					if (varShape.length == 1) {
						ArrayByte.D1 data = (ArrayByte.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToMapValues(data, (Map<?, Byte>)targetValuesMap);
					} else if (varShape.length == 2) {
						ArrayByte.D2 data = (ArrayByte.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayByteToMapValues(data, (Map<?, byte[]>)targetValuesMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if ((dataType == DataType.CHAR)) {
					if (varShape.length == 1) {
						ArrayChar.D1 data = (ArrayChar.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToMapValues(data, (Map<?, Character>)targetValuesMap);
					} else if (varShape.length == 2) {
						ArrayChar.D2 data = (ArrayChar.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayCharToMapValues(data, (Map<?, char[]>)targetValuesMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.DOUBLE) {
					if (varShape.length == 1) {
						ArrayDouble.D1 data = (ArrayDouble.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayDoubleToMapValues(data, (Map<?, Double>)targetValuesMap);
					} else if (varShape.length == 2) {
						ArrayDouble.D2 data = (ArrayDouble.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayDoubleToMapValues(data, (Map<?, double[]>)targetValuesMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.INT) {
					if (varShape.length == 1) {
						ArrayInt.D1 data = (ArrayInt.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayIntToMapValues(data, (Map<?, Integer>)targetValuesMap);
					} else if (varShape.length == 2) {
						ArrayInt.D2 data = (ArrayInt.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayIntToMapValues(data, (Map<?, int[]>)targetValuesMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.BOOLEAN) {
					if (varShape.length == 1) {
						ArrayBoolean.D1 data = (ArrayBoolean.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToMapValues(data, (Map<?, Boolean>)targetValuesMap);
					} else if (varShape.length == 2) {
						ArrayBoolean.D2 data = (ArrayBoolean.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToMapValues(data, (Map<?, boolean[]>)targetValuesMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else {
					throw new UnsupportedOperationException();
				}
			} else {
				if ((dataType == DataType.BYTE)) {
					if (varShape.length == 1) {
						ArrayByte.D1 data = (ArrayByte.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToMapValues(data, (Map<?, Byte>)targetKeysMap);
					} else if (varShape.length == 2) {
						ArrayByte.D2 data = (ArrayByte.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayByteToMapValues(data, (Map<?, byte[]>)targetKeysMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if ((dataType == DataType.CHAR)) {
					if (varShape.length == 1) {
						ArrayChar.D1 data = (ArrayChar.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToMapValues(data, (Map<?, Character>)targetKeysMap);
					} else if (varShape.length == 2) {
						ArrayChar.D2 data = (ArrayChar.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayCharToMapValues(data, (Map<?, char[]>)targetKeysMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.DOUBLE) {
					if (varShape.length == 1) {
						ArrayDouble.D1 data = (ArrayDouble.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayDoubleToMapValues(data, (Map<?, Double>)targetKeysMap);
					} else if (varShape.length == 2) {
						ArrayDouble.D2 data = (ArrayDouble.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayDoubleToMapValues(data, (Map<?, double[]>)targetKeysMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.INT) {
					if (varShape.length == 1) {
						ArrayInt.D1 data = (ArrayInt.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayIntToMapValues(data, (Map<?, Integer>)targetKeysMap);
					} else if (varShape.length == 2) {
						ArrayInt.D2 data = (ArrayInt.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayIntToMapValues(data, (Map<?, int[]>)targetKeysMap);
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (dataType == DataType.BOOLEAN) {
					if (varShape.length == 1) {
						ArrayBoolean.D1 data = (ArrayBoolean.D1) var.read(fetchVarStr);
						NetCdfUtils.writeD1ArrayToMapValues(data, (Map<?, Boolean>)targetKeysMap);
					} else if (varShape.length == 2) {
						ArrayBoolean.D2 data = (ArrayBoolean.D2) var.read(fetchVarStr);
						NetCdfUtils.writeD2ArrayToMapValues(data, (Map<?, boolean[]>)targetKeysMap);
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

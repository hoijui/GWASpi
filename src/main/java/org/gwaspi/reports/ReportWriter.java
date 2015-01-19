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

package org.gwaspi.reports;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.gwaspi.constants.ExportConstants;
import org.gwaspi.constants.ImportConstants;
import org.gwaspi.global.Extractor;
import org.gwaspi.global.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportWriter {

	private static final String SEP = ExportConstants.SEPARATOR_REPORTS;

	private static final Logger LOG = LoggerFactory.getLogger(ReportWriter.class);

	private static class MapArrayValueExtractor<K, V> implements Extractor<Entry<K, V>, String> {

		private final boolean prefixSeparator;

		public MapArrayValueExtractor(boolean prefixSeparator) {

			this.prefixSeparator = prefixSeparator;
		}

		@Override
		public String extract(Entry<K, V> object) {

			StringBuilder strVal = new StringBuilder();
			if (object.getValue() instanceof double[]) {
				double[] value = (double[]) object.getValue();
				for (Double v : value) {
					strVal.append(SEP);
					strVal.append(v.toString());
				}
			}
			if (object.getValue() instanceof int[]) {
				int[] value = (int[]) object.getValue();
				for (Integer v : value) {
					strVal.append(SEP);
					strVal.append(v.toString());
				}
			}

			if (!prefixSeparator && (strVal.length() > 0)) {
				strVal.delete(0, SEP.length());
			}

			return strVal.toString();
		}
	}

	private static class MapValueExtractor<K, V> implements Extractor<Entry<K, V>, String> {

		private final String separatorPrefix;

		public MapValueExtractor(boolean prefixSeparator) {

			this.separatorPrefix = prefixSeparator ? SEP : "";
		}

		@Override
		public String extract(Entry<K, V> object) {
			return separatorPrefix + org.gwaspi.global.Utils.toMeaningfullRep(object.getValue());
		}
	}

	private static class MapKeyExtractor<K, V> implements Extractor<Entry<K, V>, String> {

		@Override
		public String extract(Entry<K, V> object) {
			return object.getKey().toString();
		}
	}

	private ReportWriter() {
	}

	protected static <S> boolean writeFirstColumnToReport(
			String reportPath,
			String reportName,
			String header,
			Collection<S> readContent,
			Extractor<S, String> keyExtractor,
			Extractor<S, String> valueExtractor)
			throws IOException
	{
		boolean appendResult = false;

		final File reportFile = new File(reportPath, reportName);

		FileWriter outputFW = new FileWriter(reportFile);
		BufferedWriter outputBW = new BufferedWriter(outputFW);

		final boolean withKey = (keyExtractor != null);

		String sep = ExportConstants.SEPARATOR_REPORTS;
		outputBW.append(header);

		for (S entry : readContent) {
			StringBuilder sb = new StringBuilder();
			String value = valueExtractor.extract(entry);
			if (withKey) {
				sb.append(keyExtractor.extract(entry));
				sb.append(sep);
			}
//			else {
//				// cut off the initial separator from the value
//				try {
//				value = value.substring(sep.length());
//				} catch (Exception ex) {
//					throw new RuntimeException(ex);
//				}
//			}
			sb.append(value);

			sb.append("\n");
			outputBW.append(sb);
		}

		outputBW.close();
		outputFW.close();

		return appendResult;
	}

	protected static <K, V> boolean appendColumnToReport(
			String reportPath,
			String reportName,
			Map<K, V> map,
			boolean isArray,
			boolean withKey) throws IOException
	{
		Extractor<Entry<K, V>, String> valueExtractor;
		if (isArray) {
			valueExtractor = new MapArrayValueExtractor<K, V>(false);
		} else {
			valueExtractor = new MapValueExtractor<K, V>(false);
		}

		Extractor<Entry<K, V>, String> keyExtractor;
		if (withKey) {
			keyExtractor = new MapKeyExtractor<K, V>();
		} else {
			keyExtractor = null;
		}

		return appendColumnToReport(reportPath, reportName, map.entrySet(), keyExtractor, valueExtractor);
	}

	protected static <S> boolean appendColumnToReport(
			String reportPath,
			String reportName,
			Collection<S> readContent,
			Extractor<S, String> keyExtractor,
			Extractor<S, String> valueExtractor) throws IOException
	{
		boolean appendResult = false;

		final File tempFile = new File(reportPath, "tmp.rep");
		final File inputFile = new File(reportPath, reportName);

		FileReader inputFR = new FileReader(inputFile);
		BufferedReader inputBR = new BufferedReader(inputFR);

		FileWriter tempFW = new FileWriter(tempFile);
		BufferedWriter tempBW = new BufferedWriter(tempFW);

		final boolean withKey = (keyExtractor != null);

		String l;
		int count = 0;
		String sep = ExportConstants.SEPARATOR_REPORTS;
		Iterator<S> readContentIt = readContent.iterator();
		while ((l = inputBR.readLine()) != null) {
			if (count == 0) {
				tempBW.append(l);
				tempBW.append("\n");
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append(l);

				S readEntry = readContentIt.next();

				sb.append(sep);
				if (withKey) {
					String key = keyExtractor.extract(readEntry);
					sb.append(key);
					sb.append(sep);
				}
				String value = valueExtractor.extract(readEntry);
				sb.append(value);

				sb.append("\n");
				tempBW.append(sb);
			}
			count++;
		}

		inputBR.close();
		inputFR.close();
		tempBW.close();
		tempFW.close();
		Utils.move(tempFile, inputFile);

		return appendResult;
	}

	protected static <S> List<S> parseReport(
			final File reportFile,
			final Extractor<String[], S> keyExtractor,
			final int numRowsToFetch)
			throws IOException
	{
		final List<S> parsedRows = new ArrayList<S>(numRowsToFetch);

		FileReader inputFileReader = null;
		BufferedReader inputBufferReader = null;
		try {
			inputFileReader = new FileReader(reportFile);
			inputBufferReader = new BufferedReader(inputFileReader);

			// read but ignore the header
			/*String header = */inputBufferReader.readLine();
			int rowIndex = 0;
			while (rowIndex < numRowsToFetch) {
				String line = inputBufferReader.readLine();
				if (line == null) {
					break;
				}
				String[] cVals = line.split(ImportConstants.Separators.separators_SpaceTab_rgxp);
				final S parsedRow = keyExtractor.extract(cVals);
				parsedRows.add(parsedRow);
				rowIndex++;
			}
		} finally {
			if (inputBufferReader != null) {
				inputBufferReader.close();
			} else if (inputFileReader != null) {
				inputFileReader.close();
			}
		}

		return parsedRows;
	}
}

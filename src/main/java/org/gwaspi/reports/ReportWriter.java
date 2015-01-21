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

public class ReportWriter {

	private static final String SEP = ExportConstants.SEPARATOR_REPORTS;

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

	protected static <S> void writeFirstColumnToReport(
			final String reportPath,
			final String reportName,
			final String header,
			final Collection<S> readContent,
			final Extractor<S, String> keyExtractor,
			final Extractor<S, String> valueExtractor)
			throws IOException
	{
		final File reportFile = new File(reportPath, reportName);

		final FileWriter outputFW = new FileWriter(reportFile);
		final BufferedWriter outputBW = new BufferedWriter(outputFW);

		final boolean withKey = (keyExtractor != null);

		final String sep = ExportConstants.SEPARATOR_REPORTS;
		outputBW.append(header);

		for (final S entry : readContent) {
			final String value = valueExtractor.extract(entry);
			if (withKey) {
				outputBW.append(keyExtractor.extract(entry));
				outputBW.append(sep);
			}
			outputBW.append(value);

			outputBW.append('\n');
		}

		outputBW.close();
	}

	protected static <K, V> void appendColumnToReport(
			final String reportPath,
			final String reportName,
			final Map<K, V> map,
			final boolean isArray,
			final boolean withKey) throws IOException
	{
		final Extractor<Entry<K, V>, String> valueExtractor;
		if (isArray) {
			valueExtractor = new MapArrayValueExtractor<K, V>(false);
		} else {
			valueExtractor = new MapValueExtractor<K, V>(false);
		}

		final Extractor<Entry<K, V>, String> keyExtractor;
		if (withKey) {
			keyExtractor = new MapKeyExtractor<K, V>();
		} else {
			keyExtractor = null;
		}

		appendColumnToReport(reportPath, reportName, map.entrySet(), keyExtractor, valueExtractor);
	}

	protected static <S> void appendColumnToReport(
			final String reportPath,
			final String reportName,
			final Collection<S> readContent,
			final Extractor<S, String> keyExtractor,
			final Extractor<S, String> valueExtractor) throws IOException
	{
		final File tempFile = new File(reportPath, "tmp.rep");
		final File inputFile = new File(reportPath, reportName);

		FileReader inputFR = null;
		BufferedReader inputBR = null;
		FileWriter outputFW = null;
		BufferedWriter outputBW = null;
		try {
			inputFR = new FileReader(inputFile);
			inputBR = new BufferedReader(inputFR);

			outputFW = new FileWriter(tempFile);
			outputBW = new BufferedWriter(outputFW);

			final boolean withKey = (keyExtractor != null);

			final String sep = ExportConstants.SEPARATOR_REPORTS;
			final Iterator<S> readContentIt = readContent.iterator();
			// read an re-write the header
			String inputLine = inputBR.readLine();
			outputBW.append(inputLine).append('\n');

			// read, extend and re-write the data
			while ((inputLine = inputBR.readLine()) != null) {
				outputBW.append(inputLine);

				final S readEntry = readContentIt.next();

				outputBW.append(sep);
				if (withKey) {
					String key = keyExtractor.extract(readEntry);
					outputBW.append(key);
					outputBW.append(sep);
				}
				final String value = valueExtractor.extract(readEntry);
				outputBW.append(value);

				outputBW.append('\n');
			}

			inputBR.close();
			outputBW.close();
		} finally {
			if (inputBR != null) {
				inputBR.close();
			} else if (inputFR != null) {
				inputFR.close();
			}
			if (outputBW != null) {
				outputBW.close();
			} else if (outputFW != null) {
				outputFW.close();
			}
		}

		Utils.move(tempFile, inputFile);
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
				final String line = inputBufferReader.readLine();
				if (line == null) {
					break;
				}
				final String[] cVals = line.split(ImportConstants.Separators.separators_SpaceTab_rgxp);
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

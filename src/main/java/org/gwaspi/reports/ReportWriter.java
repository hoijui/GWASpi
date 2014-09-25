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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.gwaspi.constants.cExport;
import org.gwaspi.global.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportWriter {

	private static final String SEP = cExport.separator_REPORTS;

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

	protected static <K, V> boolean writeFirstColumnToReport(
			String reportPath,
			String reportName,
			String header,
			Map<K, V> map,
			boolean withKey) throws IOException
	{
		Extractor<Entry<K, V>, String> valueExtractor = new MapValueExtractor<K, V>(false);

		Extractor<Entry<K, V>, String> keyExtractor;
		if (withKey) {
			keyExtractor = new MapKeyExtractor<K, V>();
		} else {
			keyExtractor = null;
		}

		return writeFirstColumnToReport(reportPath, reportName, header, map.entrySet(), keyExtractor, valueExtractor);
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

		FileWriter outputFW = new FileWriter(reportPath + reportName);
		BufferedWriter outputBW = new BufferedWriter(outputFW);

		final boolean withKey = (keyExtractor != null);

		String sep = cExport.separator_REPORTS;
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

		String tempFile = reportPath + "tmp.rep";
		String inputFile = reportPath + reportName;

		FileReader inputFR = new FileReader(inputFile);
		BufferedReader inputBR = new BufferedReader(inputFR);

		FileWriter tempFW = new FileWriter(tempFile);
		BufferedWriter tempBW = new BufferedWriter(tempFW);

		final boolean withKey = (keyExtractor != null);

		String l;
		int count = 0;
		String sep = cExport.separator_REPORTS;
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
		copyFile(tempFile, inputFile);
		deleteFile(tempFile);

		return appendResult;
	}

	private static void copyFile(String srFile, String dtFile) throws IOException {

		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(new File(srFile));
			final boolean append = false;
			out = new FileOutputStream(new File(dtFile), append);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					LOG.warn("Failed to close source stream when copying file", ex);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
					LOG.warn("Failed to close destination stream when copying file", ex);
				}
			}
		}
	}

	private static void deleteFile(String tempFile) { // TODO merge with org.gwaspi.global.Utils.tryToDeleteFile()
		File f = new File(tempFile);

		// Make sure the file or directory exists and isn't write protected
		if (!f.exists()) {
			throw new IllegalArgumentException("Delete: no such file or directory: " + tempFile);
		}

		if (!f.canWrite()) {
			throw new IllegalArgumentException("Delete: write protected: " + tempFile);
		}

		// If it is a directory, make sure it is empty
		if (f.isDirectory()) {
			String[] files = f.list();
			if (files.length > 0) {
				throw new IllegalArgumentException("Delete: directory not empty: " + tempFile);
			}
		}

		// Attempt to delete it
		boolean success = f.delete();

		if (!success) {
			throw new IllegalArgumentException("Delete: deletion failed");
		}

	}
}

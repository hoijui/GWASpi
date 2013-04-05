package org.gwaspi.reports;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.gwaspi.constants.cExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class ReportWriter {

	private static final Logger log = LoggerFactory.getLogger(ReportWriter.class);

	private ReportWriter() {
	}

	protected static <K, V> boolean writeFirstColumnToReport(
			String reportPath,
			String reportName,
			String header,
			Map<K, V> map,
			boolean withKey)
			throws IOException
	{
		boolean appendResult = false;

		FileWriter outputFW = new FileWriter(reportPath + reportName);
		BufferedWriter outputBW = new BufferedWriter(outputFW);

		String sep = cExport.separator_REPORTS;
		outputBW.append(header);

		for (Map.Entry<K, V> entry : map.entrySet()) {
			StringBuilder sb = new StringBuilder();
			if (withKey) {
				sb.append(entry.getKey().toString());
				sb.append(sep);
			}
			sb.append(org.gwaspi.global.Utils.toMeaningfullRep(entry.getValue()));

			sb.append("\n");
			outputBW.append(sb);
		}

		outputBW.close();
		outputFW.close();

		return appendResult;
	}

	protected static <K, V> boolean appendColumnToReport(String reportPath,
			String reportName,
			Map<K, V> map,
			boolean isArray,
			boolean withKey) throws IOException {
		boolean appendResult = false;

		String tempFile = reportPath + "tmp.rep";
		String inputFile = reportPath + reportName;

		FileReader inputFR = new FileReader(inputFile);
		BufferedReader inputBR = new BufferedReader(inputFR);

		FileWriter tempFW = new FileWriter(tempFile);
		BufferedWriter tempBW = new BufferedWriter(tempFW);

		String l;
		int count = 0;
		String sep = cExport.separator_REPORTS;
		Iterator<Entry<K, V>> it = map.entrySet().iterator();
		while ((l = inputBR.readLine()) != null) {
			if (count == 0) {
				tempBW.append(l);
				tempBW.append("\n");
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append(l);

				Entry<K, V> entry = it.next();
				String key = entry.getKey().toString();
				if (isArray) {
					if (withKey) {
						sb.append(sep);
						sb.append(key);
					}

					if (entry.getValue() instanceof double[]) {
						double[] value = (double[]) entry.getValue();
						for (Double v : value) {
							sb.append(sep);
							sb.append(v.toString());
						}
					}
					if (entry.getValue() instanceof int[]) {
						int[] value = (int[]) entry.getValue();
						for (Integer v : value) {
							sb.append(sep);
							sb.append(v.toString());
						}
					}
				} else {
					if (withKey) {
						sb.append(sep);
						sb.append(key.toString());
					}
					sb.append(sep);
					sb.append(org.gwaspi.global.Utils.toMeaningfullRep(entry.getValue()));
				}

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

	private static void copyFile(String srFile, String dtFile) {
		try {
			InputStream in = new FileInputStream(new File(srFile));
			final boolean append = false;
			OutputStream out = new FileOutputStream(new File(dtFile), append);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException ex) {
			log.error("File not found in the specified directory", ex);
//			org.gwaspi.gui.StartGWASpi.exit();
		} catch (IOException ex) {
			log.error(null, ex);
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

package org.gwaspi.reports;

import org.gwaspi.constants.cExport;
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

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class ReportWriter {

	private ReportWriter() {
	}

	protected static boolean writeFirstColumnToReport(String reportPath,
			String reportName,
			String header,
			Map<String, Object> lhm,
			boolean withKey) throws IOException {
		boolean appendResult = false;

		FileWriter outputFW = new FileWriter(reportPath + reportName);
		BufferedWriter outputBW = new BufferedWriter(outputFW);

		String l;
		int count = 0;
		String sep = cExport.separator_REPORTS;
		outputBW.append(header);

		for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			Object value = lhm.get(key);

			StringBuilder sb = new StringBuilder();
			if (withKey) {
				sb.append(key.toString());
				sb.append(sep);
			}
			sb.append(value.toString());

			sb.append("\n");
			outputBW.append(sb);
		}

		outputBW.close();
		outputFW.close();

		return appendResult;
	}

	protected static boolean appendColumnToReport(String reportPath,
			String reportName,
			Map<String, Object> lhm,
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
		Iterator it = lhm.keySet().iterator();
		while ((l = inputBR.readLine()) != null) {
			if (count == 0) {
				tempBW.append(l);
				tempBW.append("\n");
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append(l);

				Object key = it.next();
				if (isArray) {
					if (withKey) {
						sb.append(sep);
						sb.append(key.toString());
					}

					if (lhm.get(key).getClass().getName().equals("[D")) {
						double[] value = (double[]) lhm.get(key);
						for (Double v : value) {
							sb.append(sep);
							sb.append(v.toString());
						}
					}
					if (lhm.get(key).getClass().getName().equals("[I")) {
						int[] value = (int[]) lhm.get(key);
						for (Integer v : value) {
							sb.append(sep);
							sb.append(v.toString());
						}
					}
				} else {
					Object value = lhm.get(key);
					if (withKey) {
						sb.append(sep);
						sb.append(key.toString());
					}
					sb.append(sep);
					sb.append(value.toString());
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
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			//For Append the file.
			//      OutputStream out = new FileOutputStream(f2,true);

			//For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException ex) {
			System.out.println(ex.getMessage() + " in the specified directory.");
//            org.gwaspi.gui.StartGWASpi.exit();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void deleteFile(String tempFile) {
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

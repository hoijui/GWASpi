package org.gwaspi.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

	private Utils() {
	}

	public static String readDataDirFromScript(File src) throws IOException {
		String result = null;
		FileReader fr = new FileReader(src);
		BufferedReader br = new BufferedReader(fr);

		while (br.ready()) {
			result = br.readLine();
			if (result.startsWith("data-dir")) {
				int start = result.indexOf('=') + 1;
				result = result.substring(start);
				return result;
			}
		}

		return result;
	}

	public static List<List<String>> readArgsFromScript(File src) throws IOException {
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> tmpScript = new ArrayList<String>();
		FileReader fr = new FileReader(src);
		BufferedReader br = new BufferedReader(fr);

		boolean header = true;
		while (header && br.ready()) {
			String tmpLine = br.readLine();
			if (tmpLine.startsWith("[script]")) {
				header = false;
			}
		}

		int count = 0;
		while (br.ready()) {
			String l = br.readLine();
			if (!l.equals("[/script]")) {
				int start = l.indexOf('=') + 1;
//				if (start == -1) {
//					start = 0;
//				}
				tmpScript.add(l.substring(start));
				count++;
			} else {
				result.add(tmpScript);
				tmpScript = new ArrayList<String>();
				br.readLine(); // Ignore next [script] line
			}
		}
		return result;
	}
}

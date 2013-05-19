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

package org.gwaspi.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

	private static final Logger log
			= LoggerFactory.getLogger(Utils.class);

	private Utils() {
	}

	public static String readDataDirFromScript(File src) throws IOException {

		String result = null;

		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(src);
			br = new BufferedReader(fr);

			while (br.ready()) {
				result = br.readLine();
				if (result.startsWith("data-dir")) {
					int start = result.indexOf('=') + 1;
					result = result.substring(start);
					return result;
				}
			}
		} finally {
			try {
				if (br != null) {
					br.close();
				} else if (fr != null) {
					fr.close();
				}
			} catch (Exception ex) {
				log.warn(null, ex);
			}
		}

		return result;
	}

	public static List<List<String>> readArgsFromScript(File src) throws IOException {

		List<List<String>> result = new ArrayList<List<String>>();
		List<String> tmpScript = new ArrayList<String>();

		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(src);
			br = new BufferedReader(fr);

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
//					if (start == -1) {
//						start = 0;
//					}
					tmpScript.add(l.substring(start));
					count++;
				} else {
					result.add(tmpScript);
					tmpScript = new ArrayList<String>();
					br.readLine(); // Ignore next [script] line
				}
			}
		} finally {
			try {
				if (br != null) {
					br.close();
				} else if (fr != null) {
					fr.close();
				}
			} catch (Exception ex) {
				log.warn(null, ex);
			}
		}

		return result;
	}
}

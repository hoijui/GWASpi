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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptUtils {

	private static final Logger log
			= LoggerFactory.getLogger(ScriptUtils.class);

	private ScriptUtils() {
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

	/**
	 * Parse all scripts contained in a file.
	 * @param scriptsFile plain-text file containing possibly multiple scripts
	 *   ([script] ... [/script]).
	 * @return one map of arguments per script, in the order in which they appear in the given file
	 * @throws IOException
	 */
	public static List<Map<String, String>> readScriptsFromFile(final File scriptsFile) throws IOException {

		List<Map<String, String>> scripts = new ArrayList<Map<String, String>>();

		FileReader scriptFileReader = null;
		BufferedReader scriptReader = null;
		String line = null;
		int lineNum = -1;
		try {
			scriptFileReader = new FileReader(scriptsFile);
			scriptReader = new BufferedReader(scriptFileReader);

			Map<String, String> script = null;
			while (scriptReader.ready()) {
				line = scriptReader.readLine().trim();
				lineNum++;
				if ((script == null) && line.startsWith("[script]")) {
					// We use LinkedHashMap, because it preserves the order
					// of the entries (the order of insertion of the keys).
					// Most scripts rely on this order.
					script = new LinkedHashMap<String, String>();
				} else if (script != null) {
					if (line.startsWith("[/script]")) {
						scripts.add(script);
						script = null;
					} else if (!line.startsWith("#") && !line.isEmpty()) {
						int startKey = line.indexOf('.') + 1;
						int startValue = line.indexOf('=') + 1;
						String key = line.substring(startKey, startValue - 1).trim();
						String value = line.substring(startValue).trim();
						script.put(key, value);
					}
				}
			}
			if (script != null) {
				throw new RuntimeException("Unfinnished script");
			}
		} finally {
			try {
				if (scriptReader != null) {
					scriptReader.close();
				} else if (scriptFileReader != null) {
					scriptFileReader.close();
				}
			} catch (Exception ex) {
				log.warn("Failed parsing script at line " + lineNum + ": \"" + line + "\"" , ex);
			}
		}

		return scripts;
	}
}

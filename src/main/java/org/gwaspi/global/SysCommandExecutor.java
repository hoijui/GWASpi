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

package org.gwaspi.global;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.gwaspi.constants.GlobalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SysCommandExecutor {

	private static final Logger log = LoggerFactory.getLogger(SysCommandExecutor.class);

	private SysCommandExecutor() {
	}

	public static void sysAnalyserCommandPost(String command) {
//		AnalyserTab.textArea_commandLine.setText(command);
	}

	public static void sysAnalyserCommandExecute() {
//		try {
//			String commandLine = org.gwaspi.gui.AnalyserTab.textArea_commandLine.getText();
//			AnalyserTab.textArea_cliResult.append("Command to be executed:\n"+ commandLine +"\n"+"\n");
//
//			StringBuffer result=sysCommandExecute(commandLine);
//			AnalyserTab.textArea_cliResult.append(result.toString());
//		} catch (Exception ex) {
//			log.error("Failed to execute command", ex);
//		}
	}

	public static String sysCommandExecute(String cmd) {

		StringBuilder result = new StringBuilder();

		InputStreamReader inputStreamReader = null;
		BufferedReader inputBufferReader = null;
		try {
			final Runtime runtime = Runtime.getRuntime();

			Process process = null;
			if (GlobalConstants.OSNAME.equals("Linux")) {
				process = runtime.exec(cmd);
			} else if (GlobalConstants.OSNAME.contains("Windows")) {
				process = runtime.exec("cmd /c " + cmd);
			}
//			else { // TODO implement for at least OS X
//				JOptionPane.showMessageDialog(org.gwaspi.gui.StartGUI.getFrames()[0], "Sorry, your Operating System is not supported by our software.\nSupported platforms include Windows and Linux.");
//			}

			inputStreamReader = new InputStreamReader(process.getInputStream());
			inputBufferReader = new BufferedReader(inputStreamReader);
			String line;
			while ((line = inputBufferReader.readLine()) != null) {
				result.append(line).append('\n');
			}

			final int exitVal = process.waitFor();
			result.append('\n').append(exitVal).append('\n');
		} catch (Exception ex) {
			log.error("Failed to execute command: " + cmd, ex);
		} finally {
			try {
				if (inputBufferReader != null) {
					inputBufferReader.close();
				} else if (inputStreamReader != null) {
					inputStreamReader.close();
				}
			} catch (Exception ex) {
				log.warn(null, ex);
			}
		}

		return result.toString();
	}
}

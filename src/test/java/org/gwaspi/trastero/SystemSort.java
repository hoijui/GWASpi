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

package org.gwaspi.trastero;

import java.io.File;
import java.io.IOException;
import javax.swing.JProgressBar;
import org.gwaspi.constants.GlobalConstants;
import org.gwaspi.global.Config;

public class SystemSort {

	private static int numFiles;

	private SystemSort() {
	}

	public static void processData(File dir, String format, JProgressBar progressBar) throws IOException {
		File[] filesToImport = org.gwaspi.global.Utils.listFiles(dir.toString());
		File formatFolder = org.gwaspi.global.Utils.createFolder(Config.getSingleton().getString(
				GlobalConstants.SORT_EXEC_DIR_CONFIG,
				GlobalConstants.USER_DIR_DEFAULT), format);
		String processingDir = formatFolder.getPath() + "/";
		File[] alreadySortedFiles = org.gwaspi.global.Utils.listFiles(processingDir);
		numFiles = alreadySortedFiles.length;

		for (final File fileToImport : filesToImport) {
			String cmd = getCommandLine(fileToImport.getPath(), format);
			org.gwaspi.global.SysCommandExecutor.sysCommandExecute(cmd);
			fileToImport.delete();
		}
	}

	public static String getCommandLine(String pathToFile, String format) throws IOException {
		String executable;
		String commandLine = "";
		String outputPath = Config.getSingleton().getString(
				GlobalConstants.SORT_EXEC_DIR_CONFIG,
				GlobalConstants.USER_DIR_DEFAULT)
				+ "/" + format + "/" + (numFiles - 1) + ".csv";
		numFiles++;

		if (GlobalConstants.OSNAME.equals("Linux")) {
			executable = "sort ";
			commandLine = executable + " -u " + pathToFile + " -o " + outputPath;
		} else if (GlobalConstants.OSNAME.contains("Windows")) {
			executable = "sort ";
			commandLine = executable + pathToFile + " /O " + outputPath;
		}

		return commandLine;
	}
}

package org.gwaspi.trastero;

import java.io.File;
import java.io.IOException;
import javax.swing.JProgressBar;
import org.gwaspi.constants.cGlobal;

public class SystemSort {

	private static int numFiles;

	private SystemSort() {
	}

	public static void processData(File dir, String format, JProgressBar progressBar) throws IOException {
		File[] filesToImport = org.gwaspi.global.Utils.listFiles(dir.toString());
		File formatFolder = org.gwaspi.global.Utils.createFolder(org.gwaspi.global.Config.getConfigValue(
				cGlobal.SORT_EXEC_DIR_CONFIG,
				cGlobal.USER_DIR_DEFAULT), format);
		String processingDir = formatFolder.getPath() + "/";
		File[] alreadySortedFiles = org.gwaspi.global.Utils.listFiles(processingDir);
		numFiles = alreadySortedFiles.length;

		for (int i = 0; i < filesToImport.length; i++) {
			String cmd = getCommandLine(filesToImport[i].getPath(), format);
			org.gwaspi.global.SysCommandExecutor.sysCommandExecute(cmd);
			filesToImport[i].delete();
		}

	}

	public static String getCommandLine(String pathToFile, String format) throws IOException {
		String executable;
		String commandLine = "";
		String outputPath = org.gwaspi.global.Config.getConfigValue(
				cGlobal.SORT_EXEC_DIR_CONFIG,
				cGlobal.USER_DIR_DEFAULT).toString()
				+ "/" + format + "/" + (numFiles - 1) + ".csv";
		numFiles++;

		if (cGlobal.OSNAME.equals("Linux")) {
			executable = "sort ";
			commandLine = executable + " -u " + pathToFile + " -o " + outputPath;
		} else if (cGlobal.OSNAME.contains("Windows")) {
			executable = "sort ";
			commandLine = executable + pathToFile + " /O " + outputPath;
		}

		return commandLine;
	}
}

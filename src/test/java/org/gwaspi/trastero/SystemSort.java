/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.trastero;

import java.io.File;
import java.io.IOException;
import javax.swing.JProgressBar;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SystemSort {

	private static int numFiles;

	public static void processData(File dir, String format, JProgressBar progressBar) throws IOException {
		File[] filesToImport = org.gwaspi.global.Utils.listFiles(dir.toString(), false);
		File formatFolder = org.gwaspi.global.Utils.createFolder(org.gwaspi.global.Config.getConfigValue("ESdir", org.gwaspi.constants.cGlobal.USERDIR), format);
		String processingDir = formatFolder.getPath() + "/";
		File[] alreadySortedFiles = org.gwaspi.global.Utils.listFiles(processingDir, false);
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
		String outputPath = org.gwaspi.global.Config.getConfigValue("ESdir", org.gwaspi.constants.cGlobal.USERDIR).toString() + "/" + format + "/" + (numFiles - 1) + ".csv";
		numFiles++;

		if (org.gwaspi.constants.cGlobal.OSNAME.equals("Linux")) {
			executable = "sort ";
			commandLine = executable + " -u " + pathToFile + " -o " + outputPath;
		} else if (org.gwaspi.constants.cGlobal.OSNAME.contains("Windows")) {
			executable = "sort ";
			commandLine = executable + pathToFile + " /O " + outputPath;
		}

		return commandLine;
	}
}

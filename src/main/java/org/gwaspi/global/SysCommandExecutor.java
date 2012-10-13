package org.gwaspi.global;

import org.gwaspi.constants.cGlobal;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SysCommandExecutor {

	private static final Logger log = LoggerFactory.getLogger(SysCommandExecutor.class);
	private static Process pr = null;

	private SysCommandExecutor() {
	}

	public static void sysAnalyserCommandPost(String command) {
//		AnalyserTab.textArea_commandLine.setText(command);
	}

	public static void sysAnalyserCommandExecute() {
		try {
//			String commandLine = org.gwaspi.gui.AnalyserTab.textArea_commandLine.getText();
//			AnalyserTab.textArea_cliResult.append("Command to be executed:\n"+ commandLine +"\n"+"\n");
//
//			StringBuffer result=sysCommandExecute(commandLine);
//			AnalyserTab.textArea_cliResult.append(result.toString());
		} catch (Exception ex) {
			log.error("Failed to execute command", ex);
		}
	}

	public static String sysCommandExecute(String cmd) {

		StringBuilder result = new StringBuilder();

		try {
			Runtime rt = Runtime.getRuntime();

			if (cGlobal.OSNAME.equals("Linux")) {
				pr = rt.exec(cmd);
			} else if (cGlobal.OSNAME.contains("Windows")) {
				pr = rt.exec("cmd /c " + cmd);
			}
//			else { // TODO implement for at least OS X
//				JOptionPane.showMessageDialog(org.gwaspi.gui.StartGUI.getFrames()[0], "Sorry, your Operating System is not supported by our software.\nSupported platforms include Windows and Linux.");
//			}

			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				result.append(line).append("\n");
			}

			int exitVal = pr.waitFor();
			result.append("\n").append(exitVal).append("\n");
		} catch (Exception ex) {
			log.error("Failed to execute command: " + cmd, ex);
		}

		return result.toString();
	}
}

package org.gwaspi.global;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SysCommandExecutor {

	private static Process pr = null;

	public static void sysAnalyserCommandPost(String command) {
//        org.gwaspi.gui.AnalyserTab.textArea_commandLine.setText(command);
	}

	public static void sysAnalyserCommandExecute() {
		try {
//            String commandLine = org.gwaspi.gui.AnalyserTab.textArea_commandLine.getText();
//            org.gwaspi.gui.AnalyserTab.textArea_cliResult.append("Command to be executed:\n"+ commandLine +"\n"+"\n");
//
//            StringBuffer result=sysCommandExecute(commandLine);
//            org.gwaspi.gui.AnalyserTab.textArea_cliResult.append(result.toString());
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	public static StringBuffer sysCommandExecute(String cmd) {
		StringBuffer result = new StringBuffer();
		try {
			Runtime rt = Runtime.getRuntime();

			if (org.gwaspi.constants.cGlobal.OSNAME.equals("Linux")) {
				pr = rt.exec(cmd);
			} else if (org.gwaspi.constants.cGlobal.OSNAME.contains("Windows")) {
				pr = rt.exec("cmd /c " + cmd);
			} else {
				//JOptionPane.showMessageDialog(org.gwaspi.gui.StartGUI.getFrames()[0], "Sorry, your Operating System is not supported by our software.\nSupported platforms include Windows and Linux.");
			}

			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				result.append(line + "\n");
			}

			int exitVal = pr.waitFor();
			result.append("\n" + exitVal + "\n");

		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		return result;
	}
}

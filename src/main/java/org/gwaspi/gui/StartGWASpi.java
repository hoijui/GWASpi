package org.gwaspi.gui;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.gwaspi.threadbox.SwingDeleterItemList;
import org.gwaspi.threadbox.SwingWorkerItemList;

public class StartGWASpi extends javax.swing.JFrame {

	// create a JFrame to hold everything
	public static boolean guiMode = true;
	public static boolean logToFile = false;
	public static boolean logOff = false;
	public static String logPath;
	public static JFrame mainGUIFrame = new JFrame(org.gwaspi.constants.cGlobal.APP_NAME);
	public static JTabbedPane allTabs = new JTabbedPane();
	Preferences prefs;
	public static long maxHeapSize = 0;
	public static long maxProcessMarkers = 0;
	//THIS TO WORK IN CLI MODE
	public static String config_DataDir;
	public static String config_GTdir;
	public static String config_ExportDir;
	public static String config_ReportsDir;
	public static String config_OfflineHelpDir;
	public static String config_LogDir;

	public void initGWASpi(boolean startWithGUI, File scriptFile) throws IOException, SQLException {

		//initialize configuration of moapi
		boolean isInitiated = org.gwaspi.global.Config.initPreferences(startWithGUI, scriptFile);

		if (startWithGUI) {
			if (isInitiated) {
				mainGUIFrame.setSize(1100, 800);
				mainGUIFrame.setResizable(true);

				GWASpiExplorerPanel panel0 = new GWASpiExplorerPanel();
				ProcessTab panel1 = new ProcessTab();

				allTabs.addTab(Text.App.Tab0, panel0);
				allTabs.addTab(Text.App.Tab1, panel1);

				javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
				getContentPane().setLayout(layout);
				layout.setHorizontalGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(allTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE));
				layout.setVerticalGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(allTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE));

				mainGUIFrame.getContentPane().add(allTabs);
				mainGUIFrame.setVisible(true);
			} else {
				exit();
			}
		} else {
			if (!isInitiated) {
				exit();
			} else {
				if (logToFile) {
					//LOGGING OF SYSTEM OUTPUT
					if (logPath == null) {
						logPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/cli.log";
					}
					FileOutputStream fos = new FileOutputStream(logPath);
					PrintStream ps = new PrintStream(fos);
					System.setErr(ps);
					System.setOut(ps);
				}

			}
		}

	}

	public static void main(String[] args) throws IOException, SQLException, ParseException {

		// Get current size of heap in bytes
		maxHeapSize = Math.round(Runtime.getRuntime().totalMemory() / 1048576); //heapSize in MB
		maxProcessMarkers = Math.round(maxHeapSize * 625); //1.6GB needed for 10⁶ markers (safe, 1.4-1.5 real)

		ArrayList argsAL = new ArrayList();
		for (int i = 0; i < args.length; i++) {
			argsAL.add(args[i]);
		}

		if (argsAL.contains("script")) {
			guiMode = false;
			if (argsAL.contains("log")) {
				logToFile = true;
				logPath = argsAL.get(argsAL.indexOf("log") + 1).toString();
			}

			File scriptFile = new File(argsAL.get(argsAL.indexOf("script") + 1).toString());
			if (scriptFile.exists()) {
				if (maxHeapSize > 254) {
					System.out.println(maxHeapSize + Text.App.memoryAvailable1 + "\n"
							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3);
				} else {
					System.out.println(maxHeapSize + Text.App.memoryAvailable1 + "\n"
							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3 + "\n"
							+ Text.App.memoryAvailable4);
				}

				new StartGWASpi().initGWASpi(false, scriptFile);

				//BIT THAT READS COMMAND LINES AND EXECUTES THEM
				org.gwaspi.cli.CliExecutor.execute(scriptFile);
			} else {
				System.out.println(Text.Cli.wrongScriptFilePath);
				exit();
			}

		} else {
			if (argsAL.contains("nolog")) {
				logOff = true;
			}

			mainGUIFrame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					int jobsPending = SwingWorkerItemList.getSwingWorkerPendingItemsNb() + SwingDeleterItemList.getSwingDeleterPendingItemsNb();
					if (jobsPending == 0) {
						exit();
					} else {
						int decision = org.gwaspi.gui.utils.Dialogs.showConfirmDialogue(Text.App.jobsStillPending);
						if (decision == JOptionPane.YES_OPTION) {
							exit();
						}
					}
				}
			});

			try {
				// Set cross-platform Java L&F (also called "Metal")
				//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				// Set System L&F
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (UnsupportedLookAndFeelException e) {
				// handle exception
			} catch (ClassNotFoundException e) {
				// handle exception
			} catch (InstantiationException e) {
				// handle exception
			} catch (IllegalAccessException e) {
				// handle exception
			}

			try {
				new StartGWASpi().initGWASpi(true, null);

				if (maxHeapSize > 254) {
					org.gwaspi.gui.utils.Dialogs.showInfoDialogue(maxHeapSize + Text.App.memoryAvailable1 + "\n"
							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3);
				} else {
					org.gwaspi.gui.utils.Dialogs.showInfoDialogue(maxHeapSize + Text.App.memoryAvailable1 + "\n"
							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3 + "\n"
							+ Text.App.memoryAvailable4);
				}

			} catch (RuntimeException runtimeException) {
				runtimeException.printStackTrace();
				org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.App.warnOnlyOneInstance);
				exit();
			} catch (OutOfMemoryError e) {
				System.out.println(Text.App.outOfMemoryError);
			} catch (Exception exception) {
			}

			mainGUIFrame.setExtendedState(MAXIMIZED_BOTH);
		}




	}

	public static void exit() {
		try {
			DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
			db.shutdownConnection();
		} catch (IOException ex) {
			Logger.getLogger(StartGWASpi.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.exit(0);
	}
}

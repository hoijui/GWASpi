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

package org.gwaspi.gui;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import org.gwaspi.cli.CliExecutor;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.threadbox.TaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartGWASpi extends JFrame {

	private static final Logger log = LoggerFactory.getLogger(StartGWASpi.class);

	public static final String COMMAND_LINE_SWITCH_HELP = "help";
	public static final String COMMAND_LINE_SWITCH_LOG = "log";
	public static final String COMMAND_LINE_SWITCH_NOLOG = "nolog";
	public static final String COMMAND_LINE_SWITCH_SCRIPT = "script";
	public static final String COMMAND_LINE_SWITCH_IN_MEMORY = "memory";

	// FIXME TODO convert all this to non-static, and make configuration in general more modular (for example, use preferences for everything?)
	public static boolean guiMode = true;
	private static boolean logToFile = false;
	public static boolean logOff = false;
	public static boolean inMemoryStorage = false;
	private static String logPath;
	public static final JTabbedPane allTabs = new JTabbedPane();
	public static long maxHeapSize = 0;
	public static long maxProcessMarkers = 0;

	public StartGWASpi() {
	}

	/**
	 * The name returned from this may considerably vary in length,
	 * and will be used in places like the main window title.
	 * @return application name, possibly extended by important info
	 */
	public static String constructHighlyVisibleApplicationName() {

		String highlyVisibleApplicationName = Text.App.appName;
		if (inMemoryStorage) {
			highlyVisibleApplicationName += " - " + Text.App.inMemoryNoteShort;
		}
		return highlyVisibleApplicationName;
	}

	private static void ensureColorableProgressBars() {

		// Since Java 6, the new Java default Look & Feel is Metal with the Nimbus theme.
		// The problem with this is, that it does not allwo to change the color of progress bars;
		// they are always orange.
		// Thus we use the previous default Metal theme, called Ocean.
		if (UIManager.getLookAndFeel().getClass().equals(MetalLookAndFeel.class)) {
			try {
				// If L&F = "Metal", set the theme
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
				UIManager.setLookAndFeel(new MetalLookAndFeel());
			} catch (UnsupportedLookAndFeelException ex) {
				log.warn("Unable to switch to the Ocean theme for the Metal Look & Feel. "
						+ "This means we will not have colored progress bars.", ex);
			}
		}
	}

	private static boolean hasCommandLineSwitch(final List<String> args, final String switchName) {
		return (args.contains(switchName) || args.contains("--" + switchName));
	}

	private static String fetchCommandLineSwitchArgument(final List<String> args, final String switchName) {

		final int switchLocation = locateCommandLineSwitch(args, switchName);
		if (switchLocation >= 0) {
			return args.get(switchLocation + 1);
		} else {
			return null;
		}
	}

	private static int locateCommandLineSwitch(final List<String> args, final String switchName) {

		if (args.contains(switchName)) {
			return args.indexOf(switchName);
		} else if (args.contains("--" + switchName)) {
			return args.indexOf("--" + switchName);
		} else {
			return -1;
		}
	}

	public static void printHelp(PrintStream out) {

		out.println(Text.App.appName);
		out.println(Text.App.appDescription);
		out.println(Text.App.license);
		out.println();
		out.println("command line switches:");
		out.println("\t" + COMMAND_LINE_SWITCH_HELP + "\t:\t" + "Show this info");
		out.println("\t" + COMMAND_LINE_SWITCH_LOG + " <log-file-path>" + "\t:\t" + "(GUI mode only) log to the specified file");
		out.println("\t" + COMMAND_LINE_SWITCH_NOLOG + "\t:\t" + "(script mode only) do not log to any file");
		out.println("\t" + COMMAND_LINE_SWITCH_SCRIPT + " <script-file-path>" + "\t:\t" + "do not show the GUI, but run the given script instead");
	}

	public void start(List<String> args) throws IOException, SQLException, ParseException, UnsupportedLookAndFeelException {

		if (hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_HELP)) {
			printHelp(System.out);
			return;
		}

		// Get current size of heap in bytes
		maxHeapSize = Math.round((double) Runtime.getRuntime().totalMemory() / 1048576); // heapSize in MB
		maxProcessMarkers = Math.round((double) maxHeapSize * 625); // 1.6GB needed for 10^6 markers (safe, 1.4 - 1.5 real)

		inMemoryStorage = hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_IN_MEMORY);

		if (hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_SCRIPT)) {
			guiMode = false;
			if (hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_LOG)) {
				logToFile = true;
				logPath = fetchCommandLineSwitchArgument(args, COMMAND_LINE_SWITCH_LOG);
			}

			File scriptFile = new File(fetchCommandLineSwitchArgument(args, COMMAND_LINE_SWITCH_SCRIPT));
			if (scriptFile.exists()) {
				if (maxHeapSize > 254) {
					log.info(maxHeapSize + Text.App.memoryAvailable1 + "\n"
							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3);
				} else {
					log.info(maxHeapSize + Text.App.memoryAvailable1 + "\n"
							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3 + "\n"
							+ Text.App.memoryAvailable4);
				}

				boolean initialized = initGWASpi(false, scriptFile);

				if (initialized) {
					CliExecutor cliExecutor = new CliExecutor(scriptFile);
					boolean success = cliExecutor.execute();
					if (!success) {
						throw new IOException("Failed to execute script '" + scriptFile + "'");
					}
				}
			} else {
				log.error(Text.Cli.wrongScriptFilePath, scriptFile);
			}
		} else {
			if (hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_NOLOG)) {
				logOff = true;
			}

			ensureColorableProgressBars();

			try {
				// Set cross-platform Java L&F (also called "Metal")
				//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				// Set System L&F
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (UnsupportedLookAndFeelException ex) {
				log.warn(null, ex);
			} catch (ClassNotFoundException ex) {
				log.warn(null, ex);
			} catch (InstantiationException ex) {
				log.warn(null, ex);
			} catch (IllegalAccessException ex) {
				log.warn(null, ex);
			}

			try {
				initGWASpi(true, null);

				if (maxHeapSize > 254) {
					Dialogs.showInfoDialogue(maxHeapSize + Text.App.memoryAvailable1 + "\n"
							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3);
				} else {
					Dialogs.showInfoDialogue(maxHeapSize + Text.App.memoryAvailable1 + "\n"
							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3 + "\n"
							+ Text.App.memoryAvailable4);
				}
			} catch (RuntimeException ex) {
				log.error(Text.App.warnOnlyOneInstance, ex);
				Dialogs.showWarningDialogue(Text.App.warnOnlyOneInstance);
				return;
			} catch (OutOfMemoryError ex) {
				log.error(Text.App.outOfMemoryError, ex);
			} catch (Exception ex) {
				log.error(null, ex);
			}
		}
	}

	private boolean initGWASpi(boolean startWithGUI, File scriptFile) throws IOException, SQLException {

		final JFrame mainGUIFrame;
		if (startWithGUI) {
			mainGUIFrame = new JFrame(constructHighlyVisibleApplicationName());
			mainGUIFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent we) {
					final boolean jobsPending = TaskQueue.getInstance().isActive();
					if (jobsPending) {
						int decision = Dialogs.showConfirmDialogue(Text.App.jobsStillPending);
						if (decision == JOptionPane.YES_OPTION) {
							we.getWindow().setVisible(false);
						}
					} else {
						we.getWindow().setVisible(false);
					}
				}
			});
			mainGUIFrame.setExtendedState(MAXIMIZED_BOTH);
		} else {
			mainGUIFrame = null;
		}

		// initialize configuration of moapi
		boolean isInitiated = Config.initPreferences(startWithGUI, scriptFile, mainGUIFrame);

		if (isInitiated) {
			if (startWithGUI) {
				mainGUIFrame.setSize(1100, 800);
				mainGUIFrame.setResizable(true);

				GWASpiExplorerPanel panel0 = GWASpiExplorerPanel.getSingleton();
				ProcessTab panel1 = ProcessTab.getSingleton();

				allTabs.addTab(Text.App.Tab0, panel0);
				allTabs.addTab(Text.App.Tab1, panel1);

				GroupLayout layout = new GroupLayout(getContentPane());
				getContentPane().setLayout(layout);
				layout.setHorizontalGroup(
						layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(allTabs, GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE));
				layout.setVerticalGroup(
						layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(allTabs, GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE));

				mainGUIFrame.getContentPane().add(allTabs);
				mainGUIFrame.setVisible(true);
				mainGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			} else {
				if (logToFile) {
					// LOGGING OF SYSTEM OUTPUT
					if (logPath == null) {
						logPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/cli.log";
					}
					FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
					fileAppender.setFile(logPath);
					fileAppender.start();
					LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
					lc.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(fileAppender);
				}
			}
		}

		return isInitiated;
	}

	public static void exit() {
		System.exit(0);
	}

	public static void main(String[] args) throws IOException, SQLException, ParseException, UnsupportedLookAndFeelException {

		StartGWASpi startGWASpi = new StartGWASpi();
		startGWASpi.start(Arrays.asList(args));
	}
}

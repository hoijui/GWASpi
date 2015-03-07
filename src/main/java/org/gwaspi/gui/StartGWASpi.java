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
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.gwaspi.cli.CliExecutor;
import org.gwaspi.cli.ScriptExecutionException;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.gui.utils.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartGWASpi {

	private static final Logger log = LoggerFactory.getLogger(StartGWASpi.class);

	public static final String COMMAND_LINE_SWITCH_HELP = "help";
	public static final String COMMAND_LINE_SWITCH_VERSION = "version";
	public static final String COMMAND_LINE_SWITCH_LICENSE = "license";
	public static final String COMMAND_LINE_SWITCH_LOG = "log";
	public static final String COMMAND_LINE_SWITCH_NOLOG = "nolog";
	public static final String COMMAND_LINE_SWITCH_SCRIPT = "script";
	public static final String COMMAND_LINE_SWITCH_IN_MEMORY = "memory";
	public static final String COMMAND_LINE_SWITCH_ARRAY_GENOTYPE_LISTS = "array-genotypes-lists";

	// FIXME TODO convert all this to non-static, and make configuration in general more modular (for example, use preferences for everything?)
	private static MainFrame mainGUIFrame = null;
	private static boolean logToFile = false;
	private static String logPath;

	public StartGWASpi() {
	}

	public static MainFrame getMainFrame() { // HACK ugly singleton
		return mainGUIFrame;
	}

	/**
	 * The name returned from this may considerably vary in length,
	 * and will be used in places like the main window title.
	 * @return application name, possibly extended by important info
	 */
	public static String constructHighlyVisibleApplicationName() {

		String highlyVisibleApplicationName = Text.App.appName;
		final boolean inMemoryStorage =
				Config.getSingleton().getBoolean(Config.PROPERTY_STORAGE_IN_MEMORY, false);
		if (inMemoryStorage) {
			highlyVisibleApplicationName += " - " + Text.App.inMemoryNoteShort;
		}
		return highlyVisibleApplicationName;
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
		out.println();
		out.println("command line switches:");
		out.println("\t--" + COMMAND_LINE_SWITCH_HELP + "\t:\t" + "Show this info and exit");
		out.println("\t--" + COMMAND_LINE_SWITCH_VERSION + "\t:\t" + "Show the GWASpi version and exit");
		out.println("\t--" + COMMAND_LINE_SWITCH_LICENSE + "\t:\t" + "Show the GWASpi software license and exit");
		out.println("\t--" + COMMAND_LINE_SWITCH_LOG + " <log-file-path>" + "\t:\t" + "(GUI mode only) log to the specified file");
		out.println("\t--" + COMMAND_LINE_SWITCH_NOLOG + "\t:\t" + "(script mode only) do not log to any file");
		out.println("\t--" + COMMAND_LINE_SWITCH_SCRIPT + " <script-file-path>" + "\t:\t" + "do not show the GUI, but run the given script instead");
	}

	public static void printVersion(final PrintStream out) {

		final Properties manifestProperties = Utils.getManifestProperties();
		final String version = manifestProperties.getProperty(Utils.MANIFEST_PROPERTY_VERSION);
		final String build = manifestProperties.getProperty(Utils.MANIFEST_PROPERTY_BUILD);
		final String buildTimestamp = manifestProperties.getProperty(Utils.MANIFEST_PROPERTY_BUILD_TIMESTAMP);
		final String jdkVersion = manifestProperties.getProperty(Utils.MANIFEST_PROPERTY_JDK_VERSION);

		out.println(Text.App.appName + " version: " + version);
		out.println("Build ID: " + build);
		out.println("Build timestamp: " + buildTimestamp);
		out.println("Build JDK: " + jdkVersion);
	}

	public static void printLicense(final PrintStream out) {

		out.println(Text.App.appName + " license: " + Text.App.license);

		out.println();
		final String licenseFileContent = Utils.readLicense();
		out.println("Full license text:");
		out.println("################################################################################");
		out.println(licenseFileContent);
		out.println("################################################################################");
	}

	public void start(List<String> args) throws IOException, SQLException, ParseException {

		if (hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_HELP)) {
			printHelp(System.out);
			return;
		} else if (hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_VERSION)) {
			printVersion(System.out);
			return;
		} else if (hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_LICENSE)) {
			printLicense(System.out);
			return;
		}

		final boolean guiMode = !hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_SCRIPT);
		if (Config.getSingleton() == null) { // HACK we do it like this, because some unit test scripts might already have initialzied the config
			Config.createSingleton(guiMode);
		}

		// Get current size of heap in bytes
		final int maxHeapSize = (int) Math.round((double) Runtime.getRuntime().totalMemory() / 1048576); // heapSize in MB
		final int maxProcessMarkers = (int) Math.round((double) maxHeapSize * 625); // 1.6GB needed for 10^6 markers (safe, 1.4 - 1.5 real)
		final boolean inMemoryStorage = hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_IN_MEMORY);
		final boolean arrayGenotypes = hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_ARRAY_GENOTYPE_LISTS);

		Config.getSingleton().putInteger(Config.PROPERTY_MAX_HEAP_MB, maxHeapSize);
		Config.getSingleton().putInteger(Config.PROPERTY_MAX_PROCESS_MARKERS, maxProcessMarkers);
		Config.getSingleton().putBoolean(Config.PROPERTY_STORAGE_IN_MEMORY, inMemoryStorage);
		Config.getSingleton().putBoolean(Config.PROPERTY_STORAGE_COMPACT_GT_LISTS, !arrayGenotypes);

		if (!guiMode) {
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
					try {
						cliExecutor.execute();
					} catch (final ScriptExecutionException ex) {
						throw new IOException("Failed to execute script(s) from file '" + scriptFile + "'", ex);
					}
				}
			} else {
				log.error(Text.Cli.wrongScriptFilePath, scriptFile);
			}
		} else {
			if (hasCommandLineSwitch(args, COMMAND_LINE_SWITCH_NOLOG)) {
				Config.getSingleton().putBoolean(Config.PROPERTY_LOG_OFF, true);
			}

			try {
				initGWASpi(true, null);
			} catch (RuntimeException ex) {
				log.error(Text.App.warnOnlyOneInstance, ex);
				Dialogs.showWarningDialogue(Text.App.warnOnlyOneInstance);
			} catch (OutOfMemoryError ex) {
				log.error(Text.App.outOfMemoryError, ex);
			} catch (Exception ex) {
				log.error(null, ex);
			}
		}
	}

	private boolean initGWASpi(boolean startWithGUI, File scriptFile) throws IOException, SQLException {

		if (startWithGUI) {
			mainGUIFrame = new MainFrame();
		}

		// initialize configuration of moapi
		boolean isInitiated = Config.getSingleton().initPreferences(startWithGUI, scriptFile, mainGUIFrame);

		if (isInitiated) {
			if (startWithGUI) {
				mainGUIFrame.init();
			} else {
				if (logToFile) {
					// LOGGING OF SYSTEM OUTPUT
					if (logPath == null) {
						logPath = Config.getSingleton().getString(Config.PROPERTY_REPORTS_DIR, "") + "/cli.log";
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

	public static void main(String[] args) throws IOException, SQLException, ParseException {

		StartGWASpi startGWASpi = new StartGWASpi();
		startGWASpi.start(Arrays.asList(args));
	}
}

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

import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.gwaspi.cli.ScriptUtils;
import org.gwaspi.constants.GlobalConstants;
import org.gwaspi.dao.StudyService;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.model.StudyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Config {

	private static final Logger log = LoggerFactory.getLogger(Config.class);

	public static final String PROPERTY_LAST_OPENED_DIR = "LAST_OPENED_DIR"; // String
	public static final String PROPERTY_LAST_SELECTED_NODE = "LAST_SELECTED_NODE"; // String
	public static final String PROPERTY_DATA_DIR = "DataDir"; // String
	public static final String PROPERTY_GENOTYPES_DIR = "GTdir"; // String
	public static final String PROPERTY_EXPORT_DIR = "ExportDir"; // String
	public static final String PROPERTY_REPORTS_DIR = "ReportsDir"; // String
	public static final String PROPERTY_LOG_DIR = "LogDir"; // String
	public static final String PROPERTY_CURRENT_GWASPIDB_VERSION = "CURRENT_GWASPIDB_VERSION"; // String
	public static final String PROPERTY_GUI_MODE = "mode.gui"; // Boolean
	public static final String PROPERTY_STORAGE_IN_MEMORY = "performance.storage.inMemory"; // Boolean
	public static final String PROPERTY_STORAGE_COMPACT_GT_LISTS = "performance.storage.compactGTLists"; // Boolean
	public static final String PROPERTY_LOG_OFF = "performance.logOff"; // Boolean
	public static final String PROPERTY_MAX_HEAP_MB = "performance.heapMB.max"; // Integer
	public static final String PROPERTY_MAX_PROCESS_MARKERS = "performance.markers.max"; // INTEGER

	private final boolean guiMode;
	/** System wide preferences. */
	private final Preferences prefs;
	/**
	 * Per software (runtime-)instance preferences.
	 * We use per-thread prefs, initialized with the values from the main thread
	 * (so they use at least the same data-dir, for example).
	 */
	private ThreadLocal<Map<String, Object>> instancePrefs;

	private static Config SINGLETON = null;

	private static class PrefsThreadLocal extends ThreadLocal<Map<String, Object>> {

		private Map<String, Object> mainPrefs;

		PrefsThreadLocal() {

			this.mainPrefs = null;
		}

		@Override
		protected Map<String, Object> initialValue() {

			Map<String, Object> prefs;

			if (mainPrefs == null) {
				// this will be calle for the main thread,
				// initilaizing the prefs
				prefs = new HashMap<String, Object>();
				mainPrefs = prefs;
			} else {
				prefs = new HashMap<String, Object>(mainPrefs);
			}

			return prefs;
		}
	}

	private Config(final boolean guiMode) {

		this.guiMode = guiMode;
		if (guiMode) {
			this.prefs = Preferences.userNodeForPackage(Config.class);
			this.instancePrefs = null;
		} else {
			this.prefs = null;
			this.instancePrefs = new PrefsThreadLocal();
		}
	}

	public static void createSingleton(final boolean guiMode) { // HACK ugly singleton; we should have one instance per application instance

		if (SINGLETON != null) {
			throw new IllegalStateException(Config.class.getSimpleName() + " singleton can be created only once");
		}
		SINGLETON = new Config(guiMode);
		SINGLETON.putBoolean(PROPERTY_GUI_MODE, guiMode);
	}

	public static void destroySingleton() {
		SINGLETON = null;
	}

	public static Config getSingleton() {
		return SINGLETON;
	}

	private StudyService getStudyService() {
		return StudyList.getStudyService();
	}

	private void put(final String key, final Object value) {

		if (guiMode) {
			// GUI PREFS
//			prefs.put(key, value.toString()); // the toString() will not work for most objects!
			throw new IllegalStateException("GUI prefs can not store Object's; developper failed!");
		} else {
			// CLI & THREAD PREFS
			instancePrefs.get().put(key, value);
		}
	}

	public void putString(final String key, final String value) {

		if (guiMode) {
			prefs.put(key, value);
		} else {
			put(key, value);
		}
	}

	public void putColor(final String key, final Color value) {
		putString(key, colorToString(value));
	}

	public void putBoolean(final String key, final boolean value) {

		if (guiMode) {
			prefs.putBoolean(key, value);
		} else {
			put(key, value);
		}
	}

	public void putInteger(final String key, final int value) {

		if (guiMode) {
			prefs.putInt(key, value);
		} else {
			put(key, value);
		}
	}

	public void putDouble(final String key, final double value) {

		if (guiMode) {
			prefs.putDouble(key, value);
		} else {
			put(key, value);
		}
	}

	public void clearNonPersistent() {

		if (!guiMode) {
			instancePrefs = new PrefsThreadLocal();
		}
	}

	private Object get(final String key, final Object defaultValue) {

		final Object value;
		if (guiMode) {
			// GUI MODE
//			value = prefs.get(key, defaultValue.toString()); // the toString() will not work for most objects!
			throw new IllegalStateException("GUI prefs can not store Object's; developper failed!");
		} else {
			final Map<String, Object> myPrefs = instancePrefs.get();
			if (myPrefs.containsKey(key)) {
				value = myPrefs.get(key);
			} else {
				value = defaultValue;
				myPrefs.put(key, value);
			}
		}

		return value;
	}

	public String getString(final String key, final String defaultValue) {

		final String value;
		if (guiMode) {
			value = prefs.get(key, defaultValue);
		} else {
			value = (String) get(key, defaultValue);
		}

		return value;
	}

	public Color getColor(final String key, final Color defaultValue) {
		return stringToColor(getString(key, colorToString(defaultValue)));
	}

	public Boolean getBoolean(final String key, final boolean defaultValue) {

		final Boolean value;
		if (guiMode) {
			value = prefs.getBoolean(key, defaultValue);
		} else {
			value = (Boolean) get(key, defaultValue);
		}

		return value;
	}

	public Integer getInteger(final String key, final int defaultValue) {

		final Integer value;
		if (guiMode) {
			value = prefs.getInt(key, defaultValue);
		} else {
			value = (Integer) get(key, defaultValue);
		}

		return value;
	}

	public Double getDouble(final String key, final double defaultValue) {

		final Double value;
		if (guiMode) {
			value = prefs.getDouble(key, defaultValue);
		} else {
			value = (Double) get(key, defaultValue);
		}

		return value;
	}

	public static String colorToString(Color value) {
		return value.getRed() + "," + value.getGreen() + "," + value.getBlue();
	}

	public static Color stringToColor(String value) {

		String[] split = value.split(",");
		float[] hsbVals = Color.RGBtoHSB(
				Integer.parseInt(split[0]),
				Integer.parseInt(split[1]),
				Integer.parseInt(split[2]),
				null);
		return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
	}

	public boolean initPreferences(boolean startWithGUI, File scriptFile, final Component dialogParent) {

		boolean isInitiated = false;
		try {
			//clearConfigFile();
			File dirToData = new File(getString(PROPERTY_DATA_DIR, ""));

			// CHECK FOR RECENT GWASPI VERSION
			try {
				checkUpdates();
			} catch (final ParseException ex) {
				throw new IOException(ex);
			}

			if (startWithGUI) { // GUI MODE
				if (dirToData.getPath().isEmpty()) {
					JOptionPane.showMessageDialog(dialogParent, Text.App.initText);
					File dataDir = Dialogs.selectDirectoryDialog(
							PROPERTY_DATA_DIR,
							"Choose your " + Text.App.appName + " data directory",
							dialogParent);

					if (dataDir != null) {
						try {
							createDataStructure(dataDir, true);
							JOptionPane.showMessageDialog(dialogParent, "Databases and working folders initialized successfully!");
							isInitiated = true;
						} catch (final IOException ex) {
							JOptionPane.showMessageDialog(dialogParent, Text.App.warnUnableToInitForFirstTime);
							log.error(Text.App.warnUnableToInitForFirstTime, ex);
						} catch (final HeadlessException ex) {
							throw new IllegalStateException(ex);
						}
					}
				} else {
					File derbyCenter = new File(dirToData.getPath() + "/datacenter");
					if (!derbyCenter.exists()) {
						int recreateDataFolder = Dialogs.showOptionDialogue("Data folder unreachable", "The data folder (\"" + dirToData.getAbsolutePath() + "\") is unreachable (deleted?).\nShould GWASpi recreate it or do you want to provide a new path?", "Recreate", "New Path", "Cancel");
						if (recreateDataFolder == JOptionPane.OK_OPTION) {
							createDataStructure(dirToData, true);
							JOptionPane.showMessageDialog(dialogParent, "Databases and working folders initialized successfully!");
						} else if (recreateDataFolder == JOptionPane.NO_OPTION) {
							dirToData = Dialogs.selectDirectoryDialog(
									dirToData,
									"Choose your " + Text.App.appName + " data directory",
									dialogParent);
							createDataStructure(dirToData, true);
							JOptionPane.showMessageDialog(dialogParent, "Databases and working folders initialized successfully!");
						} else if (recreateDataFolder == JOptionPane.CANCEL_OPTION) {
							throw new RuntimeException("The data folder (\"" + dirToData.getAbsolutePath() + "\") is unreachable, and the user chose not to create it");
						}
					}

					updateConfigDataDirs(dirToData);
					isInitiated = true;
				}
			} else { // CLI & THREAD MODE
				if (dirToData.getPath().isEmpty()) {
					if (scriptFile != null) {
						// Use path from script file
						// 1st line contains data path
						File dataDir = new File(ScriptUtils.readDataDirFromScript(scriptFile));
						log.info("Using database path: {}", dataDir.getAbsolutePath());

						try {
							// create the data-structure, if it does not yet exist;
							// otherwise just set the config values
							createDataStructure(dataDir, false);
							isInitiated = true;
						} catch (final IOException ex) {
							log.error(Text.App.warnUnableToInitForFirstTime, ex);
						}
						if (isInitiated) {
							log.info("Databases and working folders initialized successfully!");
						}
					} else {
						throw new IllegalStateException("Unable to determine a data directory path");
					}
				} else {
					if (getString(PROPERTY_GENOTYPES_DIR, null) == null) {
						createDataStructure(dirToData, true);
					}
					isInitiated = true;
				}
			}
		} catch (final IOException ex) {
			isInitiated = false;
			log.error("Failed initializing the configuration", ex);
		}

		return isInitiated;
	}

	private static Document getLocalVersionDom() throws IOException {

		final URL localVersionPath = Config.class.getResource(GlobalConstants.LOCAL_VERSION_XML);
		final Document localDom;
		try {
			localDom = XMLParser.parseXmlFile(localVersionPath.toURI().toString());
		} catch (final URISyntaxException ex) {
			throw new IOException(ex);
		}

		return localDom;
	}

	private static Element getLocalDomElementZero() throws IOException {

		Element elementZero = null;

		final Document localDom = getLocalVersionDom();
		if (localDom != null) { // Found local version info
			final List<Element> localElements = XMLParser.parseDocument(localDom, Text.App.appName);
			elementZero = localElements.get(0);
		}

		return elementZero;
	}

	/**
	 * HACK rather get it from the pom.xml
	 * @return
	 * @throws IOException
	 */
	private static String getLocalVersionFromDom(final Element elementZero) throws IOException {

		String localVersion = null;

		if (elementZero != null) { // Found local version info
			localVersion = XMLParser.getTextValue(elementZero, Text.App.appName + "_DB_Version");
		}

		return localVersion;
	}

	private static String getLocalVersionFromDom() throws IOException {
		return getLocalVersionFromDom(getLocalDomElementZero());
	}

	private void createDataStructure(final File dataDir, final boolean createInitialStudy) throws IOException {

		final File dataDirCanonical = dataDir.getCanonicalFile();
		final boolean dbDirExisted = initStructureConfig(dataDirCanonical, true);
		putString(PROPERTY_CURRENT_GWASPIDB_VERSION, getLocalVersionFromDom());

		if (!dbDirExisted && createInitialStudy) {
			// HACK We should not have to add a default study, but currently have to (at least for the unit-tests)
			StudyKey newStudy = getStudyService().insertStudy(new Study("Study 1", ""));
			// We do not have to add it to the GUI,
			// as the GUI is not yet initialized,
			// and it will be read from the DB and added there later on
//			GWASpiExplorerNodes.insertStudyNode(newStudy);
		}
	}

	private File copySubDirIfExists(final File dirSrc, final File dirDst, final String subDirName) throws IOException {

		final File srcFile = new File(dirSrc, subDirName);
		final File dstFile = new File(dirDst, subDirName);
		if (srcFile.exists()) {
			org.gwaspi.global.Utils.copyFileRecursive(srcFile, dstFile);
		}

		return dstFile;
	}

	public void copyDataDir(final File dataDirOld, final File dataDirNew) throws IOException {

		final File newDbDir = copySubDirIfExists(dataDirOld, dataDirNew, "datacenter");
		Config.setDBSystemDir(newDbDir.getPath());

		copySubDirIfExists(dataDirOld, dataDirNew, "genotypes");
		copySubDirIfExists(dataDirOld, dataDirNew, "export");
		copySubDirIfExists(dataDirOld, dataDirNew, "reports");
	}

	public void moveDataDir(final File dataDirOld, final File dataDirNew) throws IOException {

		copyDataDir(dataDirOld, dataDirNew);
		initStructureConfig(dataDirNew, false);
	}

	private boolean initStructureConfig(final File dataDir, final boolean createFsStructure) throws IOException {

		final boolean dbDirExisted;

		final File genotypesDir = new File(dataDir, "genotypes");
		final File helpDir = new File(dataDir, "help");
		final File exportDir = new File(dataDir, "export");
		final File reportsDir = new File(dataDir, "reports");
		final File reportsLogDir = new File(reportsDir, "log");
		final File dbDir = new File(reportsDir, "datacenter");

		dbDirExisted = dbDir.exists();

		if (createFsStructure) {
			Utils.createFolder(dataDir);
			Utils.createFolder(genotypesDir);
			Utils.createFolder(helpDir);
			Utils.createFolder(exportDir);
			Utils.createFolder(reportsDir);
			Utils.createFolder(reportsLogDir);
		}

		putString(PROPERTY_GENOTYPES_DIR, genotypesDir.getPath());
		putString(PROPERTY_EXPORT_DIR, exportDir.getPath());
		putString(PROPERTY_REPORTS_DIR, reportsDir.getPath());
		putString(PROPERTY_LOG_DIR, reportsLogDir.getPath());
		setDBSystemDir(dbDir.getPath());

		return dbDirExisted;
	}

	public void updateConfigDataDirs(final File dataDir) throws IOException {

		initStructureConfig(dataDir, false);
		putString(PROPERTY_CURRENT_GWASPIDB_VERSION, getLocalVersionFromDom());
	}

	public void checkUpdates() throws IOException, ParseException {

		if (Utils.isInternetReachable()) {
			final Element localElementZero = getLocalDomElementZero();

			if (localElementZero != null) { // Found local version info
				System.setProperty("java.net.useSystemProxies", "true");

				final String localVersionFromDom = getLocalVersionFromDom(localElementZero);
				putString(PROPERTY_CURRENT_GWASPIDB_VERSION, localVersionFromDom);

				URL remoteVersionPath = new URL(GlobalConstants.REMOTE_VERSION_XML);
				Document remoteDom = null;
				try {
					remoteDom = XMLParser.parseXmlFile(remoteVersionPath.toURI().toString());
				} catch (Exception ex) {
					// NOTE actually, UnknownHostException will be thrown here,
					//   if we fail to connect to www.gwaspi.org,
					//   but java claims it can not be thrown :/
					log.warn("Failed to parse version info file", ex);
				}

				if (remoteDom != null) { // Found remote version info
					// Retrieve data from XML files

					Date localUpdateDate = XMLParser.getDateValue(localElementZero, "Date");
					String localVersionNumber = XMLParser.getTextValue(localElementZero, "Number");

					List<Element> remoteElements = XMLParser.parseDocument(remoteDom, Text.App.appName);
					Date remoteUpdateDate = XMLParser.getDateValue(remoteElements.get(0), "Date");
					String remoteVersionNumber = XMLParser.getTextValue(remoteElements.get(0), "Number");
					String remoteCompatibilityNumber = XMLParser.getTextValue(remoteElements.get(0), "Compatibility");

					// MAKE VERSION CHECKS
					// Check whether remote version is still compatible with local version
					final boolean remoteCompatibleWithLocalVersion =
							(remoteCompatibilityNumber.compareTo(localVersionNumber) <= 0);
					final String versionCompatibility = remoteCompatibleWithLocalVersion
							? Text.App.newVersionIsCompatible : Text.App.newVersionIsUnCompatible;
					final String tagName = remoteCompatibleWithLocalVersion
							? "ActionCompatible" : "ActionUnCompatible";

					final StringBuilder message = new StringBuilder(Text.App.newVersionAvailable);
					message
							.append("\nLocal Version: ").append(localVersionNumber)
							.append("\nNewest Version: ").append(remoteVersionNumber)
							.append("\nUpdate Type: ").append(XMLParser.getTextValue(remoteElements.get(0), "Type"))
							.append('\n').append(versionCompatibility)
							.append('\n').append(XMLParser.getTextValue(remoteElements.get(0), tagName))
							.append("\nChangelog: ").append(XMLParser.getTextValue(remoteElements.get(0), "Description"));

					if (localUpdateDate.compareTo(remoteUpdateDate) < 0) { //Remote version is more recent
						if (guiMode) {
							Dialogs.showWarningDialogue(message.toString());
						} else {
							log.error(message.toString());
						}
					}
				}
			}
		}
	}

	public static void setDBSystemDir(String dataCenter) {
		// decide on the db system directory
		System.setProperty("derby.system.home", dataCenter);
	}
}

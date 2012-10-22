package org.gwaspi.global;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import org.gwaspi.constants.cGlobal;
import org.gwaspi.database.DerbyDBReshaper;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.StudyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Config {

	private static final Logger log = LoggerFactory.getLogger(Config.class);

	public static final String PROPERTY_LAST_OPENED_DIR = "LAST_OPENED_DIR";
	public static final String PROPERTY_LAST_SELECTED_NODE = "LAST_SELECTED_NODE";
	public static final String PROPERTY_DATA_DIR = "DataDir";
	public static final String PROPERTY_GENOTYPES_DIR = "GTdir";
	public static final String PROPERTY_EXPORT_DIR = "ExportDir";
	public static final String PROPERTY_REPORTS_DIR = "ReportsDir";
	public static final String PROPERTY_LOG_DIR = "LogDir";
	public static final String PROPERTY_CURRENT_GWASPIDB_VERSION = "CURRENT_GWASPIDB_VERSION";

	private static Properties properties = new Properties();
	private static JFileChooser fc;
	private static Preferences prefs = Preferences.userNodeForPackage(Config.class.getClass());
	private static int JVMbits = Integer.parseInt(System.getProperty("sun.arch.data.model", "32"));
	private static boolean startWithGUI = true;

	private Config() {
	}

	public static void setConfigValue(String key, Object value) throws IOException {
		// GUI PREFS
		prefs.put(key, value.toString());

		// CLI & THREAD PREFS
		if (key.equals(PROPERTY_DATA_DIR)) {
			StartGWASpi.config_DataDir = (String) value;
		}
		if (key.equals(PROPERTY_GENOTYPES_DIR)) {
			StartGWASpi.config_GTdir = (String) value;
		}
		if (key.equals(PROPERTY_EXPORT_DIR)) {
			StartGWASpi.config_ExportDir = (String) value;
		}
		if (key.equals(PROPERTY_REPORTS_DIR)) {
			StartGWASpi.config_ReportsDir = (String) value;
		}
		if (key.equals(PROPERTY_LOG_DIR)) {
			StartGWASpi.config_LogDir = (String) value;
		}
	}

	public static String getConfigValue(String key, String defaultV) throws IOException {
		String prop = "";
		if (StartGWASpi.guiMode) {
			// GUI MODE
			prop = prefs.get(key, defaultV);
		} else {
			// CLI MODE
			if (key.equals(PROPERTY_DATA_DIR)) {
				if (StartGWASpi.config_DataDir != null) {
					prop = StartGWASpi.config_DataDir;
				} else {
					prop = defaultV;
				}
			}
			if (key.equals(PROPERTY_GENOTYPES_DIR)) {
				if (StartGWASpi.config_GTdir != null) {
					prop = StartGWASpi.config_GTdir;
				} else {
					prop = defaultV;
				}
			}
			if (key.equals(PROPERTY_EXPORT_DIR)) {
				if (StartGWASpi.config_ExportDir != null) {
					prop = StartGWASpi.config_ExportDir;
				} else {
					prop = defaultV;
				}
			}
			if (key.equals(PROPERTY_REPORTS_DIR)) {
				if (StartGWASpi.config_ReportsDir != null) {
					prop = StartGWASpi.config_ReportsDir;
				} else {
					prop = defaultV;
				}
			}
			if (key.equals("CHART_MANHATTAN_PLOT_THRESHOLD")) {
				prop = defaultV;
			}
			if (key.equals("CHART_MANHATTAN_PLOT_BCKG")) {
				prop = defaultV;
			}
			if (key.equals("CHART_MANHATTAN_PLOT_BCKG_ALT")) {
				prop = defaultV;
			}
			if (key.equals("CHART_MANHATTAN_PLOT_DOT")) {
				prop = defaultV;
			}
			if (key.equals("CHART_QQ_PLOT_BCKG")) {
				prop = defaultV;
			}
			if (key.equals("CHART_QQ_PLOT_DOT")) {
				prop = defaultV;
			}
			if (key.equals("CHART_QQ_PLOT_2SIGMA")) {
				prop = defaultV;
			}
			if (key.equals("CHART_SAMPLEQA_HETZYG_THRESHOLD")) {
				prop = defaultV;
			}
			if (key.equals("CHART_SAMPLEQA_MISSING_THRESHOLD")) {
				prop = defaultV;
			}
			if (key.equals(PROPERTY_CURRENT_GWASPIDB_VERSION)) {
				prop = defaultV;
			}
			if (key.equals(PROPERTY_LOG_DIR)) {
				if (StartGWASpi.config_LogDir != null) {
					prop = StartGWASpi.config_LogDir;
				} else {
					prop = defaultV;
				}
			}
		}

		return prop;
	}

//	public static int getConfigValue(String key, int defaultV) throws IOException {
//		int prop = prefs.getInt(key, defaultV);
//		return prop;
//	}

	public static void clearConfigFile() throws IOException, BackingStoreException {
		if (startWithGUI) {
			// GUI MODE
			prefs.clear();
		} else {
			// CLI MODE
			StartGWASpi.config_DataDir = null;
			StartGWASpi.config_GTdir = null;
			StartGWASpi.config_ExportDir = null;
			StartGWASpi.config_ReportsDir = null;
			StartGWASpi.config_OfflineHelpDir = null;
			StartGWASpi.config_LogDir = null;
		}
	}

	public static boolean initPreferences(boolean _startWithGUI, File scriptFile) {
		boolean isInitiated = false;
		//startWithGUI = _startWithGUI;
		try {
			//clearConfigFile();
			File dirToData = new File(getConfigValue(PROPERTY_DATA_DIR, ""));

			// CHECK FOR RECENT GWASPI VERSION
			checkUpdates();

			if (_startWithGUI) { // GUI MODE
				if (dirToData.getPath().equals("")) {
					JOptionPane.showMessageDialog(StartGWASpi.mainGUIFrame, Text.App.initText);
					File dataDir = Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION);

					if (dataDir != null) {
						try {
							if (dataDir != null) {
								createDataStructure(dataDir);
								JOptionPane.showMessageDialog(StartGWASpi.mainGUIFrame, "Databases and working folders initialized successfully!");
							}
							isInitiated = true;
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(StartGWASpi.mainGUIFrame, Text.App.warnUnableToInitForFirstTime);
							log.error(Text.App.warnUnableToInitForFirstTime, ex);
						}
					}
				} else {
					File derbyCenter = new File(dirToData.getPath() + "/datacenter");
					if (!derbyCenter.exists()) {
						int recreateDataFolder = Dialogs.showOptionDialogue("Data folder unreachable", "The data folder is unreachable (deleted?).\nShould GWASpi recreate it or do you want to provide a new path?", "Recreate", "New Path", "Cancel");
						if (recreateDataFolder == JOptionPane.OK_OPTION) {
							createDataStructure(dirToData);
							JOptionPane.showMessageDialog(StartGWASpi.mainGUIFrame, "Databases and working folders initialized successfully!");
						} else if (recreateDataFolder == JOptionPane.NO_OPTION) {
							dirToData = Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION);
							createDataStructure(dirToData);
							JOptionPane.showMessageDialog(StartGWASpi.mainGUIFrame, "Databases and working folders initialized successfully!");
						} else if (recreateDataFolder == JOptionPane.CANCEL_OPTION) {
							System.exit(0);
						}
					}

//					if (getConfigValue(PROPERTY_GENOTYPES_DIR, "").equals("")) {
					updateConfigDataDirs(dirToData);
//					} else {
					setDBSystemDir(derbyCenter.getPath());
//					}

					isInitiated = true;
				}
			} else { // CLI & THREAD MODE
				if (dirToData.getPath().equals("")) {
					if (scriptFile != null) {
						// Use path from script file
						File dataDir = new File(org.gwaspi.cli.Utils.readDataDirFromScript(scriptFile)); //1st line contains data path
						if (!dataDir.exists()) {
							dataDir = null;
						}

						if (dataDir != null) {
							try {
								createDataStructure(dataDir);
								isInitiated = true;
							} catch (Exception ex) {
								log.error(Text.App.warnUnableToInitForFirstTime, ex);
							}
						}
						log.info("Databases and working folders initialized successfully!");
						isInitiated = true;
					}
				} else {
					if (getConfigValue(PROPERTY_GENOTYPES_DIR, "").equals("")) {
						updateConfigDataDirs(dirToData);
					}
					isInitiated = true;
				}

			}

			// ALTER EXISTING DERBY DB TABLES TO SUIT CURRENT GWASPI VERSION
			DerbyDBReshaper.alterTableUpdates();
		} catch (Exception ex) {
			log.error("Failed initializing the configuration", ex);
		}

		return isInitiated;
	}

	protected static void createDataStructure(File dataDir) throws IOException, BackingStoreException, URISyntaxException {
		clearConfigFile();
		setConfigValue(PROPERTY_DATA_DIR, dataDir.getPath());
		File derbyCenter = new File(dataDir.getPath() + "/datacenter");
		setDBSystemDir(derbyCenter.getPath());

		if (!derbyCenter.exists()) {
			org.gwaspi.database.DatabaseGenerator.initDataCenter();
		}

		Utils.createFolder(dataDir.getPath(), "genotypes");
		Utils.createFolder(dataDir.getPath(), "help");
		Utils.createFolder(dataDir.getPath(), "export");
		Utils.createFolder(dataDir.getPath(), "reports");
		Utils.createFolder(dataDir.getPath() + "/reports", "log");

		setConfigValue(PROPERTY_GENOTYPES_DIR, dataDir.getPath() + "/genotypes");
		setConfigValue(PROPERTY_EXPORT_DIR, dataDir.getPath() + "/export");
		setConfigValue(PROPERTY_REPORTS_DIR, dataDir.getPath() + "/reports");
		setConfigValue(PROPERTY_LOG_DIR, dataDir.getPath() + "/reports/log");

		// SET CHART PREFERENCES
		setConfigValue("CHART_MANHATTAN_PLOT_BCKG", "200,200,200");
		setConfigValue("CHART_MANHATTAN_PLOT_BCKG_ALT", "230,230,230");
		setConfigValue("CHART_MANHATTAN_PLOT_DOT", "0,0,255");
		setConfigValue("CHART_MANHATTAN_PLOT_THRESHOLD", "5E-7");

		setConfigValue("CHART_QQ_PLOT_BCKG", "230,230,230");
		setConfigValue("CHART_QQ_PLOT_DOT", "0,0,255");
		setConfigValue("CHART_QQ_PLOT_2SIGMA", "170,170,170");

		setConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", "0.5");
		setConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", "0.5");

		URL localVersionPath = Config.class.getClass().getResource(cGlobal.LOCAL_VERSION_XML);
		Document localDom = XMLParser.parseXmlFile(localVersionPath.toURI().toString());
		List<Element> localElements = XMLParser.parseDocument(localDom, "GWASpi");
		setConfigValue(PROPERTY_CURRENT_GWASPIDB_VERSION, XMLParser.getTextValue(localElements.get(0), "GWASpi_DB_Version"));

		StudyList.createStudyLogFile(0);
	}

	protected static void updateConfigDataDirs(File dataDir) throws IOException, BackingStoreException, URISyntaxException {
		String lastOpenedDir = getConfigValue(PROPERTY_LAST_OPENED_DIR, cGlobal.HOMEDIR);
		String lastSelectedNode = getConfigValue(PROPERTY_LAST_SELECTED_NODE, Text.App.appName);

		String lastMnhttBack = getConfigValue("CHART_MANHATTAN_PLOT_BCKG", "200,200,200");
		String lastMnhttBackAlt = getConfigValue("CHART_MANHATTAN_PLOT_BCKG_ALT", "230,230,230");
		String lastMnhttDot = getConfigValue("CHART_MANHATTAN_PLOT_DOT", "0,0,255");
		String lastMnhttThreshold = getConfigValue("CHART_MANHATTAN_PLOT_THRESHOLD", "5E-7");
		String lastQQBack = getConfigValue("CHART_QQ_PLOT_BCKG", "230,230,230");
		String lastQQDot = getConfigValue("CHART_QQ_PLOT_DOT", "0,0,255");
		String lastQQCi = getConfigValue("CHART_QQ_PLOT_2SIGMA", "170,170,170");
		String lastSampleQAHetzyg = getConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", "0.5");
		String lastSampleQAMissingratio = getConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", "0.5");
		String lastVersionNb = getConfigValue(PROPERTY_CURRENT_GWASPIDB_VERSION, "2.0.1");

		clearConfigFile();
		setConfigValue(PROPERTY_DATA_DIR, dataDir.getPath());
		File derbyCenter = new File(dataDir.getPath() + "/datacenter");

		setConfigValue(PROPERTY_GENOTYPES_DIR, dataDir.getPath() + "/genotypes");
		setConfigValue(PROPERTY_EXPORT_DIR, dataDir.getPath() + "/export");
		setConfigValue(PROPERTY_REPORTS_DIR, dataDir.getPath() + "/reports");
		setConfigValue(PROPERTY_LOG_DIR, dataDir.getPath() + "/reports/log");
		setConfigValue(PROPERTY_LAST_OPENED_DIR, lastOpenedDir);
		setConfigValue(PROPERTY_LAST_SELECTED_NODE, lastSelectedNode);

		// SET CHART PREFERENCES
		setConfigValue("CHART_MANHATTAN_PLOT_BCKG", lastMnhttBack);
		setConfigValue("CHART_MANHATTAN_PLOT_BCKG_ALT", lastMnhttBackAlt);
		setConfigValue("CHART_MANHATTAN_PLOT_DOT", lastMnhttDot);
		setConfigValue("CHART_MANHATTAN_PLOT_THRESHOLD", lastMnhttThreshold);

		setConfigValue("CHART_QQ_PLOT_BCKG", lastQQBack);
		setConfigValue("CHART_QQ_PLOT_DOT", lastQQDot);
		setConfigValue("CHART_QQ_PLOT_2SIGMA", lastQQCi);

		setConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", lastSampleQAHetzyg);
		setConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", lastSampleQAMissingratio);

		URL localVersionPath = Config.class.getClass().getResource(cGlobal.LOCAL_VERSION_XML);
		Document localDom = XMLParser.parseXmlFile(localVersionPath.toURI().toString());
		List<Element> localElements = XMLParser.parseDocument(localDom, "GWASpi");
		setConfigValue(PROPERTY_CURRENT_GWASPIDB_VERSION, XMLParser.getTextValue(localElements.get(0), "GWASpi_DB_Version"));

		setDBSystemDir(derbyCenter.getPath());
	}

	public static void checkUpdates() throws IOException, ParseException, ParserConfigurationException, SAXException, URISyntaxException {
		if (Utils.checkInternetConnection()) {
			URL localVersionPath = Config.class.getClass().getResource(cGlobal.LOCAL_VERSION_XML);
			Document localDom = XMLParser.parseXmlFile(localVersionPath.toURI().toString());

			if (localDom != null) { // Found local version info
				System.setProperty("java.net.useSystemProxies", "true");

				List<Element> localElements = XMLParser.parseDocument(localDom, "GWASpi");
				setConfigValue(PROPERTY_CURRENT_GWASPIDB_VERSION, XMLParser.getTextValue(localElements.get(0), "GWASpi_DB_Version"));

				URL remoteVersionPath = new URL(cGlobal.REMOTE_VERSION_XML);
				Document remoteDom = XMLParser.parseXmlFile(remoteVersionPath.toURI().toString());

				if (remoteDom != null) { // Found remote version info
					// Retrieve data from XML files

					Date localUpdateDate = XMLParser.getDateValue(localElements.get(0), "Date");
					String localVersionNumber = XMLParser.getTextValue(localElements.get(0), "Number");

					List<Element> remoteElements = XMLParser.parseDocument(remoteDom, "GWASpi");
					Date remoteUpdateDate = XMLParser.getDateValue(remoteElements.get(0), "Date");
					String remoteVersionNumber = XMLParser.getTextValue(remoteElements.get(0), "Number");
					String remoteCompatibilityNumber = XMLParser.getTextValue(remoteElements.get(0), "Compatibility");

					StringBuilder message = new StringBuilder(Text.App.newVersionAvailable);
					message.append("\nLocal Version: ").append(localVersionNumber);
					message.append("\nNewest Version: ").append(remoteVersionNumber);
					message.append("\nUpdate Type: ").append(XMLParser.getTextValue(remoteElements.get(0), "Type"));

					// MAKE VERSION CHECKS
					if (remoteCompatibilityNumber.compareTo(localVersionNumber) <= 0) { //Remote version is still compatible with local version
						message.append("\n").append(Text.App.newVersionIsCompatible).append("\n").append(XMLParser.getTextValue(remoteElements.get(0), "ActionCompatible"));
					} else { // Remote version is NOT compatible with local version
						message.append("\n").append(Text.App.newVersionIsUnCompatible).append("\n").append(XMLParser.getTextValue(remoteElements.get(0), "ActionUnCompatible"));
					}
					message.append("\nChangelog: ").append(XMLParser.getTextValue(remoteElements.get(0), "Description"));

					if (localUpdateDate.compareTo(remoteUpdateDate) < 0) { //Remote version is more recent
						if (StartGWASpi.guiMode) {
							Dialogs.showWarningDialogue(message.toString());
						} else {
							log.error(message.toString());
						}
					}
				}
			}
		}
	}

	public static void setDBSystemDir(String dataCenter) throws IOException {
		// decide on the db system directory
		System.setProperty("derby.system.home", dataCenter);
	}

	public static boolean downloadFile(String dwnlUrl, String savePath, String saveName) {
		boolean result;
		File saveFile = new File(savePath + "/" + saveName);
		result = FileDownload.download(dwnlUrl, saveFile.getPath());
		return result;
	}
}

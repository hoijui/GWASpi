package org.gwaspi.global;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import org.gwaspi.database.DerbyDBReshaper;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.Preferences;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
 

public class Config {
  
    protected static Properties properties = new Properties();
    protected static JFileChooser fc;
    protected static Preferences prefs = Preferences.userNodeForPackage (Config.class.getClass());
    protected static int JVMbits = Integer.parseInt(System.getProperty("sun.arch.data.model", "32"));
    protected static boolean startWithGUI = true;

    public static void setConfigValue (String key, Object value) throws IOException {
        //GUI PREFS
        prefs.put (key, value.toString());

        //CLI & THREAD PREFS
        if(key.equals("DataDir")){
            org.gwaspi.gui.StartGWASpi.config_DataDir=(String) value;
        }
        if(key.equals("GTdir")){
            org.gwaspi.gui.StartGWASpi.config_GTdir=(String) value;
        }
        if(key.equals("ExportDir")){
            org.gwaspi.gui.StartGWASpi.config_ExportDir=(String) value;
        }
        if(key.equals("ReportsDir")){
            org.gwaspi.gui.StartGWASpi.config_ReportsDir=(String) value;
        }
        if(key.equals("LogDir")){
            org.gwaspi.gui.StartGWASpi.config_LogDir=(String) value;
        }
    }


    public static String getConfigValue(String key, String defaultV) throws IOException{
        String prop = "";
        if(org.gwaspi.gui.StartGWASpi.guiMode){
            //GUI MODE
            prop = prefs.get (key, defaultV);
        } else {
            //CLI MODE
            if(key.equals("DataDir")){
                if(org.gwaspi.gui.StartGWASpi.config_DataDir != null){prop = org.gwaspi.gui.StartGWASpi.config_DataDir;} else {prop = defaultV;}
            }
            if(key.equals("GTdir")){
                if(org.gwaspi.gui.StartGWASpi.config_GTdir != null){prop = org.gwaspi.gui.StartGWASpi.config_GTdir;} else {prop = defaultV;}
            }
            if(key.equals("ExportDir")){
                if(org.gwaspi.gui.StartGWASpi.config_ExportDir != null){prop = org.gwaspi.gui.StartGWASpi.config_ExportDir;} else {prop = defaultV;}
            }
            if(key.equals("ReportsDir")){
                if(org.gwaspi.gui.StartGWASpi.config_ReportsDir != null){prop = org.gwaspi.gui.StartGWASpi.config_ReportsDir;} else {prop = defaultV;}
            }
            if(key.equals("CHART_MANHATTAN_PLOT_THRESHOLD")){
                prop = defaultV;
            }
            if(key.equals("CHART_MANHATTAN_PLOT_BCKG")){
                prop = defaultV;
            }
            if(key.equals("CHART_MANHATTAN_PLOT_BCKG_ALT")){
                prop = defaultV;
            }
            if(key.equals("CHART_MANHATTAN_PLOT_DOT")){
                prop = defaultV;
            }
            if(key.equals("CHART_QQ_PLOT_BCKG")){
                prop = defaultV;
            }
            if(key.equals("CHART_QQ_PLOT_DOT")){
                prop = defaultV;
            }
            if(key.equals("CHART_QQ_PLOT_2SIGMA")){
                prop = defaultV;
            }
            if(key.equals("CHART_SAMPLEQA_HETZYG_THRESHOLD")){
                prop = defaultV;
            }
            if(key.equals("CHART_SAMPLEQA_MISSING_THRESHOLD")){
                prop = defaultV;
            }
            if(key.equals("CURRENT_GWASPIDB_VERSION")){
                prop = defaultV;
            }
            if(key.equals("LogDir")){
                if(org.gwaspi.gui.StartGWASpi.config_LogDir != null){prop = org.gwaspi.gui.StartGWASpi.config_LogDir;} else {prop = defaultV;}
            }
        }


        return prop;
    }

//    public static int getConfigValue(String key, int defaultV) throws IOException{
//        int prop = prefs.getInt(key, defaultV);
//        return prop;
//    }


    public static void clearConfigFile() throws IOException, BackingStoreException{
        if(startWithGUI){
            //GUI MODE
            prefs.clear();
        } else {
            //CLI MODE
            org.gwaspi.gui.StartGWASpi.config_DataDir=null;
            org.gwaspi.gui.StartGWASpi.config_GTdir=null;
            org.gwaspi.gui.StartGWASpi.config_ExportDir=null;
            org.gwaspi.gui.StartGWASpi.config_ReportsDir=null;
            org.gwaspi.gui.StartGWASpi.config_OfflineHelpDir=null;
            org.gwaspi.gui.StartGWASpi.config_LogDir=null;
        }
    }


    public static boolean initPreferences(boolean _startWithGUI, File scriptFile) {
        boolean isInitiated=false;
        //startWithGUI = _startWithGUI;
        try {
            //clearConfigFile();
            File dirToData = new File(getConfigValue("DataDir", ""));

            //CHECK FOR RECENT GWASPI VERSION
            checkUpdates();

            if(_startWithGUI){   //GUI MODE

                if(dirToData.getPath().equals("")){

                    

                    File dataDir = null;
                    JOptionPane.showMessageDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame, org.gwaspi.global.Text.App.initText);
                    dataDir = org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION);

                    if (dataDir!=null) {
                        try {
                            if (dataDir != null) {
                                createDataStructure(dataDir);
                                JOptionPane.showMessageDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame, "Databases and working folders initialized successfully!");
                            }
                            isInitiated=true;

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame, Text.App.warnUnableToInitForFirstTime);
                            ex.printStackTrace();
                        }
                    }

                } else {
                    File derbyCenter = new File(dirToData.getPath() + "/datacenter");
                    if(!derbyCenter.exists()){
                        int decision = org.gwaspi.gui.utils.Dialogs.showOptionDialogue("Data folder unreachable", "The data folder is unreachable (deleted?).\nShould GWASpi recreate it or do you want to provide a new path?", "Recreate", "New Path", "Cancel");
                        if(decision==JOptionPane.OK_OPTION){
                            createDataStructure(dirToData);
                            JOptionPane.showMessageDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame, "Databases and working folders initialized successfully!");
                        }
                        if(decision==JOptionPane.NO_OPTION){
                            dirToData = org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION);
                            createDataStructure(dirToData);
                            JOptionPane.showMessageDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame, "Databases and working folders initialized successfully!");
                        }
                        if(decision==JOptionPane.CANCEL_OPTION){
                            System.exit(0);
                        }
                    }

//                    if (getConfigValue("GTdir", "").equals("")) {
                        updateConfigDataDirs(dirToData);
//                    } else {
                        setDBSystemDir(derbyCenter.getPath());
//                    }
                   
                    isInitiated=true;
                }

            } else { //CLI & THREAD MODE

                if(dirToData.getPath().equals("")){
                    if (scriptFile!=null) {
                        //Use path from script file
                        File dataDir = new File(org.gwaspi.cli.Utils.readDataDirFromScript(scriptFile)); //1st line contains data path
                        if (!dataDir.exists()) {
                            dataDir = null;
                        }

                        if (dataDir!=null) {
                            try {
                                createDataStructure(dataDir);
                                isInitiated=true;
                            } catch (Exception ex) {
                                System.out.println(Text.App.warnUnableToInitForFirstTime);
                                ex.printStackTrace();
                            }
                        }
                        System.out.println("Databases and working folders initialized successfully!");
                        isInitiated=true;
                    }
                } else {
                    if (getConfigValue("GTdir", "").equals("")) {
                        updateConfigDataDirs(dirToData);
                    }
                    isInitiated=true;
                }

            }
            
        //ALTER EXISTING DERBY DB TABLES TO SUIT CURRENT GWASPI VERSION
        DerbyDBReshaper.alterTableUpdates();
        
        } catch (Exception e) {
                // Handle exception as needed
        }
        return isInitiated;
    }



    protected static void createDataStructure(File dataDir) throws IOException, BackingStoreException, URISyntaxException{
        clearConfigFile();
        setConfigValue("DataDir", dataDir.getPath());
        File derbyCenter = new File(dataDir.getPath() + "/datacenter");
        setDBSystemDir(derbyCenter.getPath());

        if(!derbyCenter.exists()){
            org.gwaspi.database.DatabaseGenerator.initDataCenter();
        }

        org.gwaspi.global.Utils.createFolder(dataDir.getPath(), "genotypes");
        org.gwaspi.global.Utils.createFolder(dataDir.getPath(), "help");
        org.gwaspi.global.Utils.createFolder(dataDir.getPath(), "export");
        org.gwaspi.global.Utils.createFolder(dataDir.getPath(), "reports");
        org.gwaspi.global.Utils.createFolder(dataDir.getPath() + "/reports", "log");

        setConfigValue("GTdir", dataDir.getPath() + "/genotypes");
        setConfigValue("ExportDir", dataDir.getPath() + "/export");
        setConfigValue("ReportsDir", dataDir.getPath() + "/reports");
        setConfigValue("LogDir", dataDir.getPath() + "/reports/log");

        //SET CHART PREFERENCES
        setConfigValue("CHART_MANHATTAN_PLOT_BCKG", "200,200,200");
        setConfigValue("CHART_MANHATTAN_PLOT_BCKG_ALT", "230,230,230");
        setConfigValue("CHART_MANHATTAN_PLOT_DOT", "0,0,255");
        setConfigValue("CHART_MANHATTAN_PLOT_THRESHOLD", "5E-7");

        setConfigValue("CHART_QQ_PLOT_BCKG", "230,230,230");
        setConfigValue("CHART_QQ_PLOT_DOT", "0,0,255");
        setConfigValue("CHART_QQ_PLOT_2SIGMA", "170,170,170");

        setConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", "0.5");
        setConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", "0.5");

        URL localVersionPath = Config.class.getClass().getResource(org.gwaspi.constants.cGlobal.LOCAL_VERSION_XML);
        Document localDom = org.gwaspi.global.XMLParser.parseXmlFile(localVersionPath.toURI().toString());
        ArrayList<Element> localElements = org.gwaspi.global.XMLParser.parseDocument(localDom, "GWASpi");
        setConfigValue("CURRENT_GWASPIDB_VERSION", org.gwaspi.global.XMLParser.getTextValue(localElements.get(0), "GWASpi_DB_Version"));
        
        org.gwaspi.database.StudyGenerator.createStudyLogFile(0);
    }

    protected static void updateConfigDataDirs(File dataDir) throws IOException, BackingStoreException, URISyntaxException{
        String lastOpenedDir = getConfigValue("LAST_OPENED_DIR", org.gwaspi.constants.cGlobal.HOMEDIR);
        String lastSelectedNode = getConfigValue("LAST_SELECTED_NODE", org.gwaspi.global.Text.App.appName);

        String lastMnhttBack = getConfigValue("CHART_MANHATTAN_PLOT_BCKG", "200,200,200");
        String lastMnhttBackAlt = getConfigValue("CHART_MANHATTAN_PLOT_BCKG_ALT", "230,230,230");
        String lastMnhttDot = getConfigValue("CHART_MANHATTAN_PLOT_DOT", "0,0,255");
        String lastMnhttThreshold = getConfigValue("CHART_MANHATTAN_PLOT_THRESHOLD", "5E-7");
        String lastQQBack = getConfigValue("CHART_QQ_PLOT_BCKG", "230,230,230");
        String lastQQDot = getConfigValue("CHART_QQ_PLOT_DOT", "0,0,255");
        String lastQQCi = getConfigValue("CHART_QQ_PLOT_2SIGMA", "170,170,170");
        String lastSampleQAHetzyg = getConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", "0.5");
        String lastSampleQAMissingratio = getConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", "0.5");
        String lastVersionNb = org.gwaspi.global.Config.getConfigValue("CURRENT_GWASPIDB_VERSION", "2.0.1");
        
        clearConfigFile();
        setConfigValue("DataDir", dataDir.getPath());
        File derbyCenter = new File(dataDir.getPath() + "/datacenter");

        setConfigValue("GTdir", dataDir.getPath() + "/genotypes");
        setConfigValue("ExportDir", dataDir.getPath() + "/export");
        setConfigValue("ReportsDir", dataDir.getPath() + "/reports");
        setConfigValue("LogDir", dataDir.getPath() + "/reports/log");
        setConfigValue("LAST_OPENED_DIR", lastOpenedDir);
        setConfigValue("LAST_SELECTED_NODE", lastSelectedNode);

        //SET CHART PREFERENCES
        setConfigValue("CHART_MANHATTAN_PLOT_BCKG", lastMnhttBack);
        setConfigValue("CHART_MANHATTAN_PLOT_BCKG_ALT", lastMnhttBackAlt);
        setConfigValue("CHART_MANHATTAN_PLOT_DOT", lastMnhttDot);
        setConfigValue("CHART_MANHATTAN_PLOT_THRESHOLD", lastMnhttThreshold);

        setConfigValue("CHART_QQ_PLOT_BCKG", lastQQBack);
        setConfigValue("CHART_QQ_PLOT_DOT", lastQQDot);
        setConfigValue("CHART_QQ_PLOT_2SIGMA", lastQQCi);

        setConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", lastSampleQAHetzyg);
        setConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", lastSampleQAMissingratio);
        
        URL localVersionPath = Config.class.getClass().getResource(org.gwaspi.constants.cGlobal.LOCAL_VERSION_XML);
        Document localDom = org.gwaspi.global.XMLParser.parseXmlFile(localVersionPath.toURI().toString());
        ArrayList<Element> localElements = org.gwaspi.global.XMLParser.parseDocument(localDom, "GWASpi");
        setConfigValue("CURRENT_GWASPIDB_VERSION", org.gwaspi.global.XMLParser.getTextValue(localElements.get(0), "GWASpi_DB_Version"));
        
        setDBSystemDir(derbyCenter.getPath());
    }


    public static void checkUpdates() throws IOException, ParseException, ParserConfigurationException, SAXException, URISyntaxException{
        if(org.gwaspi.global.Utils.checkIntenetConnection()){
            URL localVersionPath = Config.class.getClass().getResource(org.gwaspi.constants.cGlobal.LOCAL_VERSION_XML);
            Document localDom = org.gwaspi.global.XMLParser.parseXmlFile(localVersionPath.toURI().toString());
            
            if(localDom!=null){ //Found local version info
                System.setProperty("java.net.useSystemProxies", "true");
                
                ArrayList<Element> localElements = org.gwaspi.global.XMLParser.parseDocument(localDom, "GWASpi");
                setConfigValue("CURRENT_GWASPIDB_VERSION", org.gwaspi.global.XMLParser.getTextValue(localElements.get(0), "GWASpi_DB_Version"));
                
                URL remoteVersionPath = new URL(org.gwaspi.constants.cGlobal.REMOTE_VERSION_XML);
                Document remoteDom = org.gwaspi.global.XMLParser.parseXmlFile(remoteVersionPath.toURI().toString());

                if (remoteDom!=null) { //Found remote version info
                    //Retrieve data from XML files
                    
                    Date localUpdateDate = org.gwaspi.global.XMLParser.getDateValue(localElements.get(0), "Date");
                    String localVersionNumber = org.gwaspi.global.XMLParser.getTextValue(localElements.get(0), "Number");
                    
                    ArrayList<Element> remoteElements = org.gwaspi.global.XMLParser.parseDocument(remoteDom, "GWASpi");
                    Date remoteUpdateDate = org.gwaspi.global.XMLParser.getDateValue(remoteElements.get(0), "Date");
                    String remoteVersionNumber = org.gwaspi.global.XMLParser.getTextValue(remoteElements.get(0), "Number");
                    String remoteCompatibilityNumber = org.gwaspi.global.XMLParser.getTextValue(remoteElements.get(0), "Compatibility");
                    
                    StringBuilder message = new StringBuilder(org.gwaspi.global.Text.App.newVersionAvailable);
                    message.append("\nLocal Version: ").append(localVersionNumber);
                    message.append("\nNewest Version: ").append(remoteVersionNumber);
                    message.append("\nUpdate Type: ").append(org.gwaspi.global.XMLParser.getTextValue(remoteElements.get(0), "Type"));
                    
                    //MAKE VERSION CHECKS
                    if (remoteCompatibilityNumber.compareTo(localVersionNumber) <= 0) { //Remote version is still compatible with local version
                        message.append("\n").append(org.gwaspi.global.Text.App.newVersionIsCompatible).append("\n").append(org.gwaspi.global.XMLParser.getTextValue(remoteElements.get(0), "ActionCompatible"));
                    } else {     //Remote version is NOT compatible with local version
                        message.append("\n").append(org.gwaspi.global.Text.App.newVersionIsUnCompatible).append("\n").append(org.gwaspi.global.XMLParser.getTextValue(remoteElements.get(0), "ActionUnCompatible"));
                    }
                    message.append("\nChangelog: ").append(org.gwaspi.global.XMLParser.getTextValue(remoteElements.get(0), "Description"));
                    
                    if (localUpdateDate.compareTo(remoteUpdateDate) < 0) { //Remote version is more recent
                        if (org.gwaspi.gui.StartGWASpi.guiMode) {
                            org.gwaspi.gui.utils.Dialogs.showWarningDialogue(message.toString());
                        } else {
                            System.out.println(message.toString());
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
      File saveFile = new File(savePath+"/"+saveName);
      result =  org.gwaspi.global.FileDownload.download(dwnlUrl, saveFile.getPath());
      return result;
    }



}


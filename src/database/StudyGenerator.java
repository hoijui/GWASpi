package database;

import global.ServiceLocator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class StudyGenerator {
    
    public static String createStudyManagementTable(DbManager db, Object[] insertValues) {
        boolean result = false;
        try {
            //CREATE STUDIES table in APP SCHEMA and fill with data
            db.createTable(constants.cDBGWASpi.SCH_APP,
                    constants.cDBGWASpi.T_STUDIES,
                    constants.cDBGWASpi.T_CREATE_STUDIES);
           
            result = db.insertValuesInTable(constants.cDBGWASpi.SCH_APP,
                    constants.cDBGWASpi.T_STUDIES,
                    constants.cDBGWASpi.F_INSERT_STUDIES,
                    insertValues);
        } catch (Exception e) {
            System.out.println("Error creating Schema or Studies table");
            System.out.print(e);
            e.printStackTrace();
        }
        return  (result)?"1":"0";
    }
    
    
    public static void insertNewStudy(String studyName, String description) {
        boolean result = false;
        try{
            DbManager db = ServiceLocator.getDbManager(constants.cDBGWASpi.DB_DATACENTER);
            
            //INSERT study data into study management table
            result = db.insertValuesInTable(constants.cDBGWASpi.SCH_APP,
                                            constants.cDBGWASpi.T_STUDIES,
                                            constants.cDBGWASpi.F_INSERT_STUDIES ,
                                            new Object[]{studyName,         //name
                                                         description,        //description
                                                         "external",        //stydy_type
                                                         "1"});     //validity
            
        }catch(Exception e){
            System.out.println("Error creating management database");
            System.out.print(e);
            e.printStackTrace();
        }
    
    }
    
    public static void deleteStudy(int studyId, boolean deleteReports) throws IOException{
        
        model.MatricesList matrixMod = new model.MatricesList(studyId);
        
        for(int i=0; i<matrixMod.matrixList.size();i++){
            try {
                netCDF.matrices.MatrixManager.deleteMatrix(matrixMod.matrixList.get(i).getMatrixId(), deleteReports);
                gui.GWASpiExplorerPanel.updateTreePanel(true);
            } catch (IOException ex) {
            }
        }
        
        //DELETE METADATA INFO FROM DB
        DbManager dBManager = ServiceLocator.getDbManager(constants.cDBGWASpi.DB_DATACENTER);
        String statement = "DELETE FROM "+constants.cDBGWASpi.SCH_APP+"."+constants.cDBGWASpi.T_STUDIES+" WHERE "+constants.cDBGWASpi.f_ID+"="+studyId;
        dBManager.executeStatement(statement);

        //DELETE STUDY FOLDERS
        String genotypesFolder = global.Config.getConfigValue("GTdir","");
        File gtStudyFolder = new File(genotypesFolder+"/STUDY_"+studyId);
        global.Utils.deleteFolder(gtStudyFolder);

        if(deleteReports){
            String reportsFolder = global.Config.getConfigValue("ReportsDir","");
            File repStudyFolder = new File(reportsFolder+"/STUDY_"+studyId);
            global.Utils.deleteFolder(repStudyFolder);
        }

        //DELETE STUDY POOL SAMPLES
        samples.SampleManager.deleteSamplesByPoolId(studyId);

    }

    public static String createStudyLogFile(Integer studyId) throws IOException{
        String result = "";
        
        //Create log file containing study history 
        FileWriter fw = new FileWriter(global.Config.getConfigValue("LogDir", "") + "/"+studyId+".log");
        return result;
    }

}

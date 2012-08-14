package org.gwaspi.samples;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class SampleManager {

    private static String processStartTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
    private static DbManager db = null;
    private static Integer currentLoadedSamples=0;
    private static Integer totalLoadedSamples;


    ///////////////////////////////////////////
    //////////// SAMPLE INFO TABLE ////////////
    //////////////////////////////////////////
    
    public static String createSamplesInfoTable(DbManager db) {
        boolean result = false;
        try {
            //CREATE SAMPLE table in given SCHEMA
            db.createTable(org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES,
                    org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO,
                    org.gwaspi.constants.cDBSamples.T_CREATE_SAMPLES_INFO);
            result = true;
        } catch (Exception e) {
            System.out.println("Error creating Sample table");
            System.out.print(e);
            e.printStackTrace();
        }
        return  (result)?"1":"0";
    }


    public static List<Map<String, Object>> selectSampleIDList(Object poolId){
        try {
            List<Map<String, Object>> rs = null;
            DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
            String sql = "SELECT "+constants.cDBSamples.f_SAMPLE_ID+" FROM "+constants.cDBGWASpi.SCH_SAMPLES+"."+constants.cDBSamples.T_SAMPLES_INFO+" WHERE "+constants.cDBSamples.f_POOL_ID+"='"+poolId+"' ORDER BY order_id  WITH RR";
            rs = dBManager.executeSelectStatement(sql);
            return rs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static List<Map<String, Object>> getAllSampleInfoFromDB() throws IOException{
            List<Map<String, Object>> rs = null;
            DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

            String sql = "SELECT * FROM "+constants.cDBGWASpi.SCH_SAMPLES+"."+constants.cDBSamples.T_SAMPLES_INFO+" ORDER BY order_id  WITH RR";
            rs = dBManager.executeSelectStatement(sql);

            return rs;
    }

    public static List<Map<String, Object>> getAllSampleInfoFromDBByPoolID(Object poolId) throws IOException{
            List<Map<String, Object>> rs = null;
            DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

            String sql = "SELECT * FROM "+constants.cDBGWASpi.SCH_SAMPLES+"."+constants.cDBSamples.T_SAMPLES_INFO+" WHERE "+constants.cDBSamples.f_POOL_ID+"='"+poolId+"'"+" ORDER BY order_id  WITH RR";
            rs = dBManager.executeSelectStatement(sql);

            return rs;
    }

    public static List<Map<String, Object>> getCurrentSampleInfoFromDB(String sampleId, Object poolId) throws IOException{
            List<Map<String, Object>> rs = null;
            DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

            String sql = "SELECT * FROM "+constants.cDBGWASpi.SCH_SAMPLES+"."+constants.cDBSamples.T_SAMPLES_INFO+" WHERE "+constants.cDBSamples.f_SAMPLE_ID+"='"+sampleId+"' AND "+constants.cDBSamples.f_POOL_ID+"='"+poolId+"'  WITH RR";
            rs = dBManager.executeSelectStatement(sql);

            return rs;
    }


    public static void deleteSamplesByPoolId(Object poolId) throws IOException{
            DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

            String sql = "DELETE FROM "+constants.cDBGWASpi.SCH_SAMPLES+"."+constants.cDBSamples.T_SAMPLES_INFO+" WHERE "+constants.cDBSamples.f_POOL_ID+"='"+poolId+"'";
            dBManager.executeStatement(sql);
    }

    
}

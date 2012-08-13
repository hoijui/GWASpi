/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import database.DbManager;
import global.ServiceLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class ReportsList {

    public ArrayList<model.Report> reportsListAL = new ArrayList();

    public ReportsList(int opId, int matrixId) throws IOException{

        List<Map<String, Object>> rs= null;

        if(opId!=Integer.MIN_VALUE){
            rs= getReportListByOperationId(opId);
        } else {
            rs= getReportListByMatrixId(matrixId);
        }

        int rowcount = rs.size();
        if (rowcount > 0) {
            for (int i = rowcount-1; i >= 0; i--) // loop through rows of result set
            {
                //PREVENT PHANTOM-DB READS EXCEPTIONS
                if(!rs.isEmpty() && rs.get(i).size()==constants.cDBReports.T_CREATE_REPORTS.length){
                    int currentRPId = (Integer) rs.get(i).get(constants.cDBMatrix.f_ID);
                    Report currentRP = new Report(currentRPId);
                    reportsListAL.add(currentRP);
                }
            }
        }
    }

    public List<Map<String, Object>> getReportListByOperationId(int opId) throws IOException{
            List<Map<String, Object>> rs = null;
            String dbName = constants.cDBGWASpi.DB_DATACENTER;
            DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
            try {
                rs = studyDbManager.executeSelectStatement("SELECT * FROM "+constants.cDBGWASpi.SCH_MATRICES+"."+constants.cDBReports.T_REPORTS+" WHERE "+constants.cDBReports.f_PARENT_OPID+"="+opId+" ORDER BY "+constants.cDBMatrix.f_ID+" DESC  WITH RR");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return rs;
    }

    public List<Map<String, Object>> getReportListByMatrixId(int matrixId) throws IOException{
            List<Map<String, Object>> rs = null;
            String dbName = constants.cDBGWASpi.DB_DATACENTER;
            DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
            try {
                rs = studyDbManager.executeSelectStatement("SELECT * FROM "+constants.cDBGWASpi.SCH_MATRICES+"."+constants.cDBReports.T_REPORTS+" WHERE "+constants.cDBReports.f_PARENT_MATRIXID+"="+matrixId+" ORDER BY "+constants.cDBMatrix.f_ID+" DESC  WITH RR");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return rs;
    }


    public static Object[][] getReportsTable(int opId) throws IOException{
        Object[][] reportsTable = null;

        List<Map<String, Object>> rs = null;
        String dbName = constants.cDBGWASpi.DB_DATACENTER;
        DbManager dbManager = ServiceLocator.getDbManager(dbName);
        try {
            rs = dbManager.executeSelectStatement("SELECT * FROM "+constants.cDBGWASpi.SCH_MATRICES+"."+constants.cDBReports.T_REPORTS+" WHERE "+constants.cDBReports.f_ID+"="+opId+"  WITH RR");

            reportsTable = new Object[rs.size()][3];
            for(int i=0; i<rs.size();i++){
                //PREVENT PHANTOM-DB READS EXCEPTIONS
                if(!rs.isEmpty() && rs.get(i).size()==constants.cDBReports.T_CREATE_REPORTS.length){
                    reportsTable[i][0] = (Integer) rs.get(i).get(constants.cDBReports.f_ID);
                    reportsTable[i][1] = rs.get(i).get(constants.cDBReports.f_RP_NAME).toString();
                    reportsTable[i][2] = rs.get(i).get(constants.cDBReports.f_DESCRIPTION).toString();
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return reportsTable;
    }

}

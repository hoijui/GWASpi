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
public final class MatricesList {

    public ArrayList<model.Matrix> matrixList = new ArrayList();

    public MatricesList(int studyId) throws IOException{

        List<Map<String, Object>> rsMatricesList= getMatrixListByStudyId(studyId);

        int rowcount = rsMatricesList.size();
        if (rowcount > 0) {
            for (int i = rowcount-1; i >= 0; i--) // loop through rows of result set
            {
                //PREVENT PHANTOM-DB READS EXCEPTIONS
                if(!rsMatricesList.isEmpty() && rsMatricesList.get(i).size()==constants.cDBMatrix.T_CREATE_MATRICES.length){
                    int currentMatrixId = (Integer) rsMatricesList.get(i).get(constants.cDBMatrix.f_ID);
                    Matrix currentMatrix = new Matrix(currentMatrixId);
                    matrixList.add(currentMatrix);
                }
            }
        }
    }

    public MatricesList() throws IOException{

        List<Map<String, Object>> rsMatricesList= getAllMatricesList();

        int rowcount = rsMatricesList.size();
        if (rowcount > 0) {
            for (int i = rowcount-1; i >= 0; i--) // loop through rows of result set
            {
                //PREVENT PHANTOM-DB READS EXCEPTIONS
                if(!rsMatricesList.isEmpty() && rsMatricesList.get(i).size()==constants.cDBMatrix.T_CREATE_MATRICES.length){
                    int currentMatrixId = (Integer) rsMatricesList.get(i).get(constants.cDBMatrix.f_ID);
                    Matrix currentMatrix = new Matrix(currentMatrixId);
                    matrixList.add(currentMatrix);
                }
            }
        }
    }

    public List<Map<String, Object>> getMatrixListByStudyId(int studyId) throws IOException{
            List<Map<String, Object>> rs = null;
            String dbName = constants.cDBGWASpi.DB_DATACENTER;
            DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
            try {
                rs = studyDbManager.executeSelectStatement("SELECT * FROM "+constants.cDBGWASpi.SCH_MATRICES+"."+constants.cDBMatrix.T_MATRICES+" WHERE "+constants.cDBMatrix.f_STUDYID+"="+studyId+" ORDER BY "+constants.cDBMatrix.f_ID+" DESC  WITH RR");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return rs;
    }

    public static List<Map<String, Object>> getAllMatricesList() throws IOException{
            List<Map<String, Object>> rs = null;
            String dbName = constants.cDBGWASpi.DB_DATACENTER;
            DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
            try {
                rs = studyDbManager.executeSelectStatement("SELECT * FROM "+constants.cDBGWASpi.SCH_MATRICES+"."+constants.cDBMatrix.T_MATRICES+"  WITH RR");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return rs;
    }

    public static Object[][] getMatricesTable(int studyId) throws IOException{
        Object[][] table = null;

        List<Map<String, Object>> rs = null;
        String dbName = constants.cDBGWASpi.DB_DATACENTER;
        DbManager dbManager = ServiceLocator.getDbManager(dbName);
        try {
            rs = dbManager.executeSelectStatement("SELECT * FROM "+constants.cDBGWASpi.SCH_MATRICES+"."+constants.cDBMatrix.T_MATRICES+" WHERE "+constants.cDBMatrix.f_STUDYID+"="+studyId+"  WITH RR");

            table = new Object[rs.size()][4];
            for(int i=0; i<rs.size();i++){
                //PREVENT PHANTOM-DB READS EXCEPTIONS
                if(!rs.isEmpty() && rs.get(i).size()==constants.cDBMatrix.T_CREATE_MATRICES.length){
                    table[i][0] = (Integer) rs.get(i).get(constants.cDBMatrix.f_ID);
                    table[i][1] = rs.get(i).get(constants.cDBMatrix.f_MATRIX_NAME).toString();
                    table[i][2] = rs.get(i).get(constants.cDBMatrix.f_DESCRIPTION).toString();
                    String timestamp = rs.get(i).get(constants.cDBOperations.f_CREATION_DATE).toString();
                    table[i][3] = timestamp.substring(0, timestamp.lastIndexOf("."));
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return table;
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reports;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import constants.cDBGWASpi;
import constants.cExport;
import constants.cNetCDF;
import database.DbManager;
import global.ServiceLocator;
import global.Text;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import model.Operation;
import netCDF.markers.MarkerSet_opt;
import netCDF.matrices.MatrixMetadata;
import netCDF.operations.OperationManager;
import netCDF.operations.OperationMetadata;
import netCDF.operations.OperationSet;
import ucar.nc2.NetcdfFile;


public class OutputHardyWeinberg_opt {

    public static boolean writeReportsForMarkersHWData(int opId) throws FileNotFoundException, IOException{
        Operation op = new Operation(opId);
        DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

        //String hwOutName = "hw_"+op.getOperationId()+"_"+op.getOperationFriendlyName()+".hw";
        String prefix = reports.ReportManager.getreportNamePrefix(op);
        String hwOutName = prefix+"hardy-weinberg.txt";

        global.Utils.createFolder(global.Config.getConfigValue("ReportsDir", ""), "STUDY_"+op.getStudyId());

        if(processSortedHardyWeinbergReport(opId, hwOutName)){
            ReportManager.insertRPMetadata(dBManager,
                             "Hardy Weinberg Table",
                             hwOutName,
                             cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString(),
                             op.getParentMatrixId(),
                             opId,
                             "Hardy Weinberg Table",
                             op.getStudyId());

            global.Utils.sysoutCompleted("Hardy-Weinberg Report");
        }

        return true;
    }


    protected static boolean processSortedHardyWeinbergReport(int opId, String reportName) throws FileNotFoundException, IOException{
        boolean result=false;

        try {
            LinkedHashMap unsortedMarkerIdHWPval_ALTLHM = GatherHardyWeinbergData.loadHWPval_ALT(opId);
            LinkedHashMap sortingMarkerSetLHM = ReportManager.getSortedMarkerSetByDoubleValue(unsortedMarkerIdHWPval_ALTLHM);
            if (unsortedMarkerIdHWPval_ALTLHM!=null) {
                unsortedMarkerIdHWPval_ALTLHM.clear();
            }

            //STORE HW VALUES FOR LATER
            LinkedHashMap storeHWPval_ALTLHM = new LinkedHashMap();
            for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = sortingMarkerSetLHM.get(key);
                storeHWPval_ALTLHM.put(key, value);
            }

            //GET MARKER INFO
            String sep = cExport.separator_REPORTS;
            OperationMetadata rdOPMetadata = new OperationMetadata(opId);
            MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
            rdInfoMarkerSet.initFullMarkerIdSetLHM();

            //WRITE HEADER OF FILE
            String header = "MarkerID\trsID\tChr\tPosition\tMin_Allele\tMaj_Allele\t"+Text.Reports.hwPval+Text.Reports.CTRL+"\t"+Text.Reports.hwObsHetzy+Text.Reports.CTRL+"\t"+Text.Reports.hwExpHetzy+Text.Reports.CTRL+"\n";
            String reportPath = global.Config.getConfigValue("ReportsDir", "") + "/STUDY_"+rdOPMetadata.getStudyId()+"/";



            //WRITE MARKERSET RSID
            rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
            for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = rdInfoMarkerSet.markerIdSetLHM.get(key);
                sortingMarkerSetLHM.put(key, value);
            }
            ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetLHM, true);


            //WRITE MARKERSET CHROMOSOME
            rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
            for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = rdInfoMarkerSet.markerIdSetLHM.get(key);
                sortingMarkerSetLHM.put(key, value);
            }
            ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

            //WRITE MARKERSET POS
            rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
            for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = rdInfoMarkerSet.markerIdSetLHM.get(key);
                sortingMarkerSetLHM.put(key, value);
            }
            ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

            //WRITE KNOWN ALLELES FROM QA
            //get MARKER_QA Operation
            ArrayList operationsAL = OperationManager.getMatrixOperations(rdOPMetadata.getParentMatrixId());
            int markersQAopId = Integer.MIN_VALUE;
            for(int i=0;i<operationsAL.size();i++){
                Object[] element = (Object[]) operationsAL.get(i);
                if(element[1].toString().equals(cNetCDF.Defaults.OPType.MARKER_QA.toString())){
                    markersQAopId = (Integer) element[0];
                }
            }
            if(markersQAopId!=Integer.MIN_VALUE){
                OperationMetadata qaMetadata = new OperationMetadata(markersQAopId);
                NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

                OperationSet rdOperationSet = new OperationSet(rdOPMetadata.getStudyId(),markersQAopId);
                LinkedHashMap opMarkerSetLHM = rdOperationSet.getOpSetLHM();

                //MINOR ALLELE
                opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
                for (Iterator it = rdInfoMarkerSet.markerIdSetLHM.keySet().iterator(); it.hasNext();) {
                    Object key = it.next();
                    Object minorAllele = opMarkerSetLHM.get(key);
                    rdInfoMarkerSet.markerIdSetLHM.put(key, minorAllele);
                }

                //MAJOR ALLELE
                opMarkerSetLHM = rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
                opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
                for (Iterator it = rdInfoMarkerSet.markerIdSetLHM.keySet().iterator(); it.hasNext();) {
                    Object key = it.next();
                    Object minorAllele = rdInfoMarkerSet.markerIdSetLHM.get(key);
                    rdInfoMarkerSet.markerIdSetLHM.put(key, minorAllele+sep+opMarkerSetLHM.get(key));
                }


            }
            for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = rdInfoMarkerSet.markerIdSetLHM.get(key);
                sortingMarkerSetLHM.put(key, value);
            }
            ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);


            //WRITE HW PVAL
            ReportWriter.appendColumnToReport(reportPath, reportName, storeHWPval_ALTLHM, false, false);
            if (storeHWPval_ALTLHM!=null) {
                storeHWPval_ALTLHM.clear();
            }

            //WRITE HW HETZY ARRAY
            LinkedHashMap markerIdHWHETZY_CTRLLHM = GatherHardyWeinbergData.loadHWHETZY_ALT(opId);
            for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = markerIdHWHETZY_CTRLLHM.get(key);
                sortingMarkerSetLHM.put(key, value);
            }
            ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, true, false);

            result = true;
        } catch (IOException iOException) {
            result = false;
        }

        return result;
    }


}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.reports;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.gwaspi.netCDF.operations.OperationSet;
import ucar.nc2.NetcdfFile;


public class OutputHardyWeinberg {

    public static boolean writeReportsForMarkersHWData(int opId) throws FileNotFoundException, IOException{
        Operation op = new Operation(opId);
        DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

        //String hwOutName = "hw_"+op.getOperationId()+"_"+op.getOperationFriendlyName()+".hw";
        String prefix = org.gwaspi.reports.ReportManager.getreportNamePrefix(op);
        String hwOutName = prefix+"hardy-weinberg.txt";

        org.gwaspi.global.Utils.createFolder(org.gwaspi.global.Config.getConfigValue("ReportsDir", ""), "STUDY_"+op.getStudyId());

        if(processSortedHardyWeinbergReport(opId, hwOutName)){
            ReportManager.insertRPMetadata(dBManager,
                             "Hardy Weinberg Table",
                             hwOutName,
                             cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString(),
                             op.getParentMatrixId(),
                             opId,
                             "Hardy Weinberg Table",
                             op.getStudyId());

            org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Report");
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
            MatrixMetadata rdMatrixMetadata = new MatrixMetadata(rdOPMetadata.getParentMatrixId());
            MarkerSet rdInfoMarkerSet = new MarkerSet(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
            NetcdfFile matrixNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
            LinkedHashMap infoMatrixMarkerSetLHM = rdInfoMarkerSet.getMarkerIdSetLHM();

            //WRITE HEADER OF FILE
            String header = "MarkerID\trsID\tChr\tPosition\tMin_Allele\tMaj_Allele\t"+Text.Reports.hwPval+Text.Reports.CTRL+"\t"+Text.Reports.hwObsHetzy+Text.Reports.CTRL+"\t"+Text.Reports.hwExpHetzy+Text.Reports.CTRL+"\n";
            String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_"+rdOPMetadata.getStudyId()+"/";



            //WRITE MARKERSET RSID
            infoMatrixMarkerSetLHM = rdInfoMarkerSet.fillMarkerSetLHMWithVariable(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
            for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = infoMatrixMarkerSetLHM.get(key);
                sortingMarkerSetLHM.put(key, value);
            }
            ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetLHM, true);


            //WRITE MARKERSET CHROMOSOME
            infoMatrixMarkerSetLHM = rdInfoMarkerSet.fillMarkerSetLHMWithVariable(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_CHR);
            for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = infoMatrixMarkerSetLHM.get(key);
                sortingMarkerSetLHM.put(key, value);
            }
            ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

            //WRITE MARKERSET POS
            infoMatrixMarkerSetLHM = rdInfoMarkerSet.fillMarkerSetLHMWithVariable(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS);
            for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = infoMatrixMarkerSetLHM.get(key);
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
                for (Iterator it = infoMatrixMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                    Object key = it.next();
                    Object minorAllele = opMarkerSetLHM.get(key);
                    infoMatrixMarkerSetLHM.put(key, minorAllele);
                }

                //MAJOR ALLELE
                opMarkerSetLHM = rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
                opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
                for (Iterator it = infoMatrixMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                    Object key = it.next();
                    Object minorAllele = infoMatrixMarkerSetLHM.get(key);
                    infoMatrixMarkerSetLHM.put(key, minorAllele+sep+opMarkerSetLHM.get(key));
                }

//                //MAJOR ALLELE
//                opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
//                for (Iterator it = infoMatrixMarkerSetLHM.keySet().iterator(); it.hasNext();) {
//                    Object key = it.next();
//                    Object value = opMarkerSetLHM.get(key);
//                    infoMatrixMarkerSetLHM.put(key, value);
//                }
//
//                //MINOR ALLELE
//                opMarkerSetLHM = rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
//                opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
//                for (Iterator it = infoMatrixMarkerSetLHM.keySet().iterator(); it.hasNext();) {
//                    Object key = it.next();
//                    Object value = infoMatrixMarkerSetLHM.get(key);
//                    infoMatrixMarkerSetLHM.put(key, value+sep+opMarkerSetLHM.get(key));
//                }
            }
            for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = infoMatrixMarkerSetLHM.get(key);
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

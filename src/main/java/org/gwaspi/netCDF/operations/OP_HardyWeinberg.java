package org.gwaspi.netCDF.operations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import org.gwaspi.constants.cNetCDF;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.gwaspi.model.Operation;
import ucar.ma2.*;
import ucar.nc2.*;
import org.gwaspi.constants.cNetCDF.*;

public class OP_HardyWeinberg {

    public static int processMatrix(Operation markerCensusOP, String censusName) throws IOException, InvalidRangeException{
        int resultOpId = Integer.MIN_VALUE;

        OperationMetadata rdOPMetadata = new OperationMetadata(markerCensusOP.getOperationId());
        NetcdfFile rdNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());

        OperationSet rdOperationSet = new OperationSet(rdOPMetadata.getStudyId(),markerCensusOP.getOperationId());
        LinkedHashMap rdMarkerSetLHM = rdOperationSet.getOpSetLHM();
        LinkedHashMap rdSampleSetLHM = rdOperationSet.getImplicitSetLHM();

        NetcdfFileWriteable wrNcFile = null;
        try {
            ///////////// CREATE netCDF-3 FILE ////////////

            OperationFactory wrOPHandler = new OperationFactory(rdOPMetadata.getStudyId(),
                                                "Hardy-Weinberg_"+censusName, //friendly name
                                                "Hardy-Weinberg test on Samples marked as controls (only females for the X chromosome)\nMarkers: "+rdMarkerSetLHM.size()+"\nSamples: "+rdSampleSetLHM.size(), //description
                                                rdMarkerSetLHM.size(),
                                                rdSampleSetLHM.size(),
                                                0,
                                                cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString(),
                                                rdOPMetadata.getParentMatrixId(), //Parent matrixId
                                                markerCensusOP.getOperationId());       //Parent operationId
            wrNcFile = wrOPHandler.getNetCDFHandler();

            try {
                wrNcFile.create();
            } catch (IOException e) {
                System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
            }
            


            //<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
            //MARKERSET MARKERID
            ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdMarkerSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
            int[] markersOrig = new int[]{0, 0};
            try {
                wrNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
            } catch (IOException e) {
                System.err.println("ERROR writing file");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }

            //MARKERSET RSID
            rdMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(rdNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
            Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

            //WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
            ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

            int[] sampleOrig = new int[]{0,0};
            try {
                wrNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
            } catch (IOException e) {
                System.err.println("ERROR writing file");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
            samplesD2 = null;
            System.out.println("Done writing SampleSet to matrix at "+global.Utils.getMediumDateTimeAsString());

            //</editor-fold>


            //<editor-fold defaultstate="collapsed" desc="GET CENSUS & PERFORM HW">
//            //PROCESS ALL SAMPLES
//            rdMarkerSetLHM = rdOperationSet.fillLHMWithDefaultValue(rdMarkerSetLHM, 0); //PURGE
//            rdMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
//            performHardyWeinberg(wrNcFile, rdMarkerSetLHM, "ALL");
//
//
//            //PROCESS CASE SAMPLES
//            rdMarkerSetLHM = rdOperationSet.fillLHMWithDefaultValue(rdMarkerSetLHM, 0); //PURGE
//            rdMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
//            performHardyWeinberg(wrNcFile, rdMarkerSetLHM, "CASE");
//
//
            //PROCESS CONTROL SAMPLES
            System.out.println(org.gwaspi.global.Text.All.processing);
            rdMarkerSetLHM = rdOperationSet.fillLHMWithDefaultValue(rdMarkerSetLHM, 0); //PURGE
            rdMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
            performHardyWeinberg(wrNcFile, rdMarkerSetLHM, "CTRL");

            //PROCESS ALTERNATE HW SAMPLES
            System.out.println(org.gwaspi.global.Text.All.processing);
            rdMarkerSetLHM = rdOperationSet.fillLHMWithDefaultValue(rdMarkerSetLHM, 0); //PURGE
            rdMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW);
            performHardyWeinberg(wrNcFile, rdMarkerSetLHM, "HW-ALT");
            //</editor-fold>

            resultOpId = wrOPHandler.getResultOPId();
            org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Equilibrium Test");
        } catch (InvalidRangeException invalidRangeException) {
        } catch (IOException iOException) {
        } finally {
            if (null != rdNcFile) try {
                rdNcFile.close();
                wrNcFile.close();
            } catch (IOException ioe) {
                System.err.println("Cannot close file: "+ioe);
            }
            return resultOpId;
        }

    }


    protected static void performHardyWeinberg(NetcdfFileWriteable wrNcFile, LinkedHashMap markersContingencyLHM, String category){
        //Iterate through markerset
        int markerNb=0;
        for (Iterator it = markersContingencyLHM.keySet().iterator(); it.hasNext();) {
            Object markerId = it.next();

            //HARDY-WEINBERG
            int[] contingencyTable = (int[]) markersContingencyLHM.get(markerId);
            int obsAA = contingencyTable[0];
            int obsAa = contingencyTable[1];
            int obsaa = contingencyTable[2];
            int sampleNb = obsAA+obsaa+obsAa;
            double obsHzy = (double) obsAa/sampleNb;

            double fA = org.gwaspi.statistics.Utils.calculatePunnettFrequency(obsAA, obsAa, sampleNb);
            double fa = org.gwaspi.statistics.Utils.calculatePunnettFrequency(obsaa, obsAa, sampleNb);

            double pAA = fA*fA;
            double pAa = 2*fA*fa;
            double paa = fa*fa;

            double expAA = pAA*sampleNb;
            double expAa = pAa*sampleNb;
            double expaa = paa*sampleNb;
            double expHzy = pAa;

            double chiSQ = org.gwaspi.statistics.Chisquare.calculateHWChiSquare(obsAA, expAA, obsAa, expAa, obsaa, expaa);
            double pvalue = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(chiSQ, 1);

            Double[] store = new Double[3];
            store[0]=pvalue;
            store[1]=obsHzy;
            store[2]=expHzy;
            markersContingencyLHM.put(markerId, store); //Re-use AALHM to store P-value value

            markerNb++;
            if(markerNb%100000==0){
                System.out.println("Processed "+markerNb+" markers on catgetory: "+category+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }
        }
        System.out.println("Processed "+markerNb+" markers on catgetory: "+category+" at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

        //<editor-fold defaultstate="collapsed" desc="HARDY-WEINBERG DATA WRITER">

//        //ALL SAMPLES
//        if(category.equals("ALL")){
//            Utils.saveArrayDoubleItemD1ToWrMatrix(wrNcFile, markersContingencyLHM, 0, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL);
//            int[] boxes = new int[]{1,2};
//            Utils.saveArrayDoubleD2ToWrMatrix(wrNcFile, markersContingencyLHM, boxes, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALL);
//        }
//
//        //CASE SAMPLES
//        if(category.equals("CASE")){
//            Utils.saveArrayDoubleItemD1ToWrMatrix(wrNcFile, markersContingencyLHM, 0, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE);
//            int[] boxes = new int[]{1,2};
//            Utils.saveArrayDoubleD2ToWrMatrix(wrNcFile, markersContingencyLHM, boxes, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CASE);
//        }
//
        //CONTROL SAMPLES
        if(category.equals("CTRL")){
            Utils.saveDoubleLHMItemD1ToWrMatrix(wrNcFile, markersContingencyLHM, 0, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
            int[] boxes = new int[]{1,2};
            Utils.saveDoubleLHMD2ToWrMatrix(wrNcFile, markersContingencyLHM, boxes, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL);
        }

        //HW-ALT SAMPLES
        if(category.equals("HW-ALT")){
            Utils.saveDoubleLHMItemD1ToWrMatrix(wrNcFile, markersContingencyLHM, 0, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT);
            int[] boxes = new int[]{1,2};
            Utils.saveDoubleLHMD2ToWrMatrix(wrNcFile, markersContingencyLHM, boxes, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT);
        }
        //</editor-fold>

    }

}

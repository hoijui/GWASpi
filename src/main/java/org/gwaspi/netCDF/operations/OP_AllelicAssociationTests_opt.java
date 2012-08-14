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
import java.text.DecimalFormat;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixMetadata;

public class OP_AllelicAssociationTests_opt {

    public static int processMatrix(int _rdMatrixId,
                                    Operation markerCensusOP,
                                    Operation hwOP,
                                    double hwThreshold) throws IOException, InvalidRangeException{
        int resultAssocId = Integer.MIN_VALUE;

        //<editor-fold defaultstate="collapsed" desc="EXCLUSION MARKERS FROM HW">

        LinkedHashMap excludeMarkerSetLHM = new LinkedHashMap();
        int totalMarkerNb = 0;

        if (hwOP!=null) {
            OperationMetadata hwMetadata = new OperationMetadata(hwOP.getOperationId());
            NetcdfFile rdHWNcFile = NetcdfFile.open(hwMetadata.getPathToMatrix());
            OperationSet rdHWOperationSet = new OperationSet(hwMetadata.getStudyId(),hwMetadata.getOPId());
            LinkedHashMap rdHWMarkerSetLHM = rdHWOperationSet.getOpSetLHM();
            totalMarkerNb = rdHWMarkerSetLHM.size();

            //EXCLUDE MARKER BY HARDY WEINBERG THRESHOLD
            rdHWMarkerSetLHM = rdHWOperationSet.fillOpSetLHMWithVariable(rdHWNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
            for (Iterator it = rdHWMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                double value = (Double) rdHWMarkerSetLHM.get(key);
                if (value < hwThreshold) {
                    excludeMarkerSetLHM.put(key, value);
                }
            }

            if (rdHWMarkerSetLHM!=null) {
                rdHWMarkerSetLHM.clear();
            }
            rdHWNcFile.close();
        }

        //</editor-fold>

        if (excludeMarkerSetLHM.size() < totalMarkerNb) {  //CHECK IF THERE IS ANY DATA LEFT TO PROCESS AFTER PICKING
            OperationMetadata rdCensusOPMetadata = new OperationMetadata(markerCensusOP.getOperationId());
            NetcdfFile rdOPNcFile = NetcdfFile.open(rdCensusOPMetadata.getPathToMatrix());

            OperationSet rdCaseMarkerSet = new OperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getOperationId());
            OperationSet rdCtrlMarkerSet = new OperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getOperationId());
            LinkedHashMap rdSampleSetLHM = rdCaseMarkerSet.getImplicitSetLHM();
            LinkedHashMap rdCaseMarkerIdSetLHM = rdCaseMarkerSet.getOpSetLHM();
            LinkedHashMap rdCtrlMarkerIdSetLHM = rdCtrlMarkerSet.getOpSetLHM();
        
            LinkedHashMap wrMarkerSetLHM = new LinkedHashMap();
            for (Iterator it = rdCtrlMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                if (!excludeMarkerSetLHM.containsKey(key)) {
                    wrMarkerSetLHM.put(key, "");
                }
            }


            //GATHER INFO FROM ORIGINAL MATRIX
            MatrixMetadata parentMatrixMetadata = new MatrixMetadata(markerCensusOP.getParentMatrixId());
            MarkerSet_opt rdMarkerSet = new MarkerSet_opt(parentMatrixMetadata.getStudyId(),markerCensusOP.getParentMatrixId());
            rdMarkerSet.initFullMarkerIdSetLHM();

            //retrieve chromosome info
            rdMarkerSet.fillMarkerSetLHMWithChrAndPos();
            wrMarkerSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(wrMarkerSetLHM, rdMarkerSet.markerIdSetLHM);
            LinkedHashMap rdChrInfoSetLHM = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerSetLHM, 0, 1);

            

            NetcdfFileWriteable wrOPNcFile = null;
            try {
                ///////////// CREATE netCDF-3 FILE ////////////

                DecimalFormat dfSci = new DecimalFormat("0.##E0#");
                OperationFactory wrOPHandler = new OperationFactory(rdCensusOPMetadata.getStudyId(),
                                                                    "Allelic Association Test", //friendly name
                                                                    "Allelic test on " + markerCensusOP.getOperationFriendlyName() + "\n" + rdCensusOPMetadata.getDescription() + "\nHardy-Weinberg threshold: " + dfSci.format(hwThreshold), //description
                                                                    wrMarkerSetLHM.size(),
                                                                    rdCensusOPMetadata.getImplicitSetSize(),
                                                                    rdChrInfoSetLHM.size(),
                                                                    cNetCDF.Defaults.OPType.ALLELICTEST.toString(),
                                                                    rdCensusOPMetadata.getParentMatrixId(), //Parent matrixId
                                                                    markerCensusOP.getOperationId());       //Parent operationId
                wrOPNcFile = wrOPHandler.getNetCDFHandler();

                try {
                    wrOPNcFile.create();
                } catch (IOException e) {
                    System.err.println("ERROR creating file " + wrOPNcFile.getLocation() + "\n" + e);
                }
                //System.out.println("Done creating netCDF handle: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


                //<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
                //MARKERSET MARKERID
                ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(wrMarkerSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
                int[] markersOrig = new int[]{0, 0};
                try {
                    wrOPNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }

                //MARKERSET RSID
                rdCaseMarkerIdSetLHM = rdCaseMarkerSet.fillOpSetLHMWithVariable(rdOPNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
                for (Iterator it = wrMarkerSetLHM.keySet().iterator(); it.hasNext();) {
                    Object key = it.next();
                    Object value = rdCaseMarkerIdSetLHM.get(key);
                    wrMarkerSetLHM.put(key, value);
                }
                Utils.saveCharLHMValueToWrMatrix(wrOPNcFile, wrMarkerSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

                //WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
                ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

                int[] sampleOrig = new int[]{0, 0};
                try {
                    wrOPNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
                samplesD2 = null;
                System.out.println("Done writing SampleSet to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

                //WRITE CHROMOSOME INFO
                //Set of chromosomes found in matrix along with number of markersinfo
                org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrOPNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
                //Number of marker per chromosome & max pos for each chromosome
                int[] columns = new int[]{0,1,2,3};
                org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrOPNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);

                //</editor-fold>


                //<editor-fold defaultstate="collapsed" desc="GET CENSUS & PERFORM ALLELICTEST TESTS">

                //CLEAN LHMs FROM MARKERS THAT FAILED THE HARDY WEINBERG THRESHOLD
                LinkedHashMap wrCaseMarkerIdSetLHM = new LinkedHashMap();
                rdCaseMarkerIdSetLHM = rdCaseMarkerSet.fillOpSetLHMWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
                for (Iterator it = rdCaseMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
                    Object key = it.next();
                    Object value = rdCaseMarkerIdSetLHM.get(key);

                    if (!excludeMarkerSetLHM.containsKey(key)) {
                        wrCaseMarkerIdSetLHM.put(key, value);
                    }
                }
                if (rdCaseMarkerIdSetLHM!=null) {
                    rdCaseMarkerIdSetLHM.clear();
                }

                LinkedHashMap wrCtrlMarkerSet = new LinkedHashMap();
                rdCtrlMarkerIdSetLHM = rdCtrlMarkerSet.fillOpSetLHMWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
                for (Iterator it = rdCtrlMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
                    Object key = it.next();
                    Object value = rdCtrlMarkerIdSetLHM.get(key);

                    if (!excludeMarkerSetLHM.containsKey(key)) {
                        wrCtrlMarkerSet.put(key, value);
                    }
                }
                if (rdCtrlMarkerIdSetLHM!=null) {
                    rdCtrlMarkerIdSetLHM.clear();
                }
                
                System.out.println(org.gwaspi.global.Text.All.processing);
                performAssociationTests(wrOPNcFile, wrCaseMarkerIdSetLHM, wrCtrlMarkerSet);

                org.gwaspi.global.Utils.sysoutCompleted("Allelic Association Tests");
                //</editor-fold>

                resultAssocId = wrOPHandler.getResultOPId();

            } catch (InvalidRangeException invalidRangeException) {
            } catch (IOException iOException) {
            } finally {
                if (null != rdOPNcFile) {
                    try {
                        rdOPNcFile.close();
                        wrOPNcFile.close();
                    } catch (IOException ioe) {
                        System.err.println("Cannot close file: " + ioe);
                    }
                    
                }
            }
        } else {    //NO DATA LEFT AFTER THRESHOLD FILTER PICKING
            System.out.println(org.gwaspi.global.Text.Operation.warnNoDataLeftAfterPicking);
        }


        return resultAssocId;
    }


    protected static void performAssociationTests(NetcdfFileWriteable wrNcFile, LinkedHashMap wrCaseMarkerIdSetLHM, LinkedHashMap wrCtrlMarkerSet){
        //Iterate through markerset
        int markerNb=0;
        for (Iterator it = wrCaseMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
            Object markerId = it.next();

            int[] caseCntgTable = (int[]) wrCaseMarkerIdSetLHM.get(markerId);
            int[] ctrlCntgTable = (int[]) wrCtrlMarkerSet.get(markerId);

            //INIT VALUES
            
            int caseAA = caseCntgTable[0];
            int caseAa = caseCntgTable[1];
            int caseaa = caseCntgTable[2];
            int caseTot = caseAA+caseaa+caseAa;
            
            int ctrlAA = ctrlCntgTable[0];
            int ctrlAa = ctrlCntgTable[1];
            int ctrlaa = ctrlCntgTable[2];
            int ctrlTot = ctrlAA+ctrlaa+ctrlAa;
            



            int AAtot = caseAA+ctrlAA;
            int Aatot = caseAa+ctrlAa;
            int aatot = caseaa+ctrlaa;
            
            int sampleNb = caseTot+ctrlTot;
            
            double allelicT = org.gwaspi.statistics.Associations.calculateAllelicAssociationChiSquare(sampleNb,
                                                                               caseAA,
                                                                               caseAa,
                                                                               caseaa,
                                                                               caseTot,
                                                                               ctrlAA,
                                                                               ctrlAa,
                                                                               ctrlaa,
                                                                               ctrlTot);
            double allelicPval = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(allelicT, 1);

            double allelicOR = org.gwaspi.statistics.Associations.calculateAllelicAssociationOR(caseAA,
                                                                               caseAa,
                                                                               caseaa,
                                                                               ctrlAA,
                                                                               ctrlAa,
                                                                               ctrlaa);


            Double[] store = new Double[3];
            store[0]=allelicT;
            store[1]=allelicPval;
            store[2]=allelicOR;
            wrCaseMarkerIdSetLHM.put(markerId, store); //Re-use LHM to store P-value and stuff

            markerNb++;
            if(markerNb%100000==0){
                System.out.println("Processed "+markerNb+" markers at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
            }
        }


        //<editor-fold defaultstate="collapsed" desc="ALLELICTEST DATA WRITER">
        int[] boxes = new int[]{0,1,2};
        Utils.saveDoubleLHMD2ToWrMatrix(wrNcFile, wrCaseMarkerIdSetLHM, boxes, cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR);
        //</editor-fold>

    }

}

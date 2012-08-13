package netCDF.operations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import constants.cNetCDF;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import netCDF.markers.MarkerSet_opt;
import netCDF.matrices.*;
import samples.SampleSet;
import ucar.ma2.*;
import ucar.nc2.*;
import constants.cNetCDF.*;
import constants.cNetCDF.Defaults.AlleleBytes;
import java.util.List;
import java.util.Map;
import netCDF.operations.CensusMethod.CensusDecision;

public class OP_QAMarkers_opt {

    public static int processMatrix(int rdMatrixId) throws IOException, InvalidRangeException{
        int resultOpId = Integer.MIN_VALUE;

        LinkedHashMap wrMarkerSetMismatchStateLHM = new LinkedHashMap();
        LinkedHashMap wrMarkerSetCensusLHM = new LinkedHashMap();
        LinkedHashMap wrMarkerSetMissingRatioLHM = new LinkedHashMap();
        LinkedHashMap wrMarkerSetKnownAllelesLHM = new LinkedHashMap();

        MatrixMetadata rdMatrixMetadata = new MatrixMetadata(rdMatrixId);

        NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

        MarkerSet_opt rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(),rdMatrixId);
        rdMarkerSet.initFullMarkerIdSetLHM();
        //LinkedHashMap rdMarkerSetLHM = rdMarkerSet.markerIdSetLHM; //This to test heap usage of copying locally the LHM from markerset

        SampleSet rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(),rdMatrixId);
        LinkedHashMap rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();

        NetcdfFileWriteable wrNcFile = null;
        try {
            ///////////// CREATE netCDF-3 FILE ////////////
            String description = "Marker Quality Assurance on "+
                                 rdMatrixMetadata.getMatrixFriendlyName()+
                                 "\nMarkers: "+rdMarkerSet.markerIdSetLHM.size()+
                                 "\nStarted at: "+global.Utils.getShortDateTimeAsString();
            OperationFactory wrOPHandler = new OperationFactory(rdMatrixMetadata.getStudyId(),
                                                "Marker QA", //friendly name
                                                description, //description
                                                rdMarkerSet.markerIdSetLHM.size(),
                                                rdSampleSetLHM.size(),
                                                0,
                                                cNetCDF.Defaults.OPType.MARKER_QA.toString(),
                                                rdMatrixMetadata.getMatrixId(), //Parent matrixId
                                                -1);       //Parent operationId

            wrNcFile = wrOPHandler.getNetCDFHandler();
            try {
                wrNcFile.create();
            } catch (IOException e) {
                System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
            }
            //System.out.println("Done creating netCDF handle: " + global.Utils.getMediumDateTimeAsString());


            //<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
            //MARKERSET MARKERID
            ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdMarkerSet.markerIdSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
            int[] markersOrig = new int[]{0, 0};
            try {
                wrNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
            } catch (IOException e) {
                System.err.println("ERROR writing file");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }

            //MARKERSET RSID
            rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
            Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

            //WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
            ArrayChar.D2 samplesD2 = netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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


            //<editor-fold defaultstate="collapsed" desc="PROCESSOR">

            //INIT MARKER AND SAMPLE INFO
            rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);

            LinkedHashMap samplesInfoLHM = null;
            List<Map<String, Object>> rsSamplesInfo = samples.SampleManager.getAllSampleInfoFromDBByPoolID(rdMatrixMetadata.getStudyId());
            samplesInfoLHM = new LinkedHashMap();
            int count = 0;
            while (count < rsSamplesInfo.size()) {
                //PREVENT PHANTOM-DB READS EXCEPTIONS
                if(!rsSamplesInfo.isEmpty() && rsSamplesInfo.get(count).size()==constants.cDBSamples.T_CREATE_SAMPLES_INFO.length){
                    String tempSampleId = rsSamplesInfo.get(count).get(constants.cDBSamples.f_SAMPLE_ID).toString();
                    if (rdSampleSetLHM.containsKey(tempSampleId)) {
                        String sex = "0";
                        Object tmpSex = rsSamplesInfo.get(count).get(constants.cDBSamples.f_SEX);
                        if (tmpSex != null) {
                            sex = tmpSex.toString();
                        }
                        samplesInfoLHM.put(tempSampleId, sex);
                    }
                }
                count++;
            }
            rsSamplesInfo.clear();

            //Iterate through markerset, take it marker by marker
            int markerNb=0;
            for (Iterator it = rdMarkerSet.markerIdSetLHM.keySet().iterator(); it.hasNext();) {
                Object markerId = it.next();

                LinkedHashMap knownAlleles = new LinkedHashMap();
                LinkedHashMap allSamplesGTsTable = new LinkedHashMap();
                LinkedHashMap allSamplesContingencyTable = new LinkedHashMap();
                Integer missingCount = 0;
                

                //Get a sampleset-full of GTs
                rdSampleSetLHM = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, rdSampleSetLHM, markerNb);
                for (Iterator it2 = rdSampleSetLHM.keySet().iterator(); it2.hasNext();) {

                    Object sampleId = it2.next();

                    //<editor-fold defaultstate="expanded" desc="THE DECIDER">
                    CensusDecision decision = CensusDecision.getDecisionByChrAndSex(rdMarkerSet.markerIdSetLHM.get(markerId).toString(), samplesInfoLHM.get(sampleId).toString());
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="SUMMING SAMPLESET GENOTYPES">
                    float counter = 1;
                    byte[] tempGT = (byte[]) rdSampleSetLHM.get(sampleId);
                    //Gather alleles different from 0 into a list of known alleles and count the number of appearences
                    //48 is byte for 0
                    //65 is byte for A
                    //67 is byte for C
                    //71 is byte for G
                    //84 is byte for T

                    if(tempGT[0]!=AlleleBytes._0){
                        float tempCount=0;
                        if(knownAlleles.containsKey(tempGT[0])){
                            tempCount =  (Float) knownAlleles.get(tempGT[0]);
                        }
                        knownAlleles.put(tempGT[0], tempCount+counter);
                    }
                    if(tempGT[1]!=AlleleBytes._0){ //48 is byte for 0
                        float tempCount=0;
                        if(knownAlleles.containsKey(tempGT[1])){
                            tempCount =  (Float) knownAlleles.get(tempGT[1]);
                        }
                        knownAlleles.put(tempGT[1], tempCount+counter);
                    }
                    if(tempGT[0]==AlleleBytes._0 && tempGT[1]==AlleleBytes._0){
                        if(decision != CensusDecision.CountFemalesNonAutosomally){
                            missingCount++;
                        }
                    }

                    
                    Integer intAlleleSum = tempGT[0]+tempGT[1]; //2 alleles per GT

                    float tempCount=0;
                    if(allSamplesGTsTable.containsKey(intAlleleSum)){
                        tempCount =  (Float) allSamplesGTsTable.get(intAlleleSum);
                    }
                    allSamplesGTsTable.put(intAlleleSum, tempCount+counter);

                    //</editor-fold>
                }

                //ALLELE CENSUS + MISMATCH STATE + MISSINGNESS

                Object[] orderedAlleles = new Object[4];
                if(knownAlleles.size()<=2){ //Check if there are mismatches in alleles

                    //<editor-fold defaultstate="collapsed" desc="KNOW YOUR ALLELES">
                    ArrayList intAA=new ArrayList();
                    ArrayList intAa=new ArrayList();
                    ArrayList intaa=new ArrayList();

                    Iterator itKnAll = knownAlleles.keySet().iterator();
                    if(knownAlleles.size()==0){ //Completely missing (00)
                        orderedAlleles[0]='0';
                        orderedAlleles[1]=0d;
                        orderedAlleles[2]='0';
                        orderedAlleles[3]=0d;
                    }
                    if(knownAlleles.size()==1){ //Homozygote (AA or aa)
                        byte byteAllele1 = (Byte) itKnAll.next();
                        byte byteAllele2 = '0';
                        int intAllele1 = byteAllele1;
                        intAA.add(intAllele1);
                        intAA.add(intAllele1*2);

                        orderedAlleles[0]= new String(new byte[]{byteAllele1});
                        orderedAlleles[1]=1d;
                        orderedAlleles[2]= new String(new byte[]{byteAllele2});
                        orderedAlleles[3]=0d;
                    }
                    if(knownAlleles.size()==2){ //Heterezygote (contains mix of AA, Aa or aa)
                        byte byteAllele1 = (Byte) itKnAll.next();
                        int countAllele1 = Math.round((Float) knownAlleles.get(byteAllele1));
                        int intAllele1 = byteAllele1;
                        byte byteAllele2 = (Byte) itKnAll.next();
                        int countAllele2 = Math.round((Float) knownAlleles.get(byteAllele2));
                        int intAllele2 = byteAllele2;
                        int totAlleles = countAllele1+countAllele2;

                        if(countAllele1>=countAllele2){ //Finding out what allele is major and minor
                            intAA.add(intAllele1);
                            intAA.add(intAllele1*2);

                            intaa.add(intAllele2);
                            intaa.add(intAllele2*2);

                            intAa.add(intAllele1+intAllele2);

                            orderedAlleles[0]= new String(new byte[]{byteAllele1});
                            orderedAlleles[1]=(double) countAllele1/totAlleles;
                            orderedAlleles[2]= new String(new byte[]{byteAllele2});
                            orderedAlleles[3]=(double) countAllele2/totAlleles;
                        } else {
                            intAA.add(intAllele2);
                            intAA.add(intAllele2*2);

                            intaa.add(intAllele1);
                            intaa.add(intAllele1*2);

                            intAa.add(intAllele1+intAllele2);

                            orderedAlleles[0]= new String(new byte[]{byteAllele2});
                            orderedAlleles[1]=(double) countAllele2/totAlleles;
                            orderedAlleles[2]= new String(new byte[]{byteAllele1});
                            orderedAlleles[3]=(double) countAllele1/totAlleles;
                        }
                    }
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="CONTINGENCY ALL SAMPLES">
                    for (Iterator itUnqGT = allSamplesGTsTable.keySet().iterator(); itUnqGT.hasNext();) {
                        Object key = itUnqGT.next();
                        Integer value = Math.round((Float) allSamplesGTsTable.get(key));

                        if(intAA.contains(key)){ //compare to all possible character values of AA
                            //ALL CENSUS
                            int tempCount = 0;
                            if(allSamplesContingencyTable.containsKey("AA")){
                                tempCount = (Integer) allSamplesContingencyTable.get("AA");
                            }
                            allSamplesContingencyTable.put("AA", tempCount+value);
                        }
                        if(intAa.contains(key)){ //compare to all possible character values of Aa
                            //ALL CENSUS
                            int tempCount = 0;
                            if(allSamplesContingencyTable.containsKey("Aa")){
                                tempCount = (Integer) allSamplesContingencyTable.get("Aa");
                            }
                            allSamplesContingencyTable.put("Aa", tempCount+value);
                        }
                        if(intaa.contains(key)){ //compare to all possible character values of aa
                            //ALL CENSUS
                            int tempCount = 0;
                            if(allSamplesContingencyTable.containsKey("aa")){
                                tempCount = (Integer) allSamplesContingencyTable.get("aa");
                            }
                            allSamplesContingencyTable.put("aa", tempCount+value);
                        }
                    }
                    //</editor-fold>

                    //CENSUS
                    int obsAllAA = 0;
                    int obsAllAa = 0;
                    int obsAllaa = 0;
                    int obsCaseAA = 0;
                    int obsCaseAa = 0;
                    int obsCaseaa = 0;
                    int obsCntrlAA = 0;
                    int obsCntrlAa = 0;
                    int obsCntrlaa = 0;
                    if(allSamplesContingencyTable.containsKey("AA")){
                        obsAllAA = (Integer) allSamplesContingencyTable.get("AA");
                    }
                    if(allSamplesContingencyTable.containsKey("Aa")){
                        obsAllAa = (Integer) allSamplesContingencyTable.get("Aa");
                    }
                    if(allSamplesContingencyTable.containsKey("aa")){
                        obsAllaa = (Integer) allSamplesContingencyTable.get("aa");
                    }

                    int[] census = new int[4];

                    census[0] = obsAllAA; //all
                    census[1] = obsAllAa; //all
                    census[2] = obsAllaa; //all
                    census[3] = missingCount; //all


                    wrMarkerSetCensusLHM.put(markerId, census);
                    wrMarkerSetMismatchStateLHM.put(markerId, cNetCDF.Defaults.DEFAULT_MISMATCH_NO);

                    if(orderedAlleles[0]==null && orderedAlleles[2]!=null){
                        orderedAlleles[0]=orderedAlleles[2];
                    }
                    if(orderedAlleles[2]==null && orderedAlleles[0]!=null){
                        orderedAlleles[2]=orderedAlleles[0];
                    }
                    if(orderedAlleles[0]==null && orderedAlleles[2]==null){
                        orderedAlleles[0]='0';
                        orderedAlleles[2]='0';
                    }
                    wrMarkerSetKnownAllelesLHM.put(markerId, orderedAlleles);
                } else {
                    int[] census = new int[4];
                    wrMarkerSetCensusLHM.put(markerId, census);
                    wrMarkerSetMismatchStateLHM.put(markerId, cNetCDF.Defaults.DEFAULT_MISMATCH_YES);

                    orderedAlleles[0]='0';
                    orderedAlleles[1]=0d;
                    orderedAlleles[2]='0';
                    orderedAlleles[3]=0d;
                    wrMarkerSetKnownAllelesLHM.put(markerId, orderedAlleles);
                }

                double missingRatio = (double) missingCount/rdSampleSet.getSampleSetSize();
                wrMarkerSetMissingRatioLHM.put(markerId, missingRatio);

                markerNb++;
                if(markerNb==1){
                    System.out.println(global.Text.All.processing);
                } else if(markerNb%100000==0){
                    System.out.println("Processed markers: "+markerNb+" at " + global.Utils.getMediumDateTimeAsString());
                }
            }
            //</editor-fold>


            //<editor-fold defaultstate="collapsed" desc="QA DATA WRITER">
            //MISSING RATIO
            Utils.saveDoubleLHMD1ToWrMatrix(wrNcFile, wrMarkerSetMissingRatioLHM, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);

            //MISMATCH STATE
            Utils.saveIntLHMD1ToWrMatrix(wrNcFile, wrMarkerSetMismatchStateLHM, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);

            //KNOWN ALLELES
            //Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesLHM, cNetCDF.Census.VAR_OP_MARKERS_KNOWNALLELES, cNetCDF.Strides.STRIDE_GT);
            Utils.saveCharLHMItemToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesLHM, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, 0, cNetCDF.Strides.STRIDE_GT/2);
            Utils.saveDoubleLHMItemD1ToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesLHM, 1, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);
            Utils.saveCharLHMItemToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesLHM, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, 2, cNetCDF.Strides.STRIDE_GT/2);
            Utils.saveDoubleLHMItemD1ToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesLHM, 3, cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);

            //ALL CENSUS
            int[] columns = new int[]{0,1,2,3};
            Utils.saveIntLHMD2ToWrMatrix(wrNcFile, wrMarkerSetCensusLHM, columns, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);

            //</editor-fold>

            resultOpId = wrOPHandler.getResultOPId();
        } finally {
            if (null != rdNcFile) try {
                rdNcFile.close();
                wrNcFile.close();
            } catch (IOException ioe) {
                System.err.println("Cannot close file: "+ioe);
            }

            global.Utils.sysoutCompleted("Marker QA");
            return resultOpId;
        }


    }
}
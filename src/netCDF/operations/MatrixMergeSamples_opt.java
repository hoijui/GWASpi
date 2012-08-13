
package netCDF.operations;

import constants.cNetCDF;
import database.DbManager;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import netCDF.markers.MarkerSet_opt;
import netCDF.matrices.MatrixFactory;
import netCDF.matrices.MatrixMetadata;
import samples.SampleSet;
import ucar.ma2.*;
import ucar.nc2.*;
import constants.cNetCDF.*;
import global.ServiceLocator;
import global.Text;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixMergeSamples_opt {

    protected int studyId = Integer.MIN_VALUE;
    protected int rdMatrix1Id = Integer.MIN_VALUE;
    protected int rdMatrix2Id = Integer.MIN_VALUE;
    protected int wrMatrixId = Integer.MIN_VALUE;
    protected String wrMatrixFriendlyName="";
    protected String wrMatrixDescription="";

    MatrixMetadata rdMatrix1Metadata = null;
    MatrixMetadata rdMatrix2Metadata = null;
    MatrixMetadata wrMatrixMetadata = null;

    MarkerSet_opt rdwrMarkerSet1 = null;
    MarkerSet_opt rdMarkerSet2 = null;
    MarkerSet_opt wrMarkerSet = null;

    SampleSet rdSampleSet1 = null;
    SampleSet rdSampleSet2 = null;
    SampleSet wrSampleSet = null;


    protected static DbManager dBManager = null;

    /**
    * This constructor to join 2 Matrices.
    * The MarkerSet from the 1st Matrix will be used in the result Matrix.
    * No new Markers from the 2nd Matrix will be added.
    * Samples from the 2nd Matrix will be appended to the end of the SampleSet from the 1st Matrix.
    * Duplicate Samples from the 2nd Matrix will overwrite Samples in the 1st Matrix
    */
    public MatrixMergeSamples_opt(int _studyId,
                           int _rdMatrix1Id,
                           int _rdMatrix2Id,
                           String _wrMatrixFriendlyName,
                           String _wrMatrixDescription) throws IOException, InvalidRangeException{

        /////////// INIT EXTRACTOR OBJECTS //////////
        rdMatrix1Metadata = new MatrixMetadata(_rdMatrix1Id);
        rdMatrix2Metadata = new MatrixMetadata(_rdMatrix2Id);
        studyId = rdMatrix1Metadata.getStudyId();

        rdMatrix1Id = _rdMatrix1Id;
        rdMatrix2Id = _rdMatrix2Id;

        rdMatrix1Metadata = new MatrixMetadata(rdMatrix1Id);
        rdMatrix2Metadata = new MatrixMetadata(rdMatrix2Id);

        rdwrMarkerSet1 = new MarkerSet_opt(rdMatrix1Metadata.getStudyId(),rdMatrix1Id);
        rdMarkerSet2 = new MarkerSet_opt(rdMatrix2Metadata.getStudyId(),rdMatrix2Id);

        rdSampleSet1 = new SampleSet(rdMatrix1Metadata.getStudyId(),rdMatrix1Id);
        rdSampleSet2 = new SampleSet(rdMatrix2Metadata.getStudyId(),rdMatrix2Id);

        wrMatrixFriendlyName = _wrMatrixFriendlyName;
        wrMatrixDescription = _wrMatrixDescription;

    }

    public int appendSamplesKeepMarkersConstant() throws IOException, InvalidRangeException{
        int resultMatrixId= Integer.MIN_VALUE;
        
        NetcdfFile rdNcFile1 = NetcdfFile.open(rdMatrix1Metadata.getPathToMatrix());
        NetcdfFile rdNcFile2 = NetcdfFile.open(rdMatrix2Metadata.getPathToMatrix());


        //Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
        LinkedHashMap rdSampleSetLHM1 = rdSampleSet1.getSampleIdSetLHM();
        LinkedHashMap rdSampleSetLHM2 = rdSampleSet2.getSampleIdSetLHM();
        LinkedHashMap wrComboSampleSetLHM = getComboSampleSetwithPosArray(rdSampleSetLHM1, rdSampleSetLHM2);

        rdwrMarkerSet1.initFullMarkerIdSetLHM();
        rdMarkerSet2.initFullMarkerIdSetLHM();

        //RETRIEVE CHROMOSOMES INFO
//        rdwrMarkerSet1.fillMarkerSetLHMWithChrAndPos();
//        wrMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(wrMarkerIdSetLHM, rdMarkerSet.markerIdSetLHM);
//        rdChrInfoSetLHM = netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerIdSetLHM, 0, 1);

        LinkedHashMap rdChrInfoSetLHM = rdwrMarkerSet1.getChrInfoSetLHM();
        

        try {
            ///////////// CREATE netCDF-3 FILE ////////////
            int hasDictionary = 0;
            if(rdMatrix1Metadata.getHasDictionray()==rdMatrix2Metadata.getHasDictionray()){
                hasDictionary = rdMatrix1Metadata.getHasDictionray();
            }
            String gtEncoding = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN.toString();
            if(rdMatrix1Metadata.getGenotypeEncoding().equals(rdMatrix2Metadata.getGenotypeEncoding())){
                gtEncoding = rdMatrix1Metadata.getGenotypeEncoding();
            }
            String technology = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN.toString();
            if(rdMatrix1Metadata.getTechnology().equals(rdMatrix2Metadata.getTechnology())){
                technology = rdMatrix1Metadata.getTechnology();
            }

            StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
            descSB.append(global.Utils.getShortDateTimeAsString());
            descSB.append("\n");
            descSB.append("Markers: ").append(rdSampleSetLHM1.size()).append(", Samples: ").append(wrComboSampleSetLHM.size());
            descSB.append("\n");
            descSB.append(Text.Trafo.mergedFrom);
            descSB.append("\nMX-");
            descSB.append(rdMatrix1Metadata.getMatrixId());
            descSB.append(" - ");
            descSB.append(rdMatrix1Metadata.getMatrixFriendlyName());
            descSB.append("\nMX-");
            descSB.append(rdMatrix2Metadata.getMatrixId());
            descSB.append(" - ");
            descSB.append(rdMatrix2Metadata.getMatrixFriendlyName());
            descSB.append("\n\n");
            descSB.append("Merge Method - ");
            descSB.append(Text.Trafo.mergeSamplesOnly);
            descSB.append(":\n");
            descSB.append(Text.Trafo.mergeMethodSampleJoin);

            MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
                    technology, //technology
                    wrMatrixFriendlyName,
                    wrMatrixDescription+"\n\n"+descSB.toString(), //description
                    rdMatrix1Metadata.getStrand(),
                    hasDictionary, //has dictionary?
                    wrComboSampleSetLHM.size(), //Use comboed wrComboSampleSetLHM as SampleSet
                    rdwrMarkerSet1.getMarkerSetSize(), //Keep rdwrMarkerIdSetLHM1 from Matrix1. MarkerSet is constant
                    rdChrInfoSetLHM.size(),
                    gtEncoding,
                    rdMatrix1Id,          //Parent matrixId 1
                    rdMatrix2Id);         //Parent matrixId 2


            NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
            try {
                wrNcFile.create();
            } catch (IOException e) {
                System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
            }
            //System.out.println("Done creating netCDF handle in MatrixSampleJoin_opt: " + global.Utils.getMediumDateTimeAsString());


            //<editor-fold defaultstate="collapsed" desc="METADATA WRITER">

            //SAMPLESET
            ArrayChar.D2 samplesD2 = Utils.writeLHMKeysToD2ArrayChar(wrComboSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

            int[] sampleOrig = new int[]{0, 0};
            try {
                wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
            } catch (IOException e) {
                System.err.println("ERROR writing file");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
            samplesD2 = null;
            System.out.println("Done writing SampleSet to matrix at " + global.Utils.getMediumDateTimeAsString());


            //Keep rdwrMarkerIdSetLHM1 from Matrix1 constant
            //MARKERSET MARKERID
            ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdwrMarkerSet1.markerIdSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
            int[] markersOrig = new int[]{0, 0};
            try {
                wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
            } catch (IOException e) {
                System.err.println("ERROR writing file");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }

            //MARKERSET RSID
            rdwrMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
            Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdwrMarkerSet1.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

            //MARKERSET CHROMOSOME
            rdwrMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
            Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdwrMarkerSet1.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

            //Set of chromosomes found in matrix along with number of markersinfo
            netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
            //Number of marker per chromosome & max pos for each chromosome
            int[] columns = new int[]{0,1,2,3};
            netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);


            //MARKERSET POSITION
            rdwrMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
            //Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdwrMarkerIdSetLHM1, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
            Utils.saveIntLHMD1ToWrMatrix(wrNcFile, rdwrMarkerSet1.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS);


            //MARKERSET DICTIONARY ALLELES
            Attribute hasDictionary1 = rdNcFile1.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY);
            if ((Integer) hasDictionary1.getNumericValue()==1) {
                rdwrMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdwrMarkerSet1.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);
            }

            //GENOTYPE STRAND
            rdwrMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
            Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdwrMarkerSet1.markerIdSetLHM, cNetCDF.Variables.VAR_GT_STRAND, 3);

            //</editor-fold>


            //<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

            rdMarkerSet2.initFullMarkerIdSetLHM();

            //Iterate through wrSampleSetLHM, use item position to read correct sample GTs into rdMarkerIdSetLHM.
            for (Iterator it = wrComboSampleSetLHM.keySet().iterator(); it.hasNext();) {
                Object sampleId = it.next();                 //Next SampleId
                int[] sampleIndices = (int[]) wrComboSampleSetLHM.get(sampleId); //Next position[rdMatrixNb, rdPos, wrPos] to read/write

                //Iterate through wrMarkerIdSetLHM, get the correct GT from rdMarkerIdSetLHM
                if(sampleIndices[0]==1){ //Read from Matrix1
                    rdwrMarkerSet1.fillInitLHMWithMyValue(constants.cNetCDF.Defaults.DEFAULT_GT);
                    rdwrMarkerSet1.fillGTsForCurrentSampleIntoInitLHM(sampleIndices[1]);
                }
                if(sampleIndices[0]==2){ //Read from Matrix2
                    rdwrMarkerSet1.fillInitLHMWithMyValue(constants.cNetCDF.Defaults.DEFAULT_GT);
                    rdMarkerSet2.fillGTsForCurrentSampleIntoInitLHM(sampleIndices[1]);
                    for (Iterator it3 = rdwrMarkerSet1.markerIdSetLHM.keySet().iterator(); it3.hasNext();) {
                        Object key = it3.next();
                        if(rdMarkerSet2.markerIdSetLHM.containsKey(key)){
                            Object value = rdMarkerSet2.markerIdSetLHM.get(key);
                            rdwrMarkerSet1.markerIdSetLHM.put(key, value);
                        }
                    }
                    //rdwrMarkerIdSetLHM1 = rdMarkerSet2.fillWrLHMWithRdLHMValue(rdwrMarkerIdSetLHM1, rdMarkerIdSetLHM2);
                }

                //Write wrMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
                Utils.saveSingleSampleGTsToMatrix(wrNcFile, rdwrMarkerSet1.markerIdSetLHM, sampleIndices[2]);

            }

            //</editor-fold>


            // CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
            try {
                    //GENOTYPE ENCODING
                    ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1,8);
                    Index index = guessedGTCodeAC.getIndex();
                    guessedGTCodeAC.setString(index.set(0,0),rdMatrix1Metadata.getGenotypeEncoding());
                    int[] origin = new int[]{0,0};
                    wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

                    descSB.append("\nGenotype encoding: ");
                    descSB.append(rdMatrix1Metadata.getGenotypeEncoding());
                    DbManager db = ServiceLocator.getDbManager(constants.cDBGWASpi.DB_DATACENTER);
                    db.updateTable(constants.cDBGWASpi.SCH_MATRICES,
                            constants.cDBMatrix.T_MATRICES,
                            new String[]{constants.cDBMatrix.f_DESCRIPTION},
                            new Object[]{descSB.toString()},
                            new String[]{constants.cDBMatrix.f_ID},
                            new Object[]{resultMatrixId});
                
                resultMatrixId = wrMatrixHandler.getResultMatrixId();

                wrNcFile.close();
                rdNcFile1.close();
                rdNcFile2.close();
                
                //CHECK FOR MISMATCHES
                if(rdMatrix1Metadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString())
                   || rdMatrix1Metadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.O1234.toString())){
                    double[] mismatchState = checkForMismatches(wrMatrixHandler.getResultMatrixId()); //mismatchCount, mismatchRatio
                    if(mismatchState[1]>0.01){
                        System.out.println("Mismatch ratio is bigger that 1% ("+mismatchState[1]*100+" %)!\nThere might be an issue with strand positioning of your genotypes!");
                        //resultMatrixId = new int[]{wrMatrixHandler.getResultMatrixId(),-4};  //The threshold of acceptable mismatching genotypes has been crossed
                    }
                }

            } catch (IOException e) {
                System.err.println("ERROR creating file "+wrNcFile.getLocation()+"\n"+e);
            }

            global.Utils.sysoutCompleted("extraction to new Matrix");

        } catch (InvalidRangeException invalidRangeException) {
        } catch (IOException iOException) {
        }


        return resultMatrixId;
    }


    protected LinkedHashMap getComboSampleSetwithPosArray(LinkedHashMap sampleSetLHM1, LinkedHashMap sampleSetLHM2){
        LinkedHashMap resultLHM = new LinkedHashMap();

        int wrPos = 0;
        int rdPos = 0;
        for (Iterator it = sampleSetLHM1.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            int[] position = new int[]{1,rdPos,wrPos}; //rdMatrixNb, rdPos, wrPos
            resultLHM.put(key, position);
            wrPos++;
            rdPos++;
        }

        rdPos = 0;
        for (Iterator it = sampleSetLHM2.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            int[] position = new int[3];
            //IF SAMPLE ALLREADY EXISTS IN MATRIX1 SUBSTITUTE VALUES WITH MATRIX2
            if(resultLHM.containsKey(key)){
                position = (int[]) resultLHM.get(key);
                position[0]=2; //rdMatrixNb
                position[1]=rdPos; //rdPos
            } else {
                position = new int[]{2,rdPos,wrPos}; //rdMatrixNb, rdPos, wrPos
            }
            
            resultLHM.put(key, position);
            wrPos++;
            rdPos++;
        }

        return resultLHM;
    }


    protected double[] checkForMismatches(int wrMatrixId) throws IOException, InvalidRangeException{
        double[] result = new double[2];

        wrMatrixMetadata = new MatrixMetadata(wrMatrixId);
        wrSampleSet = new SampleSet(wrMatrixMetadata.getStudyId(),wrMatrixId);
        wrMarkerSet = new MarkerSet_opt(wrMatrixMetadata.getStudyId(),wrMatrixId);
        wrMarkerSet.initFullMarkerIdSetLHM();
        LinkedHashMap wrSampleSetLHM = wrSampleSet.getSampleIdSetLHM();

        NetcdfFile rdNcFile = NetcdfFile.open(wrMatrixMetadata.getPathToMatrix());

        //Iterate through markerset, take it marker by marker
        int markerNb=0;
        double mismatchCount = 0;
        double mismatchRatio = 0;

        //Iterate through markerSet
        for (Iterator it = wrMarkerSet.markerIdSetLHM.keySet().iterator(); it.hasNext();) {
            Object markerId = it.next();
            LinkedHashMap knownAlleles = new LinkedHashMap();

            //Get a sampleset-full of GTs
            wrSampleSetLHM = wrSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, wrSampleSetLHM, markerNb);
            
            //Iterate through sampleSet
            for (Iterator it2 = wrSampleSetLHM.keySet().iterator(); it2.hasNext();) {

                Object sampleId = it2.next();

                char[] tempGT = wrSampleSetLHM.get(sampleId).toString().toCharArray();

                //Gather alleles different from 0 into a list of known alleles and count the number of appearences
                if(tempGT[0]!='0'){
                    int tempCount=0;
                    if(knownAlleles.containsKey(tempGT[0])){
                        tempCount =  (Integer) knownAlleles.get(tempGT[0]);
                    }
                    knownAlleles.put(tempGT[0], tempCount+1);
                }
                if(tempGT[1]!='0'){
                    int tempCount=0;
                    if(knownAlleles.containsKey(tempGT[1])){
                        tempCount =  (Integer) knownAlleles.get(tempGT[1]);
                    }
                    knownAlleles.put(tempGT[1], tempCount+1);
                }
            }

            if(knownAlleles.size()>2){
                mismatchCount++;
            }

            markerNb++;
            if(markerNb%100000==0){
                System.out.println("Checking markers for mismatches: "+markerNb+" at " + global.Utils.getMediumDateTimeAsString());
            }
        }

        mismatchRatio = (double) mismatchCount/wrSampleSet.getSampleSetSize();
        result[0]=mismatchCount;
        result[1]=mismatchRatio;

        return result;
    }



}


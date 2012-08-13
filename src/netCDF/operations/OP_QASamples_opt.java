package netCDF.operations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import constants.cNetCDF;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import netCDF.matrices.*;
import samples.SampleSet;
import ucar.ma2.*;
import ucar.nc2.*;
import constants.cNetCDF.*;
import constants.cNetCDF.Defaults.AlleleBytes;
import netCDF.markers.MarkerSet_opt;

public class OP_QASamples_opt {

    public static int processMatrix(int rdMatrixId) throws IOException, InvalidRangeException{
        int resultOpId = Integer.MIN_VALUE;

        LinkedHashMap wrSampleSetMissingCountLHM = new LinkedHashMap();
        LinkedHashMap wrSampleSetMissingRatioLHM = new LinkedHashMap();
        LinkedHashMap wrSampleSetHetzyRatioLHM = new LinkedHashMap();
        LinkedHashMap rdChrSetLHM = new LinkedHashMap();

        MatrixMetadata rdMatrixMetadata = new MatrixMetadata(rdMatrixId);

        NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

        MarkerSet_opt rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(),rdMatrixId);
        rdMarkerSet.initFullMarkerIdSetLHM();

        rdChrSetLHM = rdMarkerSet.getChrInfoSetLHM();

        //LinkedHashMap rdMarkerSetLHM = rdMarkerSet.markerIdSetLHM; //This to test heap usage of copying locally the LHM from markerset

        SampleSet rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(),rdMatrixId);
        LinkedHashMap rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();

        //Iterate through samples
        int sampleNb=0;
        for (Iterator it = rdSampleSetLHM.keySet().iterator(); it.hasNext();) {
            Object sampleId = it.next();

            Integer missingCount = 0;
            Integer heterozygCount = 0;

            //Iterate through markerset
            rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleNb);
            int markerIndex = 0;
            for (Iterator it2 = rdMarkerSet.markerIdSetLHM.keySet().iterator(); it2.hasNext();) {
                Object markerId = it2.next();
                byte[] tempGT = (byte[]) rdMarkerSet.markerIdSetLHM.get(markerId);
                if(tempGT[0]==AlleleBytes._0 && tempGT[1]==AlleleBytes._0){
                    missingCount++;
                }

                //WE DON'T WANT NON AUTOSOMAL CHR FOR HETZY
                String currentChr = MarkerSet_opt.getChrByMarkerIndex(rdChrSetLHM, markerIndex);
                if (!currentChr.equals("X") &&
                    !currentChr.equals("Y") &&
                    !currentChr.equals("XY") &&
                    !currentChr.equals("MT")) {
                    if (tempGT[0] != tempGT[1]) {
                        heterozygCount++;
                    }
                }
                markerIndex++;
            }

            wrSampleSetMissingCountLHM.put(sampleId, missingCount);

            double missingRatio = (double) missingCount/rdMarkerSet.markerIdSetLHM.size();
            wrSampleSetMissingRatioLHM.put(sampleId, missingRatio);
            double heterozygRatio = (double) heterozygCount/(rdMarkerSet.markerIdSetLHM.size()-missingCount);
            wrSampleSetHetzyRatioLHM.put(sampleId, heterozygRatio);
            

            sampleNb++;

            if(sampleNb%100==0){
                System.out.println("Processed samples: "+sampleNb+" at " + global.Utils.getMediumDateTimeAsString());
            }
        }

        //Write census, missing-ratio and mismatches to netCDF
        NetcdfFileWriteable wrNcFile = null;
        try {
            ///////////// CREATE netCDF-3 FILE ////////////

            OperationFactory wrOPHandler = new OperationFactory(rdMatrixMetadata.getStudyId(),
                                                "Sample QA", //friendly name
                                                "Sample census on "+rdMatrixMetadata.getMatrixFriendlyName()+"\nSamples: "+wrSampleSetMissingCountLHM.size(), //description
                                                wrSampleSetMissingCountLHM.size(),
                                                rdMatrixMetadata.getMarkerSetSize(),
                                                0,
                                                cNetCDF.Defaults.OPType.SAMPLE_QA.toString(),
                                                rdMatrixMetadata.getMatrixId(), //Parent matrixId
                                                -1);                            //Parent operationId

            wrNcFile = wrOPHandler.getNetCDFHandler();
            try {
                wrNcFile.create();
            } catch (IOException e) {
                System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
            }
            //System.out.println("Done creating netCDF handle: " + global.Utils.getMediumDateTimeAsString());


            //<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
            //SAMPLESET
            ArrayChar.D2 samplesD2 = Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

            int[] sampleOrig = new int[]{0, 0};
            try {
                wrNcFile.write(cNetCDF.Variables.VAR_OPSET, sampleOrig, samplesD2);
            } catch (IOException e) {
                System.err.println("ERROR writing file");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
            samplesD2 = null;
            System.out.println("Done writing SampleSet to matrix at "+global.Utils.getMediumDateTimeAsString());

            //WRITE MARKERSET TO MATRIX
            ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdMarkerSet.markerIdSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
            int[] markersOrig = new int[]{0, 0};
            try {
                wrNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, markersOrig, markersD2);
            } catch (IOException e) {
                System.err.println("ERROR writing file");
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
            samplesD2 = null;
            System.out.println("Done writing MarkerSet to matrix at "+global.Utils.getMediumDateTimeAsString());

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="CENSUS DATA WRITER">
            //MISSING RATIO
            Utils.saveDoubleLHMD1ToWrMatrix(wrNcFile, wrSampleSetMissingRatioLHM, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);

            //MISSING COUNT
            Utils.saveIntLHMD1ToWrMatrix(wrNcFile, wrSampleSetMissingCountLHM, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT);

            //HETEROZYGOSITY RATIO
            Utils.saveDoubleLHMD1ToWrMatrix(wrNcFile, wrSampleSetHetzyRatioLHM, cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);
            //</editor-fold>

            resultOpId = wrOPHandler.getResultOPId();
        } finally {
            if (null != rdNcFile) try {
                rdNcFile.close();
                wrNcFile.close();
            } catch (IOException ioe) {
                System.err.println("Cannot close file: "+ioe);
            }

            global.Utils.sysoutCompleted("Sample QA");
            return resultOpId;
        }


    }
}

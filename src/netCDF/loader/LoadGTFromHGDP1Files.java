/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netCDF.loader;

import constants.cImport;
import constants.cNetCDF;
import constants.cNetCDF.*;
import database.DbManager;
import global.ServiceLocator;
import global.Text;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import ucar.ma2.*;
import ucar.nc2.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import netCDF.matrices.MatrixFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class LoadGTFromHGDP1Files {


    private String gtFilePath;
    private String sampleFilePath;
    private String annotationFilePath;
    private LinkedHashMap wrSampleSetLHM = new LinkedHashMap();
    private int studyId;
    private String format = cImport.ImportFormat.HGDP1.toString();
    private String chromosome;
    private String strand;
    private String friendlyName;
    private String description;
    private String gtCode;
    private constants.cNetCDF.Defaults.GenotypeEncoding guessedGTCode = constants.cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;
    private LinkedHashMap wrMarkerSetLHM = new LinkedHashMap();

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">

    public LoadGTFromHGDP1Files(String _gtFilePath,
                                   String _sampleFilePath,
                                   String _annotationFilePath,
                                   int _studyId,
                                   String _chromosome,
                                   String _strand,
                                   String _friendlyName,
                                   String _gtCode,
                                   String _description,
                                   LinkedHashMap _sampleInfoLHM) throws FileNotFoundException, IOException{

        gtFilePath = _gtFilePath;
        sampleFilePath = _sampleFilePath;
        annotationFilePath = _annotationFilePath;
        studyId = _studyId;
        chromosome = _chromosome;
        strand = _strand;
        friendlyName = _friendlyName;
        gtCode = _gtCode;
        description = _description;

        wrSampleSetLHM = _sampleInfoLHM;
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PROCESS GENOTYPES">

    public int processHGDP1GTFiles() throws FileNotFoundException, IOException, InvalidRangeException{
        int result = Integer.MIN_VALUE;

        String startTime= global.Utils.getMediumDateTimeAsString();


        //<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">
        MetadataLoaderHGDP1 markerSetLoader = new MetadataLoaderHGDP1(annotationFilePath, strand, studyId);
        wrMarkerSetLHM = markerSetLoader.getSortedMarkerSetWithMetaData();

        System.out.println("Done initializing sorted MarkerSetLHM at "+global.Utils.getMediumDateTimeAsString());

        ///////////// CREATE netCDF-3 FILE ////////////
        StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
        descSB.append(global.Utils.getShortDateTimeAsString());
        if (!description.isEmpty()) {
            descSB.append("\nDescription: ");
            descSB.append(description);
            descSB.append("\n");
        }
//        descSB.append("\nStrand: ");
//        descSB.append(strand);
//        descSB.append("\n");
//        descSB.append("Genotype encoding: ");
//        descSB.append(gtCode);
        descSB.append("\n");
        descSB.append("Markers: "+wrMarkerSetLHM.size()+", Samples: "+wrSampleSetLHM.size());
        descSB.append("\n");
        descSB.append(Text.Matrix.descriptionHeader2);
        descSB.append(format);
        descSB.append("\n");
        descSB.append(Text.Matrix.descriptionHeader3);
        descSB.append("\n");
        descSB.append(gtFilePath);
        descSB.append(" (Genotype file)\n");
        descSB.append(annotationFilePath);
        descSB.append(" (Marker file)\n");
        if(new File(sampleFilePath).exists()){
            descSB.append(sampleFilePath);
            descSB.append(" (Sample Info file)\n");
        }

        //RETRIEVE CHROMOSOMES INFO
        LinkedHashMap chrSetLHM = netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerSetLHM, 2, 3);

        MatrixFactory matrixFactory = new MatrixFactory(studyId,
                                                      format,
                                                      friendlyName,
                                                      descSB.toString(), //description
                                                      gtCode,
                                                      cNetCDF.Defaults.StrandType.UNKNOWN.toString(),   //Affymetrix standard
                                                      0,
                                                      wrSampleSetLHM.size(),
                                                      wrMarkerSetLHM.size(),
                                                      chrSetLHM.size(),
                                                      gtFilePath);

        NetcdfFileWriteable ncfile = matrixFactory.getNetCDFHandler();

        // create the file
        try {
            ncfile.create();
        } catch (IOException e) {
            System.err.println("ERROR creating file "+ncfile.getLocation()+"\n"+e);
        }
        //System.out.println("Done creating netCDF handle at "+global.Utils.getMediumDateTimeAsString());
        //</editor-fold>


        // <editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">
        //WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
        ArrayChar.D2 samplesD2 = netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(wrSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

        int[] sampleOrig = new int[]{0,0};
        try {
            ncfile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
        } catch (IOException e) {
            System.err.println("ERROR writing file");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
        samplesD2 = null;
        System.out.println("Done writing SampleSet to matrix at "+global.Utils.getMediumDateTimeAsString());

        //WRITE RSID & MARKERID METADATA FROM METADATALHM
        ArrayChar.D2 markersD2 = netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrMarkerSetLHM, 1, cNetCDF.Strides.STRIDE_MARKER_NAME);

        int[] markersOrig = new int[]{0,0};
        try {
            ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
        } catch (IOException e) {
            System.err.println("ERROR writing file");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
        markersD2 = netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrMarkerSetLHM, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);
        try {
            ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
        } catch (IOException e) {
            System.err.println("ERROR writing file");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
        System.out.println("Done writing MarkerId and RsId to matrix at "+global.Utils.getMediumDateTimeAsString());

        //WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
        markersD2 = netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrMarkerSetLHM, 2, cNetCDF.Strides.STRIDE_CHR);

        try {
            ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
        } catch (IOException e) {
            System.err.println("ERROR writing file");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
        System.out.println("Done writing chromosomes to matrix at "+global.Utils.getMediumDateTimeAsString());

        //Set of chromosomes found in matrix along with number of markersinfo
        netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(ncfile, chrSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);

        //Number of marker per chromosome & max pos for each chromosome
        int[] columns = new int[]{0,1,2,3};
        netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(ncfile, chrSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);

        

        //WRITE POSITION METADATA FROM ANNOTATION FILE
        //markersD2 = netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrMarkerSetLHM, 3, cNetCDF.Strides.STRIDE_POS);
        ArrayInt.D1 markersPosD1 = netCDF.operations.Utils.writeLHMValueItemToD1ArrayInt(wrMarkerSetLHM, 3);
        int[] posOrig = new int[1];
        try {
            ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
        } catch (IOException e) {
            System.err.println("ERROR writing file");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
        System.out.println("Done writing positions to matrix at "+global.Utils.getMediumDateTimeAsString());



        //WRITE GT STRAND FROM ANNOTATION FILE
        int[] gtOrig = new int[]{0,0};
        String strandFlag=cImport.StrandFlags.strandUNK;
        switch(cNetCDF.Defaults.StrandType.compareTo(strand)){
            case PLUS:
                strandFlag=cImport.StrandFlags.strandPLS;
                break;
            case MINUS:
                strandFlag=cImport.StrandFlags.strandMIN;
                break;
            case FWD:
                strandFlag=cImport.StrandFlags.strandFWD;
                break;
            case REV:
                strandFlag=cImport.StrandFlags.strandREV;
                break;
        }
        for (Iterator it = wrMarkerSetLHM.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            wrMarkerSetLHM.put(key, strandFlag);
        }
        markersD2 = netCDF.operations.Utils.writeLHMValueToD2ArrayChar(wrMarkerSetLHM, cNetCDF.Strides.STRIDE_STRAND);

        try {
            ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
        } catch (IOException e) {
            System.err.println("ERROR writing file");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
        markersD2 = null;
        System.out.println("Done writing strand info to matrix at "+global.Utils.getMediumDateTimeAsString());


        // </editor-fold>


        // <editor-fold defaultstate="collapsed" desc="MATRIX GENOTYPES LOAD ">

        int sampleIndex = 0;
        for (Iterator it = wrSampleSetLHM.keySet().iterator(); it.hasNext();) {
            String sampleId = it.next().toString();

            //PURGE MarkerIdLHM
            for (Iterator it2=wrMarkerSetLHM.keySet().iterator(); it2.hasNext();) {
                Object markerId = it2.next();
                wrMarkerSetLHM.put(markerId, cNetCDF.Defaults.DEFAULT_GT);
            }

            try {
                wrMarkerSetLHM = loadIndividualFiles(new File(gtFilePath),
                                                    sampleId,
                                                    wrMarkerSetLHM);

                /////////// WRITING GENOTYPE DATA INTO netCDF FILE ////////////
                netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, wrMarkerSetLHM, sampleIndex);



            } catch (IOException iOException) {
                //NOTHING
            } catch (InvalidRangeException invalidRangeException) {
                //NOTHING
            }

            sampleIndex++;
            if(sampleIndex==1){
                System.out.println(global.Text.All.processing);
            } else if(sampleIndex%100==0){
                System.out.println("Done processing sample Nº"+sampleIndex+" at "+global.Utils.getMediumDateTimeAsString());
            }
        }


        System.out.println("Done writing genotypes to matrix at "+global.Utils.getMediumDateTimeAsString());
        // </editor-fold>

        
        // CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
        try {
            //GUESS GENOTYPE ENCODING
            ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1,8);
            Index index = guessedGTCodeAC.getIndex();
            guessedGTCodeAC.setString(index.set(0,0),guessedGTCode.toString().trim());
            int[] origin = new int[]{0,0};
            ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

            descSB.append("Genotype encoding: ");
            descSB.append(guessedGTCode);
            DbManager db = ServiceLocator.getDbManager(constants.cDBGWASpi.DB_DATACENTER);
            db.updateTable(constants.cDBGWASpi.SCH_MATRICES,
                    constants.cDBMatrix.T_MATRICES,
                    new String[]{constants.cDBMatrix.f_DESCRIPTION},
                    new Object[]{descSB.toString()},
                    new String[]{constants.cDBMatrix.f_ID},
                    new Object[]{matrixFactory.matrixMetaData.getMatrixId()});

            //CLOSE FILE
            ncfile.close();
            result = matrixFactory.matrixMetaData.getMatrixId();
        } catch (IOException e) {
            System.err.println("ERROR creating file "+ncfile.getLocation()+"\n"+e);
        }

        logAsWhole(startTime, studyId, gtFilePath, format, friendlyName, description);

        global.Utils.sysoutCompleted("writing data to Matrix");
        return result;
    }

    
    public LinkedHashMap loadIndividualFiles(File file,
                                   String currSampleId,
                                   LinkedHashMap wrMarkerSetLHM) throws FileNotFoundException, IOException, InvalidRangeException{

        FileReader inputFileReader = new FileReader(file);
        BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

        int gtStride = cNetCDF.Strides.STRIDE_GT;
        StringBuilder sb = new StringBuilder(gtStride);
        for(int i=0; i<sb.capacity(); i++){
            sb.append('0');
        }

        LinkedHashMap tempMarkerIdLHM = new LinkedHashMap();
        LinkedHashMap sampleOrderLHM = new LinkedHashMap();

        String sampleHeader = inputBufferReader.readLine();
        String[] headerFields = null;
        headerFields = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
        for (int i=0; i<headerFields.length; i++) {
            if(!headerFields[i].isEmpty()){
                sampleOrderLHM.put(headerFields[i], i);
            }
        }

        String l;
        while ((l = inputBufferReader.readLine()) != null) {
            //GET ALLELES FROM MARKER ROWS
            String[] cVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
            String currMarkerId = cVals[cImport.Genotypes.HGDP1_Standard.markerId];

            Object columnNb = sampleOrderLHM.get(currSampleId);
            if (!columnNb.equals(null)) {
                String strAlleles = cVals[(Integer) columnNb];
                if (strAlleles.equals((cImport.Genotypes.HGDP1_Standard.missing))) {
                    tempMarkerIdLHM.put(currMarkerId, cNetCDF.Defaults.DEFAULT_GT);
                } else {
                    byte[] tmpAlleles = new byte[]{(byte) strAlleles.charAt(0),
                                                   (byte) strAlleles.charAt(0)};
                    tempMarkerIdLHM.put(currMarkerId, tmpAlleles);
                }
            }
        }

        wrMarkerSetLHM.putAll(tempMarkerIdLHM);

        if(guessedGTCode.equals(constants.cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)){
            guessedGTCode = Utils.detectGTEncoding(wrMarkerSetLHM);
        } else if(guessedGTCode.equals(constants.cNetCDF.Defaults.GenotypeEncoding.O12)){
            guessedGTCode = Utils.detectGTEncoding(wrMarkerSetLHM);
        }

        return wrMarkerSetLHM;
    }
    //</editor-fold>

    
    //<editor-fold defaultstate="collapsed" desc="HELPER METHODS">

    private LinkedHashMap getBeagleSampleIds(File hapmapGTFile) throws FileNotFoundException, IOException {

        LinkedHashMap uniqueSamples = new LinkedHashMap();

        FileReader fr = new FileReader(hapmapGTFile.getPath());
        BufferedReader inputBeagleBr = new BufferedReader(fr);

        String l;
        String sampleHeader="";
        boolean gotSamples = false;
        while ((l = inputBeagleBr.readLine()) != null && !gotSamples) {
            if(l.startsWith("I")){
                sampleHeader=l;
                gotSamples = true;
            }
        }

        String[] beagleSamples = sampleHeader.split(cImport.Separators.separators_SpaceTab_rgxp);

        for(int i=2;i<beagleSamples.length;i++){
            uniqueSamples.put(beagleSamples[i],"");
        }

        return uniqueSamples;
    }

    private static void logAsWhole(String startTime, int studyId, String dirPath, String format, String matrixName, String description) throws IOException {
         //LOG OPERATION IN STUDY HISTORY
         StringBuffer operation = new StringBuffer("\nLoaded raw "+format+" genotype data in path "+dirPath+".\n");
         operation.append("Start Time: " + startTime + "\n");
         operation.append("End Time: "+global.Utils.getMediumDateTimeAsString()+".\n");
         operation.append("Data stored in matrix "+matrixName+".\n");
         operation.append("Description: "+description+".\n");
         global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
         ////////////////////////////////
    }

    //</editor-fold>
}


package org.gwaspi.netCDF.operations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

import org.gwaspi.constants.cNetCDF;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.*;
import org.gwaspi.samples.SampleSet;
import ucar.ma2.*;
import ucar.nc2.*;
import org.gwaspi.constants.cNetCDF.*;
import org.gwaspi.global.Text;

public class MatrixTranslator_opt {


    protected int studyId = Integer.MIN_VALUE;
    protected int rdMatrixId = Integer.MIN_VALUE;
    protected int wrMatrixId = Integer.MIN_VALUE;
    protected String wrMatrixFriendlyName="";
    protected String wrMatrixDescription="";

    MatrixMetadata rdMatrixMetadata = null;
    MatrixMetadata wrMatrixMetadata = null;

    MarkerSet_opt rdMarkerSet = null;
    MarkerSet_opt wrMarkerSet = null;

    SampleSet rdSampleSet = null;
    SampleSet wrSampleSet = null;

    LinkedHashMap wrMarkerIdSetLHM = new LinkedHashMap();

    LinkedHashMap rdChrInfoSetLHM = null;

    LinkedHashMap rdSampleSetLHM = null;
    LinkedHashMap wrSampleSetLHM = new LinkedHashMap();


    public MatrixTranslator_opt(int _studyId,
                               int _rdMatrixId,
                               String _wrMatrixFriendlyName,
                               String _wrMatrixDescription
                               ) throws IOException, InvalidRangeException{

        /////////// INIT EXTRACTOR OBJECTS //////////

        rdMatrixId = _rdMatrixId;
        rdMatrixMetadata = new MatrixMetadata(rdMatrixId);
        studyId = rdMatrixMetadata.getStudyId();
        wrMatrixFriendlyName = _wrMatrixFriendlyName;
        wrMatrixDescription = _wrMatrixDescription;

        rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(),rdMatrixId);
        rdMarkerSet.initFullMarkerIdSetLHM();

        rdChrInfoSetLHM = rdMarkerSet.getChrInfoSetLHM();

        rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(),rdMatrixId);
        rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();

    }
    

    public int translateAB12AllelesToACGT() throws InvalidRangeException, IOException{
        int result=Integer.MIN_VALUE;

        NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
        String rdMatrixGtCode = rdMatrixMetadata.getGenotypeEncoding();

        if(!rdMatrixGtCode.equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString())){ //Has not allready been translated

            try {
                ///////////// CREATE netCDF-3 FILE ////////////
                StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
                descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
                descSB.append("\nThrough Matrix translation from parent Matrix MX: ").append(rdMatrixId).append(" - ").append(rdMatrixMetadata.getMatrixFriendlyName());
                descSB.append("\nTranslation method: AB0 or 012 to ACGT0 using the parent's dictionnary");
                if (!wrMatrixDescription.isEmpty()) {
                    descSB.append("\n\nDescription: ");
                    descSB.append(wrMatrixDescription);
                    descSB.append("\n");
                }
                descSB.append("\n");
                descSB.append("Markers: ").append(rdMatrixMetadata.getMarkerSetSize()).append(", Samples: ").append(rdMatrixMetadata.getSampleSetSize());
                descSB.append("\nGenotype encoding: ");
                descSB.append(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString());

                MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
                                                                rdMatrixMetadata.getTechnology(), //technology
                                                                wrMatrixFriendlyName,
                                                                descSB.toString(), //description
                                                                rdMatrixMetadata.getStrand(),
                                                                rdMatrixMetadata.getHasDictionray(), //has dictionary?
                                                                rdSampleSet.getSampleSetSize(),
                                                                rdMarkerSet.getMarkerSetSize(),
                                                                rdChrInfoSetLHM.size(),
                                                                cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString(), //New matrix genotype encoding
                                                                rdMatrixId, //Orig matrixId 1
                                                                Integer.MIN_VALUE);         //Orig matrixId 2

                NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
                try {
                    wrNcFile.create();
                } catch (IOException e) {
                    System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
                }
                //System.out.println("Done creating netCDF handle in MatrixataTransform: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


                //<editor-fold defaultstate="collapsed" desc="METADATA WRITER">

                //////// WRITING METADATA TO MATRIX /////////

                //SAMPLESET
                ArrayChar.D2 samplesD2 = Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

                int[] sampleOrig = new int[]{0, 0};
                try {
                    wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
                samplesD2 = null;
                System.out.println("Done writing SampleSet to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


                //MARKERSET MARKERID
                ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdMarkerSet.markerIdSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
                int[] markersOrig = new int[]{0, 0};
                try {
                    wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }


                //MARKERSET RSID
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

                //MARKERSET CHROMOSOME
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

                //Set of chromosomes found in matrix along with number of markersinfo
                org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
                //Number of marker per chromosome & max pos for each chromosome
                int[] columns = new int[]{0,1,2,3};
                org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);
                
                //MARKERSET POSITION
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
                Utils.saveIntLHMD1ToWrMatrix(wrNcFile, rdMarkerSet.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS);

                //MARKERSET DICTIONARY ALLELES
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
                wrMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(wrMarkerIdSetLHM, rdMarkerSet.markerIdSetLHM);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

                //GENOTYPE STRAND
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.markerIdSetLHM, cNetCDF.Variables.VAR_GT_STRAND, cNetCDF.Strides.STRIDE_STRAND);


                //</editor-fold>


                //<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

                //Get correct bases dictionary for translation
                LinkedHashMap dictionnaryLHM = rdMarkerSet.getDictionaryBasesLHM();

                //Iterate through Samples, use Sample item position to read all Markers GTs from rdMarkerIdSetLHM.
                int sampleIndex = 0;
                for (int i=0; i<rdSampleSetLHM.size(); i++) {
                    //Get alleles from read matrix
                    rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleIndex);
                    //Send to be translated
                    wrMarkerIdSetLHM = translateCurrentSampleAB12AllelesLHM(rdMarkerSet.markerIdSetLHM, rdMatrixGtCode, dictionnaryLHM);

                    //Write wrMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
                    Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrMarkerIdSetLHM, sampleIndex);
                    if(sampleIndex%100==0){
                        System.out.println("Samples translated:"+sampleIndex);
                    }
                    sampleIndex++;
                }
                System.out.println("Total Samples translated:"+sampleIndex);

                //</editor-fold>

                // CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
                try {
                    //GUESS GENOTYPE ENCODING
                    ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1,8);
                    Index index = guessedGTCodeAC.getIndex();
                    guessedGTCodeAC.setString(index.set(0,0),cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString());
                    int[] origin = new int[]{0,0};
                    wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

                    wrNcFile.close();
                    result = wrMatrixHandler.getResultMatrixId();
                } catch (IOException e) {
                    System.err.println("ERROR creating file "+wrNcFile.getLocation()+"\n"+e);
                }

                org.gwaspi.global.Utils.sysoutCompleted("Translation");

            } catch (InvalidRangeException invalidRangeException) {
            } catch (IOException iOException) {
            } finally {
                if (null != rdNcFile) try {
                    rdNcFile.close();
                } catch (IOException ioe) {
                    System.out.println("Cannot close file: "+ioe);
                }
            }


        }

        return result;
    }

    //TODO: Test translate1234AllelesToACGT
    public int translate1234AllelesToACGT() throws IOException, InvalidRangeException{
        int result=Integer.MIN_VALUE;

        NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
        String rdMatrixGTCode = rdMatrixMetadata.getGenotypeEncoding();

        if(!rdMatrixGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString())){ //Has not allready been translated
            try {
                ///////////// CREATE netCDF-3 FILE ////////////
                StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
                descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
                descSB.append("\nThrough Matrix translation from parent Matrix MX: "+rdMatrixMetadata.getMatrixId()+" - "+rdMatrixMetadata.getMatrixFriendlyName());
                descSB.append("\nTranslation method: O1234 to ACGT0 using 0=0, 1=A, 2=C, 3=G, 4=T");
                if (!wrMatrixDescription.isEmpty()) {
                    descSB.append("\n\nDescription: ");
                    descSB.append(wrMatrixDescription);
                    descSB.append("\n");
                }
                descSB.append("\nGenotype encoding: ");
                descSB.append(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString());
                descSB.append("\n");
                descSB.append("Markers: "+rdMatrixMetadata.getMarkerSetSize()+", Samples: "+rdMatrixMetadata.getSampleSetSize());

                MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
                        rdMatrixMetadata.getTechnology(), //technology
                        wrMatrixFriendlyName,
                        descSB.toString(), //description
                        rdMatrixMetadata.getStrand(),
                        rdMatrixMetadata.getHasDictionray(), //has dictionary?
                        rdSampleSet.getSampleSetSize(),
                        rdMarkerSet.getMarkerSetSize(),
                        rdChrInfoSetLHM.size(),
                        cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString(), //New matrix genotype encoding
                        rdMatrixId, //Orig matrixId 1
                        Integer.MIN_VALUE);         //Orig matrixId 2

                NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
                try {
                    wrNcFile.create();
                } catch (IOException e) {
                    System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
                }
                //System.out.println("Done creating netCDF handle in MatrixataTransform: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


                //<editor-fold defaultstate="collapsed" desc="METADATA WRITER">

                //////// WRITING METADATA TO MATRIX /////////

                //SAMPLESET
                ArrayChar.D2 samplesD2 = Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

                int[] sampleOrig = new int[]{0, 0};
                try {
                    wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
                samplesD2 = null;
                System.out.println("Done writing SampleSet to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


                //MARKERSET MARKERID
                ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdMarkerSet.markerIdSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
                int[] markersOrig = new int[]{0, 0};
                try {
                    wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
                } catch (IOException e) {
                    System.err.println("ERROR writing file");
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }


                //MARKERSET RSID
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

                //MARKERSET CHROMOSOME
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

                //MARKERSET POSITION
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
                //Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
                Utils.saveIntLHMD1ToWrMatrix(wrNcFile, rdMarkerSet.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS);

                //MARKERSET DICTIONARY ALLELES
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.markerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

                //GENOTYPE STRAND
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdMarkerSet.markerIdSetLHM, cNetCDF.Variables.VAR_GT_STRAND, cNetCDF.Strides.STRIDE_STRAND);


                //</editor-fold>


                //<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

                //Get correct strand of each marker for newStrand translation
                LinkedHashMap markerStrandsLHM = new LinkedHashMap();
                markerStrandsLHM.putAll(rdSampleSetLHM);

                //Iterate through pmAllelesAndStrandsLHM, use Sample item position to read all Markers GTs from rdMarkerIdSetLHM.
                int sampleNb = 0;
                for (int i=0; i<rdSampleSetLHM.size(); i++) {
                    //Get alleles from read matrix
                    rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleNb);
                    //Send to be translated
                    wrMarkerIdSetLHM = translateCurrentSample1234AllelesLHM(rdMarkerSet.markerIdSetLHM, markerStrandsLHM);

                    //Write wrMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
                    Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrMarkerIdSetLHM, sampleNb);

                    if(sampleNb%100==0){
                        System.out.println("Samples translated:"+sampleNb);
                    }
                    sampleNb++;
                }
                System.out.println("Total Samples translated:"+sampleNb);

                //</editor-fold>

                // CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
                try {
                    //GUESS GENOTYPE ENCODING
                    ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1,8);
                    Index index = guessedGTCodeAC.getIndex();
                    guessedGTCodeAC.setString(index.set(0,0),cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString());
                    int[] origin = new int[]{0,0};
                    wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

                    wrNcFile.close();
                    result = wrMatrixHandler.getResultMatrixId();
                } catch (IOException e) {
                    System.err.println("ERROR creating file "+wrNcFile.getLocation()+"\n"+e);
                }

                org.gwaspi.global.Utils.sysoutCompleted("Translation");

            } catch (InvalidRangeException invalidRangeException) {
            } catch (IOException iOException) {
            } finally {
                if (null != rdNcFile) try {
                    rdNcFile.close();
                } catch (IOException ioe) {
                    System.out.println("Cannot close file: "+ioe);
                }
            }
        }

        return result;
    }

 

    protected LinkedHashMap translateCurrentSampleAB12AllelesLHM(LinkedHashMap codedLHM, String rdMatrixType, LinkedHashMap dictionaryLHM){
        byte alleleA = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.A;
        byte alleleB = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.B;

        switch(cNetCDF.Defaults.GenotypeEncoding.compareTo(rdMatrixType)){
            case AB0:
                //Iterate through all markers
                for (Iterator it = codedLHM.keySet().iterator(); it.hasNext();) {
                    Object markerId = it.next();
                    char[] basesDict = dictionaryLHM.get(markerId).toString().toCharArray();
                    byte[] codedAlleles = (byte[]) codedLHM.get(markerId);
                    byte[] transAlleles = new byte[2];

                    if(codedAlleles[0]==alleleA){ transAlleles[0]=(byte)basesDict[0];}
                    else if(codedAlleles[0]==alleleB){transAlleles[0]=(byte)basesDict[1];}
                    else {transAlleles[0]=constants.cNetCDF.Defaults.AlleleBytes._0;}

                    if(codedAlleles[1]==alleleA){transAlleles[1]=(byte)basesDict[0];}
                    else if(codedAlleles[1]==alleleB){transAlleles[1]=(byte)basesDict[1];}
                    else {transAlleles[1]=constants.cNetCDF.Defaults.AlleleBytes._0;}

                    codedLHM.put(markerId, transAlleles);
                }

                break;
            case O12:
                alleleA = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._1;
                alleleB = org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._2;

                //Iterate through all markers
                for (Iterator it = codedLHM.keySet().iterator(); it.hasNext();) {
                    Object markerId = it.next();
                    char[] basesDict = dictionaryLHM.get(markerId).toString().toCharArray();
                    byte[] codedAlleles = (byte[]) codedLHM.get(markerId);
                    byte[] transAlleles = new byte[2];

                    if(codedAlleles[0]==alleleA){transAlleles[0]=(byte)basesDict[0];}
                    else if(codedAlleles[0]==alleleB){transAlleles[0]=(byte)basesDict[1];}
                    else {transAlleles[0]=constants.cNetCDF.Defaults.AlleleBytes._0;}

                    if(codedAlleles[1]==alleleA){transAlleles[1]=(byte)basesDict[0];}
                    else if(codedAlleles[1]==alleleB){transAlleles[1]=(byte)basesDict[1];}
                    else {transAlleles[0]=constants.cNetCDF.Defaults.AlleleBytes._0;}

                    codedLHM.put(markerId, transAlleles);
                }

                break;
            }
        return codedLHM;
    }


    protected LinkedHashMap translateCurrentSample1234AllelesLHM(LinkedHashMap codedLHM, LinkedHashMap markerStrandsLHM){

        HashMap dictionary = new HashMap();
        dictionary.put(org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._0, org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._0);
        dictionary.put(org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._1, org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.A);
        dictionary.put(org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._2, org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.C);
        dictionary.put(org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._3, org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.G);
        dictionary.put(org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes._4, org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes.T);

        //Iterate through all markers
        for (Iterator it = markerStrandsLHM.keySet().iterator(); it.hasNext();) {
            Object markerId = it.next();
            byte[] codedAlleles = (byte[]) codedLHM.get(markerId);

            byte[] transAlleles = new byte[2];
            transAlleles[0]=(Byte) dictionary.get(codedAlleles[0]);
            transAlleles[1]=(Byte) dictionary.get(codedAlleles[1]);

            codedLHM.put(markerId, transAlleles);
        }
        return codedLHM;
    }


    //<editor-fold defaultstate="collapsed" desc="ACCESSORS">
    public int getRdMatrixId() {
        return rdMatrixId;
    }

    public int getStudyId() {
        return studyId;
    }

    public int getWrMatrixId() {
        return wrMatrixId;
    }
    //</editor-fold>


}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netCDF.operations;

import constants.cNetCDF;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import netCDF.markers.MarkerSet_opt;
import netCDF.matrices.*;
import samples.SampleSet;
import ucar.ma2.*;
import ucar.nc2.*;
import constants.cNetCDF.*;
import database.DbManager;
import global.ServiceLocator;
import global.Text;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */


public class MatrixDataExtractor_opt {

    protected int studyId = Integer.MIN_VALUE;
    protected int rdMatrixId = Integer.MIN_VALUE;
    protected int wrMatrixId = Integer.MIN_VALUE;
    protected String wrMatrixFriendlyName="";
    protected String wrMatrixDescription="";

    protected File markerCriteriaFile;
    protected File sampleCriteriaFile;
    protected cNetCDF.Defaults.SetMarkerPickCase markerPickCase;
    protected String markerPickerVar;
    protected StringBuilder markerPickerCriteria = new StringBuilder();
    protected cNetCDF.Defaults.SetSamplePickCase samplePickCase;
    protected String samplePickerVar;
    protected StringBuilder samplePickerCriteria = new StringBuilder();

    MatrixMetadata rdMatrixMetadata = null;
    MatrixMetadata wrMatrixMetadata = null;

    MarkerSet_opt rdMarkerSet = null;
    MarkerSet_opt wrMarkerSet = null;

    SampleSet rdSampleSet = null;
    SampleSet wrSampleSet = null;

    LinkedHashMap wrMarkerIdSetLHM = new LinkedHashMap();

    LinkedHashMap rdSampleSetLHM = null;
    LinkedHashMap wrSampleSetLHM = new LinkedHashMap();

    LinkedHashMap rdChrInfoSetLHM = new LinkedHashMap();

    /**
    * This constructor to extract data from Matrix a by passing
    * a variable and the criteria to filter items by.
     * @param _studyId
     * @param _rdMatrixId
     * @param _wrMatrixFriendlyName
     * @param _wrMatrixDescription
     * @param _markerPickCase
     * @param _samplePickCase
     * @param _markerPickerVar
     * @param _samplePickerVar
     * @param _markerCriteria
     * @param _sampleCriteria
     * @param _sampleFilterPos
     * @param _markerPickerFile
     * @param _samplePickerFile
     * @throws IOException
     * @throws InvalidRangeException 
     */
    public MatrixDataExtractor_opt(int _studyId,
                               int _rdMatrixId,
                               String _wrMatrixFriendlyName,
                               String _wrMatrixDescription,
                               cNetCDF.Defaults.SetMarkerPickCase _markerPickCase,
                               cNetCDF.Defaults.SetSamplePickCase _samplePickCase,
                               String _markerPickerVar,
                               String _samplePickerVar,
                               HashSet _markerCriteria,
                               HashSet _sampleCriteria,
                               int _sampleFilterPos,
                               File _markerPickerFile,
                               File _samplePickerFile
                               ) throws IOException, InvalidRangeException{

        /////////// INIT EXTRACTOR OBJECTS //////////
        markerPickCase=_markerPickCase;
        markerPickerVar=_markerPickerVar;
        samplePickCase=_samplePickCase;
        samplePickerVar=_samplePickerVar;
        markerCriteriaFile=_markerPickerFile;
        sampleCriteriaFile=_samplePickerFile;


        rdMatrixId = _rdMatrixId;
        rdMatrixMetadata = new MatrixMetadata(rdMatrixId);
        studyId = rdMatrixMetadata.getStudyId();
        wrMatrixFriendlyName = _wrMatrixFriendlyName;
        wrMatrixDescription = _wrMatrixDescription;
        
        rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(),rdMatrixId);
        rdMarkerSet.initFullMarkerIdSetLHM();

        rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(),rdMatrixId);
        rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();


        //<editor-fold defaultstate="collapsed" desc="MARKERSET PICKING">
        for (Iterator it = _markerCriteria.iterator(); it.hasNext();) {
            markerPickerCriteria.append(it.next().toString());
            markerPickerCriteria.append(",");
        }


        //Pick markerId by criteria file
        if (!_markerPickerFile.toString().isEmpty() && _markerPickerFile.isFile()) {
            FileReader fr = new FileReader(_markerPickerFile);
            BufferedReader br = new BufferedReader(fr);
            String l = "";
            _markerCriteria.clear();
            while ((l = br.readLine()) != null) {
                _markerCriteria.add(l);
                markerPickerCriteria.append(l);
                markerPickerCriteria.append(",");
            }
        }


        switch(_markerPickCase){
            case ALL_MARKERS:
                //Get all markers
                wrMarkerIdSetLHM.putAll(rdMarkerSet.markerIdSetLHM);
                wrMarkerIdSetLHM = rdMarkerSet.fillLHMWithMyValue(wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
                break;
            case MARKERS_INCLUDE_BY_NETCDF_CRITERIA:
                //Pick by netCDF field value and criteria
                wrMarkerIdSetLHM = rdMarkerSet.pickValidMarkerSetItemsByValue(_markerPickerVar, _markerCriteria, true);
                wrMarkerIdSetLHM = rdMarkerSet.fillLHMWithMyValue(wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
                break;
            case MARKERS_EXCLUDE_BY_NETCDF_CRITERIA:
                //Exclude by netCDF field value and criteria
                wrMarkerIdSetLHM = rdMarkerSet.pickValidMarkerSetItemsByValue(_markerPickerVar, _markerCriteria, false);
                wrMarkerIdSetLHM = rdMarkerSet.fillLHMWithMyValue(wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
                break;
            case MARKERS_INCLUDE_BY_ID:
                wrMarkerIdSetLHM = rdMarkerSet.pickValidMarkerSetItemsByKey(_markerCriteria, true);
                wrMarkerIdSetLHM = rdMarkerSet.fillLHMWithMyValue(wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
                break;
            case MARKERS_EXCLUDE_BY_ID:
                wrMarkerIdSetLHM = rdMarkerSet.pickValidMarkerSetItemsByKey(_markerCriteria, false);
                wrMarkerIdSetLHM = rdMarkerSet.fillLHMWithMyValue(wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
                break;
            default:
                //Get all markers
                wrMarkerIdSetLHM.putAll(rdMarkerSet.markerIdSetLHM);
                wrMarkerIdSetLHM = rdMarkerSet.fillLHMWithMyValue(wrMarkerIdSetLHM, cNetCDF.Defaults.DEFAULT_GT);
        }


        //RETRIEVE CHROMOSOMES INFO
        rdMarkerSet.fillMarkerSetLHMWithChrAndPos();
        wrMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(wrMarkerIdSetLHM, rdMarkerSet.markerIdSetLHM);
        rdChrInfoSetLHM = netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerIdSetLHM, 0, 1);


        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="SAMPLESET PICKING">
        for (Iterator it = _sampleCriteria.iterator(); it.hasNext();) {
            samplePickerCriteria.append(it.next().toString());
            samplePickerCriteria.append(",");
        }

        //USE cNetCDF Key and criteria or list file
        if (!_samplePickerFile.toString().isEmpty() && _samplePickerFile.isFile()) {
            FileReader fr = new FileReader(_samplePickerFile);
            BufferedReader br = new BufferedReader(fr);
            String l = "";
            _sampleCriteria.clear();
            while ((l = br.readLine()) != null) {
                _sampleCriteria.add(l);
                samplePickerCriteria.append(l);
                samplePickerCriteria.append(",");
            }
       }

        switch(_samplePickCase){
            case ALL_SAMPLES:
                //Get all samples
                wrSampleSetLHM.putAll(rdSampleSetLHM);
                int i=0;
                for (Iterator it = wrSampleSetLHM.keySet().iterator(); it.hasNext();) {
                    Object key = it.next();
                    wrSampleSetLHM.put(key, i);
                    i++;
                }
                break;
            case SAMPLES_INCLUDE_BY_NETCDF_FILTER:
                //USE cNetCDF Filter Data and criteria
                wrSampleSetLHM = rdSampleSet.pickValidSampleSetItemsByNetCDFFilter(rdSampleSetLHM, _samplePickerVar, _sampleFilterPos, _sampleCriteria, true);
                break;
            case SAMPLES_EXCLUDE_BY_NETCDF_FILTER:
                //USE cNetCDF Filter Data and criteria
                wrSampleSetLHM = rdSampleSet.pickValidSampleSetItemsByNetCDFFilter(rdSampleSetLHM, _samplePickerVar, _sampleFilterPos, _sampleCriteria, false);
                break;
            case SAMPLES_INCLUDE_BY_NETCDF_CRITERIA:
                //USE cNetCDF Value and criteria
                wrSampleSetLHM = rdSampleSet.pickValidSampleSetItemsByNetCDFValue(rdSampleSetLHM, _samplePickerVar, _sampleCriteria, true);
                break;
            case SAMPLES_EXCLUDE_BY_NETCDF_CRITERIA:
                //USE cNetCDF Value and criteria
                wrSampleSetLHM = rdSampleSet.pickValidSampleSetItemsByNetCDFValue(rdSampleSetLHM, _samplePickerVar, _sampleCriteria, false);
                break;
            case SAMPLES_INCLUDE_BY_ID:
               wrSampleSetLHM = rdSampleSet.pickValidSampleSetItemsByNetCDFKey(rdSampleSetLHM, _sampleCriteria, true);
               break;
            case SAMPLES_EXCLUDE_BY_ID:
               wrSampleSetLHM = rdSampleSet.pickValidSampleSetItemsByNetCDFKey(rdSampleSetLHM, _sampleCriteria, false);
                break;
            case SAMPLES_INCLUDE_BY_DB_FIELD:
                //USE DB DATA
                wrSampleSetLHM = rdSampleSet.pickValidSampleSetItemsByDBField(_studyId, rdSampleSetLHM, _samplePickerVar, _sampleCriteria, true);
                break;
            case SAMPLES_EXCLUDE_BY_DB_FIELD:
                //USE DB DATA
                wrSampleSetLHM = rdSampleSet.pickValidSampleSetItemsByDBField(_studyId, rdSampleSetLHM, _samplePickerVar, _sampleCriteria, false);
                break;
            default:
                int j=0;
                for (Iterator it = wrSampleSetLHM.keySet().iterator(); it.hasNext();) {
                    Object key = it.next();
                    wrSampleSetLHM.put(key, j);
                    j++;
                }
        }

        //</editor-fold>

    }

    public int extractGenotypesToNewMatrix() throws IOException{
        int resultMatrixId=Integer.MIN_VALUE;
        
        if (wrSampleSetLHM.size()>0 && wrMarkerIdSetLHM.size()>0) {
            try {
                ///////////// CREATE netCDF-3 FILE ////////////
                StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
                descSB.append(global.Utils.getShortDateTimeAsString());
                descSB.append("\nThrough Matrix extraction from parent Matrix MX: ").append(rdMatrixMetadata.getMatrixId()).append(" - ").append(rdMatrixMetadata.getMatrixFriendlyName());

                descSB.append("\nMarker Filter Variable: ");
                String pickPrefix = "All Markers";
                if(markerPickCase.toString().contains("EXCLUDE")){
                    pickPrefix = "Exclude by ";
                } else if(markerPickCase.toString().contains("INCLUDE")){
                    pickPrefix = "Include by ";
                }
                descSB.append(pickPrefix).append(markerPickerVar.replaceAll("_", " ").toUpperCase());
                if(markerCriteriaFile.isFile()){
                    descSB.append("\nMarker Criteria File: ");
                    descSB.append(markerCriteriaFile.getPath());
                } else if(!pickPrefix.equals("All Markers")){
                    descSB.append("\nMarker Criteria: ");
                    descSB.append(markerPickerCriteria.deleteCharAt(markerPickerCriteria.length()-1));
                }

                descSB.append("\nSample Filter Variable: ");
                pickPrefix = "All Samples";
                if(samplePickCase.toString().contains("EXCLUDE")){
                    pickPrefix = "Exclude by ";
                } else if(samplePickCase.toString().contains("INCLUDE")){
                    pickPrefix = "Include by ";
                }
                descSB.append(pickPrefix).append(samplePickerVar.replaceAll("_", " ").toUpperCase());
                if(sampleCriteriaFile.isFile()){
                    descSB.append("\nSample Criteria File: ");
                    descSB.append(sampleCriteriaFile.getPath());
                } else if(!pickPrefix.equals("All Samples")){
                    descSB.append("\nSample Criteria: ");
                    descSB.append(samplePickerCriteria.deleteCharAt(samplePickerCriteria.length()-1));
                }
                
                if (!wrMatrixDescription.isEmpty()) {
                    descSB.append("\n\nDescription: ");
                    descSB.append(wrMatrixDescription);
                    descSB.append("\n");
                }
//                descSB.append("\nGenotype encoding: ");
//                descSB.append(rdMatrixMetadata.getGenotypeEncoding());
                descSB.append("\n");
                descSB.append("Markers: ").append(wrMarkerIdSetLHM.size()).append(", Samples: ").append(wrSampleSetLHM.size());


                MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
                        rdMatrixMetadata.getTechnology(), //technology
                        wrMatrixFriendlyName,
                        descSB.toString(), //description
                        rdMatrixMetadata.getStrand(),
                        rdMatrixMetadata.getHasDictionray(), //has dictionary?
                        wrSampleSetLHM.size(),
                        wrMarkerIdSetLHM.size(),
                        rdChrInfoSetLHM.size(),
                        rdMatrixMetadata.getGenotypeEncoding(), //Matrix genotype encoding from orig matrix genotype encoding
                        rdMatrixId, //Orig matrixId 1
                        Integer.MIN_VALUE);         //Orig matrixId 2

                resultMatrixId = wrMatrixHandler.getResultMatrixId();

                NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
                NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
                try {
                    wrNcFile.create();
                } catch (IOException e) {
                    System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
                }
                //System.out.println("Done creating netCDF handle in MatrixataExtractor: " + global.Utils.getMediumDateTimeAsString());


                //<editor-fold defaultstate="collapsed" desc="METADATA WRITER">

                //////// WRITING METADATA TO MATRIX /////////

                //SAMPLESET
                ArrayChar.D2 samplesD2 = Utils.writeLHMKeysToD2ArrayChar(wrSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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


                //MARKERSET MARKERID
                ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(wrMarkerIdSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
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
                wrMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(wrMarkerIdSetLHM, rdMarkerSet.markerIdSetLHM);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

                //MARKERSET CHROMOSOME
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
                wrMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(wrMarkerIdSetLHM, rdMarkerSet.markerIdSetLHM);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

                //Set of chromosomes found in matrix along with number of markersinfo
                netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
                //Number of marker per chromosome & max pos for each chromosome
                int[] columns = new int[]{0,1,2,3};
                netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);


                //MARKERSET POSITION
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
                wrMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(wrMarkerIdSetLHM, rdMarkerSet.markerIdSetLHM);
                //Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
                Utils.saveIntLHMD1ToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_POS);


                //MARKERSET DICTIONARY ALLELES
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
                wrMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(wrMarkerIdSetLHM, rdMarkerSet.markerIdSetLHM);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);

                //GENOTYPE STRAND
                rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
                wrMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(wrMarkerIdSetLHM, rdMarkerSet.markerIdSetLHM);
                Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrMarkerIdSetLHM, cNetCDF.Variables.VAR_GT_STRAND, 3);

                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

                //Iterate through wrSampleSetLHM, use item position to read correct sample GTs into rdMarkerIdSetLHM.
                System.out.println(global.Text.All.processing);
                int sampleWrIndex = 0;
                for (Iterator it = wrSampleSetLHM.keySet().iterator(); it.hasNext();) {
                    //Iterate through wrMarkerIdSetLHM, get the correct GT from rdMarkerIdSetLHM
                    Object key = it.next();
                    Integer rdPos = (Integer) wrSampleSetLHM.get(key);
                    rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(rdPos);
//                    rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleWrPos);
                    wrMarkerIdSetLHM = rdMarkerSet.fillWrLHMWithRdLHMValue(wrMarkerIdSetLHM, rdMarkerSet.markerIdSetLHM);

                    //Write wrMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
                    Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrMarkerIdSetLHM, sampleWrIndex);
                    if(sampleWrIndex%100==0){
                        System.out.println("Samples copied: "+sampleWrIndex);
                    }
                    sampleWrIndex++;
                }
                //</editor-fold>

                // CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
                try {
                    //GENOTYPE ENCODING
                    ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1,8);
                    Index index = guessedGTCodeAC.getIndex();
                    guessedGTCodeAC.setString(index.set(0,0),rdMatrixMetadata.getGenotypeEncoding());
                    int[] origin = new int[]{0,0};
                    wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

                    descSB.append("\nGenotype encoding: ");
                    descSB.append(rdMatrixMetadata.getGenotypeEncoding());
                    DbManager db = ServiceLocator.getDbManager(constants.cDBGWASpi.DB_DATACENTER);
                    db.updateTable(constants.cDBGWASpi.SCH_MATRICES,
                            constants.cDBMatrix.T_MATRICES,
                            new String[]{constants.cDBMatrix.f_DESCRIPTION},
                            new Object[]{descSB.toString()},
                            new String[]{constants.cDBMatrix.f_ID},
                            new Object[]{resultMatrixId});

                    wrNcFile.close();
                } catch (IOException e) {
                    System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
                }

                global.Utils.sysoutCompleted("Extraction to new Matrix");

            } catch (InvalidRangeException invalidRangeException) {
            } catch (IOException iOException) {
            }
        } else {
            gui.utils.Dialogs.showWarningDialogue(global.Text.Trafo.criteriaReturnsNoResults);
        }

        
        return resultMatrixId;
    }


}

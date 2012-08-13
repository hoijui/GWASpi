package netCDF.matrices;

import constants.cDBGWASpi;
import constants.cNetCDF;
import database.DbManager;
import global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import ucar.ma2.*;
import ucar.nc2.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixFactory {
        
        private NetcdfFileWriteable netCDFHandler = null;
        private String resultMatrixName = "";
        private int resultMatrixId = Integer.MIN_VALUE;
        public MatrixMetadata matrixMetaData = null;
    
        //Costructor to use with matrix input
        public MatrixFactory(int studyId,
                              String technology, 
                              String friendlyName, 
                              String description, 
                              String strand,
                              int hasDictionary,
                              int samplesDimSize, 
                              int markerDimSize,
                              int chrDimSize,
                              String gtCode,
                              int origMatrix1Id,
                              int origMatrix2Id) throws InvalidRangeException, IOException{
            
            if (samplesDimSize>0 && markerDimSize>0) {
                resultMatrixName = netCDF.matrices.MatrixManager.generateMatrixNetCDFNameByDate();
                netCDFHandler = generateNetcdfHandler(studyId,
                        resultMatrixName,
                        technology,
                        description,
                        gtCode,
                        strand,
                        hasDictionary,
                        samplesDimSize,
                        markerDimSize,
                        chrDimSize);

                //CHECK IF JVM IS 32/64 bits to use LFS or not
                int JVMbits = Integer.parseInt(System.getProperty("sun.arch.data.model", "32"));
                if (JVMbits == 64) {
                    netCDFHandler.setLargeFile(true);
                }
                netCDFHandler.setFill(true);

                DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
                netCDF.matrices.MatrixManager.insertMatrixMetadata(dBManager,
                        studyId,
                        friendlyName,
                        resultMatrixName,
                        gtCode,
                        origMatrix1Id,
                        origMatrix2Id,
                        "Matrix is result of " + origMatrix1Id,
                        description,
                        0);

                matrixMetaData = new MatrixMetadata(resultMatrixName);

                resultMatrixId = matrixMetaData.getMatrixId();
            }
        
        }
        
        //Costructor to use with file input
        public MatrixFactory(Integer studyId, 
                              String technology, 
                              String friendlyName,
                              String description, 
                              String matrixType,
                              String strand,
                              int hasDictionary,
                              int samplesDimSize, 
                              int markerDimSize,
                              int chrDimSize,
                              String data_location) throws InvalidRangeException, IOException{
            
            if (samplesDimSize>0 && markerDimSize>0) {
                resultMatrixName = netCDF.matrices.MatrixManager.generateMatrixNetCDFNameByDate();
                netCDFHandler = generateNetcdfHandler(studyId,
                        resultMatrixName,
                        technology,
                        description,
                        matrixType,
                        strand,
                        hasDictionary,
                        samplesDimSize,
                        markerDimSize,
                        chrDimSize);

                //CHECK IF JVM IS 32/64 bits to use LFS or not
                int JVMbits = Integer.parseInt(System.getProperty("sun.arch.data.model", "32"));
                if (JVMbits == 64) {
                    netCDFHandler.setLargeFile(true);
                }
                netCDFHandler.setFill(true);

                DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
                netCDF.matrices.MatrixManager.insertMatrixMetadata(dBManager,
                        studyId,
                        friendlyName,
                        resultMatrixName,
                        matrixType,
                        -1,
                        -1,
                        data_location,
                        description,
                        0);

                matrixMetaData = new MatrixMetadata(resultMatrixName);

                resultMatrixId = matrixMetaData.getMatrixId();
            }
        
        }
        
        
        ///// ACCESSORS /////
        public NetcdfFileWriteable getNetCDFHandler(){
            return netCDFHandler;
        }
        public String getResultMatrixName(){
            return resultMatrixName;
        }
        public int getResultMatrixId(){
            return resultMatrixId;
        }
        public MatrixMetadata getResultMatrixMetadata(){
            return matrixMetaData;
        } 
    
        
        public static NetcdfFileWriteable generateNetcdfHandler(Integer studyId,
                                          String matrixName,
                                          String technology, 
                                          String description,
                                          String matrixType,
                                          String strand,
                                          int hasDictionary,
                                          int sampleSetSize,
                                          int markerSetSize,
                                          int chrSetSize) throws InvalidRangeException, IOException {

        ///////////// CREATE netCDF-3 FILE ////////////
        String genotypesFolder = global.Config.getConfigValue("GTdir","");
        File pathToStudy = new File(genotypesFolder+"/STUDY_"+studyId);
        if(!pathToStudy.exists()){
            global.Utils.createFolder(genotypesFolder, "/STUDY_"+studyId);
        }

        int gtStride = cNetCDF.Strides.STRIDE_GT;
        int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
        int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;
//        int strandStride = cNetCDF.Strides.STRIDE_STRAND;
        
        
        String writeFileName = pathToStudy+"/"+matrixName+".nc";
        NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

        //global attributes
        ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyId);
        ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY, technology);
        String versionNb = global.Config.getConfigValue("CURRENT_GWASPIDB_VERSION", "2.0.2");
        ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_GWASPIDB_VERSION, versionNb);
        ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);
        ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STRAND, strand);
        ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY, hasDictionary);

        //dimensions
        Dimension samplesDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESET, 0, true, true, false);
        Dimension markersDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSET, markerSetSize);
        Dimension chrSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_CHRSET, chrSetSize);
        Dimension gtStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_GTSTRIDE, gtStride);
        Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
        Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
//        Dimension dim32 = ncfile.addDimension(cNetCDF.Dimensions.DIM_32, 32);
//        Dimension dim16 = ncfile.addDimension(cNetCDF.Dimensions.DIM_16, 16);
        Dimension dim8 = ncfile.addDimension(cNetCDF.Dimensions.DIM_8, 8);
        Dimension dim4 = ncfile.addDimension(cNetCDF.Dimensions.DIM_4, 4);
        Dimension dim2 = ncfile.addDimension(cNetCDF.Dimensions.DIM_2, 2);
        Dimension dim1 = ncfile.addDimension(cNetCDF.Dimensions.DIM_1, 1);

        //GENOTYPE SPACES
        ArrayList genotypeSpace = new ArrayList();
        genotypeSpace.add(samplesDim);
        genotypeSpace.add(markersDim);
        genotypeSpace.add(gtStrideDim);

        //MARKER SPACES
        ArrayList markerNameSpace = new ArrayList();
        markerNameSpace.add(markersDim);
        markerNameSpace.add(markerStrideDim);

        ArrayList markerPositionSpace = new ArrayList();
        markerPositionSpace.add(markersDim);

        ArrayList markerPropertySpace8 = new ArrayList();
        markerPropertySpace8.add(markersDim);
        markerPropertySpace8.add(dim8);

        ArrayList markerPropertySpace4 = new ArrayList();
        markerPropertySpace4.add(markersDim);
        markerPropertySpace4.add(dim4);

        ArrayList markerPropertySpace2 = new ArrayList();
        markerPropertySpace2.add(markersDim);
        markerPropertySpace2.add(dim2);

        //CHROMOSOME SPACES
        ArrayList chrSetSpace = new ArrayList();
        chrSetSpace.add(chrSetDim);
        chrSetSpace.add(dim8);

        ArrayList chrInfoSpace = new ArrayList();
        chrInfoSpace.add(chrSetDim);
        chrInfoSpace.add(dim4);


        //SAMPLE SPACES
        ArrayList sampleSetSpace = new ArrayList();
        sampleSetSpace.add(samplesDim);
        sampleSetSpace.add(sampleStrideDim);


        //OTHER SPACES
        ArrayList gtEncodingSpace = new ArrayList();
        gtEncodingSpace.add(dim1);
        gtEncodingSpace.add(dim8);

        
        // Define Marker Variables
        ncfile.addVariable(cNetCDF.Variables.VAR_MARKERSET, DataType.CHAR, markerNameSpace);
        ncfile.addVariableAttribute(cNetCDF.Variables.VAR_MARKERSET, cNetCDF.Attributes.LENGTH, markerSetSize);

        ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markerNameSpace);
        ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_CHR, DataType.CHAR, markerPropertySpace8);
        ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_POS, DataType.INT, markerPositionSpace);
        ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, DataType.CHAR, markerPropertySpace2);

        // Define Chromosome Variables
        ncfile.addVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX, DataType.CHAR, chrSetSpace);
        ncfile.addVariable(cNetCDF.Variables.VAR_CHR_INFO, DataType.INT, chrInfoSpace);

        // Define Sample Variables
        ncfile.addVariable(cNetCDF.Variables.VAR_SAMPLESET, DataType.CHAR, sampleSetSpace);
        ncfile.addVariableAttribute(cNetCDF.Variables.VAR_SAMPLESET, cNetCDF.Attributes.LENGTH, sampleSetSize);


        // Define Genotype Variables
        ncfile.addVariable(cNetCDF.Variables.VAR_GENOTYPES, DataType.BYTE, genotypeSpace);
        ncfile.addVariableAttribute(cNetCDF.Variables.VAR_GENOTYPES, cNetCDF.Attributes.GLOB_STRAND, "");
        ncfile.addVariable(cNetCDF.Variables.VAR_GT_STRAND, DataType.CHAR, markerPropertySpace4);

        //ENCODING VARIABLE
        ncfile.addVariable(cNetCDF.Variables.GLOB_GTENCODING, DataType.CHAR, gtEncodingSpace);

        return ncfile;

    }

}

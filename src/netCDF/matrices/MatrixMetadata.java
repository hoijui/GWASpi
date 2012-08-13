
package netCDF.matrices;

import database.DbManager;
import global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixMetadata{
    private int matrixId = Integer.MIN_VALUE;
    private String matrixFriendlyName = "";
    private String matrixNetCDFName = "";
    private String pathToMatrix = "";
    private String technology = "";
    private String gwaspiDBVersion = "";
    private String description = "";
    private String gtEncoding = "";
    private String strand = "";
    private int hasDictionray = -1;
    private int markerSetSize = Integer.MIN_VALUE;
    private int sampleSetSize = Integer.MIN_VALUE;
    private int studyId = Integer.MIN_VALUE;
    private String matrixType = "";         //matrix_type VARCHAR(32) NOT NULL
    private int parentMatrixId1 = Integer.MIN_VALUE;    //parent_matrix1_id INTEGER
    private int parentMatrixId2 = Integer.MIN_VALUE;    //parent_matrix2_id INTEGER
    private String input_location = "";     //input_location VARCHAR(1000)
    private String loaded = "";             //loaded CHAR(1)


    public MatrixMetadata(int _matrixId) throws IOException{
        DbManager dBManager = ServiceLocator.getDbManager(constants.cDBGWASpi.DB_DATACENTER);
        List<Map<String, Object>> rs = null;
        rs = dBManager.executeSelectStatement("SELECT * FROM "+constants.cDBGWASpi.SCH_MATRICES+"."+constants.cDBMatrix.T_MATRICES+" WHERE "+constants.cDBMatrix.f_ID+"="+_matrixId+"  WITH RR");

        matrixId = _matrixId;

        //PREVENT PHANTOM-DB READS EXCEPTIONS
        if(!rs.isEmpty() && rs.get(0).size()==constants.cDBMatrix.T_CREATE_MATRICES.length){
            matrixFriendlyName = (rs.get(0).get(constants.cDBMatrix.f_MATRIX_NAME) != null) ? rs.get(0).get(constants.cDBMatrix.f_MATRIX_NAME).toString() : ""; //matrix_name VARCHAR(64) NOT NULL
            matrixNetCDFName = (rs.get(0).get(constants.cDBMatrix.f_NETCDF_NAME) != null) ? rs.get(0).get(constants.cDBMatrix.f_NETCDF_NAME).toString() : "";   //netcdf_name VARCHAR(64) NOT NULL
            matrixType = (rs.get(0).get(constants.cDBMatrix.f_MATRIX_TYPE) != null) ? rs.get(0).get(constants.cDBMatrix.f_MATRIX_TYPE).toString() : "";         //matrix_type VARCHAR(32) NOT NULL
            parentMatrixId1 = (rs.get(0).get(constants.cDBMatrix.f_PARENT_MATRIX1_ID) != null) ? Integer.parseInt(rs.get(0).get(constants.cDBMatrix.f_PARENT_MATRIX1_ID).toString()) : -1;    //parent_matrix1_id INTEGER
            parentMatrixId2 = (rs.get(0).get(constants.cDBMatrix.f_PARENT_MATRIX2_ID) != null) ? Integer.parseInt(rs.get(0).get(constants.cDBMatrix.f_PARENT_MATRIX2_ID).toString()) : -1;    //parent_matrix2_id INTEGER
            input_location = (rs.get(0).get(constants.cDBMatrix.f_INPUT_LOCATION) != null) ? rs.get(0).get(constants.cDBMatrix.f_INPUT_LOCATION).toString() : "";     //input_location VARCHAR(1000)
            description = (rs.get(0).get(constants.cDBMatrix.f_DESCRIPTION) != null) ? rs.get(0).get(constants.cDBMatrix.f_DESCRIPTION).toString() : "";        //description VARCHAR(2000)
            loaded = (rs.get(0).get(constants.cDBMatrix.f_LOADED) != null) ? rs.get(0).get(constants.cDBMatrix.f_LOADED).toString() : "0";             //loaded CHAR(1)
            studyId = (rs.get(0).get(constants.cDBMatrix.f_STUDYID) != null) ? Integer.parseInt(rs.get(0).get(constants.cDBMatrix.f_STUDYID).toString()) : 0;
        }


        String genotypesFolder = global.Config.getConfigValue("GTdir","");
        String pathToStudy = genotypesFolder+"/STUDY_"+studyId+"/";
        pathToMatrix=pathToStudy+matrixNetCDFName+".nc";
        NetcdfFile ncfile = null;
        if (new File(pathToMatrix).exists()) {
            try {
                ncfile = NetcdfFile.open(pathToMatrix);

                technology = ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_TECHNOLOGY).getStringValue();
                try{
                    gwaspiDBVersion = ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_GWASPIDB_VERSION).getStringValue();
                }catch(Exception e){}
                

                Variable var = ncfile.findVariable(constants.cNetCDF.Variables.GLOB_GTENCODING);
                if (var != null ) {
                    try {
                        ArrayChar.D2 gtCodeAC = (ArrayChar.D2) var.read("(0:0:1, 0:7:1)");
                        gtEncoding = gtCodeAC.getString(0);
                    } catch (InvalidRangeException ex) {
                        Logger.getLogger(MatrixMetadata.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                strand = ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_STRAND).getStringValue();
                hasDictionray = (Integer) ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_HAS_DICTIONARY).getNumericValue();

                Dimension markerSetDim = ncfile.findDimension(constants.cNetCDF.Dimensions.DIM_MARKERSET);
                markerSetSize = markerSetDim.getLength();

                Dimension sampleSetDim = ncfile.findDimension(constants.cNetCDF.Dimensions.DIM_SAMPLESET);
                sampleSetSize = sampleSetDim.getLength();

            } catch (IOException ioe) {
                System.out.println("Cannot open file: " + ioe);
            } finally {
                if (null != ncfile) {
                    try {
                        ncfile.close();
                    } catch (IOException ioe) {
                        System.out.println("Cannot close file: " + ioe);
                    }

                }
            }
        }

    }
    
    public MatrixMetadata(String netCDFname) throws IOException{
        DbManager dBManager = ServiceLocator.getDbManager(constants.cDBGWASpi.DB_DATACENTER);
        List<Map<String, Object>> rs = null;
        rs = dBManager.executeSelectStatement("SELECT * FROM "+constants.cDBGWASpi.SCH_MATRICES+"."+constants.cDBMatrix.T_MATRICES+" WHERE "+constants.cDBMatrix.f_NETCDF_NAME+"='"+netCDFname+"'  WITH RR");
//        rs = dBManager.executeSelectStatement("SELECT * FROM "+constants.cDBGWASpi.SCH_MATRICES+"."+constants.cDBMatrix.T_MATRICES+" WHERE "+constants.cDBMatrix.f_INPUT_LOCATION+"='"+netCDFname+"'  WITH RR");
        
        //PREVENT PHANTOM-DB READS EXCEPTIONS
        if(!rs.isEmpty() && rs.get(0).size()==constants.cDBMatrix.T_CREATE_MATRICES.length){
            matrixId = Integer.parseInt(rs.get(0).get(constants.cDBMatrix.f_ID).toString());
            matrixFriendlyName = (rs.get(0).get(constants.cDBMatrix.f_MATRIX_NAME) != null) ? rs.get(0).get(constants.cDBMatrix.f_MATRIX_NAME).toString() : ""; //matrix_name VARCHAR(64) NOT NULL
            matrixNetCDFName = (rs.get(0).get(constants.cDBMatrix.f_NETCDF_NAME) != null) ? rs.get(0).get(constants.cDBMatrix.f_NETCDF_NAME).toString() : "";   //netcdf_name VARCHAR(64) NOT NULL
            matrixType = (rs.get(0).get(constants.cDBMatrix.f_MATRIX_TYPE) != null) ? rs.get(0).get(constants.cDBMatrix.f_MATRIX_TYPE).toString() : "";         //matrix_type VARCHAR(32) NOT NULL
            parentMatrixId1 = (rs.get(0).get(constants.cDBMatrix.f_PARENT_MATRIX1_ID) != null) ? Integer.parseInt(rs.get(0).get(constants.cDBMatrix.f_PARENT_MATRIX1_ID).toString()) : -1;    //parent_matrix1_id INTEGER
            parentMatrixId2 = (rs.get(0).get(constants.cDBMatrix.f_PARENT_MATRIX2_ID) != null) ? Integer.parseInt(rs.get(0).get(constants.cDBMatrix.f_PARENT_MATRIX2_ID).toString()) : -1;    //parent_matrix2_id INTEGER
            input_location = (rs.get(0).get(constants.cDBMatrix.f_INPUT_LOCATION) != null) ? rs.get(0).get(constants.cDBMatrix.f_INPUT_LOCATION).toString() : "";     //input_location VARCHAR(1000)
            description = (rs.get(0).get(constants.cDBMatrix.f_DESCRIPTION) != null) ? rs.get(0).get(constants.cDBMatrix.f_DESCRIPTION).toString() : "";        //description VARCHAR(2000)
            loaded = (rs.get(0).get(constants.cDBMatrix.f_LOADED) != null) ? rs.get(0).get(constants.cDBMatrix.f_LOADED).toString() : "0";             //loaded CHAR(1)
            studyId = (rs.get(0).get(constants.cDBMatrix.f_STUDYID) != null) ? Integer.parseInt(rs.get(0).get(constants.cDBMatrix.f_STUDYID).toString()) : 0;

        }

        String genotypesFolder = global.Config.getConfigValue("GTdir","");
        String pathToStudy = genotypesFolder+"/STUDY_"+studyId+"/";
        pathToMatrix=pathToStudy+matrixNetCDFName+".nc";
        NetcdfFile ncfile = null;
        if (new File(pathToMatrix).exists()) {
            try {
                ncfile = NetcdfFile.open(pathToMatrix);

                technology = ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_TECHNOLOGY).getStringValue();
                try{
                    gwaspiDBVersion = ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_GWASPIDB_VERSION).getStringValue();
                }catch(Exception e){}

                Variable var = ncfile.findVariable(constants.cNetCDF.Variables.GLOB_GTENCODING);
                if (var != null ) {
                    try {
                        ArrayChar.D2 gtCodeAC = (ArrayChar.D2) var.read("(0:0:1, 0:7:1)");
                        gtEncoding = gtCodeAC.getString(0);
                    } catch (InvalidRangeException ex) {
                        Logger.getLogger(MatrixMetadata.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                strand = ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_STRAND).getStringValue();
                hasDictionray = (Integer) ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_HAS_DICTIONARY).getNumericValue();
                
                Dimension markerSetDim = ncfile.findDimension(constants.cNetCDF.Dimensions.DIM_MARKERSET);
                markerSetSize = markerSetDim.getLength();

                Dimension sampleSetDim = ncfile.findDimension(constants.cNetCDF.Dimensions.DIM_SAMPLESET);
                sampleSetSize = sampleSetDim.getLength();

            } catch (IOException ioe) {
                System.out.println("Cannot open file: " + ioe);
            } finally {
                if (null != ncfile) {
                    try {
                        ncfile.close();
                    } catch (IOException ioe) {
                        System.out.println("Cannot close file: " + ioe);
                    }

                }
            }
        }


    }

    //**
    /* This Method used to import GWASpi matrix from an external file
     * The size of this LHM is very small.
     */
    public MatrixMetadata(String netCDFpath, int _studyId, String newMatrixName) throws IOException{
        matrixId = Integer.MIN_VALUE;
        matrixFriendlyName = newMatrixName;
        matrixNetCDFName = netCDF.matrices.MatrixManager.generateMatrixNetCDFNameByDate();
        studyId = _studyId;

        pathToMatrix=netCDFpath;
        NetcdfFile ncfile = null;
        if (new File(pathToMatrix).exists()) {
            try {
                ncfile = NetcdfFile.open(pathToMatrix);

                technology = ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_TECHNOLOGY).getStringValue();
                try{
                    gwaspiDBVersion = ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_GWASPIDB_VERSION).getStringValue();
                }catch(Exception e){}

                Variable var = ncfile.findVariable(constants.cNetCDF.Variables.GLOB_GTENCODING);
                if (var != null ) {
                    try {
                        ArrayChar.D2 gtCodeAC = (ArrayChar.D2) var.read("(0:0:1, 0:7:1)");
                        gtEncoding = gtCodeAC.getString(0);
                    } catch (InvalidRangeException ex) {
                        Logger.getLogger(MatrixMetadata.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                strand = ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_STRAND).getStringValue();
                hasDictionray = (Integer) ncfile.findGlobalAttribute(constants.cNetCDF.Attributes.GLOB_HAS_DICTIONARY).getNumericValue();

                Dimension markerSetDim = ncfile.findDimension(constants.cNetCDF.Dimensions.DIM_MARKERSET);
                markerSetSize = markerSetDim.getLength();

                Dimension sampleSetDim = ncfile.findDimension(constants.cNetCDF.Dimensions.DIM_SAMPLESET);
                sampleSetSize = sampleSetDim.getLength();

            } catch (IOException ioe) {
                System.out.println("Cannot open file: " + ioe);
            } finally {
                if (null != ncfile) {
                    try {
                        ncfile.close();
                    } catch (IOException ioe) {
                        System.out.println("Cannot close file: " + ioe);
                    }

                }
            }
        }
    }

    public int getHasDictionray() {
        return hasDictionray;
    }

    public int getMatrixId() {
            return matrixId;
    }
    public int getStudyId() {
            return studyId;
    }
    public String getMatrixFriendlyName() {
            return matrixFriendlyName;
    }
    public String getTechnology() {
        return technology;
    }
    public String getGwaspiDBVersion() {
        return gwaspiDBVersion;
    }
    public String getGenotypeEncoding() {
        return gtEncoding;
    }
    public int getMarkerSetSize() {
        return markerSetSize;
    }
    public int getSampleSetSize() {
        return sampleSetSize;
    }
    public String getPathToMatrix() {
        return pathToMatrix;
    }
    public String getStrand() {
        return strand;
    }

    public String getDescription() {
        return description;
    }


    public String getInput_location() {
        return input_location;
    }

    public String getLoaded() {
        return loaded;
    }

    public String getMatrixNetCDFName() {
        return matrixNetCDFName;
    }

    public String getMatrixType() {
        return matrixType;
    }

    public int getParentMatrixId1() {
        return parentMatrixId1;
    }

    public int getParentMatrixId2() {
        return parentMatrixId2;
    }

}

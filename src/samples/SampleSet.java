package samples;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import netCDF.matrices.*;
import ucar.ma2.*;
import ucar.nc2.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SampleSet {

    //SAMPLESET_MEATADATA
    private int sampleset_id = Integer.MIN_VALUE;   //id
    private int sampleSetSize = 0;
    private MatrixMetadata matrixMetadata;
        
    private LinkedHashMap sampleIdSetLHM = new LinkedHashMap();
    
    
    public SampleSet(int studyId, int matrixId) throws IOException{
        matrixMetadata = new MatrixMetadata(matrixId);
        sampleSetSize = matrixMetadata.getMarkerSetSize();
        
    }

    public SampleSet(int studyId, String netCDFName) throws IOException{
        matrixMetadata = new MatrixMetadata(netCDFName);
        sampleSetSize = matrixMetadata.getMarkerSetSize();

    }
    
    public SampleSet(int studyId, String netCDFPath, String netCDFName) throws IOException{
        matrixMetadata = new MatrixMetadata(netCDFPath, studyId, netCDFName);
        sampleSetSize = matrixMetadata.getMarkerSetSize();

    }
    
    ////////// ACCESSORS /////////
    
    public int getSampleSetId() {
            return sampleset_id;
    }
    public int getSampleSetSize() {
            return sampleSetSize;
    }
    
    public MatrixMetadata getMatrixMetadata(ArrayList _sampleSetAL) {
            return matrixMetadata;
    }


    //<editor-fold defaultstate="collapsed" desc="SAMPLESET FETCHERS">

    public LinkedHashMap getSampleIdSetLHM() throws InvalidRangeException {
        NetcdfFile ncfile = null;

        try{
            ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
            Variable var = ncfile.findVariable(constants.cNetCDF.Variables.VAR_SAMPLESET);

            if (null == var) return null;
            
            int[] varShape = var.getShape();
            Dimension markerSetDim = ncfile.findDimension(constants.cNetCDF.Dimensions.DIM_SAMPLESET);
            
            try {
                sampleSetSize = markerSetDim.getLength();
                ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:"+(sampleSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");

                sampleIdSetLHM = netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(sampleSetAC);

            } catch (IOException ioe) {
                System.out.println("Cannot read data: "+ioe);
            } catch (InvalidRangeException e) {
                System.out.println("Cannot read data: "+e);
            }

        } catch (IOException ioe) {
            System.out.println("Cannot open file: "+ioe);
        } finally {
            if (null != ncfile) try {
                ncfile.close();
            } catch (IOException ioe) {
                System.out.println("Cannot close file: "+ioe);
            }
        }

        return sampleIdSetLHM;
    }

    public LinkedHashMap getSampleIdSetLHM(String matrixImportPath) throws InvalidRangeException {
        NetcdfFile ncfile = null;

        try{
            ncfile = NetcdfFile.open(matrixImportPath);
            Variable var = ncfile.findVariable(constants.cNetCDF.Variables.VAR_SAMPLESET);

            if (null == var) return null;
            
            int[] varShape = var.getShape();
            Dimension markerSetDim = ncfile.findDimension(constants.cNetCDF.Dimensions.DIM_SAMPLESET);
            
            try {
                sampleSetSize = markerSetDim.getLength();
                ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:"+(sampleSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");

                sampleIdSetLHM = netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(sampleSetAC);

            } catch (IOException ioe) {
                System.out.println("Cannot read data: "+ioe);
            } catch (InvalidRangeException e) {
                System.out.println("Cannot read data: "+e);
            }

        } catch (IOException ioe) {
            System.out.println("Cannot open file: "+ioe);
        } finally {
            if (null != ncfile) try {
                ncfile.close();
            } catch (IOException ioe) {
                System.out.println("Cannot close file: "+ioe);
            }
        }

        return sampleIdSetLHM;
    }


    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="SAMPLESET FILLERS">

    public LinkedHashMap readAllSamplesGTsFromCurrentMarkerToLHM(NetcdfFile rdNcFile, LinkedHashMap rdLhm, int markerNb) throws IOException{
        try {
            Variable genotypes = rdNcFile.findVariable(constants.cNetCDF.Variables.VAR_GENOTYPES);

            if (null == genotypes) {return rdLhm;}
            try {
                int[] varShape = genotypes.getShape();
//                Dimension markerSetDim = ncfile.findDimension(constants.cNetCDF.DIM_MARKERSET);
//                ArrayChar.D3 gt_ACD3 = (ArrayChar.D3) genotypes.read("(" + markerNb + ":" + markerNb + ":1, 0:" + (markerSetDim.getLength() - 1) + ":1, 0:" + (varShape[2] - 1) + ":1)");

                Dimension sampleSetDim = rdNcFile.findDimension(constants.cNetCDF.Dimensions.DIM_SAMPLESET);
//                ArrayChar.D3 gt_ACD3 = (ArrayChar.D3) genotypes.read("(0:" + (sampleSetDim.getLength() - 1) + ":1, "
//                                                                     + markerNb + ":" + markerNb + ":1, "
//                                                                     + "0:" + (varShape[2] - 1) + ":1)");

                ArrayByte.D3 gt_ACD3 = (ArrayByte.D3) genotypes.read("(0:" + (sampleSetDim.getLength() - 1) + ":1, "
                                                                     + markerNb + ":" + markerNb + ":1, "
                                                                     + "0:" + (varShape[2] - 1) + ":1)");

                int[] shp = gt_ACD3.getShape();
                int reducer = 0;
                if(shp[0]==1){reducer++;}
                if(shp[1]==1){reducer++;}
                if(shp[2]==1){reducer++;}

                if(reducer==1){
                    ArrayByte.D2 gt_ACD2 = (ArrayByte.D2) gt_ACD3.reduce();
                    rdLhm = netCDF.operations.Utils.writeD2ArrayByteToLHMValues(gt_ACD2, rdLhm);
                } else if(reducer==2){
                    ArrayByte.D1 gt_ACD1 = (ArrayByte.D1) gt_ACD3.reduce();
                    rdLhm = netCDF.operations.Utils.writeD1ArrayByteToLHMValues(gt_ACD1, rdLhm);
                }

//                ArrayChar.D2 gt_ACD2 = (ArrayChar.D2) gt_ACD3.reduce();
//                rdLhm = netCDF.operations.Utils.writeD2ArrayCharToLHMValues(gt_ACD2, rdLhm);

            } catch (InvalidRangeException e) {
                System.out.println("Cannot read data: " + e);
            }
        } catch (IOException iOException) {
            System.out.println("Cannot open file: " + iOException);
        }

        return rdLhm;
    }



    public LinkedHashMap fillSampleIdSetLHMWithVariable(NetcdfFile ncfile, String variable) {
        try{
            Variable var = ncfile.findVariable(variable);

            if (null == var) return null;

            DataType dataType = var.getDataType();
            int[] varShape = var.getShape();
            
            sampleSetSize = varShape[0];
            if(dataType==DataType.CHAR){
                ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:"+(sampleSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");
                sampleIdSetLHM = netCDF.operations.Utils.writeD2ArrayCharToLHMValues(sampleSetAC, sampleIdSetLHM);
            }
            if(dataType==DataType.DOUBLE){
                ArrayDouble.D1 sampleSetAF = (ArrayDouble.D1) var.read("(0:"+(sampleSetSize-1)+":1");
                sampleIdSetLHM = netCDF.operations.Utils.writeD1ArrayDoubleToLHMValues(sampleSetAF, sampleIdSetLHM);
            }

        } catch (IOException ioe) {
            System.out.println("Cannot open file: "+ioe);
        } catch (InvalidRangeException e) {
                System.out.println("Cannot read data: "+e);
            }finally {
            if (null != ncfile) try {
                ncfile.close();
            } catch (IOException ioe) {
                System.out.println("Cannot close file: "+ioe);
            }
        }

        return sampleIdSetLHM;
    }


    public LinkedHashMap fillSampleIdSetLHMWithVariable(LinkedHashMap lhm, String variable) {
        NetcdfFile ncfile = null;

        try{
            ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
            Variable var = ncfile.findVariable(variable);

            if (null == var) return null;
            
            DataType dataType = var.getDataType();
            int[] varShape = var.getShape();
            Dimension sampleSetDim = ncfile.findDimension(constants.cNetCDF.Dimensions.DIM_SAMPLESET);
            
            try {
                sampleSetSize = sampleSetDim.getLength();
                if(dataType==DataType.CHAR){
                    ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:"+(sampleSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");
                    sampleIdSetLHM = netCDF.operations.Utils.writeD2ArrayCharToLHMValues(sampleSetAC, lhm);
                }
                if(dataType==DataType.DOUBLE){
                    ArrayDouble.D1 sampleSetAF = (ArrayDouble.D1) var.read("(0:"+(sampleSetSize-1)+":1");
                    sampleIdSetLHM = netCDF.operations.Utils.writeD1ArrayDoubleToLHMValues(sampleSetAF, lhm);
                }

            } catch (IOException ioe) {
                System.out.println("Cannot read data: "+ioe);
            } catch (InvalidRangeException e) {
                System.out.println("Cannot read data: "+e);
            }

        } catch (IOException ioe) {
            System.out.println("Cannot open file: "+ioe);
        } finally {
            if (null != ncfile) try {
                ncfile.close();
            } catch (IOException ioe) {
                System.out.println("Cannot close file: "+ioe);
            }
        }

        return sampleIdSetLHM;
    }

    public LinkedHashMap fillSampleIdSetLHMWithFilterVariable(LinkedHashMap lhm, String variable, int filterPos) {
        NetcdfFile ncfile = null;

        try{
            ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
            Variable var = ncfile.findVariable(variable);

            if (null == var) return null;
            
            DataType dataType = var.getDataType();
            int[] varShape = var.getShape();
            Dimension sampleSetDim = ncfile.findDimension(constants.cNetCDF.Dimensions.DIM_SAMPLESET);
            
            try {
                sampleSetSize = sampleSetDim.getLength();
                if(dataType==DataType.CHAR){
                    ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:"+(sampleSetSize-1)+":1, "+filterPos+":"+filterPos+":1)");
                    sampleIdSetLHM = netCDF.operations.Utils.writeD2ArrayCharToLHMValues(sampleSetAC, lhm);
                }
            } catch (IOException ioe) {
                System.out.println("Cannot read data: "+ioe);
            } catch (InvalidRangeException e) {
                System.out.println("Cannot read data: "+e);
            }

        } catch (IOException ioe) {
            System.out.println("Cannot open file: "+ioe);
        } finally {
            if (null != ncfile) try {
                ncfile.close();
            } catch (IOException ioe) {
                System.out.println("Cannot close file: "+ioe);
            }
        }

        return sampleIdSetLHM;
    }



    public LinkedHashMap fillLHMWithDefaultValue(LinkedHashMap lhm, Object defaultVal){
        for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            lhm.put(key, defaultVal);
        }
        return lhm;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="SAMPLESET PICKERS">
    public LinkedHashMap pickValidSampleSetItemsByDBField(Object poolId, LinkedHashMap lhm, String dbField, HashSet criteria, boolean include) throws IOException{
        LinkedHashMap returnLHM = new LinkedHashMap();
        List<Map<String, Object>> rs = samples.SampleManager.getAllSampleInfoFromDBByPoolID(poolId);

        int pickCounter=0;
        if (include) {
            for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                for (int i = 0; i < rs.size(); i++) // loop through rows of result set
                {
                    //PREVENT PHANTOM-DB READS EXCEPTIONS - CAUTION!!
                    if(!rs.isEmpty() && rs.get(i).size()==constants.cDBSamples.T_CREATE_SAMPLES_INFO.length){
                        if (rs.get(i).get(constants.cDBSamples.f_SAMPLE_ID).toString().equals(key.toString())) {
                            if (criteria.contains(rs.get(i).get(dbField).toString())) {
                                returnLHM.put(key, pickCounter);
                            }
                        }
                    }
                }
                pickCounter++;
            }
        } else {
            for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                for (int i = 0; i < rs.size(); i++) // loop through rows of result set
                {
                    //PREVENT PHANTOM-DB READS EXCEPTIONS - CAUTION!!
                    if(!rs.isEmpty() && rs.get(i).size()==constants.cDBSamples.T_CREATE_SAMPLES_INFO.length){
                        if (rs.get(i).get(constants.cDBSamples.f_SAMPLE_ID).toString().equals(key.toString())) {
                            if (!criteria.contains(rs.get(i).get(dbField).toString())) {
                                returnLHM.put(key, pickCounter);
                            }
                        }
                    }
                }
                pickCounter++;
            }
        }

        return returnLHM;
    }

    public LinkedHashMap pickValidSampleSetItemsByNetCDFValue(LinkedHashMap lhm, String variable, HashSet criteria, boolean include){
        LinkedHashMap returnLHM = new LinkedHashMap();
        lhm = this.fillSampleIdSetLHMWithVariable(lhm, variable);

        int pickCounter=0;
        if (include) {
            for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = lhm.get(key);
                if (criteria.contains(value)) {
                    returnLHM.put(key, pickCounter);
                }
                pickCounter++;
            }
        } else {
            for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = lhm.get(key);
                if (!criteria.contains(value)) {
                    returnLHM.put(key, pickCounter);
                }
                pickCounter++;
            }
        }

        return returnLHM;
    }

    public LinkedHashMap pickValidSampleSetItemsByNetCDFFilter(LinkedHashMap lhm, String variable, int fiterPos, HashSet criteria, boolean include){
        LinkedHashMap returnLHM = new LinkedHashMap();
        lhm = this.fillSampleIdSetLHMWithFilterVariable(lhm, variable, fiterPos);

        int pickCounter=0;
        if (include) {
            for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = lhm.get(key);
                if (criteria.contains(value)) {
                    returnLHM.put(key, pickCounter);
                }
                pickCounter++;
            }
        } else {
            for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = lhm.get(key);
                if (!criteria.contains(value)) {
                    returnLHM.put(key, pickCounter);
                }
                pickCounter++;
            }
        }

        return returnLHM;
    }

    public LinkedHashMap pickValidSampleSetItemsByNetCDFKey(LinkedHashMap lhm, HashSet criteria, boolean include) throws IOException{
        LinkedHashMap returnLHM = new LinkedHashMap();

        int pickCounter=0;
        if (include) {
            for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                if (criteria.contains(key)) {
                    returnLHM.put(key, pickCounter);
                }
                pickCounter++;
            }
        } else {
            for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                if (!criteria.contains(key)) {
                    returnLHM.put(key, pickCounter);
                }
                pickCounter++;
            }
        }

        return returnLHM;
    }
    //</editor-fold>


    
}

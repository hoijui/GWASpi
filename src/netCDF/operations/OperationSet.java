package netCDF.operations;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import ucar.ma2.*;
import ucar.nc2.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OperationSet {

    //MARKERSET_MEATADATA
    private int opSetSize = 0;
    private int implicitSetSize = 0;
    private OperationMetadata opMetadata;
    
    private LinkedHashMap opSetLHM = new LinkedHashMap();
    private LinkedHashMap opRsIdSetLHM = new LinkedHashMap();

    
    public OperationSet(int studyId, int opId) throws IOException{
        opMetadata = new OperationMetadata(opId);
        opSetSize = opMetadata.getOpSetSize();
    }


    ////////// ACCESSORS /////////
    public int getOpSetSize() {
            return opSetSize;
    }

    public int getImplicitSetSize() {
        return implicitSetSize;
    }


    //<editor-fold defaultstate="collapsed" desc="OPERATION-SET FETCHERS">

    public LinkedHashMap getOpSetLHM() {
        NetcdfFile ncfile = null;

        try{
            ncfile = NetcdfFile.open(opMetadata.getPathToMatrix());
            Variable var = ncfile.findVariable(constants.cNetCDF.Variables.VAR_OPSET);
            
            if (null == var) return null;
            
            DataType dataType = var.getDataType();
            int[] varShape = var.getShape();
            
            try {
                opSetSize = varShape[0];

                if(dataType==DataType.CHAR){
                    ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:"+(opSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");
                    opSetLHM = netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
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

        return opSetLHM;
    }

    public LinkedHashMap getMarkerRsIdSetLHM() {
        NetcdfFile ncfile = null;

        try{
            ncfile = NetcdfFile.open(opMetadata.getPathToMatrix());
            Variable var = ncfile.findVariable(constants.cNetCDF.Variables.VAR_MARKERS_RSID);

            if (null == var) return null;
            
            DataType dataType = var.getDataType();
            int[] varShape = var.getShape();
            
            try {
                opSetSize = varShape[0];
                if(dataType==DataType.CHAR){
                    ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:"+(opSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");
                    opSetLHM = netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
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

        return opRsIdSetLHM;
    }


    LinkedHashMap getImplicitSetLHM() {
        NetcdfFile ncfile = null;
        LinkedHashMap implicitSetLHM = new LinkedHashMap();

        try{
            ncfile = NetcdfFile.open(opMetadata.getPathToMatrix());
            Variable var = ncfile.findVariable(constants.cNetCDF.Variables.VAR_IMPLICITSET);
            
            if (null == var) return null;
            
            int[] varShape = var.getShape();
            try {
                implicitSetSize = varShape[0];
                ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:"+(implicitSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");

                implicitSetLHM = netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(sampleSetAC);

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

        return implicitSetLHM;
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CHROMOSOME INFO">

    public LinkedHashMap getChrInfoSetLHM() {
        NetcdfFile ncfile = null;
        LinkedHashMap chrInfoLHM = new LinkedHashMap();

        try{
            ncfile = NetcdfFile.open(opMetadata.getPathToMatrix());

            //GET NAMES OF CHROMOSOMES
            Variable var = ncfile.findVariable(constants.cNetCDF.Variables.VAR_CHR_IN_MATRIX);
            
            if (null == var) return null;
            
            DataType dataType = var.getDataType();
            int[] varShape = var.getShape();
            
            try {
                if(dataType==DataType.CHAR){
                    ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:"+(varShape[0]-1)+":1, 0:7:1)");
                    chrInfoLHM = netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
                }
            } catch (IOException ioe) {
                System.out.println("Cannot read data: "+ioe);
            } catch (InvalidRangeException e) {
                System.out.println("Cannot read data: "+e);
            }

            //GET INFO FOR EACH CHROMOSOME
            var = ncfile.findVariable(constants.cNetCDF.Variables.VAR_CHR_INFO); //Nb of markers, first physical position, last physical position, start index number in MarkerSet
            dataType = var.getDataType();
            varShape = var.getShape();

            if (null == var) return null;
            try {
                if(dataType==DataType.INT){
                    ArrayInt.D2 chrSetAI = (ArrayInt.D2) var.read("(0:"+(varShape[0]-1)+":1, 0:3:1)");
                    chrInfoLHM = netCDF.operations.Utils.writeD2ArrayIntToLHMValues(chrSetAI, chrInfoLHM);
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

        return chrInfoLHM;
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="OPERATION-SET FILLERS">

    public LinkedHashMap fillOpSetLHMWithVariable(NetcdfFile ncfile, String variable) {

        Variable var = ncfile.findVariable(variable);
        
        if (null == var) return null;
        
        DataType dataType = var.getDataType();
        int[] varShape = var.getShape();
        try {
            opSetSize = varShape[0];
            if(dataType==DataType.CHAR){
                if(varShape.length==2){
                    ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:"+(opSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");
                    opSetLHM = netCDF.operations.Utils.writeD2ArrayCharToLHMValues(markerSetAC, opSetLHM);
                }
            }
            if(dataType==DataType.DOUBLE){
                if(varShape.length==1){
                    ArrayDouble.D1 markerSetAF = (ArrayDouble.D1) var.read("(0:"+(opSetSize-1)+":1)");
                    opSetLHM = netCDF.operations.Utils.writeD1ArrayDoubleToLHMValues(markerSetAF, opSetLHM);
                }
                if(varShape.length==2){
                    ArrayDouble.D2 markerSetAF = (ArrayDouble.D2) var.read("(0:"+(opSetSize-1)+":1, 0:"+(varShape[1]-1)+":1))");
                    opSetLHM = netCDF.operations.Utils.writeD2ArrayDoubleToLHMValues(markerSetAF, opSetLHM);
                }
            }
            if(dataType==DataType.INT){
                if(varShape.length==1){
                    ArrayInt.D1 markerSetAD = (ArrayInt.D1) var.read("(0:"+(opSetSize-1)+":1)");
                    opSetLHM = netCDF.operations.Utils.writeD1ArrayIntToLHMValues(markerSetAD, opSetLHM);
                }
                if(varShape.length==2){
                    ArrayInt.D2 markerSetAD = (ArrayInt.D2) var.read("(0:"+(opSetSize-1)+":1, 0:"+(varShape[1]-1)+":1))");
                    opSetLHM = netCDF.operations.Utils.writeD2ArrayIntToLHMValues(markerSetAD, opSetLHM);
                }
            }

        } catch (IOException ioe) {
            System.err.println("Cannot read data: "+ioe);
        } catch (InvalidRangeException e) {
            System.err.println("Cannot read data: "+e);
            e.printStackTrace();
        }

        return opSetLHM;
    }

    public LinkedHashMap fillLHMWithDefaultValue(LinkedHashMap lhm, Object defaultVal){
        for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            lhm.put(key, defaultVal);
        }
        return lhm;
    }

    public LinkedHashMap fillWrLHMWithRdLHMValue(LinkedHashMap wrLHM, LinkedHashMap rdLHM){
        for (Iterator it = wrLHM.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            Object value = rdLHM.get(key);
            wrLHM.put(key, value);
        }
        return wrLHM;
    }

    public ArrayList getALWithVariable(NetcdfFile ncfile, String variable){

        ArrayList al = new ArrayList();
        
        Variable var = ncfile.findVariable(variable);
        if (null == var) return null;

        DataType dataType = var.getDataType();
        int[] varShape = var.getShape();
        opSetSize = varShape[0];
        al.ensureCapacity(opSetSize);
        
        try {
            if(dataType==DataType.CHAR){
                if(varShape.length==2){
                    ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:"+(opSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");
                    al = netCDF.operations.Utils.writeD2ArrayCharToAL(markerSetAC);
                }
            }
            if(dataType==DataType.DOUBLE){
                if(varShape.length==1){
                    ArrayDouble.D1 markerSetAD = (ArrayDouble.D1) var.read("(0:"+(opSetSize-1)+":1)");
                    al = netCDF.operations.Utils.writeD1ArrayDoubleToAL(markerSetAD);
                }
                if(varShape.length==2){
                    ArrayDouble.D2 markerSetAD = (ArrayDouble.D2) var.read("(0:"+(opSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");
                    al = netCDF.operations.Utils.writeD2ArrayDoubleToAL(markerSetAD);
                }
            }

        } catch (IOException ioe) {
            System.err.println("Cannot read data: "+ioe);
        } catch (InvalidRangeException e) {
            System.err.println("Cannot read data: "+e);
        }

        return al;
    }

    public LinkedHashMap appendVariableToMarkerSetLHMValue(NetcdfFile ncfile, String variable, String separator) {

        Variable var = ncfile.findVariable(variable);

        if (null == var) return null;
        DataType dataType = var.getDataType();
        int[] varShape = var.getShape();
        
        try {
            opSetSize = varShape[0];
            if(dataType==DataType.CHAR){
                if(varShape.length==2){
                    ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:"+(opSetSize-1)+":1, 0:"+(varShape[1]-1)+":1)");

                    int[] shape = markerSetAC.getShape();
                    Index index = markerSetAC.getIndex();
                    Iterator it = opSetLHM.keySet().iterator();
                    for (int i=0; i<shape[0]; i++) {
                        Object key = it.next();
                        String value = opSetLHM.get(key).toString();
                        if(!value.isEmpty()){
                            value+=separator;
                        }
                        StringBuilder newValue = new StringBuilder();
                        for (int j=0; j<shape[1]; j++) {
                            newValue.append(markerSetAC.getChar(index.set(i,j)));
                        }
                        opSetLHM.put(key, value+newValue.toString().trim());
                    }
                }
            }
            if(dataType==DataType.FLOAT){
                if(varShape.length==1){
                    ArrayFloat.D1 markerSetAF = (ArrayFloat.D1) var.read("(0:"+(opSetSize-1)+":1)");

                    Float floatValue = Float.NaN;
                    int[] shape = markerSetAF.getShape();
                    Index index = markerSetAF.getIndex();
                    Iterator it = opSetLHM.keySet().iterator();
                    for (int i=0; i<shape[0]; i++) {
                        Object key = it.next();
                        String value = opSetLHM.get(key).toString();
                        if(!value.isEmpty()){
                            value+=separator;
                        }
                        floatValue = markerSetAF.getFloat(index.set(i));
                        opSetLHM.put(key, value+floatValue.toString());
                    }
                }
            }
            if(dataType==DataType.INT){
                if(varShape.length==1){
                    ArrayInt.D1 markerSetAF = (ArrayInt.D1) var.read("(0:"+(opSetSize-1)+":1)");

                    Integer intValue = 0;
                    int[] shape = markerSetAF.getShape();
                    Index index = markerSetAF.getIndex();
                    Iterator it = opSetLHM.keySet().iterator();
                    for (int i=0; i<shape[0]; i++) {
                        Object key = it.next();
                        String value = opSetLHM.get(key).toString();
                        if(!value.isEmpty()){
                            value+=separator;
                        }
                        intValue = markerSetAF.getInt(index.set(i));
                        opSetLHM.put(key, value+intValue.toString());
                    }
                }
            }

        } catch (IOException ioe) {
            System.err.println("Cannot read data: "+ioe);
        } catch (InvalidRangeException e) {
            System.err.println("Cannot read data: "+e);
        }

        return opSetLHM;
    }


    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="OPERATION-SET PICKERS">
    public LinkedHashMap pickValidMarkerSetItemsByValue(NetcdfFile ncfile, String variable, HashSet criteria, boolean includes){
        LinkedHashMap returnLHM = new LinkedHashMap();
        LinkedHashMap readLhm = this.fillOpSetLHMWithVariable(ncfile, variable);

        if (includes) {
            for (Iterator it = readLhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = readLhm.get(key);
                if (criteria.contains(value)) {
                    returnLHM.put(key, value);
                }
            }
        } else {
            for (Iterator it = readLhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = readLhm.get(key);
                if (!criteria.contains(value)) {
                    returnLHM.put(key, value);
                }
            }
        }

        return returnLHM;
    }


    public LinkedHashMap pickValidMarkerSetItemsByKey(LinkedHashMap lhm, HashSet criteria, boolean includes){
        LinkedHashMap returnLHM = new LinkedHashMap();

        if (includes) {
            for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = lhm.get(key);
                if (criteria.contains(key)) {
                    returnLHM.put(key, value);
                }
            }
        } else {
            for (Iterator it = lhm.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = lhm.get(key);
                if (!criteria.contains(key)) {
                    returnLHM.put(key, value);
                }
            }
        }

        return returnLHM;
    }

    //</editor-fold>


    //////////// HELPER METHODS ///////////
    

    


}

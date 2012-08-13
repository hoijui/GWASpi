package reports;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import netCDF.operations.OperationMetadata;

import netCDF.operations.OperationSet;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class GatherHardyWeinbergData {


    //<editor-fold defaultstate="collapsed" desc="HARDY-WEINBERG REPORT METHODS">

    public static LinkedHashMap loadHWPval_ALT(int opId) throws FileNotFoundException, IOException{

        OperationMetadata rdOPMetadata = new OperationMetadata(opId);

        OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(),opId);
        LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

        NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
        rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, constants.cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT);

        assocNcFile.close();
        return rdMatrixMarkerSetLHM;
    }


    public static LinkedHashMap loadHWPval_ALL(int opId) throws FileNotFoundException, IOException{

        OperationMetadata rdOPMetadata = new OperationMetadata(opId);

        OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(),opId);
        LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

        NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
        rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, constants.cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL);

        assocNcFile.close();
        return rdMatrixMarkerSetLHM;
    }

    public static LinkedHashMap loadHWPval_CASE(int opId) throws FileNotFoundException, IOException{

        OperationMetadata rdOPMetadata = new OperationMetadata(opId);

        OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(),opId);
        LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

        NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
        rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, constants.cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE);

        assocNcFile.close();
        return rdMatrixMarkerSetLHM;
    }

    public static LinkedHashMap loadHWPval_CTRL(int opId) throws FileNotFoundException, IOException{

        OperationMetadata rdOPMetadata = new OperationMetadata(opId);

        OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(),opId);
        LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

        NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
        rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, constants.cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);

        assocNcFile.close();
        return rdMatrixMarkerSetLHM;
    }





    public static LinkedHashMap loadHWHETZY_ALT(int opId) throws FileNotFoundException, IOException{

        OperationMetadata rdOPMetadata = new OperationMetadata(opId);

        OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(),opId);
        LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

        NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
        rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, constants.cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT);

        assocNcFile.close();
        return rdMatrixMarkerSetLHM;
    }


    public static LinkedHashMap loadHWHETZY_ALL(int opId) throws FileNotFoundException, IOException{

        OperationMetadata rdOPMetadata = new OperationMetadata(opId);

        OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(),opId);
        LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

        NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
        rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, constants.cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALL);

        assocNcFile.close();
        return rdMatrixMarkerSetLHM;
    }

    public static LinkedHashMap loadHWHETZY_CASE(int opId) throws FileNotFoundException, IOException{

        OperationMetadata rdOPMetadata = new OperationMetadata(opId);

        OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(),opId);
        LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

        NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
        rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, constants.cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CASE);

        assocNcFile.close();
        return rdMatrixMarkerSetLHM;
    }

    public static LinkedHashMap loadHWHETZY_CTRL(int opId) throws FileNotFoundException, IOException{

        OperationMetadata rdOPMetadata = new OperationMetadata(opId);

        OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(),opId);
        LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

        NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
        rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, constants.cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL);

        assocNcFile.close();
        return rdMatrixMarkerSetLHM;
    }

    //</editor-fold>

}

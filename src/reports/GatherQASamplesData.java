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

public class GatherQASamplesData {
    
    public static LinkedHashMap loadSamplesQAMissingRatio(int opId) throws FileNotFoundException, IOException{

        OperationMetadata rdOPMetadata = new OperationMetadata(opId);

        OperationSet rdInfoSampleSet = new OperationSet(rdOPMetadata.getStudyId(),opId);
        LinkedHashMap rdMatrixSampleSetLHM = rdInfoSampleSet.getOpSetLHM();

        NetcdfFile ncFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
        rdMatrixSampleSetLHM = rdInfoSampleSet.fillOpSetLHMWithVariable(ncFile, constants.cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);

        ncFile.close();
        return rdMatrixSampleSetLHM;
    }

    public static LinkedHashMap loadSamplesQAHetZygRatio(int opId) throws FileNotFoundException, IOException{

        OperationMetadata rdOPMetadata = new OperationMetadata(opId);

        OperationSet rdInfoSampleSet = new OperationSet(rdOPMetadata.getStudyId(),opId);
        LinkedHashMap rdMatrixSampleSetLHM = rdInfoSampleSet.getOpSetLHM();

        NetcdfFile ncFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
        rdMatrixSampleSetLHM = rdInfoSampleSet.fillOpSetLHMWithVariable(ncFile, constants.cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);

        ncFile.close();
        return rdMatrixSampleSetLHM;
    }
  

}

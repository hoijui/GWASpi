package org.gwaspi.reports;

import java.io.IOException;
import java.util.LinkedHashMap;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.gwaspi.netCDF.operations.OperationSet;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class GatherQASamplesData {

	private GatherQASamplesData() {
	}

	public static LinkedHashMap loadSamplesQAMissingRatio(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoSampleSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		LinkedHashMap rdMatrixSampleSetLHM = rdInfoSampleSet.getOpSetLHM();

		NetcdfFile ncFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixSampleSetLHM = rdInfoSampleSet.fillOpSetLHMWithVariable(ncFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);

		ncFile.close();
		return rdMatrixSampleSetLHM;
	}

	public static LinkedHashMap loadSamplesQAHetZygRatio(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoSampleSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		LinkedHashMap rdMatrixSampleSetLHM = rdInfoSampleSet.getOpSetLHM();

		NetcdfFile ncFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixSampleSetLHM = rdInfoSampleSet.fillOpSetLHMWithVariable(ncFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);

		ncFile.close();
		return rdMatrixSampleSetLHM;
	}
}

package org.gwaspi.reports;

import org.gwaspi.constants.cNetCDF;
import java.io.IOException;
import java.util.Map;
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

	public static Map<String, Object> loadSamplesQAMissingRatio(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoSampleSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixSampleSetMap = rdInfoSampleSet.getOpSetMap();

		NetcdfFile ncFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixSampleSetMap = rdInfoSampleSet.fillOpSetMapWithVariable(ncFile, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);

		ncFile.close();
		return rdMatrixSampleSetMap;
	}

	public static Map<String, Object> loadSamplesQAHetZygRatio(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoSampleSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixSampleSetMap = rdInfoSampleSet.getOpSetMap();

		NetcdfFile ncFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixSampleSetMap = rdInfoSampleSet.fillOpSetMapWithVariable(ncFile, cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);

		ncFile.close();
		return rdMatrixSampleSetMap;
	}
}

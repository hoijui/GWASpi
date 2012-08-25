package org.gwaspi.reports;

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
public class GatherQAMarkersData {

	private GatherQAMarkersData() {
	}

	public static Map<String, Object> loadMarkerQAMissingRatio(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadMarkerQAMismatchState(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadMarkerQAMinorAlleles(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadMarkerQAMajorAlleles(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadMarkerQAMinorAlleleFrequency(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadMarkerQAMajorAlleleFrequency(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;
	}
}

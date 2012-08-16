package org.gwaspi.reports;

import java.io.FileNotFoundException;
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
public class GatherQAMarkersData {

	public static LinkedHashMap loadMarkerQAMissingRatio(int opId) throws FileNotFoundException, IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static LinkedHashMap loadMarkerQAMismatchState(int opId) throws FileNotFoundException, IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;

	}

	public static LinkedHashMap loadMarkerQAMinorAlleles(int opId) throws FileNotFoundException, IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;

	}

	public static LinkedHashMap loadMarkerQAMajorAlleles(int opId) throws FileNotFoundException, IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;

	}

	public static LinkedHashMap loadMarkerQAMinorAlleleFrequency(int opId) throws FileNotFoundException, IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;

	}

	public static LinkedHashMap loadMarkerQAMajorAlleleFrequency(int opId) throws FileNotFoundException, IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		LinkedHashMap rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(markerQANcFile, org.gwaspi.constants.cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);

		markerQANcFile.close();
		return rdMatrixMarkerSetLHM;

	}
}

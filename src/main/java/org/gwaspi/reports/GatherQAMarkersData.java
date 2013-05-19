package org.gwaspi.reports;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import ucar.nc2.NetcdfFile;

public class GatherQAMarkersData {

	private GatherQAMarkersData() {
	}

	public static Map<MarkerKey, Double> loadMarkerQAMissingRatio(int opId) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
		Map<MarkerKey, Double> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}

	public static Map<MarkerKey, Integer> loadMarkerQAMismatchState(int opId) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
		Map<MarkerKey, Integer> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}

	public static Map<MarkerKey, char[]> loadMarkerQAMinorAlleles(int opId) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
		Map<MarkerKey, char[]> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}

	public static Map<MarkerKey, char[]> loadMarkerQAMajorAlleles(int opId) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
		Map<MarkerKey, char[]> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}

	public static Map<MarkerKey, Double> loadMarkerQAMinorAlleleFrequency(int opId) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
		Map<MarkerKey, Double> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}

	public static Map<MarkerKey, Double> loadMarkerQAMajorAlleleFrequency(int opId) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
		Map<MarkerKey, Double> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}
}

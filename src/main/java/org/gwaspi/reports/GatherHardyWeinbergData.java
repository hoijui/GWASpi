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
public class GatherHardyWeinbergData {

	private GatherHardyWeinbergData() {
	}

	//<editor-fold defaultstate="collapsed" desc="HARDY-WEINBERG REPORT METHODS">
	public static Map<String, Object> loadHWPval_ALT(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT);

		assocNcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadHWPval_ALL(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL);

		assocNcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadHWPval_CASE(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE);

		assocNcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadHWPval_CTRL(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);

		assocNcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadHWHETZY_ALT(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT);

		assocNcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadHWHETZY_ALL(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALL);

		assocNcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadHWHETZY_CASE(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CASE);

		assocNcFile.close();
		return rdMatrixMarkerSetLHM;
	}

	public static Map<String, Object> loadHWHETZY_CTRL(int opId) throws IOException {

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);

		OperationSet rdInfoMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
		Map<String, Object> rdMatrixMarkerSetLHM = rdInfoMarkerSet.getOpSetLHM();

		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetLHM = rdInfoMarkerSet.fillOpSetLHMWithVariable(assocNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL);

		assocNcFile.close();
		return rdMatrixMarkerSetLHM;
	}
	//</editor-fold>
}

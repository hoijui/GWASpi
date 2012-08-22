package org.gwaspi.netCDF.matrices;

import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixManager {

	private MatrixManager() {
	}

	public static String createMatricesTable(DbManager db) {
		boolean result = false;
		try {
			//CREATE MATRIX_METADATA table in MATRICES SCHEMA
			db.createTable(org.gwaspi.constants.cDBGWASpi.SCH_MATRICES,
					org.gwaspi.constants.cDBMatrix.T_MATRICES,
					org.gwaspi.constants.cDBMatrix.T_CREATE_MATRICES);

		} catch (Exception e) {
			System.out.println("Error creating management database");
			System.out.print(e);
			e.printStackTrace();
		}

		return (result) ? "1" : "0";
	}

	public static void insertMatrixMetadata(DbManager dBManager,
			int studyId,
			String matrix_name,
			String netCDF_name,
			String matrix_type,
			int parent_matrix1_id,
			int parent_matrix2_id,
			String input_location,
			String description,
			int loaded) throws IOException {

		if (description.length() > 1999) {
			description = description.substring(0, 1999);
		}

		Object[] matrixMetaData = new Object[]{matrix_name,
			netCDF_name,
			matrix_type,
			parent_matrix1_id,
			parent_matrix2_id,
			input_location,
			description,
			loaded,
			studyId
		};

		dBManager.insertValuesInTable(org.gwaspi.constants.cDBGWASpi.SCH_MATRICES,
				org.gwaspi.constants.cDBMatrix.T_MATRICES,
				org.gwaspi.constants.cDBMatrix.F_INSERT_MATRICES,
				matrixMetaData);
	}

	public static void deleteMatrix(int matrixId, boolean deleteReports) {
		try {
			MatrixMetadata matrixMetadata = new MatrixMetadata(matrixId);

			String genotypesFolder = org.gwaspi.global.Config.getConfigValue("GTdir", "");
			genotypesFolder += "/STUDY_" + matrixMetadata.getStudyId() + "/";

			//DELETE OPERATION netCDFs FROM THIS MATRIX
			org.gwaspi.model.OperationsList opList = new OperationsList(matrixId);
			for (Operation op : opList.operationsListAL) {
				File opFile = new File(genotypesFolder + op.getOperationNetCDFName() + ".nc");
				if (opFile.exists()) {
					if (!opFile.canWrite()) {
						throw new IllegalArgumentException("Delete: write protected: " + opFile.getPath());
					}

					boolean success = opFile.delete();
				}
			}

			org.gwaspi.reports.ReportManager.deleteReportByMatrixId(matrixId);

			//DELETE MATRIX NETCDF FILE
			File matrixFile = new File(genotypesFolder + matrixMetadata.getMatrixNetCDFName() + ".nc");
			if (matrixFile.exists()) {
				if (!matrixFile.canWrite()) {
					throw new IllegalArgumentException("Delete: write protected: " + matrixFile.getPath());
				}

				boolean success = matrixFile.delete();
			}

			//DELETE METADATA INFO FROM DB
			DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
			String statement = "DELETE FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBMatrix.T_MATRICES + " WHERE ID=" + matrixMetadata.getMatrixId();
			dBManager.executeStatement(statement);

		} catch (Exception e) {
			System.out.println("Error deleteing Matrix!");
			System.out.print(e);
			e.printStackTrace();
		}
	}

	public static String generateMatrixNetCDFNameByDate() {
		String matrixName = "GT_";
		matrixName += org.gwaspi.global.Utils.getShortDateTimeForFileName();
		matrixName = matrixName.replace(":", "");
		matrixName = matrixName.replace(" ", "");
		matrixName = matrixName.replace("/", "");
		matrixName.replaceAll("[a-zA-Z]", "");

		//matrixName = matrixName.substring(0, matrixName.length()-3); //Remove "CET" from name
		return matrixName;
	}

	public static MatrixMetadata getLatestMatrixId() throws IOException {

		List<Map<String, Object>> rs = null;
		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT " + org.gwaspi.constants.cDBMatrix.f_ID + " FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBMatrix.T_MATRICES + " ORDER BY " + org.gwaspi.constants.cDBMatrix.f_ID + " DESC  WITH RR");
		} catch (Exception e) {
			e.printStackTrace();
		}

		MatrixMetadata mxMetaData = new MatrixMetadata((Integer) rs.get(0).get(org.gwaspi.constants.cDBMatrix.f_ID));

		return mxMetaData;
	}
}

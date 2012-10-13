package org.gwaspi.netCDF.matrices;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixManager {

	private static final Logger log = LoggerFactory.getLogger(MatrixManager.class);

	private static final String SQL_STATEMENT_SELECT_LATEST_MATRIX
			= "SELECT " + cDBMatrix.f_ID + " FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " ORDER BY " + cDBMatrix.f_ID + " DESC  WITH RR";
	private static final String SQL_STATEMENT_DELETE_REPORT_BY_MATRIX_ID
			= "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " WHERE ID=%d";

	private MatrixManager() {
	}

	public static String createMatricesTable(DbManager db) {
		boolean result = false;
		try {
			// CREATE MATRIX_METADATA table in MATRICES SCHEMA
			db.createTable(
					cDBGWASpi.SCH_MATRICES,
					cDBMatrix.T_MATRICES,
					cDBMatrix.T_CREATE_MATRICES);
		} catch (Exception ex) {
			log.error("Failed creating management database", ex);
		}

		return (result) ? "1" : "0";
	}

	public static void insertMatrixMetadata(
			DbManager dBManager,
			int studyId,
			String matrix_name,
			String netCDF_name,
			GenotypeEncoding matrix_type,
			int parent_matrix1_id,
			int parent_matrix2_id,
			String input_location,
			String description,
			int loaded)
			throws IOException
	{
		String trimmedDescription = description;
		if (trimmedDescription.length() > 1999) {
			trimmedDescription = trimmedDescription.substring(0, 1999);
		}

		Object[] matrixMetaData = new Object[] {
			matrix_name,
			netCDF_name,
			matrix_type.toString(),
			parent_matrix1_id,
			parent_matrix2_id,
			input_location,
			trimmedDescription,
			loaded,
			studyId
		};

		dBManager.insertValuesInTable(
				cDBGWASpi.SCH_MATRICES,
				cDBMatrix.T_MATRICES,
				cDBMatrix.F_INSERT_MATRICES,
				matrixMetaData);
	}

	public static void deleteMatrix(int matrixId, boolean deleteReports) {
		try {
			MatrixMetadata matrixMetadata = new MatrixMetadata(matrixId);

			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
			genotypesFolder += "/STUDY_" + matrixMetadata.getStudyId() + "/";

			// DELETE OPERATION netCDFs FROM THIS MATRIX
			List<Operation> operations = OperationsList.getOperationsList(matrixId);
			for (Operation op : operations) {
				File opFile = new File(genotypesFolder + op.getNetCDFName() + ".nc");
				org.gwaspi.global.Utils.tryToDeleteFile(opFile);
			}

			org.gwaspi.reports.ReportManager.deleteReportByMatrixId(matrixId);

			// DELETE MATRIX NETCDF FILE
			File matrixFile = new File(genotypesFolder + matrixMetadata.getMatrixNetCDFName() + ".nc");
			org.gwaspi.global.Utils.tryToDeleteFile(matrixFile);

			// DELETE METADATA INFO FROM DB
			DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			String statement = String.format(SQL_STATEMENT_DELETE_REPORT_BY_MATRIX_ID, matrixMetadata.getMatrixId());
			dBManager.executeStatement(statement);
		} catch (Exception ex) {
			log.error("Failed deleting Matrix", ex);
		}
	}

	public static String generateMatrixNetCDFNameByDate() {
		String matrixName = "GT_";
		matrixName += org.gwaspi.global.Utils.getShortDateTimeForFileName();
		matrixName = matrixName.replace(":", "");
		matrixName = matrixName.replace(" ", "");
		matrixName = matrixName.replace("/", "");
//		matrixName = matrixName.replaceAll("[a-zA-Z]", "");

//		matrixName = matrixName.substring(0, matrixName.length() - 3); // Remove "CET" from name
		return matrixName;
	}

	public static MatrixMetadata getLatestMatrixId() throws IOException {

		MatrixMetadata mxMetaData = null;

		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>> rs = studyDbManager.executeSelectStatement(SQL_STATEMENT_SELECT_LATEST_MATRIX);
			mxMetaData = new MatrixMetadata((Integer) rs.get(0).get(cDBMatrix.f_ID));
		} catch (Exception ex) {
			log.error("Failed retreiving latest Matrix", ex);
		}

		return mxMetaData;
	}
}

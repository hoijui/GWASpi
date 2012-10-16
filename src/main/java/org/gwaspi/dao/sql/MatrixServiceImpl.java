package org.gwaspi.dao.sql;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cDBOperations;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.dao.MatrixService;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Matrix;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.ReportsList;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixServiceImpl implements MatrixService {

	private static final Logger log
			= LoggerFactory.getLogger(MatrixServiceImpl.class);

	private static final String SQL_STATEMENT_SELECT_LATEST_MATRIX
			= "SELECT " + cDBMatrix.f_ID + " FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " ORDER BY " + cDBMatrix.f_ID + " DESC  WITH RR";
	private static final String SQL_STATEMENT_DELETE_REPORT_BY_MATRIX_ID
			= "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " WHERE ID=%d";


	/**
	 * This will init the Matrix object requested from the DB
	 */
	@Override
	public Matrix getById(int matrixId) throws IOException {

		MatrixMetadata matrixMetadata = new MatrixMetadata(matrixId);
		int studyId = matrixMetadata.getStudyId();

		return new Matrix(matrixId, studyId, matrixMetadata);
	}

	@Override
	public List<Matrix> getMatrixList(int studyId) throws IOException {

		List<Matrix> matrixList = new ArrayList<Matrix>();

		List<Map<String, Object>> rsMatricesList = getMatrixListByStudyId(studyId);

		int rowcount = rsMatricesList.size();
		if (rowcount > 0) {
			for (int i = rowcount - 1; i >= 0; i--) // loop through rows of result set
			{
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsMatricesList.isEmpty() && rsMatricesList.get(i).size() == cDBMatrix.T_CREATE_MATRICES.length) {
					int currentMatrixId = (Integer) rsMatricesList.get(i).get(cDBMatrix.f_ID);
					Matrix currentMatrix = getById(currentMatrixId);
					matrixList.add(currentMatrix);
				}
			}
		}

		return matrixList;
	}

	@Override
	public List<Matrix> getMatrixList() throws IOException {

		List<Matrix> matrixList = new ArrayList<Matrix>();

		List<Map<String, Object>> rsMatricesList = getAllMatricesList();

		int rowcount = rsMatricesList.size();
		if (rowcount > 0) {
			for (int i = rowcount - 1; i >= 0; i--) // loop through rows of result set
			{
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsMatricesList.isEmpty() && rsMatricesList.get(i).size() == cDBMatrix.T_CREATE_MATRICES.length) {
					int currentMatrixId = (Integer) rsMatricesList.get(i).get(cDBMatrix.f_ID);
					Matrix currentMatrix = getById(currentMatrixId);
					matrixList.add(currentMatrix);
				}
			}
		}

		return matrixList;
	}

	private static List<Map<String, Object>> getMatrixListByStudyId(int studyId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " WHERE " + cDBMatrix.f_STUDYID + "=" + studyId + " ORDER BY " + cDBMatrix.f_ID + " DESC  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return rs;
	}

	@Override
	public List<Map<String, Object>> getAllMatricesList() throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + "  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return rs;
	}

	@Override
	public Object[][] getMatricesTable(int studyId) throws IOException {
		Object[][] table = null;

		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager dbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>> rs = dbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " WHERE " + cDBMatrix.f_STUDYID + "=" + studyId + "  WITH RR");

			table = new Object[rs.size()][4];
			for (int i = 0; i < rs.size(); i++) {
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == cDBMatrix.T_CREATE_MATRICES.length) {
					table[i][0] = (Integer) rs.get(i).get(cDBMatrix.f_ID);
					table[i][1] = rs.get(i).get(cDBMatrix.f_MATRIX_NAME).toString();
					table[i][2] = rs.get(i).get(cDBMatrix.f_DESCRIPTION).toString();
					String timestamp = rs.get(i).get(cDBOperations.f_CREATION_DATE).toString();
					table[i][3] = timestamp.substring(0, timestamp.lastIndexOf('.'));
				}
			}
		} catch (Exception ex) {
			log.error(null, ex);
		}
		return table;
	}

	@Override
	public String createMatricesTable(DbManager db) {
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

	@Override
	public void insertMatrixMetadata(
			DbManager dBManager,
			int studyId,
			String matrix_name,
			String netCDF_name,
			cNetCDF.Defaults.GenotypeEncoding matrix_type,
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

	@Override
	public void deleteMatrix(int matrixId, boolean deleteReports) {
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

			ReportsList.deleteReportByMatrixId(matrixId);

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

	@Override
	public String generateMatrixNetCDFNameByDate() {
		String matrixName = "GT_";
		matrixName += org.gwaspi.global.Utils.getShortDateTimeForFileName();
		matrixName = matrixName.replace(":", "");
		matrixName = matrixName.replace(" ", "");
		matrixName = matrixName.replace("/", "");
//		matrixName = matrixName.replaceAll("[a-zA-Z]", "");

//		matrixName = matrixName.substring(0, matrixName.length() - 3); // Remove "CET" from name
		return matrixName;
	}

	@Override
	public MatrixMetadata getLatestMatrixId() throws IOException {

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

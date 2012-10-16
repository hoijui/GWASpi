package org.gwaspi.dao.sql;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cDBOperations;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.dao.MatrixService;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.Matrix;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.ReportsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class MatrixServiceImpl implements MatrixService {

	private static final Logger log
			= LoggerFactory.getLogger(MatrixServiceImpl.class);

	private static final String SQL_STATEMENT_SELECT_LATEST_MATRIX
			= "SELECT " + cDBMatrix.f_ID + " FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " ORDER BY " + cDBMatrix.f_ID + " DESC  WITH RR";
	private static final String SQL_STATEMENT_DELETE_REPORT_BY_MATRIX_ID
			= "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " WHERE ID=%d";
	private static final String SQL_STATEMENT_SELECT_MATRIX_BY_ID
			= "SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " WHERE " + cDBMatrix.f_ID + "=%d  WITH RR";
	private static final String SQL_STATEMENT_SELECT_MATRIX_BY_NAME
			= "SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " WHERE " + cDBMatrix.f_NETCDF_NAME + "='%s'  WITH RR";

	/**
	 * This will init the Matrix object requested from the DB
	 */
	@Override
	public Matrix getById(int matrixId) throws IOException {

		MatrixMetadata matrixMetadata = getMatrixMetadataById(matrixId);
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

	@Override
	public void deleteMatrix(int matrixId, boolean deleteReports) {
		try {
			MatrixMetadata matrixMetadata = getMatrixMetadataById(matrixId);

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
			mxMetaData = getMatrixMetadataById((Integer) rs.get(0).get(cDBMatrix.f_ID));
		} catch (Exception ex) {
			log.error("Failed retreiving latest Matrix", ex);
		}

		return mxMetaData;
	}

	@Override
	public MatrixMetadata getMatrixMetadataById(int matrixId) throws IOException {

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		String statement = String.format(SQL_STATEMENT_SELECT_MATRIX_BY_ID, matrixId);
		List<Map<String, Object>> rs = dBManager.executeSelectStatement(statement);

		return loadMatrixMetadataFromResultRest(matrixId, rs);
	}

	@Override
	public MatrixMetadata getMatrixMetadataByNetCDFname(String netCDFname) throws IOException {

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		String statement = String.format(SQL_STATEMENT_SELECT_MATRIX_BY_NAME, netCDFname);
		List<Map<String, Object>> rs = dBManager.executeSelectStatement(statement);

		int matrixId = Integer.MIN_VALUE;
		if (!rs.isEmpty() && rs.get(0).size() == cDBMatrix.T_CREATE_MATRICES.length) {
			matrixId = Integer.parseInt(rs.get(0).get(cDBMatrix.f_ID).toString());
		}

		return loadMatrixMetadataFromResultRest(matrixId, rs);
	}

    /**
	 * This Method used to import GWASpi matrix from an external file
	 * The size of this Map is very small.
	 */
	@Override
	public MatrixMetadata getMatrixMetadata(String netCDFpath, int studyId, String newMatrixName) throws IOException {

		int matrixId = Integer.MIN_VALUE;
		String matrixFriendlyName = newMatrixName;
		String matrixNetCDFName = MatricesList.generateMatrixNetCDFNameByDate();
		String description = "";
		String matrixType = "";

		String pathToMatrix = netCDFpath;
		return loadMatrixMetadataFromFile(matrixId, matrixFriendlyName, matrixNetCDFName, studyId, pathToMatrix, description, matrixType);
	}

	private MatrixMetadata loadMatrixMetadataFromResultRest(int matrixId, List<Map<String, Object>> rs) throws IOException {

		String matrixFriendlyName = "";
		String matrixNetCDFName = "";
		String description = "";
		String matrixType = "";
		int studyId = Integer.MIN_VALUE;

		// PREVENT PHANTOM-DB READS EXCEPTIONS
		if (!rs.isEmpty() && rs.get(0).size() == cDBMatrix.T_CREATE_MATRICES.length) {
//			matrixId = Integer.parseInt(rs.get(0).get(cDBMatrix.f_ID).toString());
			matrixFriendlyName = (rs.get(0).get(cDBMatrix.f_MATRIX_NAME) != null) ? rs.get(0).get(cDBMatrix.f_MATRIX_NAME).toString() : ""; // matrix_name VARCHAR(64) NOT NULL
			matrixNetCDFName = (rs.get(0).get(cDBMatrix.f_NETCDF_NAME) != null) ? rs.get(0).get(cDBMatrix.f_NETCDF_NAME).toString() : ""; // netcdf_name VARCHAR(64) NOT NULL
			matrixType = (rs.get(0).get(cDBMatrix.f_MATRIX_TYPE) != null) ? rs.get(0).get(cDBMatrix.f_MATRIX_TYPE).toString() : ""; // matrix_type VARCHAR(32) NOT NULL
//			parentMatrixId1 = (rs.get(0).get(cDBMatrix.f_PARENT_MATRIX1_ID) != null) ? Integer.parseInt(rs.get(0).get(cDBMatrix.f_PARENT_MATRIX1_ID).toString()) : -1; // parent_matrix1_id INTEGER
//			parentMatrixId2 = (rs.get(0).get(cDBMatrix.f_PARENT_MATRIX2_ID) != null) ? Integer.parseInt(rs.get(0).get(cDBMatrix.f_PARENT_MATRIX2_ID).toString()) : -1; // parent_matrix2_id INTEGER
//			input_location = (rs.get(0).get(cDBMatrix.f_INPUT_LOCATION) != null) ? rs.get(0).get(cDBMatrix.f_INPUT_LOCATION).toString() : ""; // input_location VARCHAR(1000)
			description = (rs.get(0).get(cDBMatrix.f_DESCRIPTION) != null) ? rs.get(0).get(cDBMatrix.f_DESCRIPTION).toString() : ""; // description VARCHAR(2000)
//			loaded = (rs.get(0).get(cDBMatrix.f_LOADED) != null) ? rs.get(0).get(cDBMatrix.f_LOADED).toString() : "0"; // loaded CHAR(1)
			studyId = (rs.get(0).get(cDBMatrix.f_STUDYID) != null) ? Integer.parseInt(rs.get(0).get(cDBMatrix.f_STUDYID).toString()) : 0;
		}

		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
		String pathToStudy = genotypesFolder + "/STUDY_" + studyId + "/";
		String pathToMatrix = pathToStudy + matrixNetCDFName + ".nc";
		return loadMatrixMetadataFromFile(matrixId, matrixFriendlyName, matrixNetCDFName, studyId, pathToMatrix, description, matrixType);
	}

	private MatrixMetadata loadMatrixMetadataFromFile(int matrixId, String matrixFriendlyName, String matrixNetCDFName, int studyId, String pathToMatrix, String description, String matrixType) throws IOException {

		String gwaspiDBVersion = "";
		ImportFormat technology = ImportFormat.UNKNOWN;
		GenotypeEncoding gtEncoding = GenotypeEncoding.UNKNOWN;
		StrandType strand = StrandType.UNKNOWN;
		boolean hasDictionray = false;
		int markerSetSize = Integer.MIN_VALUE;
		int sampleSetSize = Integer.MIN_VALUE;

//		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
//		String pathToStudy = genotypesFolder + "/STUDY_" + studyId + "/";
//		pathToMatrix = pathToStudy + matrixNetCDFName + ".nc";
		NetcdfFile ncfile = null;
		if (new File(pathToMatrix).exists()) {
			try {
				ncfile = NetcdfFile.open(pathToMatrix);

				technology = ImportFormat.compareTo(ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY).getStringValue());
				try {
					gwaspiDBVersion = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GWASPIDB_VERSION).getStringValue();
				} catch (Exception ex) {
					log.error(null, ex);
				}

				Variable var = ncfile.findVariable(cNetCDF.Variables.GLOB_GTENCODING);
				if (var != null) {
					try {
						ArrayChar.D2 gtCodeAC = (ArrayChar.D2) var.read("(0:0:1, 0:7:1)");
//						gtEncoding = GenotypeEncoding.valueOf(gtCodeAC.getString(0));
						gtEncoding = GenotypeEncoding.compareTo(gtCodeAC.getString(0)); // HACK, the above was used before
					} catch (InvalidRangeException ex) {
						log.error(null, ex);
					}
				}

				strand = StrandType.valueOf(ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_STRAND).getStringValue());
				hasDictionray = ((Integer) ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY).getNumericValue() != 0);

				Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_MARKERSET);
				markerSetSize = markerSetDim.getLength();

				Dimension sampleSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
				sampleSetSize = sampleSetDim.getLength();
			} catch (IOException ex) {
				log.error("Cannot open file: " + ncfile, ex);
			} finally {
				if (null != ncfile) {
					try {
						ncfile.close();
					} catch (IOException ex) {
						log.warn("Cannot close file: " + ncfile, ex);
					}
				}
			}
		}

		MatrixMetadata matrixMetadata = new MatrixMetadata(
				matrixId,
				matrixFriendlyName,
				matrixNetCDFName,
				pathToMatrix,
				technology,
				gwaspiDBVersion,
				description,
				gtEncoding,
				strand,
				hasDictionray,
				markerSetSize,
				sampleSetSize,
				studyId,
				matrixType);

		return matrixMetadata;
	}
}

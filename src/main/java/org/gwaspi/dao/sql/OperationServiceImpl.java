package org.gwaspi.dao.sql;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBOperations;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.OperationService;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.ReportsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;

public class OperationServiceImpl implements OperationService {

	private static final Logger log
			= LoggerFactory.getLogger(OperationServiceImpl.class);

	/**
	 * This will init the Matrix object requested from the DB
	 */
	@Override
	public Operation getById(int operationId) throws IOException {

		Operation operation = null;

		List<Map<String, Object>> rs = getOperationMetadataRS(operationId);

		// PREVENT PHANTOM-DB READS EXCEPTIONS
		if (!rs.isEmpty() && rs.get(0).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
			String friendlyName = (rs.get(0).get(cDBOperations.f_OP_NAME) != null) ? rs.get(0).get(cDBOperations.f_OP_NAME).toString() : "";
			String netCDFName = (rs.get(0).get(cDBOperations.f_OP_NETCDF_NAME) != null) ? rs.get(0).get(cDBOperations.f_OP_NETCDF_NAME).toString() : "";
			String type = (rs.get(0).get(cDBOperations.f_OP_TYPE) != null) ? rs.get(0).get(cDBOperations.f_OP_TYPE).toString() : "";
			int parentMatrixId = (rs.get(0).get(cDBOperations.f_PARENT_MATRIXID) != null) ? Integer.parseInt(rs.get(0).get(cDBOperations.f_PARENT_MATRIXID).toString()) : -1;
			int parentOperationId = (rs.get(0).get(cDBOperations.f_PARENT_OPID) != null) ? Integer.parseInt(rs.get(0).get(cDBOperations.f_PARENT_OPID).toString()) : -1;
			String command = (rs.get(0).get(cDBOperations.f_OP_COMMAND) != null) ? rs.get(0).get(cDBOperations.f_OP_COMMAND).toString() : "";
			String description = (rs.get(0).get(cDBOperations.f_DESCRIPTION) != null) ? rs.get(0).get(cDBOperations.f_DESCRIPTION).toString() : "";
			int studyId = (rs.get(0).get(cDBOperations.f_STUDYID) != null) ? Integer.parseInt(rs.get(0).get(cDBOperations.f_STUDYID).toString()) : 0;

			operation = new Operation(
					operationId,
					friendlyName,
					netCDFName,
					type,
					parentMatrixId,
					parentOperationId,
					command,
					description,
					studyId);
		}

		return operation;
	}

	private static List<Map<String, Object>> getOperationMetadataRS(int opId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + opId + "  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return rs;
	}

	@Override
	public List<Operation> getOperationsList(int matrixId) throws IOException {

		List<Operation> operationsList = new ArrayList<Operation>();

		List<Map<String, Object>> rsOperations = getOperationsListByMatrixId(matrixId);

		int rowcount = rsOperations.size();
		if (rowcount > 0) {
			for (int i = 0; i < rowcount; i++) // loop through rows of result set
			{
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsOperations.isEmpty() && rsOperations.get(i).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
					int currentOPId = (Integer) rsOperations.get(i).get(cDBOperations.f_ID);
					Operation currentOP = getById(currentOPId);
					operationsList.add(currentOP);
				}
			}
		}

		return operationsList;
	}

	@Override
	public List<Operation> getOperationsList(int matrixId, int parentOpId) throws IOException {

		List<Operation> operationsList = new ArrayList<Operation>();

		List<Map<String, Object>> rsOperations = getOperationsListByMatrixId(matrixId);

		int rowcount = rsOperations.size();
		if (rowcount > 0) {
			// loop through rows of result set
			for (int i = 0; i < rowcount; i++) {
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsOperations.isEmpty() && rsOperations.get(i).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
					int currentParentOPId = (Integer) rsOperations.get(i).get(cDBOperations.f_PARENT_OPID);
					int currentOpId = (Integer) rsOperations.get(i).get(cDBOperations.f_ID);
					if (currentParentOPId == parentOpId) {
						Operation currentOP = getById(currentOpId);
						operationsList.add(currentOP);
					}
				}
			}
		}

		return operationsList;
	}

	@Override
	public List<Operation> getOperationsList(int matrixId, int parentOpId, OPType opType) throws IOException {

		List<Operation> operationsList = new ArrayList<Operation>();

		List<Map<String, Object>> rsOperations = getOperationsListByMatrixId(matrixId);

		int rowcount = rsOperations.size();
		if (rowcount > 0) {
			// loop through rows of result set
			for (int i = 0; i < rowcount; i++) {
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsOperations.isEmpty() && rsOperations.get(i).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
					int currentParentOPId = (Integer) rsOperations.get(i).get(cDBOperations.f_PARENT_OPID);
					int currentOpId = (Integer) rsOperations.get(i).get(cDBOperations.f_ID);
					String currentOpType = rsOperations.get(i).get(cDBOperations.f_OP_TYPE).toString();
					if (currentParentOPId == parentOpId && currentOpType.equals(opType.toString())) {
						Operation currentOP = getById(currentOpId);
						operationsList.add(currentOP);
					}
				}
			}
		}

		return operationsList;
	}

	private static List<Map<String, Object>> getOperationsListByMatrixId(int matrixId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_PARENT_MATRIXID + "=" + matrixId + "  WITH RR");
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return rs;
	}

	//<editor-fold defaultstate="collapsed" desc="OPERATIONS TABLES">
	@Override
	public Object[][] getOperationsTable(int matrixId) throws IOException {
		Object[][] table = null;

		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager dbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>>  rs = dbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_PARENT_MATRIXID + "=" + matrixId + " AND " + cDBOperations.f_PARENT_OPID + " = -1" + "  WITH RR");

			table = new Object[rs.size()][4];
			for (int i = 0; i < rs.size(); i++) {
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
					table[i][0] = (Integer) rs.get(i).get(cDBOperations.f_ID);
					table[i][1] = rs.get(i).get(cDBOperations.f_OP_NAME).toString();
					table[i][2] = rs.get(i).get(cDBOperations.f_DESCRIPTION).toString();
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
	public Object[][] getOperationsTable(int matrixId, int opId) throws IOException {
		Object[][] table = null;

		String dbName = cDBGWASpi.DB_DATACENTER;
		DbManager dbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>> rs = dbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_PARENT_MATRIXID + "=" + matrixId + " AND " + cDBOperations.f_PARENT_OPID + "=" + opId + "  WITH RR");

			table = new Object[rs.size() + 1][4];
			List<Map<String, Object>> rsSelf = dbManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_PARENT_MATRIXID + "=" + matrixId + " AND " + cDBOperations.f_ID + "=" + opId + "  WITH RR");

			table[0][0] = (Integer) rsSelf.get(0).get(cDBOperations.f_ID);
			table[0][1] = rsSelf.get(0).get(cDBOperations.f_OP_NAME).toString();
			table[0][2] = rsSelf.get(0).get(cDBOperations.f_DESCRIPTION).toString();
			String timestamp = rsSelf.get(0).get(cDBOperations.f_CREATION_DATE).toString();
			table[0][3] = timestamp.substring(0, timestamp.lastIndexOf('.'));

			for (int i = 0; i < rs.size(); i++) {
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
					table[i + 1][0] = (Integer) rs.get(i).get(cDBOperations.f_ID);
					table[i + 1][1] = rs.get(i).get(cDBOperations.f_OP_NAME).toString();
					table[i + 1][2] = rs.get(i).get(cDBOperations.f_DESCRIPTION).toString();
					timestamp = rs.get(i).get(cDBOperations.f_CREATION_DATE).toString();
					table[i + 1][3] = timestamp.substring(0, timestamp.lastIndexOf('.'));
				}
			}
		} catch (Exception ex) {
			log.error(null, ex);
		}
		return table;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	@Override
	public int getIdOfLastOperationTypeOccurance(List<Operation> operationsList, OPType opType) {
		int result = Integer.MIN_VALUE;
		for (int i = 0; i < operationsList.size(); i++) {
			if (operationsList.get(i).getOperationType().equals(OPType.MARKER_QA.toString())) {
				result = operationsList.get(i).getId();
			}
		}
		return result;
	}
	//</editor-fold>

	@Override
	public String createOperationsMetadataTable() {
		boolean result = false;
		try {
			DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			// CREATE SAMPLESET_METADATA table in given SCHEMA
			db.createTable(cDBGWASpi.SCH_MATRICES,
					cDBOperations.T_OPERATIONS,
					cDBOperations.T_CREATE_OPERATIONS);

		} catch (Exception ex) {
			log.error("Failed creating management database", ex);
		}

		return (result ? "1" : "0");
	}

	@Override
	public void insertOPMetadata(
			int parentMatrixId,
			int parentOperationId,
			String friendlyName,
			String resultOPName,
			String type,
			String command,
			String description,
			Integer studyId)
			throws IOException
	{
		Object[] opMetaData = new Object[]{parentMatrixId,
			parentOperationId,
			friendlyName,
			resultOPName,
			type,
			command,
			description,
			studyId};

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		dBManager.insertValuesInTable(cDBGWASpi.SCH_MATRICES,
				cDBOperations.T_OPERATIONS,
				cDBOperations.F_INSERT_OPERATION,
				opMetaData);
	}

	@Override
	public List<Object[]> getMatrixOperations(int matrixId) throws IOException {
		List<Object[]> result = new ArrayList<Object[]>();

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		List<Map<String, Object>> rs = dBManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_PARENT_MATRIXID + "=" + matrixId + "  WITH RR");

		for (int rowcount = 0; rowcount < rs.size(); rowcount++) {
			// PREVENT PHANTOM-DB READS EXCEPTIONS
			if (!rs.isEmpty() && rs.get(rowcount).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
				Object[] element = new Object[2];
				element[0] = (Integer) rs.get(rowcount).get(cDBOperations.f_ID);
				element[1] = rs.get(rowcount).get(cDBOperations.f_OP_TYPE).toString();
				result.add(element);
			}
		}

		return result;
	}

	@Override
	public void deleteOperationBranch(int studyId, int opId, boolean deleteReports) throws IOException {

		try {
			Operation op = OperationsList.getById(opId);
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");

			List<Operation> operations = OperationsList.getOperationsList(op.getParentMatrixId(), opId);
			if (!operations.isEmpty()) {
				operations.add(op);
				for (int i = 0; i < operations.size(); i++) {
					File matrixOPFile = new File(genotypesFolder + "/STUDY_" + studyId + "/" + operations.get(i).getNetCDFName() + ".nc");
					org.gwaspi.global.Utils.tryToDeleteFile(matrixOPFile);
					if (deleteReports) {
						ReportsList.deleteReportByOperationId(operations.get(i).getId());
					}

					DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
					String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + operations.get(i).getId();
					dBManager.executeStatement(statement);
				}
			} else {
				File matrixOPFile = new File(genotypesFolder + "/STUDY_" + studyId + "/" + op.getNetCDFName() + ".nc");
				org.gwaspi.global.Utils.tryToDeleteFile(matrixOPFile);
				if (deleteReports) {
					ReportsList.deleteReportByOperationId(opId);
				}

				DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
				String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + opId;
				dBManager.executeStatement(statement);
			}
		} catch (IOException ex) {
			log.warn(null, ex);
			// PURGE INEXISTING OPERATIONS FROM DB
			DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + opId;
			dBManager.executeStatement(statement);
		} catch (IllegalArgumentException ex) {
			log.warn(null, ex);
			// PURGE INEXISTING OPERATIONS FROM DB
			DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + opId;
			dBManager.executeStatement(statement);
		}
	}

	@Override
	public OperationMetadata getOperationMetadata(int opId) throws IOException {

		OperationMetadata operationMetadata = null;

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		List<Map<String, Object>> rs = dBManager.executeSelectStatement(
				"SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + opId + "  WITH RR");
		if (!rs.isEmpty()) {
			int parentMatrixId = Integer.MIN_VALUE;
			int parentOperationId = Integer.MIN_VALUE;
			String op_name = "";
			String netCDF_name = "";
			String description = "";
			String gtCode = "";
			int studyId = Integer.MIN_VALUE;
			int opSetSize = Integer.MIN_VALUE;
			int implicitSetSize = Integer.MIN_VALUE;

			// PREVENT PHANTOM-DB READS EXCEPTIONS
			if (!rs.isEmpty() && rs.get(0).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
				parentMatrixId = Integer.parseInt(rs.get(0).get(cDBOperations.f_PARENT_MATRIXID).toString());
				parentOperationId = Integer.parseInt(rs.get(0).get(cDBOperations.f_PARENT_OPID).toString());
				op_name = rs.get(0).get(cDBOperations.f_OP_NAME).toString();
				netCDF_name = rs.get(0).get(cDBOperations.f_OP_NETCDF_NAME).toString();
				description = rs.get(0).get(cDBOperations.f_DESCRIPTION).toString();
				studyId = (Integer) rs.get(0).get(cDBOperations.f_STUDYID);
			}

			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
			String pathToStudy = genotypesFolder + "/STUDY_" + studyId + "/";
			String pathToMatrix = pathToStudy + netCDF_name + ".nc";
			NetcdfFile ncfile = null;
			if (new File(pathToMatrix).exists()) {
				try {
					ncfile = NetcdfFile.open(pathToMatrix);
//					gtCode = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GTCODE).getStringValue();

					Dimension setDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_OPSET);
					opSetSize = setDim.getLength();

					Dimension implicitDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_IMPLICITSET);
					implicitSetSize = implicitDim.getLength();
				} catch (IOException ex) {
					log.error("Cannot open file: " + pathToMatrix, ex);
				} finally {
					if (null != ncfile) {
						try {
							ncfile.close();
						} catch (IOException ex) {
							log.warn("Cannot close file: " + ncfile.getLocation(), ex);
						}
					}
				}
			}

			operationMetadata = new OperationMetadata(
					opId,
					parentMatrixId,
					parentOperationId,
					op_name,
					netCDF_name,
					description,
					pathToMatrix,
					gtCode,
					opSetSize,
					implicitSetSize,
					studyId);
		}

		return operationMetadata;
	}

	@Override
	public OperationMetadata getOperationMetadata(String netCDFname) throws IOException {

		OperationMetadata operationMetadata;

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String sql = "SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_OP_NETCDF_NAME + "='" + netCDFname + "' ORDER BY " + cDBOperations.f_ID + " DESC  WITH RR";
		List<Map<String, Object>> rs = dBManager.executeSelectStatement(sql);

		int opId = Integer.MIN_VALUE;
		int parentMatrixId = Integer.MIN_VALUE;
		int parentOperationId = Integer.MIN_VALUE;
		String opName = "";
		String netCDF_name = "";
		String description = "";
		String gtCode = "";
		int studyId = Integer.MIN_VALUE;
		int opSetSize = Integer.MIN_VALUE;
		int implicitSetSize = Integer.MIN_VALUE;

		// PREVENT PHANTOM-DB READS EXCEPTIONS
		if (!rs.isEmpty() && rs.get(0).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
			opId = Integer.parseInt(rs.get(0).get(cDBOperations.f_ID).toString());
			parentMatrixId = Integer.parseInt(rs.get(0).get(cDBOperations.f_PARENT_MATRIXID).toString());
			parentOperationId = Integer.parseInt(rs.get(0).get(cDBOperations.f_PARENT_OPID).toString());
			opName = rs.get(0).get(cDBOperations.f_OP_NAME).toString();
			netCDF_name = netCDFname;
			description = rs.get(0).get(cDBOperations.f_DESCRIPTION).toString();
			studyId = (Integer) rs.get(0).get(cDBOperations.f_STUDYID);
		}

		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
		String pathToStudy = genotypesFolder + "/STUDY_" + studyId + "/";
		String pathToMatrix = pathToStudy + netCDF_name + ".nc";
		NetcdfFile ncfile = null;
		if (new File(pathToMatrix).exists()) {
			try {
				ncfile = NetcdfFile.open(pathToMatrix);

//				gtCode = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GTCODE).getStringValue();

				Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_OPSET);
				opSetSize = markerSetDim.getLength();

				Dimension implicitDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_IMPLICITSET);
				implicitSetSize = implicitDim.getLength();

			} catch (IOException ex) {
				log.error("Cannot open file: " + pathToMatrix, ex);
			} finally {
				if (null != ncfile) {
					try {
						ncfile.close();
					} catch (IOException ex) {
						log.warn("Cannot close file: " + ncfile.getLocation(), ex);
					}
				}
			}
		}

		operationMetadata = new OperationMetadata(
				opId,
				parentMatrixId,
				parentOperationId,
				opName,
				netCDF_name,
				description,
				pathToMatrix,
				gtCode,
				opSetSize,
				implicitSetSize,
				studyId);

		return operationMetadata;
	}
}

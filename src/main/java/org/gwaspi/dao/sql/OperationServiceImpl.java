package org.gwaspi.dao.sql;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBOperations;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.OperationService;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationServiceImpl implements OperationService {

	private static final Logger log
			= LoggerFactory.getLogger(OperationServiceImpl.class);

	/**
	 * This will init the Matrix object requested from the DB
	 */
	@Override
	public Operation getById(int operationId) throws IOException {

		Operation operation = null;

		List<Map<String, Object>> rs = getOperationMetadata(operationId);

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

	private static List<Map<String, Object>> getOperationMetadata(int opId) throws IOException {
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
}

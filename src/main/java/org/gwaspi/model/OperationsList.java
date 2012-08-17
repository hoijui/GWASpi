package org.gwaspi.model;

import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OperationsList {

	public ArrayList<model.Operation> operationsListAL = new ArrayList();

	public OperationsList(int matrixId) throws IOException {

		List<Map<String, Object>> rsOperations = getOperationsListByMatrixId(matrixId);

		int rowcount = rsOperations.size();
		if (rowcount > 0) {
			for (int i = 0; i < rowcount; i++) // loop through rows of result set
			{
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsOperations.isEmpty() && rsOperations.get(i).size() == org.gwaspi.constants.cDBOperations.T_CREATE_OPERATIONS.length) {
					int currentOPId = (Integer) rsOperations.get(i).get(org.gwaspi.constants.cDBOperations.f_ID);
					Operation currentOP = new Operation(currentOPId);
					operationsListAL.add(currentOP);
				}
			}
		}
	}

	public OperationsList(int matrixId, int parentOpId) throws IOException {

		List<Map<String, Object>> rsOperations = getOperationsListByMatrixId(matrixId);

		int rowcount = rsOperations.size();
		if (rowcount > 0) {
			for (int i = 0; i < rowcount; i++) // loop through rows of result set
			{
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsOperations.isEmpty() && rsOperations.get(i).size() == org.gwaspi.constants.cDBOperations.T_CREATE_OPERATIONS.length) {
					int currentParentOPId = (Integer) rsOperations.get(i).get(org.gwaspi.constants.cDBOperations.f_PARENT_OPID);
					int currentOpId = (Integer) rsOperations.get(i).get(org.gwaspi.constants.cDBOperations.f_ID);
					if (currentParentOPId == parentOpId) {
						Operation currentOP = new Operation(currentOpId);
						operationsListAL.add(currentOP);
					}
				}
			}
		}
	}

	public OperationsList(int matrixId, int parentOpId, OPType opType) throws IOException {

		List<Map<String, Object>> rsOperations = getOperationsListByMatrixId(matrixId);

		int rowcount = rsOperations.size();
		if (rowcount > 0) {
			for (int i = 0; i < rowcount; i++) // loop through rows of result set
			{
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsOperations.isEmpty() && rsOperations.get(i).size() == org.gwaspi.constants.cDBOperations.T_CREATE_OPERATIONS.length) {
					int currentParentOPId = (Integer) rsOperations.get(i).get(org.gwaspi.constants.cDBOperations.f_PARENT_OPID);
					int currentOpId = (Integer) rsOperations.get(i).get(org.gwaspi.constants.cDBOperations.f_ID);
					String currentOpType = rsOperations.get(i).get(org.gwaspi.constants.cDBOperations.f_OP_TYPE).toString();
					if (currentParentOPId == parentOpId && currentOpType.equals(opType.toString())) {
						Operation currentOP = new Operation(currentOpId);
						operationsListAL.add(currentOP);
					}
				}
			}
		}
	}

	protected List<Map<String, Object>> getOperationsListByMatrixId(int matrixId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBOperations.T_OPERATIONS + " WHERE " + org.gwaspi.constants.cDBOperations.f_PARENT_MATRIXID + "=" + matrixId + "  WITH RR");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;
	}

	//<editor-fold defaultstate="collapsed" desc="OPERATIONS TABLES">
	public static Object[][] getOperationsTable(int matrixId) throws IOException {
		Object[][] table = null;

		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager dbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>>  rs = dbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBOperations.T_OPERATIONS + " WHERE " + org.gwaspi.constants.cDBOperations.f_PARENT_MATRIXID + "=" + matrixId + " AND " + org.gwaspi.constants.cDBOperations.f_PARENT_OPID + " = -1" + "  WITH RR");

			table = new Object[rs.size()][4];
			for (int i = 0; i < rs.size(); i++) {
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == org.gwaspi.constants.cDBOperations.T_CREATE_OPERATIONS.length) {
					table[i][0] = (Integer) rs.get(i).get(org.gwaspi.constants.cDBOperations.f_ID);
					table[i][1] = rs.get(i).get(org.gwaspi.constants.cDBOperations.f_OP_NAME).toString();
					table[i][2] = rs.get(i).get(org.gwaspi.constants.cDBOperations.f_DESCRIPTION).toString();
					String timestamp = rs.get(i).get(org.gwaspi.constants.cDBOperations.f_CREATION_DATE).toString();
					table[i][3] = timestamp.substring(0, timestamp.lastIndexOf('.'));
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return table;
	}

	public static Object[][] getOperationsTable(int matrixId, int opId) throws IOException {
		Object[][] table = null;

		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager dbManager = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>> rs = dbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBOperations.T_OPERATIONS + " WHERE " + org.gwaspi.constants.cDBOperations.f_PARENT_MATRIXID + "=" + matrixId + " AND " + org.gwaspi.constants.cDBOperations.f_PARENT_OPID + "=" + opId + "  WITH RR");

			table = new Object[rs.size() + 1][4];
			List<Map<String, Object>> rsSelf = dbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBOperations.T_OPERATIONS + " WHERE " + org.gwaspi.constants.cDBOperations.f_PARENT_MATRIXID + "=" + matrixId + " AND " + org.gwaspi.constants.cDBOperations.f_ID + "=" + opId + "  WITH RR");

			table[0][0] = (Integer) rsSelf.get(0).get(org.gwaspi.constants.cDBOperations.f_ID);
			table[0][1] = rsSelf.get(0).get(org.gwaspi.constants.cDBOperations.f_OP_NAME).toString();
			table[0][2] = rsSelf.get(0).get(org.gwaspi.constants.cDBOperations.f_DESCRIPTION).toString();
			String timestamp = rsSelf.get(0).get(org.gwaspi.constants.cDBOperations.f_CREATION_DATE).toString();
			table[0][3] = timestamp.substring(0, timestamp.lastIndexOf('.'));

			for (int i = 0; i < rs.size(); i++) {
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == org.gwaspi.constants.cDBOperations.T_CREATE_OPERATIONS.length) {
					table[i + 1][0] = (Integer) rs.get(i).get(org.gwaspi.constants.cDBOperations.f_ID);
					table[i + 1][1] = rs.get(i).get(org.gwaspi.constants.cDBOperations.f_OP_NAME).toString();
					table[i + 1][2] = rs.get(i).get(org.gwaspi.constants.cDBOperations.f_DESCRIPTION).toString();
					timestamp = rs.get(i).get(org.gwaspi.constants.cDBOperations.f_CREATION_DATE).toString();
					table[i + 1][3] = timestamp.substring(0, timestamp.lastIndexOf('.'));
				}
			}
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return table;
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	public int getIdOfLastOperationTypeOccurance(org.gwaspi.constants.cNetCDF.Defaults.OPType opType) {
		int result = Integer.MIN_VALUE;
		ArrayList<Operation> opAL = this.operationsListAL;
		for (int i = 0; i < opAL.size(); i++) {
			if (opAL.get(i).getOperationType().equals(org.gwaspi.constants.cNetCDF.Defaults.OPType.MARKER_QA.toString())) {
				result = opAL.get(i).getOperationId();
			}
		}
		return result;
	}
	//</editor-fold>
}

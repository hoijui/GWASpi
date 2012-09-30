package org.gwaspi.model;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cDBOperations;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public final class MatricesList {

	private final static Logger log
			= LoggerFactory.getLogger(MatricesList.class);

	private MatricesList() {
	}

	public static List<Matrix> getMatrixList(int studyId) throws IOException {

		List<Matrix> matrixList = new ArrayList<Matrix>();

		List<Map<String, Object>> rsMatricesList = getMatrixListByStudyId(studyId);

		int rowcount = rsMatricesList.size();
		if (rowcount > 0) {
			for (int i = rowcount - 1; i >= 0; i--) // loop through rows of result set
			{
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsMatricesList.isEmpty() && rsMatricesList.get(i).size() == cDBMatrix.T_CREATE_MATRICES.length) {
					int currentMatrixId = (Integer) rsMatricesList.get(i).get(cDBMatrix.f_ID);
					Matrix currentMatrix = new Matrix(currentMatrixId);
					matrixList.add(currentMatrix);
				}
			}
		}

		return matrixList;
	}

	public static List<Matrix> getMatrixList() throws IOException {

		List<Matrix> matrixList = new ArrayList<Matrix>();

		List<Map<String, Object>> rsMatricesList = getAllMatricesList();

		int rowcount = rsMatricesList.size();
		if (rowcount > 0) {
			for (int i = rowcount - 1; i >= 0; i--) // loop through rows of result set
			{
				// PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsMatricesList.isEmpty() && rsMatricesList.get(i).size() == cDBMatrix.T_CREATE_MATRICES.length) {
					int currentMatrixId = (Integer) rsMatricesList.get(i).get(cDBMatrix.f_ID);
					Matrix currentMatrix = new Matrix(currentMatrixId);
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

	public static List<Map<String, Object>> getAllMatricesList() throws IOException {
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

	public static Object[][] getMatricesTable(int studyId) throws IOException {
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
//			log.error(null, ex);
		}
		return table;
	}
}

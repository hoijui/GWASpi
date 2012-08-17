package org.gwaspi.samples;

import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SampleManager {

	private SampleManager() {
	}

	///////////////////////////////////////////
	//////////// SAMPLE INFO TABLE ////////////
	//////////////////////////////////////////
	public static String createSamplesInfoTable(DbManager db) {
		boolean result = false;
		try {
			//CREATE SAMPLE table in given SCHEMA
			db.createTable(org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES,
					org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO,
					org.gwaspi.constants.cDBSamples.T_CREATE_SAMPLES_INFO);
			result = true;
		} catch (Exception e) {
			System.out.println("Error creating Sample table");
			System.out.print(e);
			e.printStackTrace();
		}
		return (result) ? "1" : "0";
	}

	public static List<Map<String, Object>> selectSampleIDList(Object poolId) {
		try {
			DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
			String sql = "SELECT " + org.gwaspi.constants.cDBSamples.f_SAMPLE_ID + " FROM " + org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES + "." + org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO + " WHERE " + org.gwaspi.constants.cDBSamples.f_POOL_ID + "='" + poolId + "' ORDER BY order_id  WITH RR";
			return dBManager.executeSelectStatement(sql);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Map<String, Object>> getAllSampleInfoFromDB() throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

		String sql = "SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES + "." + org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO + " ORDER BY order_id  WITH RR";
		return dBManager.executeSelectStatement(sql);
	}

	public static List<Map<String, Object>> getAllSampleInfoFromDBByPoolID(Object poolId) throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

		String sql = "SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES + "." + org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO + " WHERE " + org.gwaspi.constants.cDBSamples.f_POOL_ID + "='" + poolId + "'" + " ORDER BY order_id  WITH RR";
		return dBManager.executeSelectStatement(sql);
	}

	public static List<Map<String, Object>> getCurrentSampleInfoFromDB(String sampleId, Object poolId) throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

		String sql = "SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES + "." + org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO + " WHERE " + org.gwaspi.constants.cDBSamples.f_SAMPLE_ID + "='" + sampleId + "' AND " + org.gwaspi.constants.cDBSamples.f_POOL_ID + "='" + poolId + "'  WITH RR";
		return dBManager.executeSelectStatement(sql);
	}

	public static void deleteSamplesByPoolId(Object poolId) throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

		String sql = "DELETE FROM " + org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES + "." + org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO + " WHERE " + org.gwaspi.constants.cDBSamples.f_POOL_ID + "='" + poolId + "'";
		dBManager.executeStatement(sql);
	}
}

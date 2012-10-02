package org.gwaspi.samples;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
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
public class SampleManager {

	private final static Logger log = LoggerFactory.getLogger(SampleManager.class);

	private SampleManager() {
	}

	///////////////////////////////////////////
	//////////// SAMPLE INFO TABLE ////////////
	//////////////////////////////////////////
	public static String createSamplesInfoTable(DbManager db) {
		boolean result = false;
		try {
			//CREATE SAMPLE table in given SCHEMA
			db.createTable(cDBGWASpi.SCH_SAMPLES,
					cDBSamples.T_SAMPLES_INFO,
					cDBSamples.T_CREATE_SAMPLES_INFO);
			result = true;
		} catch (Exception ex) {
			log.error("Error creating Sample table", ex);
		}
		return (result) ? "1" : "0";
	}

	public static List<Map<String, Object>> selectSampleIDList(Object poolId) {
		try {
			DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			String sql = "SELECT " + cDBSamples.f_SAMPLE_ID + " FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_POOL_ID + "='" + poolId + "' ORDER BY order_id  WITH RR";
			return dBManager.executeSelectStatement(sql);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static List<Map<String, Object>> getAllSampleInfoFromDB() throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String sql = "SELECT * FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " ORDER BY order_id  WITH RR";
		return dBManager.executeSelectStatement(sql);
	}

	public static List<Map<String, Object>> getAllSampleInfoFromDBByPoolID(Object poolId) throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String sql = "SELECT * FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_POOL_ID + "='" + poolId + "'" + " ORDER BY order_id  WITH RR";
		return dBManager.executeSelectStatement(sql);
	}

	public static List<Map<String, Object>> getCurrentSampleInfoFromDB(String sampleId, Object poolId) throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String sql = "SELECT * FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_SAMPLE_ID + "='" + sampleId + "' AND " + cDBSamples.f_POOL_ID + "='" + poolId + "'  WITH RR";
		return dBManager.executeSelectStatement(sql);
	}

	public static void deleteSamplesByPoolId(Object poolId) throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String sql = "DELETE FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_POOL_ID + "='" + poolId + "'";
		dBManager.executeStatement(sql);
	}
}

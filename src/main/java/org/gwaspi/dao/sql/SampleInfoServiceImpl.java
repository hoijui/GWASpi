package org.gwaspi.dao.sql;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gwaspi.dao.SampleInfoService;
import org.gwaspi.model.SampleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SampleInfoServiceImpl implements SampleInfoService {

	private static final Logger log = LoggerFactory.getLogger(SampleInfoServiceImpl.class);

	public SampleInfoServiceImpl() {
	}

	@Override
	public String createSamplesInfoTable(DbManager db) {
		boolean result = false;
		try {
			// CREATE SAMPLE table in given SCHEMA
			db.createTable(cDBGWASpi.SCH_SAMPLES,
					cDBSamples.T_SAMPLES_INFO,
					cDBSamples.T_CREATE_SAMPLES_INFO);
			result = true;
		} catch (Exception ex) {
			log.error("Error creating Sample table", ex);
		}
		return (result) ? "1" : "0";
	}

	@Override
	public List<String> selectSampleIDList(Object poolId) {
		try {
			DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			String sql = "SELECT " + cDBSamples.f_SAMPLE_ID + " FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_POOL_ID + "='" + poolId + "' ORDER BY order_id  WITH RR";
			List<Map<String, Object>> rs = dBManager.executeSelectStatement(sql);

			List<String> sampleIds = new ArrayList<String>(rs.size());
			for (Map<String, Object> id : rs) {
				// PREVENT PHANTOM-DB READS EXCEPTIONS - CAUTION!!
				if (!id.isEmpty()) {
					sampleIds.add(id.values().iterator().next().toString());
				}
			}

			return sampleIds;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<SampleInfo> getAllSampleInfoFromDB() throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String sql = "SELECT * FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " ORDER BY order_id  WITH RR";
		List<Map<String, Object>> rs = dBManager.executeSelectStatement(sql);

		return parseSampleInfos(rs);
	}

	@Override
	public List<SampleInfo> getAllSampleInfoFromDBByPoolID(Object poolId) throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String sql = "SELECT * FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_POOL_ID + "='" + poolId + "'" + " ORDER BY order_id  WITH RR";
		List<Map<String, Object>> rs = dBManager.executeSelectStatement(sql);

		return parseSampleInfos(rs);
	}

	@Override
	public List<SampleInfo> getCurrentSampleInfoFromDB(String sampleId, Object poolId) throws IOException {

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String sql = "SELECT * FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_SAMPLE_ID + "='" + sampleId + "' AND " + cDBSamples.f_POOL_ID + "='" + poolId + "'  WITH RR";
		List<Map<String, Object>> rs = dBManager.executeSelectStatement(sql);

		return parseSampleInfos(rs);
	}

	@Override
	public void deleteSamplesByPoolId(Object poolId) throws IOException {
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String sql = "DELETE FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_POOL_ID + "='" + poolId + "'";
		dBManager.executeStatement(sql);
	}

	private static List<SampleInfo> parseSampleInfos(List<Map<String, Object>> rs) {

		List<SampleInfo> sampleInfos = new ArrayList<SampleInfo>(rs.size());
		int id = 1;
		for (Map<String, Object> sampleProperties : rs) {
			// PREVENT PHANTOM-DB READS EXCEPTIONS - CAUTION!!
			if (!rs.isEmpty() && (rs.size() == cDBSamples.T_CREATE_SAMPLES_INFO.length)) {
				sampleInfos.add(parseSampleInfo(id, sampleProperties));
				id++;
			}
		}

		return sampleInfos;
	}

	private static SampleInfo parseSampleInfo(int id, Map<String, Object> properties) {

		// TODO this can be improved, performance wise (or maybe better replace with JPA directy!)
		Object familyId = properties.get(cDBSamples.f_FAMILY_ID).toString();
		Object sampleId = properties.get(cDBSamples.f_SAMPLE_ID).toString();
		Object fatherId = properties.get(cDBSamples.f_FATHER_ID).toString();
		Object motherId = properties.get(cDBSamples.f_MOTHER_ID).toString();
		Object sexStr = properties.get(cDBSamples.f_SEX).toString();
		Object affectionStr = properties.get(cDBSamples.f_AFFECTION).toString();
		Object ageStr = properties.get(cDBSamples.f_AGE).toString();
		Object category = properties.get(cDBSamples.f_CATEGORY).toString();
		Object disease = properties.get(cDBSamples.f_DISEASE).toString();
		Object population = properties.get(cDBSamples.f_POPULATION).toString();
		Object filter = properties.get(cDBSamples.f_FILTER).toString();
		Object poolId = properties.get(cDBSamples.f_POOL_ID).toString();
		Object approvedStr = properties.get(cDBSamples.f_APPROVED).toString();
		Object statusStr = properties.get(cDBSamples.f_STATUS_ID_FK).toString();

		SampleInfo.Sex sex = SampleInfo.Sex.values()[Integer.parseInt(sexStr.toString())];
		SampleInfo.Affection affection = SampleInfo.Affection.values()[Integer.parseInt(affectionStr.toString())];
		int age = Integer.parseInt(ageStr.toString());
		int approved = Integer.parseInt(approvedStr.toString());
		int status = Integer.parseInt(statusStr.toString());

		SampleInfo sampleInfo = new SampleInfo(
				id,
				sampleId.toString(),
				familyId.toString(),
				fatherId.toString(),
				motherId.toString(),
				sex,
				affection,
				category.toString(),
				disease.toString(),
				population.toString(),
				age,
				filter.toString(),
				poolId.toString(),
				approved,
				status
				);

		return sampleInfo;
	}
}

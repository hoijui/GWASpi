package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import org.gwaspi.dao.SampleInfoService;
import org.gwaspi.dao.sql.SampleInfoServiceImpl;
import org.gwaspi.database.DbManager;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated use SampleInfoService directly
 */
public class SampleInfoList {

	private static final SampleInfoService sampleInfoService = new SampleInfoServiceImpl();

	private SampleInfoList() {
	}

	public static String createSamplesInfoTable(DbManager db) {
		return sampleInfoService.createSamplesInfoTable(db);
	}

	public static List<String> selectSampleIDList(Object poolId) {
		return sampleInfoService.selectSampleIDList(poolId);
	}

	public static List<SampleInfo> getAllSampleInfoFromDB() throws IOException {
		return sampleInfoService.getAllSampleInfoFromDB();
	}

	public static List<SampleInfo> getAllSampleInfoFromDBByPoolID(Object poolId) throws IOException {
		return sampleInfoService.getAllSampleInfoFromDBByPoolID(poolId);
	}

	public static List<SampleInfo> getCurrentSampleInfoFromDB(String sampleId, Object poolId) throws IOException {
		return sampleInfoService.getCurrentSampleInfoFromDB(sampleId, poolId);
	}

	public static void deleteSamplesByPoolId(Object poolId) throws IOException {
		sampleInfoService.deleteSamplesByPoolId(poolId);
	}
}

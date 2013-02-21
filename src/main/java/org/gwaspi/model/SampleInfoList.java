package org.gwaspi.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.gwaspi.dao.SampleInfoService;
import org.gwaspi.dao.sql.SampleInfoServiceImpl;

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

	public static String createSamplesInfoTable() {
		return sampleInfoService.createSamplesInfoTable();
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

	public static List<SampleInfo> getCurrentSampleInfoFromDB(SampleKey key, Object poolId) throws IOException {
		return sampleInfoService.getCurrentSampleInfoFromDB(key, poolId);
	}

	public static void deleteSamplesByPoolId(Object poolId) throws IOException {
		sampleInfoService.deleteSamplesByPoolId(poolId);
	}

	public static List<String> insertSampleInfos(Integer studyId, Collection<SampleInfo> sampleInfos) throws IOException {
		return sampleInfoService.insertSampleInfos(studyId, sampleInfos);
	}
}

package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.SampleInfo;

public interface SampleInfoService {

	String createSamplesInfoTable();

	List<String> selectSampleIDList(Object poolId);

	List<SampleInfo> getAllSampleInfoFromDB() throws IOException;

	List<SampleInfo> getAllSampleInfoFromDBByPoolID(Object poolId) throws IOException;

	List<SampleInfo> getCurrentSampleInfoFromDB(String sampleId, Object poolId) throws IOException;

	void deleteSamplesByPoolId(Object poolId) throws IOException;

	// TODO replace Map<String, Object> with SampleInfo
	List<String> insertSampleInfo(Integer studyId, Map<String, Object> sampleInfoMap) throws IOException;
}

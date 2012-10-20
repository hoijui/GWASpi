package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import org.gwaspi.database.DbManager;
import org.gwaspi.model.SampleInfo;

public interface SampleInfoService {

	String createSamplesInfoTable(DbManager db);

	List<String> selectSampleIDList(Object poolId);

	List<SampleInfo> getAllSampleInfoFromDB() throws IOException;

	List<SampleInfo> getAllSampleInfoFromDBByPoolID(Object poolId) throws IOException;

	List<SampleInfo> getCurrentSampleInfoFromDB(String sampleId, Object poolId) throws IOException;

	void deleteSamplesByPoolId(Object poolId) throws IOException;
}

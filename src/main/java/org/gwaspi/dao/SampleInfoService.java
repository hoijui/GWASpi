package org.gwaspi.dao;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;

public interface SampleInfoService {

	String createSamplesInfoTable();

	List<SampleInfo> getAllSampleInfoFromDB() throws IOException;

	List<SampleInfo> getAllSampleInfoFromDBByPoolID(Integer studyId) throws IOException;

	List<SampleInfo> getCurrentSampleInfoFromDB(SampleKey key, Integer studyId) throws IOException;

	void deleteSamplesByPoolId(Integer studyId) throws IOException;

	void insertSampleInfos(Integer studyId, Collection<SampleInfo> sampleInfos) throws IOException;
}

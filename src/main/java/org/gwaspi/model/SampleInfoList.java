package org.gwaspi.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.gwaspi.dao.SampleInfoService;
import org.gwaspi.dao.jpa.JPASampleInfoService;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated use SampleInfoService directly
 */
public class SampleInfoList {

	private static final SampleInfoService sampleInfoService
			= new JPASampleInfoService(StudyList.getEntityManagerFactory());

	private SampleInfoList() {
	}

	public static String createSamplesInfoTable() {
		return sampleInfoService.createSamplesInfoTable();
	}

	public static List<SampleInfo> getAllSampleInfoFromDB() throws IOException {
		return sampleInfoService.getAllSampleInfoFromDB();
	}

	public static List<SampleInfo> getAllSampleInfoFromDBByPoolID(Integer poolId) throws IOException {
		return sampleInfoService.getAllSampleInfoFromDBByPoolID(poolId);
	}

	public static List<SampleInfo> getCurrentSampleInfoFromDB(SampleKey key, Integer poolId) throws IOException {
		return sampleInfoService.getCurrentSampleInfoFromDB(key, poolId);
	}

	public static void deleteSamplesByPoolId(Integer poolId) throws IOException {
		sampleInfoService.deleteSamplesByPoolId(poolId);
	}

	public static void insertSampleInfos(Integer studyId, Collection<SampleInfo> sampleInfos) throws IOException {
		sampleInfoService.insertSampleInfos(studyId, sampleInfos);
	}
}

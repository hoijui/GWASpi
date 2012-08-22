package org.gwaspi.samples;

import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class InitDummySamples {

	private static String processStartTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
	private static DbManager db = null;
	private static Integer currentLoadedSamples = 0;

	private InitDummySamples() {
	}

	public static void processData(ArrayList sampleList, int studyId) throws IOException {
		initSamples(sampleList, studyId);
	}

	private static boolean initSamples(ArrayList samplesFromFileAL, int studyId) throws IOException {

		db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
		ArrayList samplesAllreadyInDBAL = new ArrayList();

		try {
			List<Map<String, Object>> rs = SampleManager.selectSampleIDList(studyId);
			for (int i = 0; i < rs.size(); i++) // loop through rows of result set
			{
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == 1) {
					samplesAllreadyInDBAL.add(rs.get(i).get(org.gwaspi.constants.cDBSamples.f_SAMPLE_ID).toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Object[]> samplesValuesList = new ArrayList<Object[]>();

		boolean result = false;
		for (int i = 0; i < samplesFromFileAL.size(); i++) {
			//Insert new Samples
			//sample_id
			//status_id_fk

			if (samplesAllreadyInDBAL.contains(samplesFromFileAL.get(i))) {
				samplesValuesList.add(new Object[]{samplesFromFileAL.get(i), //SampleID
							studyId});                   //POOL ID
			}

			if (samplesValuesList.size() > 1000) { //Writing data to DB batch by batch
				for (int j = 0; j < samplesValuesList.size(); j++) {
					result = db.insertValuesInTable(org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES,
							org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO,
							org.gwaspi.constants.cDBSamples.F_INSERT_DUMMY_SAMPLES_INFO,
							(Object[]) samplesValuesList.get(j));
				}
				samplesValuesList.clear();
			}
			currentLoadedSamples++;
		}


		//Writing remaining Samples to DB
		if (samplesValuesList.size() > 0) {
			for (int i = 0; i < samplesValuesList.size(); i++) {
				result = db.insertValuesInTable(org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES,
						org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO,
						org.gwaspi.constants.cDBSamples.F_INSERT_DUMMY_SAMPLES_INFO,
						(Object[]) samplesValuesList.get(i));
			}
			samplesValuesList.clear();
		}

		//LOG OPERATION IN STUDY HISTORY
		StringBuilder operation = new StringBuilder("Start Time: ");
		operation.append(processStartTime);
		operation.append("\n");
		operation.append("Initialized ").append(currentLoadedSamples).append(" dummy Samples from genotype input files.\n");
		operation.append("End Time:").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append("\n");
		org.gwaspi.global.Utils.logBlockInStudyDesc(operation.toString(), studyId);
		//////////////////////////////

		return result;
	}
}

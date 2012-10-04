package org.gwaspi.samples;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cImport.Annotation.GWASpi;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.ArrayList;
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
public class InsertSampleInfo {

	private static final Logger log = LoggerFactory.getLogger(InsertSampleInfo.class);

	private static String processStartTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
	private static DbManager db = null;

	private InsertSampleInfo() {
	}

	public static List<String> processData(Integer studyId, Map<String, Object> sampleInfoMap) throws IOException {
		/////////////////////////////////////////////////
		///////// Retrieving Samplelist from DB /////////
		/////////////////////////////////////////////////
		List<String> samplesAllreadyInDBAL = new ArrayList<String>();
		String dbName = cDBGWASpi.DB_DATACENTER;
		db = ServiceLocator.getDbManager(dbName);
		try {
			List<Map<String, Object>> rs = SampleManager.selectSampleIDList(studyId);
			for (int i = 0; i < rs.size(); i++) // loop through rows of result set
			{
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rs.isEmpty() && rs.get(i).size() == 1) {
					samplesAllreadyInDBAL.add(rs.get(i).get(cDBSamples.f_SAMPLE_ID).toString());
				}
			}
		} catch (Exception ex) {
			log.error(null, ex);
		}

		List<String> result = new ArrayList<String>();
		if (!sampleInfoMap.isEmpty()) {
			//FIRST UPDATE SAMPLES ALLREADY IN DB
			updateSamplesByHashMap(studyId, sampleInfoMap, samplesAllreadyInDBAL);

			//NEXT INSERT ANY NEW SAMPLES
			result = insertSamplesByHashMap(studyId, sampleInfoMap, samplesAllreadyInDBAL);
		}
		return result;
	}

	private static List<String> insertSamplesByHashMap(Integer studyId, Map<String, Object> sampleInfoMap, List<String> samplesAllreadyInDBAL) throws IOException {

		db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		List<String> result = new ArrayList<String>();
		for (Object value : sampleInfoMap.values()) {
			String[] cVals = (String[]) value;

			String sampleId = cVals[GWASpi.sampleId];
			result.add(sampleId);

			// Standardizing affection to CHAR(1), having 1=Unaffected, 2=Affected
			// HACK TODO someday this should allow other affection values
			String affection = cVals[GWASpi.affection];
			if (!affection.equals("1") && !affection.equals("2")) {
				affection = "0";
			}

			if (!samplesAllreadyInDBAL.contains(sampleId)) {

				db.insertValuesInTable(cDBGWASpi.SCH_SAMPLES,
						cDBSamples.T_SAMPLES_INFO,
						cDBSamples.F_INSERT_SAMPLES_ALLINFO,
						new Object[]{sampleId, //SampleID (max 32 chars, null=unknown)
							cVals[GWASpi.familyId], //FamilyID (max 32 chars, null=unknown)
							cVals[GWASpi.fatherId], //FatherID (max 32 chars, null=unknown)
							cVals[GWASpi.motherId], //MotherID (max 32 chars, null=unknown)
							cVals[GWASpi.sex], //Sex (1=male,2=female,0=unknown)
							affection, //Affection (1=unaffected,2=affected,0=unknown)
							cVals[GWASpi.category], //category
							cVals[GWASpi.disease], //disease
							cVals[GWASpi.population], //population
							cVals[GWASpi.age], //age
							studyId});  //POOL ID
			}
		}

		log.info("Samples stored in DB: {}", result.size());
		db = null;

		if (result.size() > 0) {
			// LOG OPERATION IN STUDY HISTORY
			StringBuilder operation = new StringBuilder("Start Time: ");
			operation.append(processStartTime);
			operation.append("\n");
			operation.append("Inserted ").append(result.size()).append(" Samples from info file.\n");
			operation.append("End Time:").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append("\n");
			org.gwaspi.global.Utils.logBlockInStudyDesc(operation.toString(), studyId);
		}

		return result;
	}

	public static int updateSamplesByHashMap(Integer studyId, Map<String, Object> sampleInfoMap, List<String> samplesAllreadyInDBAL) throws IOException {

		db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		int result = 0;

		for (Object value : sampleInfoMap.values()) {
			String[] cVals = (String[]) value;

			// Standardizing affection to CHAR(1), having 1=Unaffected, 2=Affected
			// HACK TODO someday this should allow other affection values
			String affection = cVals[GWASpi.affection];
			if (!affection.equals("1") && !affection.equals("2")) {
				affection = "0";
			}

			if (samplesAllreadyInDBAL.contains(cVals[1].toString())) {
				db.updateTable(cDBGWASpi.SCH_SAMPLES,
						cDBSamples.T_SAMPLES_INFO,
						cDBSamples.F_UPDATE_SAMPLES_ALLINFO,
						new Object[]{cVals[0], //FamilyID (max 32 chars, null=unknown)
							cVals[2], //FatherID (max 32 chars, null=unknown)
							cVals[3], //MotherID (max 32 chars, null=unknown)
							cVals[4], //Sex (1=male,2=female,0=unknown)
							affection, //Affection (1=unaffected,2=affected,0=unknown)
							cVals[6], //category
							cVals[7], //disease
							cVals[8], //population
							Integer.parseInt(cVals[9]), //age
							studyId.toString(),
							100}, //status_id_fk = 100 (OK)
						new String[]{cDBSamples.f_SAMPLE_ID, cDBSamples.f_POOL_ID},
						new String[]{cVals[1].toString(), studyId.toString()});
				result++;
			}

		}

		log.info("Updated {} samples", result);
		db = null;

		if (result > 0) {
			// LOG OPERATION IN STUDY HISTORY
			StringBuilder operation = new StringBuilder("Start Time: ");
			operation.append(processStartTime);
			operation.append("\n");
			operation.append("Updated ").append(result).append(" Samples from info file.\n");
			operation.append("End Time:").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append("\n");
			org.gwaspi.global.Utils.logBlockInStudyDesc(operation.toString(), studyId);
		}

		return result;
	}
}

package org.gwaspi.samples;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport.Annotation.GWASpi;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class InsertSampleInfo {

	private static String processStartTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
	private static DbManager db = null;

	public static ArrayList processData(Integer studyId, LinkedHashMap sampleInfoLHM) throws FileNotFoundException, IOException {
		/////////////////////////////////////////////////
		///////// Retrieving Samplelist from DB /////////
		/////////////////////////////////////////////////
		List<Map<String, Object>> rs = null;
		ArrayList samplesAllreadyInDBAL = new ArrayList();
		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		db = org.gwaspi.global.ServiceLocator.getDbManager(dbName);
		try {
			rs = SampleManager.selectSampleIDList(studyId);
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

		ArrayList result = new ArrayList();
		if (!sampleInfoLHM.isEmpty()) {
			//FIRST UPDATE SAMPLES ALLREADY IN DB
			updateSamplesByHashMap(studyId, sampleInfoLHM, samplesAllreadyInDBAL);

			//NEXT INSERT ANY NEW SAMPLES
			result = insertSamplesByHashMap(studyId, sampleInfoLHM, samplesAllreadyInDBAL);
		}
		return result;
	}

	private static ArrayList insertSamplesByHashMap(Integer studyId, LinkedHashMap sampleInfoLHM, ArrayList samplesAllreadyInDBAL) throws IOException {

		db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

		ArrayList result = new ArrayList();
		for (Iterator it = sampleInfoLHM.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			String[] cVals = (String[]) sampleInfoLHM.get(key);

			String sampleId = cVals[GWASpi.sampleId];
			result.add(sampleId);

			//Standardizing affection to CHAR(1), having 1=Unaffected, 2=Affected
			//TODO: someday this should allow other affection values
			String affection = cVals[GWASpi.affection];
			if (!affection.equals("1") && !affection.equals("2")) {
				affection = "0";
			}

			if (!samplesAllreadyInDBAL.contains(sampleId)) {

				db.insertValuesInTable(org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES,
						org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO,
						org.gwaspi.constants.cDBSamples.F_INSERT_SAMPLES_ALLINFO,
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
				cVals = null;
			}
		}

		////////
		System.out.println("Samples stored in DB: " + result.size());
		db = null;

		if (result.size() > 0) {
			//LOG OPERATION IN STUDY HISTORY
			StringBuffer operation = new StringBuffer("Start Time: ");
			operation.append(processStartTime);
			operation.append("\n");
			operation.append("Inserted " + result.size() + " Samples from info file.\n");
			operation.append("End Time:" + org.gwaspi.global.Utils.getMediumDateTimeAsString() + "\n");
			org.gwaspi.global.Utils.logBlockInStudyDesc(operation.toString(), studyId);
			//////////////////////////////
		}


		return result;
	}

	public static int updateSamplesByHashMap(Integer studyId, LinkedHashMap sampleInfoLHM, ArrayList samplesAllreadyInDBAL) throws IOException {

		db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
		int result = 0;

		for (Iterator it = sampleInfoLHM.keySet().iterator(); it.hasNext();) {
			Object sampleId = it.next();
			String[] cVals = (String[]) sampleInfoLHM.get(sampleId);

			//Standardizing affection to CHAR(1), having 1=Unaffected, 2=Affected
			//TODO: someday this should allow other affection values
			String affection = cVals[GWASpi.affection];
			if (!affection.equals("1") && !affection.equals("2")) {
				affection = "0";
			}

			if (samplesAllreadyInDBAL.contains(cVals[1].toString())) {
				db.updateTable(org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES,
						org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO,
						org.gwaspi.constants.cDBSamples.F_UPDATE_SAMPLES_ALLINFO,
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
						new String[]{constants.cDBSamples.f_SAMPLE_ID, org.gwaspi.constants.cDBSamples.f_POOL_ID},
						new String[]{cVals[1].toString(), studyId.toString()});
				result++;
				cVals = null;
			}

		}

		System.out.println("Updated " + result + " samples");
		db = null;

		if (result > 0) {
			//LOG OPERATION IN STUDY HISTORY
			StringBuffer operation = new StringBuffer("Start Time: ");
			operation.append(processStartTime);
			operation.append("\n");
			operation.append("Updated " + result + " Samples from info file.\n");
			operation.append("End Time:" + org.gwaspi.global.Utils.getMediumDateTimeAsString() + "\n");
			org.gwaspi.global.Utils.logBlockInStudyDesc(operation.toString(), studyId);
			//////////////////////////////
		}

		return result;
	}
}

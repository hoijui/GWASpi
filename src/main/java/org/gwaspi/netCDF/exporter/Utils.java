package org.gwaspi.netCDF.exporter;

import java.io.IOException;
import java.util.List;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Utils {

	private Utils() {
	}

	public static SampleInfo getCurrentSampleFormattedInfo(SampleKey key, Integer poolId) throws IOException {

		SampleInfo sampleInfo = null;

		List<SampleInfo> sampleInfos = SampleInfoList.getCurrentSampleInfoFromDB(key, poolId);

		// PREVENT PHANTOM-DB READS EXCEPTIONS
		if (!sampleInfos.isEmpty()) {
			SampleInfo baseSampleInfo = sampleInfos.get(0);

			// XXX maybe we should make use of the familyId in key instead (or at least aswell, checking this value against it)
			String familyId = baseSampleInfo.getFamilyId();
			if (familyId == null) {
				familyId = "0";
			}

			String fatherId = baseSampleInfo.getFatherId();
			if (fatherId == null) {
				fatherId = "0";
			}

			String motherId = baseSampleInfo.getMotherId();
			if (motherId == null) {
				motherId = "0";
			}

			SampleInfo.Sex sex = baseSampleInfo.getSex();
			if (sex == null) {
				sex = SampleInfo.Sex.UNKNOWN;
			}

			SampleInfo.Affection affection = baseSampleInfo.getAffection();
			if (affection == null) {
				affection = SampleInfo.Affection.UNKNOWN;
			}

			String disease = baseSampleInfo.getDisease();
			if (disease == null) {
				disease = "0";
			}

			String category = baseSampleInfo.getCategory();
			if (category == null) {
				category = "0";
			}

			String population = baseSampleInfo.getPopulation();
			if (population == null) {
				population = "0";
			}

			int age = baseSampleInfo.getAge();
			if (age == -1) {
				age = 0;
			}

			sampleInfo = new SampleInfo(
					baseSampleInfo.getOrderId(),
					key.getSampleId(),
					familyId,
					fatherId,
					motherId,
					sex,
					affection,
					category,
					disease,
					population,
					age,
					baseSampleInfo.getFilter(),
					baseSampleInfo.getPoolId(),
					baseSampleInfo.getApproved(),
					baseSampleInfo.getStatus());
		}

		return sampleInfo;
	}
}

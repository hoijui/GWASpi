/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.netCDF.exporter;

import java.io.IOException;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;

public class Utils {

	private Utils() {
	}

	public static SampleInfo getCurrentSampleFormattedInfo(SampleKey key) throws IOException {

		SampleInfo sampleInfo = SampleInfoList.getSample(key);

		// PREVENT PHANTOM-DB READS EXCEPTIONS
		if (sampleInfo == null) {
			throw new IOException("No sample-info found in the DB for sample-key: " + key.toString());
		} else {
			SampleInfo baseSampleInfo = sampleInfo;

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
					baseSampleInfo.getStudyKey(),
					baseSampleInfo.getApproved(),
					baseSampleInfo.getStatus());
		}

		return sampleInfo;
	}
}

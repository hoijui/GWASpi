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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesInfosSource;

public class Utils {

	private Utils() {
	}

	public static File checkDirPath(final String path) throws IOException {

		final File dir = new File(path);
		if (!dir.exists()) {
			throw new IOException("Directory does not exist: " + path);
		}
		if (!dir.isDirectory()) {
			throw new IOException("Path does not point to a directory: " + path);
		}

		return dir;
	}

	public static Map<SampleKey, SampleInfo> createSampleKeyToInfoMap(final SamplesInfosSource samplesInfosSource) {

		final Map<SampleKey, SampleInfo> sampleKeyToInfo = new HashMap<SampleKey, SampleInfo>();
		for (SampleInfo sampleInfo : samplesInfosSource) {
			sampleKeyToInfo.put(SampleKey.valueOf(sampleInfo), sampleInfo);
		}
		return sampleKeyToInfo;
	}

	public static SampleInfo getCurrentSampleFormattedInfo(SampleKey key) throws IOException {

		SampleInfo baseSampleInfo = SampleInfoList.getSample(key);

		// PREVENT PHANTOM-DB READS EXCEPTIONS
		if (baseSampleInfo == null) {
			throw new IOException("No sample-info found in the DB for sample-key: " + key.toString());
		} else {
			baseSampleInfo = formatSampleInfo(baseSampleInfo);
		}

		return baseSampleInfo;
	}

	public static SampleInfo formatSampleInfo(final SampleInfo baseSampleInfo) throws IOException {

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

		return new SampleInfo(
				baseSampleInfo.getStudyKey(),
				baseSampleInfo.getSampleId(),
				familyId,
				baseSampleInfo.getOrderId(),
				fatherId,
				motherId,
				sex,
				affection,
				category,
				disease,
				population,
				age,
				baseSampleInfo.getFilter(),
				baseSampleInfo.getApproved(),
				baseSampleInfo.getStatus());
	}
}

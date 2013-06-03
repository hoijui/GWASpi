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

package org.gwaspi.model;

public final class SampleKeyFactory implements KeyFactory<SampleKey> {

	private final StudyKey studyKey;

	public SampleKeyFactory(StudyKey studyKey) {
		this.studyKey = studyKey;
	}

	public static String encodeStatic(SampleKey key) {
		return key.getSampleId() + " " + key.getFamilyId();
	}

	public static SampleKey decodeStatic(StudyKey studyKey, String keyStr) {
		SampleKey key = null;

		String[] parts = keyStr.split(" ", 3);
		if (parts.length == 2) {
			key = new SampleKey(studyKey, parts[0], parts[1]);
		}

		// TODO throw some type of runtime exception if keyStr does not conform strictly to a valid key
		return key;
	}

	@Override
	public String encode(SampleKey key) {
		return encodeStatic(key);
	}

	@Override
	public SampleKey decode(String keyStr) {
		return decodeStatic(studyKey, keyStr);
	}
}

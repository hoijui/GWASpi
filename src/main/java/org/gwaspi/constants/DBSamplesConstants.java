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

package org.gwaspi.constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfo.Sex;

public class DBSamplesConstants {

	// * ALL SAMPLES INFO *
	public static final String f_ID = "order_id";
	public static final String f_SAMPLE_ID = "sample_id";
	public static final String f_FAMILY_ID = "family_id";
	public static final String f_FATHER_ID = "father_id";
	public static final String f_MOTHER_ID = "mother_id";
	public static final String f_SEX = "sex";
	public static final String f_AFFECTION = "affection";
	public static final String f_CATEGORY = "category";
	public static final String f_DISEASE = "disease";
	public static final String f_POPULATION = "population";
	public static final String f_AGE = "age";
	public static final String f_FILTER = "filter";
	public static final String f_POOL_ID = "pool_id";
	public static final String f_APPROVED = "approved";
	public static final String f_STATUS_ID_FK = "status_id_fk";
	public static final List<String> f_PHENOTYPES_COLUMNS;
	static {
		final ArrayList<String> tmpPhenotypesColumns = new ArrayList<String>();
		tmpPhenotypesColumns.add(f_AFFECTION);
		tmpPhenotypesColumns.add(f_AGE);
		tmpPhenotypesColumns.add(f_CATEGORY);
		tmpPhenotypesColumns.add(f_DISEASE);
		tmpPhenotypesColumns.add(f_FAMILY_ID);
		tmpPhenotypesColumns.add(f_FATHER_ID);
		tmpPhenotypesColumns.add(f_MOTHER_ID);
		tmpPhenotypesColumns.add(f_POPULATION);
		tmpPhenotypesColumns.add(f_SEX);
		tmpPhenotypesColumns.trimToSize();
		f_PHENOTYPES_COLUMNS = Collections.unmodifiableList(tmpPhenotypesColumns);
	}

	public static Object parseFromField(String fieldName, String value) {

		if (fieldName.equals(DBSamplesConstants.f_SAMPLE_ID)) {
			return value.toCharArray();
		} else if (fieldName.equals(DBSamplesConstants.f_FAMILY_ID)) {
			return value.toCharArray();
		} else if (fieldName.equals(DBSamplesConstants.f_SEX)) {
			return Sex.parse(value);
		} else if (fieldName.equals(DBSamplesConstants.f_AFFECTION)) {
			return Affection.parse(value);
		} else if (fieldName.equals(DBSamplesConstants.f_CATEGORY)) {
			return value.toCharArray();
		} else if (fieldName.equals(DBSamplesConstants.f_DISEASE)) {
			return value.toCharArray();
		} else if (fieldName.equals(DBSamplesConstants.f_POPULATION)) {
			return value.toCharArray();
		} else if (fieldName.equals(DBSamplesConstants.f_AGE)) {
			return Integer.parseInt(value);
		} else {
			throw new IllegalArgumentException("Can not parse to type of sample field \"" + fieldName + "\"");
		}
	}

	private DBSamplesConstants() {
	}
}

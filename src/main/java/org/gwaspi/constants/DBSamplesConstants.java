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
	public static final String F_ID = "order_id";
	public static final String F_SAMPLE_ID = "sample_id";
	public static final String F_FAMILY_ID = "family_id";
	public static final String F_FATHER_ID = "father_id";
	public static final String F_MOTHER_ID = "mother_id";
	public static final String F_SEX = "sex";
	public static final String F_AFFECTION = "affection";
	public static final String F_CATEGORY = "category";
	public static final String F_DISEASE = "disease";
	public static final String F_POPULATION = "population";
	public static final String F_AGE = "age";
	public static final String F_FILTER = "filter";
	public static final String F_POOL_ID = "pool_id";
	public static final String F_APPROVED = "approved";
	public static final String F_STATUS_ID_FK = "status_id_fk";
	public static final List<String> F_PHENOTYPES_COLUMNS;
	static {
		final ArrayList<String> tmpPhenotypesColumns = new ArrayList<String>();
		tmpPhenotypesColumns.add(F_AFFECTION);
		tmpPhenotypesColumns.add(F_AGE);
		tmpPhenotypesColumns.add(F_CATEGORY);
		tmpPhenotypesColumns.add(F_DISEASE);
		tmpPhenotypesColumns.add(F_FAMILY_ID);
		tmpPhenotypesColumns.add(F_FATHER_ID);
		tmpPhenotypesColumns.add(F_MOTHER_ID);
		tmpPhenotypesColumns.add(F_POPULATION);
		tmpPhenotypesColumns.add(F_SEX);
		tmpPhenotypesColumns.trimToSize();
		F_PHENOTYPES_COLUMNS = Collections.unmodifiableList(tmpPhenotypesColumns);
	}

	public static Object parseFromField(String fieldName, String value) {

		if (fieldName.equals(DBSamplesConstants.F_SAMPLE_ID)) {
			return value.toCharArray();
		} else if (fieldName.equals(DBSamplesConstants.F_FAMILY_ID)) {
			return value.toCharArray();
		} else if (fieldName.equals(DBSamplesConstants.F_SEX)) {
			return Sex.parse(value);
		} else if (fieldName.equals(DBSamplesConstants.F_AFFECTION)) {
			return Affection.parse(value);
		} else if (fieldName.equals(DBSamplesConstants.F_CATEGORY)) {
			return value.toCharArray();
		} else if (fieldName.equals(DBSamplesConstants.F_DISEASE)) {
			return value.toCharArray();
		} else if (fieldName.equals(DBSamplesConstants.F_POPULATION)) {
			return value.toCharArray();
		} else if (fieldName.equals(DBSamplesConstants.F_AGE)) {
			return Integer.parseInt(value);
		} else {
			throw new IllegalArgumentException("Can not parse to type of sample field \"" + fieldName + "\"");
		}
	}

	private DBSamplesConstants() {
	}
}

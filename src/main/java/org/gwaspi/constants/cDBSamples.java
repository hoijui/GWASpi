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

public class cDBSamples {

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
	public static final String[] f_PHENOTYPES_COLUMNS = new String[] {
		f_AFFECTION,
		f_AGE,
		f_CATEGORY,
		f_DISEASE,
		f_FAMILY_ID,
		f_FATHER_ID,
		f_MOTHER_ID,
		f_POPULATION,
		f_SEX
	};

	private cDBSamples() {
	}
}

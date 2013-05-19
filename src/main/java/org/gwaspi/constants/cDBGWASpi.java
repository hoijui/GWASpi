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

public class cDBGWASpi {

	/* ****************************
	 * DATABASES DEFINITION BLOCK
	 * **************************** */
	public static final String DB_DATACENTER = "derbyDB";
	/* *************************
	 * SCHEMA DEFINITION BLOCK
	 * ************************* */
	public static final String SCH_APP = "APP";
	public static final String SCH_MARKERS = "MARKERS";
	public static final String SCH_SAMPLES = "SAMPLES";
	public static final String SCH_MATRICES = "MATRICES";
	/* ************************
	 * TABLE DEFINITION BLOCK
	 * ************************ */
	// * MANAGEMENT TABLES *
    /* STUDIES */
	public static final String STUDY_PREFIX = "STUDY_";
	public static final String f_ID = "id";
	public static final String f_NAME = "name";
	public static final String f_STUDY_TYPE = "study_type";
	public static final String f_VALIDITY = "validity";
	public static final String f_STUDY_DESCRIPTION = "study_description";
	public static final String f_CREATION_DATE = "creation_date";
	public static final String T_STUDIES = "STUDIES";
	public static final String[] T_CREATE_STUDIES = new String[] {
		f_ID + " INTEGER generated by default as identity",
		f_NAME + " VARCHAR(64)",
		f_STUDY_DESCRIPTION + " LONG VARCHAR",
		f_STUDY_TYPE + " VARCHAR(255)",
		f_VALIDITY + " SMALLINT",
		f_CREATION_DATE + " TIMESTAMP default CURRENT_TIMESTAMP"
	};
	public static final String[] F_INSERT_STUDIES = new String[] {
		f_NAME,
		f_STUDY_DESCRIPTION,
		f_STUDY_TYPE,
		f_VALIDITY
	};
	public static final String[] F_SELECT_STUDIES = new String[] {
		f_ID,
		f_NAME,
		f_STUDY_DESCRIPTION,
		f_STUDY_TYPE,
		f_VALIDITY
	};
	/* STATUS_TYPES */
	public static final String T_STATUS_TYPES = "STATUS_TYPES";
	public static final String[] T_CREATE_STATUS_TYPES = new String[] {
		"id INTEGER",
		"name VARCHAR(50)",
		"status_description VARCHAR(250)"
	};
	public static final String[] F_STATUS_TYPES = new String[] {
		"id",
		"name",
		"status_description"
	};
	public static final String IE_STATUS_TYPES_INIT = "INSERT INTO " + cDBGWASpi.SCH_APP + "." + T_STATUS_TYPES
			+ " (id, name, status_description) "
			+ "VALUES "
			+ "(0,'LOADED','Freshly loaded data'),"
			+ "(2,'DUPL_BY_HASH','Row is duplicate by hash value'),"
			+ "(100,'OK','OKd data'),"
			+ "(200,'MISSING','Data with missing info'),"
			+ "(300,'INCOMPLETE','Incoplete Data'),"
			+ "(400,'INCONSISTENT','Data with inconsistent info'),"
			+ "(500,'ARTIFACT','Data with artifact info'),"
			+ "(600,'ERRONEOUS','Data with erroneous info'),"
			+ "(900,'DELETED','Data to be ignored')";

	private cDBGWASpi() {
	}
}

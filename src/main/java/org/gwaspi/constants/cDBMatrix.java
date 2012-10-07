package org.gwaspi.constants;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class cDBMatrix {

	// * MATRIX METADATA *
	public static final String f_ID = "id";
	public static final String f_MATRIX_NAME = "matrix_name";
	public static final String f_NETCDF_NAME = "netcdf_name";
	public static final String f_MATRIX_TYPE = "matrix_type";
	public static final String f_PARENT_MATRIX1_ID = "parent_matrix1_id";
	public static final String f_PARENT_MATRIX2_ID = "parent_matrix2_id";
	public static final String f_INPUT_LOCATION = "input_location";
	public static final String f_DESCRIPTION = "description";
	public static final String f_LOADED = "loaded";
	public static final String f_STUDYID = "study_id";
	public static final String f_CREATION_DATE = "creation_date";
	public static final String T_MATRICES = "MATRICES";
	public static final String[] T_CREATE_MATRICES = new String[] {
		f_ID + " INTEGER generated by default as identity",
		f_MATRIX_NAME + " VARCHAR(64) NOT NULL",
		f_NETCDF_NAME + " VARCHAR(64) NOT NULL",
		f_MATRIX_TYPE + " VARCHAR(32) NOT NULL",
		f_PARENT_MATRIX1_ID + " INTEGER",
		f_PARENT_MATRIX2_ID + " INTEGER",
		f_INPUT_LOCATION + " VARCHAR(1000)",
		f_DESCRIPTION + " LONG VARCHAR",
		f_LOADED + " CHAR(1)",
		f_STUDYID + " INTEGER",
		f_CREATION_DATE + " TIMESTAMP default CURRENT_TIMESTAMP"
	};
	public static final String[] F_INSERT_MATRICES = new String[] {
		f_MATRIX_NAME,
		f_NETCDF_NAME,
		f_MATRIX_TYPE,
		f_PARENT_MATRIX1_ID,
		f_PARENT_MATRIX2_ID,
		f_INPUT_LOCATION,
		f_DESCRIPTION,
		f_LOADED,
		f_STUDYID
	};
	public static final String[] F_SELECT_MATRICES = new String[] {
		f_ID,
		f_MATRIX_NAME,
		f_NETCDF_NAME,
		f_MATRIX_TYPE,
		f_PARENT_MATRIX1_ID,
		f_PARENT_MATRIX2_ID,
		f_INPUT_LOCATION,
		f_DESCRIPTION,
		f_LOADED,
		f_STUDYID
	};

	private cDBMatrix() {
	}
}

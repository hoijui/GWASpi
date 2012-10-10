package org.gwaspi.netCDF.matrices;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixMetadata {

	private final Logger log = LoggerFactory.getLogger(MatrixManager.class);

	private static final String SQL_STATEMENT_SELECT_MATRIX_BY_ID
			= "SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " WHERE " + cDBMatrix.f_ID + "=%d  WITH RR";
	private static final String SQL_STATEMENT_SELECT_MATRIX_BY_NAME
			= "SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBMatrix.T_MATRICES + " WHERE " + cDBMatrix.f_NETCDF_NAME + "='%s'  WITH RR";
	private int matrixId = Integer.MIN_VALUE;
	private String matrixFriendlyName = "";
	private String matrixNetCDFName = "";
	private String pathToMatrix = "";
	private ImportFormat technology = ImportFormat.UNKNOWN;
	private String gwaspiDBVersion = "";
	private String description = "";
	private GenotypeEncoding gtEncoding = null;
	private StrandType strand = null;
	private boolean hasDictionray = false;
	private int markerSetSize = Integer.MIN_VALUE;
	private int sampleSetSize = Integer.MIN_VALUE;
	private int studyId = Integer.MIN_VALUE;
	private String matrixType = ""; // matrix_type VARCHAR(32) NOT NULL
	private int parentMatrixId1 = Integer.MIN_VALUE; // parent_matrix1_id INTEGER
	private int parentMatrixId2 = Integer.MIN_VALUE; // parent_matrix2_id INTEGER
	private String input_location = ""; // input_location VARCHAR(1000)
	private String loaded = ""; // loaded CHAR(1)

	public MatrixMetadata(int matrixId) throws IOException {

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		String statement = String.format(SQL_STATEMENT_SELECT_MATRIX_BY_ID, matrixId);
		List<Map<String, Object>> rs = dBManager.executeSelectStatement(statement);

		this.matrixId = matrixId;

		loadFromResultRest(rs);
	}

	public MatrixMetadata(String netCDFname) throws IOException {

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		String statement = String.format(SQL_STATEMENT_SELECT_MATRIX_BY_NAME, netCDFname);
		List<Map<String, Object>> rs = dBManager.executeSelectStatement(statement);

		if (!rs.isEmpty() && rs.get(0).size() == cDBMatrix.T_CREATE_MATRICES.length) {
			this.matrixId = Integer.parseInt(rs.get(0).get(cDBMatrix.f_ID).toString());
		}

		loadFromResultRest(rs);
	}

    /**
	 * This Method used to import GWASpi matrix from an external file
	 * The size of this Map is very small.
	 */
	public MatrixMetadata(String netCDFpath, int studyId, String newMatrixName) throws IOException {

		this.matrixId = Integer.MIN_VALUE;
		this.matrixFriendlyName = newMatrixName;
		this.matrixNetCDFName = org.gwaspi.netCDF.matrices.MatrixManager.generateMatrixNetCDFNameByDate();
		this.studyId = studyId;

		this.pathToMatrix = netCDFpath;
		loadMatrixFromFile();
	}

	private void loadFromResultRest(List<Map<String, Object>> rs) throws IOException {

		// PREVENT PHANTOM-DB READS EXCEPTIONS
		if (!rs.isEmpty() && rs.get(0).size() == cDBMatrix.T_CREATE_MATRICES.length) {
			matrixId = Integer.parseInt(rs.get(0).get(cDBMatrix.f_ID).toString());
			matrixFriendlyName = (rs.get(0).get(cDBMatrix.f_MATRIX_NAME) != null) ? rs.get(0).get(cDBMatrix.f_MATRIX_NAME).toString() : ""; // matrix_name VARCHAR(64) NOT NULL
			matrixNetCDFName = (rs.get(0).get(cDBMatrix.f_NETCDF_NAME) != null) ? rs.get(0).get(cDBMatrix.f_NETCDF_NAME).toString() : ""; // netcdf_name VARCHAR(64) NOT NULL
			matrixType = (rs.get(0).get(cDBMatrix.f_MATRIX_TYPE) != null) ? rs.get(0).get(cDBMatrix.f_MATRIX_TYPE).toString() : ""; // matrix_type VARCHAR(32) NOT NULL
			parentMatrixId1 = (rs.get(0).get(cDBMatrix.f_PARENT_MATRIX1_ID) != null) ? Integer.parseInt(rs.get(0).get(cDBMatrix.f_PARENT_MATRIX1_ID).toString()) : -1; // parent_matrix1_id INTEGER
			parentMatrixId2 = (rs.get(0).get(cDBMatrix.f_PARENT_MATRIX2_ID) != null) ? Integer.parseInt(rs.get(0).get(cDBMatrix.f_PARENT_MATRIX2_ID).toString()) : -1; // parent_matrix2_id INTEGER
			input_location = (rs.get(0).get(cDBMatrix.f_INPUT_LOCATION) != null) ? rs.get(0).get(cDBMatrix.f_INPUT_LOCATION).toString() : ""; // input_location VARCHAR(1000)
			description = (rs.get(0).get(cDBMatrix.f_DESCRIPTION) != null) ? rs.get(0).get(cDBMatrix.f_DESCRIPTION).toString() : ""; // description VARCHAR(2000)
			loaded = (rs.get(0).get(cDBMatrix.f_LOADED) != null) ? rs.get(0).get(cDBMatrix.f_LOADED).toString() : "0"; // loaded CHAR(1)
			studyId = (rs.get(0).get(cDBMatrix.f_STUDYID) != null) ? Integer.parseInt(rs.get(0).get(cDBMatrix.f_STUDYID).toString()) : 0;
		}

		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
		String pathToStudy = genotypesFolder + "/STUDY_" + studyId + "/";
		pathToMatrix = pathToStudy + matrixNetCDFName + ".nc";
		loadMatrixFromFile();
	}

	private void loadMatrixFromFile() throws IOException {

		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
		String pathToStudy = genotypesFolder + "/STUDY_" + studyId + "/";
		pathToMatrix = pathToStudy + matrixNetCDFName + ".nc";
		NetcdfFile ncfile = null;
		if (new File(pathToMatrix).exists()) {
			try {
				ncfile = NetcdfFile.open(pathToMatrix);

				technology = ImportFormat.compareTo(ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY).getStringValue());
				try {
					gwaspiDBVersion = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GWASPIDB_VERSION).getStringValue();
				} catch (Exception ex) {
					log.error(null, ex);
				}

				Variable var = ncfile.findVariable(cNetCDF.Variables.GLOB_GTENCODING);
				if (var != null) {
					try {
						ArrayChar.D2 gtCodeAC = (ArrayChar.D2) var.read("(0:0:1, 0:7:1)");
//						gtEncoding = GenotypeEncoding.valueOf(gtCodeAC.getString(0));
						gtEncoding = GenotypeEncoding.compareTo(gtCodeAC.getString(0)); // HACK, the above was used before
					} catch (InvalidRangeException ex) {
						log.error(null, ex);
					}
				}

				strand = StrandType.valueOf(ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_STRAND).getStringValue());
				hasDictionray = ((Integer) ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY).getNumericValue() != 0);

				Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_MARKERSET);
				markerSetSize = markerSetDim.getLength();

				Dimension sampleSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
				sampleSetSize = sampleSetDim.getLength();
			} catch (IOException ex) {
				log.error("Cannot open file: " + ncfile, ex);
			} finally {
				if (null != ncfile) {
					try {
						ncfile.close();
					} catch (IOException ex) {
						log.warn("Cannot close file: " + ncfile, ex);
					}
				}
			}
		}
	}

	public boolean getHasDictionray() {
		return hasDictionray;
	}

	public int getMatrixId() {
		return matrixId;
	}

	public int getStudyId() {
		return studyId;
	}

	public String getMatrixFriendlyName() {
		return matrixFriendlyName;
	}

	public ImportFormat getTechnology() {
		return technology;
	}

	public String getGwaspiDBVersion() {
		return gwaspiDBVersion;
	}

	public GenotypeEncoding getGenotypeEncoding() {
		return gtEncoding;
	}

	public int getMarkerSetSize() {
		return markerSetSize;
	}

	public int getSampleSetSize() {
		return sampleSetSize;
	}

	public String getPathToMatrix() {
		return pathToMatrix;
	}

	public StrandType getStrand() {
		return strand;
	}

	public String getDescription() {
		return description;
	}

	public String getInput_location() {
		return input_location;
	}

	public String getLoaded() {
		return loaded;
	}

	public String getMatrixNetCDFName() {
		return matrixNetCDFName;
	}

	public String getMatrixType() {
		return matrixType;
	}

	public int getParentMatrixId1() {
		return parentMatrixId1;
	}

	public int getParentMatrixId2() {
		return parentMatrixId2;
	}
}

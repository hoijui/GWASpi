package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.dao.MatrixService;
import org.gwaspi.dao.sql.MatrixServiceImpl;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated use MatrixService directly
 */
public final class MatricesList {

	private static final MatrixService matrixService = new MatrixServiceImpl();

	private MatricesList() {
	}

	public static Matrix getById(int matrixId) throws IOException {
		return matrixService.getById(matrixId);
	}

	public static List<Matrix> getMatrixList(int studyId) throws IOException {
		return matrixService.getMatrixList(studyId);
	}

	public static List<Matrix> getMatrixList() throws IOException {
		return matrixService.getMatrixList();
	}

	public static List<Map<String, Object>> getAllMatricesList() throws IOException {
		return matrixService.getAllMatricesList();
	}

	public static Object[][] getMatricesTable(int studyId) throws IOException {
		return matrixService.getMatricesTable(studyId);
	}

	public static String createMatricesTable() {
		return matrixService.createMatricesTable();
	}

	public static void insertMatrixMetadata(
			int studyId,
			String matrix_name,
			String netCDF_name,
			GenotypeEncoding matrix_type,
			int parent_matrix1_id,
			int parent_matrix2_id,
			String input_location,
			String description,
			int loaded)
			throws IOException
	{
		matrixService.insertMatrixMetadata(
				studyId,
				matrix_name,
				netCDF_name,
				matrix_type,
				parent_matrix1_id,
				parent_matrix2_id,
				input_location,
				description,
				loaded);
	}

	public static void deleteMatrix(int matrixId, boolean deleteReports) {
		matrixService.deleteMatrix(matrixId, deleteReports);
	}

	public static String generateMatrixNetCDFNameByDate() {
		return matrixService.generateMatrixNetCDFNameByDate();
	}

	public static void saveMatrixDescription(int matrixId, String description) throws IOException {
		matrixService.saveMatrixDescription(matrixId, description);
	}

	public static MatrixMetadata getLatestMatrixId() throws IOException {
		return matrixService.getLatestMatrixId();
	}

	public static MatrixMetadata getMatrixMetadataById(int matrixId) throws IOException {
		return matrixService.getMatrixMetadataById(matrixId);
	}

	public static MatrixMetadata getMatrixMetadataByNetCDFname(String netCDFname) throws IOException {
		return matrixService.getMatrixMetadataByNetCDFname(netCDFname);
	}

	public static MatrixMetadata getMatrixMetadata(String netCDFpath, int studyId, String newMatrixName) throws IOException {
		return matrixService.getMatrixMetadata(netCDFpath, studyId, newMatrixName);
	}

	public static void shutdownBackend() throws IOException {
		matrixService.shutdownBackend();
	}
}

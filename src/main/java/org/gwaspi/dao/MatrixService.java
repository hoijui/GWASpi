package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.model.Matrix;
import org.gwaspi.model.MatrixMetadata;

public interface MatrixService {

	Matrix getById(int matrixId) throws IOException;

	List<Matrix> getMatrixList(int studyId) throws IOException;

	List<Matrix> getMatrixList() throws IOException;

	List<Map<String, Object>> getAllMatricesList() throws IOException;

	Object[][] getMatricesTable(int studyId) throws IOException;

	String createMatricesTable();

	void insertMatrixMetadata(
			int studyId,
			String matrix_name,
			String netCDF_name,
			GenotypeEncoding matrix_type,
			int parent_matrix1_id,
			int parent_matrix2_id,
			String input_location,
			String description,
			int loaded)
			throws IOException;

	void deleteMatrix(int matrixId, boolean deleteReports);

	String generateMatrixNetCDFNameByDate();

	void saveMatrixDescription(int matrixId, String description) throws IOException;

	MatrixMetadata getLatestMatrixId() throws IOException;

	MatrixMetadata getMatrixMetadataById(int matrixId) throws IOException;

	MatrixMetadata getMatrixMetadataByNetCDFname(String netCDFname) throws IOException;

    /**
	 * This Method used to import GWASpi matrix from an external file
	 * The size of this Map is very small.
	 */
	MatrixMetadata getMatrixMetadata(String netCDFpath, int studyId, String newMatrixName) throws IOException;

	void shutdownBackend() throws IOException;
}

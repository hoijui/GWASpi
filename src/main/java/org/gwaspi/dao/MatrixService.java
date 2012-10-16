package org.gwaspi.dao;

import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.database.DbManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Matrix;
import org.gwaspi.netCDF.matrices.MatrixMetadata;

public interface MatrixService {

	Matrix getById(int matrixId) throws IOException;

	List<Matrix> getMatrixList(int studyId) throws IOException;

	List<Matrix> getMatrixList() throws IOException;

	List<Map<String, Object>> getAllMatricesList() throws IOException;

	Object[][] getMatricesTable(int studyId) throws IOException;

	String createMatricesTable(DbManager db);

	void insertMatrixMetadata(
			DbManager dBManager,
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

	MatrixMetadata getLatestMatrixId() throws IOException;
}

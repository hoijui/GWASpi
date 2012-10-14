package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Matrix;

public interface MatrixService {

	Matrix getById(int matrixId) throws IOException;

	List<Matrix> getMatrixList(int studyId) throws IOException;

	List<Matrix> getMatrixList() throws IOException;

	List<Map<String, Object>> getAllMatricesList() throws IOException;

	Object[][] getMatricesTable(int studyId) throws IOException;
}

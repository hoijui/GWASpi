package org.gwaspi.model;

import org.gwaspi.dao.MatrixService;
import org.gwaspi.dao.sql.MatrixServiceImpl;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated use MatrixService directly
 */
public final class MatricesList {

	private static final Logger log
			= LoggerFactory.getLogger(MatricesList.class);

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
}

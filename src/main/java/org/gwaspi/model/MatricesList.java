package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import org.gwaspi.dao.MatrixService;
import org.gwaspi.dao.jpa.JPAMatrixService;
import org.gwaspi.dao.sql.MatrixServiceImpl;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated use MatrixService directly
 */
public final class MatricesList {

	public static final boolean USE_JPA = true;

	private static final MatrixService matrixService
			= USE_JPA
			? new JPAMatrixService(StudyList.getEntityManagerFactory())
			: new MatrixServiceImpl();

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

	public static List<MatrixMetadata> getMatricesTable(int studyId) throws IOException {
		return matrixService.getMatricesTable(studyId);
	}

	public static String createMatricesTable() {
		return matrixService.createMatricesTable();
	}

	public static void insertMatrixMetadata(MatrixMetadata matrixMetadata) throws IOException {
		matrixService.insertMatrixMetadata(matrixMetadata);
	}

	public static void deleteMatrix(int matrixId, boolean deleteReports) {
		matrixService.deleteMatrix(matrixId, deleteReports);
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

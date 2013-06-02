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

package org.gwaspi.model;

import java.io.IOException;
import java.util.List;
import org.gwaspi.dao.MatrixService;
import org.gwaspi.dao.jpa.JPAMatrixService;

/**
 * @deprecated use MatrixService directly
 */
public final class MatricesList {

	private static final MatrixService matrixService
			= new JPAMatrixService(StudyList.getEntityManagerFactory());

	private MatricesList() {
	}

	public static List<MatrixKey> getMatrixList(int studyId) throws IOException {
		return matrixService.getMatrixList(studyId);
	}

	public static List<MatrixKey> getMatrixList() throws IOException {
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

	public static void deleteMatrix(MatrixKey matrixKey, boolean deleteReports) throws IOException {
		matrixService.deleteMatrix(matrixKey, deleteReports);
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

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

	public static List<MatrixKey> getMatrixList(StudyKey studyKey) throws IOException {
		return matrixService.getMatrixKeys(studyKey);
	}

	public static List<MatrixKey> getMatrixList() throws IOException {
		return matrixService.getMatrixKeys();
	}

	public static List<MatrixMetadata> getMatricesTable(StudyKey studyKey) throws IOException {
		return matrixService.getMatrices(studyKey);
	}

	public static MatrixKey insertMatrixMetadata(MatrixMetadata matrixMetadata) throws IOException {
		return matrixService.insertMatrix(matrixMetadata);
	}

	public static void deleteMatrix(MatrixKey matrixKey, boolean deleteReports) throws IOException {
		matrixService.deleteMatrix(matrixKey, deleteReports);
	}

	public static void updateMatrix(MatrixMetadata matrixMetadata) throws IOException {
		matrixService.updateMatrix(matrixMetadata);
	}

	public static MatrixMetadata getMatrixMetadataById(MatrixKey matrixKey) throws IOException {
		return matrixService.getMatrix(matrixKey);
	}

	public static DataSetMetadata getDataSetMetadata(DataSetKey key) throws IOException {

		if (key.isMatrix()) {
			return getMatrixMetadataById(key.getMatrixParent());
		} else {
			return OperationsList.getOperationMetadata(key.getOperationParent());
		}
	}

	public static List<MatrixKey> getMatrixKeysBySimpleName(String simpleName) throws IOException {
		return matrixService.getMatrixKeysBySimpleName(simpleName);
	}

	public static List<MatrixKey> getMatrixKeysByName(String friendlyName) throws IOException {
		return matrixService.getMatrixKeysByName(friendlyName);
	}
}

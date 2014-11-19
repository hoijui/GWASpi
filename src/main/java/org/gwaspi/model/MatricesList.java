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

	private static MatrixService matrixService = null;

	private MatricesList() {
	}

	static void clearInternalService() {
		matrixService = null;
	}

	private static MatrixService getMatrixService() {

		if (matrixService == null) {
			matrixService = new JPAMatrixService(StudyList.getEntityManagerFactory());
		}

		return matrixService;
	}

	public static List<MatrixKey> getMatrixList(StudyKey studyKey) throws IOException {
		return getMatrixService().getMatrixKeys(studyKey);
	}

	public static List<MatrixMetadata> getMatricesTable(StudyKey studyKey) throws IOException {
		return getMatrixService().getMatrices(studyKey);
	}

	public static MatrixKey insertMatrixMetadata(MatrixMetadata matrixMetadata) throws IOException {
		return getMatrixService().insertMatrix(matrixMetadata);
	}

	public static void deleteMatrix(MatrixKey matrixKey, boolean deleteReports) throws IOException {
		getMatrixService().deleteMatrix(matrixKey, deleteReports);
	}

	public static void updateMatrix(MatrixMetadata matrixMetadata) throws IOException {
		getMatrixService().updateMatrix(matrixMetadata);
	}

	public static MatrixMetadata getMatrixMetadataById(MatrixKey matrixKey) throws IOException {
		return getMatrixService().getMatrix(matrixKey);
	}

	public static DataSetMetadata getDataSetMetadata(DataSetKey key) throws IOException {

		if (key.isMatrix()) {
			return getMatrixMetadataById(key.getMatrixParent());
		} else {
			return OperationsList.getOperationMetadata(key.getOperationParent());
		}
	}

	public static List<MatrixKey> getMatrixKeysBySimpleName(final StudyKey studyKey, final String simpleName) throws IOException {
		return getMatrixService().getMatrixKeysBySimpleName(studyKey, simpleName);
	}

	public static List<MatrixKey> getMatrixKeysByName(final StudyKey studyKey, final String friendlyName) throws IOException {
		return getMatrixService().getMatrixKeysByName(studyKey, friendlyName);
	}
}

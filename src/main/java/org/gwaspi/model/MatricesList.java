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
import org.gwaspi.dao.MatrixService;
import org.gwaspi.dao.OperationService;
import org.gwaspi.dao.jpa.JPAMatrixService;

public final class MatricesList {

	private static MatrixService matrixService = null;

	private MatricesList() {
	}

	static void clearInternalService() {
		matrixService = null;
	}

	private static OperationService getOperationService() {
		return OperationsList.getOperationService();
	}

	public static MatrixService getMatrixService() {

		if (matrixService == null) {
			matrixService = new JPAMatrixService(StudyList.getEntityManagerFactory());
		}

		return matrixService;
	}

	public static DataSetMetadata getDataSetMetadata(DataSetKey key) throws IOException {

		if (key.isMatrix()) {
			return getMatrixService().getMatrix(key.getMatrixParent());
		} else {
			return getOperationService().getOperationMetadata(key.getOperationParent());
		}
	}
}

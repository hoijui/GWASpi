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

package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;

public interface MatrixService {

	List<MatrixKey> getMatrixList(int studyId) throws IOException;

	List<MatrixKey> getMatrixList() throws IOException;

	List<MatrixMetadata> getMatricesTable(int studyId) throws IOException;

	void insertMatrixMetadata(MatrixMetadata matrixMetadata) throws IOException;

	void deleteMatrix(MatrixKey matrixKey, boolean deleteReports) throws IOException;

	void saveMatrixDescription(int matrixId, String description) throws IOException;

	MatrixMetadata getLatestMatrixId() throws IOException;

	MatrixMetadata getMatrixMetadataById(int matrixId) throws IOException;

	MatrixMetadata getMatrixMetadataByNetCDFname(String netCDFname) throws IOException;

    /**
	 * This Method used to import GWASpi matrix from an external file
	 * The size of this Map is very small.
	 */
	MatrixMetadata getMatrixMetadata(String netCDFpath, int studyId, String newMatrixName) throws IOException;
}

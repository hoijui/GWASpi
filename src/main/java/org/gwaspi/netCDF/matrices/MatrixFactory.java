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

package org.gwaspi.netCDF.matrices;

import java.io.IOException;
import java.util.Date;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.datasource.netcdf.NetCDFDataSetSource;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.netCDF.operations.OperationFactory;

public class MatrixFactory {

	private MatrixFactory() {}

	public static String generateMatrixNetCDFNameByDate(Date date) {

		String matrixName = "GT_";
		matrixName += org.gwaspi.global.Utils.getShortDateTimeForFileName(date);
		matrixName = matrixName.replace(":", "");
		matrixName = matrixName.replace(" ", "");
		matrixName = matrixName.replace("/", "");
//		matrixName = matrixName.replaceAll("[a-zA-Z]", "");
//		matrixName = matrixName.substring(0, matrixName.length() - 3); // Remove "CET" from name

		return matrixName;
	}

	public static DataSetSource generateDataSetSource(DataSetKey dataSetKey) {

		try {
			if (dataSetKey.isMatrix()) {
				return generateMatrixDataSetSource(dataSetKey.getMatrixParent());
			} else {
				return OperationFactory.generateOperationDataSet(dataSetKey.getOperationParent());
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static DataSetSource generateMatrixDataSetSource(MatrixKey matrixKey) {

		try {
			return new NetCDFDataSetSource(matrixKey);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}

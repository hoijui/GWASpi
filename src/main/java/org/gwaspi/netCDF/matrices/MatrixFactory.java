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
import java.util.Map;
import org.gwaspi.datasource.inmemory.MatrixInMemoryDataSetSource;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.datasource.netcdf.NetCDFDataSetSource;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.loader.InMemorySamplesReceiver;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.MatrixCreatingNetCDFDataSetDestination;
import org.gwaspi.operations.MatrixCreatingOperationParams;
import org.gwaspi.operations.MatrixMetadataFactory;
import org.gwaspi.operations.OperationFactory;
import org.gwaspi.operations.OperationManager;

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
				return OperationManager.generateOperationDataSet(dataSetKey.getOperationParent());
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static String getOrDefaultString(
			Map<String, Object> properties,
			String key,
			String defaultValue)
	{
		// in Java 8 we could do this
//		return (String) properties.getOrDefault(key, defaultValue);

		// ... but as we do not rely on that yet, we do this
		String value = (String) properties.get(key);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	public static <PT extends MatrixCreatingOperationParams> DataSetDestination generateMatrixDataSetDestination(
			PT params,
			MatrixMetadataFactory<DataSet, PT> metadataFactory,
			Map<String, Object> properties)
	{
		final String storageType = getOrDefaultString(
				properties,
				OperationFactory.PROPERTY_NAME_TYPE,
				AbstractDefaultTypesOperationFactory.PROPERTY_VALUE_TYPE_NETCDF);
		if (storageType.equals(AbstractDefaultTypesOperationFactory.PROPERTY_VALUE_TYPE_NETCDF)) {
			return new MatrixCreatingNetCDFDataSetDestination<PT>(params, metadataFactory);
		} else if (storageType.equals(AbstractDefaultTypesOperationFactory.PROPERTY_VALUE_TYPE_MEMORY)) {
			return new InMemorySamplesReceiver<PT>(params, metadataFactory); // TODO somehow give it the params nad metadataFactory too?
		} else {
			throw new IllegalArgumentException("Storage type not suported: " + storageType);
		}
	}

	public static <PT extends MatrixCreatingOperationParams> DataSetDestination generateMatrixDataSetDestination(PT params, MatrixMetadataFactory<DataSet, PT> metadataFactory) {
		return generateMatrixDataSetDestination(params, metadataFactory, OperationManager.FACTORY_DEFAULT_PROPERTIES);
	}

	public static DataSetSource generateMatrixDataSetSource(MatrixKey matrixKey, Map<String, Object> properties) {

		try {
			final String storageType = getOrDefaultString(
					properties,
					OperationFactory.PROPERTY_NAME_TYPE,
					AbstractDefaultTypesOperationFactory.PROPERTY_VALUE_TYPE_NETCDF);
			if (storageType.equals(AbstractDefaultTypesOperationFactory.PROPERTY_VALUE_TYPE_NETCDF)) {
				return new NetCDFDataSetSource(matrixKey);
			} else if (storageType.equals(AbstractDefaultTypesOperationFactory.PROPERTY_VALUE_TYPE_MEMORY)) {
				return new MatrixInMemoryDataSetSource(matrixKey);
			} else {
				throw new IllegalArgumentException("Storage type not suported: " + storageType);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static DataSetSource generateMatrixDataSetSource(MatrixKey matrixKey) {
		return generateMatrixDataSetSource(matrixKey, OperationManager.FACTORY_DEFAULT_PROPERTIES);
	}
}

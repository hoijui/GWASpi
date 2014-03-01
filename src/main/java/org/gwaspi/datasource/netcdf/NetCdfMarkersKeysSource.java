/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.datasource.netcdf;

import java.io.IOException;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerKeyFactory;
import org.gwaspi.model.MarkersKeysSource;
import ucar.nc2.NetcdfFile;

public class NetCdfMarkersKeysSource extends AbstractNetCdfKeysSource<MarkerKey> implements MarkersKeysSource {

	private static final int DEFAULT_CHUNK_SIZE = 200;

	public NetCdfMarkersKeysSource(NetcdfFile rdNetCdfFile, String varDimension, String varOriginalIndices, String varKeys) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, varDimension, varOriginalIndices, varKeys);
	}

	public static MarkersKeysSource createForMatrix(NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfMarkersKeysSource(rdNetCdfFile, cNetCDF.Dimensions.DIM_MARKERSET, null, cNetCDF.Variables.VAR_MARKERSET);
	}

	public static MarkersKeysSource createForOperation(NetcdfFile rdNetCdfFile, boolean operationSetMarkers) throws IOException {

		final String varDimension;
		final String varOriginalIndices;
		final String varKeys;
		if (operationSetMarkers) {
			varDimension = cNetCDF.Dimensions.DIM_OPSET;
			varOriginalIndices = cNetCDF.Variables.VAR_OPSET_IDX;
			varKeys = cNetCDF.Variables.VAR_OPSET;
		} else {
			varDimension = cNetCDF.Dimensions.DIM_IMPLICITSET;
			varOriginalIndices = cNetCDF.Variables.VAR_IMPLICITSET_IDX;
			varKeys = cNetCDF.Variables.VAR_IMPLICITSET;
		}

		return new NetCdfMarkersKeysSource(rdNetCdfFile, varDimension, varOriginalIndices, varKeys);
	}

	@Override
	protected KeyFactory<MarkerKey> createKeyFactory() {
		return new MarkerKeyFactory();
	}
}

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
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerKeyFactory;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MatrixKey;
import ucar.nc2.NetcdfFile;

public class NetCdfMarkersKeysSource extends AbstractNetCdfKeysSource<MarkerKey> implements MarkersKeysSource {

	private static final int DEFAULT_CHUNK_SIZE = 200;

	private MarkersKeysSource originSource;

	public NetCdfMarkersKeysSource(MatrixKey origin, NetcdfFile rdNetCdfFile, String varMarkersDimension, String varOriginalIndices, String varKeys) {
		super(origin, rdNetCdfFile, DEFAULT_CHUNK_SIZE, varMarkersDimension, varOriginalIndices, varKeys);

		this.originSource = null;
	}

	public static MarkersKeysSource createForMatrix(MatrixKey origin, NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfMarkersKeysSource(origin, rdNetCdfFile, NetCDFConstants.Dimensions.DIM_MARKERSET, null, NetCDFConstants.Variables.VAR_MARKERSET);
	}

	public static MarkersKeysSource createForOperation(MatrixKey origin, NetcdfFile rdNetCdfFile, boolean operationSetMarkers) throws IOException {

		final String varDimension;
		final String varOriginalIndices;
		final String varKeys;
		if (operationSetMarkers) {
			varDimension = NetCDFConstants.Dimensions.DIM_OPSET;
			varOriginalIndices = NetCDFConstants.Variables.VAR_OPSET_IDX;
			varKeys = NetCDFConstants.Variables.VAR_OPSET;
		} else {
			varDimension = NetCDFConstants.Dimensions.DIM_IMPLICITSET;
			varOriginalIndices = NetCDFConstants.Variables.VAR_IMPLICITSET_IDX;
			varKeys = NetCDFConstants.Variables.VAR_IMPLICITSET;
		}

		return new NetCdfMarkersKeysSource(origin, rdNetCdfFile, varDimension, varOriginalIndices, varKeys);
	}

	@Override
	public MarkersKeysSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getMarkersKeysSource();
			}
		}

		return originSource;
	}

	@Override
	protected KeyFactory<MarkerKey> createKeyFactory() {
		return new MarkerKeyFactory();
	}
}

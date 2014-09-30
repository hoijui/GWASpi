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
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.ChromosomeKeyFactory;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.KeyFactory;
import ucar.nc2.NetcdfFile;

public class NetCdfChromosomesKeysSource extends AbstractNetCdfKeysSource<ChromosomeKey> implements ChromosomesKeysSource {

	/**
	 * As we have max 23 chromosomes, in general,
	 * this constant does not really matter;
	 * though it should be at least 23.
	 */
	private static final int DEFAULT_CHUNK_SIZE = 100;

	private static final String varDimension = cNetCDF.Dimensions.DIM_CHRSET;
	private static final String varOriginalIndices = cNetCDF.Variables.VAR_CHR_IN_MATRIX_IDX;
	private static final String varKeys = cNetCDF.Variables.VAR_CHR_IN_MATRIX;

	private NetCdfChromosomesKeysSource(NetcdfFile rdNetCdfFile) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, varDimension, varOriginalIndices, varKeys);
	}

	public static ChromosomesKeysSource createForMatrix(NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfChromosomesKeysSource(rdNetCdfFile);
	}

	public static ChromosomesKeysSource createForOperation(NetcdfFile rdNetCdfFile, boolean operationSetMarkers) throws IOException {
		return new NetCdfChromosomesKeysSource(rdNetCdfFile);
	}

	@Override
	protected KeyFactory<ChromosomeKey> createKeyFactory() {
		return new ChromosomeKeyFactory();
	}
}

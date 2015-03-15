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
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.ChromosomeKeyFactory;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.KeyFactory;
import org.gwaspi.model.MatrixKey;
import ucar.nc2.NetcdfFile;

public class NetCdfChromosomesKeysSource extends AbstractNetCdfKeysSource<ChromosomeKey> implements ChromosomesKeysSource {

	/**
	 * As we have max 23 chromosomes, in general,
	 * this constant does not really matter;
	 * though it should be at least 23.
	 */
	private static final int DEFAULT_CHUNK_SIZE = 100;

	private static final String VAR_DIMENSION = NetCDFConstants.Dimensions.DIM_CHRSET;
	private static final String VAR_KEYS = NetCDFConstants.Variables.VAR_CHR_IN_MATRIX;

	private ChromosomesKeysSource originSource;

	private NetCdfChromosomesKeysSource(MatrixKey origin, NetcdfFile rdNetCdfFile, String varOriginalIndices) {
		super(origin, rdNetCdfFile, DEFAULT_CHUNK_SIZE, VAR_DIMENSION, varOriginalIndices, VAR_KEYS);

		this.originSource = null;
	}

	public static ChromosomesKeysSource createForMatrix(MatrixKey origin, NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfChromosomesKeysSource(origin, rdNetCdfFile, null);
	}

	public static ChromosomesKeysSource createForOperation(MatrixKey origin, NetcdfFile rdNetCdfFile, boolean operationSetMarkers) throws IOException {
		return new NetCdfChromosomesKeysSource(origin, rdNetCdfFile, NetCDFConstants.Variables.VAR_CHR_IN_MATRIX_IDX);
	}

	@Override
	public ChromosomesKeysSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getChromosomesKeysSource();
			}
		}

		return originSource;
	}

	@Override
	protected KeyFactory<ChromosomeKey> createKeyFactory() {
		return new ChromosomeKeyFactory();
	}
}

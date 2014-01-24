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
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SampleKeyFactory;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.model.StudyKey;
import ucar.nc2.NetcdfFile;

public class NetCdfSamplesKeysSource extends AbstractNetCdfKeysSource<SampleKey> implements SamplesKeysSource {

	private static final int DEFAULT_CHUNK_SIZE = 200;

	private final StudyKey studyKey;

	private NetCdfSamplesKeysSource(StudyKey studyKey, NetcdfFile rdNetCdfFile, String varDimension, String varOriginalIndices, String varKeys) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, varDimension, varOriginalIndices, varKeys);

		this.studyKey = studyKey;
	}

	public static SamplesKeysSource createForMatrix(StudyKey studyKey, NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfSamplesKeysSource(studyKey, rdNetCdfFile, cNetCDF.Dimensions.DIM_SAMPLESET, null, cNetCDF.Variables.VAR_SAMPLE_KEY);
	}

	public static SamplesKeysSource createForOperation(StudyKey studyKey, NetcdfFile rdNetCdfFile, boolean operationSetMarkers) throws IOException {

		final String varDimension;
		final String varOriginalIndices;
		final String varKeys;
		if (operationSetMarkers) {
			varDimension = cNetCDF.Dimensions.DIM_IMPLICITSET;
			varOriginalIndices = cNetCDF.Variables.VAR_IMPLICITSET_IDX;
			varKeys = cNetCDF.Variables.VAR_IMPLICITSET;
		} else {
			varDimension = cNetCDF.Dimensions.DIM_OPSET;
			varOriginalIndices = cNetCDF.Variables.VAR_OPSET_IDX;
			varKeys = cNetCDF.Variables.VAR_OPSET;
		}

		return new NetCdfSamplesKeysSource(studyKey, rdNetCdfFile, varDimension, varOriginalIndices, varKeys);
	}

	@Override
	protected KeyFactory<SampleKey> createKeyFactory() {
		return new SampleKeyFactory(studyKey);
	}
}

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
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SampleKeyFactory;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.model.StudyKey;
import ucar.nc2.NetcdfFile;

public class NetCdfSamplesKeysSource extends AbstractNetCdfKeysSource<SampleKey> implements SamplesKeysSource {

	private static final int DEFAULT_CHUNK_SIZE = 200;

	private final StudyKey studyKey;
	private SamplesKeysSource originSource;

	private NetCdfSamplesKeysSource(MatrixKey origin, StudyKey studyKey, NetcdfFile rdNetCdfFile, String varSamplesDimension, String varOriginalIndices, String varKeys) {
		super(origin, rdNetCdfFile, DEFAULT_CHUNK_SIZE, varSamplesDimension, varOriginalIndices, varKeys);

		this.studyKey = studyKey;
		this.originSource = null;
	}

	public static SamplesKeysSource createForMatrix(MatrixKey origin, StudyKey studyKey, NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfSamplesKeysSource(origin, studyKey, rdNetCdfFile, NetCDFConstants.Dimensions.DIM_SAMPLESET, null, NetCDFConstants.Variables.VAR_SAMPLE_KEY);
	}

	public static SamplesKeysSource createForOperation(MatrixKey origin, StudyKey studyKey, NetcdfFile rdNetCdfFile, boolean operationSetMarkers) throws IOException {

		final String varDimension;
		final String varOriginalIndices;
		final String varKeys;
		if (operationSetMarkers) {
			varDimension = NetCDFConstants.Dimensions.DIM_IMPLICITSET;
			varOriginalIndices = NetCDFConstants.Variables.VAR_IMPLICITSET_IDX;
			varKeys = NetCDFConstants.Variables.VAR_IMPLICITSET;
		} else {
			varDimension = NetCDFConstants.Dimensions.DIM_OPSET;
			varOriginalIndices = NetCDFConstants.Variables.VAR_OPSET_IDX;
			varKeys = NetCDFConstants.Variables.VAR_OPSET;
		}

		return new NetCdfSamplesKeysSource(origin, studyKey, rdNetCdfFile, varDimension, varOriginalIndices, varKeys);
	}

	@Override
	public SamplesKeysSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getSamplesKeysSource();
			}
		}

		return originSource;
	}

	@Override
	protected KeyFactory<SampleKey> createKeyFactory() {
		return new SampleKeyFactory(studyKey);
	}
}

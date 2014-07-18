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

package org.gwaspi.datasource.netcdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.datasource.AbstractListSource;
import org.gwaspi.global.Extractor;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.operations.NetCdfUtils;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;

/**
 * TODO
 * @param <VT> list value type
 */
public abstract class AbstractNetCdfListSource<VT> extends AbstractListSource<VT> {

	private final String varNameDimension;
	private Integer size;
	private final NetcdfFile rdNetCdfFile;

	private AbstractNetCdfListSource(MatrixKey origin, NetcdfFile rdNetCdfFile, int chunkSize, List<Integer> originalIndices, String varNameDimension) {
		super(origin, chunkSize, originalIndices);

		this.varNameDimension = varNameDimension;
		this.size = null;
		this.rdNetCdfFile = rdNetCdfFile;
	}

	AbstractNetCdfListSource(MatrixKey origin, NetcdfFile rdNetCdfFile, int chunkSize, String varNameDimension) {
		this(origin, rdNetCdfFile, chunkSize, null, varNameDimension);
	}

	AbstractNetCdfListSource(MatrixKey origin, NetcdfFile rdNetCdfFile, int chunkSize, String varNameDimension, List<Integer> originalIndices) {
		this(origin, rdNetCdfFile, chunkSize, originalIndices, varNameDimension);
	}

	protected NetcdfFile getReadNetCdfFile() {
		return rdNetCdfFile;
	}

	protected <LVT> List<LVT> readVar(String varName, int from, int to) throws IOException {

		List<LVT> values = new ArrayList<LVT>(0);
		NetCdfUtils.readVariable(rdNetCdfFile, varName, from, to, values, null);
		return values;
	}

	protected <ST, LVT> List<LVT> readVar(String varName, Extractor<ST, LVT> storageToFinalValueExtractor, int from, int to) throws IOException {

		List<ST> storageValues = new ArrayList<ST>(0);
		NetCdfUtils.readVariable(rdNetCdfFile, varName, from, to, storageValues, null);

		List<LVT> values = new ArrayList<LVT>(storageValues.size());
		for (ST st : storageValues) {
			values.add(storageToFinalValueExtractor.extract(st));
		}

		return values;
	}

	@Override
	public int sizeInternal() {

		if (size == null) {
			Dimension dim = rdNetCdfFile.findDimension(varNameDimension);
			size =  dim.getLength();
		}

		return size;
	}
}

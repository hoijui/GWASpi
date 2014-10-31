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

package org.gwaspi.datasource;

import java.io.IOException;
import java.util.List;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;

/**
 * TODO add class description
 * @param <VT> list value type
 */
public abstract class AbstractListSource<VT> extends AbstractOrigIndicesFilteredChunkedListSource<VT> {

	private final MatrixKey origin;
	private DataSetSource originDataSetSource;

	protected AbstractListSource(MatrixKey origin, int chunkSize, List<Integer> originalIndices) {
		super(chunkSize, originalIndices);

		this.origin = origin;
	}

	protected AbstractListSource(MatrixKey origin, int chunkSize) {
		this(origin, chunkSize, null);
	}

	protected MatrixKey getOrigin() {
		return origin;
	}

	public DataSetSource getOrigDataSetSource() throws IOException {

		if (originDataSetSource == null) {
			originDataSetSource = MatrixFactory.generateMatrixDataSetSource(origin);
		}

		return originDataSetSource;
	}
}

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

package org.gwaspi.datasource.inmemory;

import java.io.IOException;
import java.util.List;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomesInfosSource;
import org.gwaspi.model.MatrixKey;

public class InMemoryChromosomesInfosSource extends AbstractInMemoryListSource<ChromosomeInfo> implements ChromosomesInfosSource {

	private ChromosomesInfosSource originSource;

	private InMemoryChromosomesInfosSource(MatrixKey origin, final List<ChromosomeInfo> items) {
		super(origin, items);
	}

	private InMemoryChromosomesInfosSource(MatrixKey origin, final List<ChromosomeInfo> items, List<Integer> originalIndices) {
		super(origin, items, originalIndices);
	}

	public static ChromosomesInfosSource createForMatrix(MatrixKey origin, final List<ChromosomeInfo> items) throws IOException {
		return new InMemoryChromosomesInfosSource(origin, items);
	}

	public static ChromosomesInfosSource createForOperation(MatrixKey origin, final List<ChromosomeInfo> items, List<Integer> originalIndices) throws IOException {
		return new InMemoryChromosomesInfosSource(origin, items, originalIndices);
	}

	// XXX same code as in the NetCDF counterpart!
	@Override
	public ChromosomesInfosSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getChromosomesInfosSource();
			}
		}

		return originSource;
	}
}

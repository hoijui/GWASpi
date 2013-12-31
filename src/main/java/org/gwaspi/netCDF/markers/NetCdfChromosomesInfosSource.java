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

package org.gwaspi.netCDF.markers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomesInfosSource;
import ucar.nc2.NetcdfFile;

public class NetCdfChromosomesInfosSource extends AbstractListSource<ChromosomeInfo> implements ChromosomesInfosSource {

	/**
	 * As we have max 23 chromosomes, in general,
	 * this constant does not really matter;
	 * though it should be at least 23.
	 */
	private static final int DEFAULT_CHUNK_SIZE = 100;
	private static final int DEFAULT_CHUNK_SIZE_SHATTERED = 1;

	private NetCdfChromosomesInfosSource(NetcdfFile rdNetCdfFile) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_CHRSET);
	}

	private NetCdfChromosomesInfosSource(NetcdfFile rdNetCdfFile, List<Integer> originalIndices) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE_SHATTERED, originalIndices);
	}

	public static ChromosomesInfosSource createForMatrix(NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfChromosomesInfosSource(rdNetCdfFile);
	}

	public static ChromosomesInfosSource createForOperation(NetcdfFile rdNetCdfFile, List<Integer> originalIndices) throws IOException {
		return new NetCdfChromosomesInfosSource(rdNetCdfFile, originalIndices);
	}

	@Override
	public List<ChromosomeInfo> getRange(int from, int to) throws IOException {

		List<ChromosomeInfo> chromosomes;

		List<int[]> chromosomeInfosRaw = readVar(cNetCDF.Variables.VAR_CHR_INFO, from, to);

		chromosomes = new ArrayList<ChromosomeInfo>(chromosomeInfosRaw.size());
		for (int[] infoRaw : chromosomeInfosRaw) {
			chromosomes.add(new ChromosomeInfo(
					infoRaw[0],
					infoRaw[1],
					infoRaw[2],
					infoRaw[3]));
		}

		return chromosomes;
	}
}
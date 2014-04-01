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
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.CompactGenotypesList;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.GenotypesListFactory;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MatrixKey;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCdfMarkersGenotypesSource extends AbstractNetCdfListSource<GenotypesList> implements MarkersGenotypesSource {

	private final GenotypesListFactory genotypesListFactory;
	private MarkersGenotypesSource originSource;

	private static final int DEFAULT_CHUNK_SIZE = 200;
	private static final int DEFAULT_CHUNK_SIZE_SHATTERED = 1;
	/** Whether to read all markers GTs in the range at once, or read each one separately */
	public static boolean READ_GTS_IN_BULK = false;

	private NetCdfMarkersGenotypesSource(MatrixKey origin, NetcdfFile rdNetCdfFile) {
		super(origin, rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_MARKERSET);

		this.genotypesListFactory = CompactGenotypesList.FACTORY;
	}

	public static MarkersGenotypesSource createForMatrix(MatrixKey origin, NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfMarkersGenotypesSource(origin, rdNetCdfFile);
	}

	@Override
	public MarkersGenotypesSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getMarkersGenotypesSource();
			}
		}

		return originSource;
	}

	@Override
	public List<GenotypesList> getRange(int from, int to) throws IOException {
		return readMarkerGTs(getReadNetCdfFile(), cNetCDF.Variables.VAR_GENOTYPES, from, to, genotypesListFactory, READ_GTS_IN_BULK);
	}

	public static List<GenotypesList> readMarkerGTs(NetcdfFile rdNetCdf, String netCdfVarName, int fromMarkerIndex, int toMarkerIndex, final GenotypesListFactory genotypesListFactory, final boolean readInBulk) throws IOException {

		Variable var = rdNetCdf.findVariable(netCdfVarName);

		if (var == null) {
			throw new IOException("Variable " + netCdfVarName + " not found in NetCdf file " + rdNetCdf.getLocation());
		}

		if (readInBulk) {
			return NetCdfSamplesGenotypesSource.readMarkerGTLists(var, -1, -1, fromMarkerIndex, toMarkerIndex, genotypesListFactory);
		} else {
			List<GenotypesList> values = new ArrayList<GenotypesList>(toMarkerIndex - fromMarkerIndex + 1);

			for (int mi = fromMarkerIndex; mi <= toMarkerIndex; mi++) {
				List<byte[]> sampleGTs = NetCdfSamplesGenotypesSource.readMarkerGTs(var, -1, -1, mi);
				GenotypesList genotypesList = genotypesListFactory.extract(sampleGTs);
				values.add(genotypesList);
			}

			return values;
		}
	}
}

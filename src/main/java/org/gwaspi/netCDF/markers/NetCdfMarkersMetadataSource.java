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
import java.util.Iterator;
import java.util.List;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersMetadataSource;
import ucar.nc2.NetcdfFile;

public class NetCdfMarkersMetadataSource extends AbstractNetCdfListSource<MarkerMetadata> implements MarkersMetadataSource {

	private static final int DEFAULT_CHUNK_SIZE = 50;
	private static final int DEFAULT_CHUNK_SIZE_SHATTERED = 1;

	private NetCdfMarkersMetadataSource(NetcdfFile rdNetCdfFile) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_MARKERSET);
	}

	private NetCdfMarkersMetadataSource(NetcdfFile rdNetCdfFile, List<Integer> originalIndices) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE_SHATTERED, originalIndices);
	}

	public static MarkersMetadataSource createForMatrix(NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfMarkersMetadataSource(rdNetCdfFile);
	}

	public static MarkersMetadataSource createForOperation(NetcdfFile rdNetCdfFile, List<Integer> originalIndices) throws IOException {
		return new NetCdfMarkersMetadataSource(rdNetCdfFile, originalIndices);
	}

	@Override
	public List<MarkerMetadata> getRange(int from, int to) throws IOException {

		List<MarkerMetadata> values = new ArrayList<MarkerMetadata>(to - from);

		List<String> markerIdsIt = getMarkerIds(from, to);
		Iterator<String> rsIdsIt = getRsIds(from, to).iterator();
		Iterator<String> chromosomesIt = getChromosomes(from, to).iterator();
		Iterator<Integer> positionsIt = getPositions(from, to).iterator();
		Iterator<String> allelesIt = getAlleles(from, to).iterator();
		Iterator<String> strandsIt = getStrands(from, to).iterator();
		for (String markerId : markerIdsIt) {
			values.add(new MarkerMetadata(
					markerId,
					rsIdsIt.next(),
					chromosomesIt.next(),
					positionsIt.next(),
					allelesIt.next(),
					strandsIt.next()
			));
		}

		return values;
	}

	@Override
	public List<String> getMarkerIds() throws IOException {
		return getMarkerIds(-1, -1);
	}

	@Override
	public List<String> getRsIds() throws IOException {
		return getRsIds(-1, -1);
	}

	@Override
	public List<String> getChromosomes() throws IOException {
		return getChromosomes(-1, -1);
	}

	@Override
	public List<Integer> getPositions() throws IOException {
		return getPositions(-1, -1);
	}

	@Override
	public List<String> getAlleles() throws IOException {
		return getAlleles(-1, -1);
	}

	@Override
	public List<String> getStrands() throws IOException {
		return getStrands(-1, -1);
	}

	@Override
	public List<String> getMarkerIds(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_MARKERSET, from, to);
	}

	@Override
	public List<String> getRsIds(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_MARKERS_RSID, from, to);
	}

	@Override
	public List<String> getChromosomes(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_MARKERS_CHR, from, to);
	}

	@Override
	public List<Integer> getPositions(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_MARKERS_POS, from, to);
	}

	@Override
	public List<String> getAlleles(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, from, to);
	}

	@Override
	public List<String> getStrands(int from, int to) throws IOException {
		return readVar(cNetCDF.Variables.VAR_GT_STRAND, from, to);
	}
}

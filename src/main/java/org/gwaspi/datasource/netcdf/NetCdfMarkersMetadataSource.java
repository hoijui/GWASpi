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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import ucar.nc2.NetcdfFile;

public class NetCdfMarkersMetadataSource extends AbstractNetCdfListSource<MarkerMetadata> implements MarkersMetadataSource {

	private static final int DEFAULT_CHUNK_SIZE = 50;
	private static final int DEFAULT_CHUNK_SIZE_SHATTERED = 1;

	private final MatrixKey origin;
	private DataSetSource originDataSetSource;
	private MarkersMetadataSource originSource;

	private NetCdfMarkersMetadataSource(MatrixKey origin, NetcdfFile rdNetCdfFile) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_MARKERSET);

		this.origin = origin;
		this.originDataSetSource = null;
		this.originSource = null;
	}

	private NetCdfMarkersMetadataSource(MatrixKey origin, NetcdfFile rdNetCdfFile, List<Integer> originalIndices) {
		super(rdNetCdfFile, DEFAULT_CHUNK_SIZE_SHATTERED, originalIndices);

		this.origin = origin;
		this.originDataSetSource = null;
		this.originSource = null;
	}
	private DataSetSource getDataSetSource() throws IOException {

		if (originDataSetSource == null) {
			originDataSetSource = MatrixFactory.generateMatrixDataSetSource(origin);
		}

		return originDataSetSource;
	}

	private DataSetSource getOrigDataSetSource() throws IOException {

		if (originDataSetSource == null) {
			originDataSetSource = MatrixFactory.generateMatrixDataSetSource(origin);
		}

		return originDataSetSource;
	}

	private MarkersMetadataSource getOrigSource() throws IOException {

		if (originSource == null) {
			originSource = getOrigDataSetSource().getMarkersMetadatasSource();
		}

		return originSource;
	}

	public static MarkersMetadataSource createForMatrix(NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfMarkersMetadataSource(null, rdNetCdfFile);
	}

	public static MarkersMetadataSource createForOperation(MatrixKey origin, NetcdfFile rdNetCdfFile, List<Integer> originalIndices) throws IOException {
		return new NetCdfMarkersMetadataSource(origin, rdNetCdfFile, originalIndices);
	}

	@Override
	public MarkersKeysSource getKeysSource() throws IOException {
		return getDataSetSource().getMarkersKeysSource();
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

//	@Override
	public List<String> getMarkerIds(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all marker info attributes
			return readVar(cNetCDF.Variables.VAR_MARKERSET, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final MarkersMetadataSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getMarkersKeysSource().getIndices();
			final List<String> allOriginMarkerIds = origSource.getMarkerIds();
			final List<String> localMarkerIds = extractValuesByOrigIndices(allOriginIndices, allOriginMarkerIds, toExtractSampleOrigIndices);
			return localMarkerIds;
		}
	}

//	@Override
	public List<String> getRsIds(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all marker info attributes
			return readVar(cNetCDF.Variables.VAR_MARKERS_RSID, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final MarkersMetadataSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getMarkersKeysSource().getIndices();
			final List<String> allOriginRsIds = origSource.getRsIds();
			final List<String> localRsIds = extractValuesByOrigIndices(allOriginIndices, allOriginRsIds, toExtractSampleOrigIndices);
			return localRsIds;
		}
	}

//	@Override
	public List<String> getChromosomes(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all marker info attributes
			return readVar(cNetCDF.Variables.VAR_MARKERS_CHR, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final MarkersMetadataSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getMarkersKeysSource().getIndices();
			final List<String> allOriginChromosomes = origSource.getChromosomes();
			final List<String> localChromosomes = extractValuesByOrigIndices(allOriginIndices, allOriginChromosomes, toExtractSampleOrigIndices);
			return localChromosomes;
		}
	}

//	@Override
	public List<Integer> getPositions(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all marker info attributes
			return readVar(cNetCDF.Variables.VAR_MARKERS_POS, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final MarkersMetadataSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getMarkersKeysSource().getIndices();
			final List<Integer> allOriginPositions = origSource.getPositions();
			final List<Integer> localPositions = extractValuesByOrigIndices(allOriginIndices, allOriginPositions, toExtractSampleOrigIndices);
			return localPositions;
		}
	}

//	@Override
	public List<String> getAlleles(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all marker info attributes
			return readVar(cNetCDF.Variables.VAR_MARKERS_BASES_DICT, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final MarkersMetadataSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getMarkersKeysSource().getIndices();
			final List<String> allOriginAlleles = origSource.getAlleles();
			final List<String> localAlleles = extractValuesByOrigIndices(allOriginIndices, allOriginAlleles, toExtractSampleOrigIndices);
			return localAlleles;
		}
	}

//	@Override
	public List<String> getStrands(int from, int to) throws IOException {

		if (origin == null) {
			// we are the origin
			// we have direct storage of all marker info attributes
			return readVar(cNetCDF.Variables.VAR_GT_STRAND, from, to);
		} else {
			// we do not have direct storage, thus we extract it from the origin
			final List<Integer> toExtractSampleOrigIndices = getKeysSource().getIndices(from, to);
			final MarkersMetadataSource origSource = getOrigSource();
			final List<Integer> allOriginIndices = getOrigDataSetSource().getMarkersKeysSource().getIndices();
			final List<String> allOriginStrands = origSource.getStrands();
			final List<String> localStrands = extractValuesByOrigIndices(allOriginIndices, allOriginStrands, toExtractSampleOrigIndices);
			return localStrands;
		}
	}
}

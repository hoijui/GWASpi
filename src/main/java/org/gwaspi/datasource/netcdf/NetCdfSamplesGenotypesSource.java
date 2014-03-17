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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.CompactGenotypesList;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.GenotypesListFactory;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * This modification of the MarkerSet can create instances of the MarkerSetMap.
 * The markerSet object has several methods to initialize it's Map,
 * which can later be filled with it's corresponding values.
 * The Map is not copied or passed as a parameter to the fillers.
 *
 * The matrix netCDF file is opened at creation of the MarkerSet and closed
 * at finalization of the class. No need to pass a netCDF handler anymore.
 */
public class NetCdfSamplesGenotypesSource extends AbstractNetCdfListSource<GenotypesList> implements SamplesGenotypesSource {

	private static final Logger log
			= LoggerFactory.getLogger(NetCdfSamplesGenotypesSource.class);

	private final GenotypesListFactory genotyesListFactory;
	private SamplesGenotypesSource originSource;

	private static final int DEFAULT_CHUNK_SIZE = 50;
	private static final int DEFAULT_CHUNK_SIZE_SHATTERED = 1;

	private NetCdfSamplesGenotypesSource(MatrixKey origin, NetcdfFile rdNetCdfFile) {
		super(origin, rdNetCdfFile, DEFAULT_CHUNK_SIZE, cNetCDF.Dimensions.DIM_SAMPLESET);

		this.genotyesListFactory = CompactGenotypesList.FACTORY;
		this.originSource = null;
	}

	public static SamplesGenotypesSource createForMatrix(MatrixKey origin, NetcdfFile rdNetCdfFile) throws IOException {
		return new NetCdfSamplesGenotypesSource(origin, rdNetCdfFile);
	}

	@Override
	public SamplesGenotypesSource getOrigSource() throws IOException {

		if (originSource == null) {
			if (getOrigin() == null) {
				originSource = this;
			} else {
				originSource = getOrigDataSetSource().getSamplesGenotypesSource();
			}
		}

		return originSource;
	}

	@Override
	public List<GenotypesList> getRange(int from, int to) throws IOException {
		return readSampleGTs(getReadNetCdfFile(), cNetCDF.Variables.VAR_GENOTYPES, from, to);
	}

	private List<GenotypesList> readSampleGTs(NetcdfFile rdNetCdf, String netCdfVarName, int fromSampleIndex, int toSampleIndex) throws IOException {

		Variable var = rdNetCdf.findVariable(netCdfVarName);

		if (var != null) {
			List<GenotypesList> values = new ArrayList<GenotypesList>(toSampleIndex - fromSampleIndex + 1);

			for (int si = fromSampleIndex; si <= toSampleIndex; si++) {
				List<byte[]> markerGTs = readSampleGTs(var, si, -1, -1);
				GenotypesList genotypesList = genotyesListFactory.extract(markerGTs);
				values.add(genotypesList);
			}

			return values;
		} else {
			throw new IOException("Variable " + netCdfVarName + " not found in NetCdf file " + rdNetCdf.getLocation());
		}
	}

	private static String buildNetCdfReadString(int fromSampleIndex, int toSampleIndex, int fromMarkerIndex, int toMarkerIndex, int numGTs) {

		StringBuilder netCdfReadStr = new StringBuilder(64);

		netCdfReadStr
				.append("(")
				.append(fromSampleIndex)
				.append(":")
				.append(toSampleIndex)
				.append(":1, ")
				.append(fromMarkerIndex)
				.append(":")
				.append(toMarkerIndex)
				.append(":1, " + "0:")
				.append(numGTs - 1)
				.append(":1)");

		return netCdfReadStr.toString();
	}

	static List<byte[]> readSampleGTs(Variable netCdfGTsVar, int sampleIndex, int fromMarkerIndex, int toMarkerIndex) throws IOException {

		final int[] varShape = netCdfGTsVar.getShape();

		if (fromMarkerIndex == -1) {
			fromMarkerIndex = 0;
		}
		if (toMarkerIndex == -1) {
			toMarkerIndex = varShape[1] - 1;
		}

		try {
			String netCdfReadStr = buildNetCdfReadString(sampleIndex, sampleIndex, fromMarkerIndex, toMarkerIndex, varShape[2]);
			ArrayByte.D3 sampleMarkerGTs = (ArrayByte.D3) netCdfGTsVar.read(netCdfReadStr);
			ArrayByte.D2 markerGTs = (ArrayByte.D2) sampleMarkerGTs.reduce(0);
			List<byte[]> rawList = NetCdfUtils.writeD2ArrayByteToList(markerGTs);

			return rawList;
		} catch (InvalidRangeException ex) {
			throw new IOException("Cannot read data", ex);
		}
	}

	static List<byte[]> readMarkerGTs(Variable netCdfGTsVar, int fromSampleIndex, int toSampleIndex, int markerIndex) throws IOException {

		final int[] varShape = netCdfGTsVar.getShape();

		if (fromSampleIndex == -1) {
			fromSampleIndex = 0;
		}
		if (toSampleIndex == -1) {
			toSampleIndex = varShape[0] - 1;
		}

		try {
			String netCdfReadStr = buildNetCdfReadString(fromSampleIndex, toSampleIndex, markerIndex, markerIndex, varShape[2]);
			ArrayByte.D3 sampleMarkerGTs = (ArrayByte.D3) netCdfGTsVar.read(netCdfReadStr);
			ArrayByte.D2 sampleGTs = (ArrayByte.D2) sampleMarkerGTs.reduce(1);
			List<byte[]> rawList = NetCdfUtils.writeD2ArrayByteToList(sampleGTs);

			return rawList;
		} catch (InvalidRangeException ex) {
			throw new IOException("Cannot read data", ex);
		}
	}

	static void readSampleGTs(ArrayByte.D3 from, Collection<GenotypesList> to, GenotypesListFactory genotyesListFactory) throws IOException {

		try {
			int[] shp = from.getShape();
			List<Range> ranges = new ArrayList<Range>(3);
			ranges.add(new Range(0, 0));
			ranges.add(new Range(0, shp[1] - 1));
			ranges.add(new Range(0, shp[2] - 1));
			for (int r0 = 0; r0 < shp[0]; r0++) {
				ranges.set(0, new Range(r0, r0));
				ArrayByte.D2 gt_ACD2 = (ArrayByte.D2) from.section(ranges);
				List<byte[]> rawList = NetCdfUtils.writeD2ArrayByteToList(gt_ACD2);
				GenotypesList innerList = genotyesListFactory.extract(rawList);
				to.add(innerList);
			}
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}
	}

	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(Map<String, IV> markerIdAlleles) {
		Map<MarkerKey, IV> reparsedData = new LinkedHashMap<MarkerKey, IV>();
		for (Map.Entry<String, IV> entry : markerIdAlleles.entrySet()) {
			reparsedData.put(MarkerKey.valueOf(entry.getKey()), entry.getValue());
		}
		return reparsedData;
	}

	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(ArrayByte.D2 markersAC) {
		Map<String, IV> markerIdAlleles = NetCdfUtils.writeD2ArrayByteToMapKeys(markersAC);
		return wrapToMarkerKeyMap(markerIdAlleles);
	}

	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(ArrayChar.D2 markersAC, IV commonValue) {
		Map<String, IV> markerIdAlleles = NetCdfUtils.writeD2ArrayCharToMapKeys(markersAC, commonValue);
		return wrapToMarkerKeyMap(markerIdAlleles);
	}

	private static <IV> Map<MarkerKey, IV> wrapToMarkerKeyMap(ArrayChar.D2 markersAC) {
		return wrapToMarkerKeyMap(markersAC, null);
	}

	private static <IV> Map<ChromosomeKey, IV> wrapToChromosomeKeyMap(Map<String, IV> chromosomeInfos) {
		Map<ChromosomeKey, IV> reparsedData = new LinkedHashMap<ChromosomeKey, IV>();
		for (Map.Entry<String, IV> entry : chromosomeInfos.entrySet()) {
			reparsedData.put(ChromosomeKey.valueOf(entry.getKey()), entry.getValue());
		}
		return reparsedData;
	}

	private static <IV> Map<ChromosomeKey, IV> wrapToChromosomeKeyMap(ArrayChar.D2 markersAC, IV commonValue) {
		Map<String, IV> markerIdAlleles = NetCdfUtils.writeD2ArrayCharToMapKeys(markersAC, commonValue);
		return wrapToChromosomeKeyMap(markerIdAlleles);
	}

	private static <IV> Map<ChromosomeKey, IV> wrapToChromosomeKeyMap(ArrayChar.D2 markersAC) {
		return wrapToChromosomeKeyMap(markersAC, null);
	}
}

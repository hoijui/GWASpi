///*
// * Copyright (C) 2013 Universitat Pompeu Fabra
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package org.gwaspi.netCDF.markers;
//
//import java.io.IOException;
//import java.util.AbstractMap;
//import java.util.AbstractSet;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import org.gwaspi.constants.cNetCDF;
//import org.gwaspi.model.GenotypesList;
//import org.gwaspi.model.MarkerKey;
//import org.gwaspi.model.MarkersGenotypesSource;
//import org.gwaspi.model.MatrixKey;
//import org.gwaspi.model.OperationKey;
//import org.gwaspi.model.OperationMetadata;
//import org.gwaspi.model.OperationsList;
//import org.gwaspi.model.SampleInfo;
//import org.gwaspi.model.SampleInfoList;
//import org.gwaspi.model.SampleKey;
//import org.gwaspi.netCDF.operations.MarkerOperationSet;
//import org.gwaspi.samples.SampleSet;
//import ucar.ma2.InvalidRangeException;
//import ucar.nc2.NetcdfFile;
//
///**
// * TODO
// * NOTE Probably not needed/required?
// */
//public class NetCDFMarkersGenotypesSource extends AbstractMap<MarkerKey, GenotypesList> implements MarkersGenotypesSource
//{
//	public NetCDFMarkersGenotypesSource(MatrixKey matrixKey, Excluder<MarkerKey> excluder) throws IOException {
//
//		this.matrixKey = matrixKey;
//		this.excluder = excluder;
//
//		if (excluder != null) {
//			excluder.init();
//		}
//
//		MarkerSet rdMarkerSet = new MarkerSet(matrixKey);
//		rdMarkerSet.initFullMarkerIdSetMap();
////		rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(readStudyId);
////		rdMarkerSet.fillWith(cNetCDF.Defaults.DEFAULT_GT);
////
////		Map<MarkerKey, byte[]> wrMarkerSetMap = new LinkedHashMap<MarkerKey, byte[]>();
////		wrMarkerSetMap.putAll(rdMarkerSet.getMarkerIdSetMapByteArray());
//
//		this.sampleSet = new SampleSet(matrixKey);
////			samples = sampleSet.getSampleIdSetMapByteArray();
//		this.sampleKeys = sampleSet.getSampleKeys();
//		// This one has to be ordered! (and it is, due to the map being a LinkedHashMap)
//		this.markerKeys = new ArrayList<MarkerKey>(rdMarkerSet.getMarkerIdSetMapInteger().keySet());
//
//		this.sampleInfos = retrieveSampleInfos();
//	}
//
//	public NetCDFMarkersGenotypesSource(MatrixKey matrixKey) throws IOException, InvalidRangeException {
//		this(matrixKey, null);
//	}
//
//	@Override
//	public Set<Entry<MarkerKey, GenotypesList>> entrySet() {
//		throw new UnsupportedOperationException("Not supported yet."); XXX;
//	}
//}

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

package org.gwaspi.reports;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
//import ucar.nc2.NetcdfFile;

public class GatherQAMarkersData {

	private GatherQAMarkersData() {
	}

	public static Map<MarkerKey, Double> loadMarkerQAMissingRatio(OperationKey operationKey) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(operationKey);
		Map<MarkerKey, Double> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}

	public static Map<MarkerKey, Integer> loadMarkerQAMismatchState(OperationKey operationKey) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(operationKey);
		Map<MarkerKey, Integer> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}

	public static Map<MarkerKey, byte[]> loadMarkerQAMinorAlleles(OperationKey opKey) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperation(opKey);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(opKey);
		Map<MarkerKey, byte[]> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}

	public static Map<MarkerKey, byte[]> loadMarkerQAMajorAlleles(OperationKey operationKey) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(operationKey);
		Map<MarkerKey, byte[]> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}

	public static Map<MarkerKey, Double> loadMarkerQAMinorAlleleFrequency(OperationKey operationKey) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(operationKey);
		Map<MarkerKey, Double> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}

	public static Map<MarkerKey, Double> loadMarkerQAMajorAlleleFrequency(OperationKey operationKey) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(operationKey);
		Map<MarkerKey, Double> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile markerQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(markerQANcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);

		markerQANcFile.close();
		return rdMatrixMarkerSetMap;
	}
}

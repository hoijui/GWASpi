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
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import ucar.nc2.NetcdfFile;

public class GatherHardyWeinbergData {

	private GatherHardyWeinbergData() {
	}

	public static Map<MarkerKey, Double> loadHWPval_ALT(int opId) throws IOException {
		return loadHWVar(opId, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT);
	}

	public static Map<MarkerKey, Double> loadHWPval_ALL(int opId) throws IOException {
		return loadHWVar(opId, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL);
	}

	public static Map<MarkerKey, Double> loadHWPval_CASE(int opId) throws IOException {
		return loadHWVar(opId, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE);
	}

	public static Map<MarkerKey, Double> loadHWPval_CTRL(int opId) throws IOException {
		return loadHWVar(opId, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
	}

	public static Map<MarkerKey, Double> loadHWHETZY_ALT(int opId) throws IOException {
		return loadHWVar(opId, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT);
	}

	public static Map<MarkerKey, Double> loadHWHETZY_ALL(int opId) throws IOException {
		return loadHWVar(opId, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALL);
	}

	public static Map<MarkerKey, Double> loadHWHETZY_CASE(int opId) throws IOException {
		return loadHWVar(opId, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CASE);
	}

	public static Map<MarkerKey, Double> loadHWHETZY_CTRL(int opId) throws IOException {
		return loadHWVar(opId, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL);
	}

	private static Map<MarkerKey, Double> loadHWVar(int opId, String variableName) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);

		MarkerOperationSet rdInfoMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
		Map<MarkerKey, Double> rdMatrixMarkerSetMap = rdInfoMarkerSet.getOpSetMap();

		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixMarkerSetMap = rdInfoMarkerSet.fillOpSetMapWithVariable(assocNcFile, variableName);

		assocNcFile.close();
		return rdMatrixMarkerSetMap;
	}
}

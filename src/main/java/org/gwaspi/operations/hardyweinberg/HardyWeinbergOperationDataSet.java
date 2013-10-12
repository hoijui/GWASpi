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

package org.gwaspi.operations.hardyweinberg;

import java.io.IOException;
import java.util.Collection;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.OperationDataSet;

public interface HardyWeinbergOperationDataSet extends OperationDataSet<HardyWeinbergOperationEntry> {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL: Control P-Value [Double[1]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL: Control Obs Hetzy & Exp Hetzy [Double[2]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT: Hardy-Weinberg Alternate P-Value [Double[1]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT: Hardy-Weinberg Alternate Obs Hetzy & Exp Hetzy [Double[2]]

//	/**
//	 * @param pValue P-Value
//	 *   NetCDF variables:
//	 *   - HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL
//	 *   - HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT
//	 * @param obsHzy Hardy-Weinberg Obs Hetzy
//	 *   NetCDF variables:
//	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL [0] Control
//	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT  [0] Alternate
//	 * @param expHzy Hardy-Weinberg Exp Hetzy
//	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL [1] Control
//	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT  [1] Alternate
//	 * @param control whether the to be added marker values denote a control
//	 *   or an alternate entry
//	 */
//	void addMarkerHWValues(double pValue, double obsHzy, double expHzy, boolean control) throws IOException;
	void addEntry(HardyWeinbergOperationEntry entry) throws IOException;

	void setHardyWeinbergName(String hwName);

	void setMarkerCensusOperationKey(OperationKey markerCensusOPKey);

	Collection<HardyWeinbergOperationEntry> getEntriesControl() throws IOException;

	Collection<Double> getPs(HardyWeinbergOperationEntry.Category category, int from, int to) throws IOException;
	Collection<Double> getHwHetzyObses(HardyWeinbergOperationEntry.Category category, int from, int to) throws IOException;
	Collection<Double> getHwHetzyExps(HardyWeinbergOperationEntry.Category category, int from, int to) throws IOException;
}

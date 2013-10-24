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

import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.OperationDataEntry;

public interface HardyWeinbergOperationEntry extends OperationDataEntry<MarkerKey> {

	/**
	 * @return P-Value
	 *   NetCDF variables:
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT
	 * @see #isControl()
	 */
	double getP();

	/**
	 * @return Obs Hetzy
	 *   NetCDF variables:
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL [0] Control
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT  [0] Alternate
	 * @see #isControl()
	 */
	double getObsHzy();

	/**
	 * @return Hardy-Weinberg Exp Hetzy
	 *   NetCDF variables:
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL [1] Control
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT  [1] Alternate
	 * @see #isControl()
	 */
	double getExpHzy();

	/**
	 * @return whether this marker values denote a control
	 *   or an alternate entry
	 */
	boolean isControl();
}

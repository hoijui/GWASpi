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

package org.gwaspi.operations.hardyweinberg;

import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractOperationParams;

public class ByHardyWeinbergThresholdFilterOperationParams extends AbstractOperationParams {

	private final OperationKey hardyWeinbergOperationKey;
	private final double hardyWeinbergPValueThreshold;

	/**
	 * @param parent
	 * @param name
	 * @param hardyWeinbergOperationKey This operation should be the parent,
	 *   or one of its ancestors, to make sure we have a hardy&weinberg entry
	 *   for every entry in the parent
	 * @param hardyWeinbergPValueThreshold
	 */
	public ByHardyWeinbergThresholdFilterOperationParams(OperationKey parent, String name, OperationKey hardyWeinbergOperationKey, double hardyWeinbergPValueThreshold) {
		super(OPType.FILTER_BY_HW_THREASHOLD, new DataSetKey(parent), name);

		this.hardyWeinbergOperationKey = hardyWeinbergOperationKey;
		this.hardyWeinbergPValueThreshold = hardyWeinbergPValueThreshold;
	}

	@Override
	protected String getNameDefault() {
		return "Exclude by H&W P-val";
	}

	public OperationKey getHardyWeinbergOperationKey() {
		return hardyWeinbergOperationKey;
	}

	public double getHardyWeinbergPValueThreshold() {
		return hardyWeinbergPValueThreshold;
	}
}

/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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

package org.gwaspi.formats;

import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.operations.AbstractOperationParams;

public class ReducingFilterOperationParams extends AbstractOperationParams {

	private final double remainingFraction;

	public ReducingFilterOperationParams(
			final DataSetKey parent,
			final String name,
			final double remainingFraction)
	{
		super(OPType.FILTER_BY_VALID_AFFECTION, parent, name);

		this.remainingFraction = remainingFraction;
	}

	@Override
	protected String getNameDefault() {
		return "Reduce #samples filter";
	}

	public double getRemainingFraction() {
		return remainingFraction;
	}
}

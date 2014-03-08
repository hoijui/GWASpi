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

package org.gwaspi.gui.utils;

public class AbsolutePercentageModel {

	private final Number initialValue;
	private final Number defaultValue;
	/**
	 * 100%
	 */
	private final Number totalValue;
	private final Number minimumValue;
	private final Number stepSize;
	private final Number maximumValue;

	public AbsolutePercentageModel(
			final Number initialValue,
			final Number defaultValue,
			final Number totalValue,
			final Number minimumValue,
			final Number stepSize,
			final Number maximumValue)
	{
		this.initialValue = initialValue;
		this.defaultValue = defaultValue;
		this.totalValue = totalValue;
		this.minimumValue = minimumValue;
		this.stepSize = stepSize;
		this.maximumValue = maximumValue;
	}

	public Number getInitialValue() {
		return initialValue;
	}

	public Number getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return 100%
	 */
	public Number getTotalValue() {
		return totalValue;
	}

	public Number getMinimumValue() {
		return minimumValue;
	}

	public Number getStepSize() {
		return stepSize;
	}

	public Number getMaximumValue() {
		return maximumValue;
	}
}

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

package org.gwaspi.operations.trendtest;

import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.AbstractOperationDataEntry;

public class DefaultTrendTestOperationEntry extends AbstractOperationDataEntry<MarkerKey> implements TrendTestOperationEntry {

	private final double t;
	private final double p;

	public DefaultTrendTestOperationEntry(MarkerKey key, int index, double t, double p) {
		super(key, index);

		this.t = t;
		this.p = p;
	}

	@Override
	public double getT() {
		return t;
	}

	@Override
	public double getP() {
		return p;
	}
}

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
import org.gwaspi.operations.AbstractOperationDataEntry;

public class DefaultHardyWeinbergOperationEntry extends AbstractOperationDataEntry<MarkerKey> implements HardyWeinbergOperationEntry {

	private final double pValue;
	private final double obsHzy;
	private final double expHzy;
	private final Category category;

	public DefaultHardyWeinbergOperationEntry(MarkerKey key, int index, Category category, double pValue, double obsHzy, double expHzy) {
		super(key, index);

		this.category = category;
		this.pValue = pValue;
		this.obsHzy = obsHzy;
		this.expHzy = expHzy;
	}

	@Override
	public Category getCategory() {
		return category;
	}

	@Override
	public double getP() {
		return pValue;
	}

	@Override
	public double getObsHzy() {
		return obsHzy;
	}

	@Override
	public double getExpHzy() {
		return expHzy;
	}
}

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

package org.gwaspi.operations.combi;

import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.operations.AbstractOperationDataEntry;

public class DefaultCombiTestOperationEntry extends AbstractOperationDataEntry<MarkerKey> implements CombiTestOperationEntry {

	private final double missingRatio;
	private final int missingCount;
	private final double hetzyRatio;

	public DefaultCombiTestOperationEntry(SampleKey key, int index, double missingRatio, int missingCount, double hetzyRatio) {
		super(key, index);

		this.missingRatio = missingRatio;
		this.missingCount = missingCount;
		this.hetzyRatio = hetzyRatio;
	}

	@Override
	public double getMissingRatio() {
		return missingRatio;
	}

	@Override
	public int getMissingCount() {
		return missingCount;
	}

	@Override
	public double getHetzyRatio() {
		return hetzyRatio;
	}
}

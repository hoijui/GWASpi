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

package org.gwaspi.operations.genotypicassociationtest;

import org.gwaspi.operations.allelicassociationtest.DefaultAllelicAssociationOperationEntry;
import org.gwaspi.model.MarkerKey;

public class DefaultGenotypicAssociationOperationEntry extends DefaultAllelicAssociationOperationEntry implements GenotypicAssociationTestOperationEntry {

	private final double or2;

	public DefaultGenotypicAssociationOperationEntry(MarkerKey key, int index, double t, double p, double or, double or2) {
		super(key, index, t, p, or);

		this.or2 = or2;
	}

	@Override
	public double getOR2() {
		return or2;
	}
}

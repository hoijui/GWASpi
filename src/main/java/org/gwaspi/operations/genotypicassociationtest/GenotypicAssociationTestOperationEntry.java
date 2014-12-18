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

import org.gwaspi.global.Extractor;
import org.gwaspi.operations.allelicassociationtest.AllelicAssociationTestOperationEntry;

public interface GenotypicAssociationTestOperationEntry extends AllelicAssociationTestOperationEntry {

	final Extractor<GenotypicAssociationTestOperationEntry, Double> TO_OR2
			= new Extractor<GenotypicAssociationTestOperationEntry, Double>()
	{
		@Override
		public Double extract(GenotypicAssociationTestOperationEntry from) {
			return from.getOR2();
		}
	};

	/**
	 * @return the markers OR2 value
	 * NetCDF variable:
	 * - Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR [3]
	 * - Association.VAR_OP_MARKERS_OR2
	 */
	double getOR2();
}

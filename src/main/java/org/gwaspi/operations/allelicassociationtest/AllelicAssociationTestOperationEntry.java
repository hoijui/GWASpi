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

package org.gwaspi.operations.allelicassociationtest;

import org.gwaspi.global.Extractor;
import org.gwaspi.operations.trendtest.TrendTestOperationEntry;

public interface AllelicAssociationTestOperationEntry extends TrendTestOperationEntry {

	Extractor<AllelicAssociationTestOperationEntry, Double> TO_OR
			= new Extractor<AllelicAssociationTestOperationEntry, Double>()
	{
		@Override
		public Double extract(AllelicAssociationTestOperationEntry from) {
			return from.getOR();
		}
	};

	/**
	 * @return the markers OR value
	 * NetCDF variable:
	 * - Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR [2]
	 * - Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR [2]
	 * - Association.VAR_OP_MARKERS_OR
	 */
	double getOR();
}

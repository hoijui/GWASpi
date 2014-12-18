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

import java.io.Serializable;
import java.util.Comparator;
import org.gwaspi.global.Extractor;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.OperationDataEntry;

public interface TrendTestOperationEntry extends OperationDataEntry<MarkerKey> {

	class PValueComparator implements Comparator<TrendTestOperationEntry>, Serializable {

		@Override
		public int compare(TrendTestOperationEntry entry1, TrendTestOperationEntry entry2) {
			return Double.compare(entry1.getP(), entry2.getP());
		}
	}

	Extractor<TrendTestOperationEntry, Double> TO_T
			= new Extractor<TrendTestOperationEntry, Double>()
	{
		@Override
		public Double extract(TrendTestOperationEntry from) {
			return from.getT();
		}
	};

	Extractor<TrendTestOperationEntry, Double> TO_P
			= new Extractor<TrendTestOperationEntry, Double>()
	{
		@Override
		public Double extract(TrendTestOperationEntry from) {
			return from.getP();
		}
	};

	/**
	 * @return the markers T value (also known as: Chi-Square / X^2)
	 * NetCDF variable:
	 * - Association.VAR_OP_MARKERS_ASTrendTestTP [0]
	 * - Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR [0]
	 * - Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR [0]
	 */
	double getT();

	/**
	 * @return the markers P value
	 * NetCDF variable:
	 * - Association.VAR_OP_MARKERS_ASTrendTestTP [1]
	 * - Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR [1]
	 * - Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR [1]
	 */
	double getP();
}

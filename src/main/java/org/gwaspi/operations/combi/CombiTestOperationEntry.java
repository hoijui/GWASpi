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

import java.io.Serializable;
import org.gwaspi.global.Extractor;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.OperationDataEntry;

public interface CombiTestOperationEntry extends OperationDataEntry<MarkerKey> {

	class MissingRatioExtractor implements Extractor<CombiTestOperationEntry, Double>, Serializable
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Double extract(CombiTestOperationEntry from) {
			return from.getWeight();
		}
	};
	Extractor<CombiTestOperationEntry, Double> TO_WEIGHTS = new MissingRatioExtractor();

	/**
	 * @return the (SVM) weight for this marker
	 *   NetCDF variables:
	 *   - {@link Combi.VAR_OP_MARKERS_WEIGHT}
	 */
	double getWeight();
}

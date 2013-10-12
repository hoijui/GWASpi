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

import java.util.Collection;
import org.gwaspi.operations.OperationDataSet;

public interface TrendTestsOperationDataSet extends OperationDataSet {

	/**
	 * @param markerTs
	 *   the T values, one per marker in this operation
	 * NetCDF variable:
	 * - Association.VAR_OP_MARKERS_ASTrendTestTP [0]
	 * - Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR [0]
	 * - Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR [0]
	 */
	void setMarkerTs(Collection<Double> markerTs);

	/**
	 * @param markerPs
	 *   the P values, one per marker in this operation
	 * NetCDF variable:
	 * - Association.VAR_OP_MARKERS_ASTrendTestTP [1]
	 * - Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR [1]
	 * - Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR [1]
	 */
	void setMarkerPs(Collection<Double> markerPs);

	Collection<Double> getMarkerTs();
	Collection<Double> getMarkerPs();
}

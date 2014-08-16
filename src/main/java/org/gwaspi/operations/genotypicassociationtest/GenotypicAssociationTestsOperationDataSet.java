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

import java.io.IOException;
import java.util.List;
import org.gwaspi.operations.trendtest.CommonTestOperationDataSet;

public interface GenotypicAssociationTestsOperationDataSet extends CommonTestOperationDataSet<GenotypicAssociationTestOperationEntry> {

//	/**
//	 * @param markerOR2s
//	 *   the OR 2 values, one per marker in this operation
//	 * NetCDF variable:
//	 * - Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR [3]
//	 * - Association.VAR_OP_MARKERS_OR2
//	 */
//	void setOR2s(List<Double> markerOR2s);

	List<Double> getORs(int from, int to) throws IOException;
	List<Double> getOR2s(int from, int to) throws IOException;
}

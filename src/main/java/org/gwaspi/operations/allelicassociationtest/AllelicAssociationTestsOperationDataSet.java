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

import java.io.IOException;
import java.util.Collection;
import org.gwaspi.operations.OperationDataSet;

public interface AllelicAssociationTestsOperationDataSet extends OperationDataSet<AllelicAssociationTestOperationEntry> {

//	/**
//	 * @param markerORs
//	 *   the OR values, one per marker in this operation
//	 * NetCDF variable:
//	 * - Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR [2]
//	 * - Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR [2]
//	 * - Association.VAR_OP_MARKERS_OR
//	 */
//	void setORs(Collection<Double> markerORs);

	void addEntry(AllelicAssociationTestOperationEntry entry) throws IOException;

	Collection<Double> getTs(int from, int to) throws IOException;
	Collection<Double> getPs(int from, int to) throws IOException;
	Collection<Double> getORs(int from, int to) throws IOException;
}

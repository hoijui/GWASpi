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

package org.gwaspi.threadbox;

import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.operations.MatrixOperation;
import org.gwaspi.netCDF.operations.MergeAllMatrixOperation;
import org.gwaspi.netCDF.operations.MergeMarkersMatrixOperation;

public class Threaded_MergeMatrices extends AbstractThreaded_MergeMatrices {

	/**
	 * Whether to merge all, or only the marked samples
	 * TODO the second part of the previous sentence needs revising
	 */
	private final boolean all;

	public Threaded_MergeMatrices(
			MatrixKey parentMatrixKey1,
			MatrixKey parentMatrixKey2,
			String newMatrixName,
			String description,
			boolean all)
	{
		super(
				parentMatrixKey1,
				parentMatrixKey2,
				newMatrixName,
				description);

		this.all = all;
	}

	@Override
	protected MatrixOperation createMatrixOperation() throws Exception {

		final MatrixOperation joinMatrices;

		if (all) {
			joinMatrices = new MergeAllMatrixOperation(
				parentMatrixKey1,
				parentMatrixKey2,
				newMatrixName,
				description);
		} else {
			joinMatrices = new MergeMarkersMatrixOperation(
				parentMatrixKey1,
				parentMatrixKey2,
				newMatrixName,
				description);
		}

		return joinMatrices;
	}
}

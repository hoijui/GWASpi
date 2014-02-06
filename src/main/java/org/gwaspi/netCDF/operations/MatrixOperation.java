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

package org.gwaspi.netCDF.operations;

import java.io.IOException;

public interface MatrixOperation {

	/**
	 * Whether the operation is valid, given its parameters.
	 * @return true, if the operation seems to be valid,
	 *   and may be able to execute properly; false otherwise
	 * @throws IOException
	 */
	boolean isValid() throws IOException;

	/**
	 * Returns true, if the operation looks to be valid,
	 * and may be able to execute properly.
	 * @return a human readable description of the problem
	 *   with the given parameters if the operation is invalid;
	 *   <code>null</code> otherwise
	 * @see #isValid()
	 */
	String getProblemDescription();

	/**
	 * Returns whether this creates a matrix or an operation as result.
	 * @return <code>true</code> if this creates a matrix,
	 *   <code>true</code> if this creates an operation
	 */
	boolean isCreatingResultMatrix();

	/**
	 * Execute this operation.
	 * @return the resulting matrixes ID if the operation succeeded,
	 *   with the given parameters if the operation is invalid;
	 *   <code>Integer#MIN_VALUE</code> otherwise
	 */
	int processMatrix() throws IOException;
}

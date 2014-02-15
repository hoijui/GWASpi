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

package org.gwaspi.model;

/**
 * Uniquely identifies a matrix or operation,
 * which both may serve as data-set source specifier.
 */
public final class DataSetKey {

	private final MatrixKey matrixParent;
	private final OperationKey operationParent;

	public DataSetKey(final MatrixKey matrixParent) {

		this.matrixParent = matrixParent;
		this.operationParent = null;
	}

	public DataSetKey(final OperationKey operationParent) {

		this.matrixParent = null;
		this.operationParent = operationParent;
	}

	public boolean isMatrix() {
		return (matrixParent != null);
	}

	public boolean isOperation() {
		return (operationParent != null);
	}

	public MatrixKey getOrigin() {

		if (isMatrix()) {
			return getMatrixParent();
		} else {
			return getOperationParent().getParentMatrixKey();
		}
	}

	public MatrixKey getMatrixParent() {
		return matrixParent;
	}

	public OperationKey getOperationParent() {
		return operationParent;
	}

	@Override
	public String toString() {

		if (isMatrix()) {
			return getMatrixParent().toString();
		} else {
			return getOperationParent().toString();
		}
	}
}

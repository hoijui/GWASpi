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
public final class DataSetKey implements Identifier<DataSetKey> {

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

	public Identifier<?> getInternalIdentifier() {

		if (isMatrix()) {
			return getMatrixParent();
		} else {
			return getOperationParent();
		}
	}

	@Override
	public boolean isVirtual() {
		return false;
	}

	@Override
	public String toString() {
		return getInternalIdentifier().toString();
	}

	@Override
	public String toRawIdString() {
		return getInternalIdentifier().toRawIdString();
	}

	@Override
	public String toIdString() {
		return getInternalIdentifier().toIdString();
	}

	@Override
	public String fetchName() {
		return getInternalIdentifier().fetchName();
	}

	@Override
	public boolean equals(final Object other) {

		if (other == null) {
			return false;
		}
		if (other.getClass().equals(VirtualDataSetIdentifier.class)) {
			return other.equals(this);
		}
		if (!getClass().equals(other.getClass())) {
			return false;
		}
		final DataSetKey otherKey = (DataSetKey) other;
		if (this.isMatrix() && otherKey.isMatrix()) {
			return this.getMatrixParent().equals(otherKey.getMatrixParent());
		} else if (this.isOperation() && otherKey.isOperation()) {
			return this.getOperationParent().equals(otherKey.getOperationParent());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + (this.matrixParent != null ? this.matrixParent.hashCode() : 0);
		hash = 23 * hash + (this.operationParent != null ? this.operationParent.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(final Identifier<DataSetKey> other) {

		if (other instanceof DataSetKey) {
			final DataSetKey otherKey = (DataSetKey) other;
			if (this.isMatrix() && otherKey.isMatrix()) {
				return this.getMatrixParent().compareTo(otherKey.getMatrixParent());
			} else if (this.isOperation() && otherKey.isOperation()) {
				return this.getOperationParent().compareTo(otherKey.getOperationParent());
			} else {
				return this.isMatrix() ? 1 : -1;
			}
		} else {
			return - other.compareTo(this);
		}
	}
}

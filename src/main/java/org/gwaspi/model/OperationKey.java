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

import java.io.IOException;
import javax.persistence.Transient;

/**
 * Uniquely identifies an operation.
 */
public class OperationKey implements Identifier<OperationKey> {

	public static final int NULL_ID = -1; // alternatively: Integer.MIN_VALUE

	private static final String NAME_UNKNOWN = "<operation-name-unknown>";

	private MatrixKey parentMatrixKey; // XXX should be renamed to origin
	private int id;

	public OperationKey(MatrixKey parentMatrixKey, int id) {

		this.parentMatrixKey = parentMatrixKey;
		this.id = id;
	}

	protected OperationKey() {
		this(
				new MatrixKey(
				new StudyKey(StudyKey.NULL_ID),
				MatrixKey.NULL_ID),
				NULL_ID);
	}

	public static OperationKey valueOf(OperationMetadata operation) {
		return new OperationKey(
				operation.getParentMatrixKey(),
				operation.getId());
	}

	@Transient
	@Override
	public boolean isVirtual() {
		return false;
	}

	@Override
	public int compareTo(final Identifier<OperationKey> other) {

		if (other instanceof OperationKey) {
			final OperationKey otherKey = (OperationKey) other;
			int diff = this.getParentMatrixKey().compareTo(otherKey.getParentMatrixKey());
			if (diff == 0) {
				diff = this.getId() - otherKey.getId();
			}
			return diff;
		} else {
			return - other.compareTo(this);
		}
	}

	@Override
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj.getClass() == VirtualOperationIdentifier.class) {
			return obj.equals(this);
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final OperationKey other = (OperationKey) obj;
		if (!this.getParentMatrixKey().equals(other.getParentMatrixKey())) {
			return false;
		}
		return (this.getId() == other.getId());
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + (this.parentMatrixKey != null ? this.parentMatrixKey.hashCode() : 0);
		hash = 97 * hash + this.id;
		return hash;
	}

	@Override
	public String toRawIdString() {

		StringBuilder strRep = new StringBuilder();

		strRep.append("study-id: ").append(getStudyId());
		strRep.append(", matrix-id: ").append(getParentMatrixId());
		strRep.append(", id: ").append(getId());

		return strRep.toString();
	}

	@Override
	public String toIdString() {

		StringBuilder strRep = new StringBuilder();

		strRep.append(getClass().getSimpleName());
		strRep.append("[");
		strRep.append(toRawIdString());
		strRep.append("]");

		return strRep.toString();
	}

	/**
	 * This function accesses the storage system,
	 * but guarantees to also work if the operation is not available there,
	 * or an other problem occurs.
	 */
	@Override
	public String fetchName() {

		String operationName;


		OperationMetadata operation = null;
		try {
			operation = OperationsList.getOperationMetadata(this);
		} catch (IOException ex) {
			// do nothing, as operation will be null
		}

		if (operation == null) {
			operationName = NAME_UNKNOWN;
		} else {
			operationName = operation.getFriendlyName();
		}

		return operationName;
	}

	@Override
	public String toString() {

		StringBuilder strRep = new StringBuilder();

		strRep.append(fetchName());
		strRep.append(" [");
		strRep.append(toRawIdString());
		strRep.append("]");

		return strRep.toString();
	}

	public int getStudyId() {
		return parentMatrixKey.getStudyId();
	}

	protected void setStudyId(int studyId) {
		this.parentMatrixKey = new MatrixKey(
				new StudyKey(studyId),
				parentMatrixKey.getMatrixId()
				);
	}

	public int getParentMatrixId() {
		return parentMatrixKey.getMatrixId();
	}

	protected void setParentMatrixId(int parentMatrixId) {
		this.parentMatrixKey = new MatrixKey(
				parentMatrixKey.getStudyKey(),
				parentMatrixId
				);
	}

	@Transient
	public MatrixKey getParentMatrixKey() {
		return parentMatrixKey;
	}

	public int getId() {
		return id;
	}

	protected void setId(int id) {
		this.id = id;
	}
}

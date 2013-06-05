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

import java.io.Serializable;
import javax.persistence.Transient;

/**
 * Uniquely identifies an operation.
 */
public class OperationKey implements Comparable<OperationKey>, Serializable {

	private MatrixKey parentMatrixKey;
	private int id;

	/**
	 * @deprecated
	 */
	public OperationKey(int studyId, int parentMatrixId, int id) {

		this.parentMatrixKey = new MatrixKey(new StudyKey(studyId), parentMatrixId);
		this.id = id;
	}
	
	public OperationKey(MatrixKey parentMatrixKey, int id) {

		this.parentMatrixKey = parentMatrixKey;
		this.id = id;
	}

	protected OperationKey() {
		this(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public static OperationKey valueOf(OperationMetadata operation) {
		return new OperationKey(
				operation.getStudyId(),
				operation.getParentMatrixId(),
				operation.getId());
	}

	@Override
	public int compareTo(OperationKey other) {
		return hashCode() - other.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final OperationKey other = (OperationKey) obj;
		if (!this.getParentMatrixKey().equals(other.getParentMatrixKey())) {
			return false;
		}
		return (this.getId() != other.getId());
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + (this.parentMatrixKey != null ? this.parentMatrixKey.hashCode() : 0);
		hash = 97 * hash + this.id;
		return hash;
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

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

/**
 * Uniquely identifies an operation.
 */
public class OperationKey implements Comparable<OperationKey>, Serializable {

	private int studyId;
	private int parentMatrixId;
	private int id;

	public OperationKey(int studyId, int parentMatrixId, int id) {

		this.studyId = studyId;
		this.parentMatrixId = parentMatrixId;
		this.id = id;
	}

	protected OperationKey() {
		this(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
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
		if (this.getStudyId() != other.getStudyId()) {
			return false;
		}
		if (this.getId() != other.getId()) {
			return false;
		}
		if (this.getParentMatrixId() != other.getParentMatrixId()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + this.studyId;
		hash = 83 * hash + this.parentMatrixId;
		hash = 83 * hash + this.id;
		return hash;
	}

	public int getStudyId() {
		return studyId;
	}

	protected void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	public int getId() {
		return id;
	}

	protected void setId(int id) {
		this.id = id;
	}

	public int getParentMatrixId() {
		return parentMatrixId;
	}

	protected void setParentMatrixId(int parentMatrixId) {
		this.parentMatrixId = parentMatrixId;
	}
}

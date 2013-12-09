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
import java.io.Serializable;
import javax.persistence.Transient;

/**
 * Uniquely identifies a matrix.
 */
public class MatrixKey implements Comparable<MatrixKey>, Serializable {

	private static final String NAME_UNKNOWN = "<matrix-name-unknown>";

	private StudyKey studyKey;
	private int matrixId;

	public MatrixKey(StudyKey studyKey, int matrixId) {

		this.studyKey = studyKey;
		this.matrixId = matrixId;
	}

	protected MatrixKey() {
		this(null, Integer.MIN_VALUE);
	}

	public static MatrixKey valueOf(MatrixMetadata matrix) {
		return new MatrixKey(
				matrix.getStudyKey(),
				matrix.getMatrixId());
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MatrixKey other = (MatrixKey) obj;
		if (this.getStudyId() != other.getStudyId()) {
			return false;
		}
		if (this.getMatrixId() != other.getMatrixId()) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 19 * hash + this.getStudyId();
		hash = 19 * hash + this.getMatrixId();
		return hash;
	}

	@Override
	public int compareTo(MatrixKey other) {

		int diffStudy = this.getStudyId() - other.getStudyId();
		if (diffStudy == 0) {
			diffStudy = this.getMatrixId() - other.getMatrixId();
		}

		return diffStudy;
	}

	public String toRawIdString() {

		StringBuilder strRep = new StringBuilder();

		strRep.append("study-id: ").append(getStudyId());
		strRep.append(", id: ").append(getMatrixId());

		return strRep.toString();
	}

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
	 * but guarantees to also work if the matrix is not available there,
	 * or an other problem occurs.
	 */
	public String fetchName() {

		String matrixName;

		MatrixMetadata matrix = null;
		try {
			matrix = MatricesList.getMatrixMetadataById(this);
		} catch (IOException ex) {
			// do nothing, as matrix will be null
		}

		if (matrix == null) {
			matrixName = NAME_UNKNOWN;
		} else {
			matrixName = matrix.getFriendlyName();
		}

		return matrixName;
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
		return studyKey.getId();
	}

	protected void setStudyId(int studyId) {
		this.studyKey = new StudyKey(studyId);
	}

	@Transient
	public StudyKey getStudyKey() {
		return studyKey;
	}

	public int getMatrixId() {
		return matrixId;
	}

	protected void setMatrixId(int matrixId) {
		this.matrixId = matrixId;
	}
}

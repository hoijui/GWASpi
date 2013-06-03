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
 * Uniquely identifies a matrix.
 */
public class MatrixKey implements Comparable<MatrixKey>, Serializable {

	private int studyId;
	private int matrixId;

	protected MatrixKey() {

		this.studyId = Integer.MIN_VALUE;
		this.matrixId = Integer.MIN_VALUE;
	}

	public MatrixKey(int studyId, int matrixId) {

		this.studyId = studyId;
		this.matrixId = matrixId;
	}

	public static MatrixKey valueOf(MatrixMetadata matrix) {
		return new MatrixKey(
				matrix.getStudyId(),
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

	public int getStudyId() {
		return studyId;
	}

	protected void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	public int getMatrixId() {
		return matrixId;
	}

	protected void setMatrixId(int matrixId) {
		this.matrixId = matrixId;
	}
}

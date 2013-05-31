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
 * Identifies a matrix, possibly by different criteria.
 */
public class MatrixKey extends IdNameKey {

	private final StudyKey studyKey;

	public MatrixKey(StudyKey studyKey, int id) {
		super(id);

		this.studyKey = studyKey;
	}

	public MatrixKey(StudyKey studyKey, String name) {
		super(name);

		this.studyKey = studyKey;
	}

	public StudyKey getStudyKey() {
		return studyKey;
	}

	@Override
	public boolean equals(Object other) {

		if (super.equals(other) && (other instanceof MatrixKey)) {
			final MatrixKey otherMatrixKey = (MatrixKey) other;
			return getStudyKey().equals(otherMatrixKey.getStudyKey());
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3 + super.hashCode();
		hash = 37 * hash + (this.studyKey != null ? this.studyKey.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(IdNameKey other) {

		if (!(other instanceof MatrixKey)) {
			return -1;
		}
		final MatrixKey otherMatrixKey = (MatrixKey) other;
		int diffStudy = getStudyKey().compareTo(otherMatrixKey.getStudyKey());
		if (diffStudy == 0) {
			return super.compareTo(other);
		} else {
			return diffStudy;
		}
	}
}

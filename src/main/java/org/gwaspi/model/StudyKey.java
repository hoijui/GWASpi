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

/**
 * Uniquely identifies a study.
 */
public class StudyKey implements Comparable<StudyKey>, Serializable {

	private int id;

	public StudyKey(int id) {
		this.id = id;
	}

	protected StudyKey() {
		this(Integer.MIN_VALUE);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final StudyKey other = (StudyKey) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + this.id;
		return hash;
	}

	@Override
	public int compareTo(StudyKey other) {

		return this.getId() - other.getId();
	}

	public String toRawIdString() {

		StringBuilder strRep = new StringBuilder();

		strRep.append("id: ").append(getId());

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

	public String fetchName() {

		String studyName;

		try {
			Study study = StudyList.getStudy(this);
			studyName = study.getName();
		} catch (IOException ex) {
			studyName = "<study-name-unknown>";
		}

		return studyName;
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

	public static StudyKey valueOf(Study study) {
		return new StudyKey(study.getId());
	}

	public int getId() {
		return id;
	}

	protected void setId(int id) {
		this.id = id;
	}
}

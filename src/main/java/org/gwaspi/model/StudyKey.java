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
import org.gwaspi.dao.StudyService;

/**
 * Uniquely identifies an existing study.
 */
public class StudyKey implements Identifier<StudyKey> {

	public static final int NULL_ID = -1; // alternatively: Integer.MIN_VALUE

	private static final String NAME_UNKNOWN = "<study-name-unknown>";

	private int id;

	public StudyKey(int id) {
		this.id = id;
	}

	protected StudyKey() {
		this(NULL_ID);
	}

	private static StudyService getStudyService() {
		return StudyList.getStudyService();
	}

	@Transient
	@Override
	public boolean isVirtual() {
		return false;
	}

	@Override
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj.getClass() == VirtualStudyIdentifier.class) {
			return obj.equals(this);
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final StudyKey other = (StudyKey) obj;
		return (this.getId() == other.getId());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + this.id;
		return hash;
	}

	@Override
	public int compareTo(final Identifier<StudyKey> other) {

		if (other instanceof StudyKey) {
			return this.getId() - ((StudyKey) other).getId();
		} else {
			return -other.compareTo(this);
		}
	}

	@Override
	public String toRawIdString() {

		final StringBuilder strRep = new StringBuilder(64);

		strRep.append("id: ").append(getId());

		return strRep.toString();
	}

	@Override
	public String toIdString() {

		final StringBuilder strRep = new StringBuilder(128);

		strRep
				.append(getClass().getSimpleName())
				.append('[')
				.append(toRawIdString())
				.append(']');

		return strRep.toString();
	}

	/**
	 * This function accesses the storage system,
	 * but guarantees to also work if the study is not available there,
	 * or an other problem occurs.
	 */
	@Override
	public String fetchName() {

		String studyName;

		Study study = null;
		try {
			study = getStudyService().getStudy(this);
		} catch (final IOException ex) {
			// do nothing, as study will be null
		}

		if (study == null) {
			studyName = NAME_UNKNOWN;
		} else {
			studyName = study.getName();
		}

		return studyName;
	}

	@Override
	public String toString() {

		final StringBuilder strRep = new StringBuilder(128);

		strRep
				.append(fetchName())
				.append(" [")
				.append(toRawIdString())
				.append(']');

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

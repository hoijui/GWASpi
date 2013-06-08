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
 * Uniquely identifies a report.
 */
public class ReportKey implements Comparable<ReportKey>, Serializable {

	private StudyKey studyKey;
	private int parentMatrixId;
	private int parentOperationId;
	private int id;

	public ReportKey(OperationKey parentOperationKey, int id) {

		this.studyKey = parentOperationKey.getParentMatrixKey().getStudyKey();
		this.parentMatrixId = parentOperationKey.getParentMatrixKey().getMatrixId();
		this.parentOperationId = parentOperationKey.getId();
		this.id = id;
	}

	public ReportKey(StudyKey studyKey, int parentMatrixId, int parentOperationId, int id) {

		this.studyKey = studyKey;
		this.parentMatrixId = parentMatrixId;
		this.parentOperationId = parentOperationId;
		this.id = id;
	}

	protected ReportKey() {
		this(null, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public static ReportKey valueOf(Report report) {
		return new ReportKey(
				report.getStudyKey(),
				report.getParentMatrixId(),
				report.getParentOperationId(),
				report.getId());
	}

	@Override
	public int compareTo(ReportKey other) {
		return hashCode() - other.hashCode();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 61 * hash + this.studyKey.hashCode();
		hash = 61 * hash + this.id;
		hash = 61 * hash + this.parentMatrixId;
		hash = 61 * hash + this.parentOperationId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ReportKey other = (ReportKey) obj;
		if (!this.getStudyKey().equals(other.getStudyKey())) {
			return false;
		}
		if (this.getId() != other.getId()) {
			return false;
		}
		if (this.getParentMatrixId() != other.getParentMatrixId()) {
			return false;
		}
		if (this.getParentOperationId() != other.getParentOperationId()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {

		StringBuilder strRep = new StringBuilder();

		strRep.append(getClass().getSimpleName());
		strRep.append("[");
		strRep.append("study-id: ").append(getStudyId());
		strRep.append(", matrix-id: ").append(getParentMatrixId());
		strRep.append(", operation-id: ").append(getParentOperationId());
		strRep.append(", id: ").append(getId());
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

	public int getParentOperationId() {
		return parentOperationId;
	}

	protected void setParentOperationId(int parentOperationId) {
		this.parentOperationId = parentOperationId;
	}
}

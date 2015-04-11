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
 * Uniquely identifies a report.
 */
public class ReportKey implements Comparable<ReportKey>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final int NULL_ID = -1; // alternatively: Integer.MIN_VALUE

	private static final String NAME_UNKNOWN = "<report-name-unknown>";

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
		this(new StudyKey(StudyKey.NULL_ID), MatrixKey.NULL_ID, OperationKey.NULL_ID, ReportKey.NULL_ID);
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

	public String toRawIdString() {

		final StringBuilder strRep = new StringBuilder(128);

		strRep
				.append("study-id: ").append(getStudyId())
				.append(", matrix-id: ").append(getParentMatrixId())
				.append(", operation-id: ").append(getParentOperationId())
				.append(", id: ").append(getId());

		return strRep.toString();
	}

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
	 * but guarantees to also work if the report is not available there,
	 * or an other problem occurs.
	 */
	public String fetchName() {

		String reportName;

		Report report = null;
		try {
			report = ReportsList.getReport(this);
		} catch (IOException ex) {
			// do nothing, as report will be null
		}

		if (report == null) {
			reportName = NAME_UNKNOWN;
		} else {
			reportName = report.getFriendlyName();
		}

		return reportName;
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

	@Transient
	public DataSetKey getParent() {

		final MatrixKey originMatrixKey = new MatrixKey(getStudyKey(), getParentMatrixId());
		if (getParentOperationId() == OperationKey.NULL_ID) {
			return new DataSetKey(originMatrixKey);
		} else {
			final OperationKey parentOperationKey = new OperationKey(originMatrixKey, getParentOperationId());
			return new DataSetKey(parentOperationKey);
		}
	}
}

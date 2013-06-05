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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;

@Entity
@Table(name = "report")
@IdClass(ReportKey.class)
@NamedQueries({
	@NamedQuery(
		name = "report_fetchById",
		query = "SELECT r FROM Report r WHERE r.id = :id"),
	@NamedQuery(
		name = "report_fetchByParentOperationId",
		query = "SELECT r FROM Report r WHERE r.parentOperationId = :parentOperationId"),
	@NamedQuery(
		name = "report_fetchByParentMatrixId",
		query = "SELECT r FROM Report r WHERE r.parentMatrixId = :parentMatrixId"),
	@NamedQuery(
		name = "report_fetchByParentMatrixIdParentOperationId",
		query = "SELECT r FROM Report r WHERE r.parentMatrixId = :parentMatrixId AND r.parentOperationId = :parentOperationId"),
	@NamedQuery(
		name = "report_deleteByParentMatrixId",
		query = "DELETE FROM Report r WHERE r.parentMatrixId = :parentMatrixId"),
	@NamedQuery(
		name = "report_deleteByParentOperationId",
		query = "DELETE FROM Report r WHERE r.parentOperationId = :parentOperationId"),
})
public class Report implements Serializable {

	private ReportKey key;
//	private int id; // INTEGER generated by default as identity
	private String friendlyName; // VARCHAR(255) NOT NULL
	private String fileName; // VARCHAR(255) NOT NULL
	private OPType type; // VARCHAR(32) NOT NULL
//	private int parentMatrixId; // INTEGER
//	private int parentOpId; // INTEGER
	private String description; // VARCHAR(255)
//	private StudyKey studyKey; // INTEGER

	protected Report() {

		this.key = new ReportKey(null, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		this.friendlyName = "";
		this.fileName = "";
		this.type = null;
		this.description = "";
	}

	public Report(
			int id,
			String friendlyName,
			String fileName,
			OPType type,
			OperationKey parentOpKey,
			String description,
			StudyKey studyKey)
	{
		this.key = new ReportKey(parentOpKey, id);
		this.friendlyName = friendlyName;
		this.fileName = fileName;
		this.type = type;
		this.description = description;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Report other = (Report) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		if (this.getStudyId() != other.getStudyId()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 17 * hash + this.getId();
		hash = 17 * hash + this.getStudyId();
		return hash;
	}

	/**
	 * Returns the unique report identification number.
	 * @return the report-id
	 */
	@Id
	@SequenceGenerator(name = "seqReportId")
	@GeneratedValue(strategy = javax.persistence.GenerationType.TABLE, generator = "seqReportId")
	@Column(
		name       = "id",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getId() {
		return key.getId();
	}

//	/**
//	 * Sets the unique report identification number.
//	 * @param id the report-id to set
//	 */
	protected void setId(int id) {
		this.key = new ReportKey(
				key.getStudyKey(),
				key.getParentMatrixId(),
				key.getParentOperationId(),
				id);
	}

	@Id
	@Column(
		name       = "parentMatrixId",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getParentMatrixId() {
		return key.getParentMatrixId();
	}

	protected void setParentMatrixId(int parentMatrixId) {
		this.key = new ReportKey(
				key.getStudyKey(),
				parentMatrixId,
				key.getParentOperationId(),
				key.getId());
	}

	@Transient
	public MatrixKey getParentMatrixKey() {
		return new MatrixKey(key.getStudyKey(), key.getParentMatrixId());
	}

	@Id
	@Column(
		name       = "studyId",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getStudyId() {
		return key.getStudyKey().getId();
	}

	protected void setStudyId(int studyId) {
		this.key = new ReportKey(
				new StudyKey(studyId),
				key.getParentMatrixId(),
				key.getParentOperationId(),
				key.getId());
	}

	@Transient
	public StudyKey getStudyKey() {
		return key.getStudyKey();
	}

	@Column(
		name       = "friendlyName",
		length     = 255,
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getFriendlyName() {
		return friendlyName;
	}

	protected void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	@Column(
		name       = "fileName",
		length     = 255,
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getFileName() {
		return fileName;
	}

	protected void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(
		name       = "type",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public OPType getReportType() {
		return type;
	}

	protected void setReportType(OPType type) {
		this.type = type;
	}

	@Id
	@Column(
		name       = "parentOpId",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getParentOperationId() {
		return key.getParentOperationId();
	}

	protected void setParentOperationId(int parentOpId) {
		this.key = new ReportKey(
				key.getStudyKey(),
				key.getParentMatrixId(),
				parentOpId,
				key.getId());
	}

	@Transient
	public OperationKey getParentOperationKey() {
		return new OperationKey(getParentMatrixKey(), getParentOperationId());
	}

	@Column(
		name       = "description",
		length     = 255,
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getDescription() {
		return description;
	}

	protected void setDescription(String description) {
		this.description = description;
	}
}

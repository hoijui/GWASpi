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
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "study")
@NamedQueries({
//	@NamedQuery(name = "acc_size",             query = "SELECT count(a.id) FROM Account a"),
//	@NamedQuery(name = "size_active",          query = "SELECT count(a.id) FROM Account a WHERE ((a.inGameTime >= :minInGameTime) AND (a.lastLogin > :oneWeekAgo))"),
//	q_size_active.setParameter("minInGameTime", Account.Rank.Beginner.getRequiredTime());
//	@NamedQuery(name = "acc_size_active",      query = "SELECT count(a.id) FROM Account a WHERE ((a.inGameTime >= " + /*Account.Rank.Beginner.getRequiredTime()*/(5 * 60 * 60) + ") AND (a.lastLogin > :oneWeekAgo))"),
	@NamedQuery(name = "study_list",           query = "SELECT s FROM Study s"),
//	@NamedQuery(name = "acc_fetchByName",      query = "SELECT a FROM Account a WHERE a.name = :name"),
//	@NamedQuery(name = "acc_fetchByLowerName", query = "SELECT a FROM Account a WHERE (LOWER(a.name) = :lowerName)"),
//	@NamedQuery(name = "acc_fetchByLastIP",    query = "SELECT a FROM Account a WHERE a.lastIp = :ip"),
//	@NamedQuery(name = "acc_fetchByEmail",     query = "SELECT a FROM Account a WHERE a.email = :email")
	@NamedQuery(name = "study_fetchById",      query = "SELECT s FROM Study s WHERE s.id = :id"),
})
public class Study implements Serializable {

	private static final Logger log
			= LoggerFactory.getLogger(Study.class);

	private int id; // id INTEGER generated by default as identity
	private String name = ""; // name VARCHAR(64)
	private String description = ""; // study_description LONG VARCHAR
	private String studyType = ""; // study_type VARCHAR(255)
	private String validity = ""; // validity SMALLINT
	private Date creationDate; // creation_date TIMESTAMP (2009-05-13 17:22:10.984)
//	private List<Integer> studyMatrices = new ArrayList<Integer>();

	protected Study() {

		this.id = Integer.MIN_VALUE;
		this.name = "";
		this.description = "";
		this.studyType = "";
		this.validity = "";
		this.creationDate = new Date();
//		this.studyMatrices = new ArrayList<Integer>();
	}

	public Study(String name, String description) {

		this.id = Integer.MIN_VALUE;
		this.name = name;
		this.description = description;
		this.studyType = "";
		this.validity = "";
		this.creationDate = new Date();
//		this.studyMatrices = new ArrayList<Integer>();
	}

	public Study(int id, String name, String description, String studyType, String validity, Date creationDate) {

		this.id = id;
		this.name = name;
		this.description = description;
		this.studyType = studyType;
		this.validity = validity;
		this.creationDate = (Date) creationDate.clone();
//		this.studyMatrices = new ArrayList<Integer>();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Study other = (Study) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + this.getId();
		return hash;
	}

	/**
	 * Returns the unique study identification number.
	 * @return the study-id
	 */
	@Id
	@SequenceGenerator(name = "seqStudyId")
	@GeneratedValue(strategy = javax.persistence.GenerationType.TABLE, generator = "seqStudyId")
	@Column(
		name       = "id",
		unique     = true,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getId() {
		return id;
	}

	/**
	 * Sets the unique study identification number.
	 * @param id the study-id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	@Column(
		name       = "name",
		length     = 127,
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
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

	@Column(
		name       = "studyType",
		length     = 63,
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getStudyType() {
		return studyType;
	}

	protected void setStudyType(String studyType) {
		this.studyType = studyType;
	}

	@Column(
		name       = "validity",
		length     = 31,
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getValidity() {
		return validity;
	}

	protected void setValidity(String validity) {
		this.validity = validity;
	}

	@Temporal(TemporalType.DATE)
	@Column(
		name       = "creationDate",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public Date getCreationDate() {
		return creationDate;
	}

	protected void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
}

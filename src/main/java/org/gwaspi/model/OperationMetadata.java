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


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.netCDF.matrices.MatrixFactory;

@Entity
@Table(name = "operationMetadata")
@IdClass(OperationKey.class)
@NamedQueries({
	@NamedQuery(
		name = "operationMetadata_fetchById",
		query = "SELECT om FROM OperationMetadata om WHERE om.id = :id"),
	@NamedQuery(
		name = "operationMetadata_listByFriendlyName",
		query = "SELECT om.studyId, om.parentMatrixId, om.id FROM OperationMetadata om WHERE om.name = :name"),
	@NamedQuery(
		name = "operationMetadata_listByParentMatrixId",
		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId"),
	@NamedQuery(
		name = "operationMetadata_listByParentMatrixIdOperationId",
		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId AND om.id = :operationId"),
	@NamedQuery(
		name = "operationMetadata_listByParentMatrixIdParentOperationId",
		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId AND om.parentOperationId = :parentOperationId"),
	@NamedQuery(
		name = "operationMetadata_listByParentMatrixIdParentOperationIdOperationType",
		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId AND om.parentOperationId = :parentOperationId AND om.genotypeCode = :operationType"),
})
public class OperationMetadata implements DataSetMetadata, Serializable {

	private OperationKey key;
	private String name; // == Operation.friendlyName == OperationMetadata.opName
	private String simpleName;
	private OPType gtCode; // == Operation.type
	private int parentOperationId;
	private String description;
	private int opSetSize;
	private int implicitSetSize;
	private int numChromosomes;
	private boolean opSetMarkers;
	private Date creationDate;

	protected OperationMetadata() {

		this.key = new OperationKey();
		this.parentOperationId = OperationKey.NULL_ID;
		this.name = "";
		this.simpleName = "";
		this.description = "";
		this.gtCode = null;
		this.opSetSize = Integer.MIN_VALUE;
		this.implicitSetSize = Integer.MIN_VALUE;
		this.opSetMarkers = true;
		this.creationDate = new Date();
	}

//	/**
//	 * Full constructor, used when restoring from a storage backend.
//	 */
//	public OperationMetadata(
//			int id,
//			MatrixKey parentMatrixKey,
//			int parentOperationId,
//			String name,
//			String simpleName,
//			String description,
//			OPType gtCode,
//			int opSetSize,
//			int implicitSetSize,
//			int numChromosomes,
//			Date creationDate
//			)
//	{
//		this.key = new OperationKey(parentMatrixKey, id);
//		this.parentOperationId = parentOperationId;
//		this.name = name;
//		this.simpleName = simpleName;
//		this.description = description;
//		this.gtCode = gtCode;
//		this.opSetSize = opSetSize;
//		this.implicitSetSize = implicitSetSize;
//		this.numChromosomes = numChromosomes;
//		this.creationDate = (creationDate == null)
//				? null : (Date) creationDate.clone();
//	}

	public OperationMetadata(
			DataSetKey parent,
			String name,
			String description,
			OPType gtCode,
			int opSetSize,
			int implicitSetSize,
			int numChromosomes,
			boolean opSetMarkers
			)
	{
		final MatrixKey origin = parent.isMatrix() ? parent.getMatrixParent() : parent.getOperationParent().getParentMatrixKey();
		this.key = new OperationKey(origin, OperationKey.NULL_ID);
		this.parentOperationId = parent.isOperation() ? parent.getOperationParent().getId() : OperationKey.NULL_ID;
		this.name = name;
		this.description = description;
		this.gtCode = gtCode;
		this.opSetSize = opSetSize;
		this.implicitSetSize = implicitSetSize;
		this.numChromosomes = numChromosomes;
		this.opSetMarkers = opSetMarkers;
		this.creationDate = new Date();
		this.simpleName = gtCode.name() + "_" + MatrixFactory.generateMatrixNetCDFNameByDate(creationDate);
	}

//	/**
//	 * Constructor to be used when creating a new operation,
//	 * that will be completed later on.
//	 */
//	public OperationMetadata(
//			MatrixKey parentMatrixKey,
//			int parentOperationId,
//			String name,
//			String description,
//			OPType gtCode
//			)
//	{
//		this(
//				parentMatrixKey,
//				parentOperationId,
//				name,
//				description,
//				gtCode,
//				Integer.MIN_VALUE,
//				Integer.MIN_VALUE,
//				Integer.MIN_VALUE);
//	}

	@Transient
	@Override
	public boolean isOrigin() {
		return false;
	}

	@Transient
	@Override
	public MatrixKey getOrigin() {
		return key.getParentMatrixKey();
	}

	@Transient
	@Override
	public DataSetKey getDataSetKey() {
		return new DataSetKey(key);
	}

	@Transient
	@Override
	public int getNumMarkers() {
		return isOpSetMarkers() ? getOpSetSize() : getImplicitSetSize();
	}

	@Transient
	@Override
	public int getNumSamples() {
		return isOpSetMarkers() ? getImplicitSetSize() : getOpSetSize();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final OperationMetadata other = (OperationMetadata) obj;
		if (this.getStudyId() != other.getStudyId()) {
			return false;
		}
		if (this.getParentMatrixId() != other.getParentMatrixId()) {
			return false;
		}
		return (this.getOPId() == other.getOPId());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + this.getStudyId();
		hash = 29 * hash + this.getParentMatrixId();
		hash = 29 * hash + this.getOPId();
		return hash;
	}

	@Transient
	public boolean isWithOperationParent() {
		return (getParentOperationId() != OperationKey.NULL_ID);
	}

	@Transient
	@Override
	public DataSetKey getParent() {

		if (isWithOperationParent()) {
			return new DataSetKey(getParentOperationKey());
		} else {
			return new DataSetKey(getParentMatrixKey());
		}
	}

	@Id
	@SequenceGenerator(name = "seqOperationId")
	@GeneratedValue(strategy = javax.persistence.GenerationType.TABLE, generator = "seqOperationId")
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

	protected void setId(int id) {
		this.key = new OperationKey(key.getParentMatrixKey(), id);
	}

	@Transient
	public int getOPId() {
		return getId();
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
		return key.getParentMatrixKey().getMatrixId();
	}

	protected void setParentMatrixId(int parentMatrixId) {
		this.key = new OperationKey(new MatrixKey(
				key.getParentMatrixKey().getStudyKey(),
				parentMatrixId),
				getId());
	}

	@Transient
	public MatrixKey getParentMatrixKey() {
		return key.getParentMatrixKey();
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
		return key.getParentMatrixKey().getStudyKey().getId();
	}

	protected void setStudyId(int studyId) {
		this.key = new OperationKey(new MatrixKey(
				new StudyKey(studyId),
				key.getParentMatrixKey().getMatrixId()),
				getId());
	}

	@Transient
	public StudyKey getStudyKey() {
		return key.getParentMatrixKey().getStudyKey();
	}

	/**
	 * A human eye friendly name.
	 * @return a string matching with any characters
	 */
	@Column(
		name       = "name",
		length     = 127,
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	public String getName() {
		return name;
	}

	protected void setName(String opName) {
		this.name = opName;
	}

	@Transient
	@Override
	public String getFriendlyName() {
		return getName();
	}

	/**
	 * A simple, (generally) unique machine friendly name for this operation,
	 * to be used for storage file names, for example.
	 * @return a string matching "[0-9a-zA-Z_:.]+"
	 */
	@Column(
		name       = "simpleName",
		length     = 255,
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	@Override
	public String getSimpleName() {
		return simpleName;
	}

	protected void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	@Column(
		name       = "gtCode",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public OPType getGenotypeCode() {
		return gtCode;
	}

	protected void setGenotypeCode(OPType gtCode) {
		this.gtCode = gtCode;
	}

	@Transient
	public OPType getOperationType() {
		return getGenotypeCode();
	}

	@Column(
		name       = "opSetSize",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	public int getOpSetSize() {
		return opSetSize;
	}

	public void setOpSetSize(int opSetSize) {
		this.opSetSize = opSetSize;
	}

	@Column(
		name       = "description",
		length     = 511,
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	@Override
	public String getDescription() {
		return description;
	}

	protected void setDescription(String description) {
		this.description = description;
	}

	@Id
	@Column(
		name       = "parentOperationId",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getParentOperationId() {
		return parentOperationId;
	}

	protected void setParentOperationId(int parentOperationId) {
		this.parentOperationId = parentOperationId;
	}

	@Transient
	public OperationKey getParentOperationKey() {
		return new OperationKey(getParentMatrixKey(), getParentOperationId());
	}

	@Column(
		name       = "implicitSetSize",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	public int getImplicitSetSize() {
		return implicitSetSize;
	}

	public void setImplicitSetSize(int implicitSetSize) {
		this.implicitSetSize = implicitSetSize;
	}

	@Column(
		name       = "numChromosomes",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	@Override
	public int getNumChromosomes() {
		return numChromosomes;
	}

	public void setNumChromosomes(int numChromosomes) {
		this.numChromosomes = numChromosomes;
	}

	@Column(
		name       = "opSetMarkers",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public boolean isOpSetMarkers() {
		return opSetMarkers;
	}

	public void setOpSetMarkers(boolean opSetMarkers) {
		this.opSetMarkers = opSetMarkers;
	}

	@Temporal(TemporalType.DATE)
	@Column(
		name       = "creationDate",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	@Override
	public Date getCreationDate() {
		return (creationDate == null) ? null : (Date) creationDate.clone();
	}

	protected void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Transient
	public OPType getType() {
		return getGenotypeCode();
	}

	public static File generatePathToNetCdfFile(OperationMetadata operation) throws IOException {

		String genotypesFolder = Study.constructGTPath(operation.getStudyKey());
		return new File(genotypesFolder, operation.getSimpleName() + ".nc");
	}
}

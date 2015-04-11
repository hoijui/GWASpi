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
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.constants.NetCDFConstants.Defaults.StrandType;
import org.gwaspi.global.Config;
import org.gwaspi.netCDF.matrices.MatrixFactory;

@Entity
@Table(name = "matrixMetadata")
@IdClass(MatrixKey.class)
@NamedQueries({
	@NamedQuery(
		name = "matrixMetadata_fetchById",
		query = "SELECT mm FROM MatrixMetadata mm WHERE mm.matrixId = :id"),
	@NamedQuery(
		name = "matrixMetadata_listKeysByStudyIdAndSimpleName",
		query = "SELECT mm.matrixId FROM MatrixMetadata mm WHERE mm.studyId = :studyId AND mm.simpleName = :simpleName"),
	@NamedQuery(
		name = "matrixMetadata_listKeysByStudyIdAndFriendlyName",
		query = "SELECT mm.matrixId FROM MatrixMetadata mm WHERE mm.studyId = :studyId AND mm.friendlyName = :friendlyName"),
	@NamedQuery(
		name = "matrixMetadata_listIdsByStudyId",
		query = "SELECT mm.matrixId FROM MatrixMetadata mm WHERE mm.studyId = :studyId"),
	@NamedQuery(
		name = "matrixMetadata_listIds",
		query = "SELECT mm.matrixId FROM MatrixMetadata mm ORDER BY mm.matrixId"),
	@NamedQuery(
		name = "matrixMetadata_listByStudyId",
		query = "SELECT mm FROM MatrixMetadata mm WHERE mm.studyId = :studyId"),
})
public class MatrixMetadata implements DataSetMetadata, Serializable {

	private static final long serialVersionUID = 1L;

	private MatrixKey key;
	private String friendlyName;
	private String simpleName;
	private ImportFormat technology;
	private String gwaspiDBVersion;
	private String description;
	private GenotypeEncoding gtEncoding;
	private StrandType strand;
	private boolean hasDictionary;
	private int numMarkers;
	private int numSamples;
	private int numChromosomes;
	private String matrixType; // matrix_type VARCHAR(32) NOT NULL
	private int parent1MatrixId;
	private int parent2MatrixId;
	private String inputLocation;
	private Date creationDate;

	protected MatrixMetadata() {

		this.key = new MatrixKey(new StudyKey(StudyKey.NULL_ID), MatrixKey.NULL_ID);
		this.friendlyName = "";
		this.technology = ImportFormat.UNKNOWN;
		this.gwaspiDBVersion = "";
		this.description = "";
		this.gtEncoding = null;
		this.strand = null;
		this.hasDictionary = false;
		this.numMarkers = Integer.MIN_VALUE;
		this.numSamples = Integer.MIN_VALUE;
		this.numChromosomes = Integer.MIN_VALUE;
		this.matrixType = "";
		this.parent1MatrixId = -1;
		this.parent2MatrixId = -1;
		this.inputLocation = "";
		this.creationDate = new Date();
		this.simpleName = MatrixFactory.generateMatrixNetCDFNameByDate(this.creationDate);
	}

	/**
	 * Full constructor, setting everything directly.
	 */
	public MatrixMetadata(
			MatrixKey key,
			String friendlyName,
			String simpleName,
			ImportFormat technology,
			String gwaspiDBVersion,
			String description,
			GenotypeEncoding gtEncoding,
			StrandType strand,
			boolean hasDictionary,
			int numMarkers,
			int numSamples,
			int numChromosomes,
			String matrixType,
			Date creationDate,
			int parent1MatrixId,
			int parent2MatrixId,
			String inputLocation)
	{
		this.key = key;
		this.friendlyName = friendlyName;
		this.simpleName = simpleName;
		this.technology = technology;
		this.gwaspiDBVersion = gwaspiDBVersion;
		this.description = description;
		this.gtEncoding = gtEncoding;
		this.strand = strand;
		this.hasDictionary = hasDictionary;
		this.numMarkers = numMarkers;
		this.numSamples = numSamples;
		this.numChromosomes = numChromosomes;
		this.matrixType = matrixType;
		this.parent1MatrixId = parent1MatrixId;
		this.parent2MatrixId = parent2MatrixId;
		this.inputLocation = inputLocation;
		this.creationDate = (creationDate == null)
				? null // XXX should we maybe use new Date() or new Date(0) here?
				: (Date) creationDate.clone();
	}

	public MatrixMetadata(
			String friendlyName,
			String description,
			GenotypeEncoding gtEncoding,
			StudyKey studyKey
			)
	{
		this.key = new MatrixKey(studyKey, MatrixKey.NULL_ID);
		this.friendlyName = friendlyName;
		this.technology = ImportFormat.UNKNOWN;
		this.gwaspiDBVersion = "";
		this.description = description;
		this.gtEncoding = gtEncoding;
		this.strand = null;
		this.hasDictionary = false;
		this.numMarkers = Integer.MIN_VALUE;
		this.numSamples = Integer.MIN_VALUE;
		this.numChromosomes = Integer.MIN_VALUE;
		this.matrixType = "";
		this.parent1MatrixId = MatrixKey.NULL_ID;
		this.parent2MatrixId = MatrixKey.NULL_ID;
		this.inputLocation = "";
		this.creationDate = new Date();
		this.simpleName = MatrixFactory.generateMatrixNetCDFNameByDate(this.creationDate);
	}

	/**
	 * Helper constructor, because a technicality problem
	 * with the creation date.
	 */
	private MatrixMetadata(
			StudyKey studyKey,
			String friendlyName,
			ImportFormat technology,
			String description,
			GenotypeEncoding gtEncoding,
			StrandType strand,
			boolean hasDictionary,
			int numMarkers,
			int numSamples,
			int numChromosomes,
			String matrixType,
			Date creationDate,
			int parent1MatrixId,
			int parent2MatrixId,
			String inputLocation)
			throws IOException
	{
		this(
				new MatrixKey(studyKey, MatrixKey.NULL_ID), // key
				friendlyName,
				MatrixFactory.generateMatrixNetCDFNameByDate(creationDate), // simpleName
				technology,
				Config.getSingleton().getString(Config.PROPERTY_CURRENT_GWASPIDB_VERSION, null), // gwaspiDBVersion
				description,
				gtEncoding,
				strand,
				hasDictionary,
				numMarkers,
				numSamples,
				numChromosomes,
				matrixType,
				creationDate,
				parent1MatrixId,
				parent2MatrixId,
				inputLocation);
	}

	/**
	 * Used when creating a new matrix, comprised of other matrices.
	 * When invoking this, the matrix does not yet have an ID.
	 * It should be inserted into the DB,
	 * and at that moment receive a unique ID.
	 */
	public MatrixMetadata(
			StudyKey studyKey,
			String friendlyName,
			ImportFormat technology,
			String description,
			GenotypeEncoding gtEncoding,
			StrandType strand,
			boolean hasDictionary,
			int numMarkers,
			int numSamples,
			int numChromosomes,
			int parent1MatrixId,
			int parent2MatrixId)
			throws IOException
	{
		this(
				studyKey,
				friendlyName,
				technology,
				description,
				gtEncoding,
				strand,
				hasDictionary,
				numMarkers,
				numSamples,
				numChromosomes,
				"", // matrixType
				new Date(), // creationDate
				parent1MatrixId,
				parent2MatrixId,
				"Result of the matrix with ID: " + parent1MatrixId);
	}

	/**
	 * Used when creating a new matrix, loading the data from an other format.
	 * When invoking this, the matrix does not yet have an ID.
	 * It should be inserted into the DB,
	 * and at that moment receive a unique ID.
	 */
	public MatrixMetadata(
			StudyKey studyKey,
			String friendlyName,
			ImportFormat technology,
			String description,
			GenotypeEncoding gtEncoding,
			StrandType strand,
			boolean hasDictionary,
			int numMarkers,
			int numSamples,
			int numChromosomes,
			String inputLocation)
			throws IOException
	{
		this(
				studyKey,
				friendlyName,
				technology,
				description,
				gtEncoding,
				strand,
				hasDictionary,
				numMarkers,
				numSamples,
				numChromosomes,
				"", // matrixType
				new Date(), // creationDate
				MatrixKey.NULL_ID, // parent1MatrixId
				MatrixKey.NULL_ID, // parent2MatrixId
				inputLocation);
	}

	/**
	 * Used when loading the matrix from a net-CDF source.
	 */
	public MatrixMetadata(
			MatrixKey key,
			String friendlyName,
			String simpleName,
			ImportFormat technology,
			String gwaspiDBVersion,
			String description,
			GenotypeEncoding gtEncoding,
			StrandType strand,
			boolean hasDictionary,
			int numMarkers,
			int numSamples,
			int numChromosomes,
			String matrixType,
			Date creationDate)
	{
		this(
				key,
				friendlyName,
				simpleName,
				technology,
				gwaspiDBVersion,
				description,
				gtEncoding,
				strand,
				hasDictionary,
				numMarkers,
				numSamples,
				numChromosomes,
				matrixType,
				creationDate,
				MatrixKey.NULL_ID, // parent1MatrixId
				MatrixKey.NULL_ID, // parent2MatrixId
				""); // inputLocation
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MatrixMetadata other = (MatrixMetadata) obj;
		if (this.getStudyId() != other.getStudyId()) {
			return false;
		}
		return (this.getMatrixId() == other.getMatrixId());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 17 * hash + this.getMatrixId();
		hash = 17 * hash + this.getStudyId();
		return hash;
	}

	@Column(
		name       = "hasDictionary",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = true
		)
	public boolean getHasDictionary() {
		return hasDictionary;
	}

	public void setHasDictionary(boolean hasDictionary) {
		this.hasDictionary = hasDictionary;
	}

	@Id
	@SequenceGenerator(name = "seqMatrixId")
	@GeneratedValue(strategy = javax.persistence.GenerationType.TABLE, generator = "seqMatrixId")
	@Column(
		name       = "matrixId",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getMatrixId() {
		return key.getMatrixId();
	}

	protected void setMatrixId(int matrixId) {
		this.key = new MatrixKey(key.getStudyKey(), matrixId);
	}

	@Transient
	public MatrixKey getKey() {
		return key;
	}

	@Transient
	@Override
	public DataSetKey getDataSetKey() {
		return new DataSetKey(getKey());
	}

	@Transient
	@Override
	public boolean isOrigin() {
		return true;
	}

	@Transient
	@Override
	public MatrixKey getOrigin() {
		return getKey();
	}

	@Transient
	@Override
	public DataSetKey getParent() {
		return null;
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
		return key.getStudyId();
	}

	protected void setStudyId(int studyId) {
		this.key = new MatrixKey(new StudyKey(studyId), key.getMatrixId());
	}

	@Transient
	@Override
	public StudyKey getStudyKey() {
		return key.getStudyKey();
	}

	/**
	 * A human eye friendly name.
	 * @return a string matching with any characters
	 */
	@Column(
		name       = "friendlyName",
		length     = 255,
		unique     = true,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	@Override
	public String getFriendlyName() {
		return friendlyName;
	}

	protected void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	/**
	 * A simple, (generally) unique machine friendly name for this matrix,
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
		name       = "technology",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	public ImportFormat getTechnology() {
		return technology;
	}

	public void setTechnology(ImportFormat technology) {
		this.technology = technology;
	}

	@Column(
		name       = "gwaspiDBVersion",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = true
		)
	public String getGwaspiDBVersion() {
		return gwaspiDBVersion;
	}

	public void setGwaspiDBVersion(String gwaspiDBVersion) {
		this.gwaspiDBVersion = gwaspiDBVersion;
	}

	@Column(
		name       = "gtEncoding",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	public GenotypeEncoding getGenotypeEncoding() {
		return gtEncoding;
	}

	public void setGenotypeEncoding(GenotypeEncoding gtEncoding) {
		this.gtEncoding = gtEncoding;
	}

	@Column(
		name       = "numMarkers",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	@Override
	public int getNumMarkers() {
		return numMarkers;
	}

	public void setNumMarkers(int numMarkers) {
		this.numMarkers = numMarkers;
	}

	@Column(
		name       = "numSamples",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	@Override
	public int getNumSamples() {
		return numSamples;
	}

	public void setNumSamples(int numSamples) {
		this.numSamples = numSamples;
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
		name       = "strand",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	public StrandType getStrand() {
		return strand;
	}

	public void setStrand(StrandType strand) {
		this.strand = strand;
	}

	@Column(
		name       = "description",
		length     = 1023,
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(
		name       = "matrixType",
		length     = 31,
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	public String getMatrixType() {
		return matrixType;
	}

	protected void setMatrixType(String matrixType) {
		this.matrixType = matrixType;
	}

	@Column(
		name       = "parent1MatrixId",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	public Integer getParent1MatrixId() {
		return parent1MatrixId;
	}

	protected void setParent1MatrixId(int parent1MatrixId) {
		this.parent1MatrixId = parent1MatrixId;
	}

	@Column(
		name       = "parent2MatrixId",
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	public Integer getParent2MatrixId() {
		return parent2MatrixId;
	}

	protected void setParent2MatrixId(int parent2MatrixId) {
		this.parent2MatrixId = parent2MatrixId;
	}

	@Column(
		name       = "inputLocation",
		length     = 1023,
		unique     = false,
		nullable   = true,
		insertable = true,
		updatable  = false
		)
	public String getInputLocation() {
		return inputLocation;
	}

	protected void setInputLocation(String inputLocation) {
		this.inputLocation = inputLocation;
	}

	@Temporal(TemporalType.DATE)
	@Column(
		name       = "creationDate",
		unique     = false,
		nullable   = false,
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

	public static File generatePathToNetCdfFileGeneric(DataSetMetadata dataSet) throws IOException {

		if (dataSet.getDataSetKey().isMatrix()) {
			return generatePathToNetCdfFile((MatrixMetadata) dataSet);
		} else {
			return OperationMetadata.generatePathToNetCdfFile((OperationMetadata) dataSet);
		}
	}

	public static File generatePathToNetCdfFile(MatrixMetadata matrix) throws IOException {

		String genotypesFolder = Study.constructGTPath(matrix.getStudyKey());
		return new File(genotypesFolder, matrix.getSimpleName() + ".nc");
	}
}

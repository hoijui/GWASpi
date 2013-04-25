package org.gwaspi.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
@Entity
@Table(name = "matrixMetadata")
@NamedQueries({
	@NamedQuery(
		name = "matrixMetadata_fetchById",
		query = "SELECT mm FROM MatrixMetadata mm WHERE mm.matrixId = :id"),
	@NamedQuery(
		name = "matrixMetadata_fetchByNetCDFName",
		query = "SELECT mm FROM MatrixMetadata mm WHERE mm.matrixNetCDFName = :netCDFName"),
	@NamedQuery(
		name = "matrixMetadata_listIds",
		query = "SELECT mm.matrixId FROM MatrixMetadata mm ORDER BY mm.matrixId"),
	@NamedQuery(
		name = "matrixMetadata_listByStudyId",
		query = "SELECT mm FROM MatrixMetadata mm WHERE mm.studyId = :studyId"),
})
public class MatrixMetadata implements Serializable {

	private int matrixId;
	private String matrixFriendlyName;
	private String matrixNetCDFName;
	private String pathToMatrix;
	private ImportFormat technology;
	private String gwaspiDBVersion;
	private String description;
	private GenotypeEncoding gtEncoding;
	private StrandType strand;
	private boolean hasDictionray;
	private int markerSetSize;
	private int sampleSetSize;
	private int studyId;
	private String matrixType; // matrix_type VARCHAR(32) NOT NULL
	private int parent1MatrixId;
	private int parent2MatrixId;
	private String inputLocation;
	private Date creationDate;

	protected MatrixMetadata() {

		this.matrixId = Integer.MIN_VALUE;
		this.matrixFriendlyName = "";
		this.matrixNetCDFName = "";
		this.pathToMatrix = "";
		this.technology = ImportFormat.UNKNOWN;
		this.gwaspiDBVersion = "";
		this.description = "";
		this.gtEncoding = null;
		this.strand = null;
		this.hasDictionray = false;
		this.markerSetSize = Integer.MIN_VALUE;
		this.sampleSetSize = Integer.MIN_VALUE;
		this.studyId = Integer.MIN_VALUE;
		this.matrixType = "";
		this.parent1MatrixId = -1;
		this.parent2MatrixId = -1;
		this.inputLocation = "";
		this.creationDate = new Date();
	}

	public MatrixMetadata(
			String matrixFriendlyName,
			String matrixNetCDFName,
			String description,
			GenotypeEncoding gtEncoding,
			int studyId,
			int parent1MatrixId,
			int parent2MatrixId,
			String inputLocation
			)
	{
		this.matrixId = Integer.MIN_VALUE;
		this.matrixFriendlyName = matrixFriendlyName;
		this.matrixNetCDFName = matrixNetCDFName;
		this.pathToMatrix = "";
		this.technology = ImportFormat.UNKNOWN;
		this.gwaspiDBVersion = "";
		this.description = description;
		this.gtEncoding = gtEncoding;
		this.strand = null;
		this.hasDictionray = false;
		this.markerSetSize = Integer.MIN_VALUE;
		this.sampleSetSize = Integer.MIN_VALUE;
		this.studyId = studyId;
		this.matrixType = "";
		this.parent1MatrixId = parent1MatrixId;
		this.parent2MatrixId = parent2MatrixId;
		this.inputLocation = inputLocation;
		this.creationDate = new Date();
	}

	public MatrixMetadata(
			int matrixId,
			String matrixFriendlyName,
			String matrixNetCDFName,
			String pathToMatrix,
			ImportFormat technology,
			String gwaspiDBVersion,
			String description,
			GenotypeEncoding gtEncoding,
			StrandType strand,
			boolean hasDictionray,
			int markerSetSize,
			int sampleSetSize,
			int studyId,
			String matrixType,
			Date creationDate)
	{
		this.matrixId = matrixId;
		this.matrixFriendlyName = matrixFriendlyName;
		this.matrixNetCDFName = matrixNetCDFName;
		this.pathToMatrix = pathToMatrix;
		this.technology = technology;
		this.gwaspiDBVersion = gwaspiDBVersion;
		this.description = description;
		this.gtEncoding = gtEncoding;
		this.strand = strand;
		this.hasDictionray = hasDictionray;
		this.markerSetSize = markerSetSize;
		this.sampleSetSize = sampleSetSize;
		this.studyId = studyId;
		this.matrixType = matrixType;
		this.parent1MatrixId = -1;
		this.parent2MatrixId = -1;
		this.inputLocation = "";
		this.creationDate = (Date) creationDate.clone();
	}

	@Column(
		name       = "hasDictionray",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public boolean getHasDictionray() {
		return hasDictionray;
	}

	@Id
	@Column(
		name       = "matrixId",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getMatrixId() {
		return matrixId;
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
		return studyId;
	}

	@Column(
		name       = "matrixFriendlyName",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getMatrixFriendlyName() {
		return matrixFriendlyName;
	}

	@Column(
		name       = "technology",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public ImportFormat getTechnology() {
		return technology;
	}

	@Column(
		name       = "gwaspiDBVersion",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getGwaspiDBVersion() {
		return gwaspiDBVersion;
	}

	@Column(
		name       = "gtEncoding",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public GenotypeEncoding getGenotypeEncoding() {
		return gtEncoding;
	}

	@Column(
		name       = "markerSetSize",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getMarkerSetSize() {
		return markerSetSize;
	}

	@Column(
		name       = "sampleSetSize",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getSampleSetSize() {
		return sampleSetSize;
	}

	@Column(
		name       = "pathToMatrix",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getPathToMatrix() {
		return pathToMatrix;
	}

	@Column(
		name       = "strand",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public StrandType getStrand() {
		return strand;
	}

	@Column(
		name       = "description",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(
		name       = "matrixNetCDFName",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getMatrixNetCDFName() {
		return matrixNetCDFName;
	}

	@Column(
		name       = "matrixType",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getMatrixType() {
		return matrixType;
	}

	@Id
	@Column(
		name       = "parent1MatrixId",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getParent1MatrixId() {
		return parent1MatrixId;
	}

	@Id
	@Column(
		name       = "parent2MatrixId",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public int getParent2MatrixId() {
		return parent2MatrixId;
	}

	@Column(
		name       = "inputLocation",
		unique     = false,
		nullable   = false,
		insertable = true,
		updatable  = false
		)
	public String getInputLocation() {
		return inputLocation;
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
}

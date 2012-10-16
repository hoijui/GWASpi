package org.gwaspi.model;

import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixMetadata {

	private int matrixId = Integer.MIN_VALUE;
	private String matrixFriendlyName = "";
	private String matrixNetCDFName = "";
	private String pathToMatrix = "";
	private ImportFormat technology = ImportFormat.UNKNOWN;
	private String gwaspiDBVersion = "";
	private String description = "";
	private GenotypeEncoding gtEncoding = null;
	private StrandType strand = null;
	private boolean hasDictionray = false;
	private int markerSetSize = Integer.MIN_VALUE;
	private int sampleSetSize = Integer.MIN_VALUE;
	private int studyId = Integer.MIN_VALUE;
	private String matrixType = ""; // matrix_type VARCHAR(32) NOT NULL

//	public MatrixMetadata() {
//
//		this.matrixId = Integer.MIN_VALUE;
//		this.matrixFriendlyName = "";
//		this.matrixNetCDFName = "";
//		this.pathToMatrix = "";
//		this.technology = ImportFormat.UNKNOWN;
//		this.gwaspiDBVersion = "";
//		this.description = "";
//		this.gtEncoding = null;
//		this.strand = null;
//		this.hasDictionray = false;
//		this.markerSetSize = Integer.MIN_VALUE;
//		this.sampleSetSize = Integer.MIN_VALUE;
//		this.studyId = Integer.MIN_VALUE;
//		this.matrixType = "";
//	}

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
			String matrixType)
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
	}

	public boolean getHasDictionray() {
		return hasDictionray;
	}

	public int getMatrixId() {
		return matrixId;
	}

	public int getStudyId() {
		return studyId;
	}

	public String getMatrixFriendlyName() {
		return matrixFriendlyName;
	}

	public ImportFormat getTechnology() {
		return technology;
	}

	public String getGwaspiDBVersion() {
		return gwaspiDBVersion;
	}

	public GenotypeEncoding getGenotypeEncoding() {
		return gtEncoding;
	}

	public int getMarkerSetSize() {
		return markerSetSize;
	}

	public int getSampleSetSize() {
		return sampleSetSize;
	}

	public String getPathToMatrix() {
		return pathToMatrix;
	}

	public StrandType getStrand() {
		return strand;
	}

	public String getDescription() {
		return description;
	}

	public String getMatrixNetCDFName() {
		return matrixNetCDFName;
	}

	public String getMatrixType() {
		return matrixType;
	}
}

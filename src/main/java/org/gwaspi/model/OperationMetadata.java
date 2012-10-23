package org.gwaspi.model;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OperationMetadata {

	private int id;
	private int parentMatrixId;
	private int parentOperationId;
	private String opName;
	private String netCDFName;
	private String description;
	private String pathToMatrix;
	private String gtCode;
	private int opSetSize;
	private int implicitSetSize;
	private int studyId;
	private long creationDate;

//	public OperationMetadata() {
//
//		this.id = Integer.MIN_VALUE;
//		this.parentMatrixId = Integer.MIN_VALUE;
//		this.parentOperationId = Integer.MIN_VALUE;
//		this.opName = "";
//		this.netCDFName = "";
//		this.description = "";
//		this.pathToMatrix = "";
//		this.gtCode = "";
//		this.opSetSize = Integer.MIN_VALUE;
//		this.implicitSetSize = Integer.MIN_VALUE;
//		this.studyId = Integer.MIN_VALUE;
//		this.creationDate = Long.MIN_VALUE;
//	}

	public OperationMetadata(
			int id,
			int parentMatrixId,
			int parentOperationId,
			String opName,
			String netCDFName,
			String description,
			String pathToMatrix,
			String gtCode,
			int opSetSize,
			int implicitSetSize,
			int studyId,
			long creationDate
			)
	{
		this.id = id;
		this.parentMatrixId = parentMatrixId;
		this.parentOperationId = parentOperationId;
		this.opName = opName;
		this.netCDFName = netCDFName;
		this.description = description;
		this.pathToMatrix = pathToMatrix;
		this.gtCode = gtCode;
		this.opSetSize = opSetSize;
		this.implicitSetSize = implicitSetSize;
		this.studyId = studyId;
		this.creationDate = creationDate;
	}

	public int getOPId() {
		return id;
	}

	public int getParentMatrixId() {
		return parentMatrixId;
	}

	public int getStudyId() {
		return studyId;
	}

	public String getOPName() {
		return opName;
	}

	public String getMatrixCDFName() {
		return netCDFName;
	}

	public String getGenotypeCode() {
		return gtCode;
	}

	public int getOpSetSize() {
		return opSetSize;
	}

	public String getPathToMatrix() {
		return pathToMatrix;
	}

	public String getDescription() {
		return description;
	}

	public int getParentOperationId() {
		return parentOperationId;
	}

	public int getImplicitSetSize() {
		return implicitSetSize;
	}

	public long getCreationDate() {
		return creationDate;
	}
}

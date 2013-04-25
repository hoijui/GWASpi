package org.gwaspi.model;


import java.util.Date;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;

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
	private OPType gtCode;
	private int opSetSize;
	private int implicitSetSize;
	private int studyId;
	private Date creationDate;

//	public OperationMetadata() {
//
//		this.id = Integer.MIN_VALUE;
//		this.parentMatrixId = Integer.MIN_VALUE;
//		this.parentOperationId = Integer.MIN_VALUE;
//		this.opName = "";
//		this.netCDFName = "";
//		this.description = "";
//		this.pathToMatrix = "";
//		this.gtCode = null;
//		this.opSetSize = Integer.MIN_VALUE;
//		this.implicitSetSize = Integer.MIN_VALUE;
//		this.studyId = Integer.MIN_VALUE;
//		this.creationDate = new Date();
//	}

	public OperationMetadata(
			int id,
			int parentMatrixId,
			int parentOperationId,
			String opName,
			String netCDFName,
			String description,
			String pathToMatrix,
			OPType gtCode,
			int opSetSize,
			int implicitSetSize,
			int studyId,
			Date creationDate
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
		this.creationDate = (Date) creationDate.clone();
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

	public OPType getGenotypeCode() {
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

	public Date getCreationDate() {
		return creationDate;
	}
}

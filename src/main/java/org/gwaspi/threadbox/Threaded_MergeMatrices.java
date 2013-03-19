package org.gwaspi.threadbox;

import org.gwaspi.netCDF.operations.MatrixOperation;
import org.gwaspi.netCDF.operations.MergeAllMatrixOperation;
import org.gwaspi.netCDF.operations.MergeMarkersMatrixOperation;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_MergeMatrices extends AbstractThreaded_MergeMatrices {

	/**
	 * Whether to merge all, or only the marked samples
	 * TODO the second part of the previous sentence needs revising
	 */
	private final boolean all;

	public Threaded_MergeMatrices(
			int studyId,
			int parentMatrixId1,
			int parentMatrixId2,
			String newMatrixName,
			String description,
			boolean all)
	{
		super(
				studyId,
				parentMatrixId1,
				parentMatrixId2,
				newMatrixName,
				description);

		this.all = all;
	}

	@Override
	protected MatrixOperation createMatrixOperation() throws Exception {

		final MatrixOperation joinMatrices;

		if (all) {
			joinMatrices = new MergeAllMatrixOperation(
				studyId,
				parentMatrixId1,
				parentMatrixId2,
				newMatrixName,
				description);
		} else {
			joinMatrices = new MergeMarkersMatrixOperation(
				studyId,
				parentMatrixId1,
				parentMatrixId2,
				newMatrixName,
				description);
		}

		return joinMatrices;
	}
}

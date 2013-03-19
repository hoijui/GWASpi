package org.gwaspi.threadbox;

import org.gwaspi.netCDF.operations.MatrixMergeSamples;
import org.gwaspi.netCDF.operations.MatrixOperation;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_MergeMatricesAddSamples extends AbstractThreaded_MergeMatrices {

	public Threaded_MergeMatricesAddSamples(
			int studyId,
			int parentMatrixId1,
			int parentMatrixId2,
			String newMatrixName,
			String description)
	{
		super(
				studyId,
				parentMatrixId1,
				parentMatrixId2,
				newMatrixName,
				description);
	}

	@Override
	protected MatrixOperation createMatrixOperation() throws Exception {

		return new MatrixMergeSamples(
				studyId,
				parentMatrixId1,
				parentMatrixId2,
				newMatrixName,
				description);
	}
}

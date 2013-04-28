package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.OperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_HardyWeinberg extends CommonRunnable {

	private int censusOpId;

	public Threaded_HardyWeinberg(
			int matrixId,
			int censusOpId)
	{
		super(
				"Hardy-Weinberg",
				"Hardy-Weinberg test",
				"Hardy-Weinberg on Matrix ID: " + matrixId,
				"Hardy-Weinberg");

		this.censusOpId = censusOpId;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_HardyWeinberg.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		// HW ON GENOTYPE FREQ.
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)
				&& (censusOpId != Integer.MIN_VALUE))
		{
			int hwOpId = OperationManager.performHardyWeinberg(censusOpId, cNetCDF.Defaults.DEFAULT_AFFECTION);
			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, hwOpId);
		}
	}
}

package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.GWASpiExplorerNodes;
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
			String timeStamp,
			int matrixId,
			int censusOpId)
	{
		super("Hardy-Weinberg", timeStamp, "Hardy-Weinberg test", "Hardy-Weinberg");

		this.censusOpId = censusOpId;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_HardyWeinberg.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		// HW ON GENOTYPE FREQ.
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			if (censusOpId != Integer.MIN_VALUE) {
				int hwOpId = org.gwaspi.netCDF.operations.OperationManager.performHardyWeinberg(censusOpId, cNetCDF.Defaults.DEFAULT_AFFECTION);
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, hwOpId);
			}
		}
	}
}

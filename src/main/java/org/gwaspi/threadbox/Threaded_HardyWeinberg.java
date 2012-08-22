package org.gwaspi.threadbox;

import org.gwaspi.constants.cNetCDF;
import java.io.File;
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

	private int matrixId; // FIXME remove? copy paste left-over?
	private int censusOpId;
	private File phenotypeFile; // FIXME remove? copy paste left-over?

	public Threaded_HardyWeinberg(String threadName,
			String timeStamp,
			int matrixId,
			int censusOpId)
	{
		super(threadName, timeStamp, "Hardy-Weinberg test");

		this.matrixId = matrixId;
		this.censusOpId = censusOpId;
		this.phenotypeFile = phenotypeFile;

		startInternal("Hardy-Weinberg");
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_HardyWeinberg.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		// HW ON GENOTYPE FREQ.
		int hwOpId = Integer.MIN_VALUE;
		if (thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)) {
			if (censusOpId != Integer.MIN_VALUE) {
				hwOpId = org.gwaspi.netCDF.operations.OperationManager.performHardyWeinberg(censusOpId, cNetCDF.Defaults.DEFAULT_AFFECTION);
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, hwOpId);
			}
		}
	}
}

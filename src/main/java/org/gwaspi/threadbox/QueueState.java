package org.gwaspi.threadbox;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public enum QueueState {
	QUEUED,
	PROCESSING,
	DONE,
	ABORT,
	ERROR,
	DELETED;

	/**
	 * Checks whether a given state is one at the end of a life-cycle.
	 * @param queueState to be checked
	 * @return true, if queueState is necessarily at the end of a life-cycle.
	 */
	public static boolean isFinalizingState(QueueState queueState) {

		return ((queueState == QueueState.DONE)
				|| (queueState == QueueState.ABORT)
				|| (queueState == QueueState.ERROR));
	}
}

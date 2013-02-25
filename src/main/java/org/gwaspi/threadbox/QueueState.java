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

	public static boolean isFinalizingState(QueueState queueState) {

		return ((queueState == QueueState.DONE)
				|| (queueState == QueueState.ABORT)
				|| (queueState == QueueState.ERROR));
	}
}

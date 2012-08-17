package org.gwaspi.threadbox;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class QueueStates {

	public static final String QUEUED = "QUEUED";
	public static final String PROCESSING = "PROCESSING";
	public static final String DONE = "DONE";
	public static final String ABORT = "ABORT";
	public static final String ERROR = "ERROR";
	public static final String DELETED = "DELETED";

	private QueueStates() {
	}
}

package org.gwaspi.threadbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
class RunnableThread implements Runnable {

	private final Logger log = LoggerFactory.getLogger(RunnableThread.class);

	private Thread runner;

	RunnableThread() {
	}

	RunnableThread(String threadName) {
		runner = new Thread(this, threadName); // (1) Create a new thread.
		log.info(runner.getName());
		runner.start(); // (2) Start the thread.
	}

	public void run() {
		// Display info about this particular thread
		log.info(Thread.currentThread().toString());
	}
}

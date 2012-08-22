package org.gwaspi.threadbox;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
class RunnableThread implements Runnable {

	private Thread runner;

	RunnableThread() {
	}

	RunnableThread(String threadName) {
		runner = new Thread(this, threadName); // (1) Create a new thread.
		System.out.println(runner.getName());
		runner.start(); // (2) Start the thread.
	}

	public void run() {
		//Display info about this particular thread
		System.out.println(Thread.currentThread());
	}
}

package org.gwaspi.threadbox;

import org.gwaspi.global.Text;
import org.slf4j.Logger;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public abstract class CommonRunnable implements Runnable {

	private final Logger log;
	private final String timeStamp;
	private final String threadName;
	private final String taskDescription;
	private Thread runner;

	public CommonRunnable(String threadName, String timeStamp, String taskDescription) {
		this.log = createLog();
		this.timeStamp = timeStamp;
		this.threadName = threadName;
		this.taskDescription = taskDescription;
		this.runner = null;
	}

	protected final void startInternal(String startDescription) {

		try {
			org.gwaspi.global.Utils.sysoutStart(startDescription);
			org.gwaspi.global.Config.initPreferences(false, null);

			runner = new Thread(this, threadName); // (1) Create a new thread.
			runner.start(); // (2) Start the thread.
			runner.join();
		} catch (InterruptedException ex) {
			getLog().error(null, ex);
		}
	}

	protected abstract Logger createLog();

	protected Logger getLog() {
		return log;
	}

	protected abstract void runInternal(SwingWorkerItem thisSwi) throws Exception;

	public void run() {
		SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

		try {
			runInternal(thisSwi);

			// FINISH OFF
			if (!thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.ABORT)) {
				MultiOperations.printFinished("Performing " + taskDescription);
				SwingWorkerItemList.flagCurrentItemDone(timeStamp);
			} else {
				getLog().info("");
				getLog().info(Text.Processes.abortingProcess);
				getLog().info("Process Name: " + thisSwi.getSwingWorkerName());
				getLog().info("Process Launch Time: " + thisSwi.getLaunchTime());
				getLog().info("");
				getLog().info("");
			}

			MultiOperations.updateTree(); // XXX Threaded_ExportMatrix also had this here, others not
			MultiOperations.updateProcessOverviewStartNext();
		} catch (OutOfMemoryError ex) {
			getLog().error(Text.App.outOfMemoryError, ex);
		} catch (Exception ex) {
			MultiOperations.printError("Performing " + taskDescription);
			getLog().error(null, ex);
			try {
				SwingWorkerItemList.flagCurrentItemError(timeStamp);
				MultiOperations.updateTree();
				MultiOperations.updateProcessOverviewStartNext();
			} catch (Exception ex1) {
			}
		}
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	public String getTimeStamp() {
		return timeStamp;
	}
}

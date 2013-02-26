package org.gwaspi.threadbox;

import org.apache.felix.scr.annotations.Reference;
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
	/** This is visible in OS tools that list threads */
	private final String threadName;
	/** This is visible to the user in the GUI */
	private final String taskName;
	/** This is visible in the log */
	private final String taskDescription;
	/**
	 * This is visible in the log too.
	 * @deprecated Should probably be replaced by taskDescription
	 */
	private final String startDescription;
	private Throwable runException;
	@Reference
	private SwingWorkerItemList swingWorkerItemList;
	@Reference
	private MultiOperations multiOperations;

	protected void bindSwingWorkerItemList(SwingWorkerItemList swingWorkerItemList) {
		this.swingWorkerItemList = swingWorkerItemList;
	}

	protected void unbindSwingWorkerItemList(SwingWorkerItemList swingWorkerItemList) {

		if (this.swingWorkerItemList == swingWorkerItemList) {
			this.swingWorkerItemList = null;
		}
	}

	protected void bindMultiOperations(MultiOperations multiOperations) {
		this.multiOperations = multiOperations;
	}

	protected void unbindMultiOperations(MultiOperations multiOperations) {

		if (this.multiOperations == multiOperations) {
			this.multiOperations = null;
		}
	}

	public CommonRunnable(String threadName, String taskDescription, String taskName, String startDescription) {
		this.log = createLog();
		this.timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		this.threadName = threadName;
		this.taskName = taskName;
		this.taskDescription = taskDescription;
		this.startDescription = startDescription;
		this.runException = null;
	}

	protected abstract Logger createLog();

	protected Logger getLog() {
		return log;
	}

	protected abstract void runInternal(SwingWorkerItem thisSwi) throws Exception;

	@Override
	public void run() {

		org.gwaspi.global.Utils.sysoutStart(startDescription);
		org.gwaspi.global.Config.initPreferences(false, null);

		SwingWorkerItem thisSwi = swingWorkerItemList.getItemByTimeStamp(timeStamp);

		try {
			runInternal(thisSwi);

			// FINISH OFF
			if (!thisSwi.getQueueState().equals(QueueState.ABORT)) {
				MultiOperations.printFinished("Performing " + taskDescription);
				swingWorkerItemList.flagCurrentItemDone(timeStamp);
			} else {
				getLog().info("");
				getLog().info(Text.Processes.abortingProcess);
				getLog().info("Process Name: " + thisSwi.getTask().getTaskName());
				getLog().info("Process Launch Time: " + thisSwi.getLaunchTime());
				getLog().info("");
				getLog().info("");
			}

			multiOperations.updateTree(); // XXX Threaded_ExportMatrix also had this here, others not
			multiOperations.updateProcessOverviewStartNext();
		} catch (OutOfMemoryError ex) {
			runException = ex;
			getLog().error(Text.App.outOfMemoryError, ex);
		} catch (Exception ex) {
			runException = ex;
			MultiOperations.printError(taskDescription);
			getLog().error("Failed performing " + taskDescription, ex);
			try {
				swingWorkerItemList.flagCurrentItemError(timeStamp);
				multiOperations.updateTree();
				multiOperations.updateProcessOverviewStartNext();
			} catch (Exception ex1) {
				getLog().warn(null, ex1);
			}
		}
	}

	public String getThreadName() {
		return threadName;
	}

	public String getTaskName() {
		return taskName;
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public Throwable getRunException() {
		return runException;
	}
}

/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.threadbox;

import org.gwaspi.global.Text;
import org.gwaspi.progress.ProgressSource;
import org.slf4j.Logger;

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

	public CommonRunnable(String threadName, String taskDescription, String taskName, String startDescription) {
		this.log = createLog();
		this.timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		this.threadName = threadName;
		this.taskName = taskName;
		this.taskDescription = taskDescription;
		this.startDescription = startDescription;
	}

	protected abstract Logger createLog();

	public abstract ProgressSource getProgressSource();

	protected Logger getLog() {
		return log;
	}

	public static void doRunNowInThread(CommonRunnable task, SwingWorkerItem thisSwi) throws Exception {

		task.runInternal(thisSwi);
	}

	protected abstract void runInternal(SwingWorkerItem thisSwi) throws Exception;

	@Override
	public void run() {

		org.gwaspi.global.Utils.sysoutStart(startDescription);
		org.gwaspi.global.Config.initPreferences(false, null, null);

		SwingWorkerItem thisSwi = SwingWorkerItemList.getItemByTimeStamp(timeStamp);

		try {
			runInternal(thisSwi);

			// FINISH OFF
			if (thisSwi.getQueueState().equals(QueueState.ABORT)) {
				getLog().info("");
				getLog().info(Text.Processes.abortingProcess);
				getLog().info("Process Name: " + thisSwi.getTask().getTaskName());
				getLog().info("Process Launch Time: " + thisSwi.getLaunchTime());
				getLog().info("");
				getLog().info("");
			} else {
				MultiOperations.printFinished("Performing " + taskDescription);
				SwingWorkerItemList.flagCurrentItemDone(timeStamp);
			}

			MultiOperations.updateTree(); // XXX Threaded_ExportMatrix also had this here, others not
			MultiOperations.updateProcessOverviewStartNext();
		} catch (OutOfMemoryError ex) {
			getLog().error(Text.App.outOfMemoryError, ex);
		} catch (Exception ex) {
			MultiOperations.printError(taskDescription);
			getLog().error("Failed performing " + taskDescription, ex);
			try {
				SwingWorkerItemList.flagCurrentItemError(timeStamp);
				MultiOperations.updateTree();
				MultiOperations.updateProcessOverviewStartNext();
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
}

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
	/** This is visible in OS tools that list threads and in the log. */
	private final String name;
	/** This is visible to the user in the GUI. */
	private final String details;

	public CommonRunnable(String name, String details) {
		this.log = createLog();
		this.timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		this.name = name;
		this.details = details;
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
	public final void run() {

		try {
			org.gwaspi.global.Utils.sysoutStart(getDetailedName());
			org.gwaspi.global.Config.initPreferences(false, null, null);

			SwingWorkerItem thisSwi = SwingWorkerItemList.getItemByTimeStamp(timeStamp);

			runInternal(thisSwi);

			// FINISH OFF
			if (thisSwi.getQueueState().equals(QueueState.ABORT)) {
				getLog().info("");
				getLog().info(Text.Processes.abortingProcess);
				getLog().info("Process Name: " + thisSwi.getTask().getDetailedName());
				getLog().info("Process Launch Time: " + thisSwi.getLaunchTime());
				getLog().info("");
				getLog().info("");
			} else {
				MultiOperations.printFinished("Performing " + getDetailedName());
				SwingWorkerItemList.flagCurrentItemDone(timeStamp);
			}

			MultiOperations.updateTree(); // XXX Threaded_ExportMatrix also had this here, others not
			MultiOperations.updateProcessOverviewStartNext();
		} catch (Throwable thr) {
			MultiOperations.printError(getName());
			if (thr instanceof OutOfMemoryError) {
				getLog().error(Text.App.outOfMemoryError);
			}
			getLog().error("Failed performing " + getName(), thr);
			try {
				SwingWorkerItemList.flagCurrentItemError(timeStamp);
				MultiOperations.updateTree();
				MultiOperations.updateProcessOverviewStartNext();
			} catch (Exception ex1) {
				getLog().warn("Failed flagging items with state 'error'", ex1);
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getDetailedName() {
		return name + " " + details;
	}

	public String getTimeStamp() {
		return timeStamp;
	}
}

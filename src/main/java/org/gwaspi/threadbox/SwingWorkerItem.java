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

import java.util.Date;
import org.gwaspi.global.Text;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for a task that adds managing functionality.
 * Probably most importantly, it allows to interrupt the task
 * with {@link Thread#interrupt()}, does cleanup (TODO!),
 * and sets the status to ABORTED.
 */
public class SwingWorkerItem implements Task {

	private final Logger log = LoggerFactory.getLogger(SwingWorkerItem.class);

	private final CommonRunnable task;
	private final Date createTime;
	private Date startTime;
	private Date endTime;
	private QueueState queueStatus;

	SwingWorkerItem(final CommonRunnable task) {

		this.createTime = new Date();
		this.startTime = null;
		this.endTime = null;
		this.task = task;
		this.queueStatus = QueueState.CREATED;
	}

//	public static ProcessStatus toProcessStatus(QueueState queueState) {
//
//		switch (queueState) {
//			case ABORT: return ProcessStatus.ABORTED;
//			case DELETED: return null;
//			case DONE: return ProcessStatus.COMPLEETED;
//			case ERROR: return ProcessStatus.FAILED;
//			case PROCESSING: return ProcessStatus.RUNNING;
//			case QUEUED: return ProcessStatus.INITIALIZING;
//			default: return null;
//		}
//	}

	@Override
	public QueueState getStatus() {
		return queueStatus;
	}

	@Override
	public Date getCreateTime() {
		return createTime;
	}

	@Override
	public Date getStartTime() {
		return startTime;
	}

	@Override
	public void setStartTime(final Date startTime) {

		if (this.startTime != null) {
			throw new IllegalStateException("start-time is already set");
		}

		this.startTime = startTime;
	}

	@Override
	public Date getEndTime() {
		return endTime;
	}

	@Override
	public void setEndTime(final Date endTime) {

		if (this.endTime != null) {
			throw new IllegalStateException("end-time is already set");
		}

		this.endTime = endTime;
	}

	public CommonRunnable getTask() {
		return task;
	}

	@Override
	public void setStatus(final QueueState queueStatus) {

		if ((startTime == null) && queueStatus.equals(QueueState.PROCESSING)) {
			setStartTime(new Date());
		} else if ((endTime == null) && queueStatus.equals(QueueState.DONE)) {
			setEndTime(new Date());
		}
		this.queueStatus = queueStatus;

//		if (task.getProgressSource() instanceof ProgressHandler) {
//			final ProgressHandler progressHandler = (ProgressHandler) task.getProgressSource(); // HACK
//			progressHandler.setNewStatus(SwingWorkerItem.toProcessStatus(queueStatus));
//		} else {
//			log.warn("Non-serious program code problem detected: {}\nMore Info: '{}' '{}' '{}'",
//					"We can not report failure of the process to the progress API.",
//					task.getName(),
//					task.getDetailedName(),
//					task.getProgressSource().getClass().getCanonicalName());
//		}
	}

	@Override
	public String getName() {
		return getTask().getName();
	}

	@Override
	public String getDescription() {
		return getTask().getDetailedName();
	}

	@Override
	public ProgressSource getProgressSource() {
		return getTask().getProgressSource();
	}

	@Override
	public TaskLockProperties getTaskLockProperties() {
		return getTask().getTaskLockProperties();
	}

	@Override
	public void run() {

		SwingWorkerItem thisSwi = null;
		try {
			org.gwaspi.global.Utils.sysoutStart(getTask().getDetailedName());
			org.gwaspi.global.Config.initPreferences(false, null, null); // XXX this should probably not be here.. we should ensure initialized preferences before

			// NOTE ABORTION_POINT We could be gracefully abort here

			getTask().run();
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Abortion of the task was requested");
			}
		} catch (InterruptedException thr) {
			// NOTE We receive this exception only in certain special cases,
			//   NOT always when Thread#interrupt() is called on us!
			getTask().getProgressHandler().setNewStatus(ProcessStatus.ABORTED);
		} catch (Throwable thr) {
			getTask().getProgressHandler().setNewStatus(ProcessStatus.FAILED);

			MultiOperations.printError(getName());
			if (thr instanceof OutOfMemoryError) {
				log.error(Text.App.outOfMemoryError);
			}
			log.error("Failed performing " + getName(), thr);
			try {
				setStatus(QueueState.DONE);
			} catch (Exception ex1) {
				log.warn("Failed flagging items with state 'error': " + getName(), ex1);
			}
		}

		// FINISH OFF
		if (getProgressSource().getStatus().isBad()) {
			log.info("");
			log.info(Text.Processes.abortingProcess);
			log.info("Process Name: " + getName());
			log.info("Process Launch Time: " + org.gwaspi.global.Utils.getShortDateTimeAsString(thisSwi.getCreateTime()));
			log.info("");
			log.info("");
		} else {
			MultiOperations.printFinished("Performing " + getTask().getDetailedName());
			setStatus(QueueState.DONE);
		}

	}
}

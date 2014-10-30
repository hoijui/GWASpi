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

import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwingWorkerItem {

	private final Logger log = LoggerFactory.getLogger(SwingWorkerItem.class);

	private final CommonRunnable task;
	private final String launchTime;
	private String startTime;
	private String endTime;
	private QueueState queueState;

	SwingWorkerItem(final CommonRunnable task) {

		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.startTime = null;
		this.endTime = null;
		this.task = task;
		this.queueState = QueueState.QUEUED;
	}

	public static ProcessStatus toProcessStatus(QueueState queueState) {

		switch (queueState) {
			case ABORT: return ProcessStatus.ABORTED;
			case DELETED: return null;
			case DONE: return ProcessStatus.COMPLEETED;
			case ERROR: return ProcessStatus.FAILED;
			case PROCESSING: return ProcessStatus.RUNNING;
			case QUEUED: return ProcessStatus.INITIALIZING;
			default: return null;
		}
	}

	public QueueState getQueueState() {
		return queueState;
	}

	public ProcessStatus getStatus() {
		return toProcessStatus(getQueueState());
	}

	public boolean isCurrent() {
		return ((queueState == QueueState.QUEUED) || (queueState == QueueState.PROCESSING));
	}

	public String getLaunchTime() {
		return launchTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public CommonRunnable getTask() {
		return task;
	}

	public String getTimeStamp() {
		return task.getTimeStamp();
	}

	public void setQueueState(QueueState queueState) {

		if ((startTime == null) && queueState.equals(QueueState.PROCESSING)) {
			setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
		} else if ((endTime == null) && QueueState.isFinalizingState(queueState)) {
			setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
		}
		this.queueState = queueState;

		if (task.getProgressSource() instanceof ProgressHandler) {
			final ProgressHandler progressHandler = (ProgressHandler) task.getProgressSource(); // HACK
			progressHandler.setNewStatus(SwingWorkerItem.toProcessStatus(queueState));
		} else {
			log.warn("Non-serious program code problem detected: {}\nMore Info: '{}' '{}' '{}'",
					"We can not report failure of the process to the progress API.",
					task.getName(),
					task.getDetailedName(),
					task.getProgressSource().getClass().getCanonicalName());
		}
	}

	private void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	private void setStartTime(String startTime) {
		this.startTime = startTime;
	}
}

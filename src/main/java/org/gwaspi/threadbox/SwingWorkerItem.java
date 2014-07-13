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


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.gwaspi.progress.AbstractProgressSource;
import org.gwaspi.progress.ProcessStatus;

public class SwingWorkerItem extends AbstractProgressSource {

	private final CommonRunnable task;
	private final String launchTime;
	private String startTime;
	private String endTime;
	private QueueState queueState;
	private final Collection<Integer> parentStudyIds;
	private final Collection<Integer> parentMatricesIds;
	private final Collection<Integer> parentOperationsIds;

	SwingWorkerItem(
			CommonRunnable swingWorker,
			Integer[] parentStudyIds)
	{
		this(swingWorker, parentStudyIds, new Integer[] {});
	}

	SwingWorkerItem(
			CommonRunnable swingWorker,
			Integer[] parentStudyIds,
			Integer[] parentMatricesIds)
	{
		this(swingWorker, parentStudyIds, parentMatricesIds, new Integer[] {});
	}

	SwingWorkerItem(
			CommonRunnable task,
			Integer[] parentStudyIds,
			Integer[] parentMatricesIds,
			Integer[] parentOperationsIds)
	{
		super(task.getProgressSource().getInfo(), task.getProgressSource().getNumIntervals());

		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.task = task;
		this.queueState = QueueState.QUEUED;
		this.parentStudyIds = Collections.unmodifiableCollection(Arrays.asList(parentStudyIds));
		this.parentMatricesIds = Collections.unmodifiableCollection(Arrays.asList(parentMatricesIds));
		this.parentOperationsIds = Collections.unmodifiableCollection(Arrays.asList(parentOperationsIds));
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

	public Collection<Integer> getParentMatricesIds() {
		return parentMatricesIds;
	}

	public Collection<Integer> getParentOperationsIds() {
		return parentOperationsIds;
	}

	public Collection<Integer> getParentStudyIds() {
		return parentStudyIds;
	}

	public String getTimeStamp() {
		return task.getTimeStamp();
	}

	public void setQueueState(QueueState queueState) {

		this.queueState = queueState;
		fireStatusChanged(SwingWorkerItem.toProcessStatus(queueState));
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
}

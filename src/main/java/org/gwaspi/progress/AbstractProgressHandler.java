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

package org.gwaspi.progress;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProgressHandler<ST> implements ProgressHandler<ST> {

	private final List<ProgressListener> progressListeners;
	private final ProcessInfo processInfo;
	private Integer numIntervals;
	private long startTime;
	private long endTime;
	private int nextEventIndex;
	private ProcessStatus currentStatus;

	protected AbstractProgressHandler(ProcessInfo processInfo, Integer numIntervals) {

		if (numIntervals <= 0) {
			throw new IllegalStateException("a progress has to consist of one or more intervals (is " + numIntervals + ")");
		}

		this.progressListeners = new ArrayList<ProgressListener>(1);
		this.processInfo = processInfo;
		this.numIntervals = numIntervals;
		this.nextEventIndex = 0;
		this.startTime = -1;
		this.endTime = -1;
		this.currentStatus = null;
	}

	protected int getNextEventIndex() {
		return nextEventIndex;
	}

	@Override
	public ProcessInfo getInfo() {
		return processInfo;
	}

	@Override
	public Integer getNumIntervals() {
		return numIntervals;
	}

	@Override
	public void setNumIntervals(Integer numIntervals) {

		this.numIntervals = numIntervals;
		fireProcessDetailsChanged();
	}

	@Override
	public void addProgressListener(ProgressListener lst) {
		progressListeners.add(lst);
	}

	@Override
	public void removeProgressListener(ProgressListener lst) {
		progressListeners.remove(lst);
	}

	@Override
	public List<ProgressListener> getProgressListeners() {
		return progressListeners;
	}

	protected void fireProcessDetailsChanged() {

		ProcessDetailsChangeEvent evt = new ProcessDetailsChangeEvent(this);
		for (ProgressListener progressListener : progressListeners) {
			progressListener.processDetailsChanged(evt);
		}
	}

	@Override
	public ProcessStatus getStatus() {
		return currentStatus;
	}

	protected void fireStatusChanged(ProcessStatus newStatus) {

		ProcessStatusChangeEvent evt = new ProcessStatusChangeEvent(this, newStatus);
		currentStatus = newStatus;
		for (ProgressListener progressListener : progressListeners) {
			progressListener.statusChanged(evt);
		}
	}

	protected void fireProgressHappened(Double completionFraction, ST currentState) {

		final long time = System.currentTimeMillis() - startTime;
		fireProgressHappened(new ProgressEvent(
				this,
				time,
				nextEventIndex,
				completionFraction,
				currentState));
	}

	protected void fireProgressHappened(ProgressEvent evt) {

		for (ProgressListener progressListener : progressListeners) {
			progressListener.progressHappened(evt);
		}
		nextEventIndex++;
	}

	@Override
	public void setNewStatus(ProcessStatus newStatus) {
		fireStatusChanged(newStatus);
	}

//	@Override
//	public void starting() {
//		fireStatusChanged(ProcessStatus.INITIALIZING);
//	}
//
//	@Override
//	public void initialized() {
//		fireStatusChanged(ProcessStatus.RUNNING);
//	}
//
//	@Override
//	public void finalized() {
//		fireStatusChanged(ProcessStatus.COMPLEETED);
//	}
}

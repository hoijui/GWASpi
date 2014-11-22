/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

public abstract class AbstractProgressSource<ST> implements ProgressSource<ST> {

	private final List<ProgressListener> progressListeners;
	private ProcessInfo processInfo;
	private Integer numIntervals;
	private long startTime;
	private long endTime;
	private int nextEventIndex;
	private ProcessStatus currentStatus;

	protected AbstractProgressSource(ProcessInfo processInfo, Integer numIntervals) {

//		if (numIntervals <= 0) {
//			throw new IllegalStateException("a progress has to consist of one or more intervals (is " + numIntervals + ")");
//		}

		this.progressListeners = new ArrayList<ProgressListener>(1);
		this.processInfo = processInfo;
		this.numIntervals = numIntervals;
		this.nextEventIndex = 0;
		this.startTime = -1;
		this.endTime = -1;
		this.currentStatus = ProcessStatus.NONE;
	}

	protected int getNextEventIndex() {
		return nextEventIndex;
	}

	protected void setInfo(ProcessInfo processInfo) {
		this.processInfo = processInfo;
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

	protected void fireProcessDetailsChanged(final ProcessDetailsChangeEvent evt) {

		for (ProgressListener progressListener : progressListeners) {
			progressListener.processDetailsChanged(evt);
		}
	}

	protected void fireProcessDetailsChanged() {

		fireProcessDetailsChanged(new ProcessDetailsChangeEvent(this));
	}

	@Override
	public ProcessStatus getStatus() {
		return currentStatus;
	}

	protected void fireStatusChanged(final ProcessStatusChangeEvent evt) {

		currentStatus = evt.getNewStatus();
		// Using this copy of the list prevents ConcurrentModificationException's
		// when we try to add to or remove from the list of listeners as a result
		// of the status changing.
		final List<ProgressListener> progressListenersCopy
				= new ArrayList<ProgressListener>(progressListeners);
		for (ProgressListener progressListener : progressListenersCopy) {
			progressListener.statusChanged(evt);
		}
	}

	protected void fireStatusChanged(ProcessStatus newStatus) {

		if (currentStatus == newStatus) {
			return;
		}
		fireStatusChanged(new ProcessStatusChangeEvent(this, newStatus));
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
}

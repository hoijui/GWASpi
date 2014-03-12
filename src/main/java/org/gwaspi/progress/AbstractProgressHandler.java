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
	private final String shortName;
	private final Integer numIntervals;
	private long startTime;
	private long endTime;
	private int nextEventIndex;

	protected AbstractProgressHandler(String shortName, Integer numIntervals) {

		if (numIntervals <= 0) {
			throw new IllegalStateException("a progress has to consist of one or more intervals (is " + numIntervals + ")");
		}

		this.progressListeners = new ArrayList<ProgressListener>(1);
		this.shortName = shortName;
		this.numIntervals = numIntervals;
		this.nextEventIndex = 0;
		this.startTime = -1;
		this.endTime = -1;
	}

	protected AbstractProgressHandler() {
		this("generic process", null);
	}

	protected int getNextEventIndex() {
		return nextEventIndex;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public Integer getNumIntervals() {
		return numIntervals;
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

	protected void fireProcessStarted() {

		startTime = System.currentTimeMillis();
		for (ProgressListener progressListener : progressListeners) {
			progressListener.processStarted();
		}
	}

	protected void fireProcessInitialized() {

		for (ProgressListener progressListener : progressListeners) {
			progressListener.processInitialized();
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

	protected void fireProcessEnded() {

		endTime = System.currentTimeMillis();
		for (ProgressListener progressListener : progressListeners) {
			progressListener.processEnded();
		}
	}

	protected void fireProcessFinalized() {

		endTime = System.currentTimeMillis();
		for (ProgressListener progressListener : progressListeners) {
			progressListener.processFinalized();
		}
	}

	@Override
	public void starting() {
		fireProcessStarted();
	}

	@Override
	public void initialized() {
		fireProcessInitialized();
	}

	@Override
	public void finalized() {
		fireProcessFinalized();
	}
}

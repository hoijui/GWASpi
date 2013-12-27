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

public class AbstractProgressSource<ST> implements ProgressSource<ST> {

	private final List<ProgressListener> progressListeners;
	private final Integer numInterfalls;
	private long startTime;
	private long endTime;
	private int nextEventIndex;

	protected AbstractProgressSource(Integer numInterfalls) {

		this.progressListeners = new ArrayList<ProgressListener>(1);
		this.numInterfalls = numInterfalls;
		this.nextEventIndex = 0;
		this.startTime = -1;
		this.endTime = -1;
	}

	protected AbstractProgressSource() {
		this(null);
	}

	@Override
	public Integer getNumIntervalls() {
		return numInterfalls;
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

		startTime = System.currentTimeMillis();
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
}

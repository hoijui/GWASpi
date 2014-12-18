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

import java.util.EventObject;

/**
 * Signals a progression in progress.
 * The TODO
 */
public class ProgressEvent<S> extends EventObject {

	/**
	 * Wall Clock Time passed since the start of the process.
	 */
	private final long time;
	/**
	 * Serial event number/index.
	 */
	private final int intervalIndex;
	/**
	 * The fraction of completion of the process.
	 * This is either a value in (0.0, 1.0],
	 * or <code>null</code>,
	 * if it is unknown (because the final state is unknown).
	 */
	private final Double completionFraction;
	/**
	 * Current state of progress.
	 * This will usually be an Integer, Long or a Double,
	 * but could also be an <code>enum</code>, for example.
	 */
	private final S currentState;

	public ProgressEvent(
			ProgressSource source,
			long time,
			int intervalIndex,
			double completionFraction,
			S currentState)
	{
		super(source);

		this.time = time;
		this.intervalIndex = intervalIndex;
		this.completionFraction = completionFraction;
		this.currentState = currentState;
	}

	@Override
	public ProgressSource getSource() {
		return (ProgressSource) super.getSource();
	}

	/**
	 * Wall Clock Time passed since the start of the process.
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Serial event number/index.
	 * @return the intervalIndex
	 */
	public int getIntervalIndex() {
		return intervalIndex;
	}

	/**
	 * The fraction of completion of the process.
	 * This is either a value in (0.0, 1.0],
	 * or <code>null</code>,
	 * if it is unknown (because the final state is unknown).
	 * @return the completionFraction
	 */
	public Double getCompletionFraction() {
		return completionFraction;
	}

	/**
	 * Current state of progress.
	 * This will usually be an Integer, Long or a Double,
	 * but could also be an <code>enum</code>, for example.
	 * @return the currentState
	 */
	public S getCurrentState() {
		return currentState;
	}
}

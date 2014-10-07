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

public class IntegerProgressHandler extends AbstractProgressHandler<Integer> {

	private Integer startState;
	private Integer endState;

	public IntegerProgressHandler(ProcessInfo processInfo, Integer startState, Integer endState) {
		super(processInfo, calculateNumIntervalls(startState, endState));

		this.startState = startState;
		this.endState = endState;
	}

	private static Integer calculateNumIntervalls(final Integer startState, final Integer endState) {

		if (
				(startState == null)
				|| (startState < 0)
				|| (endState == null)
				|| (endState < 0)
				|| (startState.equals(endState)))
		{
			// we can not calculate a #intervals that makes sense
			return null;
		} else {
			return Math.abs(endState - startState) + 1;
		}
	}

	private void recalculateNumIntervalls() {
		setNumIntervals(calculateNumIntervalls(startState, endState));
	}

	public Integer getStartState() {
		return startState;
	}

	public void setStartState(final Integer startState) {

		this.startState = startState;
		recalculateNumIntervalls();
	}

	public Integer getEndState() {
		return endState;
	}

	public void setEndState(final Integer endState) {

		this.endState = endState;
		recalculateNumIntervalls();
	}

	@Override
	public void setProgress(Integer currentState) {

		fireProgressHappened(currentState);
		if (currentState.equals(endState)) {
			fireStatusChanged(ProcessStatus.COMPLEETED);
		}
	}

	protected void fireProgressHappened(Integer currentState) {

		final Double completionFraction;
		if (endState.equals(startState)) {
			// We have to handle this case (only one interval) separately,
			// to prevent div-by-0 -> NaN.
			completionFraction = currentState.equals(endState) ? 1.0 : 0.0;
		} else if (getNumIntervals() == null) {
			completionFraction = 0.5;
		} else {
			completionFraction = (double) (Math.abs(currentState - startState) + 1) / getNumIntervals();
		}
		fireProgressHappened(completionFraction, currentState);
	}
}

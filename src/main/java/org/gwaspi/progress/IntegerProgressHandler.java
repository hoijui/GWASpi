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

	private final Integer startState;
	private final Integer endState;

	public IntegerProgressHandler(String shortName, Integer startState, Integer endState) {
		super(shortName, Math.abs(endState - startState) + 1);

		this.startState = startState;
		this.endState = endState;
	}

	public Integer getStartState() {
		return startState;
	}

	public Integer getEndState() {
		return endState;
	}

	@Override
	public void setProgress(Integer currentState) {

		fireProgressHappened(currentState);
		if (currentState.equals(endState)) {
			fireProcessEnded();
		}
	}

	protected void fireProgressHappened(Integer currentState) {

		final Double completionFraction;
		if (endState.equals(startState)) {
			// We have to handle this case (only one interval) separately,
			// to prevent div-by-0 -> NaN.
			completionFraction = currentState.equals(endState) ? 1.0 : 0.0;
		} else {
			completionFraction = (double) Math.abs((double) (currentState - startState) / getNumIntervals());
		}
		fireProgressHappened(completionFraction, currentState);
	}
}

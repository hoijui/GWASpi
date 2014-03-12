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

public class DoubleProgressHandler extends AbstractProgressHandler<Double> {

	private final Double startState;
	private final Double endState;
	private final Double difference;

	public DoubleProgressHandler(String shortName, Double startState, Double endState) {
		super(shortName, null);

		this.startState = startState;
		this.endState = endState;
		this.difference = (endState - startState);
	}

	@Override
	public void setProgress(Double currentState) {

		fireProgressHappened(currentState);
		if (currentState >= endState) { // XXX maybe this poses a problem, if the endState is actually reached, but the precise value is a tiny bit smaller then the expected end-state value
			fireProcessEnded();
		}
	}

	protected void fireProgressHappened(Double currentState) {

		final Double completionFraction;
		if (endState == startState) {
			// We have to handle this case (only one interval) separately,
			// to prevent div-by-0 -> NaN.
			completionFraction = (currentState == endState) ? 1.0 : 0.0; // XXX maybe this poses a problem, if the endState is actually reached, but the precise value is a tiny bit smaller then the expected end-state value
		} else {
			completionFraction = (double) Math.abs((currentState - startState) / difference);
		}
		fireProgressHappened(completionFraction, currentState);
	}
}

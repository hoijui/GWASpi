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
		super(shortName, Math.abs(endState - startState));

		this.startState = startState;
		this.endState = endState;
	}

	@Override
	public void setProgress(Integer currentState) {

		fireProgressHappened(currentState);
		if (currentState == endState) {
			fireProcessEnded();
		}
	}

	protected void fireProgressHappened(Integer currentState) {

		final Double completionFraction = (double) (currentState - startState) / getNumIntervalls();
		fireProgressHappened(completionFraction, currentState);
	}
}

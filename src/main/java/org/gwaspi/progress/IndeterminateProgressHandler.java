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

/**
 * This is to be used for a process that goes through the different statuses,
 * but does not supply any intermediate progress reporting.
 */
public class IndeterminateProgressHandler extends AbstractProgressHandler<Object> {

	public IndeterminateProgressHandler(ProcessInfo processInfo) {
		super(processInfo, null);
	}

	@Override
	public void setProgress(Object currentState) {
		throw new UnsupportedOperationException(
				"This progress handler does by design not support progress reporting");
	}
}

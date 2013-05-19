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

package org.gwaspi.threadbox;

public enum QueueState {
	QUEUED,
	PROCESSING,
	DONE,
	ABORT,
	ERROR,
	DELETED;

	/**
	 * Checks whether a given state is one at the end of a life-cycle.
	 * @param queueState to be checked
	 * @return true, if queueState is necessarily at the end of a life-cycle.
	 */
	public static boolean isFinalizingState(QueueState queueState) {

		return ((queueState == QueueState.DONE)
				|| (queueState == QueueState.ABORT)
				|| (queueState == QueueState.ERROR));
	}
}

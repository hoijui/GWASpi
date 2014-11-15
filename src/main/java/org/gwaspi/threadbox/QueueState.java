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
	/** just created, not yet added to a queue */
	CREATED,
	/** is to be done somewhen in the future */
	QUEUED,
	/**
	 * has unsatisfied requirements.
	 * For example, an operation, outputting data that is to be read, is not yet done.
	 */
	UNSATIFIED,
	/** awaiting free processing capabilities (for example a free slot in a thread-set) */
	SCHEDULED,
	/** currently being processed in a thread */
	PROCESSING,
	/** currently being processed in a thread, but not actually receiving CPU time */
	PAUSED,
	/** done processing */
	DONE,
	/** removed from the queue */
	REMOVED;
}

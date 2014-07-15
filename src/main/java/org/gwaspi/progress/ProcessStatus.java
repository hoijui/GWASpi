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
 * Every process adheres to one of these statuses at any time.
 *
 * NOTE
 * Why "Status" instead of "State"?
 * As a first approximation, one can think of State as "analog" and of Status
 * as "digital". The State of an object can be anything, like the answer to "how are you today?
 * While the Status of an object can only be one out of a finite set of Statuses.
 * See a similar explanation in other words here:
 * {@see http://forum.wordreference.com/showthread.php?t=287984&s=0d5454f63863bcb10cb775d94faef585&p=3423244#post3423244}
 * ... while it is a different thing if we move away from IT:
 * {@see https://answers.yahoo.com/question/index?qid=20070427185233AAcmFY5}
 */
public enum ProcessStatus {
	NONE(false, false, false),
	INITIALIZING(true, false, false),
	RUNNING(true, false, false),
	PAUSED(false, false, false),
	ABORTED(false, true, true),
	FAILED(false, true, true),
	FINALIZING(true, false, false),
	COMPLEETED(false, false, true);

	private final boolean active;
	private final boolean bad;
	private final boolean end;

	private ProcessStatus(final boolean active, final boolean bad, final boolean end) {

		this.active = active;
		this.bad = bad;
		this.end = end;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isBad() {
		return bad;
	}

	public boolean isEnd() {
		return end;
	}
}
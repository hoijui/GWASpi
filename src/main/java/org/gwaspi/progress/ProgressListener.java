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

import java.util.EventListener;

/**
 * Is interested in {@link ProgressEvent}'s of (a) processes(es).
 */
public interface ProgressListener<ST> extends EventListener {

	/**
	 * Signals that the process started.
	 */
	void processStarted();

	/**
	 * Signals that the process advanced.
	 * @param evt contains details about the current state of progress.
	 */
	void progressHappened(ProgressEvent<ST> evt);

	/**
	 * Signals that the process ended.
	 */
	void processFinished();
}

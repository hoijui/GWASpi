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

import java.util.List;

/**
 * Produces {@link ProgressEvent}'s.
 * This is basically a process/task which can be run.
 * @param <ST> the status type
 */
public interface ProgressSource<ST> {

	/**
	 * Returns the number of total progress intervals this source produces,
	 * if it is known.
	 * @return number of progress intervals produced,
	 *   or <code>null</code>, if unknown.
	 */
	Integer getNumIntervalls();

	void addProgressListener(ProgressListener lst);

	void removeProgressListener(ProgressListener lst);

	List<ProgressListener> getProgressListeners();
}

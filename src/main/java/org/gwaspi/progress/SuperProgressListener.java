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
 * Is interested in {@link ProgressEvent}'s of (a) processes(es) with sub-processes.
 * @param <ST> the status type
 */
public interface SuperProgressListener<ST> extends ProgressListener<ST> {

	/**
	 * Signals that a sub process has been added.
	 * @param evt contains the added sub-process
	 */
	void subProcessAdded(SubProcessAddedEvent evt);

	/**
	 * Signals that a sub process has been removed.
	 * @param evt contains the removed sub-process
	 */
	void subProcessRemoved(SubProcessRemovedEvent evt);

	/**
	 * Signals that a sub process has been replaced.
	 * @param evt contains the replaced and the replacing sub-processes
	 */
	void subProcessReplaced(SubProcessReplacedEvent evt);
}

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

package org.gwaspi.threadbox;

import java.util.EventListener;

/**
 * Is interested in tasks being registered and deleted.
 */
public interface TasksListener extends EventListener {

	/**
	 * Signals that a new task was registered.
	 * @param evt contains details about the task at stake
	 */
	void taskRegistered(TaskEvent evt);

//	/**
//	 * Signals that a task was deleted.
//	 * @param evt contains details about the task at stake
//	 */
//	void taskDeleted(TaskEvent evt);
}

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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Contains all the info that defines what things might be locked by a task.
 * Meaning, that while the task is running, it might alter these things,
 * and thus, other parts of the code, other threads, are not allowed
 * to change these things.
 */
public class TaskLockProperties {

	private final Collection<Integer> studyIds;
	private final Collection<Integer> matricesIds;
	private final Collection<Integer> operationsIds;

	public TaskLockProperties() {

		this.studyIds = new ArrayList<Integer>();
		this.matricesIds = new ArrayList<Integer>();
		this.operationsIds = new ArrayList<Integer>();
	}

	public Collection<Integer> getMatricesIds() {
		return matricesIds;
	}

	public Collection<Integer> getOperationsIds() {
		return operationsIds;
	}

	public Collection<Integer> getStudyIds() {
		return studyIds;
	}
}

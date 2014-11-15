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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.StudyKey;

public class TaskDependencyHandler {

	private final Lock addRemoveLock;
	private final List<Integer> lockedStudiesIds;
	private final List<Integer> lockedMatricesIds;
	private final List<Integer> lockedOperationsIds;

	public TaskDependencyHandler() {

		this.addRemoveLock = new ReentrantLock();
		this.lockedStudiesIds = new ArrayList<Integer>();
		this.lockedMatricesIds = new ArrayList<Integer>();
		this.lockedOperationsIds = new ArrayList<Integer>();
	}

	public void add(final Task task) {

		addRemoveLock.lock();
		try {
			final TaskLockProperties taskLockProperties = task.getTaskLockProperties();
			lockedStudiesIds.addAll(taskLockProperties.getStudyIds());
			lockedMatricesIds.addAll(taskLockProperties.getMatricesIds());
			lockedOperationsIds.addAll(taskLockProperties.getOperationsIds());
		} finally {
			addRemoveLock.unlock();
		}
	}

	private static <T> void removeAllExactlyOnce(final Collection<T> collection, final Collection<T> toBeRemoved) {

		for (final T toBeRemovedItem : toBeRemoved) {
			if (!collection.remove(toBeRemovedItem)) {
				throw new IllegalStateException("Failed to remove element " + toBeRemovedItem.toString());
			}
		}
	}

	public void remove(final Task task) {

		addRemoveLock.lock();
		try {
			final TaskLockProperties taskLockProperties = task.getTaskLockProperties();
			// NOTE We can not use List#removeAll(Collection),
			//   because it removes all instances of all elements,
			//   though we want to remove each ID only as many times as it is locked by the task,
			//   which will be once, in practise.
//			lockedStudiesIds.removeAll(taskLockProperties.getStudyIds());
//			lockedMatricesIds.removeAll(taskLockProperties.getMatricesIds());
//			lockedOperationsIds.removeAll(taskLockProperties.getOperationsIds());
			removeAllExactlyOnce(lockedStudiesIds, taskLockProperties.getStudyIds());
			removeAllExactlyOnce(lockedMatricesIds, taskLockProperties.getMatricesIds());
			removeAllExactlyOnce(lockedOperationsIds, taskLockProperties.getOperationsIds());
		} finally {
			addRemoveLock.unlock();
		}
	}

	public boolean permitsDeletionOf(StudyKey studyKey) {
		return !lockedStudiesIds.contains(studyKey.getId());
	}

	public boolean permitsDeletionOf(MatrixKey matrixKey) {
		return !lockedMatricesIds.contains(matrixKey.getMatrixId());
	}

	public boolean permitsDeletionOf(OperationKey operationKey) {
		return !lockedOperationsIds.contains(operationKey.getId());
	}
}

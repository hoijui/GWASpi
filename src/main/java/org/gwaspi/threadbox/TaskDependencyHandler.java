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
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.Identifier;
import org.gwaspi.model.StudyKey;

public class TaskDependencyHandler {

	private final Lock addRemoveLock;
	private final List<Identifier<StudyKey>> lockedStudies;
	private final List<Identifier<DataSetKey>> lockedDataSets;
	private final List<Identifier<StudyKey>> toBeRemovedStudies;
	private final List<Identifier<DataSetKey>> toBeRemovedDataSets;

	public TaskDependencyHandler() {

		this.addRemoveLock = new ReentrantLock();
		this.lockedStudies = new ArrayList<Identifier<StudyKey>>();
		this.lockedDataSets = new ArrayList<Identifier<DataSetKey>>();
		this.toBeRemovedStudies = new ArrayList<Identifier<StudyKey>>();
		this.toBeRemovedDataSets = new ArrayList<Identifier<DataSetKey>>();
	}

	/**
	 * Adds a task to the list of to-be-done tasks.
	 * @param task to be done
	 */
	public void add(final Task task) {

		addRemoveLock.lock();
		try {
			final TaskLockProperties taskLockProperties = task.getTaskLockProperties();
			lockedStudies.addAll(taskLockProperties.getRequiredStudies());
			lockedDataSets.addAll(taskLockProperties.getRequiredDataSets());
			toBeRemovedStudies.addAll(taskLockProperties.getRemovingStudies());
			toBeRemovedDataSets.addAll(taskLockProperties.getRemovingDataSets());
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

	/**
	 * Removes a task to the list of to-be-done tasks.
	 * Either because it was done or aborted.
	 * @param task no longer to be done
	 */
	public void remove(final Task task) {

		addRemoveLock.lock();
		try {
			final TaskLockProperties taskLockProperties = task.getTaskLockProperties();
			// NOTE We can not use List#removeAll(Collection),
			//   because it removes all instances of all elements,
			//   though we want to remove each ID only as many times as it is locked by the task,
			//   which will be once, in practise.
//			lockedStudies.removeAll(taskLockProperties.getRequiredStudies());
//			lockedDataSets.removeAll(taskLockProperties.getRequiredDataSets());
			removeAllExactlyOnce(lockedStudies, taskLockProperties.getRequiredStudies());
			removeAllExactlyOnce(lockedDataSets, taskLockProperties.getRequiredDataSets());
			removeAllExactlyOnce(toBeRemovedStudies, taskLockProperties.getRemovingStudies());
			removeAllExactlyOnce(toBeRemovedDataSets, taskLockProperties.getRemovingDataSets());
		} finally {
			addRemoveLock.unlock();
		}
	}

	/**
	 * Check whether a certain tasks requirements are met.
	 * @param task to check for whether it can be done
	 * @return true if it can be done, false otherwise
	 */
	public boolean canBeDone(final Task task) {

		final TaskLockProperties lockProps = task.getTaskLockProperties();
		// check if all required things are present and not scheduled for removal
		for (final Identifier<StudyKey> requiredStudy : lockProps.getRequiredStudies()) {
			if (requiredStudy.isVirtual() || toBeRemovedStudies.contains(requiredStudy)) {
				return false;
			}
		}
		for (final Identifier<DataSetKey> requiredDataSet : lockProps.getRequiredDataSets()) {
			if (requiredDataSet.isVirtual() || toBeRemovedDataSets.contains(requiredDataSet)) {
				return false;
			}
		}

		// check if to-be-removed things are required or not yet/anymore present
		for (final Identifier<StudyKey> toBeRemovedStudy : lockProps.getRemovingStudies()) {
			if (toBeRemovedStudy.isVirtual() || lockedStudies.contains(toBeRemovedStudy)) {
				return false;
			}
		}
		for (final Identifier<DataSetKey> toBeRemovedDataSet : lockProps.getRemovingDataSets()) {
			if (toBeRemovedDataSet.isVirtual() || lockedDataSets.contains(toBeRemovedDataSet)) {
				return false;
			}
		}

		return true;
	}
}

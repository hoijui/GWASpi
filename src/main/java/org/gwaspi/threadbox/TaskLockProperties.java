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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.Identifier;
import org.gwaspi.model.StudyKey;

/**
 * Contains all the info that defines what things might be locked by a task.
 * Meaning, that while the task is running, it might alter these things,
 * and thus, other parts of the code, other threads, are not allowed
 * to change these things.
 */
public class TaskLockProperties {

	private final Set<Identifier<StudyKey>> requiredStudies;
	private final Set<Identifier<DataSetKey>> requiredDataSets;
	private final Collection<Identifier<StudyKey>> removingStudies;
	private final Collection<Identifier<DataSetKey>> removingDataSets;
	private final Collection<Identifier<StudyKey>> introducingStudies;
	private final Collection<Identifier<DataSetKey>> introducingDataSets;

	public TaskLockProperties() {

		this.requiredStudies = new HashSet<Identifier<StudyKey>>();
		this.requiredDataSets = new HashSet<Identifier<DataSetKey>>();
		this.removingStudies = new ArrayList<Identifier<StudyKey>>();
		this.removingDataSets = new ArrayList<Identifier<DataSetKey>>();
		this.introducingStudies = new ArrayList<Identifier<StudyKey>>();
		this.introducingDataSets = new ArrayList<Identifier<DataSetKey>>();
	}

	public Set<Identifier<StudyKey>> getRequiredStudies() {
		return Collections.unmodifiableSet(requiredStudies);
	}

	public Set<Identifier<DataSetKey>> getRequiredDataSets() {
		return Collections.unmodifiableSet(requiredDataSets);
	}

	public void addRequired(final StudyKey studyKey) {
		requiredStudies.add(studyKey);
	}

	public void addRequired(final DataSetKey dataSetKey) {

		requiredStudies.add(dataSetKey.getOrigin().getStudyKey());
		requiredDataSets.add(dataSetKey);
		if (dataSetKey.isOperation()) {
			// also add the origin matrix, if it is an operation
			requiredDataSets.add(new DataSetKey(dataSetKey.getOrigin()));
		}
	}

	public Collection<Identifier<StudyKey>> getRemovingStudies() {
		return Collections.unmodifiableCollection(removingStudies);
	}

	public Collection<Identifier<DataSetKey>> getRemovingDataSets() {
		return Collections.unmodifiableCollection(removingDataSets);
	}

	public void addRemoving(final StudyKey studyKey) {
		removingStudies.add(studyKey);
	}

	public void addRemoving(final DataSetKey dataSetKey) {
		removingDataSets.add(dataSetKey);
	}

	public Collection<Identifier<StudyKey>> getIntroducingStudies() {
		return Collections.unmodifiableCollection(introducingStudies);
	}

	public Collection<Identifier<DataSetKey>> getIntroducingDataSets() {
		return Collections.unmodifiableCollection(introducingDataSets);
	}

	public void addIntroducing(final StudyKey studyKey) {
		introducingStudies.add(studyKey);
	}

	public void addIntroducing(final DataSetKey dataSetKey) {
		introducingDataSets.add(dataSetKey);
	}
}

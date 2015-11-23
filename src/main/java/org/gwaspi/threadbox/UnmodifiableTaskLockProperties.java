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

import java.util.Collection;
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
public class UnmodifiableTaskLockProperties extends TaskLockProperties {

	private final TaskLockProperties internal;

	public UnmodifiableTaskLockProperties(final TaskLockProperties internal) {
		this.internal = internal;
	}

	@Override
	public Set<Identifier<StudyKey>> getRequiredStudies() {
		return internal.getRequiredStudies();
	}

	@Override
	public Set<Identifier<DataSetKey>> getRequiredDataSets() {
		return internal.getRequiredDataSets();
	}

	@Override
	public void addRequired(final StudyKey studyKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addRequired(final DataSetKey dataSetKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Identifier<StudyKey>> getRemovingStudies() {
		return internal.getRemovingStudies();
	}

	@Override
	public Collection<Identifier<DataSetKey>> getRemovingDataSets() {
		return internal.getRemovingDataSets();
	}

	@Override
	public void addRemoving(final StudyKey studyKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addRemoving(final DataSetKey dataSetKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Identifier<StudyKey>> getIntroducingStudies() {
		return internal.getIntroducingStudies();
	}

	@Override
	public Collection<Identifier<DataSetKey>> getIntroducingDataSets() {
		return internal.getIntroducingDataSets();
	}

	@Override
	public void addIntroducing(final StudyKey studyKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addIntroducing(final DataSetKey dataSetKey) {
		throw new UnsupportedOperationException();
	}
}
